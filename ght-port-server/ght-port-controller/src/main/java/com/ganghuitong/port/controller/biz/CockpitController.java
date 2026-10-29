package com.ganghuitong.port.controller.biz;

import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.CockpitReq;
import com.ganghuitong.port.api.service.biz.CockpitService;
import com.ganghuitong.port.api.vo.biz.CockpitVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 大屏驾驶舱接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/biz/cockpit")
@RequiredArgsConstructor
public class CockpitController {

    /**
     * 驾驶舱服务
     */
    private final CockpitService cockpitService;

    /**
     * 查询驾驶舱数据
     *
     * @param req 查询入参（港口ID，可为空）
     * @return 驾驶舱数据
     */
    @RequiresPermission("cockpit:view")
    @PostMapping("/overview")
    public R<CockpitVO> overview(@Validated @RequestBody CockpitReq req) {
        return R.ok(cockpitService.overview(req));
    }
}
