package com.bigyun.file.service.storage;

import com.bigyun.config.constant.ProviderConfigConstants;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.domain.SysFile;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class TencentCosStorageClient extends AbstractStorageClient
{
    @Override
    public String getProviderCode()
    {
        return ProviderConfigConstants.PROVIDER_TENCENT_COS;
    }

    @Override
    public SysFile upload(MultipartFile file, ProviderConfigDTO config) throws Exception
    {
        String objectKey = applyObjectPrefix(config, buildObjectKey(file));
        COSClient client = buildClient(config);
        try (var inputStream = file.getInputStream())
        {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            client.putObject(new PutObjectRequest(config.getBucketName(), objectKey, inputStream, metadata));
        }
        finally
        {
            client.shutdown();
        }
        String url = joinUrl(defaultDomain(config, buildDefaultDomain(config)), objectKey);
        return buildFile(objectKey, url, getProviderCode());
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
        COSClient client = buildClient(config);
        try
        {
            client.deleteObject(config.getBucketName(), objectKey);
        }
        finally
        {
            client.shutdown();
        }
    }

    private COSClient buildClient(ProviderConfigDTO config)
    {
        COSCredentials credentials = new BasicCOSCredentials(config.getAccessKey(), config.getSecretKey());
        return new COSClient(credentials, new ClientConfig(new Region(config.getRegion())));
    }

    private String buildDefaultDomain(ProviderConfigDTO config)
    {
        return "https://" + config.getBucketName() + ".cos." + config.getRegion() + ".myqcloud.com";
    }
}
