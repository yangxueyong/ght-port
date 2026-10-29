package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.PortSaveReq;
import com.ganghuitong.port.api.vo.biz.PortDetailVO;
import com.ganghuitong.port.api.vo.biz.PortVO;

import java.util.List;

/**
 * 港口服务
 *
 * @author yangxueyong
 */
public interface PortService {

    /**
     * 查询当前租户全部港口（含各状态泊位数量）
     *
     * @return 港口列表
     */
    List<PortVO> list();

    /**
     * 查询港口详情（含俯视图元素与泊位）
     *
     * @param req 主键入参（港口ID）
     * @return 港口详情
     */
    PortDetailVO detail(IdReq req);

    /**
     * 新增或修改港口基础信息
     *
     * @param req 港口保存入参
     * @return 港口ID
     */
    String save(PortSaveReq req);

    /**
     * 删除港口（存在正在作业泊位时不可删除）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);
}
