package com.bigyun.auth.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.bigyun.auth.constant.ScanLoginConstants;
import com.bigyun.auth.domain.ScanGrantPayload;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.system.domain.form.LoginReq;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.enums.LoginTypeEnum;
import com.bigyun.system.remote.RemoteUserService;

/**
 * Web 扫码登录策略。
 *
 * Web 登录页拿到 CONFIRMED 状态后，会携带一次性 grantCode 调用 loginNew(type=4)。
 * 本策略只负责消费 grantCode，不再接收或信任前端传入的 userId。
 */
@Component
public class ScanLoginStrategy implements LoginStrategy
{
    @Autowired
    private RedisService redisService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public String getType()
    {
        return LoginTypeEnum.SCAN.getCode();
    }

    @Override
    public LoginUser login(LoginReq req)
    {
        if (StringUtils.isEmpty(req.getGrantCode()))
        {
            throw new ServiceException("grantCode不能为空");
        }

        String grantKey = ScanLoginConstants.SCAN_GRANT_KEY_PREFIX + req.getGrantCode();
        ScanGrantPayload grantPayload = redisService.getCacheObject(grantKey);
        if (grantPayload == null)
        {
            throw new ServiceException("grantCode无效或已过期");
        }

        String sid = grantPayload.getSid();
        Long userId = grantPayload.getUserId();
        if (StringUtils.isEmpty(sid) || userId == null)
        {
            throw new ServiceException("grantCode数据异常");
        }

        if (StringUtils.isNotEmpty(req.getSid()) && !StringUtils.equals(req.getSid(), sid))
        {
            throw new ServiceException("sid与grantCode不匹配");
        }

        R<LoginUser> userResult = remoteUserService.getUserInfoById(userId, SecurityConstants.INNER);
        if (userResult == null || R.isError(userResult) || userResult.getData() == null
                || userResult.getData().getSysUser() == null)
        {
            throw new ServiceException(userResult == null ? "用户查询失败" : userResult.getMsg());
        }

        // grantCode 是一次性凭证，换取 token 后必须立即删除，避免重复登录。
        redisService.deleteObject(grantKey);
        return userResult.getData();
    }
}
