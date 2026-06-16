package com.bigyun.system.service.impl;

import com.bigyun.common.core.utils.DateUtils;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.system.domain.SysUserFace;
import com.bigyun.system.mapper.SysUserFaceMapper;
import com.bigyun.system.service.ISysUserFaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户人脸识别凭据业务处理。
 */
@Service
public class SysUserFaceServiceImpl implements ISysUserFaceService
{
    private static final String DEFAULT_PROVIDER_CODE = "faceplus";

    private static final String NORMAL_STATUS = "0";

    @Autowired
    private SysUserFaceMapper userFaceMapper;

    @Override
    public SysUserFace selectUserFaceByUserId(Long userId)
    {
        if (userId == null)
        {
            return null;
        }
        return userFaceMapper.selectUserFaceByUserId(userId);
    }

    @Override
    public boolean saveUserFace(SysUserFace userFace)
    {
        if (userFace == null || userFace.getUserId() == null || StringUtils.isBlank(userFace.getFaceToken()))
        {
            return false;
        }

        userFace.setProviderCode(StringUtils.defaultIfBlank(userFace.getProviderCode(), DEFAULT_PROVIDER_CODE));
        userFace.setStatus(StringUtils.defaultIfBlank(userFace.getStatus(), NORMAL_STATUS));
        userFace.setLastEnrollTime(DateUtils.getNowDate());
        if (StringUtils.isNotBlank(userFace.getLivenessMode()))
        {
            userFace.setLastLivenessTime(DateUtils.getNowDate());
        }
        userFace.setUpdateTime(DateUtils.getNowDate());

        SysUserFace exists = userFaceMapper.selectUserFaceByUserId(userFace.getUserId());
        if (exists == null)
        {
            userFace.setCreateTime(DateUtils.getNowDate());
            return userFaceMapper.insertUserFace(userFace) > 0;
        }
        userFace.setFaceId(exists.getFaceId());
        return userFaceMapper.updateUserFace(userFace) > 0;
    }

    @Override
    public boolean deleteUserFaceByUserId(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return userFaceMapper.deleteUserFaceByUserId(userId) > 0;
    }

    @Override
    public boolean updateLastLoginTime(Long userId)
    {
        if (userId == null)
        {
            return false;
        }
        return userFaceMapper.updateLastLoginTime(userId) > 0;
    }
}
