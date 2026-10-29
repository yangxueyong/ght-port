package com.ganghuitong.port.api.context;

import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.exception.CustException;

/**
 * 当前线程登录用户上下文
 *
 * @author yangxueyong
 */
public final class UserContext {

    /**
     * 当前线程登录用户
     */
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造，禁止实例化
     */
    private UserContext() {
    }

    /**
     * 设置当前登录用户
     *
     * @param user 登录用户
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前登录用户，可能为空
     *
     * @return 登录用户或null
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 获取当前登录用户，未登录时抛出异常
     *
     * @return 登录用户
     */
    public static LoginUser require() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new CustException("用户未登录或登录已过期");
        }
        return user;
    }

    /**
     * 获取当前操作人账号，未登录时返回系统账号
     *
     * @return 操作人账号
     */
    public static String getUsername() {
        LoginUser user = HOLDER.get();
        if (user == null || StrUtil.isBlank(user.getUsername())) {
            return CommonConstant.SYSTEM_USER;
        }
        return user.getUsername();
    }

    /**
     * 清理当前线程用户，防止线程复用导致数据串用
     */
    public static void clear() {
        HOLDER.remove();
    }
}
