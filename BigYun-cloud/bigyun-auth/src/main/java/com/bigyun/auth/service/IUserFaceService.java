package com.bigyun.auth.service;

import com.bigyun.auth.domain.FaceStatusResponse;
import com.bigyun.system.domain.SysUserFace;

public interface IUserFaceService
{
    String getFaceTokenByUserId(Long userId);

    SysUserFace getUserFaceByUserId(Long userId);

    void saveFaceToken(Long userId, String faceToken);

    void saveFaceToken(Long userId, String faceToken, String livenessMode, String livenessBizId, String livenessStatus);

    void saveFaceCredential(Long userId, String providerCode, String faceToken, String livenessMode,
            String livenessBizId, String livenessStatus, String remark);

    FaceStatusResponse getFaceStatus(Long userId);

    void deleteFaceToken(Long userId);

    void recordLoginSuccess(Long userId);

    void checkLocked(Long userId);

    void recordFailure(Long userId);

    void clearFailure(Long userId);
}
