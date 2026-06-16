package com.bigyun.job.task;

import org.springframework.stereotype.Component;
import com.bigyun.common.core.utils.StringUtils;

/**
 * Demo scheduled tasks for the community framework.
 *
 * @author bigyun
 */
@Component("dyTask")
public class DyTask
{
    public void dyMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("Run multi-param task: string={}, boolean={}, long={}, double={}, integer={}", s, b, l, d, i));
    }

    public void dyParams(String params)
    {
        System.out.println("Run param task: " + params);
    }

    public void dyNoParams()
    {
        System.out.println("Run no-param task");
    }
}