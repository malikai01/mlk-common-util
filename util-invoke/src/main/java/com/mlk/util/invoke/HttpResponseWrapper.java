package com.mlk.util.invoke;

import org.apache.http.Header;
import org.springframework.util.MultiValueMap;

/**
 * @author malikai
 * @date 2021-6-2 15:55
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
