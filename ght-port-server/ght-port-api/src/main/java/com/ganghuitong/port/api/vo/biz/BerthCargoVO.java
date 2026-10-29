package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.BerthCargo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 泊位到货信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BerthCargoVO extends BerthCargo {

    /**
     * 泊位名称
     */
    private String berthName;

    /**
     * 所属船运公司船只
     */
    private List<ShipVO> ships;

    /**
     * 已分派数量（到货为分派车辆数，装货为分派船只数，不含已取消）
     */
    private Integer assignCount;
}
