package com.ganghuitong.port.api.service.biz;

import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.dto.biz.ShipCompleteReq;
import com.ganghuitong.port.api.dto.biz.ShippingCompanySaveReq;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyDetailVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyVO;

/**
 * 船运公司服务
 *
 * @author yangxueyong
 */
public interface ShippingCompanyService {

    /**
     * 分页查询船运公司（含船只）
     *
     * @param req 分页查询入参
     * @return 船运公司分页数据
     */
    PageResp<ShippingCompanyVO> page(PageReq req);

    /**
     * 查询船运公司详情：装卸统计、船只及其作业状态
     *
     * @param req 主键入参（船运公司ID）
     * @return 船运公司详情
     */
    ShippingCompanyDetailVO detail(IdReq req);

    /**
     * 船只完成作业：卸货船只结束卸货；装货船只登记装船量并完成装船
     *
     * @param req 完成入参
     */
    void completeShip(ShipCompleteReq req);

    /**
     * 新增或修改船运公司
     *
     * @param req 船运公司保存入参
     * @return 船运公司ID
     */
    String save(ShippingCompanySaveReq req);

    /**
     * 删除船运公司及其船只
     *
     * @param req 主键入参
     */
    void delete(IdReq req);
}
