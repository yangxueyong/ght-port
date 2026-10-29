package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.BerthQueryReq;
import com.ganghuitong.port.api.dto.biz.BerthStatusReq;
import com.ganghuitong.port.api.dto.biz.BerthWorkTimeReq;
import com.ganghuitong.port.api.dto.biz.CargoArriveReq;
import com.ganghuitong.port.api.dto.biz.CargoLoadReq;
import com.ganghuitong.port.api.dto.biz.CargoPageReq;
import com.ganghuitong.port.api.service.biz.BerthCargoService;
import com.ganghuitong.port.api.service.biz.BerthService;
import com.ganghuitong.port.api.vo.biz.BerthCargoVO;
import com.ganghuitong.port.api.vo.biz.BerthDetailVO;
import com.ganghuitong.port.api.vo.biz.CargoDetailVO;
import com.ganghuitong.port.api.vo.biz.BerthVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 泊位管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/berth")
@RequiredArgsConstructor
public class BerthController {

    /**
     * 泊位服务
     */
    private final BerthService berthService;

    /**
     * 泊位到货服务
     */
    private final BerthCargoService berthCargoService;

    /**
     * 查询泊位列表
     *
     * @param req 泊位查询入参
     * @return 泊位列表
     */
    @RequiresPermission({"berth:board:list", "port:map:list"})
    @PostMapping("/list")
    public R<List<BerthVO>> list(@Validated @RequestBody BerthQueryReq req) {
        return R.ok(berthService.list(req));
    }

    /**
     * 查询泊位详情（货物、承运商、车辆）
     *
     * @param req 主键入参（泊位ID）
     * @return 泊位详情
     */
    @RequiresPermission({"berth:board:list", "dashboard:view", "cockpit:view"})
    @PostMapping("/detail")
    public R<BerthDetailVO> detail(@Validated @RequestBody IdReq req) {
        return R.ok(berthService.detail(req));
    }

    /**
     * 设置泊位作业时间（正在作业时返回需确认）
     *
     * @param req 作业时间入参
     * @return 确认结果
     */
    @RequiresPermission({"berth:work:edit", "port:map:edit"})
    @PostMapping("/workTime")
    public R<ConfirmResp> workTime(@Validated @RequestBody BerthWorkTimeReq req) {
        return R.ok(berthService.saveWorkTime(req));
    }

    /**
     * 变更泊位状态（维修/恢复）
     *
     * @param req 状态变更入参
     * @return 确认结果
     */
    @RequiresPermission("berth:work:edit")
    @PostMapping("/status")
    public R<ConfirmResp> status(@Validated @RequestBody BerthStatusReq req) {
        return R.ok(berthService.changeStatus(req));
    }

    /**
     * 完成泊位作业
     *
     * @param req 主键入参（泊位ID）
     * @return 确认结果
     */
    @RequiresPermission("berth:work:edit")
    @PostMapping("/finish")
    public R<ConfirmResp> finish(@Validated @RequestBody IdReq req) {
        return R.ok(berthService.finishWork(req));
    }

    /**
     * 泊位到货
     *
     * @param req 到货入参
     * @return 到货记录ID
     */
    @RequiresPermission("berth:cargo:arrive")
    @PostMapping("/arrive")
    public R<String> arrive(@Validated @RequestBody CargoArriveReq req) {
        return R.ok(berthCargoService.arrive(req));
    }

    /**
     * 泊位装货：登记待装船的货物
     *
     * @param req 装货入参
     * @return 货物记录ID
     */
    @RequiresPermission("berth:cargo:load")
    @PostMapping("/load")
    public R<String> load(@Validated @RequestBody CargoLoadReq req) {
        return R.ok(berthCargoService.load(req));
    }

    /**
     * 分页查询泊位货物（含历史）
     *
     * @param req 分页查询入参
     * @return 货物分页数据
     */
    @RequiresPermission({"berth:board:list", "shipper:list"})
    @PostMapping("/cargo/page")
    public R<PageResp<BerthCargoVO>> cargoPage(@Validated @RequestBody CargoPageReq req) {
        return R.ok(berthCargoService.page(req));
    }

    /**
     * 查询货物详情（分派车辆与承运商 / 装船船只）
     *
     * @param req 主键入参（货物ID）
     * @return 货物详情
     */
    @RequiresPermission({"berth:board:list", "shipper:list", "dashboard:view", "cockpit:view"})
    @PostMapping("/cargo/detail")
    public R<CargoDetailVO> cargoDetail(@Validated @RequestBody IdReq req) {
        return R.ok(berthCargoService.detail(req));
    }
}
