package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.ShipLoadAssignReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadConfirmReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadPageReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.CargoDirectionEnum;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.ShipLoadService;
import com.ganghuitong.port.api.service.biz.ShipService;
import com.ganghuitong.port.api.vo.biz.ShipLoadVO;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import com.ganghuitong.port.service.helper.DepartHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 装船分派服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipLoadServiceImpl implements ShipLoadService {

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 泊位货物数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 船只服务
     */
    private final ShipService shipService;

    /**
     * 装载量校验辅助
     */
    private final DepartHelper departHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 分派船只：为正在作业泊位的装货货物分派船只，可多选
     *
     * @param req 分派入参（泊位、货物、船只列表）
     * @return 本次新增的装船分派记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ShipLoadVO> assign(ShipLoadAssignReq req) {
        Berth berth = lookup.berth(req.getBerthId());
        checkBerthWorking(berth);
        BerthCargo cargo = checkCargo(req.getCargoId(), berth.getId());
        List<String> shipIds = CollUtil.distinct(req.getShipIds());
        List<ShipVO> ships = shipService.listByIds(shipIds);
        if (ships.size() != shipIds.size()) {
            throw new CustException("所选船只不存在或已删除");
        }
        List<ShipLoad> created = new ArrayList<>();
        for (ShipVO ship : ships) {
            ShipLoad load = new ShipLoad();
            load.setPortId(berth.getPortId());
            load.setBerthId(berth.getId());
            load.setCargoId(cargo.getId());
            load.setShipId(ship.getId());
            load.setCompanyId(ship.getCompanyId());
            load.setStatus(ShipLoadStatusEnum.WAITING.getCode());
            shipLoadMapper.insert(load);
            created.add(load);
        }
        log.info("泊位[{}]装货[{}]分派船只{}艘", berth.getBerthName(), cargo.getCargoName(), created.size());
        return assemble(created);
    }

    /**
     * 查询装船分派记录（不分页）
     *
     * @param req 查询入参（泊位、货物、状态）
     * @return 装船分派记录
     */
    @Override
    public List<ShipLoadVO> list(ShipLoadPageReq req) {
        return assemble(shipLoadMapper.selectList(buildQuery(req).orderByAsc(ShipLoad::getCreateTime)));
    }

    /**
     * 分页查询装船记录
     *
     * @param req 分页查询入参
     * @return 装船记录分页数据
     */
    @Override
    public PageResp<ShipLoadVO> page(ShipLoadPageReq req) {
        Page<ShipLoad> page = shipLoadMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                buildQuery(req).orderByDesc(ShipLoad::getCreateTime));
        return PageResp.of(page, assemble(page.getRecords()));
    }

    /**
     * 装船完成：登记装船量并累加到货物已装载量
     *
     * @param req 装船完成入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(ShipLoadConfirmReq req) {
        ShipLoad load = lookup.shipLoad(req.getId());
        if (!StrUtil.equals(ShipLoadStatusEnum.WAITING.getCode(), load.getStatus())) {
            log.info("装船分派[{}]状态为{}，不能确认装船", load.getId(), load.getStatus());
            throw new CustException("该船只已装船或已取消");
        }
        Berth berth = lookup.berth(load.getBerthId());
        checkBerthWorking(berth);
        BerthCargo cargo = checkCargo(load.getCargoId(), berth.getId());
        departHelper.checkLoad(req.getLoadTonnage(), req.getLoadVolume(), req.getLoadPieces(), cargo);
        int rows = shipLoadMapper.update(null, Wrappers.<ShipLoad>lambdaUpdate()
                .set(ShipLoad::getStatus, ShipLoadStatusEnum.LOADED.getCode())
                .set(ShipLoad::getLoadTonnage, NumberUtil.nullToZero(req.getLoadTonnage()))
                .set(ShipLoad::getLoadVolume, NumberUtil.nullToZero(req.getLoadVolume()))
                .set(ShipLoad::getLoadPieces, ObjectUtil.defaultIfNull(req.getLoadPieces(), 0))
                .set(ShipLoad::getLoadTime, LocalDateTime.now())
                .eq(ShipLoad::getId, load.getId())
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
        if (rows == 0) {
            log.info("装船分派[{}]状态已变化，确认装船失败", load.getId());
            throw new CustException("该船只已装船或已取消，请刷新后重试");
        }
        berthCargoMapper.increaseLoaded(cargo.getId(), NumberUtil.nullToZero(req.getLoadTonnage()),
                NumberUtil.nullToZero(req.getLoadVolume()), ObjectUtil.defaultIfNull(req.getLoadPieces(), 0));
        log.info("泊位[{}]货物[{}]装船完成，船只[{}]装船{}吨", berth.getBerthName(), cargo.getCargoName(),
                load.getShipId(), req.getLoadTonnage());
    }

    /**
     * 取消待装船的分派
     *
     * @param req 主键入参（装船分派ID）
     */
    @Override
    public void cancel(IdReq req) {
        int rows = shipLoadMapper.update(null, Wrappers.<ShipLoad>lambdaUpdate()
                .set(ShipLoad::getStatus, ShipLoadStatusEnum.CANCELLED.getCode())
                .eq(ShipLoad::getId, req.getId())
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
        if (rows == 0) {
            log.info("装船分派[{}]不是待装船状态，取消失败", req.getId());
            throw new CustException("只有待装船的分派可以取消");
        }
    }

    /**
     * 构造查询条件
     *
     * @param req 查询入参
     * @return 查询条件
     */
    private LambdaQueryWrapper<ShipLoad> buildQuery(ShipLoadPageReq req) {
        return Wrappers.<ShipLoad>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getBerthId()), ShipLoad::getBerthId, req.getBerthId())
                .eq(StrUtil.isNotBlank(req.getCargoId()), ShipLoad::getCargoId, req.getCargoId())
                .eq(StrUtil.isNotBlank(req.getCompanyId()), ShipLoad::getCompanyId, req.getCompanyId())
                .in(CollUtil.isNotEmpty(req.getStatuses()), ShipLoad::getStatus, req.getStatuses());
    }

    /**
     * 组装装船分派视图：补充船只、船运公司、泊位、货物信息
     *
     * @param loads 装船分派
     * @return 视图列表
     */
    private List<ShipLoadVO> assemble(List<ShipLoad> loads) {
        if (CollUtil.isEmpty(loads)) {
            return Collections.emptyList();
        }
        Map<String, ShipVO> shipMap = shipService.listByIds(loads.stream().map(ShipLoad::getShipId).distinct().toList())
                .stream().collect(Collectors.toMap(ShipVO::getId, Function.identity()));
        Map<String, BerthCargo> cargoMap = berthCargoMapper.selectBatchIds(loads.stream().map(ShipLoad::getCargoId).distinct().toList())
                .stream().collect(Collectors.toMap(BerthCargo::getId, Function.identity()));
        Map<String, String> berthNames = berthMapper.selectBatchIds(loads.stream().map(ShipLoad::getBerthId).distinct().toList())
                .stream().collect(Collectors.toMap(Berth::getId, Berth::getBerthName));
        return loads.stream().map(l -> {
            ShipLoadVO vo = BeanUtil.copyProperties(l, ShipLoadVO.class);
            ShipVO ship = shipMap.get(l.getShipId());
            if (ship != null) {
                vo.setShipName(ship.getShipName());
                vo.setShipType(ship.getShipType());
                vo.setShipTypeDesc(ship.getShipTypeDesc());
                vo.setShipLength(ship.getShipLength());
                vo.setHullColor(ship.getHullColor());
                vo.setCompanyName(ship.getCompanyName());
            }
            BerthCargo cargo = cargoMap.get(l.getCargoId());
            if (cargo != null) {
                vo.setCargoName(cargo.getCargoName());
                vo.setCargoTypeName(cargo.getCargoTypeName());
                vo.setPackType(cargo.getPackType());
            }
            vo.setBerthName(berthNames.get(l.getBerthId()));
            return vo;
        }).toList();
    }

    /**
     * 校验泊位为正在作业
     *
     * @param berth 泊位
     */
    private void checkBerthWorking(Berth berth) {
        if (!StrUtil.equals(BerthStatusEnum.WORKING.getCode(), berth.getStatus())) {
            log.info("泊位[{}]状态为{}，不能装船", berth.getBerthName(), berth.getStatus());
            throw new CustException("只有“正在作业”的泊位才能分派装船");
        }
    }

    /**
     * 校验货物为该泊位作业中的装货货物
     *
     * @param cargoId 货物ID
     * @param berthId 泊位ID
     * @return 货物实体
     */
    private BerthCargo checkCargo(String cargoId, String berthId) {
        BerthCargo cargo = lookup.cargo(cargoId);
        if (!StrUtil.equals(berthId, cargo.getBerthId())) {
            throw new CustException("该货物不属于当前泊位");
        }
        if (!StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), cargo.getDirection())) {
            throw new CustException("到货货物请使用“分派承运”安排车辆运出");
        }
        if (!StrUtil.equals(CargoStatusEnum.IN_PROGRESS.getCode(), cargo.getStatus())) {
            throw new CustException("该批货物已完成作业");
        }
        return cargo;
    }
}
