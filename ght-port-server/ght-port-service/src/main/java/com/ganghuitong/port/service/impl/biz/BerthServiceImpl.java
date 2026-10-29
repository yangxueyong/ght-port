package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.BerthQueryReq;
import com.ganghuitong.port.api.dto.biz.BerthStatusReq;
import com.ganghuitong.port.api.dto.biz.BerthWorkTimeReq;
import com.ganghuitong.port.api.dto.biz.DispatchQueryReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadPageReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.CargoDirectionEnum;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.BerthCargoService;
import com.ganghuitong.port.api.service.biz.BerthService;
import com.ganghuitong.port.api.service.biz.DispatchService;
import com.ganghuitong.port.api.service.biz.ShipLoadService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.BerthCargoVO;
import com.ganghuitong.port.api.vo.biz.BerthDetailVO;
import com.ganghuitong.port.api.vo.biz.BerthVO;
import com.ganghuitong.port.api.vo.biz.CarrierOptionVO;
import com.ganghuitong.port.api.vo.biz.CountVO;
import com.ganghuitong.port.api.vo.biz.DispatchVO;
import com.ganghuitong.port.api.vo.biz.ShipLoadVO;
import com.ganghuitong.port.service.helper.BerthWorkHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 泊位服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BerthServiceImpl implements BerthService {

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 泊位到货服务
     */
    private final BerthCargoService berthCargoService;

    /**
     * 分派承运服务
     */
    private final DispatchService dispatchService;

    /**
     * 装船分派服务
     */
    private final ShipLoadService shipLoadService;

    /**
     * 泊位作业状态辅助
     */
    private final BerthWorkHelper berthWorkHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 查询泊位列表（含作业统计）
     *
     * @param req 泊位查询入参（港口ID、状态）
     * @return 泊位列表
     */
    @Override
    public List<BerthVO> list(BerthQueryReq req) {
        List<Berth> berths = berthMapper.selectList(Wrappers.<Berth>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getPortId()), Berth::getPortId, req.getPortId())
                .eq(StrUtil.isNotBlank(req.getStatus()), Berth::getStatus, req.getStatus())
                .orderByAsc(Berth::getBerthCode));
        List<BerthVO> vos = BeanUtil.copyToList(berths, BerthVO.class);
        if (CollUtil.isEmpty(vos)) {
            return vos;
        }
        Set<String> ids = vos.stream().map(BerthVO::getId).collect(Collectors.toSet());
        Map<String, Integer> transit = toMap(transportTripMapper.countByBerth(ids, TripStatusEnum.IN_TRANSIT.getCode()));
        Map<String, Integer> waiting = toMap(dispatchMapper.countByBerth(ids, DispatchStatusEnum.WAITING.getCode()));
        Map<String, Long> cargoCount = berthCargoMapper.selectList(Wrappers.<BerthCargo>lambdaQuery()
                        .select(BerthCargo::getId, BerthCargo::getBerthId)
                        .in(BerthCargo::getBerthId, ids)
                        .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()))
                .stream().collect(Collectors.groupingBy(BerthCargo::getBerthId, Collectors.counting()));
        for (BerthVO vo : vos) {
            vo.setStatusDesc(CodeEnumUtil.desc(BerthStatusEnum.class, vo.getStatus()));
            vo.setInTransitCount(transit.getOrDefault(vo.getId(), 0));
            vo.setWaitingCount(waiting.getOrDefault(vo.getId(), 0));
            vo.setActiveCargoCount(cargoCount.getOrDefault(vo.getId(), 0L).intValue());
        }
        return vos;
    }

    /**
     * 查询泊位详情：作业中的货物、接驳承运商、相关车辆
     *
     * @param req 主键入参（泊位ID）
     * @return 泊位详情
     */
    @Override
    public BerthDetailVO detail(IdReq req) {
        Berth berth = lookup.berth(req.getId());
        BerthVO berthVo = BeanUtil.copyProperties(berth, BerthVO.class);
        berthVo.setStatusDesc(CodeEnumUtil.desc(BerthStatusEnum.class, berth.getStatus()));
        List<BerthCargoVO> cargos = berthCargoService.listActive(berth.getId());
        List<DispatchVO> dispatches = new ArrayList<>();
        if (CollUtil.isNotEmpty(cargos)) {
            DispatchQueryReq dq = new DispatchQueryReq();
            dq.setBerthId(berth.getId());
            dq.setStatuses(List.of(DispatchStatusEnum.WAITING.getCode(), DispatchStatusEnum.DEPARTED.getCode()));
            Set<String> cargoIds = cargos.stream().map(BerthCargoVO::getId).collect(Collectors.toSet());
            dispatches = dispatchService.list(dq).stream().filter(d -> cargoIds.contains(d.getCargoId())).toList();
        }
        berthVo.setActiveCargoCount(cargos.size());
        berthVo.setWaitingCount((int) dispatches.stream()
                .filter(d -> StrUtil.equals(DispatchStatusEnum.WAITING.getCode(), d.getStatus())).count());
        berthVo.setInTransitCount((int) dispatches.stream()
                .filter(d -> d.getTrip() != null && StrUtil.equals(TripStatusEnum.IN_TRANSIT.getCode(), d.getTrip().getStatus()))
                .count());
        List<ShipLoadVO> shipLoads = new ArrayList<>();
        if (cargos.stream().anyMatch(c -> StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), c.getDirection()))) {
            ShipLoadPageReq lq = new ShipLoadPageReq();
            lq.setBerthId(berth.getId());
            lq.setStatuses(List.of(ShipLoadStatusEnum.WAITING.getCode(), ShipLoadStatusEnum.LOADED.getCode()));
            Set<String> cargoIds = cargos.stream().map(BerthCargoVO::getId).collect(Collectors.toSet());
            shipLoads = shipLoadService.list(lq).stream().filter(l -> cargoIds.contains(l.getCargoId())).toList();
        }
        BerthDetailVO vo = new BerthDetailVO();
        vo.setBerth(berthVo);
        vo.setPortName(lookup.port(berth.getPortId()).getPortName());
        vo.setCargos(cargos);
        vo.setDispatches(dispatches);
        vo.setShipLoads(shipLoads);
        vo.setCarriers(collectCarriers(dispatches));
        return vo;
    }

    /**
     * 设置泊位作业时间等参数；泊位正在作业时需用户确认
     *
     * @param req 作业时间入参
     * @return 确认结果
     */
    @Override
    public ConfirmResp saveWorkTime(BerthWorkTimeReq req) {
        Berth berth = lookup.berth(req.getId());
        if (!req.getWorkStartTime().isBefore(req.getWorkEndTime())) {
            throw new CustException("作业开始时间必须早于结束时间");
        }
        if (isWorking(berth) && !BooleanUtil.isTrue(req.getForce())) {
            log.info("泊位[{}]正在作业，修改作业时间需确认", berth.getBerthName());
            return ConfirmResp.confirm("泊位【" + berth.getBerthName() + "】正在作业，是否继续修改作业时间？");
        }
        Berth update = new Berth();
        update.setId(berth.getId());
        update.setWorkStartTime(req.getWorkStartTime());
        update.setWorkEndTime(req.getWorkEndTime());
        update.setBerthLength(req.getBerthLength());
        update.setWaterDepth(req.getWaterDepth());
        update.setMaxTonnage(req.getMaxTonnage());
        berthMapper.updateById(update);
        log.info("泊位[{}]作业时间设置为{}-{}", berth.getBerthName(), req.getWorkStartTime(), req.getWorkEndTime());
        return ConfirmResp.done(berth.getId());
    }

    /**
     * 变更泊位状态（转维修/恢复未作业）；泊位正在作业时需用户确认
     *
     * @param req 状态变更入参
     * @return 确认结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmResp changeStatus(BerthStatusReq req) {
        Berth berth = lookup.berth(req.getId());
        String target = req.getStatus();
        if (StrUtil.equals(BerthStatusEnum.MAINTENANCE.getCode(), target)) {
            if (isWorking(berth) && !BooleanUtil.isTrue(req.getForce())) {
                log.info("泊位[{}]正在作业，转维修需确认", berth.getBerthName());
                return ConfirmResp.confirm("泊位【" + berth.getBerthName() + "】正在作业，转为维修将结束当前作业并取消待装载车辆，是否继续？");
            }
            if (isWorking(berth)) {
                berthWorkHelper.closeWork(berth.getId());
            }
        } else if (StrUtil.equals(BerthStatusEnum.IDLE.getCode(), target)) {
            if (isWorking(berth)) {
                log.info("泊位[{}]正在作业，不能直接恢复为未作业", berth.getBerthName());
                throw new CustException("泊位正在作业，请使用“完成作业”结束作业");
            }
        } else {
            log.info("泊位状态变更目标[{}]不合法", target);
            throw new CustException("只能将泊位设为“维修中”或“未作业”");
        }
        berthWorkHelper.updateStatus(berth.getId(), target);
        log.info("泊位[{}]状态由{}变更为{}", berth.getBerthName(), berth.getStatus(), target);
        return ConfirmResp.done(berth.getId());
    }

    /**
     * 完成泊位作业：运输中车辆完成运输、卸货船只完成、货物批次置为完成、取消待装载/待装船分派、泊位恢复未作业
     *
     * @param req 主键入参（泊位ID，force=true时忽略未出发车辆提示）
     * @return 确认结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmResp finishWork(IdReq req) {
        Berth berth = lookup.berth(req.getId());
        if (!isWorking(berth)) {
            throw new CustException("泊位当前未在作业");
        }
        Map<String, Integer> waiting = toMap(dispatchMapper.countByBerth(Set.of(berth.getId()), DispatchStatusEnum.WAITING.getCode()));
        int waitingCount = waiting.getOrDefault(berth.getId(), 0);
        long transitCount = berthWorkHelper.countInTransit(berth.getId());
        long waitingShips = shipLoadMapper.selectCount(Wrappers.<ShipLoad>lambdaQuery()
                .eq(ShipLoad::getBerthId, berth.getId())
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
        if ((waitingCount > 0 || transitCount > 0 || waitingShips > 0) && !BooleanUtil.isTrue(req.getForce())) {
            List<String> parts = new ArrayList<>();
            if (transitCount > 0) {
                parts.add(transitCount + "辆车运输中（将设为完成运输）");
            }
            if (waitingCount > 0) {
                parts.add(waitingCount + "辆车待装载（将取消分派）");
            }
            if (waitingShips > 0) {
                parts.add(waitingShips + "艘船待装船（将取消装船分派）");
            }
            log.info("泊位[{}]完成作业需确认：{}", berth.getBerthName(), parts);
            return ConfirmResp.confirm("泊位【" + berth.getBerthName() + "】还有" + String.join("、", parts)
                    + "，卸货船只将一并完成作业，是否继续？");
        }
        berthWorkHelper.completeTrips(berth.getId());
        berthWorkHelper.closeWork(berth.getId());
        berthWorkHelper.updateStatus(berth.getId(), BerthStatusEnum.IDLE.getCode());
        log.info("泊位[{}]已完成作业", berth.getBerthName());
        return ConfirmResp.done(berth.getId());
    }

    /**
     * 判断泊位是否正在作业
     *
     * @param berth 泊位
     * @return true-正在作业
     */
    private boolean isWorking(Berth berth) {
        return StrUtil.equals(BerthStatusEnum.WORKING.getCode(), berth.getStatus());
    }

    /**
     * 将计数列表转换为 业务ID->数量 的映射
     *
     * @param counts 计数列表
     * @return 映射
     */
    private Map<String, Integer> toMap(List<CountVO> counts) {
        return counts.stream().collect(Collectors.toMap(CountVO::getBizId, CountVO::getCnt, Integer::sum));
    }

    /**
     * 从分派记录中汇总接驳承运商
     *
     * @param dispatches 分派记录
     * @return 去重后的承运商
     */
    private List<CarrierOptionVO> collectCarriers(List<DispatchVO> dispatches) {
        Map<String, CarrierOptionVO> map = new LinkedHashMap<>();
        for (DispatchVO d : dispatches) {
            map.computeIfAbsent(d.getCarrierId(), k -> {
                CarrierOptionVO c = new CarrierOptionVO();
                c.setId(d.getCarrierId());
                c.setCarrierName(d.getCarrierName());
                return c;
            });
        }
        return new ArrayList<>(map.values());
    }
}
