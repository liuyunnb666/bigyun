package com.bigyun.provider.service;

import com.bigyun.provider.domain.ProviderModelCatalog;
import java.util.List;

public interface IProviderModelCatalogService
{
    List<ProviderModelCatalog> selectModelCatalogList(ProviderModelCatalog query);

    ProviderModelCatalog selectModelCatalogById(Long modelId);

    int insertModelCatalog(ProviderModelCatalog modelCatalog);

    int updateModelCatalog(ProviderModelCatalog modelCatalog);

    int updateModelCatalogStatus(Long modelId, String isEnabled, String updateBy);

    void deleteModelCatalogByIds(Long[] modelIds);
}
