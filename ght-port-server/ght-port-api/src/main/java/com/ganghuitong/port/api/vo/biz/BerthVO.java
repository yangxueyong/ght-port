package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.Berth;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 泊位信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BerthVO extends Berth {

    /**
     * 状态描述
     */
    private String statusDesc;

    /**
     * 作业中的到货批次数
     */
    private Integer activeCargoCount;

    /**
     * 该泊位运输中的车辆数
     */
    private Integer inTransitCount;

    /**
     * 该泊位待装载的车辆数
     */
    private Integer waitingCount;
}
