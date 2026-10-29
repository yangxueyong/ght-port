package com.ganghuitong.port.api.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;

/**
 * 编号与令牌生成工具
 *
 * @author yangxueyong
 */
public final class NoGenUtil {

    /**
     * 私有构造，禁止实例化
     */
    private NoGenUtil() {
    }

    /**
     * 生成“前缀 + 时间 + 随机数字”格式的业务编号
     *
     * @param prefix       编号前缀
     * @param datePattern  时间格式
     * @param randomLength 随机数字长度
     * @return 业务编号
     */
    public static String genNo(String prefix, String datePattern, int randomLength) {
        return prefix + DateUtil.format(DateUtil.date(), datePattern) + RandomUtil.randomNumbers(randomLength);
    }

    /**
     * 生成不可猜测的访问令牌（32位无横线UUID）
     *
     * @return 令牌
     */
    public static String genToken() {
        return IdUtil.fastSimpleUUID();
    }
}
