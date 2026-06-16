package com.bigyun.provider.service;

import com.bigyun.config.domain.CapabilityModelCompareVO;
import com.bigyun.config.domain.CapabilityModelRelationDTO;
import com.bigyun.config.domain.CapabilityModelSwitchReq;
import com.bigyun.provider.domain.ProviderCapability;
import java.util.List;

public interface IProviderCapabilityModelRelationService
{
    List<CapabilityModelRelationDTO> selectRelationList(CapabilityModelRelationDTO query);

    CapabilityModelRelationDTO selectRelationById(Long relationId);

    int insertRelation(CapabilityModelRelationDTO relation, String updateBy);

    int updateRelation(CapabilityModelRelationDTO relation, String updateBy);

    int deleteRelationByIds(Long[] relationIds);

    CapabilityModelCompareVO compare(Long leftCapabilityId, Long rightCapabilityId);

    int switchDefaultModel(CapabilityModelSwitchReq request, String updateBy);

    List<ProviderCapability> selectCandidateCapabilityList(ProviderCapability query);
}
