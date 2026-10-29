package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.dto.biz.GatePassNotifyReq;

/**
 * 消息通知服务（短信等渠道），默认实现仅记录日志，可替换为真实短信网关
 *
 * @author yangxueyong
 */
public interface NotifyService {

    /**
     * 将出门条链接发送给司机
     *
     * @param req 通知入参（手机号、车牌、出门条编号、链接）
     */
    void sendGatePassLink(GatePassNotifyReq req);
}
