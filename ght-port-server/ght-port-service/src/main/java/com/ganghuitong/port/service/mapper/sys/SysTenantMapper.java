package com.ganghuitong.port.service.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.sys.SysTenant;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface SysTenantMapper extends BaseMapper<SysTenant> {
}
