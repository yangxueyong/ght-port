package com.ganghuitong.port.api.dto.biz;

import lombok.Data;

import java.io.Serializable;

/**
 * 关键字下拉搜索入参
 *
 * @author yangxueyong
 */
@Data
public class KeywordReq implements Serializable {

    /**
     * 搜索关键字
     */
    private String keyword;

    /**
     * 上级业务ID（如承运商ID、船运公司ID），可为空
     */
    private String parentId;
}
