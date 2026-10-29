package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.GatePassPageReq;
import com.ganghuitong.port.api.dto.biz.TokenReq;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.vo.biz.GatePassVO;

/**
 * 出门条服务
 *
 * @author yangxueyong
 */
public interface GatePassService {

    /**
     * 为运输单开具出门条并通知司机
     *
     * @param trip 已生成的运输单
     * @return 出门条信息（含司机访问链接）
     */
    GatePassVO issue(TransportTrip trip);

    /**
     * 分页查询出门条
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    PageResp<GatePassVO> page(GatePassPageReq req);

    /**
     * 查询当前司机账号的出门条
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    PageResp<GatePassVO> myPasses(GatePassPageReq req);

    /**
     * 司机通过公开链接查看出门条（免登录）
     *
     * @param req 令牌入参
     * @return 出门条信息
     */
    GatePassVO publicView(TokenReq req);

    /**
     * 门卫扫码后预览出门条
     *
     * @param req 令牌入参
     * @return 出门条信息
     */
    GatePassVO preview(TokenReq req);

    /**
     * 门卫核销出门条，核销后二维码不能再次使用
     *
     * @param req 令牌入参
     * @return 核销后的出门条信息
     */
    GatePassVO verify(TokenReq req);
}
