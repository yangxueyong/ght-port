package com.ganghuitong.port.api.constant;

/**
 * 系统通用常量
 *
 * @author yangxueyong
 */
public final class CommonConstant {

    /**
     * 平台租户ID（超级管理员所在租户，也是内置角色模板所在租户）
     */
    public static final String PLATFORM_TENANT_ID = "000000";

    /**
     * 新建账号的默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";

    /**
     * 未登录时的操作人
     */
    public static final String SYSTEM_USER = "system";

    /**
     * 树形结构根节点父ID
     */
    public static final String ROOT_PARENT_ID = "0";

    /**
     * 拥有全部权限的标识
     */
    public static final String ALL_PERMISSION = "*:*:*";

    /**
     * 是：1
     */
    public static final Integer YES = 1;

    /**
     * 否：0
     */
    public static final Integer NO = 0;

    /**
     * 下拉搜索默认返回条数上限
     */
    public static final Integer SELECT_LIMIT = 50;

    /**
     * SQL 仅取一条的语句片段
     */
    public static final String LIMIT_ONE = "limit 1";

    /**
     * SQL 取前N条的语句片段
     */
    public static final String LIMIT_SQL = "limit ";

    /**
     * 私有构造，禁止实例化
     */
    private CommonConstant() {
    }
}
