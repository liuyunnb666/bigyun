package com.bigyun.provider.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.provider.db.domain.ProviderCapabilityEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Read-only mapper for Provider capability bindings.
 */
@Mapper
public interface ProviderCapabilityReadMapper extends BaseMapper<ProviderCapabilityEntity>
{
}
