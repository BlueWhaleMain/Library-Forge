package com.bluewhalemain.library.common;

import com.bluewhalemain.library.Constants;
import com.bluewhalemain.library.utils.FileUtil;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * 战利品表相关工具类
 *
 * @author BlueWhaleMain
 * @see LootTableManager
 * @see ForgeHooks#loadLootTable
 * @since 2021/6/19
 */
public class LootTables {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ResourceLocation, JsonElement> tables = new HashMap<>();
    private static final Gson GSON = LootSerializers.createLootTableSerializer().create();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * 加载战利品表 仅服务器启动调用
     *
     * @param server 服务器对象
     */
    public static void loadLootTables(MinecraftServer server) {
        try {
            replaceLootTable(server.getLootTables());
            tables.clear();
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            LOGGER.warn("战利品表替换失败");
        }
    }

    private static void replaceLootTable(LootTableManager lootTableManager) throws IllegalAccessException {
        Field[] fields = lootTableManager.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(Map.class)) {
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                    if (actualTypeArguments.length == 2 && actualTypeArguments[0].getTypeName().equals(
                            ResourceLocation.class.getTypeName()) && actualTypeArguments[1].getTypeName().equals(
                            LootTable.class.getTypeName())) {
                        field.setAccessible(true);
                        ImmutableMap<?, ?> source = ((ImmutableMap<?, ?>) field.get(lootTableManager));
                        Map<ResourceLocation, LootTable> lootTableMap = new HashMap<>();
                        for (Map.Entry<?, ?> entry : source.entrySet()) {
                            lootTableMap.put((ResourceLocation) entry.getKey(), (LootTable) entry.getValue());
                        }
                        for (Map.Entry<ResourceLocation, JsonElement> entry : tables.entrySet()) {
                            lootTableMap.put(entry.getKey(), ForgeHooks.loadLootTable(GSON, entry.getKey(),
                                    entry.getValue(), true, lootTableManager));
                        }
                        field.set(lootTableManager, ImmutableMap.copyOf(lootTableMap));
                        field.setAccessible(false);
                        LOGGER.info(String.format("替换了%d个战利品表", tables.size()));
                        break;
                    }
                }
            }
        }
    }

    static void accept(ResourceLoader.Resource resource) {
        Matcher matcher = Constants.Regex.minecraftLootTablesPatten.matcher(resource.getName());
        if (matcher.find()) {
            String resName = matcher.group(1);
            matcher = Constants.Regex.minecraftLootTableNamePatten.matcher(resName);
            if (matcher.matches()) {
                try {
                    JsonElement jsonElement = JSONUtils.fromJson(gson, FileUtil.readString(resource.getInputStream()),
                            JsonElement.class);
                    assert jsonElement != null;
                    tables.put(new ResourceLocation(String.format("%s%s%s",
                            Constants.Minecraft.id, Constants.Symbol.resourceSpit, matcher.group(1)
                                    .replaceAll("\\\\", "/"))), jsonElement);
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                    LOGGER.warn(String.format("读取失败：%s", resName));
                }
            }
        }
    }
}
