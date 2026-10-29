package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.sys.RoleMenuReq;
import com.ganghuitong.port.api.dto.sys.RoleSaveReq;
import com.ganghuitong.port.api.entity.sys.SysRole;

import java.util.List;

/**
 * 角色管理服务
 *
 * @author yangxueyong
 */
public interface SysRoleService {

    /**
     * 分页查询角色
     *
     * @param req 分页查询入参（关键字）
     * @return 角色分页数据
     */
    PageResp<SysRole> page(PageReq req);

    /**
     * 查询当前租户全部启用角色（用于下拉选择）
     *
     * @return 角色列表
     */
    List<SysRole> listAll();

    /**
     * 新增或修改角色
     *
     * @param req 角色保存入参
     * @return 角色ID
     */
    String save(RoleSaveReq req);

    /**
     * 删除角色（内置角色不可删除）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);

    /**
     * 查询角色已授权的菜单ID
     *
     * @param req 主键入参（角色ID）
     * @return 菜单ID列表
     */
    List<String> listMenuIds(IdReq req);

    /**
     * 为角色重新分配菜单权限
     *
     * @param req 角色授权入参（角色ID、菜单ID列表）
     */
    void assignMenus(RoleMenuReq req);
}
