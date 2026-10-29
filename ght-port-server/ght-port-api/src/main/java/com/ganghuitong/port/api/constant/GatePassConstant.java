package com.ganghuitong.port.api.constant;

/**
 * 出门条相关常量
 *
 * @author yangxueyong
 */
public final class GatePassConstant {

    /**
     * 出门条编号前缀
     */
    public static final String PASS_NO_PREFIX = "GP";

    /**
     * 出门条编号日期格式
     */
    public static final String PASS_NO_DATE_PATTERN = "yyyyMMddHHmmss";

    /**
     * 出门条编号随机数长度
     */
    public static final int PASS_NO_RANDOM_LENGTH = 4;

    /**
     * 司机端出门条访问路径（拼接在前端地址后）
     */
    public static final String DRIVER_PATH = "/#/pass/";

    /**
     * 私有构造，禁止实例化
     */
    private GatePassConstant() {
    }
}
