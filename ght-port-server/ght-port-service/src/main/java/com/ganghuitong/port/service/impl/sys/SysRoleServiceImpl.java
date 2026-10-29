package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.sys.RoleMenuReq;
import com.ganghuitong.port.api.dto.sys.RoleSaveReq;
import com.ganghuitong.port.api.entity.sys.SysRole;
import com.ganghuitong.port.api.entity.sys.SysRoleMenu;
import com.ganghuitong.port.api.entity.sys.SysUserRole;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.service.sys.SysRoleService;
import com.ganghuitong.port.service.mapper.sys.SysRoleMapper;
import com.ganghuitong.port.service.mapper.sys.SysRoleMenuMapper;
import com.ganghuitong.port.service.mapper.sys.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色管理服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    /**
     * 角色数据访问
     */
    private final SysRoleMapper sysRoleMapper;

    /**
     * 角色菜单关联数据访问
     */
    private final SysRoleMenuMapper sysRoleMenuMapper;

    /**
     * 用户角色关联数据访问
     */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 登录用户缓存服务
     */
    private final LoginUserService loginUserService;

    /**
     * 分页查询角色
     *
     * @param req 分页查询入参（关键字）
     * @return 角色分页数据
     */
    @Override
    public PageResp<SysRole> page(PageReq req) {
        Page<SysRole> page = sysRoleMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<SysRole>lambdaQuery()
                        .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(SysRole::getRoleName, req.getKeyword())
                                .or().like(SysRole::getRoleCode, req.getKeyword()))
                        .orderByAsc(SysRole::getSort));
        return PageResp.of(page, page.getRecords());
    }

    /**
     * 查询当前租户全部启用角色（用于下拉选择）
     *
     * @return 角色列表
     */
    @Override
    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getStatus, EnableStatusEnum.ENABLED.getCode())
                .orderByAsc(SysRole::getSort));
    }

    /**
     * 新增或修改角色
     *
     * @param req 角色保存入参
     * @return 角色ID
     */
    @Override
    public String save(RoleSaveReq req) {
        Long dup = sysRoleMapper.selectCount(Wrappers.<SysRole>lambdaQuery()
                .eq(SysRole::getRoleCode, req.getRoleCode())
                .ne(StrUtil.isNotBlank(req.getId()), SysRole::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("角色编码[" + req.getRoleCode() + "]已存在");
        }
        SysRole role = BeanUtil.copyProperties(req, SysRole.class);
        role.setStatus(StrUtil.blankToDefault(req.getStatus(), EnableStatusEnum.ENABLED.getCode()));
        if (StrUtil.isBlank(req.getId())) {
            role.setBuiltin(CommonConstant.NO);
            sysRoleMapper.insert(role);
            log.info("新增角色[{}]", role.getRoleCode());
        } else {
            SysRole old = getRequired(req.getId());
            if (CommonConstant.YES.equals(old.getBuiltin()) && !StrUtil.equals(old.getRoleCode(), req.getRoleCode())) {
                log.info("内置角色[{}]编码不可修改", old.getRoleCode());
                throw new CustException("内置角色的编码不可修改");
            }
            sysRoleMapper.updateById(role);
            log.info("修改角色[{}]", role.getRoleCode());
        }
        loginUserService.evictAll();
        return role.getId();
    }

    /**
     * 删除角色（内置角色不可删除）
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        SysRole role = getRequired(req.getId());
        if (CommonConstant.YES.equals(role.getBuiltin())) {
            log.info("内置角色[{}]不可删除", role.getRoleCode());
            throw new CustException("内置角色不可删除");
        }
        sysRoleMapper.deleteById(role.getId());
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, role.getId()));
        sysUserRoleMapper.delete(Wrappers.<SysUserRole>lambdaQuery().eq(SysUserRole::getRoleId, role.getId()));
        loginUserService.evictAll();
    }

    /**
     * 查询角色已授权的菜单ID
     *
     * @param req 主键入参（角色ID）
     * @return 菜单ID列表
     */
    @Override
    public List<String> listMenuIds(IdReq req) {
        return sysRoleMenuMapper.selectList(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, req.getId()))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    /**
     * 为角色重新分配菜单权限
     *
     * @param req 角色授权入参（角色ID、菜单ID列表）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(RoleMenuReq req) {
        getRequired(req.getRoleId());
        sysRoleMenuMapper.delete(Wrappers.<SysRoleMenu>lambdaQuery().eq(SysRoleMenu::getRoleId, req.getRoleId()));
        if (CollUtil.isNotEmpty(req.getMenuIds())) {
            for (String menuId : CollUtil.distinct(req.getMenuIds())) {
                SysRoleMenu rel = new SysRoleMenu();
                rel.setRoleId(req.getRoleId());
                rel.setMenuId(menuId);
                sysRoleMenuMapper.insert(rel);
            }
        } else {
            log.info("角色[{}]的菜单权限已清空", req.getRoleId());
        }
        loginUserService.evictAll();
    }

    /**
     * 根据ID获取角色，不存在时抛出异常
     *
     * @param roleId 角色ID
     * @return 角色实体
     */
    private SysRole getRequired(String roleId) {
        SysRole role = sysRoleMapper.selectById(roleId);
        if (role == null) {
            throw new CustException("角色不存在");
        }
        return role;
    }
}
