package com.bigyun.provider.service;

import com.bigyun.provider.domain.ProviderRecommendationVO;
import java.util.List;

public interface IProviderRecommendationService
{
    List<ProviderRecommendationVO> selectRecommendations(String configType);
}
