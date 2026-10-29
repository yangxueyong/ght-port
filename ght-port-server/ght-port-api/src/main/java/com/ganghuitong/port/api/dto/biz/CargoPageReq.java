package com.ganghuitong.port.api.dto.biz;

import com.ganghuitong.port.api.common.PageReq;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 泊位货物分页查询入参
 *
 * @author yangxueyong
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CargoPageReq extends PageReq {

    /**
     * 泊位ID
     */
    private String berthId;

    /**
     * 船运公司ID（按到货关联的船只所属船运公司筛选）
     */
    private String companyId;

    /**
     * 作业方向：UNLOAD-到货，LOAD-装货
     */
    private String direction;

    /**
     * 货物状态：IN_PROGRESS-作业中，COMPLETED-已完成
     */
    private String status;
}
