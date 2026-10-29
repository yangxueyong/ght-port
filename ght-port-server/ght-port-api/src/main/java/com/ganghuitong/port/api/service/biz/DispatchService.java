package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.DepartReq;
import com.ganghuitong.port.api.dto.biz.DispatchAssignReq;
import com.ganghuitong.port.api.dto.biz.DispatchQueryReq;
import com.ganghuitong.port.api.vo.biz.DispatchVO;
import com.ganghuitong.port.api.vo.biz.GatePassVO;

import java.util.List;

/**
 * 分派承运服务
 *
 * @author yangxueyong
 */
public interface DispatchService {

    /**
     * 分派承运：为正在作业泊位的货物分派承运商车辆，可重复分派同一车辆
     *
     * @param req 分派入参（泊位、货物、承运商、车辆列表）
     * @return 本次新增的分派记录
     */
    List<DispatchVO> assign(DispatchAssignReq req);

    /**
     * 查询分派记录（含车辆、运输单、出门条信息）
     *
     * @param req 查询入参（泊位ID、货物ID、状态）
     * @return 分派记录列表
     */
    List<DispatchVO> list(DispatchQueryReq req);

    /**
     * 装载完成，立即出发：生成运输单与出门条，并将出门条链接发送给司机
     *
     * @param req 出发入参（分派ID、装载吨数/方数/件数）
     * @return 出门条信息（含司机访问链接）
     */
    GatePassVO depart(DepartReq req);

    /**
     * 取消待装载的分派
     *
     * @param req 主键入参（分派ID）
     */
    void cancel(IdReq req);
}
