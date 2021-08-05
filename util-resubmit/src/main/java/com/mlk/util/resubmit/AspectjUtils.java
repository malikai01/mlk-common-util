package com.mlk.util.resubmit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

import java.util.HashMap;
import java.util.Map;

/**
 * @author malikai
 * @date 2021年08月05日 14:49
 */
public class AspectjUtils {
    /**
     * 获取某个Method的参数名称及对应的值
     *
     * @param joinPoint
     * @return Map<参数名称, 参数值></参数名称,参数值>
     */
    public static Map<String, Object> getNameAndValue(ProceedingJoinPoint joinPoint) {
        Map<String, Object> param = new HashMap<>();
        Object[] paramValues = joinPoint.getArgs();
        String[] paramNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < paramNames.length; i++) {
            param.put(paramNames[i], paramValues[i]);
        }
        return param;
    }
}
