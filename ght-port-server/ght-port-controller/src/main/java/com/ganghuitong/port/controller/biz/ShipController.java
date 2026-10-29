package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.dto.biz.ShipSaveReq;
import com.ganghuitong.port.api.service.biz.ShipService;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 船只管理接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/ship")
@RequiredArgsConstructor
public class ShipController {

    /**
     * 船只服务
     */
    private final ShipService shipService;

    /**
     * 船只下拉搜索（可按船运公司名或船名）
     *
     * @param req 关键字入参
     * @return 船只列表
     */
    @RequiresPermission({"shipper:list", "berth:cargo:arrive", "berth:cargo:load"})
    @PostMapping("/options")
    public R<List<ShipVO>> options(@Validated @RequestBody KeywordReq req) {
        return R.ok(shipService.options(req));
    }

    /**
     * 新增或修改船只
     *
     * @param req 船只保存入参
     * @return 船只ID
     */
    @RequiresPermission("shipper:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody ShipSaveReq req) {
        return R.ok(shipService.save(req));
    }

    /**
     * 删除船只
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("shipper:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        shipService.delete(req);
        return R.ok();
    }
}
