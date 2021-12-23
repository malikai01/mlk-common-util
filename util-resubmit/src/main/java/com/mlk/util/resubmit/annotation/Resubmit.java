package com.mlk.util.resubmit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resubmit {

    /**
     * 前缀
     *
     * @return java.lang.String
     * @author malikai
     * @date 2021/12/23 16:05
     */
    String keyName() default "";

    /**
     * 请求参数的key，不传默认全部请求参数
     *
     * @return java.lang.String[]
     * @author malikai
     * @date 2021/12/23 16:05
     */
    String[] keyParams() default {};

    /**
     * 延时时间 在延时多久后可以再次提交
     *
     * @return int
     * @author malikai
     * @date 2021/12/23 16:05
     */
    int delaySeconds() default 5;
}
