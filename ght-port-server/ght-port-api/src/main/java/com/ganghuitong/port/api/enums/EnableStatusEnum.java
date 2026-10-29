package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 启用状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum EnableStatusEnum implements CodeEnum {

    /**
     * 启用
     */
    ENABLED("ENABLED", "启用"),

    /**
     * 停用
     */
    DISABLED("DISABLED", "停用");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
