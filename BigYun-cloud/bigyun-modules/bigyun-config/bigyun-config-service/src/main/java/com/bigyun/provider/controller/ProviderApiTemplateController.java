// -*- coding: utf-8 -*-
package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.provider.domain.ProviderApiTemplate;
import com.bigyun.provider.domain.ProviderApiTemplateTestReq;
import com.bigyun.provider.service.IProviderApiTemplateService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provider API模板管理Controller
 *
 * 功能说明：
 * 提供API模板的REST接口，支持模板的增删改查操作。
 * 这是零代码配置驱动架构的前端交互入口。
 *
 * 主要接口：
 * 1. GET /list - 查询模板列表（分页）
 * 2. GET /{templateId} - 查询模板详情
 * 3. POST / - 新增模板
 * 4. PUT / - 修改模板
 * 5. DELETE /{templateIds} - 删除模板
 * 6. POST /test - 测试模板配置
 * 7. POST /import - 批量导入模板
 *
 * @author BigYun
 * @date 2024-05-22
 */
@RestController
@RequestMapping("/config/api-template")
public class ProviderApiTemplateController extends BaseController {

    @Autowired
    private IProviderApiTemplateService templateService;

    /**
     * 查询API模板列表
     *
     * @param template 查询条件
     * @return 模板列表
     */
    @RequiresPermissions("config:template:list")
    @GetMapping("/list")
    public TableDataInfo list(ProviderApiTemplate template) {
        startPage();
        List<ProviderApiTemplate> list = templateService.selectTemplateList(template);
        return getDataTable(list);
    }

    /**
     * 查询API模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    @RequiresPermissions("config:template:query")
    @GetMapping("/{templateId}")
    public AjaxResult getInfo(@PathVariable Long templateId) {
        return success(templateService.getTemplateById(templateId));
    }

    /**
     * 根据配置类型和Provider查询模板
     *
     * @param configType 配置类型
     * @param providerCode Provider编码
     * @return 模板列表
     */
    @RequiresPermissions("config:template:query")
    @GetMapping("/query")
    public AjaxResult query(@RequestParam String configType,
                           @RequestParam String providerCode) {
        List<ProviderApiTemplate> list = templateService.listTemplates(configType, providerCode);
        return success(list);
    }

    /**
     * 查询启用的模板列表
     *
     * @param configType 配置类型（可选）
     * @param providerCode Provider编码（可选）
     * @return 启用的模板列表
     */
    @RequiresPermissions("config:template:query")
    @GetMapping("/enabled")
    public AjaxResult listEnabled(@RequestParam(required = false) String configType,
                                  @RequestParam(required = false) String providerCode) {
        List<ProviderApiTemplate> list = templateService.listEnabledTemplates(configType, providerCode);
        return success(list);
    }

    /**
     * 新增API模板
     *
     * @param template 模板信息
     * @return 操作结果
     */
    @RequiresPermissions("config:template:add")
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ProviderApiTemplate template) {
        return toAjax(templateService.saveTemplate(template));
    }

    /**
     * 修改API模板
     *
     * @param template 模板信息
     * @return 操作结果
     */
    @RequiresPermissions("config:template:edit")
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ProviderApiTemplate template) {
        return toAjax(templateService.updateTemplate(template));
    }

    /**
     * 删除API模板
     *
     * @param templateIds 模板ID数组
     * @return 操作结果
     */
    @RequiresPermissions("config:template:remove")
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds) {
        return toAjax(templateService.deleteTemplates(templateIds));
    }

    /**
     * 更新模板启用状态
     *
     * @param templateId 模板ID
     * @param isEnabled 是否启用
     * @return 操作结果
     */
    @RequiresPermissions("config:template:edit")
    @PutMapping("/status/{templateId}/{isEnabled}")
    public AjaxResult updateStatus(@PathVariable Long templateId,
                                   @PathVariable String isEnabled) {
        return toAjax(templateService.updateEnabledStatus(templateId, isEnabled));
    }

    /**
     * 测试API模板配置
     *
     * 使用场景：
     * 在保存模板前测试配置是否正确，验证API调用是否成功。
     *
     * @param request 包含template和testContext的请求体
     * @return 测试结果
     */
    @RequiresPermissions("config:template:test")
    @PostMapping("/test")
    public AjaxResult test(@Valid @RequestBody ProviderApiTemplateTestReq request) {
        String result = templateService.testTemplate(request.getTemplate(), request.getTestContext());
        return success(result);
    }

    /**
     * 批量导入API模板
     *
     * 使用场景：
     * 导入预设模板或从文件批量导入模板。
     *
     * @param templates 模板列表
     * @return 导入结果
     */
    @RequiresPermissions("config:template:import")
    @PostMapping("/import")
    public AjaxResult importTemplates(@Valid @RequestBody List<@Valid ProviderApiTemplate> templates) {
        if (templates == null || templates.isEmpty()) {
            throw new ServiceException("Template list must not be empty.");
        }
        int count = templateService.batchImportTemplates(templates);
        return success("成功导入" + count + "个模板");
    }

    /**
     * 检查模板是否存在
     *
     * @param configType 配置类型
     * @param providerCode Provider编码
     * @param operation 操作类型
     * @return 是否存在
     */
    @GetMapping("/exists")
    public AjaxResult exists(@RequestParam String configType,
                            @RequestParam String providerCode,
                            @RequestParam String operation) {
        boolean exists = templateService.templateExists(configType, providerCode, operation);
        return success(exists);
    }
}
