package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.IdReq;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.CargoTypeSaveReq;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.entity.biz.CargoType;
import com.ganghuitong.port.api.service.biz.CargoTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 货物类型接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/cargo-type")
@RequiredArgsConstructor
public class CargoTypeController {

    /**
     * 货物类型服务
     */
    private final CargoTypeService cargoTypeService;

    /**
     * 按关键字查询货物类型
     *
     * @param req 关键字入参
     * @return 货物类型列表
     */
    @RequiresPermission({"cargo:type:list", "berth:cargo:arrive"})
    @PostMapping("/list")
    public R<List<CargoType>> list(@Validated @RequestBody KeywordReq req) {
        return R.ok(cargoTypeService.list(req));
    }

    /**
     * 新增或修改货物类型
     *
     * @param req 货物类型保存入参
     * @return 货物类型ID
     */
    @RequiresPermission("cargo:type:edit")
    @PostMapping("/save")
    public R<String> save(@Validated @RequestBody CargoTypeSaveReq req) {
        return R.ok(cargoTypeService.save(req));
    }

    /**
     * 删除货物类型
     *
     * @param req 主键入参
     * @return 操作结果
     */
    @RequiresPermission("cargo:type:edit")
    @PostMapping("/delete")
    public R<Void> delete(@Validated @RequestBody IdReq req) {
        cargoTypeService.delete(req);
        return R.ok();
    }
}
