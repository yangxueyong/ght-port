package com.ganghuitong.port.service.helper;

import com.ganghuitong.port.api.entity.biz.Berth;
import com.ganghuitong.port.api.entity.biz.BerthCargo;
import com.ganghuitong.port.api.entity.biz.CargoType;
import com.ganghuitong.port.api.entity.biz.Carrier;
import com.ganghuitong.port.api.entity.biz.Dispatch;
import com.ganghuitong.port.api.entity.biz.Port;
import com.ganghuitong.port.api.entity.biz.ShipLoad;
import com.ganghuitong.port.api.entity.biz.ShippingCompany;
import com.ganghuitong.port.api.entity.biz.TransportTrip;
import com.ganghuitong.port.api.entity.biz.Vehicle;
import com.ganghuitong.port.api.exception.CustException;
import com.ganghuitong.port.service.mapper.biz.BerthCargoMapper;
import com.ganghuitong.port.service.mapper.biz.BerthMapper;
import com.ganghuitong.port.service.mapper.biz.CargoTypeMapper;
import com.ganghuitong.port.service.mapper.biz.CarrierMapper;
import com.ganghuitong.port.service.mapper.biz.DispatchMapper;
import com.ganghuitong.port.service.mapper.biz.PortMapper;
import com.ganghuitong.port.service.mapper.biz.ShipLoadMapper;
import com.ganghuitong.port.service.mapper.biz.ShippingCompanyMapper;
import com.ganghuitong.port.service.mapper.biz.TransportTripMapper;
import com.ganghuitong.port.service.mapper.biz.VehicleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 业务实体查询辅助组件：按ID获取实体，不存在时统一抛出业务异常，供各服务复用
 *
 * @author yangxueyong
 */
@Component
@RequiredArgsConstructor
public class EntityLookupHelper {

    /**
     * 港口数据访问
     */
    private final PortMapper portMapper;

    /**
     * 泊位数据访问
     */
    private final BerthMapper berthMapper;

    /**
     * 泊位到货数据访问
     */
    private final BerthCargoMapper berthCargoMapper;

    /**
     * 货物类型数据访问
     */
    private final CargoTypeMapper cargoTypeMapper;

    /**
     * 承运商数据访问
     */
    private final CarrierMapper carrierMapper;

    /**
     * 车辆数据访问
     */
    private final VehicleMapper vehicleMapper;

    /**
     * 分派承运数据访问
     */
    private final DispatchMapper dispatchMapper;

    /**
     * 运输单数据访问
     */
    private final TransportTripMapper transportTripMapper;

    /**
     * 船运公司数据访问
     */
    private final ShippingCompanyMapper shippingCompanyMapper;

    /**
     * 装船分派数据访问
     */
    private final ShipLoadMapper shipLoadMapper;

    /**
     * 获取港口
     *
     * @param id 港口ID
     * @return 港口实体
     */
    public Port port(String id) {
        return require(portMapper.selectById(id), "港口不存在");
    }

    /**
     * 获取泊位
     *
     * @param id 泊位ID
     * @return 泊位实体
     */
    public Berth berth(String id) {
        return require(berthMapper.selectById(id), "泊位不存在");
    }

    /**
     * 获取泊位到货
     *
     * @param id 到货ID
     * @return 到货实体
     */
    public BerthCargo cargo(String id) {
        return require(berthCargoMapper.selectById(id), "货物记录不存在");
    }

    /**
     * 获取货物类型
     *
     * @param id 货物类型ID
     * @return 货物类型实体
     */
    public CargoType cargoType(String id) {
        return require(cargoTypeMapper.selectById(id), "货物类型不存在");
    }

    /**
     * 获取承运商
     *
     * @param id 承运商ID
     * @return 承运商实体
     */
    public Carrier carrier(String id) {
        return require(carrierMapper.selectById(id), "承运商不存在");
    }

    /**
     * 获取车辆
     *
     * @param id 车辆ID
     * @return 车辆实体
     */
    public Vehicle vehicle(String id) {
        return require(vehicleMapper.selectById(id), "车辆不存在");
    }

    /**
     * 获取分派承运记录
     *
     * @param id 分派ID
     * @return 分派实体
     */
    public Dispatch dispatch(String id) {
        return require(dispatchMapper.selectById(id), "分派记录不存在");
    }

    /**
     * 获取运输单
     *
     * @param id 运输单ID
     * @return 运输单实体
     */
    public TransportTrip trip(String id) {
        return require(transportTripMapper.selectById(id), "运输单不存在");
    }

    /**
     * 获取船运公司
     *
     * @param id 船运公司ID
     * @return 船运公司实体
     */
    public ShippingCompany shippingCompany(String id) {
        return require(shippingCompanyMapper.selectById(id), "船运公司不存在");
    }

    /**
     * 获取装船分派
     *
     * @param id 装船分派ID
     * @return 装船分派实体
     */
    public ShipLoad shipLoad(String id) {
        return require(shipLoadMapper.selectById(id), "装船记录不存在");
    }

    /**
     * 判空校验，为空时抛出业务异常
     *
     * @param entity  实体
     * @param message 为空时的提示
     * @param <T>     实体类型
     * @return 非空实体
     */
    private <T> T require(T entity, String message) {
        if (entity == null) {
            throw new CustException(message);
        }
        return entity;
    }
}
