package com.mlk.util.authorization;


import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServerAuthority {

    /*
     *   认证方式：header中传Server-Token、Server-AppKey
     *
     *   Server-AppKey格式为：系统配置的AppKey
     *
     *   Server-Token格式为：v1-{TimestampInSeconds}-{Sign}
     *
     *   Sign:格式为：sha1({AppKey}-{AppSecret}-{TimestampInSeconds}).toLowerCase()
     *
     *   配置文件中配置为：mlk.{AppKey}=123456则表示AppKey为：{AppKey},AppSecret为：123456
     *
     */
    VersionType version() default VersionType.V1;
}
