package com.mlk.util.invoke.utils;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.nio.charset.Charset;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * @Author: lee
 * @version:1.0.0
 * @Date: 2019/1/11 17:23
 **/
public class InnerUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(InnerUtil.class);

    private InnerUtil() {

    }

    public static int tryGetStringBytes(final String val) {
        if (Strings.isNullOrEmpty(val)) {
            return 0;
        }

        try {
            return val.getBytes(Charset.forName("UTF-8")).length;
        } catch (NullPointerException e) {
            //ignore
            return 0;
        }
    }

    /**
     * 获取http context 对象
     *
     * @return javax.servlet.http.HttpServletRequest
     * @author liguo
     * @date 2018/12/19 20:04
     * @version 1.0.0
     **/
    public static HttpServletRequest getRequestContext() {
        try {
            return getRequestAttributes().getRequest();
        } catch (Exception e) {
            //ignore
            return null;
        }
    }

    /**
     * 获取head value
     *
     * @param key
     * @return T
     * @author liguo
     * @date 2019/1/15 11:10
     * @version 1.0.0
     **/
    @SuppressWarnings("unchecked")
    public static <T> T getHead(String key, String defaultVal) {
        try {
            return (T) getRequestContext().getHeader(key);
        } catch (Exception e) {
            //ignore
            return (T) defaultVal;
        }
    }

    /**
     * 获取http context 对象
     *
     * @return org.springframework.web.context.request.ServletRequestAttributes
     * @throws RuntimeException ServletRequestAttributes 为空null时抛出该异常
     * @author liguo
     * @date 2019/1/11 19:54
     * @version 1.0.0
     **/
    public static ServletRequestAttributes getRequestAttributes() {
        ServletRequestAttributes attributes = null;
        try {
            attributes = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes());
        } catch (Exception e) {
            //LOGGER.debug("HttpServletRequest context is null.");
        }
        if (attributes == null) {
            throw new RuntimeException("HttpServletRequest context is null..");
        }
        return attributes;
    }

    /**
     * 获取当前请求上下文对象
     *
     * @param key
     * @param defaultVal
     * @return T
     * @author liguo
     * @date 2019/1/11 19:59
     * @version 1.0.0
     **/
    @SuppressWarnings("unchecked")
    public static <T> T getContextObject(String key, T defaultVal) {
        try {
            T t = (T) getRequestAttributes().getAttribute(key, SCOPE_REQUEST);
            return t == null ? defaultVal : t;
        } catch (Exception e) {
            //ignore
            return defaultVal;
        }
    }

    /**
     * getReqApiHost
     *
     * @param reqUrl
     * @return java.lang.String
     * @author liguo
     * @date 2019/1/14 17:33
     * @version 1.0.0
     **/
    public static String getReqApiHost(String reqUrl) {
        if (Strings.isNullOrEmpty(reqUrl)) {
            return null;
        }
        return URI.create(reqUrl).getHost();
    }

    /**
     * 字符截断
     * <pre>
     *     返回的子字符窜长度小于等于len
     * </pre>
     *
     * @param str
     * @param start
     * @param len
     * @param suffix
     * @return java.lang.String
     * @author liguo
     * @date 2018/12/29 11:27
     * @version 1.0.0
     **/
    public static String subString(String str, int start, int len, String suffix) {

        if (Strings.isNullOrEmpty(str) || str.length() <= len) {
            return str;
        }

        suffix = Strings.isNullOrEmpty(suffix) ? "..." : suffix;

        boolean append = len - suffix.length() <= 0 ? false : true;

        int realLen = Math.min(start + (len - 3 <= 0 ? len : len - 3), str.length());

        return append ? str.substring(start, realLen) + suffix : str.substring(start, realLen);
    }
}
