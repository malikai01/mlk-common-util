package com.mlk.util.invoke.model;

public class SysRestConsts {

    private SysRestConsts() {

    }

    public static final String REQUEST_ID = "requestId";
    public static final String TRACE_ID = "X-B3-TraceId";
    
    public static final String SERVER_IP = "serverIp";
    public static final String CLINET_IP = "clientIp";
    public static final Integer HTTP_TIMEOUT_IN_MS = 10000;
    
    public static final String HEADER_REQ_TIME = "X-HJ-Request-Time";
    public static final String HEADER_RESP_TIME = "X-HJ-Response-Time";
    public static final String HEADER_RESP_ID = "X-HJ-Response-ID";
    public static final String HEADER_SERVER_ID = "X-Server-ID";
    
    public static final String HEADER_POWERED_BY = "X-Powered-By";
    
    public static final String INCLUDE_URL_PATTEER = "/*";
    public static final String EXCLUSIONS_URL_PATTEER = "*do_not_delete/health_check,*do_not_delete/health_check/,*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*";
}
