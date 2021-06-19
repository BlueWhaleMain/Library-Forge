package com.bluewhalemain.library;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * 物理端通用配置
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/14
 */
public class CommonConfig {
    static final Pair<CommonConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
    /**
     * 注册
     */
    public static final ForgeConfigSpec COMMON_SPEC = pair.getRight();
    /**
     * 使用
     */
    public static final CommonConfig COMMON = pair.getLeft();
    public final ForgeConfigSpec.ConfigValue<List<String>> removedRecipes;
    public final ForgeConfigSpec.ConfigValue<RecipeLoadingMethod> recipeLoadingMethod;

    private CommonConfig(ForgeConfigSpec.Builder builder) {
        builder.push("recipes");
        removedRecipes = builder.translation("config.library.remove_recipe").define("removed", Arrays.asList(
                "minecraft:bone_block",
                "minecraft:redstone_block",
                "minecraft:diamond_block",
                "minecraft:coal_from_blasting",
                "minecraft:coal_from_smelting",
                "minecraft:coal_block",
                "minecraft:diamond_from_blasting",
                "minecraft:diamond_from_smelting",
                "minecraft:emerald_block",
                "minecraft:quartz",
                "minecraft:quartz_block",
                "minecraft:quartz_from_blasting",
                "minecraft:lapis_block",
                "minecraft:glowstone",
                "minecraft:clay",
                "minecraft:snow_block",
                "minecraft:white_wool_from_string",
                "minecraft:dried_kelp_block",
                "minecraft:slime_block",
                "minecraft:wheat",
                "minecraft:hay_block"
        ));
        recipeLoadingMethod = builder.translation("config.library.recipe_loading_method").define("loading_method",
                RecipeLoadingMethod.SAFE);
        builder.pop();
    }

    public enum RecipeLoadingMethod {
        /**
         * 不加载
         */
        NO,
        /**
         * 泛型检查安全加载
         */
        SAFE,
        /**
         * 跳过检查强制加载
         */
        FORCE
    }
}
