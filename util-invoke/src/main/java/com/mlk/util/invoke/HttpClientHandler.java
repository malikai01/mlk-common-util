package com.mlk.util.invoke;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;


public interface HttpClientHandler {

    void preHandle(HttpRequestBase httpRequestBase);
    
    void afterCompletion(Header[] headers);
    
    void throwException(Exception ex);
}
