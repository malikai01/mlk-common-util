package com.mlk.util.invoke;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;

import java.time.Duration;

/**
 * @author malikai
 * @date 2021-6-2 15:55
 */
public class HJConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

    private static final long DEFAULT_KEEP_ALIVE = Duration.ofMinutes(18).toMillis();

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        // Honor 'keep-alive' header  
        HeaderElementIterator it = new BasicHeaderElementIterator(
                response.headerIterator(HTTP.CONN_KEEP_ALIVE));
        while (it.hasNext()) {
            HeaderElement he = it.nextElement();
            String param = he.getName();
            String value = he.getValue();
            if (value != null && param.equalsIgnoreCase("timeout")) {
                return Long.parseLong(value) * 1000;
            }
        }

        return DEFAULT_KEEP_ALIVE;
    }
}
