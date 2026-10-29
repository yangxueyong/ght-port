package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.PortMapSaveReq;
import com.ganghuitong.port.api.dto.biz.PortSaveReq;
import com.ganghuitong.port.api.service.biz.PortMapService;
import com.ganghuitong.port.api.service.biz.PortService;
import com.ganghuitong.port.api.vo.biz.PortDetailVO;
import com.ganghuitong.port.api.vo.biz.PortVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 港口管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/port")
@RequiredArgsConstructor
public class PortController {

    /**
     * 港口服务
     */
    private final PortService portService;

    /**
     * 港口俯视图服务
     */
    private final PortMapService portMapService;

    /**
     * 查询港口列表
     *
     * @return 港口列表
     */
    @RequiresPermission({"port:map:list", "berth:board:list", "cockpit:view"})
    @PostMapping("/list")
    public R<List<PortVO>> list() {
        return R.ok(portService.list());
    }

    /**
     * 查询港口详情（含俯视图）
     *
     * @param req 主键入参（港口ID）
     * @return 港口详情
     */
    @RequiresPermission({"port:map:list", "berth:board:list", "cockpit:view"})
    @PostMapping("/detail")
    public R<PortDetailVO> detail(@Validated @RequestBody IdReq req) {
        return R.ok(portService.detail(req));
    }

    /**
     * 新增或修改港口基础信息
     *
     * @param req 港口保存入参
     * @return 港口ID
     */
    @RequiresPermission("port:map:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody PortSaveReq req) {
        return R.ok(portService.save(req));
    }

    /**
     * 删除港口
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("port:map:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        portService.delete(req);
        return R.ok();
    }

    /**
     * 保存港口俯视图（涉及正在作业泊位时返回需确认）
     *
     * @param req 俯视图保存入参
     * @return 确认结果
     */
    @RequiresPermission("port:map:edit")
    @PostMapping("/saveMap")
    public R<ConfirmResp> saveMap(@Validated @RequestBody PortMapSaveReq req) {
        return R.ok(portMapService.saveMap(req));
    }
}
