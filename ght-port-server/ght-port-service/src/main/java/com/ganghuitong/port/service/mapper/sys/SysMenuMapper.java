package com.ganghuitong.port.service.mapper.sys;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.sys.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单权限数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /**
     * 查询用户通过角色获得的菜单（含按钮）
     *
     * @param userId 用户ID
     * @param status 启用状态码
     * @return 菜单列表（按排序号升序）
     */
    List<SysMenu> selectByUserId(@Param("userId") String userId, @Param("status") String status);
}
