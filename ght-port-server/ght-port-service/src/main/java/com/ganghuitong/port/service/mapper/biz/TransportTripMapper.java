package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.vo.biz.CarrierStatVO;
import com.ganghuitong.port.api.vo.biz.CountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 运输单数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface TransportTripMapper extends BaseMapper<TransportTrip> {

    /**
     * 按承运商统计运输情况（趟数、运输中趟数、累计吨/方/件）
     *
     * @param carrierIds      承运商ID集合
     * @param inTransitStatus 运输中状态码
     * @return 各承运商统计结果
     */
    List<CarrierStatVO> selectCarrierStats(@Param("carrierIds") Collection<String> carrierIds,
                                           @Param("inTransitStatus") String inTransitStatus);

    /**
     * 按泊位统计指定状态的运输单数量
     *
     * @param berthIds 泊位ID集合
     * @param status   运输单状态码
     * @return 各泊位数量（bizId为泊位ID）
     */
    List<CountVO> countByBerth(@Param("berthIds") Collection<String> berthIds, @Param("status") String status);

    /**
     * 统计指定时间之后出发的运输单累计装载吨数
     *
     * @param since 起始时间
     * @return 累计吨数
     */
    BigDecimal sumTonnageSince(@Param("since") LocalDateTime since);
}
