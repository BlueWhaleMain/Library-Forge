package com.bluewhalemain.library.common;

import com.bluewhalemain.library.common.item.CoinItem;
import com.bluewhalemain.library.common.item.CoinMaterial;
import com.bluewhalemain.library.common.item.LibraryCreativeTab;
import com.bluewhalemain.library.common.item.SysBookItem;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * 物品注册工具类
 * 所有物品必须在此注册
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/14
 */
public final class Items {
    public static final SysBookItem sysBook = new SysBookItem(new Item.Properties().stacksTo(1)
            .tab(LibraryCreativeTab.getInstance()));
    public static final CoinItem gold = new CoinItem(CoinMaterial.GOLD, new Item.Properties()
            .tab(LibraryCreativeTab.getInstance()));

    /**
     * 注册物品
     */
    public static void registerItems(RegistryEvent.Register<Item> e) {
        IForgeRegistry<Item> itemIForgeRegistry = e.getRegistry();
        itemIForgeRegistry.registerAll(
                sysBook.setRegistryName("sys_book"),
                gold.setRegistryName("gold")
        );
    }
}
