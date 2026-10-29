package com.ganghuitong.port.controller.open;

import com.ganghuitong.port.annotation.Anonymous;
import com.ganghuitong.port.api.common.R;
import com.ganghuitong.port.api.dto.biz.TokenReq;
import com.ganghuitong.port.api.service.biz.GatePassService;
import com.ganghuitong.port.api.service.sys.AuthService;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import com.ganghuitong.port.api.vo.sys.TenantOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 免登录开放接口
 *
 * @author yangxueyong
 */
@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {

    /**
     * 登录认证服务
     */
    private final AuthService authService;

    /**
     * 出门条服务
     */
    private final GatePassService gatePassService;

    /**
     * 登录页租户（港口）选项
     *
     * @return 租户选项
     */
    @Anonymous
    @PostMapping("/tenants")
    public R<List<TenantOptionVO>> tenants() {
        return R.ok(authService.tenantOptions());
    }

    /**
     * 司机通过链接查看出门条
     *
     * @param req 出门条令牌入参
     * @return 出门条信息
     */
    @Anonymous
    @PostMapping("/gatePass/view")
    public R<GatePassVO> gatePass(@Validated @RequestBody TokenReq req) {
        return R.ok(gatePassService.publicView(req));
    }
}
