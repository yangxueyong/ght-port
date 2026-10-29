package com.ganghuitong.port.api.entity.biz;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ganghuitong.port.api.common.TenantEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 船只实体（表：biz_ship）
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_ship")
public class Ship extends TenantEntity {

    /**
     * 所属船运公司ID
     */
    private String companyId;

    /**
     * 船名
     */
    private String shipName;

    /**
     * 船舶识别号
     */
    private String shipNo;

    /**
     * 船舶类型：BULK-散货船，CONTAINER-集装箱船，TANKER-油船，GENERAL-杂货船，RORO-滚装船，BARGE-驳船
     */
    private String shipType;

    /**
     * 船长（米）
     */
    private BigDecimal shipLength;

    /**
     * 载重吨
     */
    private BigDecimal deadweight;

    /**
     * 船体颜色
     */
    private String hullColor;

    /**
     * 船长姓名
     */
    private String captainName;

    /**
     * 船长电话
     */
    private String captainPhone;

    /**
     * 备注
     */
    private String remark;
}
