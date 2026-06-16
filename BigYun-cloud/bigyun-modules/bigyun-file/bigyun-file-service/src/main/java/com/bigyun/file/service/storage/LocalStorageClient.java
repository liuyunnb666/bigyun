package com.bigyun.file.service.storage;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.utils.FileUploadUtils;
import com.bigyun.file.domain.SysFile;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class LocalStorageClient extends AbstractStorageClient
{
    @Value("${file.domain:http://localhost:8080/file/profile}")
    private String defaultPublicPrefix;

    @Override
    public String getProviderCode()
    {
        return ProviderConfigConstants.PROVIDER_LOCAL;
    }

    @Override
    public SysFile upload(MultipartFile file, ProviderConfigDTO config) throws Exception
    {
        if (StringUtils.isBlank(config.getBasePath()))
        {
            throw new ServiceException("本地存储根路径未配置");
        }
        String objectKey = buildObjectKey(file);
        Path target = Path.of(config.getBasePath(), objectKey.replace("/", java.io.File.separator));
        Files.createDirectories(target.getParent());
        file.transferTo(target);
        String url = joinUrl(getPublicPrefix(config), objectKey);
        return buildFile(objectKey, url, getProviderCode());
    }

    @Override
    public boolean canHandle(String fileUrl, ProviderConfigDTO config)
    {
        return resolveObjectKey(fileUrl, config) != null;
    }

    @Override
    public void delete(String fileUrl, ProviderConfigDTO config) throws Exception
    {
        String objectKey = resolveObjectKey(fileUrl, config);
        if (StringUtils.isBlank(objectKey))
        {
            return;
        }
        Path target = Path.of(config.getBasePath(), objectKey.replace("/", java.io.File.separator));
        Files.deleteIfExists(target);
    }

    private String resolveObjectKey(String fileUrl, ProviderConfigDTO config)
    {
        String objectKey = extractObjectKey(fileUrl, config.getDomain());
        if (StringUtils.isNotBlank(objectKey))
        {
            return objectKey;
        }
        return extractObjectKeyByPathPrefix(fileUrl, "/file/profile", "/profile", "/file/public", "/public");
    }

    private String getPublicPrefix(ProviderConfigDTO config)
    {
        return StringUtils.isBlank(config.getDomain()) ? defaultPublicPrefix : config.getDomain();
    }
}
