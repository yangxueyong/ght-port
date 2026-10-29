package com.ganghuitong.port.api.constant;

/**
 * 接口返回码常量
 *
 * @author yangxueyong
 */
public final class ResultCode {

    /**
     * 成功
     */
    public static final Integer SUCCESS = 200;

    /**
     * 成功提示
     */
    public static final String SUCCESS_MSG = "操作成功";

    /**
     * 未登录或登录过期
     */
    public static final Integer UNAUTHORIZED = 401;

    /**
     * 无权限
     */
    public static final Integer FORBIDDEN = 403;

    /**
     * 业务失败
     */
    public static final Integer FAIL = 500;

    /**
     * 私有构造，禁止实例化
     */
    private ResultCode() {
    }
}
