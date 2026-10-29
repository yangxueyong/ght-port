package com.ganghuitong.port.api.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页查询基础入参
 *
 * @author yangxueyong
 */
@Data
public class PageReq implements Serializable {

    /**
     * 当前页码，从1开始
     */
    private Long pageNum = 1L;

    /**
     * 每页条数
     */
    private Long pageSize = 12L;

    /**
     * 模糊搜索关键字
     */
    private String keyword;
}
