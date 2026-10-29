package com.ganghuitong.port.api.constant;

import java.math.BigDecimal;

/**
 * 业务通用常量
 *
 * @author yangxueyong
 */
public final class BizConstant {

    /**
     * 运输单号前缀
     */
    public static final String TRIP_NO_PREFIX = "TR";

    /**
     * 运输单号日期格式
     */
    public static final String TRIP_NO_DATE_PATTERN = "yyyyMMddHHmmss";

    /**
     * 运输单号随机数长度
     */
    public static final int TRIP_NO_RANDOM_LENGTH = 4;

    /**
     * 港口俯视图默认画布宽度
     */
    public static final Integer DEFAULT_CANVAS_WIDTH = 1600;

    /**
     * 港口俯视图默认画布高度
     */
    public static final Integer DEFAULT_CANVAS_HEIGHT = 900;

    /**
     * 港口俯视图默认主题
     */
    public static final String DEFAULT_THEME = "DAY";

    /**
     * 首页最近运输动态条数
     */
    public static final int RECENT_TRIP_SIZE = 8;

    /**
     * 泊位编号自动生成前缀
     */
    public static final String BERTH_CODE_PREFIX = "B";

    /**
     * 名称拼接分隔符
     */
    public static final String NAME_SEPARATOR = "、";

    /**
     * 零值
     */
    public static final BigDecimal ZERO = BigDecimal.ZERO;

    /**
     * 私有构造，禁止实例化
     */
    private BizConstant() {
    }
}
