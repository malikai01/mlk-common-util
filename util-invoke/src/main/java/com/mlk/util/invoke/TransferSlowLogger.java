package com.mlk.util.invoke;

import com.mlk.util.invoke.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.Date;

/**
 * @author malikai
 * @date 2021-6-2 15:55
 */
public class TransferSlowLogger {
    
    protected TransferSlowLogger(){}

    protected static void markSlowLogger(long requestTime, long timeout, String path, Logger log) {
        try {
            if (requestTime > 0L) {
                long receiveTimestamp = DateUtil.getTimestampInMillis();
                long timeDifference = Math.abs(receiveTimestamp - requestTime);
                if (timeDifference >= timeout) {
                    log.warn("hjframeworkSlowLogHttp transfer_time_out(ms):{}; sent_at:{}; received_at:{}; client_ip:{}; server_ip:{}; reqeust_url:{};",
                            timeDifference, dataToString(requestTime), dataToString(receiveTimestamp),
                            MDC.get("clientIp"), MDC.get("serverIp"), path);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String dataToString(long timestamp) {
        return DateUtil.toDateString(new Date(timestamp), DateUtil.DEFAULT_DATEDETAIL_PATTERN);
    }
}
