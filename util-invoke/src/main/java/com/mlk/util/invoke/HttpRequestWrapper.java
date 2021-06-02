package com.mlk.util.invoke;

import org.apache.http.client.config.RequestConfig;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Map;

/**
 * @Author: dylan
 * @Date: 2019-02-16 22:25
 * @Desc:
 */
public interface HttpRequestWrapper {

    String getId();

    String getMethod();

    String getQueryString();
    
    URI getURI();

    String getProtocol();
    
    RequestConfig getRequestConfig();

    String getCharacterEncoding();

    String getRequestURLString();

    MultiValueMap<String, String> getHeaders();
    
    void setHeader(String name, String value);

    boolean isMultipart();

    boolean isBinaryContent();

    InputStream getInputStream() throws IOException;

    Map<String, Object> getAllParamData();

    void addParamData(String key, Object data);

    String getContentTypeString();
}
