package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 船舶类型枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum ShipTypeEnum implements CodeEnum {

    /**
     * 散货船
     */
    BULK("BULK", "散货船"),

    /**
     * 集装箱船
     */
    CONTAINER("CONTAINER", "集装箱船"),

    /**
     * 油船
     */
    TANKER("TANKER", "油船"),

    /**
     * 杂货船
     */
    GENERAL("GENERAL", "杂货船"),

    /**
     * 滚装船
     */
    RORO("RORO", "滚装船"),

    /**
     * 驳船
     */
    BARGE("BARGE", "驳船");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
