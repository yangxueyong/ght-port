package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.Port;
import org.apache.ibatis.annotations.Mapper;

/**
 * 港口数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface PortMapper extends BaseMapper<Port> {
}
