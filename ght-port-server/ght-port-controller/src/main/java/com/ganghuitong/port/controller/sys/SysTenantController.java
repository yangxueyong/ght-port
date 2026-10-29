package com.ganghuitong.port.controller.sys;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.TenantPageReq;
import com.ganghuitong.port.api.dto.sys.TenantSaveReq;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import com.ganghuitong.port.api.service.sys.SysTenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/sys/tenant")
@RequiredArgsConstructor
public class SysTenantController {

    /**
     * 租户服务
     */
    private final SysTenantService sysTenantService;

    /**
     * 分页查询租户
     *
     * @param req 分页查询入参
     * @return 租户分页数据
     */
    @RequiresPermission("sys:tenant:list")
    @PostMapping("/page")
    public R<PageResp<SysTenant>> page(@Validated @RequestBody TenantPageReq req) {
        return R.ok(sysTenantService.page(req));
    }

    /**
     * 新增或修改租户
     *
     * @param req 租户保存入参
     * @return 租户ID
     */
    @RequiresPermission("sys:tenant:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody TenantSaveReq req) {
        return R.ok(sysTenantService.save(req));
    }

    /**
     * 启用或停用租户
     *
     * @param req 状态变更入参
     * @return 操作结果
     */
    @RequiresPermission("sys:tenant:edit")
    @PostMapping("/status")
    public R<Void> status(@Validated @RequestBody StatusReq req) {
        sysTenantService.changeStatus(req);
        return R.ok();
    }

    /**
     * 删除租户
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("sys:tenant:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        sysTenantService.delete(req);
        return R.ok();
    }
}
