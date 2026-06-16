package com.bigyun.auth.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.bigyun.auth.service.strategy.LoginStrategy;
import com.bigyun.common.core.exception.ServiceException;
import com.bigyun.common.core.utils.StringUtils;

/**
 * 登录策略工厂
 * 负责管理和分发不同类型的登录策略（用户名、手机号、邮箱等）
 */
@Component
public class LoginStrategyFactory
{
    private static final Logger log = LoggerFactory.getLogger(LoginStrategyFactory.class);

    private final Map<String, LoginStrategy> strategyMap = new HashMap<>();

    /**
     * 构造函数，自动注册所有LoginStrategy实现类
     * @param loginStrategies Spring自动注入的所有策略实现
     */
    public LoginStrategyFactory(List<LoginStrategy> loginStrategies)
    {
        for (LoginStrategy loginStrategy : loginStrategies)
        {
            strategyMap.put(loginStrategy.getType(), loginStrategy);
            log.info("注册登录策略: type={}, class={}", loginStrategy.getType(), loginStrategy.getClass().getSimpleName());
        }
        log.info("登录策略工厂初始化完成，共注册 {} 个策略", strategyMap.size());
    }

    /**
     * 根据登录类型获取对应的登录策略
     * @param type 登录类型 (1-用户名密码, 2-手机号验证码, 3-邮箱验证码)
     * @return 对应的登录策略
     * @throws ServiceException 当类型为空或不支持时抛出异常
     */
    public LoginStrategy getStrategy(String type)
    {
        if (StringUtils.isEmpty(type))
        {
            log.warn("登录类型为空");
            throw new ServiceException("登录类型不能为空，支持的类型: 1-用户名密码, 2-手机号验证码, 3-邮箱验证码");
        }

        LoginStrategy strategy = strategyMap.get(type);
        if (strategy == null)
        {
            log.warn("不支持的登录类型: {}, 当前已注册的类型: {}", type, strategyMap.keySet());
            throw new ServiceException("不支持的登录类型: " + type + "，支持的类型: 1-用户名密码, 2-手机号验证码, 3-邮箱验证码");
        }

        log.debug("获取登录策略成功: type={}, strategy={}", type, strategy.getClass().getSimpleName());
        return strategy;
    }

    /**
     * 获取所有已注册的登录类型
     * @return 登录类型集合
     */
    public Map<String, LoginStrategy> getAllStrategies()
    {
        return new HashMap<>(strategyMap);
    }
}
