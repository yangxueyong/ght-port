package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 货物形态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum PackTypeEnum implements CodeEnum {

    /**
     * 集装箱
     */
    CONTAINER("CONTAINER", "集装箱"),

    /**
     * 散货
     */
    BULK("BULK", "散货");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
