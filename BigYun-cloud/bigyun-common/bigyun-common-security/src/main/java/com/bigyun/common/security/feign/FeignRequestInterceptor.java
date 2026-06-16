package com.bigyun.common.security.feign;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.bigyun.common.core.constant.InnerAuthConstants;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.utils.ServletUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.ip.IpUtils;
import com.bigyun.common.security.utils.InnerAuthSignUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;

@Component
public class FeignRequestInterceptor implements RequestInterceptor
{
    @Value("${security.inner-auth.secret:bigyun-inner-auth}")
    private String innerAuthSecret;

    @Override
    public void apply(RequestTemplate requestTemplate)
    {
        HttpServletRequest httpServletRequest = ServletUtils.getRequest();
        if (StringUtils.isNull(httpServletRequest))
        {
            fillInnerHeaders(requestTemplate);
            return;
        }
        if (StringUtils.isNotNull(httpServletRequest))
        {
            Map<String, String> headers = ServletUtils.getHeaders(httpServletRequest);
            String userId = headers.get(SecurityConstants.DETAILS_USER_ID);
            if (StringUtils.isNotEmpty(userId))
            {
                requestTemplate.header(SecurityConstants.DETAILS_USER_ID, userId);
            }
            String userKey = headers.get(SecurityConstants.USER_KEY);
            if (StringUtils.isNotEmpty(userKey))
            {
                requestTemplate.header(SecurityConstants.USER_KEY, userKey);
            }
            String userName = headers.get(SecurityConstants.DETAILS_USERNAME);
            if (StringUtils.isNotEmpty(userName))
            {
                requestTemplate.header(SecurityConstants.DETAILS_USERNAME, userName);
            }
            String authentication = headers.get(SecurityConstants.AUTHORIZATION_HEADER);
            if (StringUtils.isNotEmpty(authentication))
            {
                requestTemplate.header(SecurityConstants.AUTHORIZATION_HEADER, authentication);
            }
            requestTemplate.header("X-Forwarded-For", IpUtils.getIpAddr());
            fillInnerHeaders(requestTemplate);
        }
    }

    private void fillInnerHeaders(RequestTemplate requestTemplate)
    {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String nonce = InnerAuthSignUtils.nonce();
        String signature = InnerAuthSignUtils.sign(innerAuthSecret, timestamp, nonce,
                requestTemplate.method(), requestTemplate.path());
        requestTemplate.header(SecurityConstants.FROM_SOURCE, SecurityConstants.INNER);
        requestTemplate.header(InnerAuthConstants.TIMESTAMP_HEADER, timestamp);
        requestTemplate.header(InnerAuthConstants.NONCE_HEADER, nonce);
        requestTemplate.header(InnerAuthConstants.SIGNATURE_HEADER, signature);
    }
}
