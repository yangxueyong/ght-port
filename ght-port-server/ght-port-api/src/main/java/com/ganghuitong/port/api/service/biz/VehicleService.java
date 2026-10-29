package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.VehiclePageReq;
import com.ganghuitong.port.api.dto.biz.VehicleSaveReq;
import com.ganghuitong.port.api.vo.biz.VehicleVO;

import java.util.List;

/**
 * 车辆服务
 *
 * @author yangxueyong
 */
public interface VehicleService {

    /**
     * 分页查询车辆（承运商账号仅能看到本承运商车辆）
     *
     * @param req 分页查询入参
     * @return 车辆分页数据
     */
    PageResp<VehicleVO> page(VehiclePageReq req);

    /**
     * 车辆下拉搜索（必须指定承运商，仅返回该承运商下的启用车辆）
     *
     * @param req 关键字入参（parentId为承运商ID）
     * @return 车辆列表
     */
    List<VehicleVO> options(KeywordReq req);

    /**
     * 新增或修改车辆；新增时为司机开通账号；修改运输中的车辆需用户确认
     *
     * @param req 车辆保存入参
     * @return 确认结果
     */
    ConfirmResp save(VehicleSaveReq req);

    /**
     * 删除车辆；运输中的车辆不可删除
     *
     * @param req 主键入参
     */
    void delete(IdReq req);
}
