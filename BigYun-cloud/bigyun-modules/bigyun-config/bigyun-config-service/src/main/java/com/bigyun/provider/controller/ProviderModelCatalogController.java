package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.provider.domain.ProviderModelCatalog;
import com.bigyun.provider.service.IProviderModelCatalogService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/model-catalog")
public class ProviderModelCatalogController extends BaseController
{
    private final IProviderModelCatalogService modelCatalogService;

    public ProviderModelCatalogController(IProviderModelCatalogService modelCatalogService)
    {
        this.modelCatalogService = modelCatalogService;
    }

    @RequiresPermissions("provider:model:list")
    @GetMapping("/list")
    public TableDataInfo list(ProviderModelCatalog query)
    {
        startPage();
        return getDataTable(modelCatalogService.selectModelCatalogList(query));
    }

    @RequiresPermissions("provider:model:query")
    @GetMapping("/detail")
    public AjaxResult detail(@RequestParam Long modelId)
    {
        return success(modelCatalogService.selectModelCatalogById(modelId));
    }

    @RequiresPermissions("provider:model:add")
    @Log(title = "Provider 模型目录", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult add(@Valid @RequestBody ProviderModelCatalog modelCatalog)
    {
        modelCatalog.setCreateBy(SecurityUtils.getUsername());
        return toAjax(modelCatalogService.insertModelCatalog(modelCatalog));
    }

    @RequiresPermissions("provider:model:edit")
    @Log(title = "Provider 模型目录", businessType = BusinessType.UPDATE)
    @PutMapping("/edit")
    public AjaxResult edit(@Valid @RequestBody ProviderModelCatalog modelCatalog)
    {
        modelCatalog.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(modelCatalogService.updateModelCatalog(modelCatalog));
    }

    @RequiresPermissions("provider:model:edit")
    @Log(title = "Provider 模型目录", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult status(@RequestParam Long modelId, @RequestParam String isEnabled)
    {
        return toAjax(modelCatalogService.updateModelCatalogStatus(modelId, isEnabled, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("provider:model:remove")
    @Log(title = "Provider 模型目录", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete")
    public AjaxResult delete(@RequestParam Long[] modelIds)
    {
        modelCatalogService.deleteModelCatalogByIds(modelIds);
        return success();
    }
}
