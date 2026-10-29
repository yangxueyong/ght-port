package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 泊位到货数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface BerthCargoMapper extends BaseMapper<BerthCargo> {

    /**
     * 原子累加到货的已装车吨位、方数、件数（避免并发出发时数据覆盖）
     *
     * @param cargoId 到货ID
     * @param tonnage 本次装载吨数
     * @param volume  本次装载方数
     * @param pieces  本次装载件数
     * @return 更新行数
     */
    int increaseLoaded(@Param("cargoId") String cargoId, @Param("tonnage") BigDecimal tonnage,
                       @Param("volume") BigDecimal volume, @Param("pieces") Integer pieces);
}
