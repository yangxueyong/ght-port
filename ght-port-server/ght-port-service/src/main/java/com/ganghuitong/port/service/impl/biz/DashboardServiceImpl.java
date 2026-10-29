package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.GatePass;
import com.ganghuitong.port.api.entity.biz.Port;
import com.ganghuitong.port.api.entity.biz.ShippingCompany;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.GatePassStatusEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.service.biz.DashboardService;
import com.ganghuitong.port.api.vo.biz.BerthStatusCountVO;
import com.ganghuitong.port.api.vo.biz.DashboardVO;
import com.ganghuitong.port.service.helper.TripAssembler;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.GatePassMapper;
import com.ganghuitong.port.service.mapper.biz.PortMapper;
import com.ganghuitong.port.service.mapper.biz.ShippingCompanyMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页概览服务实现
 *
 * @author yangxueyong
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /**
     * 港口数据访问
     */
    private final PortMapper portMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 船运公司数据访问
     */
    private final ShippingCompanyMapper shippingCompanyMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 出门条数据访问
     */
    private final GatePassMapper gatePassMapper;

    /**
     * 运输单视图组装器
     */
    private final TripAssembler tripAssembler;

    /**
     * 查询首页概览数据
     *
     * @return 概览数据
     */
    @Override
    public DashboardVO overview() {
        DashboardVO vo = new DashboardVO();
        vo.setPortCount(portMapper.selectCount(Wrappers.<Port>lambdaQuery()).intValue());
        Map<String, Integer> berth = berthMapper.selectStatusCount().stream()
                .collect(Collectors.toMap(BerthStatusCountVO::getStatus, BerthStatusCountVO::getCnt, Integer::sum));
        vo.setBerthIdle(berth.getOrDefault(BerthStatusEnum.IDLE.getCode(), 0));
        vo.setBerthWorking(berth.getOrDefault(BerthStatusEnum.WORKING.getCode(), 0));
        vo.setBerthMaintenance(berth.getOrDefault(BerthStatusEnum.MAINTENANCE.getCode(), 0));
        vo.setCarrierCount(carrierMapper.selectCount(Wrappers.<Carrier>lambdaQuery()).intValue());
        vo.setVehicleCount(vehicleMapper.selectCount(Wrappers.<Vehicle>lambdaQuery()).intValue());
        vo.setVehicleInTransit(vehicleMapper.selectCount(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getTransportStatus, VehicleStatusEnum.IN_TRANSIT.getCode())).intValue());
        vo.setCompanyCount(shippingCompanyMapper.selectCount(Wrappers.<ShippingCompany>lambdaQuery()).intValue());
        LocalDateTime todayStart = LocalDateTimeUtil.beginOfDay(LocalDateTime.now());
        vo.setTodayTrips(transportTripMapper.selectCount(Wrappers.<TransportTrip>lambdaQuery()
                .ge(TransportTrip::getDepartTime, todayStart)).intValue());
        vo.setTodayTonnage(transportTripMapper.sumTonnageSince(todayStart));
        vo.setUnverifiedPasses(gatePassMapper.selectCount(Wrappers.<GatePass>lambdaQuery()
                .eq(GatePass::getStatus, GatePassStatusEnum.UNUSED.getCode())).intValue());
        vo.setRecentTrips(tripAssembler.assemble(transportTripMapper.selectList(Wrappers.<TransportTrip>lambdaQuery()
                .orderByDesc(TransportTrip::getDepartTime)
                .last(CommonConstant.LIMIT_SQL + BizConstant.RECENT_TRIP_SIZE))));
        return vo;
    }
}
