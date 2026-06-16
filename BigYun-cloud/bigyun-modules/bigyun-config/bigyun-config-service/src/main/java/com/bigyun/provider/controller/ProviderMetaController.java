package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.provider.domain.ProviderMeta;
import com.bigyun.provider.manager.ProviderEnumManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Provider 元数据控制器
 * 提供配置类型和 Provider 列表的查询接口
 *
 * @author bigyun
 */
@Tag(name = "Provider 元数据管理", description = "提供配置类型和 Provider 列表查询")
@RestController
@RequestMapping("/meta")
public class ProviderMetaController extends BaseController {

    @Autowired
    private ProviderEnumManager providerEnumManager;

    /**
     * 查询所有配置类型
     */
    @Operation(summary = "查询所有配置类型", description = "获取系统支持的所有配置类型列表")
    @GetMapping("/types")
    public AjaxResult getConfigTypes() {
        List<ProviderMeta> types = providerEnumManager.getAllConfigTypes();
        return success(types);
    }

    /**
     * 根据配置类型查询 Provider 列表
     */
    @Operation(summary = "根据配置类型查询 Provider 列表", description = "根据指定的配置类型获取对应的 Provider 列表")
    @Parameter(name = "configType", description = "配置类型", required = true)
    @GetMapping("/providers/{configType}")
    public AjaxResult getProvidersByType(@PathVariable String configType) {
        List<ProviderMeta> providers = providerEnumManager.getProvidersByType(configType);
        return success(providers);
    }
}
