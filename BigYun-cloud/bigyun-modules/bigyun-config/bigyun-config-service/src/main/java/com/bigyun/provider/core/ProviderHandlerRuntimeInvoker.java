package com.bigyun.provider.core;

import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.payload.ProviderPayload;
import com.bigyun.config.domain.payload.ProviderPayloadConverter;
import com.bigyun.config.service.ProviderRequest;
import com.bigyun.config.service.ProviderResponse;
import com.bigyun.provider.domain.GenericRequest;
import com.bigyun.provider.domain.GenericResponse;
import com.bigyun.provider.factory.ProviderHandlerFactory;

/**
 * HandlerFactory 运行时适配器。
 * <p>
 * 本批不再把它注册为 Spring Bean，避免和 provider-core 的通用 HTTP 执行器竞争
 * {@link ProviderRuntimeInvoker}。保留该类只是为了后续需要回退到旧专用 Handler 时有明确适配边界。
 * </p>
 */
public class ProviderHandlerRuntimeInvoker implements ProviderRuntimeInvoker
{
    private final ProviderHandlerFactory providerHandlerFactory;

    public ProviderHandlerRuntimeInvoker(ProviderHandlerFactory providerHandlerFactory)
    {
        this.providerHandlerFactory = providerHandlerFactory;
    }

    @Override
    public GenericResponse invoke(ProviderConfigDTO config, String operation, ProviderPayload payload)
    {
        GenericRequest genericRequest = new GenericRequest();
        genericRequest.setOperation(operation);
        genericRequest.setPayload(payload);
        genericRequest.setContextMap(ProviderPayloadConverter.toContextMap(payload, null));

        ProviderRequest<GenericRequest> providerRequest = new ProviderRequest<>();
        providerRequest.setAction(operation);
        providerRequest.setData(genericRequest);

        ProviderResponse<GenericResponse> response = providerHandlerFactory
                .<GenericRequest, GenericResponse>getHandler(config.getConfigType(), config.getProviderCode())
                .execute(config, providerRequest);
        if (response == null)
        {
            throw new ServiceException("Provider execute response is null.");
        }
        if (!response.isSuccess())
        {
            throw new ServiceException(response.getErrorMessage());
        }
        return response.getData();
    }
}
