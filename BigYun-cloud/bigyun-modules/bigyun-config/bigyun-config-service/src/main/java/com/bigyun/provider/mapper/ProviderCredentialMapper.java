package com.bigyun.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.provider.domain.ProviderCredential;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProviderCredentialMapper extends BaseMapper<ProviderCredential>
{
    /**
     * 根据配置ID查询所有凭据
     *
     * @param configId 配置ID
     * @return 凭据列表
     */
    List<ProviderCredential> selectByConfigId(@Param("configId") Long configId);

    /**
     * 根据配置ID和凭据键名查询凭据
     *
     * @param configId 配置ID
     * @param credentialKey 凭据键名
     * @return 凭据信息
     */
    ProviderCredential selectByConfigIdAndKey(@Param("configId") Long configId, @Param("credentialKey") String credentialKey);

    /**
     * 根据配置ID删除所有凭据
     *
     * @param configId 配置ID
     * @return 删除数量
     */
    int deleteByConfigId(@Param("configId") Long configId);

    /**
     * 根据配置ID和凭据键名删除凭据
     *
     * @param configId 配置ID
     * @param credentialKey 凭据键名
     * @return 删除数量
     */
    int deleteByConfigIdAndKey(@Param("configId") Long configId, @Param("credentialKey") String credentialKey);
}
