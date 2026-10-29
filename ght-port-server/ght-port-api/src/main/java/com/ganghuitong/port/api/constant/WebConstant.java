package com.ganghuitong.port.api.constant;

/**
 * Web 层配置常量
 *
 * @author yangxueyong
 */
public final class WebConstant {

    /**
     * JSON 日期时间格式
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 跨域允许的所有来源/方法/请求头
     */
    public static final String CORS_ALL = "*";

    /**
     * 匹配全部路径
     */
    public static final String ALL_PATH = "/**";

    /**
     * 跨域预检缓存秒数
     */
    public static final long CORS_MAX_AGE = 3600L;

    /**
     * 私有构造，禁止实例化
     */
    private WebConstant() {
    }
}
