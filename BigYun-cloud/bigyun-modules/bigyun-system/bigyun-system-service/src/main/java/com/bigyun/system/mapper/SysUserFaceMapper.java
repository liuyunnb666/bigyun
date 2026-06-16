package com.bigyun.system.mapper;

import com.bigyun.system.domain.SysUserFace;

/**
 * 用户人脸识别凭据 数据层
 */
public interface SysUserFaceMapper
{
    SysUserFace selectUserFaceByUserId(Long userId);

    int insertUserFace(SysUserFace userFace);

    int updateUserFace(SysUserFace userFace);

    int deleteUserFaceByUserId(Long userId);

    int updateLastLoginTime(Long userId);
}
