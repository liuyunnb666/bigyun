package com.bigyun.demo;

import com.bigyun.common.security.annotation.EnableCustomConfig;
import com.bigyun.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class BigYunDemoApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunDemoApplication.class, args);
        System.out.println("BigYun demo module started.");
    }
}
