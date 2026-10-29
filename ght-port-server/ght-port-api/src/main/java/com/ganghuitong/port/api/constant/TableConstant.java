package com.ganghuitong.port.api.constant;

import java.util.Set;

/**
 * 数据表相关常量
 *
 * @author yangxueyong
 */
public final class TableConstant {

    /**
     * 租户字段名
     */
    public static final String TENANT_COLUMN = "tenant_id";

    /**
     * 不做租户隔离的表（全局共享）
     */
    public static final Set<String> TENANT_IGNORE_TABLES = Set.of("sys_tenant", "sys_menu");

    /**
     * 私有构造，禁止实例化
     */
    private TableConstant() {
    }
}
