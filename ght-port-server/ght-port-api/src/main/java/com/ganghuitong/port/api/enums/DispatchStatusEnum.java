package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分派承运状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum DispatchStatusEnum implements CodeEnum {

    /**
     * 待装载
     */
    WAITING("WAITING", "待装载"),

    /**
     * 已出发
     */
    DEPARTED("DEPARTED", "已出发"),

    /**
     * 已取消
     */
    CANCELLED("CANCELLED", "已取消");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
