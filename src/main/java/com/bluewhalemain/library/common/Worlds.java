package com.bluewhalemain.library.common;

import com.bluewhalemain.library.common.item.IItemFrameTick;
import com.bluewhalemain.library.event.ItemFrameTickEvent;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;

/**
 * 世界相关工具类
 *
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public class Worlds {
    // 保留这个日志记录器，总有一天会有用
//    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * 注册
     */
    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(Worlds::onPostWorldTick);
    }

    private static void onPostWorldTick(TickEvent.WorldTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            if (e.side.isServer()) {
                ((ServerWorld) e.world).getEntities().forEach(entity -> {
                    // 物品展示框红石刻
                    if (entity instanceof ItemFrameEntity) {
                        ItemFrameEntity itemFrameEntity = (ItemFrameEntity) entity;
                        Item item = itemFrameEntity.getItem().getItem();
                        if (item instanceof IItemFrameTick) {
                            if (((IItemFrameTick) item).onItemFrameTick(new ItemFrameTickEvent<>(e.side, e.phase,
                                    e.world, itemFrameEntity))) {
                                itemFrameEntity.playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 1.0F,
                                        1.0F);
                            }
                        }
                    }
                });
            }
        }
    }
}
