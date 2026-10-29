package com.ganghuitong.port.service.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.VehicleTypeEnum;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.TripVO;
import com.ganghuitong.port.api.vo.biz.VehicleVO;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 车辆视图组装器：补齐承运商名称、车型描述、当前运输单
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class VehicleAssembler {

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 运输单视图组装器
     */
    private final TripAssembler tripAssembler;

    /**
     * 将车辆实体批量转换为视图对象
     *
     * @param vehicles 车辆实体列表
     * @return 车辆视图列表
     */
    public List<VehicleVO> assemble(List<Vehicle> vehicles) {
        if (CollUtil.isEmpty(vehicles)) {
            return Collections.emptyList();
        }
        Map<String, Carrier> carriers = carrierMapper.selectBatchIds(vehicles.stream().map(Vehicle::getCarrierId).distinct().toList())
                .stream().collect(Collectors.toMap(Carrier::getId, Function.identity()));
        List<TransportTrip> trips = transportTripMapper.selectList(Wrappers.<TransportTrip>lambdaQuery()
                .in(TransportTrip::getVehicleId, vehicles.stream().map(Vehicle::getId).toList())
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode()));
        Map<String, TripVO> tripMap = tripAssembler.assemble(trips).stream()
                .collect(Collectors.toMap(TripVO::getVehicleId, Function.identity(), (a, b) -> a));
        return vehicles.stream().map(v -> {
            VehicleVO vo = BeanUtil.copyProperties(v, VehicleVO.class);
            Carrier c = carriers.get(v.getCarrierId());
            vo.setCarrierName(c == null ? null : c.getCarrierName());
            vo.setVehicleTypeDesc(CodeEnumUtil.desc(VehicleTypeEnum.class, v.getVehicleType()));
            vo.setCurrentTrip(tripMap.get(v.getId()));
            return vo;
        }).toList();
    }
}
