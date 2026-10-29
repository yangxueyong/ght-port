package com.ganghuitong.port.service.helper;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.constant.GatePassConstant;
import com.ganghuitong.port.api.entity.biz.GatePass;
import com.ganghuitong.port.api.enums.GatePassStatusEnum;
import com.ganghuitong.port.api.util.CodeEnumUtil;
import com.ganghuitong.port.api.vo.biz.GatePassVO;
import com.ganghuitong.port.service.config.GhtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 出门条视图组装器：补齐司机访问链接与状态描述
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class GatePassAssembler {

    /**
     * 系统配置
     */
    private final GhtProperties ghtProperties;

    /**
     * 转换单个出门条
     *
     * @param pass 出门条实体
     * @return 出门条视图
     */
    public GatePassVO toVo(GatePass pass) {
        GatePassVO vo = BeanUtil.copyProperties(pass, GatePassVO.class);
        vo.setLink(buildLink(pass.getAccessToken()));
        vo.setStatusDesc(CodeEnumUtil.desc(GatePassStatusEnum.class, pass.getStatus()));
        return vo;
    }

    /**
     * 批量转换出门条
     *
     * @param passes 出门条实体列表
     * @return 出门条视图列表
     */
    public List<GatePassVO> toVoList(List<GatePass> passes) {
        return passes.stream().map(this::toVo).toList();
    }

    /**
     * 生成司机访问链接
     *
     * @param token 访问令牌
     * @return 访问链接
     */
    public String buildLink(String token) {
        return StrUtil.removeSuffix(ghtProperties.getWebBaseUrl(), StrUtil.SLASH) + GatePassConstant.DRIVER_PATH + token;
    }
}
