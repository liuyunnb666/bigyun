package com.bigyun.provider.service;

import com.bigyun.config.domain.ProviderExecuteRequest;
import com.bigyun.config.domain.ProviderCapabilityLogDTO;
import com.bigyun.provider.domain.GenericResponse;
import com.bigyun.provider.domain.ProviderCapabilityLog;
import java.util.List;

public interface IProviderCapabilityLogService
{
    List<ProviderCapabilityLog> selectLogList(ProviderCapabilityLog query);

    void recordSuccess(ProviderExecuteRequest request, GenericResponse response, long startTimeMillis);

    void recordFailure(ProviderExecuteRequest request, Exception exception, long startTimeMillis);

    void recordLog(ProviderCapabilityLogDTO logDTO);
}
