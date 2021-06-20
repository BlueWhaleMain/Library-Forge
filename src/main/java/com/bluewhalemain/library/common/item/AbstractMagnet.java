package com.bluewhalemain.library.common.item;

import com.bluewhalemain.library.event.ItemFrameTickEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;

public abstract class AbstractMagnet extends Item implements IItemFrameTick {
    public AbstractMagnet(Properties properties) {
        super(properties);
    }

    protected boolean forceEntity(Vector3d pos, Entity entity) {
        return false;
    }

    @Override
    public <T extends ItemFrameEntity> boolean onItemFrameTick(ItemFrameTickEvent<T> event) {
        if (event.side.isServer()) {
            Iterator<Entity> iterator = ((ServerWorld) event.world).getEntities().iterator();
            while (iterator.hasNext()) {
                Entity entity = iterator.next();
                if (!forceEntity(event.itemFrameEntity.position(), entity)) {
                    break;
                }
            }
        }
        return false;
    }
}
