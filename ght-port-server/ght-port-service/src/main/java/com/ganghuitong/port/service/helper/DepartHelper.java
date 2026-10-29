package com.ganghuitong.port.service.helper;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.dto.biz.DepartReq;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.util.NoGenUtil;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 车辆出发辅助组件：校验装载量、锁定车辆、生成运输单、累加货物已装载量（调用方需开启事务）
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DepartHelper {

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 校验本次装载量：至少填写一项，且不能超过货物剩余量
     *
     * @param req   出发入参
     * @param cargo 泊位到货
     */
    public void checkLoad(DepartReq req, BerthCargo cargo) {
        checkLoad(req.getLoadTonnage(), req.getLoadVolume(), req.getLoadPieces(), cargo);
    }

    /**
     * 校验本次装载量：至少填写一项，且不能超过货物剩余量（货车装载与装船通用）
     *
     * @param loadTonnage 本次装载吨数
     * @param loadVolume  本次装载方数
     * @param loadPieces  本次装载件数
     * @param cargo       泊位货物
     */
    public void checkLoad(BigDecimal loadTonnage, BigDecimal loadVolume, Integer loadPieces, BerthCargo cargo) {
        BigDecimal tonnage = NumberUtil.nullToZero(loadTonnage);
        BigDecimal volume = NumberUtil.nullToZero(loadVolume);
        int pieces = ObjectUtil.defaultIfNull(loadPieces, 0);
        if (tonnage.signum() <= 0 && volume.signum() <= 0 && pieces <= 0) {
            throw new CustException("请至少填写装载吨数、装载方数、装载件数中的一项");
        }
        if (exceeds(cargo.getTonnage(), cargo.getLoadedTonnage(), tonnage)) {
            log.info("货物[{}]装载吨数超出剩余量", cargo.getCargoName());
            throw new CustException("装载吨数超过货物剩余吨数（剩余" + NumberUtil.sub(cargo.getTonnage(), cargo.getLoadedTonnage()) + "吨）");
        }
        if (exceeds(cargo.getVolume(), cargo.getLoadedVolume(), volume)) {
            log.info("货物[{}]装载方数超出剩余量", cargo.getCargoName());
            throw new CustException("装载方数超过货物剩余方数（剩余" + NumberUtil.sub(cargo.getVolume(), cargo.getLoadedVolume()) + "方）");
        }
        int totalPieces = ObjectUtil.defaultIfNull(cargo.getPieces(), 0);
        int loadedPieces = ObjectUtil.defaultIfNull(cargo.getLoadedPieces(), 0);
        if (totalPieces > 0 && loadedPieces + pieces > totalPieces) {
            log.info("货物[{}]装载件数超出剩余量", cargo.getCargoName());
            throw new CustException("装载件数超过货物剩余件数（剩余" + (totalPieces - loadedPieces) + "件）");
        }
    }

    /**
     * 锁定车辆为运输中：只有完成上一次运输（空闲）的车辆才能再次出发
     *
     * @param vehicle 车辆
     */
    public void lockVehicle(Vehicle vehicle) {
        int rows = vehicleMapper.update(null, Wrappers.<Vehicle>lambdaUpdate()
                .set(Vehicle::getTransportStatus, VehicleStatusEnum.IN_TRANSIT.getCode())
                .eq(Vehicle::getId, vehicle.getId())
                .eq(Vehicle::getTransportStatus, VehicleStatusEnum.IDLE.getCode()));
        if (rows == 0) {
            log.info("车辆[{}]尚未完成上一次运输，拒绝出发", vehicle.getPlateNo());
            throw new CustException("车辆【" + vehicle.getPlateNo() + "】尚未完成上一次运输，完成运输后才能再次出发");
        }
    }

    /**
     * 生成运输单，标记分派已出发，并累加货物已装载量
     *
     * @param dispatch 分派记录
     * @param cargo    泊位到货
     * @param vehicle  车辆
     * @param req      出发入参
     * @return 新生成的运输单
     */
    public TransportTrip createTrip(Dispatch dispatch, BerthCargo cargo, Vehicle vehicle, DepartReq req) {
        TransportTrip trip = new TransportTrip();
        trip.setTripNo(NoGenUtil.genNo(BizConstant.TRIP_NO_PREFIX, BizConstant.TRIP_NO_DATE_PATTERN, BizConstant.TRIP_NO_RANDOM_LENGTH));
        trip.setDispatchId(dispatch.getId());
        trip.setPortId(cargo.getPortId());
        trip.setBerthId(cargo.getBerthId());
        trip.setCargoId(cargo.getId());
        trip.setCarrierId(dispatch.getCarrierId());
        trip.setVehicleId(vehicle.getId());
        trip.setPlateNo(vehicle.getPlateNo());
        trip.setDriverName(vehicle.getDriverName());
        trip.setDriverPhone(vehicle.getDriverPhone());
        trip.setCargoName(cargo.getCargoName());
        trip.setCargoTypeName(cargo.getCargoTypeName());
        trip.setLoadTonnage(NumberUtil.nullToZero(req.getLoadTonnage()));
        trip.setLoadVolume(NumberUtil.nullToZero(req.getLoadVolume()));
        trip.setLoadPieces(ObjectUtil.defaultIfNull(req.getLoadPieces(), 0));
        trip.setStatus(TripStatusEnum.IN_TRANSIT.getCode());
        trip.setDepartTime(LocalDateTime.now());
        transportTripMapper.insert(trip);
        int rows = dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                .set(Dispatch::getStatus, DispatchStatusEnum.DEPARTED.getCode())
                .set(Dispatch::getTripId, trip.getId())
                .eq(Dispatch::getId, dispatch.getId())
                .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
        if (rows == 0) {
            log.info("分派[{}]状态已变化，出发失败", dispatch.getId());
            throw new CustException("该分派已出发或已取消，请刷新后重试");
        }
        berthCargoMapper.increaseLoaded(cargo.getId(), trip.getLoadTonnage(), trip.getLoadVolume(), trip.getLoadPieces());
        log.info("车辆[{}]装载完成出发，运输单[{}]", vehicle.getPlateNo(), trip.getTripNo());
        return trip;
    }

    /**
     * 判断本次装载是否超过剩余量（总量为0表示不限制）
     *
     * @param total  货物总量
     * @param loaded 已装载量
     * @param load   本次装载量
     * @return true-超出
     */
    private boolean exceeds(BigDecimal total, BigDecimal loaded, BigDecimal load) {
        BigDecimal t = NumberUtil.nullToZero(total);
        if (t.signum() <= 0) {
            return false;
        }
        return NumberUtil.add(NumberUtil.nullToZero(loaded), load).compareTo(t) > 0;
    }
}
