package com.ganghuitong.port.service.helper;

import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.context.LoginUser;
import com.ganghuitong.port.api.context.UserContext;
import com.ganghuitong.port.api.enums.UserTypeEnum;
import com.ganghuitong.port.api.exception.CustException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 数据范围辅助组件：承运商账号只能访问本承运商的数据
 *
 * @author yangxueyong
 */
@Slf4j
@Component
public class DataScopeHelper {

    /**
     * 判断当前登录用户是否为承运商账号
     *
     * @return true-承运商账号
     */
    public boolean isCarrier() {
        LoginUser user = UserContext.get();
        return user != null && StrUtil.equals(UserTypeEnum.CARRIER.getCode(), user.getUserType());
    }

    /**
     * 解析实际生效的承运商ID：承运商账号强制使用本承运商，其他账号使用请求值
     *
     * @param requestCarrierId 请求中的承运商ID
     * @return 实际生效的承运商ID
     */
    public String resolveCarrierId(String requestCarrierId) {
        if (!isCarrier()) {
            return requestCarrierId;
        }
        String own = UserContext.require().getRefId();
        if (StrUtil.isBlank(own)) {
            log.warn("承运商账号[{}]未关联承运商", UserContext.getUsername());
            throw new CustException("当前承运商账号未关联承运商，请联系港口管理员");
        }
        return own;
    }

    /**
     * 校验当前账号是否有权访问指定承运商的数据
     *
     * @param carrierId 被访问数据所属的承运商ID
     */
    public void checkCarrier(String carrierId) {
        if (isCarrier() && !StrUtil.equals(resolveCarrierId(null), carrierId)) {
            log.warn("承运商账号[{}]尝试访问其他承运商[{}]的数据", UserContext.getUsername(), carrierId);
            throw new CustException("无权访问其他承运商的数据");
        }
    }
}
