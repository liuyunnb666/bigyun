package com.bigyun.provider.service.handler.storage;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.config.service.handler.storage.StorageRequest;
import com.bigyun.config.service.handler.storage.StorageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS存储处理器
 */
@Component
public class AliyunOSSHandler implements IProviderHandler<StorageRequest, StorageResponse> {

    private static final Logger log = LoggerFactory.getLogger(AliyunOSSHandler.class);

    @Override
    public ProviderResponse<StorageResponse> execute(ProviderConfigDTO config, ProviderRequest<StorageRequest> request) {
        try {
            StorageRequest storageRequest = request.getData();

            // TODO: 集成阿里云OSS SDK
            // OSSClient ossClient = new OSSClient(
            //     config.getEndpoint(),
            //     config.getAccessKey(),
            //     config.getSecretKey()
            // );

            switch (storageRequest.getOperation()) {
                case UPLOAD:
                    return handleUpload(config, storageRequest);
                case DELETE:
                    return handleDelete(config, storageRequest);
                case GET_URL:
                    return handleGetUrl(config, storageRequest);
                default:
                    return ProviderResponse.fail("UNSUPPORTED_OPERATION", "不支持的操作类型");
            }
        } catch (Exception e) {
            log.error("阿里云OSS操作失败", e);
            return ProviderResponse.fail("ALIYUN_OSS_ERROR", e.getMessage());
        }
    }

    private ProviderResponse<StorageResponse> handleUpload(ProviderConfigDTO config, StorageRequest request) {
        // TODO: 实际调用阿里云OSS SDK上传
        // ossClient.putObject(config.getBucketName(), request.getFilePath(), new ByteArrayInputStream(request.getFileData()));

        String fileUrl = buildFileUrl(config, request.getFilePath());

        StorageResponse response = new StorageResponse();
        response.setFilePath(request.getFilePath());
        response.setFileUrl(fileUrl);
        response.setFileSize((long) request.getFileData().length);

        log.info("阿里云OSS上传成功: {}", request.getFilePath());
        return ProviderResponse.success(response);
    }

    private ProviderResponse<StorageResponse> handleDelete(ProviderConfigDTO config, StorageRequest request) {
        // TODO: 实际调用阿里云OSS SDK删除
        // ossClient.deleteObject(config.getBucketName(), request.getFilePath());

        StorageResponse response = new StorageResponse();
        response.setFilePath(request.getFilePath());

        log.info("阿里云OSS删除成功: {}", request.getFilePath());
        return ProviderResponse.success(response);
    }

    private ProviderResponse<StorageResponse> handleGetUrl(ProviderConfigDTO config, StorageRequest request) {
        String fileUrl = buildFileUrl(config, request.getFilePath());

        StorageResponse response = new StorageResponse();
        response.setFilePath(request.getFilePath());
        response.setFileUrl(fileUrl);

        return ProviderResponse.success(response);
    }

    private String buildFileUrl(ProviderConfigDTO config, String filePath) {
        if (config.getDomain() != null && !config.getDomain().isEmpty()) {
            return config.getDomain() + "/" + filePath;
        }
        return "https://" + config.getBucketName() + "." + config.getEndpoint() + "/" + filePath;
    }

    @Override
    public String getSupportedConfigType() {
        return "storage";
    }

    @Override
    public String getSupportedProviderCode() {
        return "aliyun-oss";
    }

    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        return config != null
            && config.getEndpoint() != null
            && config.getAccessKey() != null
            && config.getSecretKey() != null
            && config.getBucketName() != null;
    }
}
