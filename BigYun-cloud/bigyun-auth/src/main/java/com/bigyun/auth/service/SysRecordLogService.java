package com.bigyun.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.bigyun.common.core.constant.Constants;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.ip.IpUtils;
import com.bigyun.system.remote.RemoteLogService;
import com.bigyun.system.domain.SysLogininfor;

/**
 * 记录日志方法
 * 
 * @author bigyun
 */
@Component
public class SysRecordLogService
{
    private static final Logger LOG = LoggerFactory.getLogger(SysRecordLogService.class);

    @Autowired
    private RemoteLogService remoteLogService;

    /**
     * 记录登录信息
     * 
     * @param username 用户名
     * @param status 状态
     * @param message 消息内容
     * @return
     */
    public void recordLogininfor(String username, String status, String message)
    {
        SysLogininfor logininfor = new SysLogininfor();
        logininfor.setUserName(username);
        logininfor.setIpaddr(IpUtils.getIpAddr());
        logininfor.setMsg(message);
        // 日志状态
        if (StringUtils.equalsAny(status, Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER))
        {
            logininfor.setStatus(Constants.LOGIN_SUCCESS_STATUS);
        }
        else if (Constants.LOGIN_FAIL.equals(status))
        {
            logininfor.setStatus(Constants.LOGIN_FAIL_STATUS);
        }
        try
        {
            remoteLogService.saveLogininfor(logininfor, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            LOG.warn("record logininfor failed, username={}, status={}", username, status, e);
        }
    }
}
