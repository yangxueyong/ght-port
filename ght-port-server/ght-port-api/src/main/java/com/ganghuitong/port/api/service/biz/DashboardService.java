package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.vo.biz.DashboardVO;

/**
 * 首页概览服务
 *
 * @author yangxueyong
 */
public interface DashboardService {

    /**
     * 查询首页概览数据
     *
     * @return 概览数据
     */
    DashboardVO overview();
}
