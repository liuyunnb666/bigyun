package com.bigyun.system.service;

import com.bigyun.system.domain.SysUserFace;

/**
 * 用户人脸识别凭据 业务层
 */
public interface ISysUserFaceService
{
    SysUserFace selectUserFaceByUserId(Long userId);

    boolean saveUserFace(SysUserFace userFace);

    boolean deleteUserFaceByUserId(Long userId);

    boolean updateLastLoginTime(Long userId);
}
