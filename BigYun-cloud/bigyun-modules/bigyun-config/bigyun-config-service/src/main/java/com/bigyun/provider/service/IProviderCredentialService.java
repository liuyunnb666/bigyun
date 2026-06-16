package com.bigyun.provider.service;

import com.bigyun.provider.domain.ProviderCredential;

import java.util.List;
import java.util.Map;

/**
 * Provider凭据服务接口
 */
public interface IProviderCredentialService
{
    /**
     * 获取配置的所有凭据（解密后）
     *
     * @param configId 配置ID
     * @return 凭据Map，key为凭据键名，value为解密后的凭据值
     */
    Map<String, String> getCredentialsMap(Long configId);

    /**
     * 获取单个凭据（解密后）
     *
     * @param configId 配置ID
     * @param key 凭据键名
     * @return 解密后的凭据值
     */
    String getCredential(Long configId, String key);

    /**
     * 批量保存凭据（加密存储）
     *
     * @param configId 配置ID
     * @param credentials 凭据Map，key为凭据键名，value为明文凭据值
     * @return 保存成功的数量
     */
    int saveCredentials(Long configId, Map<String, String> credentials);

    /**
     * 保存单个凭据（加密存储）
     *
     * @param configId 配置ID
     * @param key 凭据键名
     * @param value 明文凭据值
     * @return 保存成功返回1，否则返回0
     */
    int saveCredential(Long configId, String key, String value);

    /**
     * 删除配置的所有凭据
     *
     * @param configId 配置ID
     * @return 删除的数量
     */
    int deleteCredentials(Long configId);

    /**
     * 删除单个凭据
     *
     * @param configId 配置ID
     * @param key 凭据键名
     * @return 删除成功返回1，否则返回0
     */
    int deleteCredential(Long configId, String key);

    /**
     * 查询配置的所有凭据（原始加密数据）
     *
     * @param configId 配置ID
     * @return 凭据列表
     */
    List<ProviderCredential> selectByConfigId(Long configId);
}
