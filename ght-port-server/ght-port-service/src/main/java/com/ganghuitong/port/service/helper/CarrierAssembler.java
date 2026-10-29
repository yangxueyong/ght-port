package com.ganghuitong.port.service.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.vo.biz.CarrierStatVO;
import com.ganghuitong.port.api.vo.biz.CarrierVO;
import com.ganghuitong.port.api.vo.biz.CountVO;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 承运商视图组装器：批量补齐车辆数、运输中车辆数、运输统计
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class CarrierAssembler {

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 将承运商实体批量转换为视图对象
     *
     * @param carriers 承运商实体列表
     * @return 承运商视图列表
     */
    public List<CarrierVO> assemble(List<Carrier> carriers) {
        if (CollUtil.isEmpty(carriers)) {
            return Collections.emptyList();
        }
        List<String> ids = carriers.stream().map(Carrier::getId).toList();
        Map<String, Integer> total = toMap(vehicleMapper.countByCarrier(ids, null));
        Map<String, Integer> transit = toMap(vehicleMapper.countByCarrier(ids, VehicleStatusEnum.IN_TRANSIT.getCode()));
        Map<String, CarrierStatVO> stats = transportTripMapper.selectCarrierStats(ids, TripStatusEnum.IN_TRANSIT.getCode())
                .stream().collect(Collectors.toMap(CarrierStatVO::getCarrierId, Function.identity()));
        return carriers.stream().map(c -> {
            CarrierVO vo = BeanUtil.copyProperties(c, CarrierVO.class);
            vo.setVehicleCount(total.getOrDefault(c.getId(), 0));
            vo.setInTransitVehicleCount(transit.getOrDefault(c.getId(), 0));
            vo.setStat(stats.getOrDefault(c.getId(), emptyStat(c.getId())));
            return vo;
        }).toList();
    }

    /**
     * 构造空统计
     *
     * @param carrierId 承运商ID
     * @return 全零统计
     */
    private CarrierStatVO emptyStat(String carrierId) {
        CarrierStatVO stat = new CarrierStatVO();
        stat.setCarrierId(carrierId);
        stat.setTripCount(0);
        stat.setInTransitCount(0);
        stat.setTotalTonnage(BizConstant.ZERO);
        stat.setTotalVolume(BizConstant.ZERO);
        stat.setTotalPieces(0);
        return stat;
    }

    /**
     * 计数列表转映射
     *
     * @param counts 计数列表
     * @return 业务ID->数量
     */
    private Map<String, Integer> toMap(List<CountVO> counts) {
        return counts.stream().collect(Collectors.toMap(CountVO::getBizId, CountVO::getCnt, Integer::sum));
    }
}
