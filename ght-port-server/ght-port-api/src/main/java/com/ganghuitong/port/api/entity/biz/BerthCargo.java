package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 泊位货物实体（到货与装货）（表：biz_berth_cargo）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_berth_cargo")
public class BerthCargo extends TenantEntity {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 作业方向：UNLOAD-到货（卸船），LOAD-装货（装船）
     */
    private String direction;

    /**
     * 货物类型ID
     */
    private String cargoTypeId;

    /**
     * 货物类型名称（冗余）
     */
    private String cargoTypeName;

    /**
     * 货物名称
     */
    private String cargoName;

    /**
     * 货物形态：CONTAINER-集装箱，BULK-散货
     */
    private String packType;

    /**
     * 货物吨位
     */
    private BigDecimal tonnage;

    /**
     * 货物方数
     */
    private BigDecimal volume;

    /**
     * 货物件数
     */
    private Integer pieces;

    /**
     * 已装载吨位
     */
    private BigDecimal loadedTonnage;

    /**
     * 已装载方数
     */
    private BigDecimal loadedVolume;

    /**
     * 已装载件数
     */
    private Integer loadedPieces;

    /**
     * 状态：IN_PROGRESS-作业中，COMPLETED-已完成
     */
    private String status;

    /**
     * 到货时间
     */
    private LocalDateTime arriveTime;

    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 备注
     */
    private String remark;
}
