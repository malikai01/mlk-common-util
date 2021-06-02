package com.mlk.util.invoke;

import org.apache.http.Header;
import org.springframework.util.MultiValueMap;

/**
 * @Author: dylan
 * @Date: 2019-02-16 22:25
 * @Desc:
 */
public interface HttpResponseWrapper {

    String getCharacterEncoding();

    byte[] toByteArray();

    String getId();

    int getStatus();

    MultiValueMap<String, String> getHeaders();

    boolean getHasGizp();

    Header[] getAllHeaders();

}
