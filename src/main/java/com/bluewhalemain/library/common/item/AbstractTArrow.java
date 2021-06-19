package com.bluewhalemain.library.common.item;

import com.bluewhalemain.library.event.ItemFrameTickEvent;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.ArrowItem;

/**
 * 滴答箭基类
 *
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public abstract class AbstractTArrow extends ArrowItem implements IItemFrameTick {
    protected int tickMax = 1;
    private int tick = 0;

    public AbstractTArrow(Properties properties) {
        super(properties);
    }

    public <T extends ItemFrameEntity> boolean onItemFrameTick(ItemFrameTickEvent<T> event) {
        tick++;
        if (tick % tickMax == 0) {
            T itemFrameEntity = event.itemFrameEntity;
            itemFrameEntity.setRotation(itemFrameEntity.getRotation() + 1);
            tick = 0;
            return true;
        }
        return false;
    }
}
