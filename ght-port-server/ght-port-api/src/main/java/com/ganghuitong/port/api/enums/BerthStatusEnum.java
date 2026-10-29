package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 泊位状态枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum BerthStatusEnum implements CodeEnum {

    /**
     * 未作业
     */
    IDLE("IDLE", "未作业"),

    /**
     * 正在作业
     */
    WORKING("WORKING", "正在作业"),

    /**
     * 维修中
     */
    MAINTENANCE("MAINTENANCE", "维修中");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
