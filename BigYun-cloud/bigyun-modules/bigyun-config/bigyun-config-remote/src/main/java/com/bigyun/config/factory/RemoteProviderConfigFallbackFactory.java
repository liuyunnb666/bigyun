package com.bigyun.config.factory;

import com.bigyun.common.core.domain.R;
import com.bigyun.config.domain.ProviderCapabilityDTO;
import com.bigyun.config.domain.ProviderCapabilityLogDTO;
import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.config.remote.RemoteProviderConfigService;
import com.bigyun.config.domain.ProviderConfigDTO;
import com.bigyun.provider.domain.GenericResponse;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class RemoteProviderConfigFallbackFactory implements FallbackFactory<RemoteProviderConfigService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteProviderConfigFallbackFactory.class);

    @Override
    public RemoteProviderConfigService create(Throwable throwable)
    {
        log.error("配置中心服务调用失败:{}", throwable.getMessage());
        return new RemoteProviderConfigService()
        {
            @Override
            public R<ProviderConfigDTO> getDefaultConfig(String configType)
            {
                return R.fail("获取默认配置失败:" + throwable.getMessage());
            }

            @Override
            public R<List<ProviderConfigDTO>> listEnabledConfigs(String configType)
            {
                return R.ok(Collections.emptyList());
            }

            @Override
            public R<ProviderCapabilityDTO> getDefaultCapability(String capabilityCode)
            {
                return R.fail("获取Provider能力配置失败:" + throwable.getMessage());
            }

            @Override
            public R<GenericResponse> execute(ProviderExecuteRequest request)
            {
                return R.fail("Provider执行失败:" + throwable.getMessage());
            }

            @Override
            public R<Void> recordCapabilityLog(ProviderCapabilityLogDTO logDTO)
            {
                return R.fail("Provider能力调用日志上报失败:" + throwable.getMessage());
            }
        };
    }
}
