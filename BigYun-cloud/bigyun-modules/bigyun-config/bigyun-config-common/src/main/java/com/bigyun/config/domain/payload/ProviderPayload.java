package com.bigyun.config.domain.payload;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "payloadType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RawProviderPayload.class, name = "raw"),
        @JsonSubTypes.Type(value = FaceDetectPayload.class, name = "faceDetect"),
        @JsonSubTypes.Type(value = FaceComparePayload.class, name = "faceCompare"),
        @JsonSubTypes.Type(value = AliyunFaceComparePayload.class, name = "aliyunFaceCompare"),
        @JsonSubTypes.Type(value = FaceLivenessPayload.class, name = "faceLiveness"),
        @JsonSubTypes.Type(value = FaceIdTokenPayload.class, name = "faceIdToken"),
        @JsonSubTypes.Type(value = FaceIdResultPayload.class, name = "faceIdResult"),
        @JsonSubTypes.Type(value = OcrRecognizePayload.class, name = "ocrRecognize"),
        @JsonSubTypes.Type(value = ProviderChatPayload.class, name = "providerChat")
})
public abstract class ProviderPayload implements Serializable
{
    private static final long serialVersionUID = 1L;

    public abstract Map<String, Object> toContextMap();

    protected Map<String, Object> newContext()
    {
        return new LinkedHashMap<>();
    }

    protected void putIfNotBlank(Map<String, Object> context, String key, String value)
    {
        if (value != null && !value.trim().isEmpty())
        {
            context.put(key, value);
        }
    }

    protected void putIfNotNull(Map<String, Object> context, String key, Object value)
    {
        if (value != null)
        {
            context.put(key, value);
        }
    }
}
