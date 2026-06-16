package com.bigyun.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.jdbc.DataSourceHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import com.bigyun.common.security.annotation.EnableRyFeignClients;

/**
 * 认证授权中心
 * 
 * @author bigyun
 */
@EnableRyFeignClients
@ComponentScan(basePackages = {"com.bigyun.auth", "com.bigyun.common.security"})
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceHealthContributorAutoConfiguration.class
})
public class BigYunAuthApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
