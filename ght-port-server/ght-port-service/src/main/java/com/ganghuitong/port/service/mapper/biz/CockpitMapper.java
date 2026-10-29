package com.ganghuitong.port.service.mapper.biz;

import com.ganghuitong.port.api.vo.biz.CockpitCargoRowVO;
import com.ganghuitong.port.api.vo.biz.CockpitTripRowVO;
import com.ganghuitong.port.api.vo.biz.TrendRowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 大屏驾驶舱统计数据访问层（只读聚合查询）
 *
 * @author yangxueyong
 */
@Mapper
public interface CockpitMapper {

    /**
     * 按泊位、货物类型、作业方向统计货物
     *
     * @param portId 港口ID
     * @return 统计行
     */
    List<CockpitCargoRowVO> selectCargoStat(@Param("portId") String portId);

    /**
     * 按泊位统计运输车次与吨数
     *
     * @param portId          港口ID
     * @param inTransitStatus 运输中状态码
     * @return 统计行
     */
    List<CockpitTripRowVO> selectTripStat(@Param("portId") String portId, @Param("inTransitStatus") String inTransitStatus);

    /**
     * 按日统计卸货（到货）吨数
     *
     * @param portId 港口ID
     * @param since  起始时间
     * @return 按日汇总
     */
    List<TrendRowVO> selectUnloadTrend(@Param("portId") String portId, @Param("since") LocalDateTime since);

    /**
     * 按日统计装船吨数
     *
     * @param portId 港口ID
     * @param since  起始时间
     * @return 按日汇总
     */
    List<TrendRowVO> selectLoadTrend(@Param("portId") String portId, @Param("since") LocalDateTime since);

    /**
     * 按日统计车辆运出吨数
     *
     * @param portId 港口ID
     * @param since  起始时间
     * @return 按日汇总
     */
    List<TrendRowVO> selectTripTrend(@Param("portId") String portId, @Param("since") LocalDateTime since);
}
