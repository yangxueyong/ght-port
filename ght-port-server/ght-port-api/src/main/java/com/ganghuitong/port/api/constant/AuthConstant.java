package com.ganghuitong.port.api.constant;

/**
 * 鉴权相关常量
 *
 * @author yangxueyong
 */
public final class AuthConstant {

    /**
     * 请求头中的令牌字段
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * JWT载荷：用户ID
     */
    public static final String CLAIM_USER_ID = "uid";

    /**
     * JWT载荷：租户ID
     */
    public static final String CLAIM_TENANT_ID = "tid";

    /**
     * JWT载荷：过期时间
     */
    public static final String CLAIM_EXPIRE = "exp";

    /**
     * 登录用户缓存时长（毫秒），过期后从数据库重新加载权限
     */
    public static final long LOGIN_CACHE_TIMEOUT = 10 * 60 * 1000L;

    /**
     * 私有构造，禁止实例化
     */
    private AuthConstant() {
    }
}
