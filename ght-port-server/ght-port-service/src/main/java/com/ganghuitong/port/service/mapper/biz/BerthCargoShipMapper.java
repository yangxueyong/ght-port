package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.BerthCargoShip;
import org.apache.ibatis.annotations.Mapper;

/**
 * 到货与船只关联数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface BerthCargoShipMapper extends BaseMapper<BerthCargoShip> {
}
