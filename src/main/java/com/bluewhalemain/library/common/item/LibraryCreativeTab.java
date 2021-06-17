package com.bluewhalemain.library.common.item;

import com.bluewhalemain.library.Constants;
import com.bluewhalemain.library.common.Items;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * 创造物品栏
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/15
 */
public class LibraryCreativeTab extends ItemGroup {
    private static final LibraryCreativeTab instance = new LibraryCreativeTab();

    private LibraryCreativeTab() {
        super(Constants.Library.id);
    }

    /**
     * 获取实例
     */
    public static LibraryCreativeTab getInstance() {
        return instance;
    }

    @Override
    // ide存在奇怪的空指针检查
    @SuppressWarnings("NullableProblems")
    public ItemStack makeIcon() {
        return new ItemStack(Items.sysBook);
    }
}
