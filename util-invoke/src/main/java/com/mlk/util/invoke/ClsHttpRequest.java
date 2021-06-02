package com.mlk.util.invoke;

import com.alibaba.fastjson.TypeReference;
import com.google.common.base.Strings;
import com.mlk.util.invoke.exception.AppException;
import com.mlk.util.invoke.model.DataResult;
import com.mlk.util.invoke.utils.HttpClientUtil;
import com.mlk.util.invoke.utils.InnerUtil;
import com.mlk.util.invoke.utils.JsonUtil;
import lombok.*;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 学习线 HTTP 同步请求封装，包含日志、反序列化
 * <pre>
 *      modified by : lee
 *      modified date: 2019/01/11
 * </pre>
 */
@ToString
@Getter
public final class ClsHttpRequest {
    private final Logger LOGGER = LoggerFactory.getLogger(ClsHttpRequest.class);

    @Builder
    public ClsHttpRequest(String httpUrl
            , String body
            , List<Header> headers
            , ClsHttpMethod method
            , Long timeout
            , TimeUnit timeUnit
            , int retryCount) {
        this.httpUrl = httpUrl;
        this.body = body;
        this.headers = headers;
        this.method = method;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.retryCount = retryCount;
    }

    /**
     * http 请求地址
     */
    @NonNull
    private String httpUrl;

    /**
     * POST、PUT 请求的 body，json 格式
     */
    private String body = "";

    /**
     * 请求头列表
     */
    @Singular
    private List<Header> headers = Collections.emptyList();

    /**
     * 请求方法
     */
    @NonNull
    private ClsHttpMethod method;
    /**
     * 超时时间,默认获取配置httpclient.socketTimeout值
     **/
    private Long timeout;
    /**
     * 超时单位,毫秒
     **/
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    /**
     * 重试次数,默认不重试
     **/
    private int retryCount = 0;
    /**
     * 请求service api 耗时
     **/
    private long requestCostTimeMs;
    /**
     * 反序列化service api response 耗时
     **/
    private long deserializeCostTimeMs;

    public String getBody() {
        return Strings.isNullOrEmpty(body) ? "" : body;
    }

    private ClsHttpRequest() {

    }

    /**
     * Execute string.
     *
     * @return the string
     */
    public String execute() {

        if (httpUrl == null || httpUrl.isEmpty()) {
            throw new IllegalArgumentException("httpUrl must not be null or empty.");
        }

        Header[] tempHeaders = getRequestHeaders();

        ensureSetTimeOut();

        long tempRequestCostTimeMs = System.currentTimeMillis();
        String rawRes = null;
        switch (method) {
            case GET:
                rawRes = HttpClientUtil.sendHttpsGetByRetry(httpUrl, timeout, timeUnit, retryCount, tempHeaders);
                break;
            case PUT:
                rawRes = HttpClientUtil.sendHttpPutByRetry(httpUrl, getBody(), timeout, timeUnit, retryCount
                        , tempHeaders);
                break;
            case POST:
                rawRes = Strings.isNullOrEmpty(getBody())
                        ? HttpClientUtil.sendHttpPostByRetry(httpUrl, timeout, timeUnit, retryCount, tempHeaders)
                        : HttpClientUtil.sendHttpPostByRetry(httpUrl, getBody(), timeout, timeUnit, retryCount
                        , tempHeaders);
                break;
            case DELETE:
                rawRes = Strings.isNullOrEmpty(getBody())
                        ? HttpClientUtil.sendHttpDeleteByRetry(httpUrl, timeout, timeUnit, retryCount, tempHeaders)
                        : HttpClientUtil.sendHttpDeleteByRetry(httpUrl, getBody(), timeout, timeUnit, retryCount
                        , tempHeaders);
                break;
            default:
                throw new AppException(-50000, String.format("not imp http method %s!", method));
        }


        requestCostTimeMs = System.currentTimeMillis() - tempRequestCostTimeMs;
        LOGGER.debug("start a http request cost {} millis, this info {}", requestCostTimeMs, this);


        return rawRes;
    }

    /**
     * Execute with status t.
     *
     * @param <T>   the type parameter
     * @param clazz the clazz
     * @return the t
     */
    public <T> T executeWithStatus(Class<T> clazz) {

        String raw = execute();

        long start = System.currentTimeMillis();
        try {
            T resp = JsonUtil.json2Object(raw, clazz);
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            return resp;
        } catch (Exception e) {
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            logDeserializeError(raw, e);

            throw e;
        }
    }

    /**
     * Execute with status t.
     *
     * @param <T>       the type parameter
     * @param reference the reference
     * @return the t
     */
    public <T> T executeWithStatus(TypeReference<T> reference) {

        String raw = execute();
        long start = System.currentTimeMillis();
        try {
            T resp = JsonUtil.json2Reference(raw, reference);
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            return resp;
        } catch (Exception e) {
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            logDeserializeError(raw, e);

            throw e;
        }
    }

    /**
     * Execute 2 reference t.
     * <pre>
     *     1.default check soa response status,if equal 0 throw exception
     * </pre>
     *
     * @param <T>       the type parameter
     * @param reference the reference
     * @return the t
     */
    public <T> T execute2Reference(TypeReference<DataResult<T>> reference) {
        return execute2Reference(reference, true);
    }

    /**
     * 执行请求并反序列化为领域对象
     *
     * @param reference     domain object
     * @param checkedStatus <code>true:检查soaStatus如果不等于0则抛出异常</code><code>false:不检查status</code>
     * @return T
     * @throws AppException checkedStatus=true 检查soaStatus如果不等于0则抛出异常
     * @author liguo
     * @date 2019/1/14 13:59
     * @version 1.0.0
     **/
    public <T> T execute2Reference(TypeReference<DataResult<T>> reference, boolean checkedStatus) {
        String raw = execute();

        long start = System.currentTimeMillis();
        DataResult<T> data;

        try {
            data = JsonUtil.json2Reference(raw, reference);
            deserializeCostTimeMs = System.currentTimeMillis() - start;
        } catch (Exception e) {
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            logDeserializeError(raw, e);

            throw e;
        }

        if (checkedStatus && data.getStatus() != 0) {
            logResponseError(raw, data.getStatus(), data.getMessage());
            throw new AppException(data.getStatus(), data.getMessage());
        }

        return data.getData();
    }

    /**
     * 执行 http 请求, 无返回值
     * 远程调用返回格式必须为 DataResult 格式
     */
    public void execute2Void() {
        execute2Void(true);
    }

    /**
     * 执行 http 请求, 无返回值，远程调用返回格式必须为 DataResult 格式
     *
     * @param checkedStatus <code>true:检查soaStatus如果不等于0则抛出AppException异常</code><code>false:不检查status</code>
     * @return void
     * @throws AppException
     * @author liguo
     * @date 2019/1/14 14:05
     * @version 1.0.0
     **/
    public void execute2Void(boolean checkedStatus) {

        String raw = execute();
        long start = System.currentTimeMillis();
        DataResult data;
        try {
            data = JsonUtil.json2Reference(raw, new TypeReference<DataResult>() {
            });
            deserializeCostTimeMs = System.currentTimeMillis() - start;
        } catch (Exception e) {
            deserializeCostTimeMs = System.currentTimeMillis() - start;
            logDeserializeError(raw, e);
            throw e;
        }

        if (checkedStatus && data.getStatus() != 0) {
            logResponseError(raw, data.getStatus(), data.getMessage());
            throw new AppException(data.getStatus(), data.getMessage());
        }

    }

    private void logDeserializeError(String raw, Exception e) {

        LOGGER.error("deserialize response to json error, httpUrl: {}, raw response: {}, exception: {}",
                httpUrl
                , raw
                , e);
    }

    private void logResponseError(String raw, int serverStatus, String serverMessage) {

        LOGGER.error("this response is not ok, url: {}, this request is {}, raw response: {}"
                , httpUrl
                , this
                , raw);
    }

    private Header[] getRequestHeaders() {

        String soaIndexValue = InnerUtil.getHead("soaIndex", "0");
        Header soaIndexHeader = new BasicHeader("soaIndex", soaIndexValue);

        if (CollectionUtils.isEmpty(headers)) {
            return new Header[]{soaIndexHeader};
        }
        headers.add(soaIndexHeader);
        return headers.toArray(new Header[]{});
    }

    private void ensureSetTimeOut() {
        boolean hasTimeOut = (timeout != null && timeout > 0 && timeUnit != null);
        if (!hasTimeOut) {
            timeout = 10000L;
            timeUnit = TimeUnit.MILLISECONDS;
        }
    }
}
