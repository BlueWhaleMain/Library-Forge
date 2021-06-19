package com.bluewhalemain.library.common.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;

/**
 * 泛力接口
 *
 * @author BlueWhaleMain
 * @since 2021/06/19
 */
public interface IForce {
    /**
     * 对实体产生作用力
     *
     * @param entity 实体
     * @param <T>    实体及其派生类
     */
    <T extends Entity> void forceEntity(T entity);

    /**
     * 对方块产生作用力
     *
     * @param block 方块
     * @param <T>   方块及其派生类
     */
    <T extends Block> void forceBlock(T block);
}
