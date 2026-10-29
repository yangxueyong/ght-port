package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.BerthQueryReq;
import com.ganghuitong.port.api.dto.biz.BerthStatusReq;
import com.ganghuitong.port.api.dto.biz.BerthWorkTimeReq;
import com.ganghuitong.port.api.vo.biz.BerthDetailVO;
import com.ganghuitong.port.api.vo.biz.BerthVO;

import java.util.List;

/**
 * 泊位服务
 *
 * @author yangxueyong
 */
public interface BerthService {

    /**
     * 查询泊位列表（含作业统计）
     *
     * @param req 泊位查询入参（港口ID、状态）
     * @return 泊位列表
     */
    List<BerthVO> list(BerthQueryReq req);

    /**
     * 查询泊位详情：作业中的货物、接驳承运商、相关车辆
     *
     * @param req 主键入参（泊位ID）
     * @return 泊位详情
     */
    BerthDetailVO detail(IdReq req);

    /**
     * 设置泊位作业时间等参数；泊位正在作业时需用户确认
     *
     * @param req 作业时间入参
     * @return 确认结果
     */
    ConfirmResp saveWorkTime(BerthWorkTimeReq req);

    /**
     * 变更泊位状态（转维修/恢复未作业）；泊位正在作业时需用户确认
     *
     * @param req 状态变更入参
     * @return 确认结果
     */
    ConfirmResp changeStatus(BerthStatusReq req);

    /**
     * 完成泊位作业：到货批次置为完成、取消待装载分派、泊位恢复未作业
     *
     * @param req 主键入参（泊位ID，force=true时忽略未出发车辆提示）
     * @return 确认结果
     */
    ConfirmResp finishWork(IdReq req);
}
