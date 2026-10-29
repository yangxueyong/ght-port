package com.ganghuitong.port.service.impl.biz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.constant.CommonConstant;
import com.ganghuitong.port.api.dto.biz.CargoTypeSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.entity.biz.CargoType;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.biz.CargoTypeService;
import com.ganghuitong.port.service.mapper.biz.CargoTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 货物类型服务实现
 *
 * @author yangxueyong
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CargoTypeServiceImpl implements CargoTypeService {

    /**
     * 货物类型数据访问
     */
    private final CargoTypeMapper cargoTypeMapper;

    /**
     * 按关键字查询货物类型（用于下拉搜索）
     *
     * @param req 关键字入参
     * @return 货物类型列表
     */
    @Override
    public List<CargoType> list(KeywordReq req) {
        return cargoTypeMapper.selectList(Wrappers.<CargoType>lambdaQuery()
                .and(StrUtil.isNotBlank(req.getKeyword()), w -> w.like(CargoType::getTypeName, req.getKeyword())
                        .or().like(CargoType::getTypeCode, req.getKeyword()))
                .orderByAsc(CargoType::getSort)
                .last(CommonConstant.LIMIT_SQL + CommonConstant.SELECT_LIMIT));
    }

    /**
     * 新增或修改货物类型
     *
     * @param req 货物类型保存入参
     * @return 货物类型ID
     */
    @Override
    public String save(CargoTypeSaveReq req) {
        Long dup = cargoTypeMapper.selectCount(Wrappers.<CargoType>lambdaQuery()
                .eq(CargoType::getTypeName, req.getTypeName())
                .ne(StrUtil.isNotBlank(req.getId()), CargoType::getId, req.getId()));
        if (dup > 0) {
            throw new CustException("货物类型[" + req.getTypeName() + "]已存在");
        }
        CargoType entity = BeanUtil.copyProperties(req, CargoType.class);
        if (StrUtil.isBlank(req.getId())) {
            cargoTypeMapper.insert(entity);
            log.info("新增货物类型[{}]", entity.getTypeName());
        } else {
            cargoTypeMapper.updateById(entity);
            log.info("修改货物类型[{}]", entity.getTypeName());
        }
        return entity.getId();
    }

    /**
     * 删除货物类型
     *
     * @param req 主键入参
     */
    @Override
    public void delete(IdReq req) {
        cargoTypeMapper.deleteById(req.getId());
    }
}
