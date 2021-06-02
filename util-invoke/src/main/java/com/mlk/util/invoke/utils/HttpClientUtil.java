package com.mlk.util.invoke.utils;

import com.google.common.base.Stopwatch;
import com.mlk.util.invoke.*;
import com.mlk.util.invoke.HttpDelete;
import com.mlk.util.invoke.HttpRequestWrapper;
import com.mlk.util.invoke.exception.AppException;
import com.mlk.util.invoke.exception.SysErrorConsts;
import com.mlk.util.invoke.exception.SysException;
import com.mlk.util.invoke.model.HttpResult;
import com.mlk.util.invoke.model.SysRestConsts;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpConnectionFactory;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultHttpResponseParserFactory;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.util.EntityUtils;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.PlatformDependent.throwException;


/**
 * HtpClient4.0封装
 *
 * @author huangxin
 */
@Slf4j
public class HttpClientUtil extends TransferSlowLogger {

    private static final String USER_AGENT = "user_agent";
    private static final String HJ_IBJ = "hj_ibj";
    private static final String TIMER_PREFIX = "timer.http.client";

    private static volatile CloseableHttpClient httpClient;

    private static volatile boolean isMetricsEnable = true;

    private static List<HttpClientHandler> httpClientHandlers = new ArrayList<>();
    private static List<HttpClientIntercepter> httpClientIntercepters = new ArrayList<>();

    static {

        ServiceLoader<HttpClientHandler> slHttpClientHandler = ServiceLoader.load(HttpClientHandler.class);
        for (HttpClientHandler filter : slHttpClientHandler) {
            httpClientHandlers.add(filter);
        }

        ServiceLoader<HttpClientIntercepter> slHttpClientLogHandler = ServiceLoader.load(HttpClientIntercepter.class);
        for (HttpClientIntercepter filter : slHttpClientLogHandler) {
            httpClientIntercepters.add(filter);
        }

        Collections.sort(httpClientIntercepters);
    }

    private HttpClientUtil() {
    }

    private static RequestConfig requestConfig = null;

    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (CloseableHttpClient.class) {
                if (httpClient == null) {
                    ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
                    Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                            .<ConnectionSocketFactory>create().register("http", plainsf)
                            .register("https", SSLConnectionSocketFactory.getSystemSocketFactory()).build();
                    HttpConnectionFactory<HttpRoute, ManagedHttpClientConnection> connFactory = new ManagedHttpClientConnectionFactory(
                            DefaultHttpRequestWriterFactory.INSTANCE, DefaultHttpResponseParserFactory.INSTANCE);
                    DnsResolver dnsResolver = SystemDefaultDnsResolver.INSTANCE;
                    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(
                            socketFactoryRegistry, connFactory, dnsResolver);

                    SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
                    connManager.setDefaultSocketConfig(defaultSocketConfig);

                    connManager.setMaxTotal(1000);
                    connManager.setDefaultMaxPerRoute(500);
                    connManager.setValidateAfterInactivity(1000);

                    requestConfig = RequestConfig.custom()
                            .setSocketTimeout(10000)
                            .setConnectTimeout(5 * 1000)
                            .setConnectionRequestTimeout(2 * 1000)
                            .build();

                    ExceptionRetryHandler retryHandler = new ExceptionRetryHandler(10, true, false);

                    HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(connManager)
                            .setConnectionManagerShared(false).evictExpiredConnections()
                            .evictIdleConnections(10, TimeUnit.SECONDS).setDefaultRequestConfig(requestConfig)
                            .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
                            .setKeepAliveStrategy(new HJConnectionKeepAliveStrategy()).setRetryHandler(retryHandler);

                    httpClient = httpClientBuilder.build();

                    Thread closeThread = new IdleConnectionMonitorThread(connManager);
                    closeThread.setDaemon(true);
                    closeThread.start();
                }
            }
        }

        return httpClient;
    }

    /**
     * 发送 get请求Https
     */
    public static String sendHttpsGetByRetry(String httpUrl, long timeout, TimeUnit timeUnit, int retryCount,
                                             Header... header) {
        HttpGet httpGet = new HttpGet(httpUrl);// 创建get请求
        if (header != null) {
            httpGet.setHeaders(header);
        }
        return sendHttpRequestByRetry(httpGet, timeout, timeUnit, retryCount);
    }

    /**
     * 发送 put请求
     *
     * @param httpUrl 地址
     * @param jsonStr 参数 json
     */
    public static String sendHttpPutByRetry(String httpUrl, String jsonStr, long timeout, TimeUnit timeUnit,
                                            int retryCount, Header... header) {
        HttpPut httpPut = new HttpPut(httpUrl);
        try {
            // 设置参数
            StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");
            stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            httpPut.setEntity(stringEntity);
            if (header != null) {
                httpPut.setHeaders(header);
            }
        } catch (Exception e) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
        }
        return sendHttpRequestByRetry(httpPut, timeout, timeUnit, retryCount);
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param jsonStr 参数(格式:json)
     */
    public static String sendHttpPostByRetry(String httpUrl, String jsonStr, long timeout, TimeUnit timeUnit,
                                             int retryCount, Header... header) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        try {
            // 设置参数
            StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");
            stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setEntity(stringEntity);
            if (header != null) {
                httpPost.setHeaders(header);
            }
        } catch (Exception e) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
        }
        return sendHttpPostByRetry(httpPost, timeout, timeUnit, retryCount);
    }

    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     */
    public static String sendHttpPostByRetry(String httpUrl, long timeout, TimeUnit timeUnit, int retryCount,
                                             Header... header) {
        HttpPost httpPost = new HttpPost(httpUrl);// 创建httpPost
        if (header != null) {
            httpPost.setHeaders(header);
        }
        return sendHttpPostByRetry(httpPost, timeout, timeUnit, retryCount);
    }

    /**
     * 发送Post请求
     */
    private static String sendHttpPost(HttpPost httpPost, long timeout, TimeUnit timeUnit) {
        return HttpClientUtil.sendHttpRequest(httpPost, timeout, timeUnit);
    }

    public static String sendHttpRequest(HttpRequestBase httpRequestBase, long timeout, TimeUnit timeUnit) {
        return sendHttpRequestByRetry(httpRequestBase, timeout, timeUnit, 0);
    }

    /**
     * 发送Post请求
     */
    private static String sendHttpPostByRetry(HttpPost httpPost, long timeout, TimeUnit timeUnit, int retryCount) {
        return HttpClientUtil.sendHttpRequestByRetry(httpPost, timeout, timeUnit, retryCount);
    }


    /**
     * 发送 post请求
     *
     * @param httpUrl 地址
     * @param timeout timeout
     * @param headers 参数
     */
    public static String sendHttpDeleteByRetry(String httpUrl, long timeout, TimeUnit timeUnit, int retryCount,
                                               Header... headers) {
        HttpDelete httpDelete = new HttpDelete(httpUrl);// 创建httpDete
        if (headers != null) {
            httpDelete.setHeaders(headers);
        }
        return sendHttpRequestByRetry(httpDelete, timeout, timeUnit, retryCount);
    }

    /**
     * 发送delete请求，带body
     *
     * @param jsonStr json
     */
    public static String sendHttpDeleteByRetry(String httpUrl, String jsonStr, long timeout, TimeUnit timeUnit,
                                               int retryCount, Header... headers) {
        HttpDelete httpDelete = new HttpDelete(httpUrl);// 创建httpDete
        try {
            // 设置参数
            StringEntity stringEntity = new StringEntity(jsonStr, "UTF-8");
            stringEntity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            httpDelete.setEntity(stringEntity);
            if (headers != null) {
                httpDelete.setHeaders(headers);
            }
        } catch (Exception e) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
        }
        return sendHttpRequestByRetry(httpDelete, timeout, timeUnit, retryCount);
    }


    public static String sendHttpRequestByRetry(HttpRequestBase httpRequestBase, long timeout, TimeUnit timeUnit,
                                                final int retryCount) {
        HttpResult result = execute2ResultByRetry(httpRequestBase, timeout, timeUnit, retryCount, false);
        if (result != null) {
            return result.getResult();
        }

        return null;
    }

    public static HttpResult execute2ResultByRetry(HttpRequestBase httpRequestBase, long timeout, TimeUnit timeUnit,
                                                   final int retryCount, boolean isRturnHttpResponse) {
        int i = 0;
        if (httpRequestBase == null) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, "HttpRequestBase is null!");
        }
        while (i <= retryCount) {
            try {
                return execute2Result(httpRequestBase, timeout, timeUnit, isRturnHttpResponse);
            } catch (NoHttpResponseException e) {
                if (i == retryCount) {
                    throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error("hjframeworkExceptionLogHttp:" + e.getMessage() + " url:" + httpRequestBase.getURI().toString(), e);
                if (e instanceof AppException) {
                    throw (AppException) e;
                } else if (e instanceof SysException) {
                    throw (SysException) e;
                } else {
                    throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
                }
            }
            i++;
            log.info("HttpClient retrycount:{}", i);
        }
        return null;
    }

    public static HttpResult execute2Result(HttpRequestBase httpRequestBase, long timeout, TimeUnit timeUnit,
                                            boolean isRturnHttpResponse) throws Exception {
        Stopwatch begin = Stopwatch.createStarted();
        if (httpRequestBase == null) {
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, "HttpRequestBase is null!");
        }
        HttpResult result = null;
        HttpRequestWrapper httpRequest = null;
        CloseableHttpResponse response = null;
        String responseContent = null;

        preHandle(httpRequestBase);

        try {
            // 动态设置请求超时
            setConfig(httpRequestBase, timeout, timeUnit);

            // 添加框架全局header
            //addGlobalHeader(httpRequestBase);

            // 请求前进行拦截
            httpRequest = beforeLog(httpRequestBase);

            // 执行请求
            response = getHttpClient().execute(httpRequestBase);

            // 请求响应结果拦截
            afterLog(httpRequest, response);

            if (isRturnHttpResponse) {
                result = new HttpResult(response);
            } else {
                if (response != null) {
                    HttpEntity entity = response.getEntity();
                    responseContent = EntityUtils.toString(entity);
                    int statusCode = response.getStatusLine().getStatusCode();
                    result = new HttpResult(statusCode, responseContent);
                }
            }

            if (response != null) {
                afterCompletion(response.getAllHeaders());
            }

            return result;
        } catch (Exception e) {
            afterThrowingLog(httpRequest, e);
            throwException(e);
            throw new SysException(SysErrorConsts.SYS_ERROR_CODE, e.getMessage(), e);
        } finally {
            if (!isRturnHttpResponse) {
                closeResources(response, httpRequestBase);
            }
            //tryMetricsMark(httpRequestBase.getMethod().toLowerCase(), httpRequestBase.getURI().getPath(), begin.stop().elapsed(TimeUnit.NANOSECONDS));
            //showSlowHttpClient(end, httpRequestBase.getURI().toString());
        }
    }

    private static void preHandle(HttpRequestBase httpRequestBase) {
        for (HttpClientHandler handler : httpClientHandlers) {
            try {
                handler.preHandle(httpRequestBase);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static void setConfig(HttpRequestBase httpRequestBase, long timeout, TimeUnit timeUnit) {
        RequestConfig config = null;
        if (timeout != 10000L && timeout > 0L) {
            int timeoutInMS = Math.toIntExact(TimeUnit.MILLISECONDS.convert(timeout, timeUnit));
            config = RequestConfig.custom().setSocketTimeout(timeoutInMS).setConnectTimeout(timeoutInMS)
                    .setConnectionRequestTimeout(timeoutInMS).build();
        } else {
            config = requestConfig;
        }
        httpRequestBase.setConfig(config);
    }

    private static HttpRequestWrapper beforeLog(HttpRequestBase httpRequestBase) {
        HttpRequestWrapper wrapper = null;
        if (httpClientIntercepters.size() > 0) {
            wrapper = new HttpRequestWrapperImpl(MDC.get(SysRestConsts.REQUEST_ID), httpRequestBase);
            for (HttpClientIntercepter intercepter : httpClientIntercepters) {
                try {
                    intercepter.before(wrapper);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }
            }
        }
        return wrapper;
    }

    private static void afterLog(HttpRequestWrapper requestWrapper, CloseableHttpResponse response) {
        HttpResponseWrapper wrapper = null;
        if (httpClientIntercepters.size() > 0) {
            wrapper = new HttpResponseWrapperImpl(MDC.get(SysRestConsts.REQUEST_ID), response);
            for (HttpClientIntercepter intercepter : httpClientIntercepters) {
                try {
                    intercepter.after(requestWrapper, wrapper);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                }

            }
        }
    }

    private static void afterCompletion(Header[] headers) {
        for (HttpClientHandler handler : httpClientHandlers) {
            try {
                handler.afterCompletion(headers);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static void afterThrowingLog(HttpRequestWrapper wrapper, Exception ex) {
        for (HttpClientIntercepter intercepter : httpClientIntercepters) {
            try {
                intercepter.afterThrowing(wrapper, ex);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    private static void closeResources(CloseableHttpResponse httpResponse, HttpRequestBase httpRequestBase) {
        try {
            if (httpResponse != null) {
                httpResponse.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (httpRequestBase != null) {
                httpRequestBase.releaseConnection();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }
}
