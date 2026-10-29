package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 车辆运输状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum VehicleStatusEnum implements CodeEnum {

    /**
     * 空闲
     */
    IDLE("IDLE", "空闲"),

    /**
     * 运输中
     */
    IN_TRANSIT("IN_TRANSIT", "运输中");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
