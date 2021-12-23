package com.mlk.util.resubmit.aspect;

import com.mlk.util.resubmit.AspectjUtils;
import com.mlk.util.resubmit.ResubmitLock;
import com.mlk.util.resubmit.annotation.Resubmit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author malikai
 * @date 2021年06月18日 13:38
 */
@Aspect
@Component
public class ResubmitDataAspect {

    private final static Object PRESENT = new Object();

    @Around("@annotation(com.mlk.util.resubmit.annotation.Resubmit)")
    public Object handleResubmit(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        //获取注解信息
        Resubmit annotation = method.getAnnotation(Resubmit.class);
        int delaySeconds = annotation.delaySeconds();
        List<String> keyParams = Arrays.asList(annotation.keyParams());
        String keyName = annotation.keyName();
        //获取参数
        Map<String, Object> requestParam = AspectjUtils.getNameAndValue(joinPoint);
        //解析参数
        StringBuffer sb = new StringBuffer(keyName);
        requestParam.forEach((k, v) -> {
            if (CollectionUtils.isEmpty(keyParams) || keyParams.contains(k)) {
                sb.append(v).append(",");
            }
        });
        //生成加密参数 使用了content_MD5的加密方式
        String key = ResubmitLock.handleKey(sb.toString());
        //执行锁
        boolean lock = false;
        try {
            //设置解锁key
            lock = ResubmitLock.getInstance().lock(key, PRESENT);
            if (lock) {
                //放行
                return joinPoint.proceed();
            } else {
                //响应重复提交异常
                throw new Exception("重复提交");
            }
        } finally {
            //设置解锁key和解锁时间
            ResubmitLock.getInstance().unLock(lock, key, delaySeconds);
        }
    }
}
