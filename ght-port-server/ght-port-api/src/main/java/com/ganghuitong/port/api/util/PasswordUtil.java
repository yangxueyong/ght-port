package com.ganghuitong.port.api.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * 密码加密工具（SHA-256 + 随机盐）
 *
 * @author yangxueyong
 */
public final class PasswordUtil {

    /**
     * 盐值长度
     */
    private static final int SALT_LENGTH = 16;

    /**
     * 私有构造，禁止实例化
     */
    private PasswordUtil() {
    }

    /**
     * 生成随机盐值
     *
     * @return 盐值
     */
    public static String randomSalt() {
        return RandomUtil.randomString(SALT_LENGTH);
    }

    /**
     * 对明文密码加盐加密
     *
     * @param rawPassword 明文密码
     * @param salt        盐值
     * @return 加密后的十六进制字符串
     */
    public static String encrypt(String rawPassword, String salt) {
        return DigestUtil.sha256Hex(StrUtil.nullToEmpty(salt) + rawPassword);
    }

    /**
     * 校验明文密码与密文是否匹配
     *
     * @param rawPassword     明文密码
     * @param salt            盐值
     * @param encodedPassword 数据库中的密文
     * @return true-匹配，false-不匹配
     */
    public static boolean matches(String rawPassword, String salt, String encodedPassword) {
        if (StrUtil.hasBlank(rawPassword, encodedPassword)) {
            return false;
        }
        return StrUtil.equals(encrypt(rawPassword, salt), encodedPassword);
    }
}
