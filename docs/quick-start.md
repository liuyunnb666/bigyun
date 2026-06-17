# Quick Start

本手册面向第一次从 GitHub 拉取 BigYun Cloud 的使用者，目标是把项目在本机跑起来，并找到二开的入口。

## 1. 环境准备

请先准备以下软件：

- JDK 17
- Maven 3.8+
- Node.js 18+ 与 npm
- MySQL 8
- Redis
- Nacos 2.x

默认端口约定：

| 服务 | 默认地址 |
| --- | --- |
| Gateway | `http://localhost:8080` |
| Vue3 管理端 | `http://localhost:8081` |
| Nacos | `127.0.0.1:8848` |
| MySQL | `localhost:3306` |
| Redis | `localhost:6379` |

## 2. 初始化 MySQL

创建默认数据库：

```sql
CREATE DATABASE `dy-cloud` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

按顺序导入 `BigYun-cloud/sql` 下的脚本：

```text
bigyun_cloud.sql
quartz.sql
seata.sql
provider.sql
provider_demo.sql
payment_skeleton.sql
demo_module.sql
```

如果你只是在做发布前隔离验收，可以创建 `dy-cloud-verify`，并通过 `MYSQL_DATABASE=dy-cloud-verify` 覆盖默认库名。

## 3. 初始化 Nacos

在本机 Nacos 创建 namespace：

```text
bigyun-cloud
```

group 使用：

```text
DEFAULT_GROUP
```

把 `BigYun-cloud/sql/nacos/*.yml` 导入该 namespace 和 group。导入后根据本机环境调整以下配置：

```text
MYSQL_HOST=localhost
MYSQL_PORT=3306
MYSQL_DATABASE=dy-cloud
MYSQL_USERNAME=root
MYSQL_PASSWORD=change_me
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
```

Provider、OSS、支付等密钥字段只填本地测试占位值，不要填写真实 AK/SK、Token 或商户密钥。

后端服务的 Nacos 连接也可以通过环境变量覆盖：

```text
NACOS_SERVER_ADDR=127.0.0.1:8848
NACOS_NAMESPACE=bigyun-cloud
NACOS_GROUP=DEFAULT_GROUP
```

如果你使用隔离验收 namespace，则启动后端前设置 `NACOS_NAMESPACE=bigyun-cloud-verify`，并把 Nacos 模板导入该 namespace。

## 4. 编译后端

进入后端目录：

```bash
cd BigYun-cloud
mvn clean compile -DskipTests -Pdev
```

`bootstrap.yml` 中的 `@activatedProfile@` 由 Maven profile 过滤，首次启动建议使用 `-Pdev`。

## 5. 启动后端服务

建议先启动核心服务：

| 顺序 | 服务 | 模块路径 | 端口 |
| --- | --- | --- | --- |
| 1 | gateway | `bigyun-gateway` | `8080` |
| 2 | auth | `bigyun-auth` | `9200` |
| 3 | system | `bigyun-modules/bigyun-system/bigyun-system-service` | `9201` |
| 4 | config | `bigyun-modules/bigyun-config/bigyun-config-service` | `9205` |

可以使用 IDE 直接运行对应 `*Application` 启动类，也可以使用 Maven：

```bash
mvn -pl bigyun-gateway -am spring-boot:run -Dspring-boot.run.profiles=dev -Pdev
mvn -pl bigyun-auth -am spring-boot:run -Dspring-boot.run.profiles=dev -Pdev
mvn -pl bigyun-modules/bigyun-system/bigyun-system-service -am spring-boot:run -Dspring-boot.run.profiles=dev -Pdev
mvn -pl bigyun-modules/bigyun-config/bigyun-config-service -am spring-boot:run -Dspring-boot.run.profiles=dev -Pdev
```

按需启动扩展服务：

| 服务 | 模块路径 | 端口 |
| --- | --- | --- |
| file | `bigyun-modules/bigyun-file/bigyun-file-service` | `9300` |
| gen | `bigyun-modules/bigyun-gen` | `9202` |
| job | `bigyun-modules/bigyun-job` | `9203` |
| demo | `bigyun-modules/bigyun-demo` | `9210` |
| payment | `bigyun-modules/bigyun-payment` | `9211` |

## 6. 启动前端

进入前端目录：

```bash
cd BigYun-cloud-Vue3
npm ci
npm run dev
```

默认访问地址：

```text
http://localhost:8081
```

开发环境前端通过 `/dev-api` 代理到 gateway：

```text
http://localhost:8080
```

初始化账号：

```text
admin/admin123
```

登录后建议检查：首页、系统管理、Provider 配置中心、支付骨架、Demo 模块。

## 7. 从 Demo 开始二开

新增业务模块时，可以先参考 `bigyun-demo`：

- 后端：按 Controller、Service、Mapper、Domain/DTO/VO 分层组织。
- SQL：新增表结构、初始化数据、菜单权限和按钮权限。
- Nacos：按服务名新增或调整 `*-dev.yml`，必要时在 gateway 路由中增加路径。
- 前端：新增 API 文件、页面组件、路由或菜单绑定。
- 权限：确认菜单 `perms` 与后端接口权限标识一致。
- 验证：编译后端、构建前端，登录管理端走一遍页面和接口。

## 8. 常见问题

- 服务读不到配置：检查 Nacos namespace、group、Data ID 是否和 `NACOS_NAMESPACE`、`NACOS_GROUP`、服务名一致。
- 数据库连接失败：检查 `MYSQL_DATABASE`、账号密码和 SQL 是否完整导入。
- 登录或验证码失败：检查 Redis 是否启动，密码是否和 Nacos 配置一致。
- 前端无法启动：确认端口 `8081` 未被占用，依赖安装使用 `npm ci`。
- 接口 404：确认 gateway 已启动，目标服务已注册到 Nacos，路由配置已导入。

## 9. 发布前隔离验收

维护者在公开发布前应使用独立本地环境验收，避免连接个人或团队私有基础设施。详见 [Runtime Verification Guide](runtime-verification.md)。
