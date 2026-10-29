package com.ganghuitong.port.controller.sys;

import com.ganghuitong.port.annotation.Anonymous;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.sys.ChangePwdReq;
import com.ganghuitong.port.api.dto.sys.LoginReq;
import com.ganghuitong.port.api.service.sys.AuthService;
import com.ganghuitong.port.api.vo.sys.LoginResp;
import com.ganghuitong.port.api.vo.sys.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录认证接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 登录认证服务
     */
    private final AuthService authService;

    /**
     * 账号密码登录
     *
     * @param req 登录入参（租户编码、账号、密码）
     * @return 令牌与用户信息
     */
    @Anonymous
    @PostMapping("/login")
    public R<LoginResp> login(@Validated @RequestBody LoginReq req) {
        return R.ok(authService.login(req));
    }

    /**
     * 获取当前登录用户信息（角色、权限、菜单）
     *
     * @return 当前用户信息
     */
    @PostMapping("/info")
    public R<UserInfoVO> info() {
        return R.ok(authService.currentUserInfo());
    }

    /**
     * 修改当前用户密码
     *
     * @param req 修改密码入参
     * @return 操作结果
     */
    @PostMapping("/changePassword")
    public R<Void> changePassword(@Validated @RequestBody ChangePwdReq req) {
        authService.changePassword(req);
        return R.ok();
    }
}
