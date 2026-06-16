package com.bigyun.auth.support;

import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.context.SecurityContextHolder;
import com.bigyun.common.security.handler.GlobalExceptionHandler;
import com.bigyun.system.domain.model.LoginUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * Controller 接口通测基座。
 *
 * <p>这个基座用于构建“轻量、隔离、可批量复制”的 MockMvc 测试环境：
 * 不启动完整 SpringBoot 容器，不连接 Nacos、Redis、MySQL 或第三方服务，
 * 只把当前 Controller 和被 mock 的依赖组装起来，快速验证接口参数、返回结构和异常转换。</p>
 *
 * <p>BigYun 的当前用户信息由 {@link SecurityContextHolder} 保存，
 * {@code SecurityUtils} 会从线程上下文读取 userId、username 和 LoginUser。
 * 所以接口测试应优先使用 {@link #setLoginUser(Long, String)} 模拟登录态，
 * 而不是照搬 StpUtil 这类 static mock。</p>
 */
public abstract class ControllerTestSupport
{
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    /**
     * 构建独立 MockMvc。
     *
     * <p>standaloneSetup 的价值是让测试只关注 Controller 行为：
     * 依赖通过 Mockito 注入，异常统一交给 GlobalExceptionHandler 转换，
     * 参数校验使用 LocalValidatorFactoryBean，响应编码固定为 UTF-8。</p>
     *
     * @param controllers 本次要测试的 Controller 实例，可以传一个或多个
     * @return 已接入全局异常处理、参数校验和 JSON 转换器的 MockMvc
     */
    protected MockMvc buildMockMvc(Object... controllers)
    {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controllers)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(
                        new ByteArrayHttpMessageConverter(),
                        new StringHttpMessageConverter(StandardCharsets.UTF_8),
                        new MappingJackson2HttpMessageConverter(objectMapper)
                )
                .build();
    }

    /**
     * 将 DTO、VO 或 Map 序列化为请求 JSON。
     *
     * <p>统一从这里序列化，后续遇到 JavaTime、枚举等 Jackson 模块时，
     * 不需要在每个测试类里重复配置 ObjectMapper。</p>
     */
    protected String toJson(Object value) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(value);
    }

    /**
     * 给 Controller 私有字段注入 mock 依赖。
     *
     * <p>BigYun 部分旧 Controller 仍是字段注入风格。
     * 测试中不启动 Spring 容器时，需要用反射把 Mockito mock 放进去；
     * 后续模块如果已经是构造器注入，可以直接 new Controller(mockA, mockB)。</p>
     */
    protected void injectField(Object target, String fieldName, Object value)
    {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    /**
     * 写入一个最小可用的登录上下文。
     *
     * <p>这会同时设置 userId、username、token 和 LoginUser，
     * 覆盖 Controller、Service 中常见的 {@code SecurityUtils.getUserId()}、
     * {@code SecurityUtils.getUsername()}、{@code SecurityUtils.getLoginUser()} 读取路径。</p>
     */
    protected void setLoginUser(Long userId, String username)
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserid(userId);
        loginUser.setUsername(username);
        loginUser.setToken("token-" + userId);
        SecurityContextHolder.setUserId(String.valueOf(userId));
        SecurityContextHolder.setUserName(username);
        SecurityContextHolder.set(SecurityConstants.USER_KEY, loginUser.getToken());
        SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
    }

    /**
     * 清理当前线程登录上下文。
     *
     * <p>SecurityContextHolder 是线程级上下文，测试之间如果不清理，
     * “未登录”用例可能被前一个“已登录”用例污染。</p>
     */
    protected void clearLoginUser()
    {
        SecurityContextHolder.remove();
    }

    /**
     * 断言 BigYun 统一返回体中的 code。
     *
     * <p>返回 ResultActions 方便继续链式追加 data、msg 等 jsonPath 断言。</p>
     */
    protected ResultActions expectCode(ResultActions actions, int code) throws Exception
    {
        return actions.andExpect(jsonPath("$.code").value(code));
    }

    /**
     * 断言 BigYun 统一返回体中的 msg。
     */
    protected ResultActions expectMessage(ResultActions actions, String message) throws Exception
    {
        return actions.andExpect(jsonPath("$.msg").value(message));
    }
}
