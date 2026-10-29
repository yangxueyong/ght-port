package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 装船分派实体（表：biz_ship_load）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ship_load")
public class ShipLoad extends TenantEntity {

    /**
     * 港口ID
     */
    private String portId;

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 泊位货物ID（装货）
     */
    private String cargoId;

    /**
     * 船只ID
     */
    private String shipId;

    /**
     * 船运公司ID
     */
    private String companyId;

    /**
     * 状态：WAITING-待装船，LOADED-已装船，CANCELLED-已取消
     */
    private String status;

    /**
     * 装船吨数
     */
    private BigDecimal loadTonnage;

    /**
     * 装船方数
     */
    private BigDecimal loadVolume;

    /**
     * 装船件数
     */
    private Integer loadPieces;

    /**
     * 装船完成时间
     */
    private LocalDateTime loadTime;

    /**
     * 备注
     */
    private String remark;
}
