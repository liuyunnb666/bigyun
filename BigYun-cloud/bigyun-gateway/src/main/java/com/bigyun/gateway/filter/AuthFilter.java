package com.bigyun.gateway.filter;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.bigyun.common.core.constant.CacheConstants;
import com.bigyun.common.core.constant.HttpStatus;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.constant.TokenConstants;
import com.bigyun.common.core.utils.JwtUtils;
import com.bigyun.common.core.utils.ServletUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import reactor.core.publisher.Mono;

/**
 * Gateway authentication filter.
 *
 * @author bigyun
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered
{
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    private static final List<String> PUBLIC_WHITES = List.of(
            "/auth/face/liveness/login/session",
            "/auth/face/video-liveness/login/upload",
            "/auth/face/liveness/notify",
            "/auth/face/liveness/return",
            "/auth/face/liveness/status",
            "/file/profile/**",
            "/file/public/**"
    );

    @Autowired
    private IgnoreWhiteProperties ignoreWhite;

    @Autowired
    private RedisService redisService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain)
    {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutate = request.mutate();
        String url = request.getURI().getPath();
        if (StringUtils.matches(url, ignoreWhite.getWhites()) || StringUtils.matches(url, PUBLIC_WHITES))
        {
            return chain.filter(exchange);
        }
        String token = getToken(request);
        if (StringUtils.isEmpty(token))
        {
            return unauthorizedResponse(exchange, "Token is required");
        }
        Claims claims;
        try
        {
            claims = JwtUtils.parseToken(token);
        }
        catch (Exception e)
        {
            return unauthorizedResponse(exchange, "Token is invalid");
        }
        if (claims == null)
        {
            return unauthorizedResponse(exchange, "Token has expired");
        }
        String userkey = JwtUtils.getUserKey(claims);
        boolean islogin = redisService.hasKey(getTokenKey(userkey));
        if (!islogin)
        {
            return unauthorizedResponse(exchange, "Login state has expired");
        }
        String userid = JwtUtils.getUserId(claims);
        String username = JwtUtils.getUserName(claims);
        if (StringUtils.isEmpty(userid) || StringUtils.isEmpty(username))
        {
            return unauthorizedResponse(exchange, "Token verification failed");
        }
        addHeader(mutate, SecurityConstants.USER_KEY, userkey);
        addHeader(mutate, SecurityConstants.DETAILS_USER_ID, userid);
        addHeader(mutate, SecurityConstants.DETAILS_USERNAME, username);
        removeHeader(mutate, SecurityConstants.FROM_SOURCE);
        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value)
    {
        if (value == null)
        {
            return;
        }
        mutate.header(name, ServletUtils.urlEncode(value.toString()));
    }

    private void removeHeader(ServerHttpRequest.Builder mutate, String name)
    {
        mutate.headers(httpHeaders -> httpHeaders.remove(name)).build();
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String msg)
    {
        log.error("[Gateway auth failed] path={}, msg={}", exchange.getRequest().getPath(), msg);
        return ServletUtils.webFluxResponseWriter(exchange.getResponse(), msg, HttpStatus.UNAUTHORIZED);
    }

    private String getTokenKey(String token)
    {
        return CacheConstants.LOGIN_TOKEN_KEY + token;
    }

    private String getToken(ServerHttpRequest request)
    {
        String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX))
        {
            token = token.replaceFirst(TokenConstants.PREFIX, StringUtils.EMPTY).trim();
        }
        return token;
    }

    @Override
    public int getOrder()
    {
        return -200;
    }
}