package com.bigyun.file.service.storage;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.file.domain.SysFile;
import org.springframework.web.multipart.MultipartFile;

public interface StorageClient
{
    String getProviderCode();

    SysFile upload(MultipartFile file, ProviderConfigDTO config) throws Exception;

    boolean canHandle(String fileUrl, ProviderConfigDTO config);

    void delete(String fileUrl, ProviderConfigDTO config) throws Exception;

    default String getAccessibleUrl(String fileUrl, ProviderConfigDTO config) throws Exception
    {
        return fileUrl;
    }
}
