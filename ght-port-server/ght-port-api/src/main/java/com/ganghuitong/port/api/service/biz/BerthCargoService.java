package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.CargoArriveReq;
import com.ganghuitong.port.api.dto.biz.CargoLoadReq;
import com.ganghuitong.port.api.dto.biz.CargoPageReq;
import com.ganghuitong.port.api.vo.biz.CargoDetailVO;
import com.ganghuitong.port.api.vo.biz.BerthCargoVO;

import java.util.List;

/**
 * 泊位货物服务（到货与装货）
 *
 * @author yangxueyong
 */
public interface BerthCargoService {

    /**
     * 泊位到货：录入货物信息，仅“未作业”“正在作业”的泊位可用，到货后泊位变为正在作业
     *
     * @param req 到货入参
     * @return 到货记录ID
     */
    String arrive(CargoArriveReq req);

    /**
     * 查询泊位作业中的到货（含所属船运公司船只）
     *
     * @param berthId 泊位ID
     * @return 到货列表
     */
    List<BerthCargoVO> listActive(String berthId);

    /**
     * 泊位装货：录入待装船的货物信息，仅“未作业”“正在作业”的泊位可用，装货后泊位变为正在作业
     *
     * @param req 装货入参
     * @return 货物记录ID
     */
    String load(CargoLoadReq req);

    /**
     * 分页查询泊位货物（含历史，按到货时间倒序）
     *
     * @param req 分页查询入参
     * @return 货物分页数据
     */
    PageResp<BerthCargoVO> page(CargoPageReq req);

    /**
     * 查询货物详情：到货货物含分派车辆与承运商，装货货物含装船船只
     *
     * @param req 主键入参（货物ID）
     * @return 货物详情
     */
    CargoDetailVO detail(IdReq req);
}
