package com.bigyun.common.sensitive.config;

import java.io.IOException;
import java.util.Objects;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.bigyun.common.core.constant.UserConstants;
import com.bigyun.common.core.context.SecurityContextHolder;
import com.bigyun.common.sensitive.annotation.Sensitive;
import com.bigyun.common.sensitive.enums.DesensitizedType;

/**
 * 数据脱敏序列化过滤
 *
 * @author bigyun
 */
public class SensitiveJsonSerializer extends StdSerializer<String> implements ContextualSerializer
{
    private final DesensitizedType desensitizedType;

    public SensitiveJsonSerializer()
    {
        super(String.class);
        this.desensitizedType = null;
    }

    public SensitiveJsonSerializer(DesensitizedType desensitizedType)
    {
        super(String.class);
        this.desensitizedType = desensitizedType;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider provider) throws IOException
    {
        if (desensitizedType != null && desensitization())
        {
            gen.writeString(desensitizedType.desensitizer().apply(value));
        }
        else
        {
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider, BeanProperty property) throws JsonMappingException
    {
        if (property == null)
        {
            return provider.findValueSerializer(String.class);
        }
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass()))
        {
            return new SensitiveJsonSerializer(annotation.desensitizedType());
        }
        return provider.findValueSerializer(property.getType(), property);
    }

    /**
     * 是否需要脱敏处理
     */
    private boolean desensitization()
    {
        try
        {
            Long userId = SecurityContextHolder.getUserId();
            // 管理员不脱敏
            return !UserConstants.isAdmin(userId);
        }
        catch (Exception e)
        {
            return true;
        }
    }
}
