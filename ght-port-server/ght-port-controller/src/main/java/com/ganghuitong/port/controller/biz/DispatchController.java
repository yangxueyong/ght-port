package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.DepartReq;
import com.ganghuitong.port.api.dto.biz.DispatchAssignReq;
import com.ganghuitong.port.api.dto.biz.DispatchQueryReq;
import com.ganghuitong.port.api.service.biz.DispatchService;
import com.ganghuitong.port.api.vo.biz.DispatchVO;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分派承运接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/dispatch")
@RequiredArgsConstructor
public class DispatchController {

    /**
     * 分派承运服务
     */
    private final DispatchService dispatchService;

    /**
     * 分派承运（可重复分派同一车辆）
     *
     * @param req 分派入参
     * @return 新增的分派记录
     */
    @RequiresPermission("berth:dispatch:assign")
    @PostMapping("/assign")
    public R<List<DispatchVO>> assign(@Validated @RequestBody DispatchAssignReq req) {
        return R.ok(dispatchService.assign(req));
    }

    /**
     * 查询分派记录
     *
     * @param req 查询入参
     * @return 分派记录
     */
    @RequiresPermission("berth:board:list")
    @PostMapping("/list")
    public R<List<DispatchVO>> list(@Validated @RequestBody DispatchQueryReq req) {
        return R.ok(dispatchService.list(req));
    }

    /**
     * 装载完成，立即出发（生成出门条）
     *
     * @param req 出发入参
     * @return 出门条信息
     */
    @RequiresPermission("berth:dispatch:depart")
    @PostMapping("/depart")
    public R<GatePassVO> depart(@Validated @RequestBody DepartReq req) {
        return R.ok(dispatchService.depart(req));
    }

    /**
     * 取消待装载的分派
     *
     * @param req 主键入参（分派ID）
     * @return 操作结果
     */
    @RequiresPermission("berth:dispatch:assign")
    @PostMapping("/cancel")
    public R<Void> cancel(@Validated @RequestBody IdReq req) {
        dispatchService.cancel(req);
        return R.ok();
    }
}
