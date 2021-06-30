package com.mlk.util.authorization;

import com.mlk.util.invoke.config.BaseProperties;
import com.mlk.util.invoke.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * 权限校验
 *
 * @author malikai
 * @version 1.0
 * @date 2021-2-5 14:52
 */
@Aspect
public class AuthorizerAspect {

    @Autowired
    private ServerAuthorizer serverAuthorizer;

    @Pointcut("@annotation(com.mlk.util.authorization.ServerAuthority)")
    public void serverAuthorityPointCut() {
    }

    @Before("serverAuthorityPointCut()")
    public void doBefore(JoinPoint point) {
        handleDataScope(point);
    }


    @After("serverAuthorityPointCut()")
    public void doAfter(JoinPoint joinPoint) {
    }

    private void handleDataScope(JoinPoint point) {
        // 获得注解
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        if (method.isAnnotationPresent(ServerAuthority.class)) {

            ServerAuthority authrity = method.getAnnotation(ServerAuthority.class);

            if (authrity.version() == VersionType.V1) {
                /*
                 *  通过Server-AppToken认证
                 *
                 *  认证方式：header中传Server-Token、Server-AppKey
                 *
                 *  Server-AppKey格式为：系统配置的AppKey
                 *
                 *  Server-Token格式为：v1-{TimestampInSeconds}-{Sign}
                 *
                 *  Sign:格式为：sha1({AppKey}-{AppSecret}-{TimestampInSeconds}).toLowerCase()
                 *
                 *  配置文件中配置为：hj.cichang.apps.cichang_hujiang_com=123456则表示AppKey为：cichang_hujiang_com,AppSecret为：123456
                 *
                 */
                RequestAttributes ra = RequestContextHolder.getRequestAttributes();
                ServletRequestAttributes sra = (ServletRequestAttributes) ra;
                HttpServletRequest request = sra.getRequest();
                String appToken = request.getHeader("Server-Token");
                String appKey = request.getHeader("Server-AppKey");
                String appSecret = BaseProperties.getString("hj.dict.apps." + appKey);
                if (StringUtils.isBlank(appSecret)) {
                    throw new AppException(-0x02000, "unknown appkey @sai");
                }
                if (!serverAuthorizer.verify(appKey, appSecret, appToken, 600)) {
                    throw new AppException(-0x02000, "AuthorizationFailed");
                }
            }
        }
    }

}
