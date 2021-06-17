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
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

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

    /**
     * 禁用合成表 仅服务器启动调用
     *
     * @param server 服务器对象
     */
    public static void disableRecipes(MinecraftServer server) {
        RecipeManager recipeManager = server.getRecipeManager();
        Collection<IRecipe<?>> recipeCollection = recipeManager.getRecipes();

        recipeCollection.removeIf(recipe -> CommonConfig.COMMON.removedRecipes.get()
                .contains(recipe.getId().toString()));
        try {
            ResourceLoader.loader(resName -> {
                if (StringUtils.isBlank(resName)) {
                    return;
                }
                if (Constants.Regex.minecraftRecipesPatten.matcher(resName).find()) {
                    Matcher matcher = Constants.Regex.fileNamePatten.matcher(resName);
                    if (matcher.matches()) {
                        try {
                            String name = (String.format("%s%s%s",
                                    Constants.Minecraft.id, Constants.Symbol.resourceSpit, matcher.group(1)));
                            InputStream inputStream = Recipes.class.getClassLoader().getResourceAsStream(resName);
                            assert inputStream != null;
                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line);
                                }
                                JsonElement jsonElement = JSONUtils.fromJson(gson, sb.toString(), JsonElement.class);
                                assert jsonElement != null;
                                IRecipe<?> newRecipe = RecipeManager.fromJson(new ResourceLocation(name),
                                        JSONUtils.convertToJsonObject(jsonElement, Constants.Symbol.recipeConvent));
                                recipeCollection.removeIf(recipe -> name.equals(recipe.getId().toString()));
                                // 确保新的配方已经成功加载才执行替换
                                recipeCollection.add(newRecipe);
                            }
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                            LOGGER.warn(String.format("读取失败：%s", resName));
                        }
                    }
                }
            });
            Recipes.replaceRecipes(recipeManager, recipeCollection);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.warn("Minecraft合成表替换失败");
        }
    }

    /**
     * 替换全部合成表，使用反射
     * 实现合成表的增加删除修改
     * 原生的方法仅在物理客户端中存在
     *
     * @param recipeManager 合成表管理器
     * @param recipes       所有的合成表（注意是全部）
     * @throws NoSuchFieldException   合成表管理器没有recipes属性
     * @throws IllegalAccessException 合成表管理器无法访问
     */
    public static void replaceRecipes(RecipeManager recipeManager, Iterable<IRecipe<?>> recipes)
            throws NoSuchFieldException, IllegalAccessException {
        Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> map = Maps.newHashMap();
        recipes.forEach((recipe) -> {
            Map<ResourceLocation, IRecipe<?>> resourceLocationIRecipeMap = map.computeIfAbsent(recipe.getType(),
                    (p_223390_0_) -> Maps.newHashMap());
            IRecipe<?> iRecipe = resourceLocationIRecipeMap.put(recipe.getId(), recipe);
            if (iRecipe != null) {
                throw new IllegalStateException("Duplicate recipe ignored with ID " + recipe.getId());
            }
        });
        // TODO 修改为类型注入 该方法在生产环境无效 字段被混淆
        // class.getDeclaredFields()
        Field field = recipeManager.getClass().getDeclaredField("recipes");
        field.setAccessible(true);
        field.set(recipeManager, ImmutableMap.copyOf(map));
        field.setAccessible(false);
    }

    /**
     * 消费者函数接口
     */
    @FunctionalInterface
    interface Consumer {
        void consume(String resName);
    }

    /**
     * 资源加载器-原生 支持文件夹和jar
     */
    public static class ResourceLoader {
        private static void push(Consumer consumer, File[] files) {
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        push(consumer, file.listFiles());
                    } else if (file.isFile()) {
                        consumer.consume(file.getAbsolutePath());
                    }
                }
            }
        }

        /**
         * 读取所有路径
         *
         * @param consumer 传入消费者
         * @throws IOException 读写异常
         */
        public static void loader(Consumer consumer) throws IOException {
            URL url = ResourceLoader.class.getClassLoader().getResource("pack.mcmeta");
            assert url != null;
            String urlStr = url.toString();
            if (urlStr.contains("!")) {
                // 找到!/ 截断之前的字符串
                try (JarFile jarFile = ((JarURLConnection) new URL(urlStr.substring(0, urlStr.indexOf("!/") + 2))
                        .openConnection()).getJarFile()) {
                    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                    while (jarEntryEnumeration.hasMoreElements()) {
                        JarEntry jarEntry = jarEntryEnumeration.nextElement();
                        String entryName = jarEntry.getName();
                        if (!jarEntry.isDirectory()) {
                            consumer.consume(entryName);
                        }
                    }
                }
            } else {
                Matcher matcher = Constants.Regex.folderPatten.matcher(urlStr);
                if (matcher.find()) {
                    push(consumer, new File(matcher.group(1)).listFiles());
                }
            }
        }
    }
}
