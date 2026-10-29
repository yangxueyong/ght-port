package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.vo.biz.CountVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 分派承运数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface DispatchMapper extends BaseMapper<Dispatch> {

    /**
     * 按泊位统计指定状态的分派数量
     *
     * @param berthIds 泊位ID集合
     * @param status   分派状态码
     * @return 各泊位数量（bizId为泊位ID）
     */
    List<CountVO> countByBerth(@Param("berthIds") Collection<String> berthIds, @Param("status") String status);
}
