# BigYun Cloud

BigYun Cloud is a community secondary-development framework based on RuoYi-Cloud. It keeps the familiar microservice layout and adds a Provider configuration center for LLM, OCR, face, storage, payment, and other third-party capabilities.

## What You Get

- Gateway, auth, system, file, gen, job, visual, and common modules.
- SpringDoc/Swagger, MyBatis-Plus, gateway CORS, permission, logging, and code generation integration.
- BigYun Provider configuration center with provider metadata, fields, templates, credentials, and runtime routing.
- Payment skeleton for Alipay and WeChat Pay configuration, menus, SQL, and API structure. No real merchant secrets are included.
- A clean demo module that shows backend layers, frontend page, menu permissions, and SQL.

## Quick Start

1. Install JDK 17, Maven, Node.js, MySQL, Redis, and Nacos.
2. Create databases and import SQL from `BigYun-cloud/sql` in this order: `bigyun_cloud.sql`, `quartz.sql`, `seata.sql`, `provider.sql`, `provider_demo.sql`, `payment_skeleton.sql`, `demo_module.sql`.
3. Import the Nacos templates from `BigYun-cloud/sql/nacos` and adjust MySQL, Redis, and Provider secrets locally.
4. Start backend services: gateway, auth, system, file, gen, job, config, demo, and payment as needed.
5. Start the Vue3 admin app in `BigYun-cloud-Vue3`.
6. Login with the initialized admin account after importing the base SQL.

## Repository Layout

```text
BigYun-cloud/        Backend microservices
BigYun-cloud-Vue3/   Vue3 admin frontend
docs/                Community development documents
```

## Security Notice

This repository must not contain real passwords, AK/SK, tokens, private JDBC URLs, personal server addresses, ID cards, phone numbers, or private medical business data. Provider and OSS configuration files use placeholders by design.

## License

The final open-source license is pending confirmation before public release.