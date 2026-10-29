package com.ganghuitong.port.service.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.TripVO;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 运输单视图组装器：批量补齐承运商、泊位、车型信息
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class TripAssembler {

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 将运输单实体批量转换为视图对象
     *
     * @param trips 运输单实体列表
     * @return 运输单视图列表
     */
    public List<TripVO> assemble(List<TransportTrip> trips) {
        if (CollUtil.isEmpty(trips)) {
            return Collections.emptyList();
        }
        Map<String, Carrier> carriers = carrierMapper.selectBatchIds(trips.stream().map(TransportTrip::getCarrierId).distinct().toList())
                .stream().collect(Collectors.toMap(Carrier::getId, Function.identity()));
        Map<String, Berth> berths = berthMapper.selectBatchIds(trips.stream().map(TransportTrip::getBerthId).distinct().toList())
                .stream().collect(Collectors.toMap(Berth::getId, Function.identity()));
        Map<String, Vehicle> vehicles = vehicleMapper.selectBatchIds(trips.stream().map(TransportTrip::getVehicleId).distinct().toList())
                .stream().collect(Collectors.toMap(Vehicle::getId, Function.identity()));
        return trips.stream().map(t -> {
            TripVO vo = BeanUtil.copyProperties(t, TripVO.class);
            vo.setStatusDesc(CodeEnumUtil.desc(TripStatusEnum.class, t.getStatus()));
            Carrier c = carriers.get(t.getCarrierId());
            vo.setCarrierName(c == null ? null : c.getCarrierName());
            Berth b = berths.get(t.getBerthId());
            vo.setBerthName(b == null ? null : b.getBerthName());
            Vehicle v = vehicles.get(t.getVehicleId());
            vo.setVehicleType(v == null ? null : v.getVehicleType());
            vo.setBodyColor(v == null ? null : v.getBodyColor());
            return vo;
        }).toList();
    }
}
