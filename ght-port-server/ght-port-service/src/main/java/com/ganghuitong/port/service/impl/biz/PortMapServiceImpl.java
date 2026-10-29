package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.ConfirmResp;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.dto.biz.PortElementReq;
import com.ganghuitong.port.api.dto.biz.PortMapSaveReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.Port;
import com.ganghuitong.port.api.entity.biz.PortElement;
import com.ganghuitong.port.api.enums.MapElementTypeEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.PortMapService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.service.helper.BerthMapSyncHelper;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.PortElementMapper;
import com.ganghuitong.port.service.mapper.biz.PortMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 港口俯视图绘制服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortMapServiceImpl implements PortMapService {

    /**
     * 港口数据访问
     */
    private final PortMapper portMapper;

    /**
     * 俯视图元素数据访问
     */
    private final PortElementMapper portElementMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 俯视图泊位同步辅助
     */
    private final BerthMapSyncHelper berthMapSyncHelper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 保存港口俯视图；移除或移动正在作业的泊位时需用户确认（force=true）后才执行
     *
     * @param req 俯视图保存入参（港口ID、画布、元素列表、是否强制）
     * @return 确认结果：needConfirm=true表示未保存需用户确认
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConfirmResp saveMap(PortMapSaveReq req) {
        Port port = lookup.port(req.getPortId());
        List<PortElementReq> elements = req.getElements() == null ? new ArrayList<>() : req.getElements();
        validate(elements);
        List<Berth> berths = berthMapper.selectList(Wrappers.<Berth>lambdaQuery().eq(Berth::getPortId, port.getId()));
        List<PortElement> oldElements = portElementMapper.selectList(Wrappers.<PortElement>lambdaQuery()
                .eq(PortElement::getPortId, port.getId()));
        List<String> affected = berthMapSyncHelper.findAffectedWorkingBerths(berths, oldElements, elements);
        if (CollUtil.isNotEmpty(affected) && !BooleanUtil.isTrue(req.getForce())) {
            log.info("港口[{}]俯视图保存涉及正在作业泊位，等待用户确认", port.getPortName());
            return ConfirmResp.confirm("泊位【" + StrUtil.join(BizConstant.NAME_SEPARATOR, affected)
                    + "】正在作业，是否继续进行操作？");
        }
        berthMapSyncHelper.sync(port.getId(), berths, elements);
        Port update = new Port();
        update.setId(port.getId());
        update.setCanvasWidth(req.getCanvasWidth() == null ? port.getCanvasWidth() : req.getCanvasWidth());
        update.setCanvasHeight(req.getCanvasHeight() == null ? port.getCanvasHeight() : req.getCanvasHeight());
        update.setTheme(StrUtil.blankToDefault(req.getTheme(), port.getTheme()));
        portMapper.updateById(update);
        portElementMapper.physicalDeleteByPort(port.getId());
        int layer = 0;
        for (PortElementReq e : elements) {
            PortElement entity = BeanUtil.copyProperties(e, PortElement.class);
            entity.setPortId(port.getId());
            entity.setRotation(NumberUtil.nullToZero(e.getRotation()));
            entity.setLayerIndex(e.getLayerIndex() == null ? layer : e.getLayerIndex());
            portElementMapper.insert(entity);
            layer++;
        }
        log.info("港口[{}]俯视图已保存，元素{}个", port.getPortName(), elements.size());
        return ConfirmResp.done(port.getId());
    }

    /**
     * 校验元素类型与泊位名称
     *
     * @param elements 元素列表
     */
    private void validate(List<PortElementReq> elements) {
        for (PortElementReq e : elements) {
            if (!CodeEnumUtil.contains(MapElementTypeEnum.class, e.getElementType())) {
                throw new CustException("不支持的元素类型：" + e.getElementType());
            }
        }
        long berthCount = elements.stream()
                .filter(e -> StrUtil.equals(MapElementTypeEnum.BERTH.getCode(), e.getElementType()))
                .map(PortElementReq::getBerthCode)
                .filter(StrUtil::isNotBlank)
                .count();
        long distinctCount = elements.stream()
                .filter(e -> StrUtil.equals(MapElementTypeEnum.BERTH.getCode(), e.getElementType()))
                .map(PortElementReq::getBerthCode)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .count();
        if (berthCount != distinctCount) {
            throw new CustException("泊位编号不能重复");
        }
    }
}
