# BigYun Cloud Vue3 管理端

这是 BigYun Cloud 社区版管理端，基于 Vue3、Vite、Pinia 和 Element Plus。社区版首页、Provider 配置中心、支付骨架和 demo 模块已按二开框架口径整理。

## 主要页面

- 首页：`BigYun Cloud` 简洁欢迎页。
- 系统管理：用户、角色、菜单、字典、参数等 RuoYi-Cloud 基础能力。
- 系统工具：代码生成、Swagger/SpringDoc 入口等。
- Provider 配置中心：配置、能力、模型目录、API 模板。
- 支付骨架：支付宝、微信支付配置和订单示例。
- Demo 模块：展示前后端、菜单权限、SQL 的完整二开链路。

## 本地运行

```bash
npm install
npm run dev
```

运行态验收时，后端建议连接本机临时库 `dy-cloud-verify` 和 Nacos namespace `bigyun-cloud-verify`。确认后端 gateway、auth、system、config、demo、payment 可用后，再使用初始化账号 `admin/admin123` 登录并检查首页、系统管理、Swagger/SpringDoc、Provider 配置中心、支付骨架和 Demo 模块。

## 生产构建

```bash
npm run build:prod
```

## 安全约定

- 不在前端仓库写入真实密钥、Token、服务器密码和私有业务数据。
- `src/assets/images/pay.png` 为保留的收款二维码素材，不在社区版抽取中替换。
- 所有第三方能力密钥只在本地 Nacos 或环境变量中配置。
