package com.ganghuitong.port.service.helper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.BerthCargoShip;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.enums.ShipWorkStatusEnum;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoShipMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 泊位作业状态辅助组件：结束作业、变更状态
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BerthWorkHelper {

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 到货船只关联数据访问
     */
    private final BerthCargoShipMapper berthCargoShipMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 结束泊位当前作业：作业中的货物置为完成，卸货船只置为完成，待装载车辆与待装船分派置为取消（调用方需开启事务）
     *
     * @param berthId 泊位ID
     */
    public void closeWork(String berthId) {
        List<String> activeCargoIds = berthCargoMapper.selectList(Wrappers.<BerthCargo>lambdaQuery()
                        .select(BerthCargo::getId)
                        .eq(BerthCargo::getBerthId, berthId)
                        .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()))
                .stream().map(BerthCargo::getId).toList();
        int shipRows = activeCargoIds.isEmpty() ? 0 : berthCargoShipMapper.update(null, Wrappers.<BerthCargoShip>lambdaUpdate()
                .set(BerthCargoShip::getStatus, ShipWorkStatusEnum.COMPLETED.getCode())
                .set(BerthCargoShip::getFinishTime, LocalDateTime.now())
                .in(BerthCargoShip::getCargoId, activeCargoIds)
                .eq(BerthCargoShip::getStatus, ShipWorkStatusEnum.WORKING.getCode()));
        int cargoRows = berthCargoMapper.update(null, Wrappers.<BerthCargo>lambdaUpdate()
                .set(BerthCargo::getStatus, CargoStatusEnum.COMPLETED.getCode())
                .set(BerthCargo::getFinishTime, LocalDateTime.now())
                .eq(BerthCargo::getBerthId, berthId)
                .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()));
        int dispatchRows = dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                .set(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode())
                .eq(Dispatch::getBerthId, berthId)
                .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
        int loadRows = shipLoadMapper.update(null, Wrappers.<ShipLoad>lambdaUpdate()
                .set(ShipLoad::getStatus, ShipLoadStatusEnum.CANCELLED.getCode())
                .eq(ShipLoad::getBerthId, berthId)
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
        log.info("泊位[{}]结束作业：完成货物{}批，完成卸货船只{}艘，取消待装载分派{}条，取消待装船分派{}条",
                berthId, cargoRows, shipRows, dispatchRows, loadRows);
    }

    /**
     * 统计泊位运输中的车辆数
     *
     * @param berthId 泊位ID
     * @return 运输中车辆数
     */
    public long countInTransit(String berthId) {
        return transportTripMapper.selectCount(Wrappers.<TransportTrip>lambdaQuery()
                .eq(TransportTrip::getBerthId, berthId)
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode()));
    }

    /**
     * 完成泊位所有运输中的车辆：运输单置为完成，车辆恢复空闲（调用方需开启事务）
     *
     * @param berthId 泊位ID
     * @return 完成的运输单数量
     */
    public int completeTrips(String berthId) {
        List<TransportTrip> trips = transportTripMapper.selectList(Wrappers.<TransportTrip>lambdaQuery()
                .select(TransportTrip::getId, TransportTrip::getVehicleId)
                .eq(TransportTrip::getBerthId, berthId)
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode()));
        if (trips.isEmpty()) {
            return 0;
        }
        int rows = transportTripMapper.update(null, Wrappers.<TransportTrip>lambdaUpdate()
                .set(TransportTrip::getStatus, TripStatusEnum.COMPLETED.getCode())
                .set(TransportTrip::getCompleteTime, LocalDateTime.now())
                .set(TransportTrip::getCompleteBy, UserContext.getUsername())
                .in(TransportTrip::getId, trips.stream().map(TransportTrip::getId).toList())
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode()));
        vehicleMapper.update(null, Wrappers.<Vehicle>lambdaUpdate()
                .set(Vehicle::getTransportStatus, VehicleStatusEnum.IDLE.getCode())
                .in(Vehicle::getId, trips.stream().map(TransportTrip::getVehicleId).distinct().toList()));
        log.info("泊位[{}]完成作业，同时完成运输中车辆{}辆", berthId, rows);
        return rows;
    }

    /**
     * 更新泊位状态
     *
     * @param berthId 泊位ID
     * @param status  目标状态
     */
    public void updateStatus(String berthId, String status) {
        Berth update = new Berth();
        update.setId(berthId);
        update.setStatus(status);
        berthMapper.updateById(update);
    }
}
