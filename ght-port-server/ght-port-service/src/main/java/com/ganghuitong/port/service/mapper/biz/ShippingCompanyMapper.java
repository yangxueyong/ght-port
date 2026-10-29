package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.entity.biz.ShippingCompany;
import org.apache.ibatis.annotations.Mapper;

/**
 * 船运公司数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface ShippingCompanyMapper extends BaseMapper<ShippingCompany> {
}
