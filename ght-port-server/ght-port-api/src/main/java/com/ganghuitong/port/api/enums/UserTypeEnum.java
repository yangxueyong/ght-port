package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum implements CodeEnum {

    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员"),

    /**
     * 港口员工
     */
    STAFF("STAFF", "港口员工"),

    /**
     * 承运商
     */
    CARRIER("CARRIER", "承运商"),

    /**
     * 司机
     */
    DRIVER("DRIVER", "司机"),

    /**
     * 门卫
     */
    GUARD("GUARD", "门卫");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
