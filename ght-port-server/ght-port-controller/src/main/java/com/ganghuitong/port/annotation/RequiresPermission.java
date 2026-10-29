package com.ganghuitong.port.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 接口权限校验注解：拥有任意一个权限标识即可访问
 *
 * @author yangxueyong
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresPermission {

    /**
     * 权限标识（满足其一即可）
     *
     * @return 权限标识数组
     */
    String[] value();
}
