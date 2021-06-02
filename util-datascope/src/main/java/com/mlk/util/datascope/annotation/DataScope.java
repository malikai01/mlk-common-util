package com.mlk.util.datascope.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解,需要配合X平台一起使用，否则不生效
 *
 * @author malikai
 * @version 1.0.0
 * @date 2021-2-5 11:52
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 关联表别名
     * @return
     */
    String tableAlias() default "";

    /**
     * 关联字段
     * @return
     */
    String orgFieldName() default "tenant_id";
}
