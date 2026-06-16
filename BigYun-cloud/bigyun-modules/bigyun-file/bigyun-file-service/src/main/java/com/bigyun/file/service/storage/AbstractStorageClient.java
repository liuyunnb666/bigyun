package com.bigyun.file.service.storage;

import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.common.core.utils.file.FileUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.utils.FileUploadUtils;
import com.bigyun.file.domain.SysFile;
import java.net.URI;
import org.springframework.web.multipart.MultipartFile;

public abstract class AbstractStorageClient implements StorageClient
{
    protected String buildObjectKey(MultipartFile file)
    {
        return trimLeadingSlash(FileUploadUtils.extractFilename(file));
    }

    protected String trimLeadingSlash(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return value;
        }
        String normalized = value.replace("\\", "/");
        while (normalized.startsWith("/"))
        {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    protected String joinUrl(String prefix, String objectKey)
    {
        if (StringUtils.isBlank(prefix))
        {
            return objectKey;
        }
        return prefix.endsWith("/") ? prefix + objectKey : prefix + "/" + objectKey;
    }

    protected String applyObjectPrefix(ProviderConfigDTO config, String objectKey)
    {
        if (config == null || StringUtils.isBlank(config.getBasePath()))
        {
            return objectKey;
        }
        String prefix = trimLeadingSlash(config.getBasePath());
        return prefix + "/" + objectKey;
    }

    protected String extractObjectKey(String fileUrl, String... prefixes)
    {
        if (StringUtils.isBlank(fileUrl))
        {
            return null;
        }
        for (String prefix : prefixes)
        {
            if (StringUtils.isBlank(prefix))
            {
                continue;
            }
            String normalizedPrefix = prefix.endsWith("/") ? prefix : prefix + "/";
            if (fileUrl.startsWith(normalizedPrefix))
            {
                return trimLeadingSlash(stripQueryAndFragment(fileUrl.substring(normalizedPrefix.length())));
            }
        }
        if (!fileUrl.contains("://"))
        {
            return trimLeadingSlash(stripQueryAndFragment(fileUrl));
        }
        return null;
    }

    protected String extractObjectKeyByPathPrefix(String fileUrl, String... pathPrefixes)
    {
        String path = extractUrlPath(fileUrl);
        if (StringUtils.isBlank(path))
        {
            return null;
        }
        for (String prefix : pathPrefixes)
        {
            if (StringUtils.isBlank(prefix))
            {
                continue;
            }
            String normalizedPrefix = prefix.startsWith("/") ? prefix : "/" + prefix;
            normalizedPrefix = normalizedPrefix.endsWith("/") ? normalizedPrefix : normalizedPrefix + "/";
            if (path.startsWith(normalizedPrefix))
            {
                return trimLeadingSlash(path.substring(normalizedPrefix.length()));
            }
        }
        return null;
    }

    protected SysFile buildFile(String objectKey, String url, String providerCode)
    {
        SysFile sysFile = new SysFile();
        sysFile.setName(FileUtils.getName(objectKey));
        sysFile.setUrl(url);
        sysFile.setProvider(providerCode);
        sysFile.setObjectKey(objectKey);
        return sysFile;
    }

    protected String defaultDomain(ProviderConfigDTO config, String fallbackDomain)
    {
        return StringUtils.isNotBlank(config.getDomain()) ? config.getDomain() : fallbackDomain;
    }

    private String extractUrlPath(String fileUrl)
    {
        try
        {
            return fileUrl.contains("://") ? URI.create(fileUrl).getPath() : fileUrl;
        }
        catch (Exception e)
        {
            return fileUrl;
        }
    }

    private String stripQueryAndFragment(String value)
    {
        if (StringUtils.isBlank(value))
        {
            return value;
        }
        int queryIndex = value.indexOf('?');
        int fragmentIndex = value.indexOf('#');
        int endIndex = -1;
        if (queryIndex >= 0 && fragmentIndex >= 0)
        {
            endIndex = Math.min(queryIndex, fragmentIndex);
        }
        else if (queryIndex >= 0)
        {
            endIndex = queryIndex;
        }
        else if (fragmentIndex >= 0)
        {
            endIndex = fragmentIndex;
        }
        return endIndex >= 0 ? value.substring(0, endIndex) : value;
    }
}
