package com.bigyun.system.controller;

import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.utils.DateUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.file.FileTypeUtils;
import com.bigyun.common.core.utils.file.MimeTypeUtils;
import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.service.TokenService;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.file.domain.SysFile;
import com.bigyun.file.remote.RemoteFileService;
import com.bigyun.system.domain.SysUser;
import com.bigyun.system.domain.model.LoginUser;
import com.bigyun.system.service.ISysUserService;
import java.util.Arrays;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人资料接口。
 *
 * <p>社区版保留系统用户头像上传和预设头像能力，不绑定任何私有端侧业务。</p>
 */
@RestController
@RequestMapping("/user/profile")
public class SysProfileController extends BaseController
{
    private static final String PRESET_AVATAR_PREFIX = "/static/avatar-presets/";

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private RemoteFileService remoteFileService;

    @GetMapping
    public AjaxResult profile()
    {
        String username = SecurityUtils.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(username));
        ajax.put("postGroup", userService.selectUserPostGroup(username));
        return ajax;
    }

    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser currentUser = loginUser.getSysUser();
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + loginUser.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser))
        {
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long userId = loginUser.getUserid();
        String password = loginUser.getSysUser().getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = SecurityUtils.encryptPassword(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            loginUser.getSysUser().setPwdUpdateDate(DateUtils.getNowDate());
            loginUser.getSysUser().setPassword(newPassword);
            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return error("上传图片异常，请联系管理员");
        }

        LoginUser loginUser = SecurityUtils.getLoginUser();
        String extension = FileTypeUtils.getExtension(file);
        if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION))
        {
            return error("文件格式不正确，请上传 " + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + " 格式");
        }

        R<SysFile> fileResult = remoteFileService.upload(file);
        if (StringUtils.isNull(fileResult) || StringUtils.isNull(fileResult.getData()))
        {
            return error("文件服务异常，请联系管理员");
        }

        String url = fileResult.getData().getUrl();
        if (!userService.updateUserAvatar(loginUser.getUserid(), url))
        {
            return error("上传图片异常，请联系管理员");
        }

        String oldAvatarUrl = loginUser.getSysUser().getAvatar();
        if (StringUtils.isNotEmpty(oldAvatarUrl) && !isPresetAvatar(oldAvatarUrl))
        {
            remoteFileService.delete(oldAvatarUrl);
        }
        loginUser.getSysUser().setAvatar(url);
        tokenService.setLoginUser(loginUser);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("imgUrl", url);
        return ajax;
    }

    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AjaxResult updateAvatar(@RequestBody Map<String, String> params)
    {
        return updateAvatarValue(params);
    }

    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PutMapping("/avatar")
    public AjaxResult updatePresetAvatar(@RequestBody Map<String, String> params)
    {
        return updateAvatarValue(params);
    }

    private AjaxResult updateAvatarValue(Map<String, String> params)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String nextAvatar = params == null ? null : StringUtils.trimToEmpty(params.get("avatar"));
        if (StringUtils.isNotEmpty(nextAvatar) && !isPresetAvatar(nextAvatar))
        {
            return error("只允许选择系统预设头像");
        }
        String oldAvatarUrl = loginUser.getSysUser().getAvatar();
        if (!userService.updateUserAvatar(loginUser.getUserid(), nextAvatar))
        {
            return error("修改头像异常，请联系管理员");
        }
        if (StringUtils.isNotEmpty(oldAvatarUrl) && !isPresetAvatar(oldAvatarUrl)
                && !StringUtils.equals(oldAvatarUrl, nextAvatar))
        {
            remoteFileService.delete(oldAvatarUrl);
        }
        loginUser.getSysUser().setAvatar(nextAvatar);
        tokenService.setLoginUser(loginUser);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("imgUrl", nextAvatar);
        return ajax;
    }

    private boolean isPresetAvatar(String avatar)
    {
        return StringUtils.isNotEmpty(avatar) && StringUtils.startsWith(avatar, PRESET_AVATAR_PREFIX);
    }
}
