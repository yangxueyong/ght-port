package com.ganghuitong.port.api.context;

/**
 * 当前线程租户上下文，优先级高于登录用户中的租户ID（用于登录、公开链接等未登录场景）
 *
 * @author yangxueyong
 */
public final class TenantContext {

    /**
     * 当前线程租户ID
     */
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造，禁止实例化
     */
    private TenantContext() {
    }

    /**
     * 设置当前线程租户ID
     *
     * @param tenantId 租户ID
     */
    public static void set(String tenantId) {
        HOLDER.set(tenantId);
    }

    /**
     * 获取当前生效的租户ID：优先取显式设置值，其次取登录用户的租户
     *
     * @return 租户ID，无法确定时返回null
     */
    public static String get() {
        String tenantId = HOLDER.get();
        if (tenantId != null) {
            return tenantId;
        }
        LoginUser user = UserContext.get();
        return user == null ? null : user.getTenantId();
    }

    /**
     * 清理当前线程租户ID
     */
    public static void clear() {
        HOLDER.remove();
    }
}
