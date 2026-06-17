# BigYun Cloud

BigYun Cloud 是一个基于 RuoYi-Cloud 的社区二开框架。仓库保留网关、认证、系统管理、文件、代码生成、定时任务、监控等微服务结构，并补充 Provider 配置中心，用于管理 LLM、OCR、人脸、存储、支付等第三方能力的配置元数据、模板和运行时路由。

## 你可以得到什么

- 一套 Spring Boot 3 / Spring Cloud 微服务基础工程。
- Vue3 + Vite + Element Plus 管理端。
- SpringDoc/Swagger、MyBatis-Plus、网关跨域、权限、日志和代码生成集成。
- BigYun Provider 配置中心：Provider 元数据、字段、模板、凭据占位和能力路由。
- 支付骨架：支付宝、微信支付配置、菜单、SQL 和接口结构，不包含真实商户密钥。
- Demo 模块：展示后端分层、前端页面、菜单权限和 SQL 的完整二开链路。

## 3 分钟启动路线

详细步骤见 [Quick Start](docs/quick-start.md)。首次拉取仓库后，按这个顺序准备：

1. 安装 JDK 17、Maven、Node.js、MySQL 8、Redis、Nacos。
2. 在 MySQL 创建默认库 `dy-cloud`，按顺序导入 `BigYun-cloud/sql` 下的 SQL。
3. 在 Nacos 创建 namespace `bigyun-cloud`，group 使用 `DEFAULT_GROUP`，导入 `BigYun-cloud/sql/nacos/*.yml`。
4. 在本地 Nacos 或环境变量里配置 MySQL、Redis、Provider、OSS、支付等占位值，不要写真实密钥。
5. 启动核心后端服务：gateway、auth、system、config。
6. 在 `BigYun-cloud-Vue3` 执行 `npm ci` 和 `npm run dev`，打开 `http://localhost:8081`。
7. 使用初始化账号 `admin/admin123` 登录管理端，检查首页、系统管理、Provider 配置中心和 Demo 模块。

## 常用配置变量

后端 Nacos 与基础设施配置支持通过环境变量覆盖：

```text
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=bigyun-cloud
NACOS_GROUP=DEFAULT_GROUP
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=dy-cloud
MYSQL_USERNAME=root
MYSQL_PASSWORD=change_me
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

默认快速启动使用 `dy-cloud` 和 `bigyun-cloud`。如果你要做社区版发布前隔离验收，可以改用 `dy-cloud-verify` 和 `bigyun-cloud-verify`，具体见 [Runtime Verification Guide](docs/runtime-verification.md)。

## 仓库结构

```text
BigYun-cloud/        后端微服务
BigYun-cloud-Vue3/   Vue3 管理前端
docs/                社区开发文档
```

## 文档导航

- [Quick Start](docs/quick-start.md)：从 clone 到本地启动。
- [Module Guide](docs/module-guide.md)：模块职责速览。
- [Code Navigation](docs/code-navigation.md)：代码入口、路由和 Mapper 位置。
- [Development Rules](docs/development-rules.md)：二开约定和交付检查。
- [Runtime Verification Guide](docs/runtime-verification.md)：维护者发布前本地隔离验收。
- [Provider Architecture](docs/provider/architecture.md)：Provider 配置中心设计说明。
- [Payment Skeleton](docs/payment/payment-skeleton.md)：支付骨架说明。

## 安全约定

仓库不得包含真实密码、AK/SK、Token、私有 JDBC 地址、个人服务器地址、身份证、手机号或私有业务数据。Provider、OSS、支付配置只能保留占位符或本地测试值。

## License

最终开源协议将在公开发布前确认。
