package com.ganghuitong.port.service.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.GatePass;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.DispatchVO;
import com.ganghuitong.port.api.vo.biz.TripVO;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.GatePassMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分派承运视图组装器：批量补齐车辆、承运商、货物、运输单、出门条信息
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class DispatchAssembler {

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 出门条数据访问
     */
    private final GatePassMapper gatePassMapper;

    /**
     * 将分派实体批量转换为视图对象
     *
     * @param dispatches 分派实体列表
     * @return 分派视图列表
     */
    public List<DispatchVO> assemble(List<Dispatch> dispatches) {
        if (CollUtil.isEmpty(dispatches)) {
            return Collections.emptyList();
        }
        Map<String, Vehicle> vehicles = byId(vehicleMapper.selectBatchIds(ids(dispatches, Dispatch::getVehicleId)), Vehicle::getId);
        Map<String, Carrier> carriers = byId(carrierMapper.selectBatchIds(ids(dispatches, Dispatch::getCarrierId)), Carrier::getId);
        Map<String, BerthCargo> cargos = byId(berthCargoMapper.selectBatchIds(ids(dispatches, Dispatch::getCargoId)), BerthCargo::getId);
        Set<String> tripIds = ids(dispatches, Dispatch::getTripId);
        Map<String, TransportTrip> trips = tripIds.isEmpty() ? Collections.emptyMap()
                : byId(transportTripMapper.selectBatchIds(tripIds), TransportTrip::getId);
        Map<String, GatePass> passes = tripIds.isEmpty() ? Collections.emptyMap()
                : byId(gatePassMapper.selectList(Wrappers.<GatePass>lambdaQuery().in(GatePass::getTripId, tripIds)), GatePass::getTripId);
        return dispatches.stream().map(d -> {
            DispatchVO vo = BeanUtil.copyProperties(d, DispatchVO.class);
            fillVehicle(vo, vehicles.get(d.getVehicleId()));
            Carrier carrier = carriers.get(d.getCarrierId());
            vo.setCarrierName(carrier == null ? null : carrier.getCarrierName());
            BerthCargo cargo = cargos.get(d.getCargoId());
            vo.setCargoName(cargo == null ? null : cargo.getCargoName());
            TransportTrip trip = StrUtil.isBlank(d.getTripId()) ? null : trips.get(d.getTripId());
            if (trip != null) {
                TripVO tripVo = BeanUtil.copyProperties(trip, TripVO.class);
                tripVo.setStatusDesc(CodeEnumUtil.desc(TripStatusEnum.class, trip.getStatus()));
                vo.setTrip(tripVo);
                GatePass pass = passes.get(trip.getId());
                vo.setPassToken(pass == null ? null : pass.getAccessToken());
                vo.setPassStatus(pass == null ? null : pass.getStatus());
            }
            return vo;
        }).toList();
    }

    /**
     * 填充车辆信息
     *
     * @param vo      分派视图
     * @param vehicle 车辆实体，可能为空
     */
    private void fillVehicle(DispatchVO vo, Vehicle vehicle) {
        if (vehicle == null) {
            return;
        }
        vo.setPlateNo(vehicle.getPlateNo());
        vo.setVehicleType(vehicle.getVehicleType());
        vo.setVehicleLength(vehicle.getVehicleLength());
        vo.setLoadCapacity(vehicle.getLoadCapacity());
        vo.setBodyColor(vehicle.getBodyColor());
        vo.setDriverName(vehicle.getDriverName());
        vo.setDriverPhone(vehicle.getDriverPhone());
        vo.setVehicleTransportStatus(vehicle.getTransportStatus());
    }

    /**
     * 提取非空ID集合
     *
     * @param list   实体列表
     * @param getter ID取值函数
     * @param <T>    实体类型
     * @return 去重后的ID集合
     */
    private <T> Set<String> ids(Collection<T> list, Function<T, String> getter) {
        return list.stream().map(getter).filter(StrUtil::isNotBlank).collect(Collectors.toSet());
    }

    /**
     * 列表转换为以键为索引的映射
     *
     * @param list   实体列表
     * @param keyFn  键取值函数
     * @param <T>    实体类型
     * @return 映射
     */
    private <T> Map<String, T> byId(Collection<T> list, Function<T, String> keyFn) {
        return list.stream().filter(Objects::nonNull)
                .collect(Collectors.toMap(keyFn, Function.identity(), (a, b) -> a));
    }
}
