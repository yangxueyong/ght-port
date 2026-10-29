package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.DepartReq;
import com.ganghuitong.port.api.dto.biz.DispatchAssignReq;
import com.ganghuitong.port.api.dto.biz.DispatchQueryReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.CargoDirectionEnum;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.DispatchService;
import com.ganghuitong.port.api.service.biz.GatePassService;
import com.ganghuitong.port.api.vo.biz.DispatchVO;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import com.ganghuitong.port.service.helper.DepartHelper;
import com.ganghuitong.port.service.helper.DispatchAssembler;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 分派承运服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 出门条服务
     */
    private final GatePassService gatePassService;

    /**
     * 分派视图组装器
     */
    private final DispatchAssembler dispatchAssembler;

    /**
     * 车辆出发辅助
     */
    private final DepartHelper departHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 分派承运：为正在作业泊位的货物分派承运商车辆，可重复分派同一车辆
     *
     * @param req 分派入参（泊位、货物、承运商、车辆列表）
     * @return 本次新增的分派记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<DispatchVO> assign(DispatchAssignReq req) {
        Berth berth = lookup.berth(req.getBerthId());
        checkBerthWorking(berth);
        BerthCargo cargo = checkCargo(req.getCargoId(), berth.getId());
        Carrier carrier = lookup.carrier(req.getCarrierId());
        if (!StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), carrier.getStatus())) {
            throw new CustException("承运商【" + carrier.getCarrierName() + "】已停用");
        }
        List<String> vehicleIds = CollUtil.distinct(req.getVehicleIds());
        List<Vehicle> vehicles = vehicleMapper.selectBatchIds(vehicleIds);
        if (vehicles.size() != vehicleIds.size()) {
            throw new CustException("所选车辆不存在或已删除");
        }
        List<Dispatch> created = new ArrayList<>();
        for (Vehicle v : vehicles) {
            if (!StrUtil.equals(carrier.getId(), v.getCarrierId())) {
                log.info("车辆[{}]不属于承运商[{}]", v.getPlateNo(), carrier.getCarrierName());
                throw new CustException("车辆【" + v.getPlateNo() + "】不属于所选承运商");
            }
            if (!StrUtil.equals(EnableStatusEnum.ENABLED.getCode(), v.getStatus())) {
                throw new CustException("车辆【" + v.getPlateNo() + "】已停用");
            }
            Dispatch d = new Dispatch();
            d.setBerthId(berth.getId());
            d.setCargoId(cargo.getId());
            d.setCarrierId(carrier.getId());
            d.setVehicleId(v.getId());
            d.setStatus(DispatchStatusEnum.WAITING.getCode());
            dispatchMapper.insert(d);
            created.add(d);
        }
        log.info("泊位[{}]货物[{}]分派承运商[{}]车辆{}辆", berth.getBerthName(), cargo.getCargoName(),
                carrier.getCarrierName(), created.size());
        return dispatchAssembler.assemble(created);
    }

    /**
     * 查询分派记录（含车辆、运输单、出门条信息）
     *
     * @param req 查询入参（泊位ID、货物ID、状态）
     * @return 分派记录列表
     */
    @Override
    public List<DispatchVO> list(DispatchQueryReq req) {
        List<Dispatch> list = dispatchMapper.selectList(Wrappers.<Dispatch>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getBerthId()), Dispatch::getBerthId, req.getBerthId())
                .eq(StrUtil.isNotBlank(req.getCargoId()), Dispatch::getCargoId, req.getCargoId())
                .in(CollUtil.isNotEmpty(req.getStatuses()), Dispatch::getStatus, req.getStatuses())
                .orderByAsc(Dispatch::getCreateTime));
        return dispatchAssembler.assemble(list);
    }

    /**
     * 装载完成，立即出发：生成运输单与出门条，并将出门条链接发送给司机
     *
     * @param req 出发入参（分派ID、装载吨数/方数/件数）
     * @return 出门条信息（含司机访问链接）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public GatePassVO depart(DepartReq req) {
        Dispatch dispatch = lookup.dispatch(req.getDispatchId());
        if (!StrUtil.equals(DispatchStatusEnum.WAITING.getCode(), dispatch.getStatus())) {
            log.info("分派[{}]状态为{}，不能出发", dispatch.getId(), dispatch.getStatus());
            throw new CustException("该分派已出发或已取消");
        }
        Berth berth = lookup.berth(dispatch.getBerthId());
        checkBerthWorking(berth);
        BerthCargo cargo = checkCargo(dispatch.getCargoId(), berth.getId());
        departHelper.checkLoad(req, cargo);
        Vehicle vehicle = lookup.vehicle(dispatch.getVehicleId());
        departHelper.lockVehicle(vehicle);
        TransportTrip trip = departHelper.createTrip(dispatch, cargo, vehicle, req);
        return gatePassService.issue(trip);
    }

    /**
     * 取消待装载的分派
     *
     * @param req 主键入参（分派ID）
     */
    @Override
    public void cancel(IdReq req) {
        int rows = dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                .set(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode())
                .eq(Dispatch::getId, req.getId())
                .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
        if (rows == 0) {
            log.info("分派[{}]不是待装载状态，取消失败", req.getId());
            throw new CustException("只有待装载的分派可以取消");
        }
    }

    /**
     * 校验泊位为正在作业
     *
     * @param berth 泊位
     */
    private void checkBerthWorking(Berth berth) {
        if (!StrUtil.equals(BerthStatusEnum.WORKING.getCode(), berth.getStatus())) {
            log.info("泊位[{}]状态为{}，不能分派承运", berth.getBerthName(), berth.getStatus());
            throw new CustException("只有“正在作业”的泊位才能分派承运");
        }
    }

    /**
     * 校验货物属于该泊位且在作业中
     *
     * @param cargoId 到货ID
     * @param berthId 泊位ID
     * @return 到货实体
     */
    private BerthCargo checkCargo(String cargoId, String berthId) {
        BerthCargo cargo = lookup.cargo(cargoId);
        if (!StrUtil.equals(berthId, cargo.getBerthId())) {
            throw new CustException("该货物不属于当前泊位");
        }
        if (StrUtil.equals(CargoDirectionEnum.LOAD.getCode(), cargo.getDirection())) {
            throw new CustException("装货货物请使用“分派船只”安排装船");
        }
        if (!StrUtil.equals(CargoStatusEnum.IN_PROGRESS.getCode(), cargo.getStatus())) {
            throw new CustException("该批货物已完成作业");
        }
        return cargo;
    }
}
