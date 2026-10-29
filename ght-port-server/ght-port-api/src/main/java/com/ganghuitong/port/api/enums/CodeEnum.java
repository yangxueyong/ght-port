package com.ganghuitong.port.api.enums;

/**
 * 带编码与描述的枚举约定
 *
 * @author yangxueyong
 */
public interface CodeEnum {

    /**
     * 获取枚举编码（存库的英文码值）
     *
     * @return 编码
     */
    String getCode();

    /**
     * 获取中文描述
     *
     * @return 描述
     */
    String getDesc();
}
