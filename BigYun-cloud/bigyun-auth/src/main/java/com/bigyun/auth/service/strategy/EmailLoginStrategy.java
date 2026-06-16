package com.bigyun.auth.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.bigyun.auth.service.SysLoginService;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.enums.LoginTypeEnum;

/**
 * 邮箱验证码登录策略
 */
@Component
public class EmailLoginStrategy implements LoginStrategy
{
    @Autowired
    private SysLoginService sysLoginService;

    @Override
    public String getType()
    {
        return LoginTypeEnum.EMAIL.getCode();
    }

    @Override
    public LoginUser login(LoginReq req)
    {
        // 参数校验
        if (StringUtils.isEmpty(req.getEmail()))
        {
            throw new ServiceException("邮箱不能为空");
        }
        if (StringUtils.isEmpty(req.getCode()))
        {
            throw new ServiceException("验证码不能为空");
        }
        if (StringUtils.isEmpty(req.getUuid()))
        {
            throw new ServiceException("验证码会话ID不能为空");
        }

        return sysLoginService.loginByEmail(req.getEmail(), req.getCode(), req.getUuid());
    }
}
