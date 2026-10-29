package com.ganghuitong.port.service.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ganghuitong.port.api.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充：创建人、创建时间、更新人、更新时间
 *
 * @author yangxueyong
 */
@Component
public class AutoFillMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时填充公共字段
     *
     * @param metaObject 实体元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        String username = UserContext.getUsername();
        this.strictInsertFill(metaObject, "createBy", String.class, username);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateBy", String.class, username);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新时填充公共字段
     *
     * @param metaObject 实体元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.setFieldValByName("updateBy", UserContext.getUsername(), metaObject);
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
