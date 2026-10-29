package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 运输单状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum TripStatusEnum implements CodeEnum {

    /**
     * 运输中
     */
    IN_TRANSIT("IN_TRANSIT", "运输中"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
