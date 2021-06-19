package com.bluewhalemain.library.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

/**
 * 一本书
 *
 * @author BlueWhaleMain
 * @version 2021/06/19
 * @since 2021/06/15
 */
public class SysBookItem extends Item {
    public SysBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        return ActionResultType.CONSUME;
    }
}
