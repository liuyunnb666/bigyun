package com.bigyun.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.stereotype.Component;
import com.bigyun.auth.constant.LoginCodeConstants;
import com.bigyun.common.core.constant.CacheConstants;
import com.bigyun.common.core.constant.Constants;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.enums.UserStatus;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.text.Convert;
import com.bigyun.common.core.utils.DateUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.ip.IpUtils;
import com.bigyun.common.redis.service.RedisService;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.system.domain.SysUser;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.remote.RemoteUserService;

@Component
public class SysLoginService
{
    private static final Logger LOG = LoggerFactory.getLogger(SysLoginService.class);

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysRecordLogService recordLogService;

    @Autowired
    private RedisService redisService;

    public LoginUser login(String username, String password)
    {
        validateUsernamePasswordParam(username, password);
        checkLoginIpBlacklist(username);
        LoginUser userInfo = checkLoginUser(username,
                remoteUserService.getUserInfo(username, SecurityConstants.INNER));
        passwordService.validate(userInfo.getSysUser(), password);
        recordLoginSuccess(userInfo);
        return userInfo;
    }

    public void validateImageCaptcha(String username, String code, String uuid)
    {
        validateLoginCode(CacheConstants.CAPTCHA_CODE_KEY, username, uuid, code);
    }

    public LoginUser loginByPhone(String phone, String code, String uuid)
    {
        validatePhoneLoginParam(phone, code, uuid);
        checkLoginIpBlacklist(phone);
        validateLoginCode(LoginCodeConstants.PHONE_CODE_PREFIX, phone, uuid, code);
        LoginUser userInfo = checkLoginUser(phone,
                remoteUserService.getUserInfoByPhone(phone, SecurityConstants.INNER));
        recordLoginSuccess(userInfo);
        return userInfo;
    }

    public LoginUser loginByEmail(String email, String code, String uuid)
    {
        validateEmailLoginParam(email, code, uuid);
        checkLoginIpBlacklist(email);
        validateLoginCode(LoginCodeConstants.EMAIL_CODE_PREFIX, email, uuid, code);
        LoginUser userInfo = checkLoginUser(email,
                remoteUserService.getUserInfoByEmail(email, SecurityConstants.INNER));
        recordLoginSuccess(userInfo);
        return userInfo;
    }

    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        try
        {
            remoteUserService.recordUserLogin(sysUser, SecurityConstants.INNER);
        }
        catch (Exception e)
        {
            LOG.warn("record user login info failed, userId={}", userId, e);
        }
    }

    public void logout(String loginName)
    {
        recordLogService.recordLogininfor(loginName, Constants.LOGOUT, "logout success");
    }

    public void unlock(String password)
    {
        String username = SecurityUtils.getUsername();
        if (StringUtils.isEmpty(password))
        {
            throw new ServiceException("password must not be empty");
        }
        R<LoginUser> userResult = remoteUserService.getUserInfo(username, SecurityConstants.INNER);
        if (R.FAIL == userResult.getCode())
        {
            throw new ServiceException(userResult.getMsg());
        }
        SysUser user = userResult.getData().getSysUser();
        if (!SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            throw new ServiceException("password invalid");
        }
    }

    public void register(String username, String password)
    {
        if (StringUtils.isAnyBlank(username, password))
        {
            throw new ServiceException("username/password required");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            throw new ServiceException("username length invalid");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            throw new ServiceException("password length invalid");
        }

        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setNickName(username);
        sysUser.setPwdUpdateDate(DateUtils.getNowDate());
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        R<?> registerResult = remoteUserService.registerUserInfo(sysUser, SecurityConstants.INNER);
        if (R.FAIL == registerResult.getCode())
        {
            throw new ServiceException(registerResult.getMsg());
        }
        recordLogService.recordLogininfor(username, Constants.REGISTER, "register success");
    }

    private void validateUsernamePasswordParam(String username, String password)
    {
        if (StringUtils.isAnyBlank(username, password))
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "username/password required");
            throw new ServiceException("username/password required");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "password length invalid");
            throw new ServiceException("password length invalid");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            recordLogService.recordLogininfor(username, Constants.LOGIN_FAIL, "username length invalid");
            throw new ServiceException("username length invalid");
        }
    }

    private void validatePhoneLoginParam(String phone, String code, String uuid)
    {
        if (StringUtils.isAnyBlank(phone, code, uuid))
        {
            recordLogService.recordLogininfor(phone, Constants.LOGIN_FAIL, "phone/code/uuid required");
            throw new ServiceException("phone/code/uuid required");
        }
    }

    private void validateEmailLoginParam(String email, String code, String uuid)
    {
        if (StringUtils.isAnyBlank(email, code, uuid))
        {
            recordLogService.recordLogininfor(email, Constants.LOGIN_FAIL, "email/code/uuid required");
            throw new ServiceException("email/code/uuid required");
        }
    }

    private void checkLoginIpBlacklist(String account)
    {
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            recordLogService.recordLogininfor(account, Constants.LOGIN_FAIL, "ip blocked");
            throw new ServiceException("ip blocked");
        }
    }

    private LoginUser checkLoginUser(String account, R<LoginUser> userResult)
    {
        if (R.FAIL == userResult.getCode() || StringUtils.isNull(userResult.getData())
                || StringUtils.isNull(userResult.getData().getSysUser()))
        {
            String message = StringUtils.isEmpty(userResult.getMsg()) ? "user not found" : userResult.getMsg();
            recordLogService.recordLogininfor(account, Constants.LOGIN_FAIL, message);
            throw new ServiceException(message);
        }

        LoginUser userInfo = userResult.getData();
        SysUser user = userInfo.getSysUser();
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag()))
        {
            recordLogService.recordLogininfor(user.getUserName(), Constants.LOGIN_FAIL, "account deleted");
            throw new ServiceException("account deleted");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus()))
        {
            recordLogService.recordLogininfor(user.getUserName(), Constants.LOGIN_FAIL, "account disabled");
            throw new ServiceException("account disabled");
        }
        return userInfo;
    }

    private void recordLoginSuccess(LoginUser userInfo)
    {
        SysUser user = userInfo.getSysUser();
        recordLogService.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, "login success");
        recordLoginInfo(user.getUserId());
    }

    private void validateLoginCode(String keyPrefix, String account, String uuid, String code)
    {
        String cacheKey = keyPrefix + uuid;
        String cacheCode = getLoginCode(cacheKey);
        if (StringUtils.isEmpty(cacheCode))
        {
            recordLogService.recordLogininfor(account, Constants.LOGIN_FAIL, "code expired");
            throw new ServiceException("code expired");
        }
        if (!StringUtils.equals(code, cacheCode))
        {
            recordLogService.recordLogininfor(account, Constants.LOGIN_FAIL, "code invalid");
            throw new ServiceException("code invalid");
        }
        redisService.deleteObject(cacheKey);
    }

    private String getLoginCode(String cacheKey)
    {
        try
        {
            String cacheCode = redisService.getCacheObject(cacheKey);
            if (StringUtils.isNotEmpty(cacheCode))
            {
                return cacheCode;
            }
            return redisService.getCacheMapValue(cacheKey, LoginCodeConstants.CODE_FIELD);
        }
        catch (RedisSystemException e)
        {
            if (!isWrongTypeException(e))
            {
                throw e;
            }
            return redisService.getCacheMapValue(cacheKey, LoginCodeConstants.CODE_FIELD);
        }
    }

    private boolean isWrongTypeException(RedisSystemException e)
    {
        Throwable cause = e;
        while (cause != null)
        {
            if (cause.getMessage() != null && cause.getMessage().contains("WRONGTYPE"))
            {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
