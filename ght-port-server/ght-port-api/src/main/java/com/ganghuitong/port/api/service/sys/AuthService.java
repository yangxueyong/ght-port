package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.dto.sys.ChangePwdReq;
import com.ganghuitong.port.api.dto.sys.LoginReq;
import com.ganghuitong.port.api.vo.sys.LoginResp;
import com.ganghuitong.port.api.vo.sys.TenantOptionVO;
import com.ganghuitong.port.api.vo.sys.UserInfoVO;

import java.util.List;

/**
 * 登录认证服务
 *
 * @author yangxueyong
 */
public interface AuthService {

    /**
     * 账号密码登录
     *
     * @param req 登录入参（租户编码、账号、密码）
     * @return 登录结果（令牌、昵称、租户名称）
     */
    LoginResp login(LoginReq req);

    /**
     * 获取当前登录用户信息（含角色、权限、菜单树）
     *
     * @return 当前用户信息
     */
    UserInfoVO currentUserInfo();

    /**
     * 修改当前用户密码
     *
     * @param req 修改密码入参（原密码、新密码）
     */
    void changePassword(ChangePwdReq req);

    /**
     * 查询登录页可选的租户（港口）列表
     *
     * @return 启用状态的租户选项
     */
    List<TenantOptionVO> tenantOptions();
}
