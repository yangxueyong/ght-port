package com.ganghuitong.port.service.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.sys.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 查询用户拥有的启用角色
     *
     * @param userId 用户ID
     * @param status 启用状态码
     * @return 角色列表
     */
    List<SysRole> selectByUserId(@Param("userId") String userId, @Param("status") String status);
}
