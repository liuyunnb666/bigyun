package com.bigyun.provider.controller;

import com.bigyun.common.core.web.controller.BaseController;
import com.bigyun.common.core.web.domain.AjaxResult;
import com.bigyun.common.core.web.page.TableDataInfo;
import com.bigyun.common.log.annotation.Log;
import com.bigyun.common.log.enums.BusinessType;
import com.bigyun.common.security.annotation.InnerAuth;
import com.bigyun.common.security.annotation.RequiresPermissions;
import com.bigyun.common.security.utils.SecurityUtils;
import com.bigyun.common.core.domain.R;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.enums.ProviderCodeEnum;
import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.config.domain.ProviderCapabilityLogDTO;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.provider.cache.CacheInvalidator;
import com.bigyun.provider.db.snapshot.ProviderRuntimeSnapshotPublisher;
import com.bigyun.provider.domain.GenericResponse;
import com.bigyun.provider.domain.ProviderCapability;
import com.bigyun.provider.domain.ProviderConfig;
import com.bigyun.provider.domain.ProviderFieldOptionVO;
import com.bigyun.provider.domain.ProviderFieldVO;
import com.bigyun.provider.domain.ProviderOptionVO;
import com.bigyun.provider.core.ProviderRuntimeExecutor;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshot;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotKeys;
import com.bigyun.provider.core.snapshot.ProviderRuntimeSnapshotStore;
import com.bigyun.provider.manager.ProviderFieldLoader;
import com.bigyun.provider.domain.ProviderCapabilityLog;
import com.bigyun.provider.domain.ProviderRuntimeSnapshotVO;
import com.bigyun.provider.service.IProviderCapabilityLogService;
import com.bigyun.provider.service.IProviderConfigService;
import com.bigyun.provider.service.IProviderCapabilityService;
import com.bigyun.provider.service.IProviderRecommendationService;
import com.alibaba.fastjson2.JSON;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provider 统一配置管理控制器。
 * <p>
 * 同时支持内部服务调用（带 {@link InnerAuth} 的接口）和前端管理页面调用
 *（带 {@link RequiresPermissions} 的接口）。
 * </p>
 *
 * @author bigyun
 */
@RestController
@RequestMapping("/config")
public class ProviderConfigController extends BaseController
{
    /** Provider 配置服务，负责配置查询、增删改和默认配置切换。 */
    private final IProviderConfigService providerConfigService;

    private final CacheInvalidator cacheInvalidator;

    private final ProviderFieldLoader providerFieldLoader;

    private final IProviderCapabilityService providerCapabilityService;

    private final IProviderRecommendationService providerRecommendationService;

    private final ProviderRuntimeExecutor providerRuntimeExecutor;

    private final ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher;

    private final ProviderRuntimeSnapshotStore runtimeSnapshotStore;

    private final IProviderCapabilityLogService providerCapabilityLogService;

    /**
     * 构造方法注入 Provider 配置相关依赖。
     *
     * @param providerConfigService Provider 配置服务
     * @param cacheInvalidator Provider 配置缓存失效通知器
     * @param providerFieldLoader Provider 动态字段加载器
     * @param providerCapabilityService Provider 能力服务
     * @param providerRecommendationService Provider 推荐服务
     */
    public ProviderConfigController(IProviderConfigService providerConfigService,
                                    CacheInvalidator cacheInvalidator,
                                    ProviderFieldLoader providerFieldLoader,
                                    IProviderCapabilityService providerCapabilityService,
                                    IProviderRecommendationService providerRecommendationService,
                                    ProviderRuntimeExecutor providerRuntimeExecutor,
                                    ProviderRuntimeSnapshotPublisher runtimeSnapshotPublisher,
                                    ProviderRuntimeSnapshotStore runtimeSnapshotStore,
                                    IProviderCapabilityLogService providerCapabilityLogService)
    {
        this.providerConfigService = providerConfigService;
        this.cacheInvalidator = cacheInvalidator;
        this.providerFieldLoader = providerFieldLoader;
        this.providerCapabilityService = providerCapabilityService;
        this.providerRecommendationService = providerRecommendationService;
        this.providerRuntimeExecutor = providerRuntimeExecutor;
        this.runtimeSnapshotPublisher = runtimeSnapshotPublisher;
        this.runtimeSnapshotStore = runtimeSnapshotStore;
        this.providerCapabilityLogService = providerCapabilityLogService;
    }

    /**
     * 分页查询 Provider 配置列表。
     * <p>支持按 configType、providerCode、providerName、status 等条件筛选。</p>
     *
     * @param query 查询条件
     */
    @RequiresPermissions("provider:config:list")
    @GetMapping("/list")
    public TableDataInfo list(ProviderConfig query)
    {
        startPage();
        List<ProviderConfig> list = providerConfigService.selectProviderConfigList(query);
        return getDataTable(list);
    }

    /**
     * 根据 ID 查询单个 Provider 配置详情。
     * <p>返回的配置会自动脱敏敏感字段，如 accessKey、secretKey。</p>
     *
     * @param configId 配置 ID
     * @return 脱敏后的配置详情
     */
    @RequiresPermissions("provider:config:query")
    @GetMapping("/{configId}")
    public AjaxResult getInfo(@PathVariable Long configId)
    {
        return success(providerConfigService.selectProviderConfigById(configId));
    }

    @RequiresPermissions("provider:config:query")
    @GetMapping("/detail")
    public AjaxResult getInfoByQuery(@RequestParam Long configId)
    {
        return success(providerConfigService.selectProviderConfigById(configId));
    }

    /**
     * 查询指定配置类型的默认 Provider 配置。
     *
     * @param configType 配置类型，例如 llm、storage
     * @return 当前类型的默认 Provider 配置
     */
    @RequiresPermissions("provider:config:query")
    @GetMapping("/default/{configType}")
    public AjaxResult getDefault(@PathVariable String configType)
    {
        return success(providerConfigService.selectDefaultProviderConfig(configType));
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/recommendations/{configType}")
    public AjaxResult recommendations(@PathVariable String configType)
    {
        return success(providerRecommendationService.selectRecommendations(configType));
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/recommendations")
    public AjaxResult recommendationsByQuery(@RequestParam String configType)
    {
        return success(providerRecommendationService.selectRecommendations(configType));
    }

    /**
     * 获取指定配置类型下可选的 Provider 列表。
     * <p>
     * 数据来源是后端 {@link ProviderCodeEnum} 枚举，前端通过该接口渲染 Provider 下拉框，
     * 避免前端硬编码导致页面可选项和后端真实支持项不一致。
     * </p>
     *
     * @param configType 配置类型，例如 llm、storage
     * @return 当前配置类型支持的 Provider 选项
     */
    @RequiresPermissions("provider:config:query")
    @GetMapping({"/providers/{configType}", "/meta/providers/{configType}"})
    public AjaxResult providers(@PathVariable String configType)
    {
        return buildProviderOptions(configType);
    }

    @RequiresPermissions("provider:config:query")
    @GetMapping({"/providers", "/meta/providers"})
    public AjaxResult providersByQuery(@RequestParam String configType)
    {
        return buildProviderOptions(configType);
    }

    private AjaxResult buildProviderOptions(String configType)
    {
        List<ProviderOptionVO> result = new ArrayList<>();
        for (ProviderCodeEnum provider : ProviderCodeEnum.values())
        {
            if (!provider.getType().getCode().equals(configType))
            {
                continue;
            }
            ProviderOptionVO item = new ProviderOptionVO();
            item.setCode(provider.getCode());
            item.setProviderCode(provider.getCode());
            item.setName(provider.getName());
            item.setProviderName(provider.getName());
            item.setType(provider.getType().getCode());
            result.add(item);
        }
        return success(result);
    }

    /**
     * 获取指定 Provider 的动态字段元数据。
     * <p>
     * 字段主体来自 provider_field_config 表，通过 ProviderFieldLoader 读取内存缓存；
     * options_json 在这里解析成 options 返回给前端。如果 JSON 格式异常，则保留字段主体并返回空候选项，
     * 让页面仍可手动输入，不会因为单条候选项配置错误导致整体不可用。
     * </p>
     *
     * @param providerCode Provider 编码，例如 aliyun-qwen、deepseek
     * @return 字段定义列表，包含提示、默认值、帮助文案和候选项
     */
    @RequiresPermissions("provider:config:query")
    @GetMapping("/fields/{providerCode}")
    public AjaxResult fields(@PathVariable String providerCode)
    {
        return buildProviderFields(providerCode);
    }

    @RequiresPermissions("provider:config:query")
    @GetMapping("/fields")
    public AjaxResult fieldsByQuery(@RequestParam String providerCode)
    {
        return buildProviderFields(providerCode);
    }

    private AjaxResult buildProviderFields(String providerCode)
    {
        providerFieldLoader.refresh();
        List<ProviderFieldVO> result = new ArrayList<>();
        for (ProviderFieldLoader.FieldDef fieldDef : providerFieldLoader.getFields(providerCode))
        {
            ProviderFieldVO field = new ProviderFieldVO();
            field.setKey(fieldDef.getKey());
            field.setLabel(fieldDef.getLabel());
            field.setType(fieldDef.getType());
            field.setRequired(fieldDef.getRequired() != null && fieldDef.getRequired() == 1);
            field.setPlaceholder(fieldDef.getPlaceholder());
            field.setDefaultValue(fieldDef.getDefaultValue());
            field.setHelpText(fieldDef.getHelpText());
            field.setSortOrder(fieldDef.getSortOrder());
            field.setOptions(parseFieldOptions(fieldDef.getOptionsJson()));
            result.add(field);
        }
        result.sort(Comparator.comparing(ProviderFieldVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)));
        return success(result);
    }

    /**
     * 解析字段候选项 JSON。
     * <p>
     * 支持 provider_field_config.options_json 存储标准 JSON 数组：
     * [{"label":"模型名称","value":"model-id","helpText":"说明"}]。
     * 解析失败时返回空列表，交给前端可输入下拉框兜底，保证配置体验不中断。
     * </p>
     *
     * @param optionsJson 数据库中保存的候选项 JSON
     * @return 可返回给前端的候选项列表
     */
    private List<ProviderFieldOptionVO> parseFieldOptions(String optionsJson)
    {
        if (optionsJson == null || optionsJson.trim().isEmpty())
        {
            return new ArrayList<>();
        }
        try
        {
            List<ProviderFieldOptionVO> parsed = JSON.parseArray(optionsJson, ProviderFieldOptionVO.class);
            return parsed == null ? new ArrayList<ProviderFieldOptionVO>() : parsed;
        }
        catch (Exception e)
        {
            return new ArrayList<>();
        }
    }

    /**
     * 内部接口：查询指定类型的默认 Provider 配置。
     *
     * @param configType 配置类型
     * @return 包含完整配置的统一响应
     */
    @InnerAuth
    @GetMapping("/internal/default/{configType}")
    public R<ProviderConfigDTO> getDefaultInternal(@PathVariable String configType)
    {
        ProviderConfigDTO config = providerConfigService.selectDefaultProviderConfigInternal(configType);
        return R.ok(config);
    }

    /**
     * 内部接口：查询指定类型下已启用的 Provider 配置列表。
     *
     * @param configType 配置类型
     * @return 已启用配置列表的统一响应
     */
    @InnerAuth
    @GetMapping("/internal/enabled/{configType}")
    public R<List<ProviderConfigDTO>> listEnabledInternal(@PathVariable String configType)
    {
        return R.ok(providerConfigService.selectEnabledProviderConfigsInternal(configType));
    }

    /**
     * 查询指定能力编码的当前默认 Provider 绑定。
     */
    @GetMapping("/capability/{capabilityCode}")
    public AjaxResult getCapability(@PathVariable String capabilityCode)
    {
        return success(providerCapabilityService.selectDefaultCapability(capabilityCode));
    }

    /**
     * 分页查询 Provider 能力绑定列表。
     * <p>
     * 后台能力中心通过该接口查看 capability 当前可选 Provider、模型、默认状态和启停状态。
     */
    @RequiresPermissions("provider:config:list")
    @GetMapping("/capability/list")
    public TableDataInfo capabilityList(ProviderCapability query)
    {
        startPage();
        return getDataTable(providerCapabilityService.selectCapabilityList(query));
    }

    /**
     * 查询单条 Provider 能力绑定详情。
     */
    @RequiresPermissions("provider:config:query")
    @GetMapping("/capability/detail")
    public AjaxResult capabilityDetail(@RequestParam Long capabilityId)
    {
        return success(providerCapabilityService.selectCapabilityById(capabilityId));
    }

    @RequiresPermissions("provider:config:add")
    @Log(title = "Provider 能力定义", businessType = BusinessType.INSERT)
    @PostMapping("/capability/add")
    public AjaxResult addCapability(@Valid @RequestBody ProviderCapability capability)
    {
        capability.setCreateBy(SecurityUtils.getUsername());
        return toAjax(providerCapabilityService.insertCapability(capability));
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力定义", businessType = BusinessType.UPDATE)
    @PutMapping("/capability/edit")
    public AjaxResult editCapability(@Valid @RequestBody ProviderCapability capability)
    {
        capability.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(providerCapabilityService.updateCapability(capability));
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力定义", businessType = BusinessType.UPDATE)
    @PutMapping("/capability/status")
    public AjaxResult changeCapabilityStatus(@RequestParam Long capabilityId, @RequestParam String status)
    {
        return toAjax(providerCapabilityService.updateCapabilityStatus(capabilityId, status, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("provider:config:remove")
    @Log(title = "Provider 能力定义", businessType = BusinessType.DELETE)
    @DeleteMapping("/capability/delete")
    public AjaxResult removeCapability(@RequestParam Long[] capabilityIds)
    {
        providerCapabilityService.deleteCapabilityByIds(capabilityIds);
        return success();
    }

    /**
     * 设置指定能力绑定为当前默认项。
     * <p>
     * 切换后通过 Redis 通知运行时刷新同类 Provider 配置缓存。
     */
    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 能力", businessType = BusinessType.UPDATE)
    @PutMapping("/capability/default")
    public AjaxResult setDefaultCapability(@RequestParam Long capabilityId)
    {
        ProviderCapability current = providerCapabilityService.selectCapabilityById(capabilityId);
        int rows = providerCapabilityService.setDefaultCapability(capabilityId, SecurityUtils.getUsername());
        if (rows <= 0)
        {
            return error();
        }
        boolean refreshed = cacheInvalidator.notify(current == null ? null : current.getConfigType());
        if (!refreshed)
        {
            return success("Default provider capability switched, but cache refresh notification failed. Runtime cache may take up to 5 minutes to pick up the new binding.");
        }
        return success("Default provider capability switched and cache refresh notification sent.");
    }

    /**
     * Provider 能力调用日志预留接口。
     * <p>
     * 当前先返回空分页，后续日志表落地后在这里接入真实调用记录。
     */
    @RequiresPermissions("provider:config:list")
    @GetMapping("/capability/logs")
    public TableDataInfo capabilityLogs(ProviderCapabilityLog query)
    {
        startPage();
        return getDataTable(providerCapabilityLogService.selectLogList(query));
    }

    /**
     * 内部接口：查询指定能力编码的当前默认 Provider 绑定。
     */
    @InnerAuth
    @GetMapping("/internal/capability/{capabilityCode}")
    public R<ProviderCapabilityDTO> getCapabilityInternal(@PathVariable String capabilityCode)
    {
        return R.ok(providerCapabilityService.selectDefaultCapability(capabilityCode));
    }

    /**
     * 内部接口：记录 Provider 能力调用审计日志。
     */
    @InnerAuth
    @PostMapping("/internal/capability-log")
    public R<Void> recordCapabilityLogInternal(@Valid @RequestBody ProviderCapabilityLogDTO logDTO)
    {
        providerCapabilityLogService.recordLog(logDTO);
        return R.ok();
    }

    /**
     * 内部接口：执行 Provider 请求。
     * <p>
     * 通过 provider-core 运行时入口执行，保留旧 configType/providerCode/operation 调用形态。
     */
    @InnerAuth
    @PostMapping("/internal/execute")
    public R<GenericResponse> executeInternal(@Valid @RequestBody ProviderExecuteRequest executeRequest)
    {
        if (executeRequest == null)
        {
            return R.fail("Provider execute request must not be null.");
        }
        // 调用日志由 DbProviderRuntimeExecutor 发布 ProviderExecutedEvent 统一记录，
        // 不再在此处直接写库，避免重复日志。
        GenericResponse response = providerRuntimeExecutor.execute(executeRequest.getConfigType(),
                executeRequest.getProviderCode(), executeRequest.getOperation(), executeRequest.getPayload());
        return R.ok(response);
    }

    /**
     * 内部接口：手动重建 Provider 运行时 Redis 快照。
     * <p>
     * 可按 capabilityCode 精确刷新，也可按 configType 刷新；都为空时刷新全部。
     */
    @InnerAuth
    @PostMapping("/internal/runtime-cache/refresh")
    public R<Integer> refreshRuntimeCacheInternal(@RequestParam(required = false) String configType,
                                                  @RequestParam(required = false) String capabilityCode)
    {
        return R.ok(refreshRuntimeSnapshot(configType, capabilityCode));
    }

    /**
     * 新增 Provider 配置。
     *
     * @param config Provider 配置对象，需要通过参数校验
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:add")
    @Log(title = "Provider 配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody ProviderConfig config)
    {
        config.setCreateBy(SecurityUtils.getUsername());
        return toAjax(providerConfigService.insertProviderConfig(config));
    }

    /**
     * 编辑 Provider 配置。
     *
     * @param config Provider 配置对象，需要通过参数校验
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody ProviderConfig config)
    {
        config.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(providerConfigService.updateProviderConfig(config));
    }

    /**
     * 修改 Provider 配置状态（启用或停用）。
     *
     * @param configId 配置 ID
     * @param status 目标状态，0 表示正常，1 表示停用
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置", businessType = BusinessType.UPDATE)
    @PutMapping("/{configId}/status/{status}")
    public AjaxResult changeStatus(@PathVariable Long configId, @PathVariable String status)
    {
        return toAjax(providerConfigService.updateProviderConfigStatus(configId, status, SecurityUtils.getUsername()));
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatusByQuery(@RequestParam Long configId, @RequestParam String status)
    {
        return toAjax(providerConfigService.updateProviderConfigStatus(configId, status, SecurityUtils.getUsername()));
    }

    /**
     * 设置指定 Provider 配置为默认配置。
     *
     * @param configId 配置 ID
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置", businessType = BusinessType.UPDATE)
    @PutMapping("/{configId}/default")
    public AjaxResult setDefault(@PathVariable Long configId)
    {
        return setDefaultProviderConfig(configId);
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置", businessType = BusinessType.UPDATE)
    @PutMapping("/default")
    public AjaxResult setDefaultByQuery(@RequestParam Long configId)
    {
        return setDefaultProviderConfig(configId);
    }

    private AjaxResult setDefaultProviderConfig(Long configId)
    {
        ProviderConfig current = providerConfigService.selectProviderConfigById(configId);
        int rows = providerConfigService.setDefaultProviderConfig(configId, SecurityUtils.getUsername());
        if (rows <= 0)
        {
            return error();
        }
        boolean refreshed = cacheInvalidator.notify(current == null ? null : current.getConfigType());
        if (!refreshed)
        {
            return success("Default provider config switched, but cache refresh notification failed. file-service may take up to 5 minutes to pick up the new default.");
        }
        return success("Default provider config switched and cache refresh notification sent.");
    }

    /**
     * 测试 Provider 连接。
     *
     * @param config 待测试的 Provider 配置
     * @return 测试结果
     */
    @RequiresPermissions("provider:config:query")
    @PostMapping("/test")
    public AjaxResult testConnection(@Valid @RequestBody ProviderConfig config)
    {
        boolean valid = providerConfigService.testProviderConnection(config);
        return valid ? success("Provider connection test passed.") : error("Provider connection test failed.");
    }

    /**
     * 刷新指定类型的 Provider 配置缓存。
     * <p>通过 Redis Pub/Sub 向所有服务实例广播缓存失效通知，
     * 各实例收到通知后会刷新本地 Caffeine 缓存。</p>
     *
     * @param configType 配置类型
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 配置缓存", businessType = BusinessType.UPDATE)
    @PostMapping("/cache/refresh/{configType}")
    public AjaxResult refreshCache(@PathVariable String configType)
    {
        runtimeSnapshotPublisher.publishConfigType(configType);
        boolean refreshed = cacheInvalidator.notify(configType);
        if (!refreshed)
        {
            return AjaxResult.warn(String.format("Cache refresh notification failed for configType=%s. Please check Redis and file-service cache listener; cache may take up to 5 minutes to expire.", configType));
        }
        return AjaxResult.success(String.format("Cache refresh notification sent for configType=%s.", configType));
    }

    @RequiresPermissions("provider:config:edit")
    @Log(title = "Provider 运行时快照", businessType = BusinessType.UPDATE)
    @PostMapping("/runtime-cache/refresh")
    public AjaxResult refreshRuntimeCache(@RequestParam(required = false) String configType,
                                          @RequestParam(required = false) String capabilityCode)
    {
        int count = refreshRuntimeSnapshot(configType, capabilityCode);
        return AjaxResult.success(String.format("Provider runtime snapshot refreshed, count=%s.", count));
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/runtime-cache/snapshot")
    public AjaxResult runtimeSnapshot(@RequestParam(required = false) String capabilityCode,
                                      @RequestParam(required = false) String configType,
                                      @RequestParam(required = false) String providerCode,
                                      @RequestParam(required = false) String operation)
    {
        Optional<ProviderRuntimeSnapshot> snapshot;
        String source;
        if (StringUtils.isNotBlank(capabilityCode))
        {
            snapshot = runtimeSnapshotStore.getByCapability(capabilityCode);
            source = "redis-capability";
        }
        else
        {
            snapshot = runtimeSnapshotStore.getByProvider(configType, providerCode, operation);
            source = "redis-provider";
        }
        return success(snapshot.map(item -> toSnapshotVO(item, source))
                .orElseGet(() -> emptySnapshotVO(capabilityCode, configType, providerCode, operation)));
    }

    @RequiresPermissions("provider:config:list")
    @GetMapping("/runtime-cache/snapshots")
    public AjaxResult runtimeSnapshots(@RequestParam(required = false) String capabilityCode,
                                       @RequestParam(required = false) String configType,
                                       @RequestParam(required = false) String providerCode,
                                       @RequestParam(required = false) String operation)
    {
        List<ProviderRuntimeSnapshotVO> result = new ArrayList<>();
        for (ProviderRuntimeSnapshotStore.SnapshotEntry entry : runtimeSnapshotStore.listSnapshots())
        {
            ProviderRuntimeSnapshotVO vo = toSnapshotVO(entry.getSnapshot(), runtimeSourceFromKey(entry.getRedisKey()));
            if (matchesSnapshot(vo, capabilityCode, configType, providerCode, operation))
            {
                result.add(vo);
            }
        }
        result.sort(this::compareSnapshot);
        return success(result);
    }

    private ProviderRuntimeSnapshotVO toSnapshotVO(ProviderRuntimeSnapshot snapshot, String runtimeSource)
    {
        ProviderRuntimeSnapshotVO vo = new ProviderRuntimeSnapshotVO();
        vo.setCapabilityCode(snapshot.getCapabilityCode());
        vo.setConfigType(snapshot.getConfigType());
        vo.setProviderCode(snapshot.getProviderCode());
        vo.setOperation(snapshot.getOperation());
        vo.setModelCode(snapshot.getModelCode());
        vo.setRuntimeSource(runtimeSource);
        vo.setVersion(snapshot.getVersion());
        vo.setPublishedAt(snapshot.getPublishedAt());
        vo.setHasProviderConfig(snapshot.getProviderConfig() != null);
        vo.setHasApiTemplate(snapshot.getApiTemplate() != null);
        return vo;
    }

    private String runtimeSourceFromKey(String redisKey)
    {
        if (StringUtils.startsWith(redisKey, ProviderRuntimeSnapshotKeys.CAPABILITY_PREFIX))
        {
            return "redis-capability";
        }
        if (StringUtils.startsWith(redisKey, ProviderRuntimeSnapshotKeys.PROVIDER_PREFIX))
        {
            return "redis-provider";
        }
        return "redis-runtime";
    }

    private boolean matchesSnapshot(ProviderRuntimeSnapshotVO vo, String capabilityCode, String configType,
                                    String providerCode, String operation)
    {
        return matchesText(capabilityCode, vo.getCapabilityCode())
                && matchesText(configType, vo.getConfigType())
                && matchesText(providerCode, vo.getProviderCode())
                && matchesText(operation, vo.getOperation());
    }

    private boolean matchesText(String expected, String actual)
    {
        return StringUtils.isBlank(expected) || StringUtils.equalsIgnoreCase(StringUtils.trim(expected), actual);
    }

    private int compareSnapshot(ProviderRuntimeSnapshotVO left, ProviderRuntimeSnapshotVO right)
    {
        int publishedCompare = Long.compare(defaultLong(right.getPublishedAt()), defaultLong(left.getPublishedAt()));
        if (publishedCompare != 0)
        {
            return publishedCompare;
        }
        int capabilityCompare = compareText(left.getCapabilityCode(), right.getCapabilityCode());
        if (capabilityCompare != 0)
        {
            return capabilityCompare;
        }
        int typeCompare = compareText(left.getConfigType(), right.getConfigType());
        if (typeCompare != 0)
        {
            return typeCompare;
        }
        int providerCompare = compareText(left.getProviderCode(), right.getProviderCode());
        if (providerCompare != 0)
        {
            return providerCompare;
        }
        return compareText(left.getOperation(), right.getOperation());
    }

    private long defaultLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private int compareText(String left, String right)
    {
        return StringUtils.defaultString(left).compareTo(StringUtils.defaultString(right));
    }

    private ProviderRuntimeSnapshotVO emptySnapshotVO(String capabilityCode, String configType,
                                                     String providerCode, String operation)
    {
        ProviderRuntimeSnapshotVO vo = new ProviderRuntimeSnapshotVO();
        vo.setCapabilityCode(capabilityCode);
        vo.setConfigType(configType);
        vo.setProviderCode(providerCode);
        vo.setOperation(operation);
        vo.setRuntimeSource("not-found");
        vo.setHasProviderConfig(false);
        vo.setHasApiTemplate(false);
        return vo;
    }

    private int refreshRuntimeSnapshot(String configType, String capabilityCode)
    {
        int count;
        if (StringUtils.isNotBlank(capabilityCode))
        {
            count = runtimeSnapshotPublisher.publishCapability(capabilityCode) ? 1 : 0;
        }
        else if (StringUtils.isNotBlank(configType))
        {
            count = runtimeSnapshotPublisher.publishConfigType(configType);
        }
        else
        {
            count = runtimeSnapshotPublisher.publishAll();
        }
        cacheInvalidator.notify(configType);
        return count;
    }

    /**
     * 批量删除 Provider 配置。
     *
     * @param configIds 要删除的配置 ID 数组
     * @return 操作结果
     */
    @RequiresPermissions("provider:config:remove")
    @Log(title = "Provider 配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public AjaxResult remove(@PathVariable Long[] configIds)
    {
        providerConfigService.deleteProviderConfigByIds(configIds);
        return success();
    }

    @RequiresPermissions("provider:config:remove")
    @Log(title = "Provider 配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete")
    public AjaxResult removeByQuery(@RequestParam Long[] configIds)
    {
        providerConfigService.deleteProviderConfigByIds(configIds);
        return success();
    }
}
