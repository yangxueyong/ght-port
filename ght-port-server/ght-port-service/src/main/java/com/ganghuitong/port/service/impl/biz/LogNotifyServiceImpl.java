package com.ganghuitong.port.service.impl.biz;

import com.ganghuitong.port.api.dto.biz.GatePassNotifyReq;
import com.ganghuitong.port.api.service.biz.NotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 消息通知服务默认实现：仅记录日志（接入短信网关时替换此实现即可）
 *
 * @author yangxueyong
 */
@Slf4j
@Service
public class LogNotifyServiceImpl implements NotifyService {

    /**
     * 将出门条链接发送给司机（记录日志模拟发送）
     *
     * @param req 通知入参（手机号、车牌、出门条编号、链接）
     */
    @Override
    public void sendGatePassLink(GatePassNotifyReq req) {
        log.info("【短信通知】发送至[{}]：您驾驶的[{}]出门条[{}]已开具，请打开链接出示二维码：{}",
                req.getPhone(), req.getPlateNo(), req.getPassNo(), req.getLink());
    }
}
