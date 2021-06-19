package com.bluewhalemain.library.common.item;

import com.bluewhalemain.library.event.ItemFrameTickEvent;
import net.minecraft.entity.item.ItemFrameEntity;

/**
 * 物品展示框滴答接口
 *
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public interface IItemFrameTick {
    /**
     * 物品展示框滴答
     *
     * @param event 事件对象
     * @param <T>   物品展示框或其派生类
     * @return 是否转动了展示框
     */
    <T extends ItemFrameEntity> boolean onItemFrameTick(ItemFrameTickEvent<T> event);
}
