package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.CargoArriveReq;
import com.ganghuitong.port.api.dto.biz.CargoLoadReq;
import com.ganghuitong.port.api.dto.biz.CargoPageReq;
import com.ganghuitong.port.api.dto.biz.DispatchQueryReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadPageReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.BerthCargoShip;
import com.ganghuitong.port.api.entity.biz.CargoType;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.CargoDirectionEnum;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.PackTypeEnum;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.enums.ShipWorkStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.BerthCargoService;
import com.ganghuitong.port.api.service.biz.DispatchService;
import com.ganghuitong.port.api.service.biz.ShipLoadService;
import com.ganghuitong.port.api.service.biz.ShipService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.BerthCargoVO;
import com.ganghuitong.port.api.vo.biz.CargoDetailVO;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoShipMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 泊位货物服务实现（到货与装货）
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BerthCargoServiceImpl implements BerthCargoService {

    /**
     * 泊位货物数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 到货船只关联数据访问
     */
    private final BerthCargoShipMapper berthCargoShipMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 船只服务
     */
    private final ShipService shipService;

    /**
     * 分派承运服务
     */
    private final DispatchService dispatchService;

    /**
     * 装船分派服务
     */
    private final ShipLoadService shipLoadService;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 泊位到货：录入货物信息，仅“未作业”“正在作业”的泊位可用，到货后泊位变为正在作业
     *
     * @param req 到货入参
     * @return 到货记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String arrive(CargoArriveReq req) {
        Berth berth = checkBerthUsable(req.getBerthId(), "维修中的泊位不能登记到货");
        List<String> shipIds = CollUtil.distinct(req.getShipIds());
        List<ShipVO> ships = shipService.listByIds(shipIds);
        if (ships.size() != shipIds.size()) {
            throw new CustException("所选船只不存在或已删除");
        }
        BerthCargo cargo = buildCargo(berth, CargoDirectionEnum.UNLOAD, req.getCargoTypeId(), req.getCargoName(), req.getPackType(),
                req.getTonnage(), req.getVolume(), req.getPieces(), req.getRemark());
        berthCargoMapper.insert(cargo);
        for (ShipVO ship : ships) {
            BerthCargoShip rel = new BerthCargoShip();
            rel.setCargoId(cargo.getId());
            rel.setShipId(ship.getId());
            rel.setCompanyId(ship.getCompanyId());
            rel.setStatus(ShipWorkStatusEnum.WORKING.getCode());
            berthCargoShipMapper.insert(rel);
        }
        startWork(berth, cargo);
        return cargo.getId();
    }

    /**
     * 泊位装货：录入待装船的货物信息，仅“未作业”“正在作业”的泊位可用，装货后泊位变为正在作业
     *
     * @param req 装货入参
     * @return 货物记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String load(CargoLoadReq req) {
        Berth berth = checkBerthUsable(req.getBerthId(), "维修中的泊位不能登记装货");
        BerthCargo cargo = buildCargo(berth, CargoDirectionEnum.LOAD, req.getCargoTypeId(), req.getCargoName(), req.getPackType(),
                req.getTonnage(), req.getVolume(), req.getPieces(), req.getRemark());
        berthCargoMapper.insert(cargo);
        startWork(berth, cargo);
        return cargo.getId();
    }

    /**
     * 查询泊位作业中的货物（含关联船只），按到货时间倒序
     *
     * @param berthId 泊位ID
     * @return 货物列表
     */
    @Override
    public List<BerthCargoVO> listActive(String berthId) {
        List<BerthCargo> cargos = berthCargoMapper.selectList(Wrappers.<BerthCargo>lambdaQuery()
                .eq(BerthCargo::getBerthId, berthId)
                .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode())
                .orderByDesc(BerthCargo::getArriveTime));
        return assemble(cargos);
    }

    /**
     * 分页查询泊位货物（含历史，按到货时间倒序）
     *
     * @param req 分页查询入参
     * @return 货物分页数据
     */
    @Override
    public PageResp<BerthCargoVO> page(CargoPageReq req) {
        Set<String> shippingCompanyCargoIds = null;
        if (StrUtil.isNotBlank(req.getCompanyId())) {
            shippingCompanyCargoIds = cargoIdsOfShippingCompany(req.getCompanyId(), req.getDirection());
            if (shippingCompanyCargoIds.isEmpty()) {
                return PageResp.of(new Page<>(req.getPageNum(), req.getPageSize()), Collections.emptyList());
            }
        }
        Page<BerthCargo> page = berthCargoMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<BerthCargo>lambdaQuery()
                        .eq(StrUtil.isNotBlank(req.getBerthId()), BerthCargo::getBerthId, req.getBerthId())
                        .eq(StrUtil.isNotBlank(req.getDirection()), BerthCargo::getDirection, req.getDirection())
                        .eq(StrUtil.isNotBlank(req.getStatus()), BerthCargo::getStatus, req.getStatus())
                        .in(shippingCompanyCargoIds != null, BerthCargo::getId, shippingCompanyCargoIds)
                        .like(StrUtil.isNotBlank(req.getKeyword()), BerthCargo::getCargoName, req.getKeyword())
                        .orderByDesc(BerthCargo::getArriveTime));
        return PageResp.of(page, assemble(page.getRecords()));
    }

    /**
     * 查询货物详情：到货货物含分派车辆与承运商，装货货物含装船船只
     *
     * @param req 主键入参（货物ID）
     * @return 货物详情
     */
    @Override
    public CargoDetailVO detail(IdReq req) {
        BerthCargo cargo = lookup.cargo(req.getId());
        CargoDetailVO vo = new CargoDetailVO();
        vo.setCargo(assemble(List.of(cargo)).get(0));
        if (StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), cargo.getDirection())) {
            ShipLoadPageReq lq = new ShipLoadPageReq();
            lq.setCargoId(cargo.getId());
            lq.setStatuses(List.of(ShipLoadStatusEnum.WAITING.getCode(), ShipLoadStatusEnum.LOADED.getCode()));
            vo.setShipLoads(shipLoadService.list(lq));
            vo.setDispatches(Collections.emptyList());
        } else {
            DispatchQueryReq dq = new DispatchQueryReq();
            dq.setCargoId(cargo.getId());
            dq.setStatuses(List.of(DispatchStatusEnum.WAITING.getCode(), DispatchStatusEnum.DEPARTED.getCode()));
            vo.setDispatches(dispatchService.list(dq));
            vo.setShipLoads(Collections.emptyList());
        }
        return vo;
    }

    /**
     * 校验泊位可登记货物（非维修中）
     *
     * @param berthId 泊位ID
     * @param message 维修中时的提示
     * @return 泊位
     */
    private Berth checkBerthUsable(String berthId, String message) {
        Berth berth = lookup.berth(berthId);
        if (StrUtil.equals(BerthStatusEnum.MAINTENANCE.getCode(), berth.getStatus())) {
            log.info("泊位[{}]维修中，拒绝登记货物", berth.getBerthName());
            throw new CustException(message);
        }
        return berth;
    }

    /**
     * 构造泊位货物
     *
     * @param berth       泊位
     * @param direction   作业方向
     * @param cargoTypeId 货物类型ID
     * @param cargoName   货物名称
     * @param packType    货物形态
     * @param tonnage     吨位
     * @param volume      方数
     * @param pieces      件数
     * @param remark      备注
     * @return 货物实体（未入库）
     */
    private BerthCargo buildCargo(Berth berth, CargoDirectionEnum direction, String cargoTypeId, String cargoName, String packType,
                                  BigDecimal tonnage, BigDecimal volume, Integer pieces, String remark) {
        if (!CodeEnumUtil.contains(PackTypeEnum.class, packType)) {
            throw new CustException("货物形态只能是集装箱或散货");
        }
        CargoType cargoType = lookup.cargoType(cargoTypeId);
        BerthCargo cargo = new BerthCargo();
        cargo.setPortId(berth.getPortId());
        cargo.setBerthId(berth.getId());
        cargo.setDirection(direction.getCode());
        cargo.setCargoTypeId(cargoType.getId());
        cargo.setCargoTypeName(cargoType.getTypeName());
        cargo.setCargoName(cargoName);
        cargo.setPackType(packType);
        cargo.setTonnage(NumberUtil.nullToZero(tonnage));
        cargo.setVolume(NumberUtil.nullToZero(volume));
        cargo.setPieces(ObjectUtil.defaultIfNull(pieces, 0));
        cargo.setStatus(CargoStatusEnum.IN_PROGRESS.getCode());
        cargo.setArriveTime(LocalDateTime.now());
        cargo.setRemark(remark);
        return cargo;
    }

    /**
     * 登记货物后，未作业的泊位进入正在作业
     *
     * @param berth 泊位
     * @param cargo 新登记的货物
     */
    private void startWork(Berth berth, BerthCargo cargo) {
        String action = CodeEnumUtil.desc(CargoDirectionEnum.class, cargo.getDirection());
        if (StrUtil.equals(BerthStatusEnum.IDLE.getCode(), berth.getStatus())) {
            berthMapper.update(null, Wrappers.<Berth>lambdaUpdate()
                    .set(Berth::getStatus, BerthStatusEnum.WORKING.getCode())
                    .eq(Berth::getId, berth.getId())
                    .eq(Berth::getStatus, BerthStatusEnum.IDLE.getCode()));
            log.info("泊位[{}]{}，状态由未作业变为正在作业", berth.getBerthName(), action);
        } else {
            log.info("泊位[{}]作业中追加{}[{}]", berth.getBerthName(), action, cargo.getCargoName());
        }
    }

    /**
     * 查询船运公司关联的货物ID：到货按到货船只关联，装货按装船分派（不含已取消）
     *
     * @param companyId 船运公司ID
     * @param direction 作业方向（为空时两者都查）
     * @return 货物ID集合
     */
    private Set<String> cargoIdsOfShippingCompany(String companyId, String direction) {
        Set<String> ids = new LinkedHashSet<>();
        if (!StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), direction)) {
            berthCargoShipMapper.selectList(Wrappers.<BerthCargoShip>lambdaQuery()
                            .select(BerthCargoShip::getCargoId)
                            .eq(BerthCargoShip::getCompanyId, companyId))
                    .forEach(r -> ids.add(r.getCargoId()));
        }
        if (!StrUtil.equals(CargoDirectionEnum.UNLOAD.getCode(), direction)) {
            shipLoadMapper.selectList(Wrappers.<ShipLoad>lambdaQuery()
                            .select(ShipLoad::getCargoId)
                            .eq(ShipLoad::getCompanyId, companyId)
                            .ne(ShipLoad::getStatus, ShipLoadStatusEnum.CANCELLED.getCode()))
                    .forEach(r -> ids.add(r.getCargoId()));
        }
        return ids;
    }

    /**
     * 组装货物视图：补充泊位名称、关联船只（到货为船运公司船只，装货为装船船只）、分派数量
     *
     * @param cargos 货物列表
     * @return 视图列表
     */
    private List<BerthCargoVO> assemble(List<BerthCargo> cargos) {
        List<BerthCargoVO> vos = BeanUtil.copyToList(cargos, BerthCargoVO.class);
        if (CollUtil.isEmpty(vos)) {
            return vos;
        }
        List<String> cargoIds = vos.stream().map(BerthCargoVO::getId).toList();
        Map<String, List<String>> cargoShipIds = new HashMap<>();
        berthCargoShipMapper.selectList(Wrappers.<BerthCargoShip>lambdaQuery().in(BerthCargoShip::getCargoId, cargoIds))
                .forEach(r -> cargoShipIds.computeIfAbsent(r.getCargoId(), k -> new ArrayList<>()).add(r.getShipId()));
        Map<String, Integer> assignCount = new HashMap<>();
        List<ShipLoad> loads = shipLoadMapper.selectList(Wrappers.<ShipLoad>lambdaQuery()
                .select(ShipLoad::getCargoId, ShipLoad::getShipId)
                .in(ShipLoad::getCargoId, cargoIds)
                .ne(ShipLoad::getStatus, ShipLoadStatusEnum.CANCELLED.getCode()));
        for (ShipLoad l : loads) {
            List<String> ids = cargoShipIds.computeIfAbsent(l.getCargoId(), k -> new ArrayList<>());
            if (!ids.contains(l.getShipId())) {
                ids.add(l.getShipId());
            }
            assignCount.merge(l.getCargoId(), 1, Integer::sum);
        }
        dispatchMapper.selectList(Wrappers.<Dispatch>lambdaQuery()
                        .select(Dispatch::getCargoId)
                        .in(Dispatch::getCargoId, cargoIds)
                        .ne(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode()))
                .forEach(d -> assignCount.merge(d.getCargoId(), 1, Integer::sum));
        List<String> allShipIds = cargoShipIds.values().stream().flatMap(List::stream).distinct().toList();
        Map<String, ShipVO> shipMap = shipService.listByIds(allShipIds).stream()
                .collect(Collectors.toMap(ShipVO::getId, Function.identity()));
        Map<String, String> berthNames = berthMapper.selectBatchIds(vos.stream().map(BerthCargoVO::getBerthId).distinct().toList())
                .stream().collect(Collectors.toMap(Berth::getId, Berth::getBerthName));
        for (BerthCargoVO vo : vos) {
            vo.setShips(cargoShipIds.getOrDefault(vo.getId(), Collections.emptyList()).stream()
                    .map(shipMap::get)
                    .filter(ObjectUtil::isNotNull)
                    .toList());
            vo.setAssignCount(assignCount.getOrDefault(vo.getId(), 0));
            vo.setBerthName(berthNames.get(vo.getBerthId()));
        }
        return vos;
    }
}
