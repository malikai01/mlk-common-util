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
 * @author malikai
 * @date 2021-6-2 15:53
 */
public class InnerUtil {

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
