package com.bigyun.provider.service.handler;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.RawProviderPayload;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.config.service.handler.IProviderHandler;
import com.bigyun.provider.core.ProviderRuntimeInvoker;
import com.bigyun.provider.domain.GenericRequest;
import com.bigyun.provider.domain.GenericResponse;
import com.bigyun.provider.domain.ProviderApiTemplate;
import com.bigyun.provider.service.IProviderApiTemplateService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 通用配置驱动 Handler。
 * <p>
 * 该 Handler 仍作为旧 {@code ProviderHandlerFactory} 的兜底入口存在，用来兼容历史
 * {@code configType/providerCode/operation} 调用链；真正的模板渲染、鉴权、HTTP 调用和响应解析统一委托
 * {@link ProviderRuntimeInvoker}，避免 config-service 与 provider-core 维护两套运行时代码。
 * </p>
 */
@Component
public class GenericConfigDrivenHandler implements IProviderHandler<GenericRequest, GenericResponse>
{
    private static final Logger log = LoggerFactory.getLogger(GenericConfigDrivenHandler.class);

    private final IProviderApiTemplateService templateService;

    private final ProviderRuntimeInvoker runtimeInvoker;

    public GenericConfigDrivenHandler(IProviderApiTemplateService templateService,
                                      ProviderRuntimeInvoker runtimeInvoker)
    {
        this.templateService = templateService;
        this.runtimeInvoker = runtimeInvoker;
    }

    /**
     * 通过 provider-core 执行通用 Provider 请求。
     */
    @Override
    public ProviderResponse<GenericResponse> execute(ProviderConfigDTO config,
                                                     ProviderRequest<GenericRequest> request)
    {
        if (config == null)
        {
            return ProviderResponse.fail("CONFIG_EMPTY", "Provider config must not be null.");
        }
        GenericRequest genericRequest = request == null ? null : request.getData();
        if (genericRequest == null)
        {
            return ProviderResponse.fail("REQUEST_EMPTY", "Provider request data must not be null.");
        }
        String operation = StringUtils.isNotBlank(genericRequest.getOperation())
                ? genericRequest.getOperation() : request.getAction();
        if (StringUtils.isBlank(operation))
        {
            return ProviderResponse.fail("OPERATION_EMPTY", "Provider operation must not be blank.");
        }

        try
        {
            log.info("通用 Provider Handler 委托 core 执行: configType={}, providerCode={}, operation={}",
                    config.getConfigType(), config.getProviderCode(), operation);
            GenericResponse response = runtimeInvoker.invoke(config, operation, toPayload(genericRequest));
            return ProviderResponse.success(response);
        }
        catch (Exception e)
        {
            log.error("通用 Provider Handler 执行失败: configType={}, providerCode={}, operation={}",
                    config.getConfigType(), config.getProviderCode(), operation, e);
            return ProviderResponse.fail("EXECUTION_ERROR", "Provider execute failed: " + e.getMessage());
        }
    }

    private ProviderPayload toPayload(GenericRequest request)
    {
        if (request.getPayload() != null)
        {
            return request.getPayload();
        }
        Map<String, Object> context = new LinkedHashMap<>(request.toContextMap());
        if (request.getFileData() != null)
        {
            context.put("fileData", request.getFileData());
        }
        if (StringUtils.isNotBlank(request.getContentType()))
        {
            context.put("contentType", request.getContentType());
        }
        return RawProviderPayload.of(context);
    }

    @Override
    public String getSupportedConfigType()
    {
        return "*";
    }

    @Override
    public String getSupportedProviderCode()
    {
        return "*";
    }

    /**
     * 基础配置校验只检查 Provider 基本信息，连接测试由 {@link #validateConnection(ProviderConfigDTO)} 完成。
     */
    @Override
    public boolean validateConfig(ProviderConfigDTO config)
    {
        return config != null
                && StringUtils.isNotBlank(config.getConfigType())
                && StringUtils.isNotBlank(config.getProviderCode());
    }

    /**
     * 连接测试优先使用 validate 模板，其次使用 ping 模板；两者都没有时跳过测试。
     */
    public boolean validateConnection(ProviderConfigDTO config)
    {
        if (!validateConfig(config))
        {
            return false;
        }
        try
        {
            ProviderApiTemplate template = findValidateTemplate(config);
            if (template == null)
            {
                log.info("未配置 Provider 连接测试模板，跳过测试: configType={}, providerCode={}",
                        config.getConfigType(), config.getProviderCode());
                return true;
            }

            GenericRequest validateRequest = new GenericRequest(template.getOperation());
            ProviderRequest<GenericRequest> request = new ProviderRequest<>();
            request.setAction(template.getOperation());
            request.setData(validateRequest);
            ProviderResponse<GenericResponse> response = execute(config, request);
            return response != null && response.isSuccess();
        }
        catch (ServiceException e)
        {
            log.warn("Provider 连接测试失败: configType={}, providerCode={}, message={}",
                    config.getConfigType(), config.getProviderCode(), e.getMessage());
            return false;
        }
        catch (Exception e)
        {
            log.warn("Provider 连接测试异常: configType={}, providerCode={}, message={}",
                    config.getConfigType(), config.getProviderCode(), e.getMessage());
            return false;
        }
    }

    private ProviderApiTemplate findValidateTemplate(ProviderConfigDTO config)
    {
        ProviderApiTemplate template = getTemplateQuietly(config, "validate");
        return template != null ? template : getTemplateQuietly(config, "ping");
    }

    private ProviderApiTemplate getTemplateQuietly(ProviderConfigDTO config, String operation)
    {
        try
        {
            return templateService.getTemplate(config.getConfigType(), config.getProviderCode(), operation);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }
}
