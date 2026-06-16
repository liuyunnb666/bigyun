package com.bigyun.provider;

import com.bigyun.common.security.annotation.EnableCustomConfig;
import com.bigyun.common.security.annotation.EnableRyFeignClients;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 配置中心启动类。
 * <p>
 * config-service 是 Provider 控制面，同时保留内部兼容执行入口。这里显式扫描 Provider 运行时需要的管理
 * Mapper、只读 Mapper 和 config-common 只读 Mapper，避免运行时快照发布组件缺少 Mapper Bean。
 * </p>
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@MapperScan(basePackages = {
        "com.bigyun.provider.mapper",
        "com.bigyun.provider.db.mapper",
        "com.bigyun.config.mapper"
})
public class BigYunConfigApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunConfigApplication.class, args);
        System.out.println("BigYun 配置中心模块启动成功");
    }
}
