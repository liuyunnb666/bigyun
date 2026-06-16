package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.config.domain.CapabilityModelCompareVO;
import com.bigyun.config.domain.CapabilityModelRelationDTO;
import com.bigyun.config.domain.CapabilityModelSwitchReq;
import com.bigyun.provider.domain.ProviderCapability;
import com.bigyun.provider.service.IProviderCapabilityModelRelationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/capability/model")
public class ProviderCapabilityRelationController extends BaseController
{
    private final IProviderCapabilityModelRelationService relationService;

    public ProviderCapabilityRelationController(IProviderCapabilityModelRelationService relationService)
    {
        this.relationService = relationService;
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/candidates")
    public AjaxResult candidates(ProviderCapability query)
    {
        List<ProviderCapability> list = relationService.selectCandidateCapabilityList(query);
        return success(list);
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/relations")
    public TableDataInfo relationList(CapabilityModelRelationDTO query)
    {
        startPage();
        return getDataTable(relationService.selectRelationList(query));
    }

    @RequiresPermissions("provider:config:query")
    @GetMapping("/relation/{relationId}")
    public AjaxResult relationDetail(@PathVariable Long relationId)
    {
        return success(relationService.selectRelationById(relationId));
    }

    @RequiresPermissions("provider:config:query")
    @GetMapping("/compare")
    public AjaxResult compare(@RequestParam Long leftCapabilityId, @RequestParam Long rightCapabilityId)
    {
        CapabilityModelCompareVO compareVO = relationService.compare(leftCapabilityId, rightCapabilityId);
        return success(compareVO);
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力关系", businessType = BusinessType.INSERT)
    @PostMapping("/relation")
    public AjaxResult addRelation(@Valid @RequestBody CapabilityModelRelationDTO relation)
    {
        relationService.insertRelation(relation, SecurityUtils.getUsername());
        return success("能力关系保存成功。");
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力关系", businessType = BusinessType.UPDATE)
    @PutMapping("/relation")
    public AjaxResult editRelation(@Valid @RequestBody CapabilityModelRelationDTO relation)
    {
        relationService.updateRelation(relation, SecurityUtils.getUsername());
        return success("能力关系更新成功。");
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力关系", businessType = BusinessType.DELETE)
    @DeleteMapping("/relation/{relationIds}")
    public AjaxResult removeRelation(@PathVariable Long[] relationIds)
    {
        relationService.deleteRelationByIds(relationIds);
        return success();
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力关系", businessType = BusinessType.UPDATE)
    @PutMapping("/switch")
    public AjaxResult switchDefaultModel(@Valid @RequestBody CapabilityModelSwitchReq request)
    {
        relationService.switchDefaultModel(request, SecurityUtils.getUsername());
        return success("默认模型切换成功，并已刷新运行时快照。");
    }
}
