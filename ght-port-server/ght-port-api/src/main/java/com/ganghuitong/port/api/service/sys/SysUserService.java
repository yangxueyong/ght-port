package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.sys.ResetPwdReq;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.UserPageReq;
import com.ganghuitong.port.api.dto.sys.UserSaveReq;
import com.ganghuitong.port.api.vo.sys.UserVO;

/**
 * 用户管理服务
 *
 * @author yangxueyong
 */
public interface SysUserService {

    /**
     * 分页查询当前租户下的用户（含角色信息）
     *
     * @param req 分页查询入参（关键字、用户类型、状态）
     * @return 用户分页数据
     */
    PageResp<UserVO> page(UserPageReq req);

    /**
     * 新增或修改用户，并重新分配角色
     *
     * @param req 用户保存入参
     * @return 用户ID
     */
    String save(UserSaveReq req);

    /**
     * 删除用户（不可删除自己）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);

    /**
     * 重置用户密码
     *
     * @param req 重置密码入参（用户ID、新密码）
     */
    void resetPassword(ResetPwdReq req);

    /**
     * 启用或停用用户
     *
     * @param req 状态变更入参（用户ID、目标状态）
     */
    void changeStatus(StatusReq req);
}
