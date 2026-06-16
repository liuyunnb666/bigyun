package com.bigyun.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.bigyun.auth.factory.LoginStrategyFactory;
import com.bigyun.auth.constant.FaceLoginConstants;
import com.bigyun.auth.form.LoginBody;
import com.bigyun.auth.form.RegisterBody;
import com.bigyun.auth.form.UnLockBody;
import com.bigyun.auth.service.AuthRateLimitService;
import com.bigyun.auth.service.SysLoginService;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.utils.JwtUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.auth.AuthUtil;
import com.bigyun.common.security.service.TokenService;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.enums.LoginTypeEnum;

/**
 * token 控制
 *
 * @author bigyun
 */
@RestController
public class TokenController
{
    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysLoginService sysLoginService;

    @Autowired
    private LoginStrategyFactory loginStrategyFactory;

    @Autowired
    private AuthRateLimitService authRateLimitService;

    /**
     * 用户名密码登录（兼容前端旧版 login() 调用）
     * 前端发送: { username, password, code, uuid }
     */
    @PostMapping("login")
    public R<?> login(@Valid @RequestBody LoginBody form)
    {
        authRateLimitService.checkLogin(form == null ? null : form.getUsername());
        LoginUser userInfo = sysLoginService.login(form.getUsername(), form.getPassword());
        return R.ok(tokenService.createToken(userInfo));
    }

    /**
     * 统一登录接口（支持多种登录方式）
     * 前端发送: { type: "1", userName: "admin", password: "xxx" }
     *
     * @param req 登录请求参数，包含登录类型和对应的登录凭证
     *            type: 登录类型 (1-用户名密码, 2-手机号验证码, 3-邮箱验证码)
     * @return 登录成功返回token信息
     */
    @PostMapping("loginNew")
    public R<?> loginNew(@Valid @RequestBody LoginReq req)
    {
        // 验证请求参数
        if (req == null)
        {
            return R.fail("登录请求参数不能为空");
        }
        if (StringUtils.isEmpty(req.getType()))
        {
            return R.fail("登录类型不能为空，支持的类型: 1-用户名密码, 2-手机号验证码, 3-邮箱验证码");
        }

        // 通过策略工厂获取对应的登录策略并执行登录
        checkLoginRateLimit(req);
        LoginUser userInfo = loginStrategyFactory.getStrategy(req.getType()).login(req);
        Map<String, Object> tokenData = tokenService.createToken(userInfo);
        if (LoginTypeEnum.FACE.getCode().equals(req.getType()) && Boolean.TRUE.equals(req.getTestImageFallback()))
        {
            tokenData.put("warning", "当前使用上传图片测试兜底，未走正式摄像头活体检测，仅用于本地联调");
            tokenData.put("livenessMode", FaceLoginConstants.LIVENESS_MODE_TEST_IMAGE);
        }
        if (LoginTypeEnum.FACE.getCode().equals(req.getType())
                && !Boolean.TRUE.equals(req.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_STATIC.equalsIgnoreCase(req.getFaceLoginMode()))
        {
            tokenData.put("livenessMode", FaceLoginConstants.LIVENESS_MODE_ALIYUN_STATIC);
        }
        if (LoginTypeEnum.FACE.getCode().equals(req.getType())
                && !Boolean.TRUE.equals(req.getTestImageFallback())
                && FaceLoginConstants.FACE_LOGIN_MODE_ALIYUN_VIDEO.equalsIgnoreCase(req.getFaceLoginMode()))
        {
            tokenData.put("livenessMode", FaceLoginConstants.LIVENESS_MODE_ALIYUN_VIDEO);
        }
        if (LoginTypeEnum.FACE.getCode().equals(req.getType())
                && FaceLoginConstants.FACE_LOGIN_MODE_IMAGE_COMPARE.equalsIgnoreCase(req.getFaceLoginMode()))
        {
            tokenData.put("livenessMode", FaceLoginConstants.LIVENESS_MODE_IMAGE_COMPARE);
        }
        return R.ok(tokenData);
    }

    private void checkLoginRateLimit(LoginReq req)
    {
        String account = resolveLoginAccount(req);
        if (LoginTypeEnum.FACE.getCode().equals(req.getType()))
        {
            authRateLimitService.checkFaceLogin(account, "login");
            return;
        }
        authRateLimitService.checkLogin(account);
    }

    private String resolveLoginAccount(LoginReq req)
    {
        if (req == null)
        {
            return null;
        }
        if (LoginTypeEnum.PHONE.getCode().equals(req.getType()))
        {
            return req.getPhone();
        }
        if (LoginTypeEnum.EMAIL.getCode().equals(req.getType()))
        {
            return req.getEmail();
        }
        if (LoginTypeEnum.SCAN.getCode().equals(req.getType()))
        {
            return StringUtils.defaultIfBlank(req.getGrantCode(), req.getSid());
        }
        if (LoginTypeEnum.FACE.getCode().equals(req.getType()))
        {
            if (req.getUserId() != null)
            {
                return String.valueOf(req.getUserId());
            }
            return firstNotBlank(req.getUserName(), req.getPhone(), req.getEmail(), req.getFaceLivenessSessionId());
        }
        return firstNotBlank(req.getUserName(), req.getPhone(), req.getEmail());
    }

    private String firstNotBlank(String... values)
    {
        if (values == null)
        {
            return null;
        }
        for (String value : values)
        {
            if (StringUtils.isNotBlank(value))
            {
                return value;
            }
        }
        return null;
    }

    @DeleteMapping("logout")
    public R<?> logout(HttpServletRequest request)
    {
        String token = SecurityUtils.getToken(request);
        if (StringUtils.isNotEmpty(token))
        {
            String username = JwtUtils.getUserName(token);
            AuthUtil.logoutByToken(token);
            sysLoginService.logout(username);
        }
        return R.ok();
    }

    @PostMapping("refresh")
    public R<?> refresh(HttpServletRequest request)
    {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser))
        {
            tokenService.refreshToken(loginUser);
            return R.ok();
        }
        return R.ok();
    }

    @PostMapping("register")
    public R<?> register(@Valid @RequestBody RegisterBody registerBody)
    {
        authRateLimitService.checkRegister(registerBody == null ? null : registerBody.getUsername());
        sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return R.ok();
    }

    @PostMapping("/unlockscreen")
    public R<?> unlockScreen(@Valid @RequestBody UnLockBody unLockBody)
    {
        sysLoginService.unlock(unLockBody.getPassword());
        return R.ok();
    }
}
