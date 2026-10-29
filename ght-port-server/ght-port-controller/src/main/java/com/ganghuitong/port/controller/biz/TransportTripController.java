package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.TripCompleteReq;
import com.ganghuitong.port.api.dto.biz.TripPageReq;
import com.ganghuitong.port.api.service.biz.TransportTripService;
import com.ganghuitong.port.api.vo.biz.TripVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运输单接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/trip")
@RequiredArgsConstructor
public class TransportTripController {

    /**
     * 运输单服务
     */
    private final TransportTripService transportTripService;

    /**
     * 分页查询运输单
     *
     * @param req 分页查询入参
     * @return 运输单分页数据
     */
    @RequiresPermission({"trip:list", "carrier:self"})
    @PostMapping("/page")
    public R<PageResp<TripVO>> page(@Validated @RequestBody TripPageReq req) {
        return R.ok(transportTripService.page(req));
    }

    /**
     * 设置车辆完成运输
     *
     * @param req 完成入参（运输单ID或车辆ID）
     * @return 操作结果
     */
    @RequiresPermission("trip:complete")
    @PostMapping("/complete")
    public R<Void> complete(@Validated @RequestBody TripCompleteReq req) {
        transportTripService.complete(req);
        return R.ok();
    }
}
