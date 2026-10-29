package com.ganghuitong.port.service.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.sys.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
