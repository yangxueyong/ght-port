package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.dto.biz.TripCompleteReq;
import com.ganghuitong.port.api.dto.biz.TripPageReq;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.TransportTripService;
import com.ganghuitong.port.api.vo.biz.TripVO;
import com.ganghuitong.port.service.helper.DataScopeHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.helper.TripAssembler;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 运输单服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransportTripServiceImpl implements TransportTripService {

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 运输单视图组装器
     */
    private final TripAssembler tripAssembler;

    /**
     * 数据范围辅助
     */
    private final DataScopeHelper dataScopeHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 分页查询运输单（承运商账号仅能看到本承运商数据）
     *
     * @param req 分页查询入参
     * @return 运输单分页数据
     */
    @Override
    public PageResp<TripVO> page(TripPageReq req) {
        String carrierId = dataScopeHelper.resolveCarrierId(req.getCarrierId());
        Page<TransportTrip> page = transportTripMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<TransportTrip>lambdaQuery()
                        .eq(StrUtil.isNotBlank(carrierId), TransportTrip::getCarrierId, carrierId)
                        .eq(StrUtil.isNotBlank(req.getVehicleId()), TransportTrip::getVehicleId, req.getVehicleId())
                        .eq(StrUtil.isNotBlank(req.getStatus()), TransportTrip::getStatus, req.getStatus())
                        .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(TransportTrip::getPlateNo, req.getKeyword())
                                .or().like(TransportTrip::getCargoName, req.getKeyword())
                                .or().like(TransportTrip::getTripNo, req.getKeyword()))
                        .orderByDesc(TransportTrip::getDepartTime));
        return PageResp.of(page, tripAssembler.assemble(page.getRecords()));
    }

    /**
     * 设置车辆完成运输：运输单置为完成，车辆恢复空闲
     *
     * @param req 完成入参（运输单ID或车辆ID）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(TripCompleteReq req) {
        TransportTrip trip;
        if (StrUtil.isNotBlank(req.getTripId())) {
            trip = lookup.trip(req.getTripId());
        } else if (StrUtil.isNotBlank(req.getVehicleId())) {
            trip = transportTripMapper.selectOne(Wrappers.<TransportTrip>lambdaQuery()
                    .eq(TransportTrip::getVehicleId, req.getVehicleId())
                    .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode())
                    .orderByDesc(TransportTrip::getDepartTime)
                    .last(CommonConstant.LIMIT_ONE));
            if (trip == null) {
                log.info("车辆[{}]没有运输中的运输单", req.getVehicleId());
                throw new CustException("该车辆当前没有运输中的任务");
            }
        } else {
            throw new CustException("请指定运输单或车辆");
        }
        dataScopeHelper.checkCarrier(trip.getCarrierId());
        int rows = transportTripMapper.update(null, Wrappers.<TransportTrip>lambdaUpdate()
                .set(TransportTrip::getStatus, TripStatusEnum.COMPLETED.getCode())
                .set(TransportTrip::getCompleteTime, LocalDateTime.now())
                .set(TransportTrip::getCompleteBy, UserContext.getUsername())
                .eq(TransportTrip::getId, trip.getId())
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode()));
        if (rows == 0) {
            log.info("运输单[{}]已不是运输中状态", trip.getTripNo());
            throw new CustException("该运输单已完成");
        }
        vehicleMapper.update(null, Wrappers.<Vehicle>lambdaUpdate()
                .set(Vehicle::getTransportStatus, VehicleStatusEnum.IDLE.getCode())
                .eq(Vehicle::getId, trip.getVehicleId()));
        log.info("车辆[{}]完成运输，运输单[{}]", trip.getPlateNo(), trip.getTripNo());
    }
}
