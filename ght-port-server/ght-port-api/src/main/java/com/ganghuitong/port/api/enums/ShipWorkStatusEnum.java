package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 船只卸货状态枚举（到货与船只关联）
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum ShipWorkStatusEnum implements CodeEnum {

    /**
     * 卸货中
     */
    WORKING("WORKING", "卸货中"),

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
