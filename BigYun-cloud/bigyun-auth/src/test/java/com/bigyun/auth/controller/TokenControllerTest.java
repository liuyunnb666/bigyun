package com.bigyun.auth.controller;

import com.bigyun.auth.factory.LoginStrategyFactory;
import com.bigyun.auth.form.LoginBody;
import com.bigyun.auth.service.AuthRateLimitService;
import com.bigyun.auth.service.SysLoginService;
import com.bigyun.auth.service.strategy.LoginStrategy;
import com.bigyun.auth.support.ControllerTestSupport;
import com.bigyun.common.security.service.TokenService;
import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.bigyun.common.core.constant.Constants.FAIL;
import static com.bigyun.common.core.constant.Constants.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TokenController 的接口级 MockMvc 通测样板。
 *
 * <p>这类测试不启动 auth 服务，也不连接 Redis/Nacos/MySQL。
 * Controller 依赖的 Service、Factory、Strategy 全部用 Mockito mock，
 * 只验证 HTTP 入参、Controller 分发逻辑、统一返回体结构和关键依赖调用参数。</p>
 */
@ExtendWith(MockitoExtension.class)
class TokenControllerTest extends ControllerTestSupport
{
    private MockMvc mockMvc;

    /*
     * @ExtendWith(MockitoExtension.class) 会在每个测试用例前创建 @Mock 对象。
     * 这些 mock 是 Controller 的外部依赖，测试只需要声明它们的返回值，
     * 不需要启动真实登录服务、token 服务或登录策略工厂。
     */
    @Mock
    private TokenService tokenService;

    @Mock
    private SysLoginService sysLoginService;

    @Mock
    private LoginStrategyFactory loginStrategyFactory;

    @Mock
    private LoginStrategy loginStrategy;

    @Mock
    private AuthRateLimitService authRateLimitService;

    @BeforeEach
    void setUp()
    {
        /*
         * TokenController 当前使用字段注入。
         * standalone MockMvc 不经过 Spring 容器创建 Controller，
         * 所以这里手动 new Controller，再把 mock 依赖反射注入进去。
         */
        TokenController controller = new TokenController();
        injectField(controller, "tokenService", tokenService);
        injectField(controller, "sysLoginService", sysLoginService);
        injectField(controller, "loginStrategyFactory", loginStrategyFactory);
        injectField(controller, "authRateLimitService", authRateLimitService);
        mockMvc = buildMockMvc(controller);
        /*
         * 即使本测试类暂时不模拟登录态，也统一清理线程上下文。
         * 这样后续新增“已登录/未登录”用例时不会被其他测试污染。
         */
        clearLoginUser();
    }

    @Test
    void loginReturnsTokenPayload() throws Exception
    {
        // 先约定 service 层返回，再用 MockMvc 只检查 Controller 如何包装 HTTP 响应。
        LoginUser loginUser = new LoginUser();
        Map<String, Object> tokenPayload = Map.of("access_token", "jwt-token", "expires_in", 120);
        when(sysLoginService.login("demoUser", "secret")).thenReturn(loginUser);
        when(tokenService.createToken(loginUser)).thenReturn(tokenPayload);

        // 普通 POST JSON 模板：contentType + toJson(dto) + status/code/data 断言。
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(buildLoginBody())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.access_token").value("jwt-token"))
                .andExpect(jsonPath("$.data.expires_in").value(120));
    }

    @Test
    void loginNewRejectsMissingType() throws Exception
    {
        // 参数缺失类用例应断言统一返回体，而不是只断言 HTTP 200。
        mockMvc.perform(post("/loginNew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(FAIL))
                .andExpect(jsonPath("$.msg").isString());
    }

    @Test
    void loginNewDispatchesStrategyAndReturnsTokenPayload() throws Exception
    {
        /*
         * loginNew 的核心不是密码校验本身，而是按 type 分发到正确策略。
         * 因此这里 mock LoginStrategyFactory 和 LoginStrategy，避免把策略内部逻辑混进 Controller 测试。
         */
        LoginUser loginUser = new LoginUser();
        Map<String, Object> tokenPayload = Map.of("access_token", "strategy-token", "expires_in", 120);
        when(loginStrategyFactory.getStrategy("1")).thenReturn(loginStrategy);
        when(loginStrategy.login(any(LoginReq.class))).thenReturn(loginUser);
        when(tokenService.createToken(loginUser)).thenReturn(tokenPayload);

        mockMvc.perform(post("/loginNew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"1",
                                  "userName":"demoUser",
                                  "password":"secret"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.access_token").value("strategy-token"));

        /*
         * 参数捕获用于校验 Controller 是否把 HTTP JSON 正确绑定并传给下游。
         * 后续测试 Feign、Redis、Service 调用参数时，也优先用 ArgumentCaptor 或 argThat。
         */
        ArgumentCaptor<LoginReq> captor = ArgumentCaptor.forClass(LoginReq.class);
        verify(loginStrategy).login(captor.capture());
        assertEquals("demoUser", captor.getValue().getUserName());
        assertEquals("secret", captor.getValue().getPassword());
    }

    private LoginBody buildLoginBody()
    {
        LoginBody body = new LoginBody();
        body.setUsername("demoUser");
        body.setPassword("secret");
        return body;
    }
}
