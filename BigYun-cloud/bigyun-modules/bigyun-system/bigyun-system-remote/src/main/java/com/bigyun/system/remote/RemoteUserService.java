package com.bigyun.system.remote;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.constant.ServiceNameConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.system.domain.SysUser;
import com.bigyun.system.domain.SysUserFace;
import com.bigyun.system.domain.form.RemoteLogoutRequest;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.factory.RemoteUserFallbackFactory;

@FeignClient(contextId = "remoteUserService", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserService
{
    @GetMapping("/user/info/{username}")
    R<LoginUser> getUserInfo(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/user/info/phone/{phonenumber}")
    R<LoginUser> getUserInfoByPhone(@PathVariable("phonenumber") String phonenumber,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/user/info/email/{email}")
    R<LoginUser> getUserInfoByEmail(@PathVariable("email") String email,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/user/info/id/{userId}")
    R<LoginUser> getUserInfoById(@PathVariable("userId") Long userId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/user/register")
    R<Boolean> registerUserInfo(@RequestBody SysUser sysUser,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/user/inner/create")
    R<SysUser> createInnerUser(@RequestBody SysUser sysUser,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PutMapping("/user/inner/phone")
    R<Boolean> updateInnerUserPhone(@RequestBody SysUser sysUser,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PutMapping("/user/recordlogin")
    R<Boolean> recordUserLogin(@RequestBody SysUser sysUser,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/user/inner/logout")
    R<Boolean> logoutByToken(@RequestBody RemoteLogoutRequest request,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @GetMapping("/user/inner/face/{userId}")
    R<SysUserFace> getUserFaceByUserId(@PathVariable("userId") Long userId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PostMapping("/user/inner/face")
    R<Boolean> saveUserFace(@RequestBody SysUserFace userFace,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @DeleteMapping("/user/inner/face/{userId}")
    R<Boolean> deleteUserFaceByUserId(@PathVariable("userId") Long userId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    @PutMapping("/user/inner/face/login/{userId}")
    R<Boolean> updateUserFaceLoginTime(@PathVariable("userId") Long userId,
            @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
