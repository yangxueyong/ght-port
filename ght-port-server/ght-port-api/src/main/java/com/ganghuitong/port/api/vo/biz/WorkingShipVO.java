package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 正在作业的船只
 *
 * @author yangxueyong
 */
@Data
public class WorkingShipVO implements Serializable {

    /**
     * 船只ID
     */
    private String shipId;

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位名称
     */
    private String berthName;

    /**
     * 泊位货物ID
     */
    private String cargoId;

    /**
     * 货物名称
     */
    private String cargoName;

    /**
     * 待装船分派ID（仅装货）
     */
    private String loadId;

    /**
     * 作业方向：UNLOAD-卸货，LOAD-装货
     */
    private String direction;

    /**
     * 开始作业时间
     */
    private LocalDateTime startTime;
}
