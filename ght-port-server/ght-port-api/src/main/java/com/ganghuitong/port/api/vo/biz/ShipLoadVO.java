package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.ShipLoad;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 装船分派信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ShipLoadVO extends ShipLoad {

    /**
     * 船名
     */
    private String shipName;

    /**
     * 船舶类型
     */
    private String shipType;

    /**
     * 船舶类型描述
     */
    private String shipTypeDesc;

    /**
     * 船长（米）
     */
    private BigDecimal shipLength;

    /**
     * 船体颜色
     */
    private String hullColor;

    /**
     * 船运公司名称
     */
    private String companyName;

    /**
     * 泊位名称
     */
    private String berthName;

    /**
     * 货物名称
     */
    private String cargoName;

    /**
     * 货物类型名称
     */
    private String cargoTypeName;

    /**
     * 货物形态
     */
    private String packType;
}
