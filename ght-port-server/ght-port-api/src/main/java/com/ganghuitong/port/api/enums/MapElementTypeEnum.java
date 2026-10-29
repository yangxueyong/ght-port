package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 港口俯视图元素类型枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum MapElementTypeEnum implements CodeEnum {

    /**
     * 江河
     */
    RIVER("RIVER", "江河"),

    /**
     * 陆地
     */
    LAND("LAND", "陆地"),

    /**
     * 泊位
     */
    BERTH("BERTH", "泊位"),

    /**
     * 道路
     */
    ROAD("ROAD", "道路"),

    /**
     * 堆场
     */
    YARD("YARD", "堆场"),

    /**
     * 仓库
     */
    WAREHOUSE("WAREHOUSE", "仓库"),

    /**
     * 门机
     */
    CRANE("CRANE", "门机"),

    /**
     * 闸口
     */
    GATE("GATE", "闸口"),

    /**
     * 办公楼
     */
    BUILDING("BUILDING", "办公楼"),

    /**
     * 储罐
     */
    TANK("TANK", "储罐"),

    /**
     * 绿植
     */
    TREE("TREE", "绿植"),

    /**
     * 船舶
     */
    SHIP("SHIP", "船舶"),

    /**
     * 文字
     */
    LABEL("LABEL", "文字"),

    /**
     * 山体（props.peak 为山体高度）
     */
    MOUNTAIN("MOUNTAIN", "山体");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
