package com.ganghuitong.port.api.vo.biz;

import com.ganghuitong.port.api.entity.biz.GatePass;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出门条信息
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GatePassVO extends GatePass {

    /**
     * 司机访问链接
     */
    private String link;

    /**
     * 状态描述
     */
    private String statusDesc;
}
