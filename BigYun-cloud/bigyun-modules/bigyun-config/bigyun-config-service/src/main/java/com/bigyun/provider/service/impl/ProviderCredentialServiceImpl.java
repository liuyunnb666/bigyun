package com.bigyun.provider.service.impl;

import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.provider.domain.ProviderCredential;
import com.bigyun.provider.mapper.ProviderCredentialMapper;
import com.bigyun.provider.service.IProviderCredentialService;
import com.bigyun.provider.util.ProviderSecretUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provider 凭据服务实现。
 * <p>
 * 负责按配置保存和读取扩展凭据，数据库中保存密文，返回给运行时前统一解密。
 */
@Service
public class ProviderCredentialServiceImpl implements IProviderCredentialService
{
    @Autowired
    private ProviderCredentialMapper credentialMapper;

    @Autowired
    private ProviderSecretUtils providerSecretUtils;

    /**
     * 查询指定 Provider 配置下的全部凭据并转换为明文 Map。
     * <p>
     * 解密失败的单个凭据会被跳过，避免坏数据影响其他凭据读取。
     *
     * @param configId Provider 配置主键
     * @return 凭据键值表
     */
    @Override
    public Map<String, String> getCredentialsMap(Long configId)
    {
        List<ProviderCredential> credentials = credentialMapper.selectByConfigId(configId);
        Map<String, String> result = new HashMap<>();

        for (ProviderCredential credential : credentials)
        {
            String decryptedValue = decryptValue(credential.getCredentialValue());
            if (decryptedValue != null)
            {
                result.put(credential.getCredentialKey(), decryptedValue);
            }
        }

        return result;
    }

    /**
     * 查询指定配置下的单个凭据明文值。
     *
     * @param configId Provider 配置主键
     * @param key 凭据键
     * @return 解密后的凭据值；不存在或解密失败时返回 null
     */
    @Override
    public String getCredential(Long configId, String key)
    {
        ProviderCredential credential = credentialMapper.selectByConfigIdAndKey(configId, key);
        if (credential == null)
        {
            return null;
        }
        return decryptValue(credential.getCredentialValue());
    }

    /**
     * 批量保存 Provider 凭据。
     * <p>
     * 每个键值会复用单条保存逻辑，已存在则更新，不存在则新增。
     *
     * @param configId Provider 配置主键
     * @param credentials 凭据键值表
     * @return 累计写入行数
     */
    @Override
    @Transactional
    public int saveCredentials(Long configId, Map<String, String> credentials)
    {
        if (credentials == null || credentials.isEmpty())
        {
            return 0;
        }

        int count = 0;
        for (Map.Entry<String, String> entry : credentials.entrySet())
        {
            count += saveCredential(configId, entry.getKey(), entry.getValue());
        }
        return count;
    }

    /**
     * 保存单个 Provider 凭据。
     * <p>
     * 入库前会加密凭据值，并统一标记为敏感字段。
     *
     * @param configId Provider 配置主键
     * @param key 凭据键
     * @param value 凭据明文值
     * @return 受影响行数
     */
    @Override
    @Transactional
    public int saveCredential(Long configId, String key, String value)
    {
        if (StringUtils.isBlank(key))
        {
            return 0;
        }

        // Check if credential exists
        ProviderCredential existing = credentialMapper.selectByConfigIdAndKey(configId, key);

        ProviderCredential credential = new ProviderCredential();
        credential.setConfigId(configId);
        credential.setCredentialKey(key);
        credential.setCredentialValue(encryptValue(value));
        credential.setIsSensitive("Y");

        if (existing != null)
        {
            // Update existing credential
            credential.setCredentialId(existing.getCredentialId());
            return credentialMapper.updateById(credential);
        }
        else
        {
            // Insert new credential
            return credentialMapper.insert(credential);
        }
    }

    /**
     * 删除指定配置下的全部凭据。
     *
     * @param configId Provider 配置主键
     * @return 删除行数
     */
    @Override
    @Transactional
    public int deleteCredentials(Long configId)
    {
        return credentialMapper.deleteByConfigId(configId);
    }

    /**
     * 删除指定配置下的单个凭据。
     *
     * @param configId Provider 配置主键
     * @param key 凭据键
     * @return 删除行数
     */
    @Override
    @Transactional
    public int deleteCredential(Long configId, String key)
    {
        return credentialMapper.deleteByConfigIdAndKey(configId, key);
    }

    /**
     * 查询指定配置下的原始凭据实体列表。
     * <p>
     * 该方法保留实体返回形态，调用方需要自行避免输出密文字段。
     *
     * @param configId Provider 配置主键
     * @return 凭据实体列表
     */
    @Override
    public List<ProviderCredential> selectByConfigId(Long configId)
    {
        return credentialMapper.selectByConfigId(configId);
    }

    /**
     * 加密凭据值。
     *
     * @param plainText 凭据明文
     * @return 加密后的密文；空值返回 null
     */
    private String encryptValue(String plainText)
    {
        if (StringUtils.isBlank(plainText))
        {
            return null;
        }
        return providerSecretUtils.encrypt(plainText);
    }

    /**
     * 解密凭据值。
     *
     * @param cipherText 凭据密文
     * @return 解密后的明文；空值或解密失败时返回 null
     */
    private String decryptValue(String cipherText)
    {
        if (StringUtils.isBlank(cipherText))
        {
            return null;
        }
        try
        {
            return providerSecretUtils.decrypt(cipherText);
        }
        catch (Exception e)
        {
            // If decryption fails, return null instead of throwing exception
            return null;
        }
    }
}
