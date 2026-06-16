package com.bigyun.auth.controller;

import com.bigyun.auth.constant.ScanLoginConstants;
import com.bigyun.auth.domain.ScanGrantPayload;
import com.bigyun.auth.domain.ScanSessionCache;
import com.bigyun.auth.support.ControllerTestSupport;
import com.bigyun.common.redis.service.RedisService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static com.bigyun.common.core.constant.Constants.FAIL;
import static com.bigyun.common.core.constant.Constants.SUCCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ScanSessionController 的扫码登录接口通测样板。
 *
 * <p>扫码登录依赖 Redis 保存 session、loginCode、grantCode 和短锁。
 * 本测试用 RedisService / RedisTemplate mock 表达这些外部状态，
 * 让测试只关注 Controller 的状态流转、key 约定、登录上下文读取和返回结构。</p>
 */
@ExtendWith(MockitoExtension.class)
class ScanSessionControllerTest extends ControllerTestSupport
{
    private MockMvc mockMvc;

    @Mock
    private RedisService redisService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @BeforeEach
    void setUp()
    {
        /*
         * ScanSessionController 依赖 RedisService，RedisService 内部又暴露 redisTemplate 字段。
         * 这里不连接真实 Redis，而是把两层对象都替换成 mock，方便断言 set/delete/setIfAbsent 调用。
         */
        ScanSessionController controller = new ScanSessionController();
        injectField(controller, "redisService", redisService);
        redisService.redisTemplate = redisTemplate;
        mockMvc = buildMockMvc(controller);
        // 默认保持未登录状态，只有需要登录态的 resolve/confirm 用例显式 setLoginUser。
        clearLoginUser();
    }

    @AfterEach
    void tearDown()
    {
        // 避免 SecurityContextHolder 的线程上下文泄漏到下一条用例。
        clearLoginUser();
    }

    @Test
    void createGeneratesSessionAndCachesSessionAndCode() throws Exception
    {
        /*
         * create 会生成 sid/loginCode，并写入两个 Redis key：
         * bigyun:scan:session:{sid} 保存完整会话；
         * bigyun:scan:code:{loginCode} 反查 sid。
         */
        when(redisService.hasKey(any())).thenReturn(false);

        mockMvc.perform(post("/scan/create"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.status").value(ScanLoginConstants.STATUS_CREATED))
                .andExpect(jsonPath("$.data.sid").isString())
                .andExpect(jsonPath("$.data.loginCode").isString());

        // 捕获写入 Redis 的 session 对象，确认初始状态和关键字段都已生成。
        ArgumentCaptor<ScanSessionCache> sessionCaptor = ArgumentCaptor.forClass(ScanSessionCache.class);
        verify(redisService).setCacheObject(
                argThat(key -> key.startsWith(ScanLoginConstants.SCAN_SESSION_KEY_PREFIX)),
                sessionCaptor.capture(),
                eq(ScanLoginConstants.SESSION_TTL_SECONDS),
                eq(TimeUnit.SECONDS)
        );
        ScanSessionCache cachedSession = sessionCaptor.getValue();
        assertEquals(ScanLoginConstants.STATUS_CREATED, cachedSession.getStatus());
        assertNotNull(cachedSession.getSid());
        assertNotNull(cachedSession.getLoginCode());
        verify(redisService, atLeastOnce()).setCacheObject(any(String.class), any(), any(Long.class), eq(TimeUnit.SECONDS));
    }

    @Test
    void statusRejectsBlankSid() throws Exception
    {
        // GET 参数校验模板：通过 .param 传参，缺失或空值走统一失败响应。
        mockMvc.perform(get("/scan/status").param("sid", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(FAIL))
                .andExpect(jsonPath("$.msg").isString());
    }

    @Test
    void statusReturnsCurrentSession() throws Exception
    {
        /*
         * status 只读取 session key，不要求登录。
         * 这里同时 mock getExpire，确保响应中的 expiresIn 来自 Redis 剩余 TTL。
         */
        ScanSessionCache session = new ScanSessionCache("sid-1", "123456", ScanLoginConstants.STATUS_CREATED, 1L, 2L, null, null, null);
        when(redisService.getCacheObject("bigyun:scan:session:sid-1")).thenReturn(session);
        when(redisService.getExpire("bigyun:scan:session:sid-1")).thenReturn(120L);

        mockMvc.perform(get("/scan/status").param("sid", "sid-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.sid").value("sid-1"))
                .andExpect(jsonPath("$.data.status").value(ScanLoginConstants.STATUS_CREATED))
                .andExpect(jsonPath("$.data.expiresIn").value(120));
    }

    @Test
    void resolveRejectsUnauthenticatedUser() throws Exception
    {
        /*
         * resolve 必须由已登录用户扫码触发。
         * 不调用 setLoginUser 时，SecurityUtils 读取不到当前用户，应走失败响应。
         */
        mockMvc.perform(post("/scan/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginCode":"123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(FAIL))
                .andExpect(jsonPath("$.msg").isString());
    }

    @Test
    void resolveMarksCreatedSessionAsResolved() throws Exception
    {
        /*
         * BigYun 登录态从 SecurityContextHolder 读取。
         * Controller 测试中用 setLoginUser 写入上下文，比 mock 静态工具类更贴近生产链路。
         */
        setLoginUser(1001L, "demoUser");
        ScanSessionCache session = new ScanSessionCache("sid-1", "123456", ScanLoginConstants.STATUS_CREATED, 1L, 2L, null, null, null);
        when(redisService.getCacheObject("bigyun:scan:code:123456")).thenReturn("sid-1");
        when(redisService.getCacheObject("bigyun:scan:session:sid-1")).thenReturn(session);
        when(redisService.getExpire("bigyun:scan:session:sid-1")).thenReturn(180L);

        mockMvc.perform(post("/scan/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "loginCode":"123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.status").value(ScanLoginConstants.STATUS_RESOLVED));

        // resolve 会把 CREATED 会话推进到 RESOLVED，并按原 TTL 回写 session key。
        assertEquals(ScanLoginConstants.STATUS_RESOLVED, session.getStatus());
        verify(redisService).setCacheObject(
                eq("bigyun:scan:session:sid-1"),
                eq(session),
                eq(180L),
                eq(TimeUnit.SECONDS)
        );
    }

    @Test
    void confirmGeneratesGrantCodeAndClearsCodeKey() throws Exception
    {
        /*
         * confirm 是扫码登录的最终确认步骤：
         * 1. 已登录用户确认；
         * 2. 用 bigyun:scan:lock:{sid} 做短锁防重复确认；
         * 3. 生成 grantCode，写入 bigyun:scan:grant:{grantCode}；
         * 4. 删除 loginCode 和 lock key。
         */
        setLoginUser(2002L, "demoUser");
        ScanSessionCache session = new ScanSessionCache("sid-1", "123456", ScanLoginConstants.STATUS_RESOLVED, 1L, 2L, null, null, null);
        redisService.redisTemplate = redisTemplate;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisService.getCacheObject("bigyun:scan:code:123456")).thenReturn("sid-1");
        when(redisService.getCacheObject("bigyun:scan:session:sid-1")).thenReturn(session);
        when(redisService.getExpire("bigyun:scan:session:sid-1")).thenReturn(200L);
        when(valueOperations.setIfAbsent(eq("bigyun:scan:lock:sid-1"), eq("1"), eq(ScanLoginConstants.LOCK_TTL_SECONDS), eq(TimeUnit.SECONDS)))
                .thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/scan/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sid":"sid-1",
                                  "loginCode":"123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(SUCCESS))
                .andExpect(jsonPath("$.data.sid").value("sid-1"))
                .andExpect(jsonPath("$.data.status").value(ScanLoginConstants.STATUS_CONFIRMED))
                .andExpect(jsonPath("$.data.grantCode").isString());

        // grantCode 的 Redis 载荷必须带上当前登录用户，loginNew(type=4) 后续会消费这个一次性凭证。
        ArgumentCaptor<ScanGrantPayload> grantCaptor = ArgumentCaptor.forClass(ScanGrantPayload.class);
        verify(redisService).setCacheObject(
                eq("bigyun:scan:grant:" + session.getGrantCode()),
                grantCaptor.capture(),
                eq(ScanLoginConstants.GRANT_TTL_SECONDS),
                eq(TimeUnit.SECONDS)
        );
        assertEquals(2002L, grantCaptor.getValue().getUserId());
        verify(redisService).deleteObject("bigyun:scan:code:123456");
        verify(redisService).deleteObject("bigyun:scan:lock:sid-1");
    }
}
