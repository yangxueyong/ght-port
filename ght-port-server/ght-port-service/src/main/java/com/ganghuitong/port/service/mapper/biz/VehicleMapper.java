package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.vo.biz.CountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 车辆数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {

    /**
     * 按承运商统计车辆数量
     *
     * @param carrierIds      承运商ID集合
     * @param transportStatus 运输状态码，为空时统计全部
     * @return 各承运商车辆数（bizId为承运商ID）
     */
    List<CountVO> countByCarrier(@Param("carrierIds") Collection<String> carrierIds,
                                 @Param("transportStatus") String transportStatus);
}
