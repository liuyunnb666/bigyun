package com.bigyun.provider.service.handler.storage;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.config.service.handler.storage.StorageRequest;
import com.bigyun.config.service.handler.storage.StorageResponse;
import com.bigyun.common.core.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地存储处理器
 */
@Component
public class LocalStorageHandler implements IProviderHandler<StorageRequest, StorageResponse> {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageHandler.class);

    @Override
    public ProviderResponse<StorageResponse> execute(ProviderConfigDTO config, ProviderRequest<StorageRequest> request) {
        try {
            StorageRequest storageRequest = request.getData();
            String basePath = config.getBasePath();
            String domain = config.getDomain();

            switch (storageRequest.getOperation()) {
                case UPLOAD:
                    return handleUpload(basePath, domain, storageRequest);
                case DELETE:
                    return handleDelete(basePath, storageRequest);
                case GET_URL:
                    return handleGetUrl(domain, storageRequest);
                default:
                    return ProviderResponse.fail("UNSUPPORTED_OPERATION", "不支持的操作类型");
            }
        } catch (Exception e) {
            log.error("本地存储操作失败", e);
            return ProviderResponse.fail("LOCAL_STORAGE_ERROR", e.getMessage());
        }
    }

    private ProviderResponse<StorageResponse> handleUpload(String basePath, String domain, StorageRequest request) throws IOException {
        String filePath = request.getFilePath();
        String fullPath = basePath + File.separator + filePath;

        // 创建目录
        Path path = Paths.get(fullPath);
        Files.createDirectories(path.getParent());

        // 写入文件
        try (FileOutputStream fos = new FileOutputStream(fullPath)) {
            fos.write(request.getFileData());
        }

        StorageResponse response = new StorageResponse();
        response.setFilePath(filePath);
        response.setFileUrl(domain + "/" + filePath);
        response.setFileSize((long) request.getFileData().length);

        log.info("本地存储上传成功: {}", fullPath);
        return ProviderResponse.success(response);
    }

    private ProviderResponse<StorageResponse> handleDelete(String basePath, StorageRequest request) throws IOException {
        String filePath = request.getFilePath();
        String fullPath = basePath + File.separator + filePath;

        Files.deleteIfExists(Paths.get(fullPath));

        StorageResponse response = new StorageResponse();
        response.setFilePath(filePath);

        log.info("本地存储删除成功: {}", fullPath);
        return ProviderResponse.success(response);
    }

    private ProviderResponse<StorageResponse> handleGetUrl(String domain, StorageRequest request) {
        String filePath = request.getFilePath();

        StorageResponse response = new StorageResponse();
        response.setFilePath(filePath);
        response.setFileUrl(domain + "/" + filePath);

        return ProviderResponse.success(response);
    }

    @Override
    public String getSupportedConfigType() {
        return "storage";
    }

    @Override
    public String getSupportedProviderCode() {
        return "local";
    }

    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        return config != null && StringUtils.isNotBlank(config.getBasePath());
    }
}
