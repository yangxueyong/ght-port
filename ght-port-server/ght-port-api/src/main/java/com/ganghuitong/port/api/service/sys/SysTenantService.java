package com.ganghuitong.port.api.service.sys;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.sys.StatusReq;
import com.ganghuitong.port.api.dto.sys.TenantPageReq;
import com.ganghuitong.port.api.dto.sys.TenantSaveReq;
import com.ganghuitong.port.api.entity.sys.SysTenant;

/**
 * 租户管理服务
 *
 * @author yangxueyong
 */
public interface SysTenantService {

    /**
     * 分页查询租户
     *
     * @param req 分页查询入参（关键字、状态）
     * @return 租户分页数据
     */
    PageResp<SysTenant> page(TenantPageReq req);

    /**
     * 新增或修改租户；新增时自动初始化内置角色与租户管理员账号
     *
     * @param req 租户保存入参
     * @return 租户ID
     */
    String save(TenantSaveReq req);

    /**
     * 启用或停用租户
     *
     * @param req 状态变更入参（租户ID、目标状态）
     */
    void changeStatus(StatusReq req);

    /**
     * 删除租户（平台租户不可删除）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);
}
