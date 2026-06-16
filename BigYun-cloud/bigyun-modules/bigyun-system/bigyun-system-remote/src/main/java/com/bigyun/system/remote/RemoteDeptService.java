package com.bigyun.system.remote;

import com.bigyun.common.core.constant.SecurityConstants;
import com.bigyun.common.core.constant.ServiceNameConstants;
import com.bigyun.common.core.domain.R;
import com.bigyun.system.domain.SysDept;
import com.bigyun.system.factory.RemoteDeptFallbackFactory;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 部门服务远程调用
 *
 * @author bigyun
 */
@FeignClient(contextId = "remoteDeptService", value = ServiceNameConstants.SYSTEM_SERVICE,
        fallbackFactory = RemoteDeptFallbackFactory.class)
public interface RemoteDeptService
{
    /**
     * 查询正常状态部门列表（供 AI 导诊推荐科室）
     */
    @GetMapping("/dept/internal/list")
    R<List<SysDept>> listNormalDepts(@RequestHeader(SecurityConstants.FROM_SOURCE) String source);

    /**
     * 根据科室ID查询科室详情。
     *
     * @param deptId 科室ID
     * @param source 内部调用标识
     * @return 科室详情响应
     */
    @GetMapping("/dept/inner/{deptId}")
    R<SysDept> getDeptById(@PathVariable("deptId") Long deptId,
                           @RequestHeader(SecurityConstants.FROM_SOURCE) String source);
}
