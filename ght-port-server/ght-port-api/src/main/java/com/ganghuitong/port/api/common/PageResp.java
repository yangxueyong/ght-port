package com.ganghuitong.port.api.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询返回结构
 *
 * @param <T> 记录类型
 * @author yangxueyong
 */
@Data
public class PageResp<T> implements Serializable {

    /**
     * 总条数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long pageNum;

    /**
     * 每页条数
     */
    private Long pageSize;

    /**
     * 当前页记录
     */
    private List<T> records;

    /**
     * 根据MyBatis-Plus分页对象与转换后的记录构建分页返回
     *
     * @param page    MyBatis-Plus分页结果
     * @param records 转换后的记录列表
     * @param <T>     记录类型
     * @return 分页返回结构
     */
    public static <T> PageResp<T> of(IPage<?> page, List<T> records) {
        PageResp<T> resp = new PageResp<>();
        resp.setTotal(page.getTotal());
        resp.setPageNum(page.getCurrent());
        resp.setPageSize(page.getSize());
        resp.setRecords(records);
        return resp;
    }
}
