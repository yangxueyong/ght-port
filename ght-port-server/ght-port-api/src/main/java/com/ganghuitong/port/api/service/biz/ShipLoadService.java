package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.ShipLoadAssignReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadConfirmReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadPageReq;
import com.ganghuitong.port.api.vo.biz.ShipLoadVO;

import java.util.List;

/**
 * 装船分派服务：将泊位装货的货物分派给船只并登记装船量
 *
 * @author yangxueyong
 */
public interface ShipLoadService {

    /**
     * 分派船只：为正在作业泊位的装货货物分派船只，可多选
     *
     * @param req 分派入参（泊位、货物、船只列表）
     * @return 本次新增的装船分派记录
     */
    List<ShipLoadVO> assign(ShipLoadAssignReq req);

    /**
     * 查询装船分派记录（不分页）
     *
     * @param req 查询入参（泊位、货物、状态）
     * @return 装船分派记录
     */
    List<ShipLoadVO> list(ShipLoadPageReq req);

    /**
     * 分页查询装船记录
     *
     * @param req 分页查询入参
     * @return 装船记录分页数据
     */
    PageResp<ShipLoadVO> page(ShipLoadPageReq req);

    /**
     * 装船完成：登记装船量并累加到货物已装载量
     *
     * @param req 装船完成入参
     */
    void confirm(ShipLoadConfirmReq req);

    /**
     * 取消待装船的分派
     *
     * @param req 主键入参（装船分派ID）
     */
    void cancel(IdReq req);
}
