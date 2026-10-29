package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.CarrierPageReq;
import com.ganghuitong.port.api.dto.biz.CarrierSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.vo.biz.CarrierDetailVO;
import com.ganghuitong.port.api.vo.biz.CarrierOptionVO;
import com.ganghuitong.port.api.vo.biz.CarrierVO;

import java.util.List;

/**
 * 承运商服务
 *
 * @author yangxueyong
 */
public interface CarrierService {

    /**
     * 分页查询承运商（含车辆数、运输统计）
     *
     * @param req 分页查询入参
     * @return 承运商分页数据
     */
    PageResp<CarrierVO> page(CarrierPageReq req);

    /**
     * 承运商下拉搜索
     *
     * @param req 关键字入参
     * @return 承运商选项
     */
    List<CarrierOptionVO> options(KeywordReq req);

    /**
     * 新增或修改承运商；新增时自动开通承运商账号并赋予承运商角色
     *
     * @param req 承运商保存入参
     * @return 承运商ID
     */
    String save(CarrierSaveReq req);

    /**
     * 删除承运商（存在运输中车辆时不可删除）
     *
     * @param req 主键入参
     */
    void delete(IdReq req);

    /**
     * 查询承运商详情（车辆、运输中货物、统计）
     *
     * @param req 主键入参
     * @return 承运商详情
     */
    CarrierDetailVO detail(IdReq req);

    /**
     * 承运商账号查询本承运商详情
     *
     * @return 承运商详情
     */
    CarrierDetailVO mine();
}
