package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.GatePass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 出门条数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface GatePassMapper extends BaseMapper<GatePass> {

    /**
     * 根据访问令牌查询出门条（跨租户查询，仅用于司机免登录查看，令牌不可猜测）
     *
     * @param token 访问令牌
     * @return 出门条，不存在时返回null
     */
    @InterceptorIgnore(tenantLine = "true")
    GatePass selectByTokenIgnoreTenant(@Param("token") String token);
}
