package com.bigyun.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.bigyun.common.security.annotation.EnableCustomConfig;
import com.bigyun.common.security.annotation.EnableRyFeignClients;

/**
 * 定时任务
 * 
 * @author bigyun
 */
@EnableCustomConfig
@EnableRyFeignClients   
@SpringBootApplication
public class BigYunJobApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(BigYunJobApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  定时任务模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
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
