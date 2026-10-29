package com.ganghuitong.port.service.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.sys.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
