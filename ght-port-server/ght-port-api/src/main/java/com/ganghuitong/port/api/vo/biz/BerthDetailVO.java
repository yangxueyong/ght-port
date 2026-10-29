package com.ganghuitong.port.api.vo.biz;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 泊位详情
 *
 * @author yangxueyong
 */
@Data
public class BerthDetailVO implements Serializable {

    /**
     * 泊位信息
     */
    private BerthVO berth;

    /**
     * 港口名称
     */
    private String portName;

    /**
     * 作业中的货物（到货与装货）
     */
    private List<BerthCargoVO> cargos;

    /**
     * 分派承运记录（待装载与已出发）
     */
    private List<DispatchVO> dispatches;

    /**
     * 装船分派记录（待装船与已装船）
     */
    private List<ShipLoadVO> shipLoads;

    /**
     * 接驳承运商
     */
    private List<CarrierOptionVO> carriers;
}
