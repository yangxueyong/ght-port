package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.VehiclePageReq;
import com.ganghuitong.port.api.dto.biz.VehicleSaveReq;
import com.ganghuitong.port.api.service.biz.VehicleService;
import com.ganghuitong.port.api.vo.biz.VehicleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 车辆管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/vehicle")
@RequiredArgsConstructor
public class VehicleController {

    /**
     * 车辆服务
     */
    private final VehicleService vehicleService;

    /**
     * 分页查询车辆
     *
     * @param req 分页查询入参
     * @return 车辆分页数据
     */
    @RequiresPermission({"vehicle:list", "carrier:list"})
    @PostMapping("/page")
    public R<PageResp<VehicleVO>> page(@Validated @RequestBody VehiclePageReq req) {
        return R.ok(vehicleService.page(req));
    }

    /**
     * 车辆下拉搜索（限定承运商）
     *
     * @param req 关键字入参（parentId为承运商ID）
     * @return 车辆列表
     */
    @RequiresPermission({"vehicle:list", "berth:dispatch:assign"})
    @PostMapping("/options")
    public R<List<VehicleVO>> options(@Validated @RequestBody KeywordReq req) {
        return R.ok(vehicleService.options(req));
    }

    /**
     * 新增或修改车辆（修改运输中车辆返回需确认）
     *
     * @param req 车辆保存入参
     * @return 确认结果
     */
    @RequiresPermission("vehicle:edit")
    @PostMapping("/save")
    public R<ConfirmResp> save(@Validated @RequestBody VehicleSaveReq req) {
        return R.ok(vehicleService.save(req));
    }

    /**
     * 删除车辆
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("vehicle:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        vehicleService.delete(req);
        return R.ok();
    }
}
