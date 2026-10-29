package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.service.biz.DashboardService;
import com.ganghuitong.port.api.vo.biz.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页概览接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 首页概览服务
     */
    private final DashboardService dashboardService;

    /**
     * 查询首页概览数据
     *
     * @return 概览数据
     */
    @RequiresPermission("dashboard:view")
    @PostMapping("/overview")
    public R<DashboardVO> overview() {
        return R.ok(dashboardService.overview());
    }
}
