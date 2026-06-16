package com.bigyun.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.provider.domain.ProviderMeta;
import org.apache.ibatis.annotations.Mapper;

/**
 * Provider元数据Mapper接口
 *
 * 功能说明：
 * 提供对sys_provider_meta表的数据访问操作
 *
 * 主要用途：
 * 1. ProviderEnumManager使用此Mapper同步枚举到数据库
 * 2. 前端通过Service层查询所有支持的配置类型和服务商
 * 3. 配置验证时检查配置类型和服务商代码的合法性
 *
 * @author BigYun
 * @date 2026-05-21
 */
@Mapper
public interface ProviderMetaMapper extends BaseMapper<ProviderMeta> {
    // 继承BaseMapper，自动拥有CRUD方法
    // 如需自定义SQL，可在此添加方法并在对应的XML中实现
}
