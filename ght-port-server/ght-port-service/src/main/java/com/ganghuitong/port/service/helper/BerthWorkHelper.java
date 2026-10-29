package com.ganghuitong.port.service.helper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.BerthCargoShip;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.enums.CargoStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.ShipLoadStatusEnum;
import com.ganghuitong.port.api.enums.ShipWorkStatusEnum;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthCargoShipMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 泊位作业状态辅助组件：结束作业、变更状态
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BerthWorkHelper {

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 到货船只关联数据访问
     */
    private final BerthCargoShipMapper berthCargoShipMapper;

    /**
     * 结束泊位当前作业：作业中的货物置为完成，卸货船只置为完成，待装载车辆分派置为取消；
     * 装货中的船只按 completeLoads 置为已装船（完成运输）或取消（调用方需开启事务）。运输中的车辆不受影响。
     *
     * @param berthId       泊位ID
     * @param completeLoads true-装货中的船只置为已装船；false-取消装船分派（如转维修）
     */
    public void closeWork(String berthId, boolean completeLoads) {
        List<BerthCargo> activeCargos = berthCargoMapper.selectList(Wrappers.<BerthCargo>lambdaQuery()
                .eq(BerthCargo::getBerthId, berthId)
                .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()));
        List<String> activeCargoIds = activeCargos.stream().map(BerthCargo::getId).toList();
        int shipRows = activeCargoIds.isEmpty() ? 0 : berthCargoShipMapper.update(null, Wrappers.<BerthCargoShip>lambdaUpdate()
                .set(BerthCargoShip::getStatus, ShipWorkStatusEnum.COMPLETED.getCode())
                .set(BerthCargoShip::getFinishTime, LocalDateTime.now())
                .in(BerthCargoShip::getCargoId, activeCargoIds)
                .eq(BerthCargoShip::getStatus, ShipWorkStatusEnum.WORKING.getCode()));
        int loadedRows = completeLoads ? completeLoads(berthId, activeCargos) : 0;
        int cargoRows = berthCargoMapper.update(null, Wrappers.<BerthCargo>lambdaUpdate()
                .set(BerthCargo::getStatus, CargoStatusEnum.COMPLETED.getCode())
                .set(BerthCargo::getFinishTime, LocalDateTime.now())
                .eq(BerthCargo::getBerthId, berthId)
                .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()));
        int dispatchRows = dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                .set(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode())
                .eq(Dispatch::getBerthId, berthId)
                .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
        int cancelLoadRows = shipLoadMapper.update(null, Wrappers.<ShipLoad>lambdaUpdate()
                .set(ShipLoad::getStatus, ShipLoadStatusEnum.CANCELLED.getCode())
                .eq(ShipLoad::getBerthId, berthId)
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
        log.info("泊位[{}]结束作业：完成货物{}批，完成卸货船只{}艘，完成装货船只{}艘，取消待装载分派{}条，取消待装船分派{}条",
                berthId, cargoRows, shipRows, loadedRows, dispatchRows, cancelLoadRows);
    }

    /**
     * 统计泊位装货中的船只数（待装船的分派）
     *
     * @param berthId 泊位ID
     * @return 装货中船只数
     */
    public long countLoadingShips(String berthId) {
        return shipLoadMapper.selectCount(Wrappers.<ShipLoad>lambdaQuery()
                .eq(ShipLoad::getBerthId, berthId)
                .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
    }

    /**
     * 统计泊位卸货中的船只数
     *
     * @param berthId 泊位ID
     * @return 卸货中船只数
     */
    public long countUnloadingShips(String berthId) {
        List<String> cargoIds = berthCargoMapper.selectList(Wrappers.<BerthCargo>lambdaQuery()
                        .select(BerthCargo::getId)
                        .eq(BerthCargo::getBerthId, berthId)
                        .eq(BerthCargo::getStatus, CargoStatusEnum.IN_PROGRESS.getCode()))
                .stream().map(BerthCargo::getId).toList();
        return cargoIds.isEmpty() ? 0 : berthCargoShipMapper.selectCount(Wrappers.<BerthCargoShip>lambdaQuery()
                .in(BerthCargoShip::getCargoId, cargoIds)
                .eq(BerthCargoShip::getStatus, ShipWorkStatusEnum.WORKING.getCode()));
    }

    /**
     * 装货中的船只置为已装船：每批货物剩余未装的数量平均分给该货物下待装船的船只，并计入货物已装数量
     *
     * @param berthId      泊位ID
     * @param activeCargos 作业中的货物
     * @return 完成装船的分派数量
     */
    private int completeLoads(String berthId, List<BerthCargo> activeCargos) {
        Map<String, List<ShipLoad>> loadsByCargo = shipLoadMapper.selectList(Wrappers.<ShipLoad>lambdaQuery()
                        .eq(ShipLoad::getBerthId, berthId)
                        .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode())
                        .orderByAsc(ShipLoad::getCreateTime))
                .stream().collect(Collectors.groupingBy(ShipLoad::getCargoId));
        int rows = 0;
        for (BerthCargo cargo : activeCargos) {
            List<ShipLoad> loads = loadsByCargo.getOrDefault(cargo.getId(), List.of());
            if (loads.isEmpty()) {
                continue;
            }
            BigDecimal[] tons = split(remain(cargo.getTonnage(), cargo.getLoadedTonnage()), loads.size());
            BigDecimal[] vols = split(remain(cargo.getVolume(), cargo.getLoadedVolume()), loads.size());
            int pieces = Math.max(0, ObjectUtil.defaultIfNull(cargo.getPieces(), 0) - ObjectUtil.defaultIfNull(cargo.getLoadedPieces(), 0));
            for (int i = 0; i < loads.size(); i++) {
                int pcs = pieces / loads.size() + (i < pieces % loads.size() ? 1 : 0);
                rows += shipLoadMapper.update(null, Wrappers.<ShipLoad>lambdaUpdate()
                        .set(ShipLoad::getStatus, ShipLoadStatusEnum.LOADED.getCode())
                        .set(ShipLoad::getLoadTonnage, tons[i])
                        .set(ShipLoad::getLoadVolume, vols[i])
                        .set(ShipLoad::getLoadPieces, pcs)
                        .set(ShipLoad::getLoadTime, LocalDateTime.now())
                        .eq(ShipLoad::getId, loads.get(i).getId())
                        .eq(ShipLoad::getStatus, ShipLoadStatusEnum.WAITING.getCode()));
            }
            berthCargoMapper.increaseLoaded(cargo.getId(), sum(tons), sum(vols), pieces);
            log.info("泊位[{}]货物[{}]完成作业，{}艘装货船只置为已装船，剩余{}吨平均分配", berthId, cargo.getCargoName(), loads.size(), sum(tons));
        }
        return rows;
    }

    /**
     * 计算剩余数量（不小于0）
     */
    private BigDecimal remain(BigDecimal total, BigDecimal done) {
        return NumberUtil.nullToZero(total).subtract(NumberUtil.nullToZero(done)).max(BigDecimal.ZERO);
    }

    /**
     * 平均拆分数量（保留2位小数，尾差计入最后一份）
     */
    private BigDecimal[] split(BigDecimal total, int n) {
        BigDecimal[] parts = new BigDecimal[n];
        BigDecimal each = total.divide(BigDecimal.valueOf(n), 2, RoundingMode.DOWN);
        for (int i = 0; i < n; i++) {
            parts[i] = each;
        }
        parts[n - 1] = total.subtract(each.multiply(BigDecimal.valueOf(n - 1)));
        return parts;
    }

    /**
     * 求和
     */
    private BigDecimal sum(BigDecimal[] parts) {
        BigDecimal s = BigDecimal.ZERO;
        for (BigDecimal p : parts) {
            s = s.add(p);
        }
        return s;
    }

    /**
     * 更新泊位状态
     *
     * @param berthId 泊位ID
     * @param status  目标状态
     */
    public void updateStatus(String berthId, String status) {
        Berth update = new Berth();
        update.setId(berthId);
        update.setStatus(status);
        berthMapper.updateById(update);
    }
}
