# BigYun Cloud 后端

BigYun Cloud 后端基于 RuoYi-Cloud 二开整理，保留网关、认证、系统管理、代码生成、定时任务、文件服务、监控和通用能力，并增强 Provider 配置中心。

## 模块说明

- `bigyun-gateway`：统一网关、跨域、鉴权过滤和路由入口。
- `bigyun-auth`：认证中心，负责验证码、登录、Token 和账号认证。
- `bigyun-common`：公共工具、权限、安全、日志、数据源、MyBatis-Plus、Swagger/SpringDoc 等基础能力。
- `bigyun-modules/bigyun-system`：用户、角色、菜单、字典、参数、公告、日志等系统管理能力。
- `bigyun-modules/bigyun-config`：Provider 配置中心，负责第三方能力元数据、字段、模板、凭据和运行时路由。
- `bigyun-modules/bigyun-file`：本地存储和对象存储抽象。
- `bigyun-modules/bigyun-gen`：代码生成。
- `bigyun-modules/bigyun-job`：Quartz 定时任务。
- `bigyun-modules/bigyun-demo`：社区版标准三层示例模块。
- `bigyun-modules/bigyun-payment`：支付宝、微信支付配置和订单骨架。
- `bigyun-visual`：监控可视化模块。

## 本地启动

完整步骤见根目录 [Quick Start](../docs/quick-start.md)。后端启动前需要准备：

1. 本机 MySQL 8、Redis、Nacos。
2. MySQL 默认库 `dy-cloud`，并按顺序导入 `sql` 下的基础脚本。
3. Nacos namespace `bigyun-cloud`，group `DEFAULT_GROUP`，并导入 `sql/nacos/*.yml`。
4. 在本地 Nacos 或环境变量中配置 MySQL、Redis、OSS、Provider、支付等占位值。

常用环境变量：

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

编译：

```bash
mvn clean compile -DskipTests -Pdev
```

建议先启动核心服务：

```text
bigyun-gateway                      8080
bigyun-auth                         9200
bigyun-system-service               9201
bigyun-config-service               9205
```

扩展服务按需启动：

```text
bigyun-file-service                 9300
bigyun-gen                          9202
bigyun-job                          9203
bigyun-demo                         9210
bigyun-payment                      9211
```

## 二开入口

新增业务模块时优先参考 `bigyun-modules/bigyun-demo`：

- 后端保持 Controller、Service、Mapper、Domain/DTO/VO 分层。
- 新增表结构、初始化数据、菜单权限和按钮权限 SQL。
- 需要新路由时调整 Nacos 中的 gateway 配置。
- 前端新增 API、页面组件和菜单绑定。
- 交付前执行后端编译和前端构建。

## 独立运行验收

发布前隔离验收建议使用临时库 `dy-cloud-verify` 和 Nacos namespace `bigyun-cloud-verify`，并通过环境变量或本地 Nacos 配置覆盖默认值。完整清单见 [Runtime Verification Guide](../docs/runtime-verification.md)。

## 安全约定

- 不提交真实密码、AK/SK、Token、私有 JDBC 地址和公网私有配置。
- Provider、OSS、支付配置只保留字段结构、占位符或本地测试值。
- 私有业务模块不进入社区版。
