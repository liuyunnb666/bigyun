# BigYun Provider配置管理系统 - 快速入门指南

> 模块拆分、`provider-core` / `provider-db` 职责和运行时调用链说明：见 [CONFIG_MODULE_DECOUPLING_GUIDE.md](CONFIG_MODULE_DECOUPLING_GUIDE.md)。
> 能力中心的 Provider、模型目录、能力定义、模型关系、默认模型、运行时快照和调用日志配置步骤：见 [CAPABILITY_CONFIGURATION_GUIDE.md](CAPABILITY_CONFIGURATION_GUIDE.md)。

## 📚 目录

1. [系统简介](#系统简介)
2. [快速开始](#快速开始)
3. [核心概念](#核心概念)
4. [使用示例](#使用示例)
5. [扩展开发](#扩展开发)
6. [常见问题](#常见问题)

---

## 系统简介

### 什么是Provider配置管理系统？

Provider配置管理系统是一个**可扩展、配置驱动**的第三方服务提供商管理平台。它解决了以下问题：

- ❌ **问题1**：每个第三方服务都要写一套配置和调用代码，重复劳动
- ❌ **问题2**：切换服务提供商需要修改代码、重新编译部署
- ❌ **问题3**：新增服务提供商需要修改多处代码，容易出错
- ❌ **问题4**：配置分散在各个模块，难以统一管理

✅ **解决方案**：

- ✅ 统一的配置管理和服务调用接口
- ✅ 数据库配置切换，无需修改代码
- ✅ 工厂+策略模式，自动注册Handler
- ✅ 枚举与数据库双向绑定，类型安全

### 核心特性

1. **枚举驱动** - Java枚举定义元数据，类型安全，编译时检查
2. **数据库存储** - 配置实例存储在数据库，支持运行时查询和修改
3. **自动同步** - 应用启动时自动同步枚举到数据库
4. **自动注册** - Handler通过Spring自动注册，无需手动维护映射
5. **配置热切换** - 修改数据库配置即可切换服务提供商
6. **易于扩展** - 添加新服务商只需3步

---

## 快速开始

### 第一步：执行数据库脚本

```sql
-- 执行SQL脚本创建表结构
source BigYun-cloud/bigyun-modules/bigyun-config/sql/provider_config_tables.sql;
```

这将创建两张表：
- `sys_provider_meta` - 存储枚举元数据
- `sys_provider_config` - 存储配置实例

### 第二步：启动应用

启动应用后，查看日志，应该看到：

```
========================================
开始同步Provider枚举到数据库...
========================================
开始同步配置类型枚举...
  [新增] 配置类型: storage - 对象存储
  [新增] 配置类型: llm - 大语言模型
配置类型枚举同步完成，共处理 8 个配置类型
开始同步服务商枚举...
  [新增] 服务商: local - 本地存储 (所属类型: 对象存储)
  [新增] 服务商: aliyun-oss - 阿里云OSS (所属类型: 对象存储)
服务商枚举同步完成，共处理 40 个服务商
========================================
Provider枚举同步完成
========================================

========================================
开始初始化Provider Handler工厂...
========================================
  [注册] storage -> local (LocalStorageHandler)
  [注册] storage -> aliyun-oss (AliyunOSSHandler)
  [注册] storage -> tencent-cos (TencentCOSHandler)
  [注册] llm -> openai-gpt (OpenAIGPTHandler)
========================================
Provider Handler工厂初始化完成
共注册 2 个配置类型，4 个Handler
========================================
```

### 第三步：添加配置

通过管理后台或SQL添加配置实例：

```sql
-- 添加本地存储配置
INSERT INTO sys_provider_config (
    config_type, provider_code, provider_name,
    base_path, domain,
    is_default, status, create_by
) VALUES (
    'storage', 'local', '本地存储',
    '/data/upload', 'https://example.com',
    'Y', '0', 'admin'
);
```

### 第四步：使用服务

在业务代码中使用：

```java
@Service
public class FileUploadService {
    @Autowired
    private IProviderService providerService;
    
    public String uploadFile(MultipartFile file) throws IOException {
        // 构建请求
        StorageRequest request = StorageRequest.upload(
            file.getOriginalFilename(),
            "uploads/2026/05/21/test.jpg",
            file.getBytes(),
            file.getContentType()
        );
        
        // 调用服务（自动使用默认配置）
        StorageResponse response = providerService.execute("storage", 
            new ProviderRequest<>(request));
        
        return response.getFileUrl();
    }
}
```

---

## 核心概念

### 1. 三层架构

```
枚举层（代码）
  ↓ 启动时自动同步
元数据层（数据库 sys_provider_meta）
  ↓ 定义支持的类型和服务商
配置层（数据库 sys_provider_config）
  ↓ 存储具体的配置实例
```

### 2. 枚举定义

**配置类型枚举** (`ProviderConfigTypeEnum`)

```java
public enum ProviderConfigTypeEnum {
    STORAGE("storage", "对象存储", "OSS、MinIO、本地存储等"),
    LLM("llm", "大语言模型", "OpenAI GPT、Claude等"),
    TTS("tts", "文本转语音", "阿里云TTS、腾讯云TTS等"),
    // ...
}
```

**服务商枚举** (`ProviderCodeEnum`)

```java
public enum ProviderCodeEnum {
    LOCAL_STORAGE("local", "本地存储", STORAGE),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS", STORAGE),
    TENCENT_COS("tencent-cos", "腾讯云COS", STORAGE),
    OPENAI_GPT("openai-gpt", "OpenAI GPT", LLM),
    // ...
}
```

### 3. Handler接口

每个服务提供商需要实现`IProviderHandler`接口：

```java
public interface IProviderHandler<T, R> {
    // 执行服务调用
    ProviderResponse<R> execute(ProviderConfigDTO config, ProviderRequest<T> request);
    
    // 获取支持的配置类型
    String getSupportedConfigType();
    
    // 获取支持的服务商代码
    String getSupportedProviderCode();
    
    // 验证配置有效性
    boolean validateConfig(ProviderConfigDTO config);
}
```

### 4. 调用流程

```
业务代码
  ↓ providerService.execute("storage", request)
ProviderServiceImpl
  ↓ 1. 查询数据库获取默认storage配置
  ↓ 2. 根据provider_code通过工厂获取Handler
  ↓ 3. handler.execute(config, request)
LocalStorageHandler / AliyunOSSHandler / TencentCOSHandler
  ↓ 执行具体操作
返回结果
```

---

## 使用示例

### 示例1：文件上传（自动切换存储）

```java
@RestController
@RequestMapping("/file")
public class FileController {
    
    @Autowired
    private IProviderService providerService;
    
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 构建存储请求
        StorageRequest request = StorageRequest.upload(
            file.getOriginalFilename(),
            generateFilePath(file.getOriginalFilename()),
            file.getBytes(),
            file.getContentType()
        );
        
        // 调用存储服务（自动使用默认配置）
        StorageResponse response = providerService.execute("storage", 
            new ProviderRequest<>(request));
        
        return AjaxResult.success(response.getFileUrl());
    }
    
    private String generateFilePath(String filename) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String ext = filename.substring(filename.lastIndexOf("."));
        return "uploads/" + date + "/" + uuid + ext;
    }
}
```

**配置切换演示：**

```sql
-- 场景1：使用本地存储
UPDATE sys_provider_config 
SET is_default = 'Y' 
WHERE config_type = 'storage' AND provider_code = 'local';
-- 代码无需修改，自动使用本地存储

-- 场景2：切换到阿里云OSS
UPDATE sys_provider_config 
SET is_default = 'Y' 
WHERE config_type = 'storage' AND provider_code = 'aliyun-oss';
-- 代码无需修改，自动使用阿里云OSS
```

### 示例2：指定服务商

```java
@Service
public class FileService {
    
    @Autowired
    private IProviderService providerService;
    
    // 上传到默认存储
    public String uploadToDefault(MultipartFile file) {
        StorageRequest request = StorageRequest.upload(...);
        StorageResponse response = providerService.execute("storage", 
            new ProviderRequest<>(request));
        return response.getFileUrl();
    }
    
    // 上传到阿里云OSS
    public String uploadToOSS(MultipartFile file) {
        StorageRequest request = StorageRequest.upload(...);
        StorageResponse response = providerService.execute("storage", "aliyun-oss",
            new ProviderRequest<>(request));
        return response.getFileUrl();
    }
    
    // 同时上传到多个存储
    public Map<String, String> uploadToMultiple(MultipartFile file) {
        StorageRequest request = StorageRequest.upload(...);
        Map<String, String> urls = new HashMap<>();
        
        // 上传到本地
        StorageResponse localResponse = providerService.execute("storage", "local",
            new ProviderRequest<>(request));
        urls.put("local", localResponse.getFileUrl());
        
        // 上传到OSS
        StorageResponse ossResponse = providerService.execute("storage", "aliyun-oss",
            new ProviderRequest<>(request));
        urls.put("oss", ossResponse.getFileUrl());
        
        return urls;
    }
}
```

### 示例3：大模型调用

```java
@Service
public class AIService {
    
    @Autowired
    private IProviderService providerService;
    
    public String chat(String message) {
        // 构建LLM请求
        LLMRequest request = new LLMRequest();
        request.setMessages(List.of(
            new Message("user", message)
        ));
        
        // 调用大模型服务（使用默认配置）
        LLMResponse response = providerService.execute("llm", 
            new ProviderRequest<>(request));
        
        return response.getContent();
    }
    
    // 指定使用GPT-4
    public String chatWithGPT4(String message) {
        LLMRequest request = new LLMRequest();
        request.setMessages(List.of(new Message("user", message)));
        
        LLMResponse response = providerService.execute("llm", "openai-gpt",
            new ProviderRequest<>(request));
        
        return response.getContent();
    }
}
```

---

## 扩展开发

### 场景1：添加新的配置类型（如视频处理）

**步骤1：添加枚举**

```java
// ProviderConfigTypeEnum.java
VIDEO_PROCESS("video_process", "视频处理", "视频转码、截图等服务"),

// ProviderCodeEnum.java
ALIYUN_MTS("aliyun-mts", "阿里云媒体处理", VIDEO_PROCESS),
TENCENT_MPS("tencent-mps", "腾讯云媒体处理", VIDEO_PROCESS),
```

**步骤2：定义请求响应类**

```java
// VideoProcessRequest.java
public class VideoProcessRequest {
    private String videoUrl;      // 视频URL
    private String outputFormat;  // 输出格式
    private Integer width;        // 宽度
    private Integer height;       // 高度
    // getters and setters
}

// VideoProcessResponse.java
public class VideoProcessResponse {
    private String taskId;        // 任务ID
    private String outputUrl;     // 输出URL
    private String status;        // 状态
    // getters and setters
}
```

**步骤3：实现Handler**

```java
@Component
public class AliyunMTSHandler implements IProviderHandler<VideoProcessRequest, VideoProcessResponse> {
    
    @Override
    public ProviderResponse<VideoProcessResponse> execute(
        ProviderConfigDTO config, 
        ProviderRequest<VideoProcessRequest> request) {
        
        VideoProcessRequest videoRequest = request.getData();
        
        // TODO: 调用阿里云MTS SDK
        // MTSClient client = new MTSClient(config.getAccessKey(), config.getSecretKey());
        // SubmitJobsResponse response = client.submitJobs(...);
        
        VideoProcessResponse videoResponse = new VideoProcessResponse();
        videoResponse.setTaskId("task-123");
        videoResponse.setOutputUrl("https://cdn.example.com/output.mp4");
        videoResponse.setStatus("Processing");
        
        return ProviderResponse.success(videoResponse);
    }
    
    @Override
    public String getSupportedConfigType() {
        return "video_process";
    }
    
    @Override
    public String getSupportedProviderCode() {
        return "aliyun-mts";
    }
    
    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        return config != null 
            && config.getAccessKey() != null 
            && config.getSecretKey() != null;
    }
}
```

**步骤4：重启应用**

- ProviderEnumManager自动同步枚举到数据库
- ProviderHandlerFactory自动注册Handler
- 在管理后台添加配置即可使用

### 场景2：添加新的服务商（如七牛云存储）

**步骤1：添加枚举**

```java
// ProviderCodeEnum.java
QINIU_KODO("qiniu-kodo", "七牛云Kodo", STORAGE),
```

**步骤2：实现Handler**

```java
@Component
public class QiniuKodoHandler implements IProviderHandler<StorageRequest, StorageResponse> {
    
    @Override
    public ProviderResponse<StorageResponse> execute(
        ProviderConfigDTO config, 
        ProviderRequest<StorageRequest> request) {
        
        StorageRequest storageRequest = request.getData();
        
        // TODO: 调用七牛云SDK
        // Configuration cfg = new Configuration(Region.region0());
        // UploadManager uploadManager = new UploadManager(cfg);
        // Auth auth = Auth.create(config.getAccessKey(), config.getSecretKey());
        // String token = auth.uploadToken(config.getBucketName());
        // Response response = uploadManager.put(storageRequest.getFileData(), 
        //     storageRequest.getFilePath(), token);
        
        StorageResponse storageResponse = new StorageResponse();
        storageResponse.setFilePath(storageRequest.getFilePath());
        storageResponse.setFileUrl(config.getDomain() + "/" + storageRequest.getFilePath());
        storageResponse.setFileSize((long) storageRequest.getFileData().length);
        
        return ProviderResponse.success(storageResponse);
    }
    
    @Override
    public String getSupportedConfigType() {
        return "storage";
    }
    
    @Override
    public String getSupportedProviderCode() {
        return "qiniu-kodo";
    }
    
    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        return config != null
            && config.getAccessKey() != null
            && config.getSecretKey() != null
            && config.getBucketName() != null;
    }
}
```

**步骤3：重启应用，添加配置**

```sql
INSERT INTO sys_provider_config (
    config_type, provider_code, provider_name,
    bucket_name, domain, access_key, secret_key,
    is_default, status, create_by
) VALUES (
    'storage', 'qiniu-kodo', '七牛云Kodo',
    'my-bucket', 'https://cdn.qiniu.com', 
    'ENCRYPTED_ACCESS_KEY', 'ENCRYPTED_SECRET_KEY',
    'N', '0', 'admin'
);
```

---

## 常见问题

### Q1：如何切换默认服务提供商？

**A：** 修改数据库配置即可，无需重启应用。

```sql
-- 将阿里云OSS设置为默认
UPDATE sys_provider_config 
SET is_default = 'Y' 
WHERE config_type = 'storage' AND provider_code = 'aliyun-oss';

-- 系统会自动将其他配置的is_default设为'N'
```

### Q2：如何验证配置是否生效？

**A：** 查看应用日志，调用服务时会打印：

```
执行Provider服务: configType=storage, providerCode=aliyun-oss
Provider服务调用成功: configType=storage, providerCode=aliyun-oss
```

### Q3：新增Handler后为什么没有注册？

**A：** 检查以下几点：

1. Handler类是否添加了`@Component`注解
2. Handler是否实现了`IProviderHandler`接口
3. `getSupportedConfigType()`和`getSupportedProviderCode()`是否返回正确的值
4. 是否重启了应用

### Q4：如何调试Handler？

**A：** 在Handler的execute方法中添加日志：

```java
@Override
public ProviderResponse<StorageResponse> execute(...) {
    log.info("开始执行存储操作: filePath={}", request.getData().getFilePath());
    
    try {
        // 执行操作
        log.info("存储操作成功");
        return ProviderResponse.success(response);
    } catch (Exception e) {
        log.error("存储操作失败", e);
        return ProviderResponse.fail("STORAGE_ERROR", e.getMessage());
    }
}
```

### Q5：如何处理敏感信息（如密钥）？

**A：** 系统已内置加密功能：

1. 保存时自动加密：`ProviderSecretUtils.encrypt()`
2. 使用时自动解密：`ProviderSecretUtils.decrypt()`
3. 前端查询时自动脱敏：`****KlMn`

### Q6：如何支持多租户？

**A：** 可以扩展配置表，添加租户ID字段：

```sql
ALTER TABLE sys_provider_config ADD COLUMN tenant_id BIGINT;
ALTER TABLE sys_provider_config ADD INDEX idx_tenant_id (tenant_id);
```

然后在查询配置时增加租户ID条件。

---

## 附录

### 已支持的配置类型

- **存储类**: storage（对象存储）
- **AI类**: llm（大语言模型）、tts（文本转语音）、stt（语音转文本）、image_gen（图像生成）、image_recognition（图像识别）、ocr（文字识别）、translation（机器翻译）
- **通信类**: sms（短信服务）、email（邮件服务）
- **支付类**: payment（支付服务）
- **地图类**: map（地图服务）

### 已支持的服务商

**存储类**：
- local（本地存储）
- minio（MinIO）
- aliyun-oss（阿里云OSS）
- tencent-cos（腾讯云COS）
- qiniu-kodo（七牛云Kodo）

**大模型**：
- openai-gpt（OpenAI GPT）
- anthropic-claude（Anthropic Claude）
- aliyun-qwen（阿里云通义千问）
- baidu-wenxin（百度文心一言）
- tencent-hunyuan（腾讯混元）
- zhipu-glm（智谱GLM）
- moonshot-kimi（月之暗面Kimi）
- deepseek（DeepSeek）

更多服务商请查看`ProviderCodeEnum`枚举类。

---

## 技术支持

如有问题，请联系开发团队或查看源码注释。

**祝你使用愉快！** 🎉
