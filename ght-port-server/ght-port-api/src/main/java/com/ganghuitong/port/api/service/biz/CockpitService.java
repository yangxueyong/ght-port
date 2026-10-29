package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.dto.biz.CockpitReq;
import com.ganghuitong.port.api.vo.biz.CockpitVO;

/**
 * 大屏驾驶舱服务
 *
 * @author yangxueyong
 */
public interface CockpitService {

    /**
     * 查询驾驶舱数据：港口3D场景、汇总、货物类型统计、各泊位统计、近7日趋势、承运商与船运公司统计
     *
     * @param req 查询入参
     * @return 驾驶舱数据
     */
    CockpitVO overview(CockpitReq req);
}
