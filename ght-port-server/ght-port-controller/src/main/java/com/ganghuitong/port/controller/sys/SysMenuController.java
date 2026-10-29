package com.ganghuitong.port.controller.sys;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.sys.MenuSaveReq;
import com.ganghuitong.port.api.service.sys.SysMenuService;
import com.ganghuitong.port.api.vo.sys.MenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单权限管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/sys/menu")
@RequiredArgsConstructor
public class SysMenuController {

    /**
     * 菜单服务
     */
    private final SysMenuService sysMenuService;

    /**
     * 查询全部菜单树
     *
     * @return 菜单树
     */
    @RequiresPermission({"sys:menu:list", "sys:role:list"})
    @PostMapping("/tree")
    public R<List<MenuVO>> tree() {
        return R.ok(sysMenuService.tree());
    }

    /**
     * 新增或修改菜单
     *
     * @param req 菜单保存入参
     * @return 菜单ID
     */
    @RequiresPermission("sys:menu:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody MenuSaveReq req) {
        return R.ok(sysMenuService.save(req));
    }

    /**
     * 删除菜单
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("sys:menu:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        sysMenuService.delete(req);
        return R.ok();
    }
}
