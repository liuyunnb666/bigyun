// -*- coding: utf-8 -*-
package com.bigyun.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bigyun.provider.domain.ProviderApiTemplate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Provider API模板Mapper接口
 *
 * 功能说明：
 * 提供Provider API模板的数据库访问操作，基于MyBatis Plus实现。
 * 支持基本的CRUD操作以及自定义查询方法。
 *
 * 主要方法：
 * 1. 根据配置类型、Provider编码、操作类型查询模板
 * 2. 查询指定Provider的所有模板
 * 3. 查询启用的模板列表
 *
 * @author BigYun
 * @date 2024-05-22
 */
public interface ProviderApiTemplateMapper extends BaseMapper<ProviderApiTemplate> {

    /**
     * 根据配置类型、Provider编码和操作类型查询API模板
     *
     * 使用场景：
     * 当需要执行某个Provider的特定操作时（如上传文件），
     * 通过此方法获取对应的API调用模板配置。
     *
     * @param configType 配置类型，如：storage、llm、tts等
     * @param providerCode Provider编码，如：aliyun-oss、openai-gpt等
     * @param operation 操作类型，如：upload、delete、chat等
     * @return API模板配置，如果不存在则返回null
     */
    ProviderApiTemplate selectByConfigAndOperation(
            @Param("configType") String configType,
            @Param("providerCode") String providerCode,
            @Param("operation") String operation
    );

    /**
     * 查询指定配置类型和Provider的所有API模板
     *
     * 使用场景：
     * 在配置管理页面展示某个Provider支持的所有操作及其API配置。
     *
     * @param configType 配置类型
     * @param providerCode Provider编码
     * @return API模板列表
     */
    List<ProviderApiTemplate> selectByConfigAndProvider(
            @Param("configType") String configType,
            @Param("providerCode") String providerCode
    );

    /**
     * 查询启用的API模板列表
     *
     * 使用场景：
     * 只查询is_enabled='1'的模板，过滤掉已禁用的模板。
     *
     * @param configType 配置类型（可选，为null时查询所有类型）
     * @param providerCode Provider编码（可选，为null时查询所有Provider）
     * @return 启用的API模板列表
     */
    List<ProviderApiTemplate> selectEnabledTemplates(
            @Param("configType") String configType,
            @Param("providerCode") String providerCode
    );

    /**
     * 批量插入API模板
     *
     * 使用场景：
     * 导入预设模板或批量创建模板时使用。
     *
     * @param templates API模板列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("templates") List<ProviderApiTemplate> templates);

    /**
     * 更新模板的启用状态
     *
     * 使用场景：
     * 快速启用或禁用某个模板，无需更新整个对象。
     *
     * @param templateId 模板ID
     * @param isEnabled 是否启用：0=否，1=是
     * @return 更新的记录数
     */
    int updateEnabledStatus(
            @Param("templateId") Long templateId,
            @Param("isEnabled") String isEnabled
    );
}
