# Runtime Verification Guide

本页用于 BigYun Cloud 社区版发布前的本地独立运行验收。目标是证明仓库拉下来后，配置 MySQL、Redis、Nacos 并导入 SQL 即可启动二开，同时避免把验证数据写入个人、团队或远程私有环境。

## 验收边界

- `D:\IdeaProjects\mySystem` 只作为只读来源，不写入、不格式化、不提交。
- 仓库模板默认库名保持 `dy-cloud`，不要为了本地验收修改模板文件。
- 本地验收使用临时库 `dy-cloud-verify`，通过本地 Nacos 配置或环境变量覆盖数据库名。
- 本地验收使用 Nacos namespace `bigyun-cloud-verify`，group 使用 `DEFAULT_GROUP`。
- 不使用远程私有 MCP、远程 MySQL、远程 Nacos 或个人服务器做社区版写入验收。

## 前置条件

- JDK 17 或兼容 JDK、Maven、Node.js 与 npm。
- 本机 MySQL 可用，并提供只用于验收的账号。该账号需要允许创建 `dy-cloud-verify`、建表和导入 SQL。
- 本机 Redis 可用，默认端口 `6379`。
- 本机 Nacos 可用，默认端口 `8848`。

## SQL 导入

在本机 MySQL 中创建临时库：

```sql
CREATE DATABASE `dy-cloud-verify` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

按以下顺序导入 `BigYun-cloud/sql` 下脚本：

1. `bigyun_cloud.sql`
2. `quartz.sql`
3. `seata.sql`
4. `provider.sql`
5. `provider_demo.sql`
6. `payment_skeleton.sql`
7. `demo_module.sql`

## Nacos 配置

1. 在本机 Nacos 创建 namespace：`bigyun-cloud-verify`。
2. 将 `BigYun-cloud/sql/nacos/*.yml` 导入该 namespace，group 使用 `DEFAULT_GROUP`。
3. 将数据库配置覆盖为本机 `dy-cloud-verify`，例如使用 `MYSQL_DATABASE=dy-cloud-verify`。
4. Redis 指向本机 Redis。
5. OSS、Provider、支付等密钥字段只保留本地测试占位值，真实密钥不要提交。

## 启动顺序

建议先启动核心服务：

1. `bigyun-gateway`
2. `bigyun-auth`
3. `bigyun-system-service`
4. `bigyun-config-service`

按需启动扩展服务：

1. `bigyun-file-service`
2. `bigyun-gen`
3. `bigyun-job`
4. `bigyun-demo`
5. `bigyun-payment`

前端在 `BigYun-cloud-Vue3` 下启动：

```bash
npm install
npm run dev
```

## 验收项

- Nacos 能看到 gateway、auth、system、config、demo、payment 等服务注册。
- Gateway 可访问系统、Provider、Demo、Payment 路由。
- Vue3 管理端可打开并使用初始化账号 `admin/admin123` 登录。
- 首页、系统管理、Swagger/SpringDoc 入口、Provider 配置中心、支付骨架、Demo 模块可访问。
- 运行后复查敏感信息和乱码扫描，确认没有把本地密码、Token、AK/SK 或私有业务数据写回仓库。

## 常见阻塞

- MySQL 只有端口监听，但没有可用本机账号密码。
- Redis 服务未启动或密码配置与 Nacos 不一致。
- Nacos `127.0.0.1:8848` 不通，或没有可启动的本机 Nacos。
- 只有远程私有 MCP 配置可用。该配置不能用于社区版写入验收。
