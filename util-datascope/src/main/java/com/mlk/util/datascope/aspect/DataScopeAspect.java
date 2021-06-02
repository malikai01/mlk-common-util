package com.mlk.util.datascope.aspect;

import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

/**
 * 数据过滤处理
 *
 * @author malikai
 * @version 1.0
 * @date 2021-2-5 14:52
 */
/*@Aspect
@Order(200)
public class DataScopeAspect {

    @Pointcut("@annotation(com.yeshj.hjclass.util.datascope.annotation.DataScope)")
    public void dataScopePointCut() {
    }

    @Before("dataScopePointCut()")
    public void doBefore(JoinPoint point) {
        handleDataScope(point);
    }


    @After("dataScopePointCut()")
    public void doAfter(JoinPoint joinPoint) {
        DataScopeContextHolder.clean();
    }

    private void handleDataScope(JoinPoint point) {
        // 获得注解
        Method method = ((MethodSignature) point.getSignature()).getMethod();

        if (method.isAnnotationPresent(DataScope.class) && (
                method.isAnnotationPresent(PlatformVerifyLogin.class))) {

            DataScope dataScope = method.getAnnotation(DataScope.class);

            //获取用户部门权限信息
            XPlatformUserDto xPlatformUser = CurrentUserContextHolder.getPlatformInfo();

            if (xPlatformUser != null) {
                dataScopeFilter(xPlatformUser, dataScope);
            }
        }
    }

    private void dataScopeFilter(XPlatformUserDto user, DataScope dataScope) {

        // 根据部门权限拼装sql
        String sqlParam;
        if (StringUtils.isNoneEmpty((dataScope.tableAlias()))) {
            sqlParam = " OR " + dataScope.tableAlias() + "." + dataScope.orgFieldName() + " = " + user.getOrgId();
        } else {
            sqlParam = " OR " + dataScope.orgFieldName() + " = " + user.getOrgId();
        }
        DataScopeContextHolder.setDataScopeParams(" AND (" + sqlParam.substring(4) + ")");
    }

}*/
