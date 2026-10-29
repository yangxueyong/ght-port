package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出门条状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum GatePassStatusEnum implements CodeEnum {

    /**
     * 未核销
     */
    UNUSED("UNUSED", "未核销"),

    /**
     * 已核销
     */
    VERIFIED("VERIFIED", "已核销");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
