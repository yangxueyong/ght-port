package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.vo.biz.BerthStatusCountVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 泊位数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface BerthMapper extends BaseMapper<Berth> {

    /**
     * 按港口、状态分组统计泊位数量
     *
     * @return 各港口各状态的泊位数量
     */
    List<BerthStatusCountVO> selectStatusCount();
}
