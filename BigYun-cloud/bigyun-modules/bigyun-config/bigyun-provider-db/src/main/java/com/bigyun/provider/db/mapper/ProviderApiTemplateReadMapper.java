package com.bigyun.provider.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.provider.db.domain.ProviderApiTemplateEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Read-only mapper for Provider API runtime templates.
 */
@Mapper
public interface ProviderApiTemplateReadMapper extends BaseMapper<ProviderApiTemplateEntity>
{
}
