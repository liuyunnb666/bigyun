package com.bigyun.gateway.handler;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import com.bigyun.common.core.exception.CaptchaException;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.gateway.service.GatewayRateLimitService;
import com.bigyun.gateway.service.ValidateCodeService;
import reactor.core.publisher.Mono;

/**
 * 验证码获取
 *
 * @author bigyun
 */
@Component
public class ValidateCodeHandler implements HandlerFunction<ServerResponse>
{
    @Autowired
    private ValidateCodeService validateCodeService;

    @Autowired
    private GatewayRateLimitService gatewayRateLimitService;

    @Override
    public Mono<ServerResponse> handle(ServerRequest serverRequest)
    {
        AjaxResult ajax;
        try
        {
            gatewayRateLimitService.checkCaptcha(serverRequest);
            ajax = validateCodeService.createCaptcha();
        }
        catch (CaptchaException | IOException e)
        {
            return Mono.error(e);
        }
        return ServerResponse.status(HttpStatus.OK).body(BodyInserters.fromValue(ajax));
    }
}
