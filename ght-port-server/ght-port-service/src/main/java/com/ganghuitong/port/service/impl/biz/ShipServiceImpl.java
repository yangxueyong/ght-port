package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.ShipSaveReq;
import com.ganghuitong.port.api.entity.biz.Ship;
import com.ganghuitong.port.api.enums.ShipTypeEnum;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.ShipService;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import com.ganghuitong.port.service.helper.EntityLookupHelper;
import com.ganghuitong.port.service.mapper.biz.ShipMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 船只服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipServiceImpl implements ShipService {

    /**
     * 船只数据访问
     */
    private final ShipMapper shipMapper;

    /**
     * 实体查询辅助
     */
    private final EntityLookupHelper lookup;

    /**
     * 船只下拉搜索（关键字可匹配船名或船运公司名，parentId为船运公司ID）
     *
     * @param req 关键字入参
     * @return 船只列表（含船运公司名称）
     */
    @Override
    public List<ShipVO> options(KeywordReq req) {
        return fillDesc(shipMapper.selectOptions(req, CommonConstant.SELECT_LIMIT));
    }

    /**
     * 新增或修改船只
     *
     * @param req 船只保存入参
     * @return 船只ID
     */
    @Override
    public String save(ShipSaveReq req) {
        lookup.shippingCompany(req.getCompanyId());
        if (!CodeEnumUtil.contains(ShipTypeEnum.class, req.getShipType())) {
            throw new CustException("船舶类型不正确");
        }
        Ship ship = BeanUtil.copyProperties(req, Ship.class);
        if (StrUtil.isBlank(req.getId())) {
            shipMapper.insert(ship);
            log.info("新增船只[{}]", ship.getShipName());
        } else {
            shipMapper.updateById(ship);
            log.info("修改船只[{}]", ship.getShipName());
        }
        return ship.getId();
    }

    /**
     * 删除船只
     *
     * @param req 主键入参
     */
    @Override
    public void delete(IdReq req) {
        shipMapper.deleteById(req.getId());
    }

    /**
     * 根据ID列表查询船只（含船运公司名称）
     *
     * @param shipIds 船只ID列表
     * @return 船只列表
     */
    @Override
    public List<ShipVO> listByIds(List<String> shipIds) {
        if (CollUtil.isEmpty(shipIds)) {
            return Collections.emptyList();
        }
        return fillDesc(shipMapper.selectVoByIds(shipIds));
    }

    /**
     * 补充船舶类型描述
     *
     * @param ships 船只列表
     * @return 原列表
     */
    private List<ShipVO> fillDesc(List<ShipVO> ships) {
        ships.forEach(s -> s.setShipTypeDesc(CodeEnumUtil.desc(ShipTypeEnum.class, s.getShipType())));
        return ships;
    }
}
