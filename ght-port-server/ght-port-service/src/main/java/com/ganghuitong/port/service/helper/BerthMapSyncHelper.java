package com.ganghuitong.port.service.helper;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.dto.biz.PortElementReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.PortElement;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.enums.DispatchStatusEnum;
import com.ganghuitong.port.api.enums.MapElementTypeEnum;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 俯视图与泊位数据同步辅助组件：识别受影响的作业泊位，并按俯视图新增/更新/移除泊位
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BerthMapSyncHelper {

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 找出本次保存会被移除或移动位置的“正在作业”泊位名称
     *
     * @param existingBerths 港口现有泊位
     * @param oldElements    港口现有俯视图元素
     * @param newElements    本次提交的俯视图元素
     * @return 受影响的正在作业泊位名称（附带操作说明）
     */
    public List<String> findAffectedWorkingBerths(List<Berth> existingBerths, List<PortElement> oldElements,
                                                  List<PortElementReq> newElements) {
        Map<String, PortElement> oldByBerth = oldElements.stream()
                .filter(e -> StrUtil.isNotBlank(e.getBerthId()))
                .collect(Collectors.toMap(PortElement::getBerthId, e -> e, (a, b) -> a));
        Map<String, PortElementReq> newByBerth = berthElements(newElements).stream()
                .filter(e -> StrUtil.isNotBlank(e.getBerthId()))
                .collect(Collectors.toMap(PortElementReq::getBerthId, e -> e, (a, b) -> a));
        List<String> affected = new ArrayList<>();
        for (Berth berth : existingBerths) {
            if (!StrUtil.equals(BerthStatusEnum.WORKING.getCode(), berth.getStatus())) {
                continue;
            }
            PortElementReq incoming = newByBerth.get(berth.getId());
            if (incoming == null) {
                log.info("正在作业的泊位[{}]将被移除", berth.getBerthName());
                affected.add(berth.getBerthName() + "（移除）");
            } else if (geometryChanged(oldByBerth.get(berth.getId()), incoming)) {
                log.info("正在作业的泊位[{}]位置或大小将被调整", berth.getBerthName());
                affected.add(berth.getBerthName() + "（调整位置）");
            }
        }
        return affected;
    }

    /**
     * 按俯视图同步泊位：已有泊位更新名称编号，新画泊位新增，未出现的泊位移除；
     * 执行后会把新泊位ID回填到元素入参中，调用方需开启事务
     *
     * @param portId         港口ID
     * @param existingBerths 港口现有泊位
     * @param newElements    本次提交的俯视图元素
     */
    public void sync(String portId, List<Berth> existingBerths, List<PortElementReq> newElements) {
        Map<String, Berth> existMap = existingBerths.stream().collect(Collectors.toMap(Berth::getId, b -> b));
        List<PortElementReq> berthElems = berthElements(newElements);
        int seq = existingBerths.size();
        for (PortElementReq elem : berthElems) {
            Berth old = StrUtil.isBlank(elem.getBerthId()) ? null : existMap.get(elem.getBerthId());
            if (old != null) {
                Berth update = new Berth();
                update.setId(old.getId());
                update.setBerthName(StrUtil.blankToDefault(elem.getBerthName(), old.getBerthName()));
                update.setBerthCode(StrUtil.blankToDefault(elem.getBerthCode(), old.getBerthCode()));
                berthMapper.updateById(update);
                continue;
            }
            seq++;
            Berth berth = new Berth();
            berth.setPortId(portId);
            berth.setBerthCode(StrUtil.blankToDefault(elem.getBerthCode(), BizConstant.BERTH_CODE_PREFIX + seq));
            berth.setBerthName(StrUtil.blankToDefault(elem.getBerthName(), berth.getBerthCode() + "号泊位"));
            berth.setStatus(BerthStatusEnum.IDLE.getCode());
            berthMapper.insert(berth);
            elem.setBerthId(berth.getId());
            log.info("俯视图新增泊位[{}]", berth.getBerthName());
        }
        Set<String> keepIds = berthElems.stream().map(PortElementReq::getBerthId).collect(Collectors.toSet());
        for (Berth berth : existingBerths) {
            if (keepIds.contains(berth.getId())) {
                continue;
            }
            berthMapper.deleteById(berth.getId());
            dispatchMapper.update(null, Wrappers.<Dispatch>lambdaUpdate()
                    .set(Dispatch::getStatus, DispatchStatusEnum.CANCELLED.getCode())
                    .eq(Dispatch::getBerthId, berth.getId())
                    .eq(Dispatch::getStatus, DispatchStatusEnum.WAITING.getCode()));
            log.info("俯视图移除泊位[{}]", berth.getBerthName());
        }
    }

    /**
     * 过滤出泊位类型的元素
     *
     * @param elements 全部元素
     * @return 泊位元素
     */
    private List<PortElementReq> berthElements(List<PortElementReq> elements) {
        return elements.stream()
                .filter(e -> StrUtil.equals(MapElementTypeEnum.BERTH.getCode(), e.getElementType()))
                .toList();
    }

    /**
     * 判断泊位元素的位置、大小、角度是否发生变化
     *
     * @param old      原元素，可能为空
     * @param incoming 新元素
     * @return true-发生变化
     */
    private boolean geometryChanged(PortElement old, PortElementReq incoming) {
        if (old == null) {
            return false;
        }
        return !NumberUtil.equals(old.getPosX(), incoming.getPosX())
                || !NumberUtil.equals(old.getPosY(), incoming.getPosY())
                || !NumberUtil.equals(old.getWidth(), incoming.getWidth())
                || !NumberUtil.equals(old.getHeight(), incoming.getHeight())
                || !NumberUtil.equals(NumberUtil.nullToZero(old.getRotation()), NumberUtil.nullToZero(incoming.getRotation()));
    }
}
