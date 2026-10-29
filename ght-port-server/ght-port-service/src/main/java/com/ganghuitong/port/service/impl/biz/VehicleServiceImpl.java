package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.VehiclePageReq;
import com.ganghuitong.port.api.dto.biz.VehicleSaveReq;
import com.ganghuitong.port.api.dto.sys.AccountProvisionReq;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.enums.RoleCodeEnum;
import com.ganghuitong.port.api.enums.UserTypeEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.enums.VehicleTypeEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.VehicleService;
import com.ganghuitong.port.api.service.sys.AccountProvisionService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.VehicleVO;
import com.ganghuitong.port.service.helper.DataScopeHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.helper.VehicleAssembler;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 车辆服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 账号开通服务
     */
    private final AccountProvisionService accountProvisionService;

    /**
     * 车辆视图组装器
     */
    private final VehicleAssembler vehicleAssembler;

    /**
     * 数据范围辅助
     */
    private final DataScopeHelper dataScopeHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 分页查询车辆（承运商账号仅能看到本承运商车辆）
     *
     * @param req 分页查询入参
     * @return 车辆分页数据
     */
    @Override
    public PageResp<VehicleVO> page(VehiclePageReq req) {
        String carrierId = dataScopeHelper.resolveCarrierId(req.getCarrierId());
        Page<Vehicle> page = vehicleMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<Vehicle>lambdaQuery()
                        .eq(StrUtil.isNotBlank(carrierId), Vehicle::getCarrierId, carrierId)
                        .eq(StrUtil.isNotBlank(req.getTransportStatus()), Vehicle::getTransportStatus, req.getTransportStatus())
                        .eq(StrUtil.isNotBlank(req.getVehicleType()), Vehicle::getVehicleType, req.getVehicleType())
                        .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(Vehicle::getPlateNo, req.getKeyword())
                                .or().like(Vehicle::getDriverName, req.getKeyword())
                                .or().like(Vehicle::getDriverPhone, req.getKeyword()))
                        .orderByDesc(Vehicle::getTransportStatus)
                        .orderByAsc(Vehicle::getPlateNo));
        return PageResp.of(page, vehicleAssembler.assemble(page.getRecords()));
    }

    /**
     * 车辆下拉搜索（必须指定承运商，仅返回该承运商下的启用车辆）
     *
     * @param req 关键字入参（parentId为承运商ID）
     * @return 车辆列表
     */
    @Override
    public List<VehicleVO> options(KeywordReq req) {
        String carrierId = dataScopeHelper.resolveCarrierId(req.getParentId());
        if (StrUtil.isBlank(carrierId)) {
            throw new CustException("请先选择承运商");
        }
        List<Vehicle> list = vehicleMapper.selectList(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getCarrierId, carrierId)
                .eq(Vehicle::getStatus, EnableStatusEnum.ENABLED.getCode())
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(Vehicle::getPlateNo, req.getKeyword())
                        .or().like(Vehicle::getDriverName, req.getKeyword()))
                .orderByAsc(Vehicle::getPlateNo)
                .last(CommonConstant.LIMIT_SQL + CommonConstant.SELECT_LIMIT));
        return vehicleAssembler.assemble(list);
    }

    /**
     * 新增或修改车辆；新增或更换司机电话时为司机开通账号；修改运输中的车辆需用户确认
     *
     * @param req 车辆保存入参
     * @return 确认结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmResp save(VehicleSaveReq req) {
        String carrierId = dataScopeHelper.resolveCarrierId(req.getCarrierId());
        if (StrUtil.isBlank(carrierId)) {
            throw new CustException("请选择所属承运商");
        }
        Carrier carrier = lookup.carrier(carrierId);
        if (!CodeEnumUtil.contains(VehicleTypeEnum.class, req.getVehicleType())) {
            throw new CustException("车型不正确");
        }
        Long dup = vehicleMapper.selectCount(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getPlateNo, req.getPlateNo())
                .ne(StrUtil.isNotBlank(req.getId()), Vehicle::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("车牌号【" + req.getPlateNo() + "】已存在");
        }
        Vehicle vehicle = BeanUtil.copyProperties(req, Vehicle.class);
        vehicle.setCarrierId(carrier.getId());
        vehicle.setStatus(StrUtil.blankToDefault(req.getStatus(), EnableStatusEnum.ENABLED.getCode()));
        boolean needAccount;
        if (StrUtil.isBlank(req.getId())) {
            vehicle.setTransportStatus(VehicleStatusEnum.IDLE.getCode());
            vehicleMapper.insert(vehicle);
            needAccount = true;
            log.info("承运商[{}]新增车辆[{}]", carrier.getCarrierName(), vehicle.getPlateNo());
        } else {
            Vehicle old = lookup.vehicle(req.getId());
            dataScopeHelper.checkCarrier(old.getCarrierId());
            boolean inTransit = StrUtil.equals(VehicleStatusEnum.IN_TRANSIT.getCode(), old.getTransportStatus());
            if (inTransit && !BooleanUtil.isTrue(req.getForce())) {
                log.info("车辆[{}]运输中，修改需确认", old.getPlateNo());
                return ConfirmResp.confirm("车辆【" + old.getPlateNo() + "】正在运输中，修改信息可能影响本次运输与出门条核对，是否继续修改？");
            }
            if (inTransit && !StrUtil.equals(old.getCarrierId(), carrier.getId())) {
                throw new CustException("运输中的车辆不能变更所属承运商");
            }
            vehicle.setTransportStatus(null);
            vehicleMapper.updateById(vehicle);
            needAccount = !StrUtil.equals(old.getDriverPhone(), req.getDriverPhone());
            log.info("修改车辆[{}]，是否更换司机账号：{}", vehicle.getPlateNo(), needAccount);
        }
        if (needAccount) {
            provisionDriver(vehicle);
        }
        return ConfirmResp.done(vehicle.getId());
    }

    /**
     * 删除车辆；运输中的车辆不可删除，同时取消其待装载分派
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        Vehicle vehicle = lookup.vehicle(req.getId());
        dataScopeHelper.checkCarrier(vehicle.getCarrierId());
        if (StrUtil.equals(VehicleStatusEnum.IN_TRANSIT.getCode(), vehicle.getTransportStatus())) {
            log.info("车辆[{}]运输中，不允许删除", vehicle.getPlateNo());
            throw new CustException("车辆【" + vehicle.getPlateNo() + "】正在运输中，不能删除");
        }
        vehicleMapper.deleteById(vehicle.getId());
        dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                .set(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode())
                .eq(Dispatch::getVehicleId, vehicle.getId())
                .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
    }

    /**
     * 为司机开通账号并赋予司机角色，回写车辆的司机账号ID
     *
     * @param vehicle 车辆
     */
    private void provisionDriver(Vehicle vehicle) {
        AccountProvisionReq account = new AccountProvisionReq();
        account.setUsername(vehicle.getDriverPhone());
        account.setNickname(vehicle.getDriverName());
        account.setUserType(UserTypeEnum.DRIVER.getCode());
        account.setRefId(vehicle.getId());
        account.setRoleCode(RoleCodeEnum.DRIVER.getCode());
        Vehicle update = new Vehicle();
        update.setId(vehicle.getId());
        update.setDriverUserId(accountProvisionService.provision(account));
        vehicleMapper.updateById(update);
    }
}
