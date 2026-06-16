# BigYun Cloud

BigYun Cloud 是基于 RuoYi-Cloud 的社区二开框架，保留微服务工程结构，并补充 Provider 配置中心，用于管理 LLM、OCR、人脸、存储、支付等第三方能力的配置元数据、模板和运行时路由。

## 你可以得到什么

- 网关、认证、系统、文件、代码生成、定时任务、监控和通用能力模块。
- SpringDoc/Swagger、MyBatis-Plus、网关跨域、权限、日志和代码生成集成。
- BigYun Provider 配置中心，包含 Provider 元数据、字段、模板、凭据占位和能力路由。
- 支付骨架，包含支付宝、微信支付配置、菜单、SQL 和接口结构，不包含真实商户密钥。
- 标准 demo 模块，展示后端分层、前端页面、菜单权限和 SQL 的完整链路。

## 本地独立验证

社区验证必须使用本机基础设施，不要连接私有服务器、远程 MCP、团队内网 MySQL/Redis/Nacos 或任何带真实业务数据的环境。

### 前置条件

- JDK 17
- Maven
- Node.js 与 npm
- MySQL 8
- Redis
- Nacos

### MySQL

创建临时验证库 `dy-cloud-verify`，只用于本地社区版验证。仓库中的 Nacos 模板默认库名仍保留为 `dy-cloud`，不要为了本地验证改动模板文件。

按以下顺序向 `dy-cloud-verify` 导入 `BigYun-cloud/sql` 下的 SQL：

```text
bigyun_cloud.sql
quartz.sql
seata.sql
provider.sql
provider_demo.sql
payment_skeleton.sql
demo_module.sql
```

### Nacos

在本机 Nacos 创建命名空间 `bigyun-cloud-verify`，分组使用 `DEFAULT_GROUP`。将 `BigYun-cloud/sql/nacos/*.yml` 导入该命名空间和分组，并在本地运行时覆盖或替换以下占位配置：

- MySQL：指向本机 `dy-cloud-verify`，可通过 `MYSQL_DATABASE=dy-cloud-verify` 覆盖模板默认值。
- Redis：指向本机 Redis。
- Provider、OSS、支付等密钥：只填本地测试占位值，不写入真实 AK/SK、Token、商户密钥。

### 服务启动顺序

1. 启动本机 MySQL、Redis、Nacos。
2. 确认 `dy-cloud-verify` 已导入 SQL，Nacos `bigyun-cloud-verify` / `DEFAULT_GROUP` 已导入模板。
3. 启动后端基础服务：gateway、auth、system。
4. 按需启动 file、gen、job、config。
5. 按需启动 demo、payment。
6. 在 `BigYun-cloud-Vue3` 启动前端管理端。
7. 使用 `admin/admin123` 登录管理端，检查首页、系统管理、Provider 配置中心和 demo 菜单是否可访问。

### 当前已知阻塞

- 本机 MySQL 不可用、账号无权创建 `dy-cloud-verify`，或 SQL 未完整导入时，后端服务无法完成数据库初始化验证。
- 本机 Redis 不可用或密码配置不一致时，登录、验证码、缓存和网关鉴权会失败。
- 本机 Nacos 不可用、命名空间不是 `bigyun-cloud-verify`、分组不是 `DEFAULT_GROUP`，或模板未导入时，服务无法读取配置或注册发现。
- 如果只连接远程私有 MCP/内网基础设施，即使能启动，也不能作为社区版独立验证结论。

更完整的执行清单见 [Runtime Verification Guide](docs/runtime-verification.md)。

## 仓库结构

```text
BigYun-cloud/        后端微服务
BigYun-cloud-Vue3/   Vue3 管理前端
docs/                社区开发文档
```

## 安全约定

仓库不得包含真实密码、AK/SK、Token、私有 JDBC 地址、个人服务器地址、身份证、手机号或私有医疗业务数据。Provider 和 OSS 配置文件只保留占位符。

## License

最终开源协议将在公开发布前确认。
