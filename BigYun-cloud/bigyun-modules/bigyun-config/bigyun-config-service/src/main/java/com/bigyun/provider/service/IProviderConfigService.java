package com.bigyun.provider.service;

import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.provider.domain.ProviderConfig;
import java.util.List;

public interface IProviderConfigService
{
    List<ProviderConfig> selectProviderConfigList(ProviderConfig query);

    ProviderConfig selectProviderConfigById(Long configId);

    ProviderConfig selectDefaultProviderConfig(String configType);

    ProviderConfigDTO selectDefaultProviderConfigInternal(String configType);

    List<ProviderConfigDTO> selectEnabledProviderConfigsInternal(String configType);

    ProviderConfigDTO getConfig(String configType, String providerCode);

    int insertProviderConfig(ProviderConfig config);

    int updateProviderConfig(ProviderConfig config);

    int updateProviderConfigStatus(Long configId, String status, String updateBy);

    int setDefaultProviderConfig(Long configId, String updateBy);

    boolean testProviderConnection(ProviderConfig config);

    void deleteProviderConfigByIds(Long[] configIds);
}
