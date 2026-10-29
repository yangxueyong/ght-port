package com.ganghuitong.port.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 菜单类型枚举
 *
 * @author yangxueyong
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements CodeEnum {

    /**
     * 目录
     */
    DIR("DIR", "目录"),

    /**
     * 菜单
     */
    MENU("MENU", "菜单"),

    /**
     * 按钮
     */
    BUTTON("BUTTON", "按钮");

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;
}
