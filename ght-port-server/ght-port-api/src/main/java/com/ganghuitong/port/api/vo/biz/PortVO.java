package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Port;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 港口信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PortVO extends Port {

    /**
     * 泊位总数
     */
    private Integer berthTotal;

    /**
     * 未作业泊位数
     */
    private Integer berthIdle;

    /**
     * 正在作业泊位数
     */
    private Integer berthWorking;

    /**
     * 维修中泊位数
     */
    private Integer berthMaintenance;
}
