package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.biz.CarrierPageReq;
import com.ganghuitong.port.api.dto.biz.CarrierSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.sys.AccountProvisionReq;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.EnableStatusEnum;
import com.ganghuitong.port.api.enums.RoleCodeEnum;
import com.ganghuitong.port.api.enums.TripStatusEnum;
import com.ganghuitong.port.api.enums.UserTypeEnum;
import com.ganghuitong.port.api.enums.VehicleStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.CarrierService;
import com.ganghuitong.port.api.service.sys.AccountProvisionService;
import com.ganghuitong.port.api.vo.biz.CarrierDetailVO;
import com.ganghuitong.port.api.vo.biz.CarrierOptionVO;
import com.ganghuitong.port.api.vo.biz.CarrierVO;
import com.ganghuitong.port.service.helper.CarrierAssembler;
import com.ganghuitong.port.service.helper.DataScopeHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.helper.TripAssembler;
import com.ganghuitong.port.service.helper.VehicleAssembler;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 承运商服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CarrierServiceImpl implements CarrierService {

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 账号开通服务
     */
    private final AccountProvisionService accountProvisionService;

    /**
     * 承运商视图组装器
     */
    private final CarrierAssembler carrierAssembler;

    /**
     * 车辆视图组装器
     */
    private final VehicleAssembler vehicleAssembler;

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
     * 分页查询承运商（含车辆数、运输统计）
     *
     * @param req 分页查询入参
     * @return 承运商分页数据
     */
    @Override
    public PageResp<CarrierVO> page(CarrierPageReq req) {
        LambdaQueryWrapper<Carrier> wrapper = Wrappers.<Carrier>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getStatus()), Carrier::getStatus, req.getStatus())
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(Carrier::getCarrierName, req.getKeyword())
                        .or().like(Carrier::getContactName, req.getKeyword())
                        .or().like(Carrier::getContactPhone, req.getKeyword()))
                .orderByDesc(Carrier::getCreateTime);
        if (BooleanUtil.isTrue(req.getOnlyInTransit())) {
            List<String> carrierIds = vehicleMapper.selectList(Wrappers.<Vehicle>lambdaQuery()
                            .select(Vehicle::getCarrierId)
                            .eq(Vehicle::getTransportStatus, VehicleStatusEnum.IN_TRANSIT.getCode()))
                    .stream().map(Vehicle::getCarrierId).distinct().toList();
            if (CollUtil.isEmpty(carrierIds)) {
                log.info("当前没有运输中的承运商");
                return PageResp.of(new Page<Carrier>(req.getPageNum(), req.getPageSize()), List.of());
            }
            wrapper.in(Carrier::getId, carrierIds);
        }
        Page<Carrier> page = carrierMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageResp.of(page, carrierAssembler.assemble(page.getRecords()));
    }

    /**
     * 承运商下拉搜索
     *
     * @param req 关键字入参
     * @return 承运商选项
     */
    @Override
    public List<CarrierOptionVO> options(KeywordReq req) {
        List<Carrier> list = carrierMapper.selectList(Wrappers.<Carrier>lambdaQuery()
                .eq(Carrier::getStatus, EnableStatusEnum.ENABLED.getCode())
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(Carrier::getCarrierName, req.getKeyword())
                        .or().like(Carrier::getShortName, req.getKeyword()))
                .orderByAsc(Carrier::getCarrierName)
                .last(CommonConstant.LIMIT_SQL + CommonConstant.SELECT_LIMIT));
        return BeanUtil.copyToList(list, CarrierOptionVO.class);
    }

    /**
     * 新增或修改承运商；新增或更换联系电话时自动开通承运商账号并赋予承运商角色
     *
     * @param req 承运商保存入参
     * @return 承运商ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String save(CarrierSaveReq req) {
        Long dup = carrierMapper.selectCount(Wrappers.<Carrier>lambdaQuery()
                .and(w -> w.eq(Carrier::getCarrierName, req.getCarrierName())
                        .or().eq(Carrier::getContactPhone, req.getContactPhone()))
                .ne(StrUtil.isNotBlank(req.getId()), Carrier::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("承运商名称或联系电话已被其他承运商使用");
        }
        Carrier carrier = BeanUtil.copyProperties(req, Carrier.class);
        carrier.setStatus(StrUtil.blankToDefault(req.getStatus(), EnableStatusEnum.ENABLED.getCode()));
        boolean needAccount;
        if (StrUtil.isBlank(req.getId())) {
            carrierMapper.insert(carrier);
            needAccount = true;
            log.info("新增承运商[{}]", carrier.getCarrierName());
        } else {
            Carrier old = lookup.carrier(req.getId());
            needAccount = !StrUtil.equals(old.getContactPhone(), req.getContactPhone());
            carrierMapper.updateById(carrier);
            log.info("修改承运商[{}]，是否更换账号：{}", carrier.getCarrierName(), needAccount);
        }
        if (needAccount) {
            AccountProvisionReq account = new AccountProvisionReq();
            account.setUsername(req.getContactPhone());
            account.setNickname(StrUtil.blankToDefault(req.getContactName(), req.getCarrierName()));
            account.setUserType(UserTypeEnum.CARRIER.getCode());
            account.setRefId(carrier.getId());
            account.setRoleCode(RoleCodeEnum.CARRIER.getCode());
            Carrier update = new Carrier();
            update.setId(carrier.getId());
            update.setAccountUserId(accountProvisionService.provision(account));
            carrierMapper.updateById(update);
        }
        return carrier.getId();
    }

    /**
     * 删除承运商（存在运输中车辆时不可删除），同时删除其车辆
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        Carrier carrier = lookup.carrier(req.getId());
        Long transit = vehicleMapper.selectCount(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getCarrierId, carrier.getId())
                .eq(Vehicle::getTransportStatus, VehicleStatusEnum.IN_TRANSIT.getCode()));
        if (transit > 0) {
            log.info("承运商[{}]有{}辆车运输中，不允许删除", carrier.getCarrierName(), transit);
            throw new CustException("该承运商有车辆正在运输，不能删除");
        }
        carrierMapper.deleteById(carrier.getId());
        vehicleMapper.delete(Wrappers.<Vehicle>lambdaQuery().eq(Vehicle::getCarrierId, carrier.getId()));
    }

    /**
     * 查询承运商详情（车辆、运输中货物、统计）
     *
     * @param req 主键入参
     * @return 承运商详情
     */
    @Override
    public CarrierDetailVO detail(IdReq req) {
        dataScopeHelper.checkCarrier(req.getId());
        Carrier carrier = lookup.carrier(req.getId());
        CarrierDetailVO vo = new CarrierDetailVO();
        vo.setCarrier(carrierAssembler.assemble(List.of(carrier)).get(0));
        vo.setVehicles(vehicleAssembler.assemble(vehicleMapper.selectList(Wrappers.<Vehicle>lambdaQuery()
                .eq(Vehicle::getCarrierId, carrier.getId())
                .orderByAsc(Vehicle::getPlateNo))));
        vo.setActiveTrips(tripAssembler.assemble(transportTripMapper.selectList(Wrappers.<TransportTrip>lambdaQuery()
                .eq(TransportTrip::getCarrierId, carrier.getId())
                .eq(TransportTrip::getStatus, TripStatusEnum.IN_TRANSIT.getCode())
                .orderByDesc(TransportTrip::getDepartTime))));
        return vo;
    }

    /**
     * 承运商账号查询本承运商详情
     *
     * @return 承运商详情
     */
    @Override
    public CarrierDetailVO mine() {
        if (!dataScopeHelper.isCarrier()) {
            throw new CustException("当前账号不是承运商账号");
        }
        IdReq req = new IdReq();
        req.setId(dataScopeHelper.resolveCarrierId(null));
        return detail(req);
    }
}
