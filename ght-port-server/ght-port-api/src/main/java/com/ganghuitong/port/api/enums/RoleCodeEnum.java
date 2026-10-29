package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 内置角色编码枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum RoleCodeEnum implements CodeEnum {

    /**
     * 超级管理员
     */
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),

    /**
     * 租户管理员
     */
    TENANT_ADMIN("TENANT_ADMIN", "租户管理员"),

    /**
     * 港口调度员
     */
    PORT_OPERATOR("PORT_OPERATOR", "港口调度员"),

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
