package com.bigyun.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 跨域配置
 *
 * @author bigyun
 */
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 允许跨域的域名
        corsConfiguration.addAllowedOriginPattern("*");

        // 允许跨域的请求头
        corsConfiguration.addAllowedHeader("*");

        // 允许跨域的请求方法
        corsConfiguration.addAllowedMethod("*");

        // 允许携带凭证（cookie）
        corsConfiguration.setAllowCredentials(true);

        // 预检请求缓存时间（秒）
        corsConfiguration.setMaxAge(3600L);

        // 对所有路径生效
        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsWebFilter(source);
    }
}
