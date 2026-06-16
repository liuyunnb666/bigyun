package com.bigyun.provider.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderCapabilityLogDTO;
import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.provider.domain.GenericResponse;
import com.bigyun.provider.domain.ProviderCapabilityLog;
import com.bigyun.provider.mapper.ProviderCapabilityLogMapper;
import com.bigyun.provider.service.IProviderCapabilityLogService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Provider 能力调用日志服务实现。
 * <p>
 * 记录能力调用成功、失败、耗时、运行时来源和响应摘要，便于排查 Provider 路由与第三方调用问题。
 */
@Service
public class ProviderCapabilityLogServiceImpl implements IProviderCapabilityLogService
{
    private static final Logger log = LoggerFactory.getLogger(ProviderCapabilityLogServiceImpl.class);

    private final ProviderCapabilityLogMapper capabilityLogMapper;

    public ProviderCapabilityLogServiceImpl(ProviderCapabilityLogMapper capabilityLogMapper)
    {
        this.capabilityLogMapper = capabilityLogMapper;
    }

    /**
     * 按筛选条件查询能力调用日志。
     *
     * @param query 查询条件，支持能力编码、配置类型、Provider、操作、模型、运行时来源和状态
     * @return 调用日志列表
     */
    @Override
    public java.util.List<ProviderCapabilityLog> selectLogList(ProviderCapabilityLog query)
    {
        QueryWrapper<ProviderCapabilityLog> wrapper = new QueryWrapper<>();
        wrapper.select("*");
        if (query != null)
        {
            wrapper.like(StringUtils.isNotBlank(query.getCapabilityCode()), "capability_code", query.getCapabilityCode());
            wrapper.eq(StringUtils.isNotBlank(query.getConfigType()), "config_type", query.getConfigType());
            wrapper.eq(StringUtils.isNotBlank(query.getProviderCode()), "provider_code", query.getProviderCode());
            wrapper.eq(StringUtils.isNotBlank(query.getOperation()), "operation", query.getOperation());
            wrapper.eq(StringUtils.isNotBlank(query.getModelCode()), "model_code", query.getModelCode());
            wrapper.eq(StringUtils.isNotBlank(query.getRuntimeSource()), "runtime_source", query.getRuntimeSource());
            wrapper.eq(StringUtils.isNotBlank(query.getStatus()), "status", query.getStatus());
        }
        wrapper.orderByDesc("create_time").orderByDesc("log_id");
        return capabilityLogMapper.selectList(wrapper);
    }

    /**
     * 记录 Provider 能力调用成功日志。
     * <p>
     * 会优先使用响应数据中的运行时路由字段补齐 capabilityCode、providerCode、modelCode 等信息。
     *
     * @param request 原始调用请求
     * @param response 调用成功响应
     * @param startTimeMillis 调用开始时间戳
     */
    @Override
    public void recordSuccess(ProviderExecuteRequest request, GenericResponse response, long startTimeMillis)
    {
        ProviderCapabilityLog entry = baseEntry(request, startTimeMillis);
        Map<String, Object> data = response == null ? null : response.getData();
        entry.setCapabilityCode(firstText(value(data, "capabilityCode"), entry.getCapabilityCode()));
        entry.setConfigType(firstText(value(data, "configType"), entry.getConfigType()));
        entry.setProviderCode(firstText(value(data, "providerCode"), entry.getProviderCode()));
        entry.setOperation(firstText(value(data, "operation"), entry.getOperation()));
        entry.setModelCode(value(data, "modelCode"));
        entry.setRuntimeSource(value(data, "runtimeSource"));
        entry.setFallback(value(data, "fallback"));
        entry.setDurationMs(numberValue(data, "durationMs", elapsed(startTimeMillis)));
        entry.setStatus("success");
        entry.setStatusCode(response == null || response.getStatusCode() == null ? null
                : String.valueOf(response.getStatusCode()));
        entry.setResponseSummary(responseSummary(response));
        saveQuietly(entry);
    }

    /**
     * 记录 Provider 能力调用失败日志。
     *
     * @param request 原始调用请求
     * @param exception 调用异常
     * @param startTimeMillis 调用开始时间戳
     */
    @Override
    public void recordFailure(ProviderExecuteRequest request, Exception exception, long startTimeMillis)
    {
        ProviderCapabilityLog entry = baseEntry(request, startTimeMillis);
        entry.setStatus("fail");
        entry.setErrorMessage(limit(exception == null ? null : exception.getMessage(), 500));
        saveQuietly(entry);
    }

    /**
     * 记录外部模块上报的 Provider 能力调用日志。
     * <p>
     * DTO 只接收脱敏后的路由元数据和摘要，不保存完整 prompt、密钥或第三方原始响应。
     *
     * @param logDTO 脱敏后的能力调用日志
     */
    @Override
    public void recordLog(ProviderCapabilityLogDTO logDTO)
    {
        if (logDTO == null)
        {
            return;
        }
        ProviderCapabilityLog entry = new ProviderCapabilityLog();
        entry.setCapabilityCode(limit(logDTO.getCapabilityCode(), 100));
        entry.setConfigType(limit(logDTO.getConfigType(), 64));
        entry.setProviderCode(limit(logDTO.getProviderCode(), 100));
        entry.setOperation(limit(logDTO.getOperation(), 100));
        entry.setModelCode(limit(logDTO.getModelCode(), 100));
        entry.setRuntimeSource(limit(logDTO.getRuntimeSource(), 64));
        entry.setFallback(limit(logDTO.getFallback(), 16));
        entry.setDurationMs(logDTO.getDurationMs());
        entry.setStatus(limit(StringUtils.defaultIfBlank(logDTO.getStatus(), "unknown"), 32));
        entry.setStatusCode(limit(logDTO.getStatusCode(), 64));
        entry.setErrorMessage(limit(logDTO.getErrorMessage(), 500));
        entry.setRequestSummary(limit(logDTO.getRequestSummary(), 500));
        entry.setResponseSummary(limit(logDTO.getResponseSummary(), 500));
        saveQuietly(entry);
    }

    /**
     * 构建日志基础字段。
     *
     * @param request 原始调用请求
     * @param startTimeMillis 调用开始时间戳
     * @return 日志实体
     */
    private ProviderCapabilityLog baseEntry(ProviderExecuteRequest request, long startTimeMillis)
    {
        ProviderCapabilityLog entry = new ProviderCapabilityLog();
        if (request != null)
        {
            entry.setConfigType(request.getConfigType());
            entry.setProviderCode(request.getProviderCode());
            entry.setOperation(request.getOperation());
            entry.setRequestSummary(requestSummary(request));
        }
        entry.setDurationMs(elapsed(startTimeMillis));
        return entry;
    }

    /**
     * 生成请求摘要，避免日志表保存完整请求体。
     *
     * @param request 原始调用请求
     * @return 请求摘要
     */
    private String requestSummary(ProviderExecuteRequest request)
    {
        if (request == null)
        {
            return null;
        }
        String payloadType = request.getPayload() == null ? "none" : request.getPayload().getClass().getSimpleName();
        int paramCount = request.getParams() == null ? 0 : request.getParams().size();
        return limit(String.format("payloadType=%s, legacyParamCount=%s", payloadType, paramCount), 500);
    }

    /**
     * 生成响应摘要，记录状态码、数据键数量和原始响应大小。
     *
     * @param response 调用响应
     * @return 响应摘要
     */
    private String responseSummary(GenericResponse response)
    {
        if (response == null)
        {
            return null;
        }
        int dataSize = response.getData() == null ? 0 : response.getData().size();
        return limit(String.format("statusCode=%s, dataKeys=%s, rawSize=%s",
                response.getStatusCode(), dataSize,
                response.getRawResponse() == null ? 0 : response.getRawResponse().length()), 500);
    }

    /**
     * 静默保存调用日志。
     * <p>
     * 日志写入失败不影响主业务调用，只记录 warn 日志。
     *
     * @param entry 日志实体
     */
    private void saveQuietly(ProviderCapabilityLog entry)
    {
        try
        {
            capabilityLogMapper.insert(entry);
        }
        catch (Exception e)
        {
            log.warn("Provider capability log save failed: {}", e.getMessage());
        }
    }

    /**
     * 从响应 data 中读取字符串字段。
     */
    private String value(Map<String, Object> data, String key)
    {
        if (data == null || data.get(key) == null)
        {
            return null;
        }
        return String.valueOf(data.get(key));
    }

    /**
     * 选择第一个非空文本。
     */
    private String firstText(String candidate, String fallback)
    {
        return StringUtils.isNotBlank(candidate) ? candidate : fallback;
    }

    /**
     * 从响应 data 中读取数字字段，解析失败时使用默认值。
     */
    private Long numberValue(Map<String, Object> data, String key, Long fallback)
    {
        Object value = data == null ? null : data.get(key);
        if (value instanceof Number)
        {
            return ((Number) value).longValue();
        }
        if (value != null)
        {
            try
            {
                return Long.parseLong(String.valueOf(value));
            }
            catch (NumberFormatException ignored)
            {
                return fallback;
            }
        }
        return fallback;
    }

    /**
     * 计算从开始时间到当前时间的耗时。
     */
    private Long elapsed(long startTimeMillis)
    {
        return Math.max(System.currentTimeMillis() - startTimeMillis, 0L);
    }

    /**
     * 截断长文本，避免日志摘要字段过长。
     */
    private String limit(String value, int maxLength)
    {
        if (value == null || value.length() <= maxLength)
        {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
