package com.mlk.util.resubmit.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Resubmit {

    /**
     * 延时时间 在延时多久后可以再次提交
     *
     * @return int
     * @author malikai
     * @date 2021-6-18 13:33
     */
    int delaySeconds() default 20;
}
