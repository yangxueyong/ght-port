package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.CarrierPageReq;
import com.ganghuitong.port.api.dto.biz.CarrierSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.service.biz.CarrierService;
import com.ganghuitong.port.api.vo.biz.CarrierDetailVO;
import com.ganghuitong.port.api.vo.biz.CarrierOptionVO;
import com.ganghuitong.port.api.vo.biz.CarrierVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 承运商管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/carrier")
@RequiredArgsConstructor
public class CarrierController {

    /**
     * 承运商服务
     */
    private final CarrierService carrierService;

    /**
     * 分页查询承运商
     *
     * @param req 分页查询入参
     * @return 承运商分页数据
     */
    @RequiresPermission("carrier:list")
    @PostMapping("/page")
    public R<PageResp<CarrierVO>> page(@Validated @RequestBody CarrierPageReq req) {
        return R.ok(carrierService.page(req));
    }

    /**
     * 承运商下拉搜索
     *
     * @param req 关键字入参
     * @return 承运商选项
     */
    @RequiresPermission({"carrier:list", "berth:dispatch:assign"})
    @PostMapping("/options")
    public R<List<CarrierOptionVO>> options(@Validated @RequestBody KeywordReq req) {
        return R.ok(carrierService.options(req));
    }

    /**
     * 新增或修改承运商（新增时自动开通承运商账号）
     *
     * @param req 承运商保存入参
     * @return 承运商ID
     */
    @RequiresPermission("carrier:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody CarrierSaveReq req) {
        return R.ok(carrierService.save(req));
    }

    /**
     * 删除承运商
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("carrier:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        carrierService.delete(req);
        return R.ok();
    }

    /**
     * 查询承运商详情
     *
     * @param req 主键入参
     * @return 承运商详情
     */
    @RequiresPermission({"carrier:list", "carrier:self"})
    @PostMapping("/detail")
    public R<CarrierDetailVO> detail(@Validated @RequestBody IdReq req) {
        return R.ok(carrierService.detail(req));
    }

    /**
     * 承运商账号查询本承运商详情
     *
     * @return 承运商详情
     */
    @RequiresPermission("carrier:self")
    @PostMapping("/mine")
    public R<CarrierDetailVO> mine() {
        return R.ok(carrierService.mine());
    }
}
