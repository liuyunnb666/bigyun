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
 * 用户名密码登录策略
 */
@Component
public class UsernameLoginStrategy implements LoginStrategy
{
    @Autowired
    private SysLoginService sysLoginService;

    @Override
    public String getType()
    {
        return LoginTypeEnum.USERNAME.getCode();
    }

    @Override
    public LoginUser login(LoginReq req)
    {
        // 参数校验
        if (StringUtils.isEmpty(req.getUserName()))
        {
            throw new ServiceException("用户名不能为空");
        }
        if (StringUtils.isEmpty(req.getPassword()))
        {
            throw new ServiceException("密码不能为空");
        }

        if (StringUtils.isAnyBlank(req.getCode(), req.getUuid()))
        {
            throw new ServiceException("图形验证码不能为空");
        }
        sysLoginService.validateImageCaptcha(req.getUserName(), req.getCode(), req.getUuid());
        return sysLoginService.login(req.getUserName(), req.getPassword());
    }
}
