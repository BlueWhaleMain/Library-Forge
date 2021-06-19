package com.bluewhalemain.library;

import net.minecraft.util.JSONUtils;

import java.util.regex.Pattern;

/**
 * 常量类
 * 建议不要使用字面量，发生错误很难调试
 *
 * @author BlueWhaleMain
 * @version 2021/6/17
 * @since 2021/6/15
 */
public final class Constants {
    /**
     * Library使用的元数据
     *
     * @see com.bluewhalemain.library.Library
     */
    public static class Library {
        /**
         * Mod ID
         */
        public static final String id = "library";
    }

    /**
     * Minecraft使用的元数据
     *
     * @see net.minecraft.client.Minecraft
     */
    public static class Minecraft {
        /**
         * Mod ID
         */
        public static final String id = "minecraft";
    }

    /**
     * 符号
     */
    public static class Symbol {
        /**
         * 资源分割符
         */
        public static final String resourceSpit = ":";
        /**
         * 压缩包分割符
         */
        public static final String packSpit = "!";
        /**
         * 合成表载入JsonObject
         *
         * @see JSONUtils#convertToJsonObject
         */
        public static final String recipeConvent = "top element";
    }

    /**
     * 正则表达式
     */
    public static class Regex {
        public static final Pattern minecraftRecipesPatten = Pattern.compile(".*(data.minecraft.recipes.*\\.json)");
        public static final Pattern fileNamePatten = Pattern.compile(".*[\\\\/]([^.]*).*");
    }
}
