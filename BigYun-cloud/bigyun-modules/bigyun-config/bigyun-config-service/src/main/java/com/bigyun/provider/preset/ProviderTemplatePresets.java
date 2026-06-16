// -*- coding: utf-8 -*-
package com.bigyun.provider.preset;

import com.bigyun.provider.domain.ProviderApiTemplate;
import com.bigyun.provider.service.IProviderApiTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Provider API模板预设管理器
 *
 * 功能说明：
 * 提供常见Provider的预设模板，方便用户快速配置。
 * 预设模板包含了主流服务商的API调用配置，用户只需填写AccessKey等认证信息即可使用。
 *
 * 支持的预设模板：
 * 1. 存储服务：阿里云OSS、腾讯云COS、七牛云Kodo
 * 2. LLM服务：OpenAI GPT、百度文心一言、阿里云通义千问
 *
 * 使用场景：
 * - 在配置管理页面选择预设模板
 * - 批量导入预设模板到数据库
 * - 获取预设模板列表供前端展示
 *
 * @author BigYun
 * @date 2024-05-22
 */
@Component
public class ProviderTemplatePresets {

    private static final Logger log = LoggerFactory.getLogger(ProviderTemplatePresets.class);

    @Autowired
    private IProviderApiTemplateService templateService;

    /**
     * 获取所有预设模板
     *
     * @return 预设模板Map，key为预设代码，value为模板列表
     */
    public Map<String, List<ProviderApiTemplate>> getAllPresets() {
        Map<String, List<ProviderApiTemplate>> presets = new HashMap<>();

        presets.put("aliyun-oss", getAliyunOSSPreset());
        presets.put("tencent-cos", getTencentCOSPreset());
        presets.put("qiniu-kodo", getQiniuKodoPreset());
        presets.put("openai-gpt", getOpenAIGPTPreset());
        presets.put("baidu-wenxin", getBaiduWenxinPreset());
        presets.put("aliyun-qwen", getAliyunQwenPreset());

        return presets;
    }

    /**
     * 获取指定预设模板
     *
     * @param presetCode 预设代码
     * @return 模板列表
     */
    public List<ProviderApiTemplate> getPreset(String presetCode) {
        switch (presetCode) {
            case "aliyun-oss":
                return getAliyunOSSPreset();
            case "tencent-cos":
                return getTencentCOSPreset();
            case "qiniu-kodo":
                return getQiniuKodoPreset();
            case "openai-gpt":
                return getOpenAIGPTPreset();
            case "baidu-wenxin":
                return getBaiduWenxinPreset();
            case "aliyun-qwen":
                return getAliyunQwenPreset();
            default:
                log.warn("未知的预设代码: {}", presetCode);
                return Collections.emptyList();
        }
    }

    /**
     * 导入预设模板到数据库
     *
     * @param presetCode 预设代码
     * @return 导入成功的数量
     */
    public int importPreset(String presetCode) {
        List<ProviderApiTemplate> templates = getPreset(presetCode);
        if (templates.isEmpty()) {
            log.warn("预设模板不存在: {}", presetCode);
            return 0;
        }

        int count = templateService.batchImportTemplates(templates);
        log.info("导入预设模板成功: presetCode={}, count={}", presetCode, count);
        return count;
    }

    /**
     * 导入所有预设模板
     *
     * @return 导入成功的总数量
     */
    public int importAllPresets() {
        int totalCount = 0;
        Map<String, List<ProviderApiTemplate>> allPresets = getAllPresets();

        for (Map.Entry<String, List<ProviderApiTemplate>> entry : allPresets.entrySet()) {
            int count = templateService.batchImportTemplates(entry.getValue());
            totalCount += count;
            log.info("导入预设模板: presetCode={}, count={}", entry.getKey(), count);
        }

        log.info("导入所有预设模板完成: totalCount={}", totalCount);
        return totalCount;
    }

    // ==================== 阿里云OSS预设 ====================

    private List<ProviderApiTemplate> getAliyunOSSPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 上传文件
        ProviderApiTemplate uploadTemplate = new ProviderApiTemplate();
        uploadTemplate.setConfigType("storage");
        uploadTemplate.setProviderCode("aliyun-oss");
        uploadTemplate.setOperation("upload");
        uploadTemplate.setHttpMethod("PUT");
        uploadTemplate.setUrlTemplate("https://${bucketName}.${endpoint}/${filePath}");
        uploadTemplate.setHeadersJson("{\"Content-Type\":\"${contentType}\"}");
        uploadTemplate.setBodyType("raw");
        uploadTemplate.setBodyTemplate("${fileData}");
        uploadTemplate.setAuthType("aliyun");
        uploadTemplate.setAuthConfigJson("{\"accessKeyId\":\"${accessKey}\",\"accessKeySecret\":\"${secretKey}\"}");
        uploadTemplate.setResponseType("json");
        uploadTemplate.setResponseMapping("{\"fileUrl\":\"$.url\",\"etag\":\"$.etag\"}");
        uploadTemplate.setTimeout(30000);
        uploadTemplate.setRetryTimes(3);
        uploadTemplate.setIsEnabled("1");
        uploadTemplate.setRemark("阿里云OSS上传文件API模板");
        templates.add(uploadTemplate);

        // 删除文件
        ProviderApiTemplate deleteTemplate = new ProviderApiTemplate();
        deleteTemplate.setConfigType("storage");
        deleteTemplate.setProviderCode("aliyun-oss");
        deleteTemplate.setOperation("delete");
        deleteTemplate.setHttpMethod("DELETE");
        deleteTemplate.setUrlTemplate("https://${bucketName}.${endpoint}/${filePath}");
        deleteTemplate.setAuthType("aliyun");
        deleteTemplate.setAuthConfigJson("{\"accessKeyId\":\"${accessKey}\",\"accessKeySecret\":\"${secretKey}\"}");
        deleteTemplate.setResponseType("json");
        deleteTemplate.setTimeout(30000);
        deleteTemplate.setRetryTimes(3);
        deleteTemplate.setIsEnabled("1");
        deleteTemplate.setRemark("阿里云OSS删除文件API模板");
        templates.add(deleteTemplate);

        return templates;
    }

    // ==================== 腾讯云COS预设 ====================

    private List<ProviderApiTemplate> getTencentCOSPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 上传文件
        ProviderApiTemplate uploadTemplate = new ProviderApiTemplate();
        uploadTemplate.setConfigType("storage");
        uploadTemplate.setProviderCode("tencent-cos");
        uploadTemplate.setOperation("upload");
        uploadTemplate.setHttpMethod("PUT");
        uploadTemplate.setUrlTemplate("https://${bucketName}.cos.${region}.myqcloud.com/${filePath}");
        uploadTemplate.setHeadersJson("{\"Content-Type\":\"${contentType}\"}");
        uploadTemplate.setBodyType("raw");
        uploadTemplate.setBodyTemplate("${fileData}");
        uploadTemplate.setAuthType("tencent");
        uploadTemplate.setAuthConfigJson("{\"secretId\":\"${accessKey}\",\"secretKey\":\"${secretKey}\"}");
        uploadTemplate.setResponseType("json");
        uploadTemplate.setResponseMapping("{\"fileUrl\":\"$.Location\",\"etag\":\"$.ETag\"}");
        uploadTemplate.setTimeout(30000);
        uploadTemplate.setRetryTimes(3);
        uploadTemplate.setIsEnabled("1");
        uploadTemplate.setRemark("腾讯云COS上传文件API模板");
        templates.add(uploadTemplate);

        // 删除文件
        ProviderApiTemplate deleteTemplate = new ProviderApiTemplate();
        deleteTemplate.setConfigType("storage");
        deleteTemplate.setProviderCode("tencent-cos");
        deleteTemplate.setOperation("delete");
        deleteTemplate.setHttpMethod("DELETE");
        deleteTemplate.setUrlTemplate("https://${bucketName}.cos.${region}.myqcloud.com/${filePath}");
        deleteTemplate.setAuthType("tencent");
        deleteTemplate.setAuthConfigJson("{\"secretId\":\"${accessKey}\",\"secretKey\":\"${secretKey}\"}");
        deleteTemplate.setResponseType("json");
        deleteTemplate.setTimeout(30000);
        deleteTemplate.setRetryTimes(3);
        deleteTemplate.setIsEnabled("1");
        deleteTemplate.setRemark("腾讯云COS删除文件API模板");
        templates.add(deleteTemplate);

        return templates;
    }

    // ==================== 七牛云Kodo预设 ====================

    private List<ProviderApiTemplate> getQiniuKodoPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 上传文件
        ProviderApiTemplate uploadTemplate = new ProviderApiTemplate();
        uploadTemplate.setConfigType("storage");
        uploadTemplate.setProviderCode("qiniu-kodo");
        uploadTemplate.setOperation("upload");
        uploadTemplate.setHttpMethod("POST");
        uploadTemplate.setUrlTemplate("https://upload.qiniup.com");
        uploadTemplate.setBodyType("multipart");
        uploadTemplate.setBodyTemplate("{\"key\":\"${filePath}\",\"token\":\"${uploadToken}\"}");
        uploadTemplate.setAuthType("qiniu");
        uploadTemplate.setAuthConfigJson("{\"accessKey\":\"${accessKey}\",\"secretKey\":\"${secretKey}\"}");
        uploadTemplate.setResponseType("json");
        uploadTemplate.setResponseMapping("{\"fileUrl\":\"$.key\",\"hash\":\"$.hash\"}");
        uploadTemplate.setTimeout(30000);
        uploadTemplate.setRetryTimes(3);
        uploadTemplate.setIsEnabled("1");
        uploadTemplate.setRemark("七牛云Kodo上传文件API模板");
        templates.add(uploadTemplate);

        // 删除文件
        ProviderApiTemplate deleteTemplate = new ProviderApiTemplate();
        deleteTemplate.setConfigType("storage");
        deleteTemplate.setProviderCode("qiniu-kodo");
        deleteTemplate.setOperation("delete");
        deleteTemplate.setHttpMethod("POST");
        deleteTemplate.setUrlTemplate("https://rs.qiniu.com/delete/${encodedEntryURI}");
        deleteTemplate.setAuthType("qiniu");
        deleteTemplate.setAuthConfigJson("{\"accessKey\":\"${accessKey}\",\"secretKey\":\"${secretKey}\"}");
        deleteTemplate.setResponseType("json");
        deleteTemplate.setTimeout(30000);
        deleteTemplate.setRetryTimes(3);
        deleteTemplate.setIsEnabled("1");
        deleteTemplate.setRemark("七牛云Kodo删除文件API模板");
        templates.add(deleteTemplate);

        return templates;
    }

    // ==================== OpenAI GPT预设 ====================

    private List<ProviderApiTemplate> getOpenAIGPTPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 聊天补全
        ProviderApiTemplate chatTemplate = new ProviderApiTemplate();
        chatTemplate.setConfigType("llm");
        chatTemplate.setProviderCode("openai-gpt");
        chatTemplate.setOperation("chat");
        chatTemplate.setHttpMethod("POST");
        chatTemplate.setUrlTemplate("https://api.openai.com/v1/chat/completions");
        chatTemplate.setHeadersJson("{\"Content-Type\":\"application/json\"}");
        chatTemplate.setBodyType("json");
        chatTemplate.setBodyTemplate("{\"model\":\"${model}\",\"messages\":${messages},\"temperature\":${temperature}}");
        chatTemplate.setAuthType("bearer");
        chatTemplate.setAuthConfigJson("{\"token\":\"${accessKey}\"}");
        chatTemplate.setResponseType("json");
        chatTemplate.setResponseMapping("{\"content\":\"$.choices[0].message.content\",\"usage\":\"$.usage\"}");
        chatTemplate.setTimeout(60000);
        chatTemplate.setRetryTimes(2);
        chatTemplate.setIsEnabled("1");
        chatTemplate.setRemark("OpenAI GPT聊天补全API模板");
        templates.add(chatTemplate);

        return templates;
    }

    // ==================== 百度文心一言预设 ====================

    private List<ProviderApiTemplate> getBaiduWenxinPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 聊天补全
        ProviderApiTemplate chatTemplate = new ProviderApiTemplate();
        chatTemplate.setConfigType("llm");
        chatTemplate.setProviderCode("baidu-wenxin");
        chatTemplate.setOperation("chat");
        chatTemplate.setHttpMethod("POST");
        chatTemplate.setUrlTemplate("https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/completions");
        chatTemplate.setHeadersJson("{\"Content-Type\":\"application/json\"}");
        chatTemplate.setBodyType("json");
        chatTemplate.setBodyTemplate("{\"messages\":${messages}}");
        chatTemplate.setAuthType("baidu");
        chatTemplate.setAuthConfigJson("{\"apiKey\":\"${accessKey}\",\"secretKey\":\"${secretKey}\"}");
        chatTemplate.setResponseType("json");
        chatTemplate.setResponseMapping("{\"content\":\"$.result\",\"usage\":\"$.usage\"}");
        chatTemplate.setTimeout(60000);
        chatTemplate.setRetryTimes(2);
        chatTemplate.setIsEnabled("1");
        chatTemplate.setRemark("百度文心一言聊天补全API模板");
        templates.add(chatTemplate);

        return templates;
    }

    // ==================== 阿里云通义千问预设 ====================

    private List<ProviderApiTemplate> getAliyunQwenPreset() {
        List<ProviderApiTemplate> templates = new ArrayList<>();

        // 聊天补全
        ProviderApiTemplate chatTemplate = new ProviderApiTemplate();
        chatTemplate.setConfigType("llm");
        chatTemplate.setProviderCode("aliyun-qwen");
        chatTemplate.setOperation("chat");
        chatTemplate.setHttpMethod("POST");
        chatTemplate.setUrlTemplate("https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation");
        chatTemplate.setHeadersJson("{\"Content-Type\":\"application/json\"}");
        chatTemplate.setBodyType("json");
        chatTemplate.setBodyTemplate("{\"model\":\"${model}\",\"input\":{\"messages\":${messages}}}");
        chatTemplate.setAuthType("bearer");
        chatTemplate.setAuthConfigJson("{\"token\":\"${accessKey}\"}");
        chatTemplate.setResponseType("json");
        chatTemplate.setResponseMapping("{\"content\":\"$.output.text\",\"usage\":\"$.usage\"}");
        chatTemplate.setTimeout(60000);
        chatTemplate.setRetryTimes(2);
        chatTemplate.setIsEnabled("1");
        chatTemplate.setRemark("阿里云通义千问聊天补全API模板");
        templates.add(chatTemplate);

        return templates;
    }

    /**
     * 获取预设模板列表（用于前端展示）
     *
     * @return 预设信息列表
     */
    public List<Map<String, Object>> getPresetList() {
        List<Map<String, Object>> presetList = new ArrayList<>();

        presetList.add(createPresetInfo("aliyun-oss", "阿里云OSS", "storage", "阿里云对象存储服务", 2));
        presetList.add(createPresetInfo("tencent-cos", "腾讯云COS", "storage", "腾讯云对象存储服务", 2));
        presetList.add(createPresetInfo("qiniu-kodo", "七牛云Kodo", "storage", "七牛云对象存储服务", 2));
        presetList.add(createPresetInfo("openai-gpt", "OpenAI GPT", "llm", "OpenAI大语言模型服务", 1));
        presetList.add(createPresetInfo("baidu-wenxin", "百度文心一言", "llm", "百度大语言模型服务", 1));
        presetList.add(createPresetInfo("aliyun-qwen", "阿里云通义千问", "llm", "阿里云大语言模型服务", 1));

        return presetList;
    }

    private Map<String, Object> createPresetInfo(String code, String name, String type, String description, int templateCount) {
        Map<String, Object> info = new HashMap<>();
        info.put("code", code);
        info.put("name", name);
        info.put("type", type);
        info.put("description", description);
        info.put("templateCount", templateCount);
        return info;
    }
}
