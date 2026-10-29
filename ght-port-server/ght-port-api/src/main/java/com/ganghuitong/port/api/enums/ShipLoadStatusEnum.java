package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 装船分派状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum ShipLoadStatusEnum implements CodeEnum {

    /**
     * 待装船
     */
    WAITING("WAITING", "待装船"),

    /**
     * 已装船
     */
    LOADED("LOADED", "已装船"),

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
