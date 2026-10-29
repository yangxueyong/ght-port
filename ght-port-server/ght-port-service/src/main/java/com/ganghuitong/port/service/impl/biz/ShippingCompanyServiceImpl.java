package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.ShipCompleteReq;
import com.ganghuitong.port.api.dto.biz.ShipLoadConfirmReq;
import com.ganghuitong.port.api.dto.biz.ShippingCompanySaveReq;
import com.ganghuitong.port.api.entity.biz.BerthCargoShip;
import com.ganghuitong.port.api.entity.biz.Ship;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.entity.biz.ShippingCompany;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.enums.ShipTypeEnum;
import com.ganghuitong.port.api.enums.ShipWorkStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.ShipLoadService;
import com.ganghuitong.port.api.service.biz.ShippingCompanyService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyDetailVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyStatVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyVO;
import com.ganghuitong.port.api.vo.biz.WorkingShipVO;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoShipMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import com.ganghuitong.port.service.mapper.biz.ShipMapper;
import com.ganghuitong.port.service.mapper.biz.ShippingCompanyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 船运公司（船只管理）服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingCompanyServiceImpl implements ShippingCompanyService {

    /**
     * 船运公司数据访问
     */
    private final ShippingCompanyMapper shippingCompanyMapper;

    /**
     * 船只数据访问
     */
    private final ShipMapper shipMapper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 到货船只关联数据访问
     */
    private final BerthCargoShipMapper berthCargoShipMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 装船分派服务
     */
    private final ShipLoadService shipLoadService;

    /**
     * 分页查询船运公司（含船只）
     *
     * @param req 分页查询入参
     * @return 船运公司分页数据
     */
    @Override
    public PageResp<ShippingCompanyVO> page(PageReq req) {
        Page<ShippingCompany> page = shippingCompanyMapper.selectPage(new Page<>(req.getPageNum(), req.getPageSize()),
                Wrappers.<ShippingCompany>lambdaQuery()
                        .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(ShippingCompany::getCompanyName, req.getKeyword())
                                .or().like(ShippingCompany::getContactName, req.getKeyword())
                                .or().like(ShippingCompany::getContactPhone, req.getKeyword()))
                        .orderByDesc(ShippingCompany::getCreateTime));
        List<ShippingCompanyVO> records = BeanUtil.copyToList(page.getRecords(), ShippingCompanyVO.class);
        fillShipsAndStats(records);
        return PageResp.of(page, records);
    }

    /**
     * 查询船运公司详情：装卸统计、船只及其作业状态
     *
     * @param req 主键入参（船运公司ID）
     * @return 船运公司详情
     */
    @Override
    public ShippingCompanyDetailVO detail(IdReq req) {
        ShippingCompanyVO vo = BeanUtil.copyProperties(lookup.shippingCompany(req.getId()), ShippingCompanyVO.class);
        fillShipsAndStats(List.of(vo));
        ShippingCompanyDetailVO detail = new ShippingCompanyDetailVO();
        detail.setCompany(vo);
        detail.setShips(vo.getShips());
        return detail;
    }

    /**
     * 船只完成作业：卸货船只结束卸货；装货船只登记装船量并完成装船
     *
     * @param req 完成入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeShip(ShipCompleteReq req) {
        Ship ship = shipMapper.selectById(req.getShipId());
        if (ship == null) {
            throw new CustException("船只不存在");
        }
        List<ShipLoad> waitingLoads = shipLoadMapper.selectList(Wrappers.<ShipLoad>lambdaQuery()
                .eq(ShipLoad::getShipId, ship.getId())
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode())
                .orderByDesc(ShipLoad::getCreateTime));
        if (!waitingLoads.isEmpty()) {
            if (NumberUtil.nullToZero(req.getLoadTonnage()).signum() <= 0) {
                throw new CustException("船只【" + ship.getShipName() + "】正在装货，请填写装船吨数");
            }
            ShipLoadConfirmReq confirm = new ShipLoadConfirmReq();
            confirm.setId(waitingLoads.get(0).getId());
            confirm.setLoadTonnage(req.getLoadTonnage());
            confirm.setLoadVolume(req.getLoadVolume());
            confirm.setLoadPieces(req.getLoadPieces());
            shipLoadService.confirm(confirm);
            for (ShipLoad other : waitingLoads.subList(1, waitingLoads.size())) {
                IdReq idReq = new IdReq();
                idReq.setId(other.getId());
                shipLoadService.cancel(idReq);
            }
        }
        int unloadRows = berthCargoShipMapper.update(null, Wrappers.<BerthCargoShip>lambdaUpdate()
                .set(BerthCargoShip::getStatus, ShipWorkStatusEnum.COMPLETED.getCode())
                .set(BerthCargoShip::getFinishTime, LocalDateTime.now())
                .eq(BerthCargoShip::getShipId, ship.getId())
                .eq(BerthCargoShip::getStatus, ShipWorkStatusEnum.WORKING.getCode()));
        if (waitingLoads.isEmpty() && unloadRows == 0) {
            log.info("船只[{}]当前没有作业", ship.getShipName());
            throw new CustException("船只【" + ship.getShipName() + "】当前没有正在进行的作业");
        }
        log.info("船只[{}]完成作业：卸货关联{}条，装船{}条", ship.getShipName(), unloadRows, waitingLoads.isEmpty() ? 0 : 1);
    }

    /**
     * 新增或修改船运公司
     *
     * @param req 船运公司保存入参
     * @return 船运公司ID
     */
    @Override
    public String save(ShippingCompanySaveReq req) {
        Long dup = shippingCompanyMapper.selectCount(Wrappers.<ShippingCompany>lambdaQuery()
                .eq(ShippingCompany::getCompanyName, req.getCompanyName())
                .ne(StrUtil.isNotBlank(req.getId()), ShippingCompany::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("船运公司【" + req.getCompanyName() + "】已存在");
        }
        ShippingCompany shippingCompany = BeanUtil.copyProperties(req, ShippingCompany.class);
        if (StrUtil.isBlank(req.getId())) {
            shippingCompanyMapper.insert(shippingCompany);
            log.info("新增船运公司[{}]", shippingCompany.getCompanyName());
        } else {
            shippingCompanyMapper.updateById(shippingCompany);
            log.info("修改船运公司[{}]", shippingCompany.getCompanyName());
        }
        return shippingCompany.getId();
    }

    /**
     * 删除船运公司及其船只
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        shippingCompanyMapper.deleteById(req.getId());
        shipMapper.delete(Wrappers.<Ship>lambdaQuery().eq(Ship::getCompanyId, req.getId()));
    }

    /**
     * 补充船运公司的船只、船只作业状态及装卸统计
     *
     * @param records 船运公司列表
     */
    private void fillShipsAndStats(List<ShippingCompanyVO> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<String> companyIds = records.stream().map(ShippingCompanyVO::getId).toList();
        List<Ship> ships = shipMapper.selectList(Wrappers.<Ship>lambdaQuery()
                .in(Ship::getCompanyId, companyIds)
                .orderByAsc(Ship::getShipName));
        Map<String, List<Ship>> byShippingCompany = ships.stream().collect(Collectors.groupingBy(Ship::getCompanyId));
        Map<String, ShippingCompanyStatVO> unloadByShippingCompany = toStatMap(shipMapper.selectUnloadStat(companyIds, false));
        Map<String, ShippingCompanyStatVO> loadByShippingCompany = toStatMap(shipMapper.selectLoadStat(companyIds, false));
        Map<String, ShippingCompanyStatVO> unloadByShip = toStatMap(shipMapper.selectUnloadStat(companyIds, true));
        Map<String, ShippingCompanyStatVO> loadByShip = toStatMap(shipMapper.selectLoadStat(companyIds, true));
        Map<String, WorkingShipVO> working = new HashMap<>();
        shipMapper.selectUnloadingShips(companyIds).forEach(w -> working.putIfAbsent(w.getShipId(), w));
        shipMapper.selectLoadingShips(companyIds).forEach(w -> working.putIfAbsent(w.getShipId(), w));
        for (ShippingCompanyVO vo : records) {
            List<ShipVO> shipVos = BeanUtil.copyToList(byShippingCompany.getOrDefault(vo.getId(), Collections.emptyList()), ShipVO.class);
            shipVos.forEach(s -> {
                s.setCompanyName(vo.getCompanyName());
                s.setShipTypeDesc(CodeEnumUtil.desc(ShipTypeEnum.class, s.getShipType()));
                s.setWorking(working.get(s.getId()));
                s.setUnloadTonnage(tonnage(unloadByShip.get(s.getId())));
                s.setUnloadVolume(volume(unloadByShip.get(s.getId())));
                s.setLoadTonnage(tonnage(loadByShip.get(s.getId())));
                s.setLoadVolume(volume(loadByShip.get(s.getId())));
            });
            vo.setShips(shipVos);
            vo.setShipCount(shipVos.size());
            vo.setWorkingShipCount((int) shipVos.stream().filter(s -> s.getWorking() != null).count());
            vo.setUnloadTonnage(tonnage(unloadByShippingCompany.get(vo.getId())));
            vo.setUnloadVolume(volume(unloadByShippingCompany.get(vo.getId())));
            vo.setLoadTonnage(tonnage(loadByShippingCompany.get(vo.getId())));
            vo.setLoadVolume(volume(loadByShippingCompany.get(vo.getId())));
        }
    }

    /**
     * 统计列表转换为 业务ID->统计 的映射
     *
     * @param stats 统计列表
     * @return 映射
     */
    private Map<String, ShippingCompanyStatVO> toStatMap(List<ShippingCompanyStatVO> stats) {
        return stats.stream().collect(Collectors.toMap(ShippingCompanyStatVO::getBizId, Function.identity(), (a, b) -> a));
    }

    /**
     * 取统计吨数，无统计时为0
     *
     * @param stat 统计
     * @return 吨数
     */
    private BigDecimal tonnage(ShippingCompanyStatVO stat) {
        return stat == null ? BigDecimal.ZERO : NumberUtil.nullToZero(stat.getTonnage());
    }

    /**
     * 取统计方数，无统计时为0
     *
     * @param stat 统计
     * @return 方数
     */
    private BigDecimal volume(ShippingCompanyStatVO stat) {
        return stat == null ? BigDecimal.ZERO : NumberUtil.nullToZero(stat.getVolume());
    }
}
