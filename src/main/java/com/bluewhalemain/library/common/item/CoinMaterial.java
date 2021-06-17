package com.bluewhalemain.library.common.item;

/**
 * 硬币材质枚举
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/15
 */
public enum CoinMaterial implements ICoinMaterial {
    /**
     * 金
     */
    GOLD("gold", false);
    private final String name;
    private final boolean magnetic;

    CoinMaterial(String name, boolean magnetic) {
        this.name = name;
        this.magnetic = magnetic;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean hasMagnetic() {
        return this.magnetic;
    }
}
