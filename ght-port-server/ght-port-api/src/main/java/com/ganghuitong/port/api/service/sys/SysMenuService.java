package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.dto.sys.MenuSaveReq;
import com.ganghuitong.port.api.vo.sys.MenuVO;

import java.util.List;

/**
 * 菜单权限管理服务
 *
 * @author yangxueyong
 */
public interface SysMenuService {

    /**
     * 查询全部菜单树（含按钮）
     *
     * @return 菜单树
     */
    List<MenuVO> tree();

    /**
     * 新增或修改菜单
     *
     * @param req 菜单保存入参
     * @return 菜单ID
     */
    String save(MenuSaveReq req);

    /**
     * 删除菜单（存在子菜单时不可删除）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);

    /**
     * 查询指定登录用户可见的菜单树（不含按钮）
     *
     * @param user 登录用户
     * @return 用户菜单树
     */
    List<MenuVO> userMenuTree(LoginUser user);
}
