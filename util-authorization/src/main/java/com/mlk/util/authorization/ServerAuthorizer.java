package com.mlk.util.authorization;

import com.mlk.util.invoke.exception.AppException;
import com.mlk.util.invoke.utils.DateUtil;
import com.mlk.util.invoke.utils.JsonUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 服务器间认证拦截 Header中需要传Server-Token和Server-Timestamp(单位秒)
 *
 * @author wangweizhen
 */
@Component
public class ServerAuthorizer {


    /**
     * token格式： v2-{TimestampInSeconds}-{Sign}
     * <p>
     * sign算法：sha1({AppKey}-{AppSecret}-{TimestampInSeconds}).toLowerCase()
     */
    public boolean verify(String appKey, String appSecret, String token, int expiredSenconds) {
        if (StringUtils.isBlank(token)) {
            return false;
        }
        final int versionIndex = 0;
        final int timestampIndex = 1;
        final int signIndex = 2;
        final String[] inviteCodeInfo = token.split("-");
        if ("v1".equalsIgnoreCase(inviteCodeInfo[0])) {
            if (inviteCodeInfo.length != 3 || "v2".equalsIgnoreCase(inviteCodeInfo[versionIndex])) {
                throw new AppException(-0x02000, "AuthorizationFailed");
            }
            String sign = DigestUtils.sha1Hex((JsonUtil
                    .format("{0}-{1}-{2}", appKey, appSecret, inviteCodeInfo[timestampIndex])).substring(0, 8)
                    .toLowerCase());
            if (!sign.equalsIgnoreCase(inviteCodeInfo[signIndex])) {
                throw new AppException(-0x02000, "Token Sign Fail");
            }
            if (expiredSenconds > 0 && Long.parseLong(inviteCodeInfo[1]) + expiredSenconds < DateUtil
                    .getTimestampInSeconds()) {
                throw new AppException(-0x02000, "Token Expired");
            }
            return true;
        } else {
            throw new AppException(-0x02000, "not support version");
        }
    }

}
