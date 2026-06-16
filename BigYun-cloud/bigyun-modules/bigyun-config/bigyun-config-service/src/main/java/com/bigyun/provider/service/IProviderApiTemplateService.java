// -*- coding: utf-8 -*-
package com.bigyun.provider.service;

import com.bigyun.provider.domain.ProviderApiTemplate;

import java.util.List;

/**
 * Provider API模板服务接口
 *
 * 功能说明：
 * 提供API模板的CRUD操作，支持模板的查询、创建、更新、删除等功能。
 * 这是零代码配置驱动架构的核心服务接口。
 *
 * 主要功能：
 * 1. 根据配置类型、Provider编码、操作类型查询模板
 * 2. 查询指定Provider的所有模板
 * 3. 保存和更新模板配置
 * 4. 删除模板
 * 5. 批量导入预设模板
 *
 * 使用场景：
 * - 在GenericConfigDrivenHandler中查询API模板
 * - 在配置管理页面进行模板的增删改查
 * - 导入预设模板到数据库
 *
 * @author BigYun
 * @date 2024-05-22
 */
public interface IProviderApiTemplateService {

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
    ProviderApiTemplate getTemplate(String configType, String providerCode, String operation);

    /**
     * 根据模板ID查询API模板
     *
     * @param templateId 模板ID
     * @return API模板配置
     */
    ProviderApiTemplate getTemplateById(Long templateId);

    /**
     * 查询指定配置类型和Provider的所有API模板
     *
     * 使用场景：
     * 在配置管理页面展示某个Provider支持的所有操作及其API配置。
     *
     * @param configType 配置类型（可选，为null时查询所有类型）
     * @param providerCode Provider编码（可选，为null时查询所有Provider）
     * @return API模板列表
     */
    List<ProviderApiTemplate> listTemplates(String configType, String providerCode);

    /**
     * 查询启用的API模板列表
     *
     * 使用场景：
     * 只查询is_enabled='1'的模板，过滤掉已禁用的模板。
     *
     * @param configType 配置类型（可选）
     * @param providerCode Provider编码（可选）
     * @return 启用的API模板列表
     */
    List<ProviderApiTemplate> listEnabledTemplates(String configType, String providerCode);

    /**
     * 分页查询API模板列表
     *
     * @param template 查询条件
     * @return API模板列表
     */
    List<ProviderApiTemplate> selectTemplateList(ProviderApiTemplate template);

    /**
     * 保存API模板
     *
     * 使用场景：
     * 在配置管理页面创建新的API模板。
     *
     * @param template API模板
     * @return 保存结果（受影响的行数）
     */
    int saveTemplate(ProviderApiTemplate template);

    /**
     * 更新API模板
     *
     * 使用场景：
     * 在配置管理页面修改现有的API模板。
     *
     * @param template API模板
     * @return 更新结果（受影响的行数）
     */
    int updateTemplate(ProviderApiTemplate template);

    /**
     * 删除API模板
     *
     * @param templateId 模板ID
     * @return 删除结果（受影响的行数）
     */
    int deleteTemplate(Long templateId);

    /**
     * 批量删除API模板
     *
     * @param templateIds 模板ID数组
     * @return 删除结果（受影响的行数）
     */
    int deleteTemplates(Long[] templateIds);

    /**
     * 更新模板的启用状态
     *
     * 使用场景：
     * 快速启用或禁用某个模板，无需更新整个对象。
     *
     * @param templateId 模板ID
     * @param isEnabled 是否启用：0=否，1=是
     * @return 更新结果（受影响的行数）
     */
    int updateEnabledStatus(Long templateId, String isEnabled);

    /**
     * 批量导入API模板
     *
     * 使用场景：
     * 导入预设模板或从文件批量导入模板。
     *
     * @param templates API模板列表
     * @return 导入成功的数量
     */
    int batchImportTemplates(List<ProviderApiTemplate> templates);

    /**
     * 测试API模板配置
     *
     * 使用场景：
     * 在保存模板前测试配置是否正确，验证API调用是否成功。
     *
     * @param template API模板
     * @param testContext 测试上下文（包含测试数据）
     * @return 测试结果
     */
    String testTemplate(ProviderApiTemplate template, java.util.Map<String, Object> testContext);

    /**
     * 检查模板是否存在
     *
     * @param configType 配置类型
     * @param providerCode Provider编码
     * @param operation 操作类型
     * @return true=存在，false=不存在
     */
    boolean templateExists(String configType, String providerCode, String operation);
}
