package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 货物作业方向枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum CargoDirectionEnum implements CodeEnum {

    /**
     * 到货：货物从船上卸到泊位，再由货车运出
     */
    UNLOAD("UNLOAD", "到货卸船"),

    /**
     * 装货：货物在泊位装到船上
     */
    LOAD("LOAD", "装货上船");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
