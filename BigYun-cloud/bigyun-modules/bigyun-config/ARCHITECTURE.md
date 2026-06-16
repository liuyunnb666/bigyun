# Provider配置管理系统 - 架构说明文档

## 📐 架构概览

本文档面向开发者和AI智能体，帮助快速理解系统架构和实现原理。

---

## 核心设计思想

### 1. 枚举驱动 + 数据库存储

```
Java枚举（元数据定义）
    ↓ 启动时自动同步
数据库表（元数据存储）
    ↓ 运行时查询
配置实例（具体配置）
```

**为什么这样设计？**

- **枚举**：类型安全、编译时检查、IDE友好
- **数据库**：运行时可配置、支持动态查询、前端展示
- **自动同步**：保证两者一致性，开发者只需维护枚举

### 2. 工厂模式 + 策略模式

```java
// 工厂负责路由
ProviderHandlerFactory.getHandler(configType, providerCode)
    ↓
// 策略负责执行
IProviderHandler.execute(config, request)
```

**参考实现**：`LoginStrategyFactory`

**优势**：
- 新增Handler只需加`@Component`，自动注册
- 无需修改工厂代码
- 符合开闭原则

---

## 核心组件

### 1. 枚举管理器 (ProviderEnumManager)

**职责**：应用启动时将枚举同步到数据库

**实现**：
```java
@Component
public class ProviderEnumManager implements CommandLineRunner {
    public void run(String... args) {
        // 同步配置类型枚举
        for (ProviderConfigTypeEnum type : values()) {
            // 插入或更新到 sys_provider_meta
        }
        // 同步服务商枚举
        for (ProviderCodeEnum provider : values()) {
            // 插入或更新到 sys_provider_meta
        }
    }
}
```

**关键点**：
- 实现`CommandLineRunner`，启动时自动执行
- 使用事务保证数据一致性
- 只新增和更新，不删除（保证数据安全）

### 2. Handler工厂 (ProviderHandlerFactory)

**职责**：管理所有Handler，提供路由功能

**数据结构**：
```java
Map<String, Map<String, IProviderHandler<?, ?>>> handlerRegistry
// 示例：{"storage": {"local": LocalStorageHandler, "aliyun-oss": AliyunOSSHandler}}
```

**注册流程**：
```java
public ProviderHandlerFactory(List<IProviderHandler<?, ?>> handlers) {
    // Spring自动注入所有Handler
    for (IProviderHandler<?, ?> handler : handlers) {
        String configType = handler.getSupportedConfigType();
        String providerCode = handler.getSupportedProviderCode();
        handlerRegistry.computeIfAbsent(configType, k -> new HashMap<>())
                       .put(providerCode, handler);
    }
}
```

**关键点**：
- 构造函数注入，Spring自动收集所有`IProviderHandler`实现类
- 二级Map结构，快速定位Handler
- 启动时打印注册日志，便于调试

### 3. 统一服务 (ProviderServiceImpl)

**职责**：提供统一的服务调用接口

**调用流程**：
```java
public <T, R> R execute(String configType, ProviderRequest<T> request) {
    // 1. 查询数据库获取默认配置
    ProviderConfigDTO config = providerConfigService.selectDefaultProviderConfigInternal(configType);
    
    // 2. 通过工厂获取Handler
    IProviderHandler<T, R> handler = handlerFactory.getHandler(
        config.getConfigType(), 
        config.getProviderCode()
    );
    
    // 3. 验证配置
    if (!handler.validateConfig(config)) {
        throw new ServiceException("配置验证失败");
    }
    
    // 4. 执行服务调用
    ProviderResponse<R> response = handler.execute(config, request);
    
    // 5. 返回结果
    return response.getData();
}
```

**关键点**：
- 配置驱动：从数据库读取配置，决定使用哪个Handler
- 统一接口：业务代码只依赖`IProviderService`
- 异常处理：统一处理异常，返回友好错误信息

### 4. Handler接口 (IProviderHandler)

**职责**：定义服务提供商的标准接口

**接口定义**：
```java
public interface IProviderHandler<T, R> {
    // 执行服务调用
    ProviderResponse<R> execute(ProviderConfigDTO config, ProviderRequest<T> request);
    
    // 获取支持的配置类型
    String getSupportedConfigType();
    
    // 获取支持的服务商代码
    String getSupportedProviderCode();
    
    // 验证配置有效性
    default boolean validateConfig(ProviderConfigDTO config) {
        return config != null && config.getAccessKey() != null;
    }
}
```

**实现示例**：
```java
@Component
public class AliyunOSSHandler implements IProviderHandler<StorageRequest, StorageResponse> {
    
    public ProviderResponse<StorageResponse> execute(ProviderConfigDTO config, ProviderRequest<StorageRequest> request) {
        // 调用阿里云OSS SDK
        // ...
        return ProviderResponse.success(response);
    }
    
    public String getSupportedConfigType() { return "storage"; }
    public String getSupportedProviderCode() { return "aliyun-oss"; }
}
```

**关键点**：
- 泛型设计：`<T, R>`分别表示请求和响应类型
- 自描述：通过`getSupportedXxx()`方法声明支持的类型
- 配置验证：每个Handler自己验证配置的有效性

---

## 数据模型

### 1. 枚举层

**ProviderConfigTypeEnum** - 配置类型枚举

```java
public enum ProviderConfigTypeEnum {
    STORAGE("storage", "对象存储", "OSS、MinIO、本地存储等"),
    LLM("llm", "大语言模型", "OpenAI GPT、Claude等"),
    // ...
    
    private final String code;        // 代码
    private final String name;        // 名称
    private final String description; // 描述
}
```

**ProviderCodeEnum** - 服务商枚举

```java
public enum ProviderCodeEnum {
    LOCAL_STORAGE("local", "本地存储", ProviderConfigTypeEnum.STORAGE),
    ALIYUN_OSS("aliyun-oss", "阿里云OSS", ProviderConfigTypeEnum.STORAGE),
    // ...
    
    private final String code;                      // 代码
    private final String name;                      // 名称
    private final ProviderConfigTypeEnum type;      // 所属类型
}
```

### 2. 元数据层

**sys_provider_meta** - 存储枚举定义

| 字段 | 类型 | 说明 |
|------|------|------|
| meta_id | BIGINT | 主键 |
| meta_type | VARCHAR(32) | 元数据类型：config_type / provider_code |
| meta_code | VARCHAR(64) | 元数据代码 |
| meta_name | VARCHAR(100) | 元数据名称 |
| meta_description | VARCHAR(500) | 元数据描述 |
| parent_code | VARCHAR(64) | 父级代码（provider_code关联config_type） |
| status | CHAR(1) | 状态：0=正常 1=停用 |
| sort_order | INT | 排序 |

**唯一索引**：`(meta_type, meta_code)`

### 3. 配置层

**sys_provider_config** - 存储配置实例

| 字段 | 类型 | 说明 |
|------|------|------|
| config_id | BIGINT | 主键 |
| config_type | VARCHAR(64) | 配置类型 |
| provider_code | VARCHAR(64) | 服务商代码 |
| provider_name | VARCHAR(100) | 服务商名称 |
| endpoint | VARCHAR(255) | 访问端点 |
| region | VARCHAR(100) | 地域 |
| bucket_name | VARCHAR(100) | Bucket名称 |
| access_key | VARCHAR(500) | Access Key（加密） |
| secret_key | VARCHAR(500) | Secret Key（加密） |
| domain | VARCHAR(255) | 自定义域名 |
| base_path | VARCHAR(255) | 基础路径 |
| ext_params_json | TEXT | 扩展参数JSON |
| is_default | CHAR(1) | 是否默认：Y / N |
| status | CHAR(1) | 状态：0=正常 1=停用 |

**唯一索引**：`(config_type, provider_code)`

---

## 调用时序图

```
业务代码                ProviderServiceImpl        ProviderHandlerFactory      Handler              数据库
   |                           |                           |                      |                    |
   |-- execute("storage") ---->|                           |                      |                    |
   |                           |-- 查询默认配置 ------------------------------------------>|
   |                           |<-- 返回配置(aliyun-oss) ----------------------------------|
   |                           |-- getHandler("storage", "aliyun-oss") -->|              |
   |                           |<-- 返回AliyunOSSHandler ------------------|              |
   |                           |-- validateConfig() ---------------------------------------->|
   |                           |<-- true ---------------------------------------------------|
   |                           |-- execute(config, request) ------------------------------>|
   |                           |                           |                      |-- 调用OSS SDK
   |                           |<-- ProviderResponse -----------------------------------|
   |<-- 返回结果 --------------|                           |                      |
```

---

## 扩展点

### 1. 添加新配置类型

**修改点**：
1. `ProviderConfigTypeEnum` - 添加枚举值
2. `ProviderCodeEnum` - 添加服务商枚举
3. 创建Request/Response类
4. 实现Handler

**无需修改**：
- ProviderEnumManager（自动同步）
- ProviderHandlerFactory（自动注册）
- ProviderServiceImpl（无需改动）

### 2. 添加新服务商

**修改点**：
1. `ProviderCodeEnum` - 添加枚举值
2. 实现Handler

**无需修改**：
- 其他所有代码

### 3. 修改配置

**修改点**：
1. 数据库`sys_provider_config`表

**无需修改**：
- 任何代码
- 无需重启应用

---

## 关键技术点

### 1. 枚举与数据库同步

**问题**：如何保证枚举和数据库一致？

**解决**：
- 应用启动时自动同步
- 使用`CommandLineRunner`接口
- 事务保证原子性
- 只新增和更新，不删除

### 2. Handler自动注册

**问题**：如何避免手动维护Handler映射？

**解决**：
- Spring依赖注入自动收集
- 构造函数注入`List<IProviderHandler>`
- Handler通过`getSupportedXxx()`自描述
- 工厂自动构建二级Map

### 3. 配置热切换

**问题**：如何不重启应用切换服务商？

**解决**：
- 配置存储在数据库
- 每次调用时查询数据库
- 修改`is_default`字段即可切换
- 无需重启应用

### 4. 密钥加密

**问题**：如何保护敏感信息？

**解决**：
- 保存时加密：`ProviderSecretUtils.encrypt()`
- 使用时解密：`ProviderSecretUtils.decrypt()`
- 前端脱敏：`****KlMn`
- 不返回原始值

---

## 最佳实践

### 1. Handler实现

```java
@Component
public class XxxHandler implements IProviderHandler<XxxRequest, XxxResponse> {
    
    private static final Logger log = LoggerFactory.getLogger(XxxHandler.class);
    
    @Override
    public ProviderResponse<XxxResponse> execute(ProviderConfigDTO config, ProviderRequest<XxxRequest> request) {
        try {
            // 1. 解析配置
            String apiKey = config.getAccessKey();
            
            // 2. 构建请求
            XxxRequest xxxRequest = request.getData();
            
            // 3. 调用第三方SDK
            // XxxClient client = new XxxClient(apiKey);
            // XxxResult result = client.doSomething(xxxRequest);
            
            // 4. 构建响应
            XxxResponse xxxResponse = new XxxResponse();
            // xxxResponse.setXxx(result.getXxx());
            
            // 5. 返回成功
            log.info("服务调用成功: {}", xxxRequest);
            return ProviderResponse.success(xxxResponse);
            
        } catch (Exception e) {
            log.error("服务调用失败", e);
            return ProviderResponse.fail("ERROR_CODE", e.getMessage());
        }
    }
    
    @Override
    public String getSupportedConfigType() {
        return "xxx_type";
    }
    
    @Override
    public String getSupportedProviderCode() {
        return "xxx-provider";
    }
    
    @Override
    public boolean validateConfig(ProviderConfigDTO config) {
        return config != null 
            && config.getAccessKey() != null
            && config.getSecretKey() != null;
    }
}
```

### 2. 业务代码使用

```java
@Service
public class BusinessService {
    
    @Autowired
    private IProviderService providerService;
    
    public void doSomething() {
        // 1. 构建请求
        XxxRequest request = new XxxRequest();
        request.setXxx("xxx");
        
        // 2. 调用服务（使用默认配置）
        XxxResponse response = providerService.execute("xxx_type", 
            new ProviderRequest<>(request));
        
        // 3. 处理响应
        String result = response.getXxx();
    }
}
```

### 3. 配置管理

```sql
-- 添加配置
INSERT INTO sys_provider_config (
    config_type, provider_code, provider_name,
    access_key, secret_key,
    is_default, status, create_by
) VALUES (
    'xxx_type', 'xxx-provider', 'Xxx服务商',
    'ENCRYPTED_ACCESS_KEY', 'ENCRYPTED_SECRET_KEY',
    'Y', '0', 'admin'
);

-- 切换默认配置
UPDATE sys_provider_config 
SET is_default = 'Y' 
WHERE config_type = 'xxx_type' AND provider_code = 'xxx-provider';

-- 停用配置
UPDATE sys_provider_config 
SET status = '1' 
WHERE config_id = 123;
```

---

## 总结

### 核心优势

1. **类型安全** - 枚举保证编译时检查
2. **配置驱动** - 数据库配置，无需改代码
3. **自动注册** - Handler自动注册，无需维护映射
4. **易于扩展** - 添加新服务商只需3步
5. **统一接口** - 业务代码与实现解耦

### 适用场景

- 需要集成多个第三方服务
- 需要支持服务商切换
- 需要统一管理配置
- 需要易于扩展的架构

### 不适用场景

- 只有一个服务提供商
- 配置固定不变
- 不需要运行时切换

---

**文档版本**：v1.0  
**最后更新**：2026-05-21  
**作者**：BigYun开发团队
