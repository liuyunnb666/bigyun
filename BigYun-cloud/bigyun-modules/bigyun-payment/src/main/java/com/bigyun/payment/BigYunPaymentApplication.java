package com.bigyun.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.bigyun.common.security.annotation.EnableCustomConfig;
import com.bigyun.common.security.annotation.EnableRyFeignClients;

/**
 * 支付骨架服务启动类。
 *
 * @author bigyun
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class BigYunPaymentApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunPaymentApplication.class, args);
    }
}