package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.PortElement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 港口俯视图元素数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface PortElementMapper extends BaseMapper<PortElement> {

    /**
     * 物理删除港口的全部俯视图元素（俯视图每次整体保存，避免逻辑删除数据膨胀）
     *
     * @param portId 港口ID
     * @return 删除行数
     */
    int physicalDeleteByPort(@Param("portId") String portId);
}
