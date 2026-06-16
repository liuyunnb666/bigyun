package com.bigyun.system.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import com.bigyun.common.core.domain.R;
import com.bigyun.system.domain.SysUser;
import com.bigyun.system.domain.SysUserFace;
import com.bigyun.system.domain.form.RemoteLogoutRequest;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.remote.RemoteUserService;

@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteUserFallbackFactory.class);

    @Override
    public RemoteUserService create(Throwable throwable)
    {
        log.error("remote user service failed: {}", throwable.getMessage());
        return new RemoteUserService()
        {
            @Override
            public R<LoginUser> getUserInfo(String username, String source)
            {
                return R.fail("get user failed: " + throwable.getMessage());
            }

            @Override
            public R<LoginUser> getUserInfoByPhone(String phonenumber, String source)
            {
                return R.fail("get user failed: " + throwable.getMessage());
            }

            @Override
            public R<LoginUser> getUserInfoByEmail(String email, String source)
            {
                return R.fail("get user failed: " + throwable.getMessage());
            }

            @Override
            public R<LoginUser> getUserInfoById(Long userId, String source)
            {
                return R.fail("get user failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> registerUserInfo(SysUser sysUser, String source)
            {
                return R.fail("register user failed: " + throwable.getMessage());
            }

            @Override
            public R<SysUser> createInnerUser(SysUser sysUser, String source)
            {
                return R.fail("create user failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> updateInnerUserPhone(SysUser sysUser, String source)
            {
                return R.fail("update user phone failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> recordUserLogin(SysUser sysUser, String source)
            {
                return R.fail("record login failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> logoutByToken(RemoteLogoutRequest request, String source)
            {
                return R.fail("logout failed: " + throwable.getMessage());
            }

            @Override
            public R<SysUserFace> getUserFaceByUserId(Long userId, String source)
            {
                return R.fail("get user face failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> saveUserFace(SysUserFace userFace, String source)
            {
                return R.fail("save user face failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> deleteUserFaceByUserId(Long userId, String source)
            {
                return R.fail("delete user face failed: " + throwable.getMessage());
            }

            @Override
            public R<Boolean> updateUserFaceLoginTime(Long userId, String source)
            {
                return R.fail("update user face login time failed: " + throwable.getMessage());
            }
        };
    }
}
