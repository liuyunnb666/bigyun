package com.bigyun.common.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.context.SecurityContextHolder;
import com.bigyun.common.core.utils.ServletUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.security.auth.AuthUtil;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.system.domain.model.LoginUser;

/**
 * 自定义请求头拦截器，将Header数据封装到线程变量中方便获取
 * 注意：此拦截器会同时验证当前用户有效期自动刷新有效期
 *
 * @author bigyun
 */
public class HeaderInterceptor implements AsyncHandlerInterceptor
{
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (!(handler instanceof HandlerMethod))
        {
            return true;
        }

        SecurityContextHolder.setUserId(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USER_ID));
        SecurityContextHolder.setUserName(ServletUtils.getHeader(request, SecurityConstants.DETAILS_USERNAME));
        SecurityContextHolder.setUserKey(ServletUtils.getHeader(request, SecurityConstants.USER_KEY));

        String token = SecurityUtils.getToken();
        if (StringUtils.isNotEmpty(token))
        {
            LoginUser loginUser = AuthUtil.getLoginUser(token);
            if (StringUtils.isNotNull(loginUser))
            {
                AuthUtil.verifyLoginUserExpire(loginUser);
                SecurityContextHolder.set(SecurityConstants.LOGIN_USER, loginUser);
                fillUserContextFromLoginUser(loginUser);
            }
        }
        return true;
    }

    /**
     * 网关正常会把 user_id、username 写入请求头；但 H5、本地调试或直连服务时，
     * 可能只有 Authorization token，没有网关注入的用户头。既然 token 已经校验通过，
     * 这里从 LoginUser 兜底回填线程上下文，避免业务接口误判“未登录”。
     */
    private void fillUserContextFromLoginUser(LoginUser loginUser)
    {
        if (SecurityUtils.getUserId() == null)
        {
            Long userId = loginUser.getUserid();
            if (userId == null && loginUser.getSysUser() != null)
            {
                userId = loginUser.getSysUser().getUserId();
            }
            if (userId != null)
            {
                SecurityContextHolder.setUserId(String.valueOf(userId));
            }
        }

        if (StringUtils.isEmpty(SecurityUtils.getUsername()))
        {
            String username = loginUser.getUsername();
            if (StringUtils.isEmpty(username) && loginUser.getSysUser() != null)
            {
                username = loginUser.getSysUser().getUserName();
            }
            if (StringUtils.isNotEmpty(username))
            {
                SecurityContextHolder.setUserName(username);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception
    {
        SecurityContextHolder.remove();
    }
}
