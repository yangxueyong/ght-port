package com.ganghuitong.port.controller.sys;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.sys.ResetPwdReq;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.UserPageReq;
import com.ganghuitong.port.api.dto.sys.UserSaveReq;
import com.ganghuitong.port.api.service.sys.SysUserService;
import com.ganghuitong.port.api.vo.sys.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/sys/user")
@RequiredArgsConstructor
public class SysUserController {

    /**
     * 用户服务
     */
    private final SysUserService sysUserService;

    /**
     * 分页查询用户
     *
     * @param req 分页查询入参
     * @return 用户分页数据
     */
    @RequiresPermission("sys:user:list")
    @PostMapping("/page")
    public R<PageResp<UserVO>> page(@Validated @RequestBody UserPageReq req) {
        return R.ok(sysUserService.page(req));
    }

    /**
     * 新增或修改用户
     *
     * @param req 用户保存入参
     * @return 用户ID
     */
    @RequiresPermission("sys:user:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody UserSaveReq req) {
        return R.ok(sysUserService.save(req));
    }

    /**
     * 删除用户
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("sys:user:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        sysUserService.delete(req);
        return R.ok();
    }

    /**
     * 重置用户密码
     *
     * @param req 重置密码入参
     * @return 操作结果
     */
    @RequiresPermission("sys:user:edit")
    @PostMapping("/resetPwd")
    public R<Void> resetPassword(@Validated @RequestBody ResetPwdReq req) {
        sysUserService.resetPassword(req);
        return R.ok();
    }

    /**
     * 启用或停用用户
     *
     * @param req 状态变更入参
     * @return 操作结果
     */
    @RequiresPermission("sys:user:edit")
    @PostMapping("/status")
    public R<Void> status(@Validated @RequestBody StatusReq req) {
        sysUserService.changeStatus(req);
        return R.ok();
    }
}
