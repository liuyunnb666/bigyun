package com.bigyun.system.factory;

import com.bigyun.common.core.domain.R;
import com.bigyun.system.domain.SysDept;
import com.bigyun.system.remote.RemoteDeptService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 部门服务降级
 *
 * @author bigyun
 */
@Component
public class RemoteDeptFallbackFactory implements FallbackFactory<RemoteDeptService>
{
    private static final Logger log = LoggerFactory.getLogger(RemoteDeptFallbackFactory.class);

    @Override
    public RemoteDeptService create(Throwable throwable)
    {
        log.error("remote dept service failed: {}", throwable.getMessage());
        return new RemoteDeptService()
        {
            @Override
            public R<List<SysDept>> listNormalDepts(String source)
            {
                return R.fail(failMessage(throwable));
            }

            @Override
            public R<SysDept> getDeptById(Long deptId, String source)
            {
                return R.fail(failMessage(throwable));
            }
        };
    }

    private static String failMessage(Throwable throwable)
    {
        return "dept service failed: " + throwable.getMessage();
    }
}
