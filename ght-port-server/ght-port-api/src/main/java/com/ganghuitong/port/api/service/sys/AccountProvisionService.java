package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.dto.sys.AccountProvisionReq;

/**
 * 内置角色账号开通服务：新增承运商、车辆时自动为相关人员开通账号并赋予内置角色
 *
 * @author yangxueyong
 */
public interface AccountProvisionService {

    /**
     * 开通账号并赋予内置角色；账号已存在时仅补充角色
     *
     * @param req 开通入参（账号、昵称、用户类型、关联业务ID、角色编码）
     * @return 用户ID
     */
    String provision(AccountProvisionReq req);
}
