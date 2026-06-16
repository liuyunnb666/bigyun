package com.bigyun.file.service.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.domain.SysFile;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AliyunOssStorageClient extends AbstractStorageClient
{
    @Value("${file.temporary-url.expire-minutes:30}")
    private long temporaryUrlExpireMinutes;

    @Override
    public String getProviderCode()
    {
        return ProviderConfigConstants.PROVIDER_ALIYUN_OSS;
    }

    @Override
    public SysFile upload(MultipartFile file, ProviderConfigDTO config) throws Exception
    {
        String objectKey = applyObjectPrefix(config, buildObjectKey(file));
        OSS client = buildClient(config);
        try (var inputStream = file.getInputStream())
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            client.putObject(config.getBucketName(), objectKey, inputStream, metadata);
        }
        finally
        {
            client.shutdown();
        }
        String url = joinUrl(defaultDomain(config, buildDefaultDomain(config)), objectKey);
        SysFile sysFile = buildFile(objectKey, url, getProviderCode());
        sysFile.setTemporaryUrl(generateTemporaryUrl(config, objectKey));
        return sysFile;
    }

    @Override
    public boolean canHandle(String fileUrl, ProviderConfigDTO config)
    {
        return extractObjectKey(fileUrl, config.getDomain(), buildDefaultDomain(config)) != null;
    }

    @Override
    public void delete(String fileUrl, ProviderConfigDTO config) throws Exception
    {
        String objectKey = extractObjectKey(fileUrl, config.getDomain(), buildDefaultDomain(config));
        if (objectKey == null)
        {
            return;
        }
        OSS client = buildClient(config);
        try
        {
            client.deleteObject(config.getBucketName(), objectKey);
        }
        finally
        {
            client.shutdown();
        }
    }

    @Override
    public String getAccessibleUrl(String fileUrl, ProviderConfigDTO config) throws Exception
    {
        String objectKey = extractObjectKey(fileUrl, config.getDomain(), buildDefaultDomain(config));
        if (StringUtils.isBlank(objectKey))
        {
            throw new ServiceException("无法解析OSS文件路径，不能生成OCR访问地址");
        }
        return generateTemporaryUrl(config, objectKey);
    }

    private OSS buildClient(ProviderConfigDTO config)
    {
        return new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());
    }

    private String generateTemporaryUrl(ProviderConfigDTO config, String objectKey)
    {
        OSS client = buildClient(config);
        try
        {
            long expireMinutes = Math.max(1L, temporaryUrlExpireMinutes);
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);
            return client.generatePresignedUrl(config.getBucketName(), objectKey, expiration).toString();
        }
        finally
        {
            client.shutdown();
        }
    }

    private String buildDefaultDomain(ProviderConfigDTO config)
    {
        String host = config.getEndpoint().replaceFirst("^https?://", "");
        return "https://" + config.getBucketName() + "." + host;
    }
}
