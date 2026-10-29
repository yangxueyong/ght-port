package com.ganghuitong.port.controller.sys;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.sys.RoleMenuReq;
import com.ganghuitong.port.api.dto.sys.RoleSaveReq;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.service.sys.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/sys/role")
@RequiredArgsConstructor
public class SysRoleController {

    /**
     * 角色服务
     */
    private final SysRoleService sysRoleService;

    /**
     * 分页查询角色
     *
     * @param req 分页查询入参
     * @return 角色分页数据
     */
    @RequiresPermission("sys:role:list")
    @PostMapping("/page")
    public R<PageResp<SysRole>> page(@Validated @RequestBody PageReq req) {
        return R.ok(sysRoleService.page(req));
    }

    /**
     * 查询全部启用角色
     *
     * @return 角色列表
     */
    @RequiresPermission({"sys:role:list", "sys:user:list"})
    @PostMapping("/list")
    public R<List<SysRole>> list() {
        return R.ok(sysRoleService.listAll());
    }

    /**
     * 新增或修改角色
     *
     * @param req 角色保存入参
     * @return 角色ID
     */
    @RequiresPermission("sys:role:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody RoleSaveReq req) {
        return R.ok(sysRoleService.save(req));
    }

    /**
     * 删除角色
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("sys:role:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        sysRoleService.delete(req);
        return R.ok();
    }

    /**
     * 查询角色已授权菜单ID
     *
     * @param req 主键入参（角色ID）
     * @return 菜单ID列表
     */
    @RequiresPermission("sys:role:list")
    @PostMapping("/menuIds")
    public R<List<String>> menuIds(@Validated @RequestBody IdReq req) {
        return R.ok(sysRoleService.listMenuIds(req));
    }

    /**
     * 为角色分配菜单权限
     *
     * @param req 角色授权入参
     * @return 操作结果
     */
    @RequiresPermission("sys:role:edit")
    @PostMapping("/assignMenus")
    public R<Void> assignMenus(@Validated @RequestBody RoleMenuReq req) {
        sysRoleService.assignMenus(req);
        return R.ok();
    }
}
