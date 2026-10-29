package com.ganghuitong.port.service.mapper.biz;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ganghuitong.port.api.dto.biz.KeywordReq;
import com.ganghuitong.port.api.entity.biz.Ship;
import com.ganghuitong.port.api.vo.biz.ShipVO;
import com.ganghuitong.port.api.vo.biz.ShippingCompanyStatVO;
import com.ganghuitong.port.api.vo.biz.WorkingShipVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * 船只数据访问层
 *
 * @author yangxueyong
 */
@Mapper
public interface ShipMapper extends BaseMapper<Ship> {

    /**
     * 船只下拉搜索：关键字匹配船名、船舶识别号或船运公司名称
     *
     * @param req   关键字入参（parentId为船运公司ID）
     * @param limit 返回条数上限
     * @return 船只列表（含船运公司名称）
     */
    List<ShipVO> selectOptions(@Param("req") KeywordReq req, @Param("limit") Integer limit);

    /**
     * 根据ID集合查询船只（含船运公司名称）
     *
     * @param ids 船只ID集合
     * @return 船只列表
     */
    List<ShipVO> selectVoByIds(@Param("ids") Collection<String> ids);

    /**
     * 统计卸货（到货）量：按船运公司或船只汇总关联到货的吨数、方数，同一船运公司多船关联同一批货物只计一次
     *
     * @param companyIds 船运公司ID集合
     * @param byShip     true-按船只汇总，false-按船运公司汇总
     * @return 统计结果（bizId为船运公司ID或船只ID）
     */
    List<ShippingCompanyStatVO> selectUnloadStat(@Param("companyIds") Collection<String> companyIds, @Param("byShip") boolean byShip);

    /**
     * 统计装货（装船）量：按船运公司或船只汇总已装船的吨数、方数
     *
     * @param companyIds 船运公司ID集合
     * @param byShip     true-按船只汇总，false-按船运公司汇总
     * @return 统计结果（bizId为船运公司ID或船只ID）
     */
    List<ShippingCompanyStatVO> selectLoadStat(@Param("companyIds") Collection<String> companyIds, @Param("byShip") boolean byShip);

    /**
     * 查询正在卸货的船只（关联作业中的到货）
     *
     * @param companyIds 船运公司ID集合
     * @return 作业船只
     */
    List<WorkingShipVO> selectUnloadingShips(@Param("companyIds") Collection<String> companyIds);

    /**
     * 查询正在装货的船只（待装船的分派）
     *
     * @param companyIds 船运公司ID集合
     * @return 作业船只
     */
    List<WorkingShipVO> selectLoadingShips(@Param("companyIds") Collection<String> companyIds);
}
