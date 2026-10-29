package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.ShipCompleteReq;
import com.ganghuitong.port.api.dto.biz.ShippingCompanySaveReq;
import com.ganghuitong.port.api.service.biz.ShippingCompanyService;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyDetailVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 船运公司管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/shipping-company")
@RequiredArgsConstructor
public class ShippingCompanyController {

    /**
     * 船运公司服务
     */
    private final ShippingCompanyService shippingCompanyService;

    /**
     * 分页查询船运公司（含船只）
     *
     * @param req 分页查询入参
     * @return 船运公司分页数据
     */
    @RequiresPermission("shipper:list")
    @PostMapping("/page")
    public R<PageResp<ShippingCompanyVO>> page(@Validated @RequestBody PageReq req) {
        return R.ok(shippingCompanyService.page(req));
    }

    /**
     * 查询船运公司详情：装卸统计、船只及其作业状态
     *
     * @param req 主键入参（船运公司ID）
     * @return 船运公司详情
     */
    @RequiresPermission("shipper:list")
    @PostMapping("/detail")
    public R<ShippingCompanyDetailVO> detail(@Validated @RequestBody IdReq req) {
        return R.ok(shippingCompanyService.detail(req));
    }

    /**
     * 船只完成作业（卸货船只结束卸货；装货船只登记装船量）
     *
     * @param req 完成入参
     * @return 操作结果
     */
    @RequiresPermission({"shipper:edit", "berth:work:edit"})
    @PostMapping("/ship/complete")
    public R<Void> completeShip(@Validated @RequestBody ShipCompleteReq req) {
        shippingCompanyService.completeShip(req);
        return R.ok();
    }

    /**
     * 新增或修改船运公司
     *
     * @param req 船运公司保存入参
     * @return 船运公司ID
     */
    @RequiresPermission("shipper:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody ShippingCompanySaveReq req) {
        return R.ok(shippingCompanyService.save(req));
    }

    /**
     * 删除船运公司及其船只
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("shipper:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        shippingCompanyService.delete(req);
        return R.ok();
    }
}
