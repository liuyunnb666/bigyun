package com.bigyun.config.remote;

import com.bigyun.common.core.constant.ServiceNameConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.config.domain.ProviderCapabilityLogDTO;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.config.factory.RemoteProviderConfigFallbackFactory;
import com.bigyun.provider.domain.GenericResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(contextId = "remoteProviderConfigService", value = ServiceNameConstants.CONFIG_SERVICE,
        fallbackFactory = RemoteProviderConfigFallbackFactory.class)
public interface RemoteProviderConfigService
{
    @GetMapping("/config/internal/default/{configType}")
    R<ProviderConfigDTO> getDefaultConfig(@PathVariable("configType") String configType);

    @GetMapping("/config/internal/enabled/{configType}")
    R<List<ProviderConfigDTO>> listEnabledConfigs(@PathVariable("configType") String configType);

    @GetMapping("/config/internal/capability/{capabilityCode}")
    R<ProviderCapabilityDTO> getDefaultCapability(@PathVariable("capabilityCode") String capabilityCode);

    @PostMapping("/config/internal/execute")
    R<GenericResponse> execute(@RequestBody ProviderExecuteRequest request);

    @PostMapping("/config/internal/capability-log")
    R<Void> recordCapabilityLog(@RequestBody ProviderCapabilityLogDTO logDTO);
}
