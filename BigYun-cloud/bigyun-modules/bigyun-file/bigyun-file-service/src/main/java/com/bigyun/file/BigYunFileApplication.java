package com.bigyun.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.bigyun.common.security.annotation.EnableCustomConfig;
import com.bigyun.common.security.annotation.EnableRyFeignClients;

/**
 * 文件服务启动入口。
 *
 * <ul>
 *   <li>{@code @EnableCustomConfig} 启用自定义配置（Feign 拦截器、全局异常处理等）</li>
 *   <li>{@code @EnableRyFeignClients} 启用 OpenFeign 远程调用客户端扫描</li>
 * </ul>
 *
 * @author bigyun
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
public class BigYunFileApplication
{
    /**
     *
     * @param args 命令行参数
     */
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunFileApplication.class, args);
        System.out.println("文件服务模块启动成功\n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\\\      \\\\   \\\\   /  /    \n" +
                " | ( ' )  |       \\\\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\\\ \\\\  |  ||   |(_,_)'         \n" +
                " |  | \\\\ `'   /|   `-'  /           \n" +
                " |  |  \\\\    /  \\\\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
