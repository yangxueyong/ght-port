package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.Carrier;
import org.apache.ibatis.annotations.Mapper;

/**
 * 承运商数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface CarrierMapper extends BaseMapper<Carrier> {
}
