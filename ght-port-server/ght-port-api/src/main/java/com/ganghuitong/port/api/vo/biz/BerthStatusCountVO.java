package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 泊位状态计数
 *
 * @author yangxueyong
 */
@Data
public class BerthStatusCountVO implements Serializable {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位状态
     */
    private String status;

    /**
     * 数量
     */
    private Integer cnt;
}
