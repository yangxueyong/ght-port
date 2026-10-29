package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.dto.biz.PortMapSaveReq;

/**
 * 港口俯视图绘制服务
 *
 * @author yangxueyong
 */
public interface PortMapService {

    /**
     * 保存港口俯视图；移除或移动正在作业的泊位时需用户确认（force=true）后才执行
     *
     * @param req 俯视图保存入参（港口ID、画布、元素列表、是否强制）
     * @return 确认结果：needConfirm=true表示未保存需用户确认
     */
    ConfirmResp saveMap(PortMapSaveReq req);
}
