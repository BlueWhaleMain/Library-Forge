package com.bluewhalemain.library.common.item;

import net.minecraft.item.Item;

/**
 * 硬币物品
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/15
 */
public class CoinItem extends Item {
    private final ICoinMaterial material;

    public CoinItem(ICoinMaterial material, Properties properties) {
        super(properties);
        this.material = material;
    }

    /**
     * 获取材质
     *
     * @return 材质
     */
    public ICoinMaterial getMaterial() {
        return material;
    }
}
