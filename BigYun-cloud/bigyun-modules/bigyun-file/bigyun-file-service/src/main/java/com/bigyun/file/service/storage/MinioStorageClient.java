package com.bigyun.file.service.storage;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.domain.SysFile;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class MinioStorageClient extends AbstractStorageClient
{
    @Override
    public String getProviderCode()
    {
        return ProviderConfigConstants.PROVIDER_MINIO;
    }

    @Override
    public SysFile upload(MultipartFile file, ProviderConfigDTO config) throws Exception
    {
        String objectKey = applyObjectPrefix(config, buildObjectKey(file));
        MinioClient client = buildClient(config);
        try (var inputStream = file.getInputStream())
        {
            client.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucketName())
                    .object(objectKey)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        }
        String url = joinUrl(defaultDomain(config, config.getEndpoint() + "/" + config.getBucketName()), objectKey);
        return buildFile(objectKey, url, getProviderCode());
    }

    @Override
    public boolean canHandle(String fileUrl, ProviderConfigDTO config)
    {
        String fallback = config.getEndpoint() + "/" + config.getBucketName();
        return extractObjectKey(fileUrl, config.getDomain(), fallback) != null;
    }

    @Override
    public void delete(String fileUrl, ProviderConfigDTO config) throws Exception
    {
        String fallback = config.getEndpoint() + "/" + config.getBucketName();
        String objectKey = extractObjectKey(fileUrl, config.getDomain(), fallback);
        if (objectKey == null)
        {
            return;
        }
        buildClient(config).removeObject(RemoveObjectArgs.builder().bucket(config.getBucketName()).object(objectKey).build());
    }

    private MinioClient buildClient(ProviderConfigDTO config)
    {
        if (config == null)
        {
            throw new ServiceException("MinIO 配置不存在");
        }
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }
}
