package com.bluewhalemain.library.common.item;

import com.bluewhalemain.library.event.ItemFrameTickEvent;
import net.minecraft.block.Block;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

/**
 * 物品磁铁
 *
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public class ItemMagnetItem extends Item implements IForce, IItemFrameTick {
    protected Vector3d pos = null;

    public ItemMagnetItem(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends Entity> void forceEntity(T entity) {
        if (entity instanceof ItemEntity) {
            entity.lookAt(EntityAnchorArgument.Type.EYES, pos);
            Vector3d move = entity.getDeltaMovement();
            Vector3d position = entity.position();
            entity.setDeltaMovement(new Vector3d(pos.x + move.x - position.x,
                    pos.y + move.y - position.y,
                    pos.z + move.z - position.z));
        }
    }

    @Override
    public <T extends Block> void forceBlock(T block) {

    }

    @Override
    public <T extends ItemFrameEntity> boolean onItemFrameTick(ItemFrameTickEvent<T> event) {
        pos = event.itemFrameEntity.position();
        if (event.side.isServer()) {
            ((ServerWorld) event.world).getEntities().forEach(entity -> {
                if (entity.position().distanceTo(event.itemFrameEntity.position()) < 32) {
                    forceEntity(entity);
                }
            });
        }
        return false;
    }
}
