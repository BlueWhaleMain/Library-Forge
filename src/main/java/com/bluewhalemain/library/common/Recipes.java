package com.bluewhalemain.library.common;

import com.bluewhalemain.library.CommonConfig;
import com.bluewhalemain.library.Constants;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 合成表相关工具类
 *
 * @author BlueWhaleMain
 * @version 2021/6/17
 * @see RecipeManager
 * @since 2021/6/14
 */
public final class Recipes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<IRecipe<?>> replaceRecipes = new ArrayList<>();

    static {
        try {
            ResourceLoader.loader(resource -> {
                Matcher matcher = Constants.Regex.minecraftRecipesPatten.matcher(resource.name);
                if (matcher.find()) {
                    String resName = matcher.group(1);
                    matcher = Constants.Regex.fileNamePatten.matcher(resName);
                    if (matcher.matches()) {
                        try {
                            String name = (String.format("%s%s%s",
                                    Constants.Minecraft.id, Constants.Symbol.resourceSpit, matcher.group(1)));
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.inputStream))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line);
                                }
                                JsonElement jsonElement = JSONUtils.fromJson(gson, sb.toString(), JsonElement.class);
                                assert jsonElement != null;
                                IRecipe<?> newRecipe = RecipeManager.fromJson(new ResourceLocation(name),
                                        JSONUtils.convertToJsonObject(jsonElement, Constants.Symbol.recipeConvent));
                                replaceRecipes.add(newRecipe);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getLocalizedMessage(), e);
                            LOGGER.warn(String.format("读取失败：%s", resName));
                        }
                    }
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("读取自身文件失败", e);
        }
    }

    /**
     * 禁用合成表 仅服务器启动调用
     *
     * @param server 服务器对象
     */
    public static void disableRecipes(MinecraftServer server) {
        if (CommonConfig.RecipeLoadingMethod.NO.equals(CommonConfig.COMMON.recipeLoadingMethod.get())) {
            return;
        }
        RecipeManager recipeManager = server.getRecipeManager();
        Collection<IRecipe<?>> recipeCollection = recipeManager.getRecipes();

        recipeCollection.removeIf(recipe -> CommonConfig.COMMON.removedRecipes.get()
                .contains(recipe.getId().toString()));
        recipeCollection.removeIf(recipe -> replaceRecipes.stream().anyMatch(iRecipe
                -> recipe.getId().equals(iRecipe.getId())));
        recipeCollection.addAll(replaceRecipes);
        try {
            Recipes.replaceRecipes(recipeManager, recipeCollection);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            LOGGER.warn("合成表替换失败");
        }
    }

    /**
     * 替换全部合成表，使用反射
     * 实现合成表的增加删除修改
     * 原生的方法仅在物理客户端中存在
     *
     * @param recipeManager 合成表管理器
     * @param recipes       所有的合成表（注意是全部）
     * @throws IllegalAccessException 合成表管理器无法访问
     */
    public static void replaceRecipes(RecipeManager recipeManager, Iterable<IRecipe<?>> recipes)
            throws IllegalAccessException {
        Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> map = Maps.newHashMap();
        recipes.forEach((recipe) -> {
            Map<ResourceLocation, IRecipe<?>> resourceLocationIRecipeMap = map.computeIfAbsent(recipe.getType(),
                    (recipeType) -> Maps.newHashMap());
            IRecipe<?> iRecipe = resourceLocationIRecipeMap.put(recipe.getId(), recipe);
            if (iRecipe != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
            }
        });
        Field[] fields = recipeManager.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(Map.class)) {
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                    if (CommonConfig.RecipeLoadingMethod.SAFE.equals(CommonConfig.COMMON.recipeLoadingMethod.get())) {
                        if (actualTypeArguments.length != 2) {
                            break;
                        }
                        String[] conditions = {"net.minecraft.item.crafting.IRecipeType<?>",
                                "java.util.Map<net.minecraft.util.ResourceLocation, net.minecraft.item.crafting.IRecipe<?>>"};
                        for (int i = 0; i < actualTypeArguments.length; i++) {
                            if (!conditions[i].equals(actualTypeArguments[i].getTypeName())) {
                                break;
                            }
                        }
                    }
                    field.setAccessible(true);
                    field.set(recipeManager, ImmutableMap.copyOf(map));
                    field.setAccessible(false);
                    LOGGER.info(String.format("替换了%d个配方", map.size()));
                    break;
                }
            }
        }
    }

    /**
     * 资源类
     */
    static class Resource {
        /**
         * 文件名
         */
        private final String name;
        /**
         * 文件流
         */
        private final InputStream inputStream;

        Resource(String name, InputStream inputStream) {
            this.name = name;
            this.inputStream = inputStream;
        }
    }

    /**
     * 资源加载器-原生 支持文件夹和jar
     */
    public static class ResourceLoader {
        private static void push(Consumer<Resource> consumer, File[] files) throws FileNotFoundException {
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        push(consumer, file.listFiles());
                    } else if (file.isFile()) {
                        consumer.accept(new Resource(file.getAbsolutePath(),
                                new FileInputStream(file.getAbsolutePath())));
                    }
                }
            }
        }

        /**
         * 读取所有文件
         *
         * @param consumer 传入消费者
         */
        public static void loader(Consumer<Resource> consumer) throws IOException {
            String beacon = "pack.mcmeta";
            Pattern folderPatten = Pattern.compile("file:/(.*)/" + beacon);
            URL url = ResourceLoader.class.getClassLoader().getResource(beacon);
            assert url != null;
            String urlString = url.toString();
            Matcher matcher = folderPatten.matcher(urlString);
            if (urlString.contains("!")) {
                List<ModInfo> mods = ModList.get().getMods();
                for (ModInfo mod : mods) {
                    if (mod.getModId().equals(Constants.Library.id)) {
                        ModFileInfo modFileInfo = mod.getOwningFile();
                        JarFile jarFile = new JarFile(modFileInfo.getFile().getFilePath().toString());
                        Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                        while (jarEntryEnumeration.hasMoreElements()) {
                            JarEntry jarEntry = jarEntryEnumeration.nextElement();
                            if (!jarEntry.isDirectory()) {
                                consumer.accept(new Resource(jarEntry.getName(), jarFile.getInputStream(jarEntry)));
                            }
                        }
                        break;
                    }
                }
            } else if (matcher.find()) {
                push(consumer, new File(matcher.group(1)).listFiles());
            } else {
                throw new IllegalStateException("无法加载资源");
            }
        }
    }
}
