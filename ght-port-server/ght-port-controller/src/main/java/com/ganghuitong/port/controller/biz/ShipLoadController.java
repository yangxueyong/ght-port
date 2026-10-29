package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.ShipLoadAssignReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadConfirmReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadPageReq;
import com.ganghuitong.port.api.service.biz.ShipLoadService;
import com.ganghuitong.port.api.vo.biz.ShipLoadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 泊位装货（装船分派）接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/ship-load")
@RequiredArgsConstructor
public class ShipLoadController {

    /**
     * 装船分派服务
     */
    private final ShipLoadService shipLoadService;

    /**
     * 分派船只：为装货货物分派船只（可多选）
     *
     * @param req 分派入参
     * @return 本次新增的装船分派
     */
    @RequiresPermission("berth:cargo:load")
    @PostMapping("/assign")
    public R<List<ShipLoadVO>> assign(@Validated @RequestBody ShipLoadAssignReq req) {
        return R.ok(shipLoadService.assign(req));
    }

    /**
     * 查询装船分派（不分页）
     *
     * @param req 查询入参
     * @return 装船分派列表
     */
    @RequiresPermission("berth:board:list")
    @PostMapping("/list")
    public R<List<ShipLoadVO>> list(@Validated @RequestBody ShipLoadPageReq req) {
        return R.ok(shipLoadService.list(req));
    }

    /**
     * 分页查询装船记录
     *
     * @param req 分页查询入参
     * @return 装船记录分页数据
     */
    @RequiresPermission({"berth:board:list", "shipper:list"})
    @PostMapping("/page")
    public R<PageResp<ShipLoadVO>> page(@Validated @RequestBody ShipLoadPageReq req) {
        return R.ok(shipLoadService.page(req));
    }

    /**
     * 装船完成：登记装船量
     *
     * @param req 装船完成入参
     * @return 操作结果
     */
    @RequiresPermission("berth:cargo:load")
    @PostMapping("/confirm")
    public R<Void> confirm(@Validated @RequestBody ShipLoadConfirmReq req) {
        shipLoadService.confirm(req);
        return R.ok();
    }

    /**
     * 取消待装船的分派
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("berth:cargo:load")
    @PostMapping("/cancel")
    public R<Void> cancel(@Validated @RequestBody IdReq req) {
        shipLoadService.cancel(req);
        return R.ok();
    }
}
