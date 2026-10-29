package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 泊位货物详情：到货货物含分派车辆与承运商，装货货物含装船船只
 *
 * @author yangxueyong
 */
@Data
public class CargoDetailVO implements Serializable {

    /**
     * 货物信息
     */
    private BerthCargoVO cargo;

    /**
     * 分派承运记录（待装载与已出发，仅到货）
     */
    private List<DispatchVO> dispatches;

    /**
     * 装船分派记录（待装船与已装船，仅装货）
     */
    private List<ShipLoadVO> shipLoads;
}
