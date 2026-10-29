package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.PageResp;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.GatePassPageReq;
import com.ganghuitong.port.api.dto.biz.TokenReq;
import com.ganghuitong.port.api.service.biz.GatePassService;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 出门条接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/gate-pass")
@RequiredArgsConstructor
public class GatePassController {

    /**
     * 出门条服务
     */
    private final GatePassService gatePassService;

    /**
     * 分页查询出门条
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    @RequiresPermission("gate:pass:list")
    @PostMapping("/page")
    public R<PageResp<GatePassVO>> page(@Validated @RequestBody GatePassPageReq req) {
        return R.ok(gatePassService.page(req));
    }

    /**
     * 司机查询自己的出门条
     *
     * @param req 分页查询入参
     * @return 出门条分页数据
     */
    @RequiresPermission("gate:pass:mine")
    @PostMapping("/mine")
    public R<PageResp<GatePassVO>> mine(@Validated @RequestBody GatePassPageReq req) {
        return R.ok(gatePassService.myPasses(req));
    }

    /**
     * 门卫扫码预览出门条
     *
     * @param req 令牌入参
     * @return 出门条信息
     */
    @RequiresPermission("gate:pass:verify")
    @PostMapping("/preview")
    public R<GatePassVO> preview(@Validated @RequestBody TokenReq req) {
        return R.ok(gatePassService.preview(req));
    }

    /**
     * 门卫核销出门条
     *
     * @param req 令牌入参
     * @return 核销后的出门条
     */
    @RequiresPermission("gate:pass:verify")
    @PostMapping("/verify")
    public R<GatePassVO> verify(@Validated @RequestBody TokenReq req) {
        return R.ok(gatePassService.verify(req));
    }
}
