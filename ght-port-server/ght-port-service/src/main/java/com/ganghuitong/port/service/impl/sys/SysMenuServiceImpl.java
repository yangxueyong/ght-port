package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.dto.sys.MenuSaveReq;
import com.ganghuitong.port.api.entity.sys.SysMenu;
import com.ganghuitong.port.api.entity.sys.SysRoleMenu;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.enums.MenuTypeEnum;
import com.ganghuitong.port.api.enums.RoleCodeEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.service.sys.SysMenuService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.util.TreeUtil;
import com.ganghuitong.port.api.vo.sys.MenuVO;
import com.ganghuitong.port.service.mapper.sys.SysMenuMapper;
import com.ganghuitong.port.service.mapper.sys.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单权限管理服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    /**
     * 菜单数据访问
     */
    private final SysMenuMapper sysMenuMapper;

    /**
     * 角色菜单关联数据访问
     */
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 登录用户缓存服务
     */
    private final LoginUserService loginUserService;

    /**
     * 查询全部菜单树（含按钮）
     *
     * @return 菜单树
     */
    @Override
    public List<MenuVO> tree() {
        List<SysMenu> menus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery().orderByAsc(SysMenu::getSort));
        return TreeUtil.build(BeanUtil.copyToList(menus, MenuVO.class), CommonConstant.ROOT_PARENT_ID);
    }

    /**
     * 新增或修改菜单
     *
     * @param req 菜单保存入参
     * @return 菜单ID
     */
    @Override
    public String save(MenuSaveReq req) {
        if (!CodeEnumUtil.contains(MenuTypeEnum.class, req.getMenuType())) {
            throw new CustException("菜单类型不正确");
        }
        SysMenu menu = BeanUtil.copyProperties(req, SysMenu.class);
        menu.setParentId(StrUtil.blankToDefault(req.getParentId(), CommonConstant.ROOT_PARENT_ID));
        if (StrUtil.isBlank(req.getId())) {
            menu.setStatus(EnableStatusEnum.ENABLED.getCode());
            sysMenuMapper.insert(menu);
            log.info("新增菜单[{}]", menu.getMenuName());
        } else {
            if (StrUtil.equals(req.getId(), menu.getParentId())) {
                throw new CustException("上级菜单不能选择自己");
            }
            sysMenuMapper.updateById(menu);
            log.info("修改菜单[{}]", menu.getMenuName());
        }
        loginUserService.evictAll();
        return menu.getId();
    }

    /**
     * 删除菜单（存在子菜单时不可删除）
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        Long childCount = sysMenuMapper.selectCount(Wrappers.<SysMenu>lambdaQuery().eq(SysMenu::getParentId, req.getId()));
        if (childCount > 0) {
            log.info("菜单[{}]存在子菜单，不允许删除", req.getId());
            throw new CustException("请先删除子菜单");
        }
        sysMenuMapper.deleteById(req.getId());
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getMenuId, req.getId()));
        loginUserService.evictAll();
    }

    /**
     * 查询指定登录用户可见的菜单树（不含按钮）；超级管理员可见全部菜单
     *
     * @param user 登录用户
     * @return 用户菜单树
     */
    @Override
    public List<MenuVO> userMenuTree(LoginUser user) {
        String enabled = EnableStatusEnum.ENABLED.getCode();
        List<SysMenu> menus;
        if (user.getRoleCodes() != null && user.getRoleCodes().contains(RoleCodeEnum.SUPER_ADMIN.getCode())) {
            log.debug("超级管理员加载全部菜单");
            menus = sysMenuMapper.selectList(Wrappers.<SysMenu>lambdaQuery()
                    .eq(SysMenu::getStatus, enabled)
                    .orderByAsc(SysMenu::getSort));
        } else {
            menus = sysMenuMapper.selectByUserId(user.getUserId(), enabled);
        }
        List<SysMenu> visibleMenus = menus.stream()
                .filter(m -> !StrUtil.equals(MenuTypeEnum.BUTTON.getCode(), m.getMenuType()))
                .filter(m -> CommonConstant.YES.equals(m.getVisible()))
                .toList();
        return TreeUtil.build(BeanUtil.copyToList(visibleMenus, MenuVO.class), CommonConstant.ROOT_PARENT_ID);
    }
}
