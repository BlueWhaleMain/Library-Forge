package com.bluewhalemain.library.event;

import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.LogicalSide;

/**
 * 物品展示框滴答事件
 *
 * @param <T> 物品展示框类及其派生类
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public class ItemFrameTickEvent<T extends ItemFrameEntity> extends TickEvent.WorldTickEvent {
    public final T itemFrameEntity;

    public ItemFrameTickEvent(LogicalSide side, Phase phase, World world, T itemFrameEntity) {
        super(side, phase, world);
        this.itemFrameEntity = itemFrameEntity;
    }
}
