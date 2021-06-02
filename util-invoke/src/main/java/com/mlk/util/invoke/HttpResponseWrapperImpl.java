package com.mlk.util.invoke;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.BufferedHttpEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;

/**
 * @author malikai
 * @date 2021-6-2 15:55
 */
public class HttpResponseWrapperImpl implements HttpResponseWrapper {
    private final Logger log = LoggerFactory.getLogger(HttpResponseWrapperImpl.class);

    private String id;

    private HttpResponse response;

    private MultiValueMap<String, String> headers;

    public HttpResponseWrapperImpl(String requestId, HttpResponse response) {
        this.id = requestId;
        this.response = response;
    }

    @Override
    public byte[] toByteArray() {
        byte[] result = null;
        try {
            BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
            response.setEntity(entity);
            result = IOUtils.toByteArray(entity.getContent());
        } catch (Exception e) {
            log.error("Client 获取响应错误：toByteArray {}", e);
        }
        return result;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCharacterEncoding() {
        return response.getEntity().getContentEncoding() != null ? response.getEntity().getContentEncoding().getValue()
                : Charsets.UTF_8.name();
    }

    @Override
    public int getStatus() {
        return response.getStatusLine().getStatusCode();
    }

    @Override
    public MultiValueMap<String, String> getHeaders() {
        if (headers == null) {
            headers = new LinkedMultiValueMap<>();
            Arrays.stream(response.getAllHeaders()).forEach(p -> {
                headers.add(p.getName(), p.getValue());
            });
        }
        return headers;
    }

    @Override
    public boolean getHasGizp() {
        try {
            MultiValueMap<String, String> headerMap = getHeaders();
            String key = "Content-Encoding";
            if (headerMap.containsKey(key)) {
                return headerMap.get(key).stream().anyMatch(p -> p.equalsIgnoreCase("gzip"));
            }
        } catch (Exception e) {
            log.error("Client Log getHasGizp error: {}", e);
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Header[] getAllHeaders() {
        return response.getAllHeaders();
    }
}

