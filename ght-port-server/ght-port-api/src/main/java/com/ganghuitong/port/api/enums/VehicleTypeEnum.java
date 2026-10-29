package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 货车车型枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum VehicleTypeEnum implements CodeEnum {

    /**
     * 轻型厢式货车
     */
    LIGHT_VAN("LIGHT_VAN", "轻型厢式货车"),

    /**
     * 中型厢式货车
     */
    MEDIUM_BOX("MEDIUM_BOX", "中型厢式货车"),

    /**
     * 重型栏板货车
     */
    HEAVY_CARGO("HEAVY_CARGO", "重型栏板货车"),

    /**
     * 半挂牵引车
     */
    SEMI_TRAILER("SEMI_TRAILER", "半挂牵引车"),

    /**
     * 自卸车
     */
    DUMP("DUMP", "自卸车"),

    /**
     * 罐式车
     */
    TANKER("TANKER", "罐式车"),

    /**
     * 平板车
     */
    FLATBED("FLATBED", "平板车"),

    /**
     * 集装箱车
     */
    CONTAINER("CONTAINER", "集装箱车");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
