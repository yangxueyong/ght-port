package com.ganghuitong.port.service.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.ganghuitong.port.api.constant.TableConstant;
import com.ganghuitong.port.api.context.TenantContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;

/**
 * 多租户行级隔离处理器：所有业务表自动追加 tenant_id 条件
 *
 * @author yangxueyong
 */
@Slf4j
public class GhtTenantLineHandler implements TenantLineHandler {

    /**
     * 获取当前租户ID表达式；无法确定租户时返回NULL，使查询结果为空，避免越权
     *
     * @return 租户ID表达式
     */
    @Override
    public Expression getTenantId() {
        String tenantId = TenantContext.get();
        if (tenantId == null) {
            log.warn("当前线程未识别到租户，租户条件将使用NULL以阻止数据访问");
            return new NullValue();
        }
        return new StringValue(tenantId);
    }

    /**
     * 获取租户字段名
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return TableConstant.TENANT_COLUMN;
    }

    /**
     * 判断表是否忽略租户隔离
     *
     * @param tableName 表名
     * @return true-忽略，false-需要隔离
     */
    @Override
    public boolean ignoreTable(String tableName) {
        return TableConstant.TENANT_IGNORE_TABLES.contains(tableName);
    }
}
