package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.ShipSaveReq;
import com.ganghuitong.port.api.vo.biz.ShipVO;

import java.util.List;

/**
 * 船只服务
 *
 * @author yangxueyong
 */
public interface ShipService {

    /**
     * 船只下拉搜索（关键字可匹配船名或船运公司名，parentId为船运公司ID）
     *
     * @param req 关键字入参
     * @return 船只列表（含船运公司名称）
     */
    List<ShipVO> options(KeywordReq req);

    /**
     * 新增或修改船只
     *
     * @param req 船只保存入参
     * @return 船只ID
     */
    String save(ShipSaveReq req);

    /**
     * 删除船只
     *
     * @param req 主键入参
     */
    void delete(IdReq req);

    /**
     * 根据ID列表查询船只（含船运公司名称）
     *
     * @param shipIds 船只ID列表
     * @return 船只列表
     */
    List<ShipVO> listByIds(List<String> shipIds);
}
