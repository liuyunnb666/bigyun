package com.bigyun.config.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.config.domain.ProviderConfigEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Provider 配置只读 Mapper 接口。
 * <p>
 * 继承 MyBatis-Plus 的 {@link BaseMapper}，自动获得基础 CRUD 方法。
 * 该 Mapper 专用于 {@link com.bigyun.config.reader.ConfigReader} 读取配置，
 * 配合 {@code @Slave} 注解实现读写分离（从库读取）。
 * </p>
 *
 * @author bigyun
 */
@Mapper
public interface ProviderConfigReadMapper extends BaseMapper<ProviderConfigEntity>
{
}
