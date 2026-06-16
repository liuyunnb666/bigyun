package com.bigyun.provider.service;

import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.provider.domain.ProviderCapability;
import java.util.List;

public interface IProviderCapabilityService
{
    ProviderCapabilityDTO selectDefaultCapability(String capabilityCode);

    List<ProviderCapability> selectCapabilityList(ProviderCapability query);

    ProviderCapability selectCapabilityById(Long capabilityId);

    int setDefaultCapability(Long capabilityId, String updateBy);

    int insertCapability(ProviderCapability capability);

    int updateCapability(ProviderCapability capability);

    int updateCapabilityStatus(Long capabilityId, String status, String updateBy);

    void deleteCapabilityByIds(Long[] capabilityIds);
}
