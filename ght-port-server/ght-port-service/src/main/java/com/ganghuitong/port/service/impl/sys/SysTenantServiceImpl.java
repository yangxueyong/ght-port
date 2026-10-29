package com.ganghuitong.port.service.impl.sys;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.TenantPageReq;
import com.ganghuitong.port.api.dto.sys.TenantSaveReq;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.service.sys.SysTenantService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.service.helper.TenantInitHelper;
import com.ganghuitong.port.service.mapper.sys.SysTenantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 租户管理服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysTenantServiceImpl implements SysTenantService {

    /**
     * 租户数据访问
     */
    private final SysTenantMapper sysTenantMapper;

    /**
     * 租户初始化辅助组件
     */
    private final TenantInitHelper tenantInitHelper;

    /**
     * 登录用户缓存服务
     */
    private final LoginUserService loginUserService;

    /**
     * 分页查询租户
     *
     * @param req 分页查询入参（关键字、状态）
     * @return 租户分页数据
     */
    @Override
    public PageResp<SysTenant> page(TenantPageReq req) {
        Page<SysTenant> page = sysTenantMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<SysTenant>lambdaQuery()
                        .eq(StrUtil.isNotBlank(req.getStatus()), SysTenant::getStatus, req.getStatus())
                        .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(SysTenant::getTenantName, req.getKeyword())
                                .or().like(SysTenant::getTenantCode, req.getKeyword()))
                        .orderByAsc(SysTenant::getCreateTime));
        return PageResp.of(page, page.getRecords());
    }

    /**
     * 新增或修改租户；新增时自动初始化内置角色与租户管理员账号
     *
     * @param req 租户保存入参
     * @return 租户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(TenantSaveReq req) {
        Long dup = sysTenantMapper.selectCount(Wrappers.<SysTenant>lambdaQuery()
                .eq(SysTenant::getTenantCode, req.getTenantCode())
                .ne(StrUtil.isNotBlank(req.getId()), SysTenant::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("租户编码[" + req.getTenantCode() + "]已存在");
        }
        SysTenant tenant = BeanUtil.copyProperties(req, SysTenant.class);
        if (StrUtil.isNotBlank(req.getId())) {
            sysTenantMapper.updateById(tenant);
            log.info("修改租户[{}]", tenant.getTenantCode());
            return tenant.getId();
        }
        if (StrUtil.isBlank(req.getAdminUsername())) {
            throw new CustException("请填写租户管理员账号");
        }
        tenant.setStatus(EnableStatusEnum.ENABLED.getCode());
        sysTenantMapper.insert(tenant);
        tenantInitHelper.init(tenant, req.getAdminUsername());
        log.info("新增租户[{}]并完成初始化，管理员账号[{}]", tenant.getTenantCode(), req.getAdminUsername());
        return tenant.getId();
    }

    /**
     * 启用或停用租户
     *
     * @param req 状态变更入参（租户ID、目标状态）
     */
    @Override
    public void changeStatus(StatusReq req) {
        if (!CodeEnumUtil.contains(EnableStatusEnum.class, req.getStatus())) {
            throw new CustException("状态值不正确");
        }
        if (StrUtil.equals(CommonConstant.PLATFORM_TENANT_ID, req.getId())) {
            throw new CustException("平台租户不可停用");
        }
        SysTenant update = new SysTenant();
        update.setId(req.getId());
        update.setStatus(req.getStatus());
        sysTenantMapper.updateById(update);
        loginUserService.evictAll();
    }

    /**
     * 删除租户（平台租户不可删除）
     *
     * @param req 主键入参
     */
    @Override
    public void delete(IdReq req) {
        if (StrUtil.equals(CommonConstant.PLATFORM_TENANT_ID, req.getId())) {
            throw new CustException("平台租户不可删除");
        }
        sysTenantMapper.deleteById(req.getId());
        loginUserService.evictAll();
        log.info("删除租户[{}]", req.getId());
    }
}
