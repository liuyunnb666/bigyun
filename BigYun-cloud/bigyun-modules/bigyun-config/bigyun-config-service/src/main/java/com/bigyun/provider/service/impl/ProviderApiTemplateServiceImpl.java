// -*- coding: utf-8 -*-
package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bigyun.provider.cache.CacheInvalidator;
import com.bigyun.provider.db.snapshot.ProviderRuntimeSnapshotPublisher;
import com.bigyun.provider.domain.ProviderApiTemplate;
import com.bigyun.provider.mapper.ProviderApiTemplateMapper;
import com.bigyun.provider.service.IProviderApiTemplateService;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Provider API 模板服务实现。
 * <p>
 * 负责 API 模板 CRUD、启停、批量导入，并在配置变更后发布运行时快照。
 */
@Service
public class ProviderApiTemplateServiceImpl implements IProviderApiTemplateService
{
    private static final Logger log = LoggerFactory.getLogger(ProviderApiTemplateServiceImpl.class);

    @Autowired
    private ProviderApiTemplateMapper templateMapper;

    @Autowired
    private CacheInvalidator cacheInvalidator;

    @Autowired
    private ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher;

    /**
     * 根据配置类型、Provider 编码和操作类型查询 API 模板。
     * <p>
     * 运行时通用 Handler 会用该模板确定目标 URL、请求方法、认证方式、请求体和响应映射规则。
     *
     * @param configType 配置类型，例如 llm、ocr、face
     * @param providerCode Provider 编码
     * @param operation 能力操作编码
     * @return 匹配的 API 模板；不存在时返回 null
     */
    @Override
    public ProviderApiTemplate getTemplate(String configType, String providerCode, String operation)
    {
        log.debug("查询 API 模板: configType={}, providerCode={}, operation={}",
                configType, providerCode, operation);

        ProviderApiTemplate template = templateMapper.selectByConfigAndOperation(
                configType, providerCode, operation);

        if (template == null)
        {
            log.warn("未找到 API 模板: configType={}, providerCode={}, operation={}",
                    configType, providerCode, operation);
        }

        return template;
    }

    /**
     * 根据模板 ID 查询 API 模板。
     *
     * @param templateId 模板主键
     * @return API 模板；不存在时返回 null
     */
    @Override
    public ProviderApiTemplate getTemplateById(Long templateId)
    {
        return templateMapper.selectById(templateId);
    }

    /**
     * 查询指定配置类型和 Provider 的 API 模板列表。
     * <p>
     * configType 和 providerCode 都存在时走 Mapper 专用查询；否则按传入字段动态过滤。
     *
     * @param configType 配置类型，可为空
     * @param providerCode Provider 编码，可为空
     * @return API 模板列表
     */
    @Override
    public List<ProviderApiTemplate> listTemplates(String configType, String providerCode)
    {
        if (configType != null && providerCode != null)
        {
            return templateMapper.selectByConfigAndProvider(configType, providerCode);
        }

        LambdaQueryWrapper<ProviderApiTemplate> queryWrapper = new LambdaQueryWrapper<>();
        if (configType != null)
        {
            queryWrapper.eq(ProviderApiTemplate::getConfigType, configType);
        }
        if (providerCode != null)
        {
            queryWrapper.eq(ProviderApiTemplate::getProviderCode, providerCode);
        }

        return templateMapper.selectList(queryWrapper);
    }

    /**
     * 查询启用状态的 API 模板列表。
     *
     * @param configType 配置类型
     * @param providerCode Provider 编码
     * @return 已启用的 API 模板列表
     */
    @Override
    public List<ProviderApiTemplate> listEnabledTemplates(String configType, String providerCode)
    {
        return templateMapper.selectEnabledTemplates(configType, providerCode);
    }

    /**
     * 按后台列表条件查询 API 模板。
     * <p>
     * 分页由 Controller 层 startPage 处理，本方法只负责拼装过滤条件和排序。
     *
     * @param template 查询条件
     * @return API 模板列表
     */
    @Override
    public List<ProviderApiTemplate> selectTemplateList(ProviderApiTemplate template)
    {
        LambdaQueryWrapper<ProviderApiTemplate> queryWrapper = new LambdaQueryWrapper<>();

        if (template.getConfigType() != null && !template.getConfigType().isEmpty())
        {
            queryWrapper.eq(ProviderApiTemplate::getConfigType, template.getConfigType());
        }
        if (template.getProviderCode() != null && !template.getProviderCode().isEmpty())
        {
            queryWrapper.eq(ProviderApiTemplate::getProviderCode, template.getProviderCode());
        }
        if (template.getOperation() != null && !template.getOperation().isEmpty())
        {
            queryWrapper.like(ProviderApiTemplate::getOperation, template.getOperation());
        }
        if (template.getIsEnabled() != null && !template.getIsEnabled().isEmpty())
        {
            queryWrapper.eq(ProviderApiTemplate::getIsEnabled, template.getIsEnabled());
        }

        queryWrapper.orderByDesc(ProviderApiTemplate::getCreateTime);
        return templateMapper.selectList(queryWrapper);
    }

    /**
     * 新增 API 模板。
     * <p>
     * 同一 configType、providerCode、operation 只能存在一条模板；写入成功后在事务提交后刷新该配置类型的运行时快照。
     *
     * @param template 待新增的 API 模板
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveTemplate(ProviderApiTemplate template)
    {
        log.info("保存 API 模板: configType={}, providerCode={}, operation={}",
                template.getConfigType(), template.getProviderCode(), template.getOperation());

        if (templateExists(template.getConfigType(), template.getProviderCode(), template.getOperation()))
        {
            log.warn("API 模板已存在，无法保存");
            throw new RuntimeException("API 模板已存在");
        }

        int rows = templateMapper.insert(template);
        if (rows > 0)
        {
            notifyAfterCommit(template.getConfigType());
        }
        return rows;
    }

    /**
     * 更新 API 模板。
     * <p>
     * 更新成功后按原模板所属配置类型刷新运行时快照，避免运行时继续使用旧请求模板。
     *
     * @param template 待更新模板
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTemplate(ProviderApiTemplate template)
    {
        log.info("更新 API 模板: templateId={}", template.getTemplateId());

        ProviderApiTemplate current = template.getTemplateId() == null ? null : templateMapper.selectById(template.getTemplateId());
        int rows = templateMapper.updateById(template);
        if (rows > 0)
        {
            notifyAfterCommit(current == null ? template.getConfigType() : current.getConfigType());
        }
        return rows;
    }

    /**
     * 删除单条 API 模板。
     *
     * @param templateId 模板主键
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTemplate(Long templateId)
    {
        log.info("删除 API 模板: templateId={}", templateId);

        ProviderApiTemplate current = templateMapper.selectById(templateId);
        int rows = templateMapper.deleteById(templateId);
        if (rows > 0 && current != null)
        {
            notifyAfterCommit(current.getConfigType());
        }
        return rows;
    }

    /**
     * 批量删除 API 模板。
     * <p>
     * 会收集所有受影响的配置类型，并在提交后逐个刷新运行时快照。
     *
     * @param templateIds 待删除模板主键数组
     * @return 删除行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTemplates(Long[] templateIds)
    {
        log.info("批量删除 API 模板: count={}", templateIds.length);

        int count = 0;
        Set<String> changedTypes = new LinkedHashSet<>();
        for (Long templateId : templateIds)
        {
            ProviderApiTemplate current = templateMapper.selectById(templateId);
            count += templateMapper.deleteById(templateId);
            if (current != null)
            {
                changedTypes.add(current.getConfigType());
            }
        }
        for (String configType : changedTypes)
        {
            notifyAfterCommit(configType);
        }
        return count;
    }

    /**
     * 启用或停用 API 模板。
     *
     * @param templateId 模板主键
     * @param isEnabled 启用标记
     * @return 受影响行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEnabledStatus(Long templateId, String isEnabled)
    {
        log.info("更新模板启用状态: templateId={}, isEnabled={}", templateId, isEnabled);

        ProviderApiTemplate current = templateMapper.selectById(templateId);
        int rows = templateMapper.updateEnabledStatus(templateId, isEnabled);
        if (rows > 0 && current != null)
        {
            notifyAfterCommit(current.getConfigType());
        }
        return rows;
    }

    /**
     * 批量导入 API 模板。
     * <p>
     * 已存在的 configType/providerCode/operation 组合会被跳过，只插入新增模板。
     * 导入成功后按配置类型刷新运行时快照。
     *
     * @param templates 待导入模板列表
     * @return 新增行数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchImportTemplates(List<ProviderApiTemplate> templates)
    {
        if (templates == null || templates.isEmpty())
        {
            return 0;
        }
        log.info("批量导入 API 模板: count={}", templates.size());

        List<ProviderApiTemplate> newTemplates = new ArrayList<>();
        for (ProviderApiTemplate template : templates)
        {
            if (!templateExists(template.getConfigType(), template.getProviderCode(), template.getOperation()))
            {
                newTemplates.add(template);
            }
            else
            {
                log.debug("跳过已存在的模板: {}/{}/{}",
                        template.getConfigType(), template.getProviderCode(), template.getOperation());
            }
        }

        if (newTemplates.isEmpty())
        {
            log.info("没有需要导入的新模板");
            return 0;
        }

        int rows = templateMapper.batchInsert(newTemplates);
        Set<String> changedTypes = new LinkedHashSet<>();
        for (ProviderApiTemplate template : newTemplates)
        {
            changedTypes.add(template.getConfigType());
        }
        for (String configType : changedTypes)
        {
            notifyAfterCommit(configType);
        }
        return rows;
    }

    /**
     * 测试 API 模板配置。
     * <p>
     * 当前方法保留接口形态，后续可接入模板渲染、HTTP 调用和响应解析链路。
     *
     * @param template 待测试模板
     * @param testContext 测试上下文参数
     * @return 测试结果文本
     */
    @Override
    public String testTemplate(ProviderApiTemplate template, Map<String, Object> testContext)
    {
        log.info("测试 API 模板: configType={}, providerCode={}, operation={}",
                template.getConfigType(), template.getProviderCode(), template.getOperation());

        try
        {
            // TODO: 实现模板测试逻辑
            // 1. 使用 TemplateEngine 渲染 URL 和请求头
            // 2. 使用 GenericHttpClient 发送请求
            // 3. 使用 ResponseParser 解析响应
            // 4. 返回测试结果
            return "测试功能待实现";
        }
        catch (Exception e)
        {
            log.error("测试 API 模板失败: {}", e.getMessage(), e);
            return "测试失败: " + e.getMessage();
        }
    }

    /**
     * 判断指定模板唯一键是否已存在。
     *
     * @param configType 配置类型
     * @param providerCode Provider 编码
     * @param operation 操作编码
     * @return true 表示模板已存在
     */
    @Override
    public boolean templateExists(String configType, String providerCode, String operation)
    {
        ProviderApiTemplate template = templateMapper.selectByConfigAndOperation(
                configType, providerCode, operation);
        return template != null;
    }

    /**
     * 注册事务提交后的运行时快照刷新。
     *
     * @param configType 受影响的配置类型
     */
    private void notifyAfterCommit(String configType)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            publishRuntimeSnapshot(configType);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit()
            {
                publishRuntimeSnapshot(configType);
            }
        });
    }

    /**
     * 发布模板所属配置类型的运行时快照并广播缓存失效。
     *
     * @param configType 受影响的配置类型
     */
    private void publishRuntimeSnapshot(String configType)
    {
        runtimeSnapshotPublisher.publishConfigType(configType);
        cacheInvalidator.notify(configType);
    }
}
