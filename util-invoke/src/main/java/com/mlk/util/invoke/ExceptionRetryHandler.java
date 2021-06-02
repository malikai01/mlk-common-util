package com.mlk.util.invoke;

import com.mlk.util.invoke.model.SysRestConsts;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.RequestLine;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by xinhuang on 2017/8/17.
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class ExceptionRetryHandler implements HttpRequestRetryHandler {

    private final Logger log = LoggerFactory.getLogger(ExceptionRetryHandler.class);
    
    
    /** the number of times a method will be retried */
    private int retryCount = 5;
    
    private boolean isConnResetRetry;
    private boolean isConnTimeoutRetry;
    
    private static final String CONNECTION_RESET_MSG = "Connection reset";
    private static final String CONNECTION_TIEMOUT_MSG = "Connection timed out";
    private static final String NOHTTPRESPONSEEXCEPTION_MSG = "NoHttpResponseException";
    private static final String UNKNOWNHOSTEXCEPTION = "UnknownHostException";
    
    public ExceptionRetryHandler(int retryCount, boolean isConnResetRetry, boolean isConnTimeoutRetry){
        this.retryCount = retryCount;
        this.isConnResetRetry = isConnResetRetry;
        this.isConnTimeoutRetry = isConnTimeoutRetry;
    }
    
    
    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        if (executionCount >= retryCount) {
            return false;
        }
        
       
        if(exception instanceof NoHttpResponseException){
            showLog(exception, executionCount, context, log, NOHTTPRESPONSEEXCEPTION_MSG);
            return true;
        }
        
        if(exception instanceof UnknownHostException){
        	showLog(exception, executionCount, context, log, UNKNOWNHOSTEXCEPTION);
        	return true;
        }
        
        if(this.isConnResetRetry){
            String message = exception.getMessage();
            if(message != null && message.contains(CONNECTION_RESET_MSG)){
                showLog(exception, executionCount, context, log, CONNECTION_RESET_MSG);
                return true;
            }
        }
        
        if(this.isConnTimeoutRetry){
            String message = exception.getMessage();
            if(message != null && message.contains(CONNECTION_TIEMOUT_MSG)){
                showLog(exception, executionCount, context, log, CONNECTION_TIEMOUT_MSG);
                return true;
            }
        }
        
        return false;
    }
    
    private void showLog(IOException exception, int executionCount, HttpContext context, Logger log, String retryMsg){
        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        HttpRequest request = clientContext.getRequest();
        RequestLine rl = request.getRequestLine();
        log.warn("HttpClientRetry {}, requestId={} httpMethod={} url={} retryCount={} case={}", retryMsg,
                MDC.get(SysRestConsts.REQUEST_ID), rl.getMethod(), rl.getUri(), executionCount,
                exception.getMessage());
    }
    
    
    
    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }
}
