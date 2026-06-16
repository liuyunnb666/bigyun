package com.bigyun.common.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.util.TimeZone;
import org.springframework.context.annotation.Bean;

/**
 * 系统配置
 *
 * @author bigyun
 */
public class ApplicationConfig
{
    /**
     * 时区配置
     */
    @Bean
    public ObjectMapper objectMapper()
    {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        objectMapper.registerModule(new JavaTimeModule());
        SimpleModule longToStringModule = new SimpleModule();
        longToStringModule.addSerializer(Long.class, ToStringSerializer.instance);
        longToStringModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        objectMapper.registerModule(longToStringModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setTimeZone(TimeZone.getDefault());
        return objectMapper;
    }
}
