package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 港口详情（含俯视图）
 *
 * @author yangxueyong
 */
@Data
public class PortDetailVO implements Serializable {

    /**
     * 港口信息
     */
    private PortVO port;

    /**
     * 俯视图元素
     */
    private List<PortElementVO> elements;

    /**
     * 泊位列表
     */
    private List<BerthVO> berths;
}
