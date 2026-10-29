package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 到货与船只关联实体（表：biz_berth_cargo_ship）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_berth_cargo_ship")
public class BerthCargoShip extends TenantEntity {

    /**
     * 泊位到货ID
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
     * 船只卸货状态：WORKING-卸货中，COMPLETED-已完成
     */
    private String status;

    /**
     * 卸货完成时间
     */
    private LocalDateTime finishTime;
}
