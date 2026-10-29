package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.constant.BizConstant;
import com.ganghuitong.port.api.dto.biz.BerthQueryReq;
import com.ganghuitong.port.api.dto.biz.PortSaveReq;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.Port;
import com.ganghuitong.port.api.entity.biz.PortElement;
import com.ganghuitong.port.api.enums.BerthStatusEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.BerthService;
import com.ganghuitong.port.api.service.biz.PortService;
import com.ganghuitong.port.api.vo.biz.BerthStatusCountVO;
import com.ganghuitong.port.api.vo.biz.PortDetailVO;
import com.ganghuitong.port.api.vo.biz.PortElementVO;
import com.ganghuitong.port.api.vo.biz.PortVO;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.PortElementMapper;
import com.ganghuitong.port.service.mapper.biz.PortMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 港口服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortServiceImpl implements PortService {

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
     * 泊位服务
     */
    private final BerthService berthService;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 查询当前租户全部港口（含各状态泊位数量）
     *
     * @return 港口列表
     */
    @Override
    public List<PortVO> list() {
        List<Port> ports = portMapper.selectList(Wrappers.<Port>lambdaQuery().orderByAsc(Port::getCreateTime));
        Map<String, List<BerthStatusCountVO>> countMap = berthMapper.selectStatusCount().stream()
                .collect(Collectors.groupingBy(BerthStatusCountVO::getPortId));
        return ports.stream().map(p -> toVo(p, countMap.getOrDefault(p.getId(), List.of()))).toList();
    }

    /**
     * 查询港口详情（含俯视图元素与泊位）
     *
     * @param req 主键入参（港口ID）
     * @return 港口详情
     */
    @Override
    public PortDetailVO detail(IdReq req) {
        Port port = lookup.port(req.getId());
        List<PortElement> elements = portElementMapper.selectList(Wrappers.<PortElement>lambdaQuery()
                .eq(PortElement::getPortId, port.getId())
                .orderByAsc(PortElement::getLayerIndex));
        BerthQueryReq berthReq = new BerthQueryReq();
        berthReq.setPortId(port.getId());
        PortDetailVO vo = new PortDetailVO();
        vo.setBerths(berthService.list(berthReq));
        List<BerthStatusCountVO> counts = vo.getBerths().stream()
                .collect(Collectors.groupingBy(Berth::getStatus, Collectors.counting()))
                .entrySet().stream()
                .map(e -> {
                    BerthStatusCountVO c = new BerthStatusCountVO();
                    c.setStatus(e.getKey());
                    c.setCnt(e.getValue().intValue());
                    return c;
                }).toList();
        vo.setPort(toVo(port, counts));
        vo.setElements(BeanUtil.copyToList(elements, PortElementVO.class));
        return vo;
    }

    /**
     * 新增或修改港口基础信息
     *
     * @param req 港口保存入参
     * @return 港口ID
     */
    @Override
    public String save(PortSaveReq req) {
        Port port = BeanUtil.copyProperties(req, Port.class);
        if (StrUtil.isBlank(req.getId())) {
            port.setCanvasWidth(BizConstant.DEFAULT_CANVAS_WIDTH);
            port.setCanvasHeight(BizConstant.DEFAULT_CANVAS_HEIGHT);
            port.setTheme(BizConstant.DEFAULT_THEME);
            portMapper.insert(port);
            log.info("新增港口[{}]", port.getPortName());
        } else {
            lookup.port(req.getId());
            portMapper.updateById(port);
            log.info("修改港口[{}]", port.getPortName());
        }
        return port.getId();
    }

    /**
     * 删除港口（存在正在作业泊位时不可删除）
     *
     * @param req 主键入参
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(IdReq req) {
        Port port = lookup.port(req.getId());
        Long working = berthMapper.selectCount(Wrappers.<Berth>lambdaQuery()
                .eq(Berth::getPortId, port.getId())
                .eq(Berth::getStatus, BerthStatusEnum.WORKING.getCode()));
        if (working > 0) {
            log.info("港口[{}]存在{}个正在作业泊位，不允许删除", port.getPortName(), working);
            throw new CustException("该港口有泊位正在作业，不能删除");
        }
        portMapper.deleteById(port.getId());
        portElementMapper.physicalDeleteByPort(port.getId());
        berthMapper.delete(Wrappers.<Berth>lambdaQuery().eq(Berth::getPortId, port.getId()));
    }

    /**
     * 将港口实体与泊位状态计数转换为视图对象
     *
     * @param port   港口实体
     * @param counts 该港口各状态泊位数量
     * @return 港口视图对象
     */
    private PortVO toVo(Port port, List<BerthStatusCountVO> counts) {
        PortVO vo = BeanUtil.copyProperties(port, PortVO.class);
        Map<String, Integer> m = counts.stream()
                .collect(Collectors.toMap(BerthStatusCountVO::getStatus, BerthStatusCountVO::getCnt, Integer::sum));
        vo.setBerthIdle(m.getOrDefault(BerthStatusEnum.IDLE.getCode(), 0));
        vo.setBerthWorking(m.getOrDefault(BerthStatusEnum.WORKING.getCode(), 0));
        vo.setBerthMaintenance(m.getOrDefault(BerthStatusEnum.MAINTENANCE.getCode(), 0));
        vo.setBerthTotal(vo.getBerthIdle() + vo.getBerthWorking() + vo.getBerthMaintenance());
        return vo;
    }
}
