package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 泊位到货状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum CargoStatusEnum implements CodeEnum {

    /**
     * 作业中
     */
    IN_PROGRESS("IN_PROGRESS", "作业中"),

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
