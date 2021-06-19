package com.bluewhalemain.library.common;

import com.bluewhalemain.library.CommonConfig;
import com.bluewhalemain.library.Constants;
import com.bluewhalemain.library.utils.FileUtil;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 合成表相关工具类
 *
 * @author BlueWhaleMain
 * @version 2021/6/19
 * @see RecipeManager
 * @since 2021/6/14
 */
public final class Recipes {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<IRecipe<?>> replaceRecipes = new ArrayList<>();

    /**
     * 加载合成表 仅服务器启动调用
     *
     * @param server 服务器对象
     */
    public static void loadRecipes(MinecraftServer server) {
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
            replaceRecipes(recipeManager, recipeCollection);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            LOGGER.warn("合成表替换失败");
        }
        replaceRecipes.clear();
    }

    private static void replaceRecipes(RecipeManager recipeManager, Iterable<IRecipe<?>> recipes)
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

    static void accept(ResourceLoader.Resource resource) {
        Matcher matcher = Constants.Regex.minecraftRecipesPatten.matcher(resource.getName());
        if (matcher.find()) {
            String resName = matcher.group(1);
            matcher = Constants.Regex.fileNamePatten.matcher(resName);
            if (matcher.matches()) {
                try {
                    JsonElement jsonElement = JSONUtils.fromJson(gson, FileUtil.readString(resource.getInputStream()),
                            JsonElement.class);
                    assert jsonElement != null;
                    IRecipe<?> newRecipe = RecipeManager.fromJson(new ResourceLocation(String.format("%s%s%s",
                            Constants.Minecraft.id, Constants.Symbol.resourceSpit, matcher.group(1))),
                            JSONUtils.convertToJsonObject(jsonElement, Constants.Symbol.recipeConvent));
                    replaceRecipes.add(newRecipe);
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    LOGGER.warn(String.format("读取失败：%s", resName));
                }
            }
        }
    }
}
