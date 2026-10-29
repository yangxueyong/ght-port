package com.ganghuitong.port.interceptor;

import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.annotation.Anonymous;
import com.ganghuitong.port.annotation.RequiresPermission;
import com.ganghuitong.port.api.constant.AuthConstant;
import com.ganghuitong.port.api.constant.ResultCode;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.context.TenantContext;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.api.service.sys.LoginUserService;
import com.ganghuitong.port.api.service.sys.TokenService;
import com.ganghuitong.port.api.vo.sys.TokenClaimVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录鉴权与接口权限拦截器：解析令牌、设置用户与租户上下文、校验接口权限
 *
 * @author yangxueyong
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * 令牌服务
     */
    private final TokenService tokenService;

    /**
     * 登录用户加载服务
     */
    private final LoginUserService loginUserService;

    /**
     * 权限判断组件
     */
    private final PermissionChecker permissionChecker;

    /**
     * 请求进入控制器前进行登录与权限校验
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @return true-放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        if (method.hasMethodAnnotation(Anonymous.class)
                || AnnotatedElementUtils.hasAnnotation(method.getBeanType(), Anonymous.class)) {
            return true;
        }
        String header = request.getHeader(AuthConstant.TOKEN_HEADER);
        String token = StrUtil.removePrefix(StrUtil.nullToEmpty(header), AuthConstant.TOKEN_PREFIX);
        TokenClaimVO claim = tokenService.parseToken(token);
        if (claim == null) {
            log.info("请求[{}]未携带有效令牌", request.getRequestURI());
            throw new CustException(ResultCode.UNAUTHORIZED, "登录已过期，请重新登录");
        }
        TenantContext.set(claim.getTenantId());
        LoginUser user = loginUserService.load(claim.getUserId(), claim.getTenantId());
        if (user == null) {
            log.info("令牌对应用户[{}]不可用", claim.getUserId());
            throw new CustException(ResultCode.UNAUTHORIZED, "账号不可用，请重新登录");
        }
        UserContext.set(user);
        RequiresPermission permission = method.getMethodAnnotation(RequiresPermission.class);
        if (permission == null) {
            permission = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), RequiresPermission.class);
        }
        if (!permissionChecker.permitted(user, permission)) {
            log.warn("用户[{}]无权访问[{}]", user.getUsername(), request.getRequestURI());
            throw new CustException(ResultCode.FORBIDDEN, "没有操作权限");
        }
        return true;
    }

    /**
     * 请求结束后清理线程上下文
     *
     * @param request  请求
     * @param response 响应
     * @param handler  处理器
     * @param ex       异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        TenantContext.clear();
    }
}
