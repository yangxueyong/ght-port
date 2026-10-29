package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.CargoTypeSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.entity.biz.CargoType;

import java.util.List;

/**
 * 货物类型服务
 *
 * @author yangxueyong
 */
public interface CargoTypeService {

    /**
     * 按关键字查询货物类型（用于下拉搜索）
     *
     * @param req 关键字入参
     * @return 货物类型列表
     */
    List<CargoType> list(KeywordReq req);

    /**
     * 新增或修改货物类型
     *
     * @param req 货物类型保存入参
     * @return 货物类型ID
     */
    String save(CargoTypeSaveReq req);

    /**
     * 删除货物类型
     *
     * @param req 主键入参
     */
    void delete(IdReq req);
}
