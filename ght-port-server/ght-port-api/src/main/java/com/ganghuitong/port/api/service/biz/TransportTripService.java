package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.TripCompleteReq;
import com.ganghuitong.port.api.dto.biz.TripPageReq;
import com.ganghuitong.port.api.vo.biz.TripVO;

/**
 * 运输单服务
 *
 * @author yangxueyong
 */
public interface TransportTripService {

    /**
     * 分页查询运输单（承运商账号仅能看到本承运商数据）
     *
     * @param req 分页查询入参
     * @return 运输单分页数据
     */
    PageResp<TripVO> page(TripPageReq req);

    /**
     * 设置车辆完成运输：运输单置为完成，车辆恢复空闲
     *
     * @param req 完成入参（运输单ID或车辆ID）
     */
    void complete(TripCompleteReq req);
}
