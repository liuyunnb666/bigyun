package com.bigyun.file.service;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.file.MimeTypeUtils;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.reader.ConfigReader;
import com.bigyun.file.domain.SysFile;
import com.bigyun.file.service.storage.StorageClient;
import com.bigyun.file.utils.FileUploadUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * <ul>
 *   <li><b>动态存储后端切换</b>：通过 {@link ConfigReader} 读取 Provider 配置，运行时决定使用哪个存储后端</li>
 *   <li><b>多存储客户端注册</b>：启动时自动发现所有 {@link StorageClient} 实现并按 providerCode 注册</li>
 * </ul>
 * </p>
 *
 * @author bigyun
 */
@Service
public class DynamicSysFileServiceImpl implements ISysFileService
{
    /** 配置读取器，从数据库读取 Provider 配置并缓存 */
    private final ConfigReader configReader;

    /** 存储客户端注册表，key=providerCode（如 local/aliyun_oss/tencent_cos），value=对应的 StorageClient 实现 */
    private final Map<String, StorageClient> storageClients = new HashMap<>();

    @Value("${file.path:${java.io.tmpdir}/bigyun-files}")
    private String legacyLocalBasePath;

    /**
     * 构造函数注入，自动收集所有 StorageClient 实现。
     * <p>Spring 会自动注入所有实现了 StorageClient 接口的 Bean 列表。</p>
     *
     * @param configReader 配置读取器
     * @param clients 所有 StorageClient 实现（自动注入）
     */
    public DynamicSysFileServiceImpl(ConfigReader configReader, List<StorageClient> clients)
    {
        this.configReader = configReader;
        for (StorageClient client : clients)
        {
            this.storageClients.put(client.getProviderCode(), client);
        }
    }

    /**
     *
     * @param file 上传的 MultipartFile
     * @return 上传后的文件信息（含 URL）
     * @throws Exception 如果文件类型不允许或上传失败
     */
    @Override
    public SysFile uploadFile(MultipartFile file) throws Exception
    {
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        ProviderConfigDTO config = loadDefaultStorageConfig();
        StorageClient client = getClient(config.getProviderCode());
        return client.upload(file, config);
    }

    @Override
    public SysFile uploadFile(MultipartFile file, String providerCode) throws Exception
    {
        FileUploadUtils.assertAllowed(file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
        ProviderConfigDTO config = loadStorageConfig(providerCode);
        StorageClient client = getClient(config.getProviderCode());
        return client.upload(file, config);
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件 URL
     * @throws Exception 如果删除失败或找不到匹配的存储客户端
     */
    @Override
    public void deleteFile(String fileUrl) throws Exception
    {
        List<ProviderConfigDTO> configs = loadEnabledStorageConfigs();
        for (ProviderConfigDTO config : configs)
        {
            StorageClient client = storageClients.get(config.getProviderCode());
            if (client != null && client.canHandle(fileUrl, config))
            {
                client.delete(fileUrl, config);
                return;
            }
        }
        throw new ServiceException("No storage config can handle this file: " + fileUrl);
    }

    @Override
    public String getAccessibleUrl(String fileUrl) throws Exception
    {
        if (StringUtils.isBlank(fileUrl))
        {
            throw new ServiceException("File url must not be blank");
        }
        List<ProviderConfigDTO> configs = loadEnabledStorageConfigs();
        return resolveAccessibleUrl(fileUrl, configs);
    }

    @Override
    public Map<String, String> getAccessibleUrls(List<String> fileUrls) throws Exception
    {
        Map<String, String> result = new LinkedHashMap<>();
        if (fileUrls == null || fileUrls.isEmpty())
        {
            return result;
        }
        List<ProviderConfigDTO> configs = loadEnabledStorageConfigs();
        for (String fileUrl : fileUrls)
        {
            if (StringUtils.isBlank(fileUrl) || result.containsKey(fileUrl))
            {
                continue;
            }
            try
            {
                result.put(fileUrl, resolveAccessibleUrl(fileUrl, configs));
            }
            catch (Exception ignored)
            {
                // Missing entries are handled by the caller as per-file failures.
            }
        }
        return result;
    }

    private String resolveAccessibleUrl(String fileUrl, List<ProviderConfigDTO> configs) throws Exception
    {
        for (ProviderConfigDTO config : configs)
        {
            StorageClient client = storageClients.get(config.getProviderCode());
            if (client != null && client.canHandle(fileUrl, config))
            {
                return client.getAccessibleUrl(fileUrl, config);
            }
        }
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://"))
        {
            return fileUrl;
        }
        throw new ServiceException("No storage config can handle this file: " + fileUrl);
    }

    /**
     *
     * @param objectKey 文件对象的相对路径（如 upload/2024/01/abc.jpg）
     * @return Spring Resource 对象，指向实际文件
     * @throws Exception 如果配置不匹配、路径不安全或文件不存在
     */
    @Override
    public Resource loadPublicFile(String objectKey) throws Exception
    {
        String normalizedKey = normalizeObjectKey(objectKey);
        Resource resource = loadFromEnabledLocalStorage(normalizedKey);
        if (resource != null)
        {
            return resource;
        }

        resource = loadLocalResource(normalizedKey, legacyLocalBasePath);
        if (resource != null)
        {
            return resource;
        }

        throw new ServiceException("Local file does not exist: " + normalizedKey);
    }

    private Resource loadFromEnabledLocalStorage(String normalizedKey) throws Exception
    {
        for (ProviderConfigDTO config : loadEnabledStorageConfigs())
        {
            if (!ProviderConfigConstants.PROVIDER_LOCAL.equals(config.getProviderCode()))
            {
                continue;
            }
            Resource resource = loadLocalResource(normalizedKey, config.getBasePath());
            if (resource != null)
            {
                return resource;
            }
        }
        return null;
    }

    private Resource loadLocalResource(String normalizedKey, String basePathValue) throws Exception
    {
        if (StringUtils.isBlank(basePathValue))
        {
            return null;
        }
        Path basePath = Path.of(basePathValue).toAbsolutePath().normalize();
        Path filePath = basePath.resolve(normalizedKey.replace("/", java.io.File.separator)).normalize();
        if (!filePath.startsWith(basePath))
        {
            throw new ServiceException("Invalid file path");
        }
        if (!Files.isRegularFile(filePath))
        {
            return null;
        }
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable())
        {
            return null;
        }
        return resource;
    }

    /**
     *
     * @return 存储类型的默认 Provider 配置 DTO
     */
    private ProviderConfigDTO loadDefaultStorageConfig()
    {
        return configReader.getDefaultConfig(ProviderConfigConstants.CONFIG_TYPE_STORAGE);
    }

    /**
     *
     * @return 已启用的存储配置 DTO 列表
     */
    private List<ProviderConfigDTO> loadEnabledStorageConfigs()
    {
        return configReader.listEnabledConfigs(ProviderConfigConstants.CONFIG_TYPE_STORAGE);
    }

    private ProviderConfigDTO loadStorageConfig(String providerCode)
    {
        if (StringUtils.isBlank(providerCode))
        {
            throw new ServiceException("Storage providerCode must not be blank");
        }
        return loadEnabledStorageConfigs().stream()
                .filter(config -> providerCode.equalsIgnoreCase(config.getProviderCode()))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Storage provider is not enabled: " + providerCode));
    }

    /**
     * 根据 providerCode 获取对应的存储客户端。
     *
     * @param providerCode Provider 编码（如 local、aliyun_oss）
     * @return StorageClient 实例
     * @throws ServiceException 如果未找到对应的客户端
     */
    private StorageClient getClient(String providerCode)
    {
        StorageClient client = storageClients.get(providerCode);
        if (client == null)
        {
            throw new ServiceException("Unsupported storage provider: " + providerCode);
        }
        return client;
    }

    /**
     *
     * @param objectKey 原始对象路径
     * @return 规范化后的路径
     * @throws ServiceException 如果路径为空或包含路径穿越字符
     */
    private String normalizeObjectKey(String objectKey)
    {
        if (StringUtils.isBlank(objectKey))
        {
            throw new ServiceException("File path must not be blank");
        }
        String normalized = objectKey.replace("\\", "/");
        while (normalized.startsWith("/"))
        {
            normalized = normalized.substring(1);
        }
        if (normalized.contains("../") || normalized.equals(".."))
        {
            throw new ServiceException("Invalid file path");
        }
        return normalized;
    }
}
