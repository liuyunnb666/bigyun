package com.bigyun.auth.service.strategy;

import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;

/**
 * 登录策略接口
 * 定义统一的登录行为，支持多种登录方式（用户名、手机号、邮箱等）
 *
 * 实现类需要：
 * 1. 使用@Component注解标注为Spring组件
 * 2. 实现getType()方法返回唯一的登录类型标识
 * 3. 实现login()方法处理具体的登录逻辑
 */
public interface LoginStrategy
{
    /**
     * 获取登录类型标识
     * @return 登录类型代码 (1-用户名, 2-手机号, 3-邮箱)
     */
    String getType();

    /**
     * 执行登录操作
     * @param req 登录请求参数
     * @return 登录用户信息
     */
    LoginUser login(LoginReq req);
}
