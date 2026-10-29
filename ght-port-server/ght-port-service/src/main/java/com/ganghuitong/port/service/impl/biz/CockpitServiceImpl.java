package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.CarrierPageReq;
import com.ganghuitong.port.api.dto.biz.CockpitReq;
import com.ganghuitong.port.api.enums.CargoDirectionEnum;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.CarrierService;
import com.ganghuitong.port.api.service.biz.CockpitService;
import com.ganghuitong.port.api.service.biz.PortService;
import com.ganghuitong.port.api.service.biz.ShippingCompanyService;
import com.ganghuitong.port.api.vo.biz.BerthStatVO;
import com.ganghuitong.port.api.vo.biz.CargoTypeStatVO;
import com.ganghuitong.port.api.vo.biz.CarrierVO;
import com.ganghuitong.port.api.vo.biz.CockpitCargoRowVO;
import com.ganghuitong.port.api.vo.biz.CockpitSummaryVO;
import com.ganghuitong.port.api.vo.biz.CockpitTripRowVO;
import com.ganghuitong.port.api.vo.biz.CockpitVO;
import com.ganghuitong.port.api.vo.biz.PortDetailVO;
import com.ganghuitong.port.api.vo.biz.PortVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyVO;
import com.ganghuitong.port.api.vo.biz.TrendRowVO;
import com.ganghuitong.port.api.vo.biz.TrendVO;
import com.ganghuitong.port.service.mapper.biz.CockpitMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 大屏驾驶舱服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CockpitServiceImpl implements CockpitService {

    /**
     * 趋势统计天数
     */
    private static final int TREND_DAYS = 7;

    /**
     * 承运商、船运公司统计的最大条数
     */
    private static final long RANK_LIMIT = 200L;

    /**
     * 驾驶舱统计数据访问
     */
    private final CockpitMapper cockpitMapper;

    /**
     * 港口服务
     */
    private final PortService portService;

    /**
     * 承运商服务
     */
    private final CarrierService carrierService;

    /**
     * 船运公司服务
     */
    private final ShippingCompanyService shippingCompanyService;

    /**
     * 查询驾驶舱数据：港口3D场景、汇总、货物类型统计、各泊位统计、近7日趋势、承运商与船运公司统计
     *
     * @param req 查询入参
     * @return 驾驶舱数据
     */
    @Override
    public CockpitVO overview(CockpitReq req) {
        String portId = req.getPortId();
        if (StrUtil.isBlank(portId)) {
            List<PortVO> ports = portService.list();
            if (ports.isEmpty()) {
                throw new CustException("还没有港口，请先在“港口管理”中新建港口");
            }
            portId = ports.get(0).getId();
        }
        IdReq idReq = new IdReq();
        idReq.setId(portId);
        PortDetailVO port = portService.detail(idReq);

        List<CockpitCargoRowVO> cargoRows = cockpitMapper.selectCargoStat(portId);
        List<CockpitTripRowVO> tripRows = cockpitMapper.selectTripStat(portId, TripStatusEnum.IN_TRANSIT.getCode());

        CarrierPageReq carrierReq = new CarrierPageReq();
        carrierReq.setPageSize(RANK_LIMIT);
        PageResp<CarrierVO> carriers = carrierService.page(carrierReq);
        PageReq shippingCompanyReq = new PageReq();
        shippingCompanyReq.setPageSize(RANK_LIMIT);
        PageResp<ShippingCompanyVO> companies = shippingCompanyService.page(shippingCompanyReq);

        CockpitVO vo = new CockpitVO();
        vo.setPort(port);
        vo.setSummary(buildSummary(cargoRows, tripRows, carriers.getRecords(), companies.getRecords()));
        vo.setCargoTypes(groupByType(cargoRows));
        vo.setBerthStats(buildBerthStats(cargoRows, tripRows));
        vo.setTrend(buildTrend(portId));
        vo.setCarriers(carriers.getRecords().stream()
                .sorted(Comparator.comparing((CarrierVO c) -> NumberUtil.nullToZero(c.getStat() == null ? null : c.getStat().getTotalTonnage())).reversed())
                .toList());
        List<ShippingCompanyVO> shippingCompanyList = companies.getRecords().stream()
                .sorted(Comparator.comparing((ShippingCompanyVO s) -> NumberUtil.add(s.getUnloadTonnage(), s.getLoadTonnage())).reversed())
                .toList();
        shippingCompanyList.forEach(s -> s.setShips(null));
        vo.setCompanies(shippingCompanyList);
        log.info("驾驶舱数据：港口[{}]货物统计{}行，承运商{}家，船运公司{}家", port.getPort().getPortName(), cargoRows.size(),
                carriers.getRecords().size(), shippingCompanyList.size());
        return vo;
    }

    /**
     * 汇总港口数据
     *
     * @param cargoRows 货物统计行
     * @param tripRows  运输统计行
     * @param carriers  承运商
     * @param companies  船运公司
     * @return 港口汇总
     */
    private CockpitSummaryVO buildSummary(List<CockpitCargoRowVO> cargoRows, List<CockpitTripRowVO> tripRows,
                                          List<CarrierVO> carriers, List<ShippingCompanyVO> companies) {
        CockpitSummaryVO s = new CockpitSummaryVO();
        s.setUnloadTonnage(sum(cargoRows, true, CockpitCargoRowVO::getTonnage));
        s.setUnloadVolume(sum(cargoRows, true, CockpitCargoRowVO::getVolume));
        s.setLoadTonnage(sum(cargoRows, false, CockpitCargoRowVO::getLoadedTonnage));
        s.setLoadVolume(sum(cargoRows, false, CockpitCargoRowVO::getLoadedVolume));
        s.setUnloadCount(cargoRows.stream().filter(this::isUnload).mapToInt(r -> ObjectUtil.defaultIfNull(r.getCnt(), 0)).sum());
        s.setLoadCount(cargoRows.stream().filter(r -> !isUnload(r)).mapToInt(r -> ObjectUtil.defaultIfNull(r.getCnt(), 0)).sum());
        s.setWorkCount(s.getUnloadCount() + s.getLoadCount());
        s.setTripCount(tripRows.stream().mapToInt(r -> ObjectUtil.defaultIfNull(r.getTripCount(), 0)).sum());
        s.setInTransitCount(tripRows.stream().mapToInt(r -> ObjectUtil.defaultIfNull(r.getInTransitCount(), 0)).sum());
        s.setTripTonnage(tripRows.stream().map(r -> NumberUtil.nullToZero(r.getTonnage())).reduce(BigDecimal.ZERO, BigDecimal::add));
        s.setCarrierCount(carriers.size());
        s.setVehicleCount(carriers.stream().mapToInt(c -> ObjectUtil.defaultIfNull(c.getVehicleCount(), 0)).sum());
        s.setCompanyCount(companies.size());
        s.setShipCount(companies.stream().mapToInt(x -> ObjectUtil.defaultIfNull(x.getShipCount(), 0)).sum());
        s.setWorkingShipCount(companies.stream().mapToInt(x -> ObjectUtil.defaultIfNull(x.getWorkingShipCount(), 0)).sum());
        return s;
    }

    /**
     * 按货物类型合并统计（卸货取到货吨位，装货取已装船吨位），按总吨数倒序
     *
     * @param rows 货物统计行
     * @return 货物类型统计
     */
    private List<CargoTypeStatVO> groupByType(List<CockpitCargoRowVO> rows) {
        Map<String, CargoTypeStatVO> map = new LinkedHashMap<>();
        for (CockpitCargoRowVO r : rows) {
            String name = StrUtil.blankToDefault(r.getTypeName(), "其他");
            CargoTypeStatVO t = map.computeIfAbsent(name, k -> {
                CargoTypeStatVO v = new CargoTypeStatVO();
                v.setTypeName(k);
                v.setColor(r.getColor());
                v.setUnloadTonnage(BigDecimal.ZERO);
                v.setLoadTonnage(BigDecimal.ZERO);
                v.setCount(0);
                return v;
            });
            if (isUnload(r)) {
                t.setUnloadTonnage(t.getUnloadTonnage().add(NumberUtil.nullToZero(r.getTonnage())));
            } else {
                t.setLoadTonnage(t.getLoadTonnage().add(NumberUtil.nullToZero(r.getLoadedTonnage())));
            }
            t.setCount(t.getCount() + ObjectUtil.defaultIfNull(r.getCnt(), 0));
        }
        return map.values().stream()
                .sorted(Comparator.comparing((CargoTypeStatVO t) -> t.getUnloadTonnage().add(t.getLoadTonnage())).reversed())
                .toList();
    }

    /**
     * 各泊位累计统计
     *
     * @param cargoRows 货物统计行
     * @param tripRows  运输统计行
     * @return 泊位统计
     */
    private List<BerthStatVO> buildBerthStats(List<CockpitCargoRowVO> cargoRows, List<CockpitTripRowVO> tripRows) {
        Map<String, List<CockpitCargoRowVO>> byBerth = cargoRows.stream().collect(Collectors.groupingBy(CockpitCargoRowVO::getBerthId));
        Map<String, CockpitTripRowVO> trips = tripRows.stream().collect(Collectors.toMap(CockpitTripRowVO::getBerthId, Function.identity(), (a, b) -> a));
        List<BerthStatVO> list = new ArrayList<>();
        byBerth.forEach((berthId, rows) -> {
            BerthStatVO b = new BerthStatVO();
            b.setBerthId(berthId);
            b.setUnloadTonnage(sum(rows, true, CockpitCargoRowVO::getTonnage));
            b.setLoadTonnage(sum(rows, false, CockpitCargoRowVO::getLoadedTonnage));
            b.setWorkCount(rows.stream().mapToInt(r -> ObjectUtil.defaultIfNull(r.getCnt(), 0)).sum());
            CockpitTripRowVO t = trips.get(berthId);
            b.setTripCount(t == null ? 0 : ObjectUtil.defaultIfNull(t.getTripCount(), 0));
            b.setCargoTypes(groupByType(rows));
            list.add(b);
        });
        return list;
    }

    /**
     * 近7日趋势（含今日，无数据的日期补0）
     *
     * @param portId 港口ID
     * @return 按日趋势
     */
    private List<TrendVO> buildTrend(String portId) {
        LocalDate start = LocalDate.now().minusDays(TREND_DAYS - 1L);
        Map<String, BigDecimal> unload = toDayMap(cockpitMapper.selectUnloadTrend(portId, start.atStartOfDay()));
        Map<String, BigDecimal> load = toDayMap(cockpitMapper.selectLoadTrend(portId, start.atStartOfDay()));
        Map<String, BigDecimal> trip = toDayMap(cockpitMapper.selectTripTrend(portId, start.atStartOfDay()));
        List<TrendVO> list = new ArrayList<>();
        for (int i = 0; i < TREND_DAYS; i++) {
            String day = start.plusDays(i).toString();
            TrendVO t = new TrendVO();
            t.setDay(day);
            t.setUnloadTonnage(unload.getOrDefault(day, BigDecimal.ZERO));
            t.setLoadTonnage(load.getOrDefault(day, BigDecimal.ZERO));
            t.setTripTonnage(trip.getOrDefault(day, BigDecimal.ZERO));
            list.add(t);
        }
        return list;
    }

    /**
     * 按日汇总行转换为 日期->吨数 映射
     *
     * @param rows 汇总行
     * @return 映射
     */
    private Map<String, BigDecimal> toDayMap(List<TrendRowVO> rows) {
        return rows.stream().collect(Collectors.toMap(TrendRowVO::getDay, r -> NumberUtil.nullToZero(r.getTonnage()), BigDecimal::add));
    }

    /**
     * 按作业方向求和
     *
     * @param rows   统计行
     * @param unload true-到货，false-装货
     * @param getter 取值函数
     * @return 合计
     */
    private BigDecimal sum(List<CockpitCargoRowVO> rows, boolean unload, Function<CockpitCargoRowVO, BigDecimal> getter) {
        return rows.stream().filter(r -> isUnload(r) == unload)
                .map(r -> NumberUtil.nullToZero(getter.apply(r)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 是否到货（卸货）统计行
     *
     * @param r 统计行
     * @return true-到货
     */
    private boolean isUnload(CockpitCargoRowVO r) {
        return !StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), r.getDirection());
    }
}
