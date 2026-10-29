package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.constant.GatePassConstant;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.dto.biz.GatePassNotifyReq;
import com.ganghuitong.port.api.dto.biz.GatePassPageReq;
import com.ganghuitong.port.api.dto.biz.TokenReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.GatePass;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.enums.GatePassStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.GatePassService;
import com.ganghuitong.port.api.service.biz.NotifyService;
import com.ganghuitong.port.api.util.NoGenUtil;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import com.ganghuitong.port.service.helper.DataScopeHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.helper.GatePassAssembler;
import com.ganghuitong.port.service.mapper.biz.GatePassMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出门条服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GatePassServiceImpl implements GatePassService {

    /**
     * 出门条数据访问
     */
    private final GatePassMapper gatePassMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 通知服务
     */
    private final NotifyService notifyService;

    /**
     * 出门条视图组装器
     */
    private final GatePassAssembler gatePassAssembler;

    /**
     * 数据范围辅助
     */
    private final DataScopeHelper dataScopeHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 为运输单开具出门条并通知司机
     *
     * @param trip 已生成的运输单
     * @return 出门条信息（含司机访问链接）
     */
    @Override
    public GatePassVO issue(TransportTrip trip) {
        Carrier carrier = lookup.carrier(trip.getCarrierId());
        Vehicle vehicle = lookup.vehicle(trip.getVehicleId());
        Berth berth = lookup.berth(trip.getBerthId());
        GatePass pass = new GatePass();
        pass.setPassNo(NoGenUtil.genNo(GatePassConstant.PASS_NO_PREFIX, GatePassConstant.PASS_NO_DATE_PATTERN,
                GatePassConstant.PASS_NO_RANDOM_LENGTH));
        pass.setAccessToken(NoGenUtil.genToken());
        pass.setTripId(trip.getId());
        pass.setCarrierId(carrier.getId());
        pass.setCarrierName(carrier.getCarrierName());
        pass.setVehicleId(vehicle.getId());
        pass.setPlateNo(vehicle.getPlateNo());
        pass.setVehicleType(vehicle.getVehicleType());
        pass.setDriverName(vehicle.getDriverName());
        pass.setDriverPhone(vehicle.getDriverPhone());
        pass.setPortName(lookup.port(trip.getPortId()).getPortName());
        pass.setBerthName(berth.getBerthName());
        pass.setCargoName(trip.getCargoName());
        pass.setCargoTypeName(trip.getCargoTypeName());
        pass.setLoadTonnage(trip.getLoadTonnage());
        pass.setLoadVolume(trip.getLoadVolume());
        pass.setLoadPieces(trip.getLoadPieces());
        pass.setIssueTime(LocalDateTime.now());
        pass.setStatus(GatePassStatusEnum.UNUSED.getCode());
        gatePassMapper.insert(pass);
        GatePassVO vo = gatePassAssembler.toVo(pass);
        GatePassNotifyReq notify = new GatePassNotifyReq();
        notify.setPhone(vehicle.getDriverPhone());
        notify.setPlateNo(vehicle.getPlateNo());
        notify.setPassNo(pass.getPassNo());
        notify.setLink(vo.getLink());
        notifyService.sendGatePassLink(notify);
        log.info("车辆[{}]出门条[{}]已开具", vehicle.getPlateNo(), pass.getPassNo());
        return vo;
    }

    /**
     * 分页查询出门条（承运商账号仅能查看本承运商）
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    @Override
    public PageResp<GatePassVO> page(GatePassPageReq req) {
        String carrierId = dataScopeHelper.resolveCarrierId(req.getCarrierId());
        LambdaQueryWrapper<GatePass> wrapper = baseQuery(req)
                .eq(StrUtil.isNotBlank(carrierId), GatePass::getCarrierId, carrierId);
        Page<GatePass> page = gatePassMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()), wrapper);
        return PageResp.of(page, gatePassAssembler.toVoList(page.getRecords()));
    }

    /**
     * 查询当前司机账号驾驶车辆的出门条
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    @Override
    public PageResp<GatePassVO> myPasses(GatePassPageReq req) {
        String userId = UserContext.require().getUserId();
        List<String> vehicleIds = vehicleMapper.selectList(Wrappers.<Vehicle>lambdaQuery()
                        .select(Vehicle::getId)
                        .eq(Vehicle::getDriverUserId, userId))
                .stream().map(Vehicle::getId).toList();
        if (CollUtil.isEmpty(vehicleIds)) {
            log.info("司机账号[{}]未关联车辆", UserContext.getUsername());
            return PageResp.of(new Page<GatePass>(req.getPageNum(), req.getPageSize()), List.of());
        }
        Page<GatePass> page = gatePassMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                baseQuery(req).in(GatePass::getVehicleId, vehicleIds));
        return PageResp.of(page, gatePassAssembler.toVoList(page.getRecords()));
    }

    /**
     * 司机通过公开链接查看出门条（免登录）
     *
     * @param req 令牌入参
     * @return 出门条信息
     */
    @Override
    public GatePassVO publicView(TokenReq req) {
        GatePass pass = gatePassMapper.selectByTokenIgnoreTenant(req.getToken());
        if (pass == null) {
            log.info("出门条令牌[{}]不存在", req.getToken());
            throw new CustException("出门条不存在或链接已失效");
        }
        return gatePassAssembler.toVo(pass);
    }

    /**
     * 门卫扫码后预览出门条（仅能查看本港口的出门条）
     *
     * @param req 令牌入参
     * @return 出门条信息
     */
    @Override
    public GatePassVO preview(TokenReq req) {
        return gatePassAssembler.toVo(getByToken(req.getToken()));
    }

    /**
     * 门卫核销出门条，核销后二维码不能再次使用
     *
     * @param req 令牌入参
     * @return 核销后的出门条信息
     */
    @Override
    public GatePassVO verify(TokenReq req) {
        GatePass pass = getByToken(req.getToken());
        if (StrUtil.equals(GatePassStatusEnum.VERIFIED.getCode(), pass.getStatus())) {
            log.info("出门条[{}]重复核销被拒绝", pass.getPassNo());
            throw new CustException("该出门条已于 " + LocalDateTimeUtil.format(pass.getVerifyTime(), DatePattern.NORM_DATETIME_PATTERN)
                    + " 核销，二维码不能重复使用");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = gatePassMapper.update(null, Wrappers.<GatePass>lambdaUpdate()
                .set(GatePass::getStatus, GatePassStatusEnum.VERIFIED.getCode())
                .set(GatePass::getVerifyTime, now)
                .set(GatePass::getVerifyBy, UserContext.getUsername())
                .eq(GatePass::getId, pass.getId())
                .eq(GatePass::getStatus, GatePassStatusEnum.UNUSED.getCode()));
        if (rows == 0) {
            log.info("出门条[{}]并发核销，本次核销失败", pass.getPassNo());
            throw new CustException("该出门条已核销，二维码不能重复使用");
        }
        log.info("门卫[{}]核销出门条[{}]，车辆[{}]放行", UserContext.getUsername(), pass.getPassNo(), pass.getPlateNo());
        pass.setStatus(GatePassStatusEnum.VERIFIED.getCode());
        pass.setVerifyTime(now);
        pass.setVerifyBy(UserContext.getUsername());
        return gatePassAssembler.toVo(pass);
    }

    /**
     * 按令牌查询本租户出门条，不存在时抛出异常
     *
     * @param token 访问令牌
     * @return 出门条实体
     */
    private GatePass getByToken(String token) {
        GatePass pass = gatePassMapper.selectOne(Wrappers.<GatePass>lambdaQuery().eq(GatePass::getAccessToken, token));
        if (pass == null) {
            log.info("出门条令牌[{}]在当前港口不存在", token);
            throw new CustException("出门条不存在或不属于本港口");
        }
        return pass;
    }

    /**
     * 构建出门条通用查询条件
     *
     * @param req 分页查询入参
     * @return 查询条件
     */
    private LambdaQueryWrapper<GatePass> baseQuery(GatePassPageReq req) {
        return Wrappers.<GatePass>lambdaQuery()
                .eq(StrUtil.isNotBlank(req.getStatus()), GatePass::getStatus, req.getStatus())
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(GatePass::getPlateNo, req.getKeyword())
                        .or().like(GatePass::getPassNo, req.getKeyword())
                        .or().like(GatePass::getDriverName, req.getKeyword()))
                .orderByDesc(GatePass::getIssueTime);
    }
}
