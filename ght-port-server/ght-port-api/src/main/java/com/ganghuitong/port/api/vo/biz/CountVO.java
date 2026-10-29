package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 按业务ID分组计数
 *
 * @author yangxueyong
 */
@Data
public class CountVO implements Serializable {

    /**
     * 业务ID
     */
    private String bizId;

    /**
     * 数量
     */
    private Integer cnt;
}
