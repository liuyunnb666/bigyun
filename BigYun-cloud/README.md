# BigYun Cloud 后端

BigYun Cloud 后端基于 RuoYi-Cloud 二开整理，保留网关、认证、系统管理、代码生成、定时任务、文件服务、监控和通用能力，并增强 Provider 配置中心。

## 模块说明

- `bigyun-gateway`：统一网关、跨域、限流和路由入口。
- `bigyun-auth`：认证中心，保留用户名、手机号、邮箱等登录扩展结构。
- `bigyun-common`：公共工具、权限、安全、日志、数据源、MyBatis-Plus、Swagger/SpringDoc 等基础能力。
- `bigyun-modules/bigyun-system`：系统管理基础模块。
- `bigyun-modules/bigyun-config`：Provider 配置中心，负责第三方能力元数据、字段、模板、凭证和运行时路由。
- `bigyun-modules/bigyun-demo`：社区版标准三层示例模块。
- `bigyun-modules/bigyun-payment`：支付宝、微信支付配置和订单骨架。
- `bigyun-visual`：监控可视化模块。

## 本地启动

1. 准备 JDK 17、Maven、MySQL、Redis、Nacos。
2. 按根目录 `README.md` 顺序导入 `sql` 下的基础脚本。
3. 将 `sql/nacos` 下模板导入 Nacos，并在本地替换 MySQL、Redis、OSS、Provider 密钥占位符。
4. 按需启动 `bigyun-gateway`、`bigyun-auth`、`bigyun-system-service`、`bigyun-config-service`、`bigyun-file-service`、`bigyun-job`、`bigyun-demo`、`bigyun-payment`。

## 独立运行验收

- 仓库模板默认库名保持 `dy-cloud`，不要为了本地验收修改模板。
- 本地验收建议创建临时库 `dy-cloud-verify`，并在本机 Nacos 中覆盖数据库名。
- 本机 Nacos namespace 建议使用 `bigyun-cloud-verify`，group 使用 `DEFAULT_GROUP`。
- 不要使用远程私有 MCP、远程 MySQL 或远程 Nacos 做社区版写入验收。
- 完整步骤见根目录 `docs/runtime-verification.md`。

## 编译

```bash
mvn clean compile -DskipTests
```

## 安全约定

- 不提交真实密码、AK/SK、Token、私有 JDBC 地址和公网私有配置。
- Provider 和 OSS 配置只保留字段结构或示例 endpoint，密钥必须使用占位符。
- 私有业务模块不进入社区版。
