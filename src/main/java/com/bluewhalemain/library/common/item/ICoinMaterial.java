package com.bluewhalemain.library.common.item;

/**
 * 硬币材质
 *
 * @author BlueWhaleMain
 * @version 2021/06/17
 * @since 2021/06/15
 */
public interface ICoinMaterial {
    /**
     * 获取材质名称
     *
     * @return 名称
     */
    String getName();

    /**
     * 能够被磁铁吸引
     *
     * @return 是否
     */
    boolean hasMagnetic();
}
