# Quick Start

1. Prepare JDK 17, Maven, Node.js, MySQL 8, Redis, and Nacos.
2. Import SQL from `BigYun-cloud/sql`.
3. Import `BigYun-cloud/sql/nacos/*.yml` into Nacos and replace placeholders with local values.
4. Start core services: gateway, auth, system, config, file, gen, job.
5. Start optional sample services: demo and payment.
6. Run the Vue3 admin frontend and login with the initialized admin account.

## Independent Runtime Verification

- Use a local temporary MySQL database `dy-cloud-verify` for acceptance testing.
- Use a local Nacos namespace `bigyun-cloud-verify` and group `DEFAULT_GROUP`.
- Keep repository templates unchanged: the default community database name remains `dy-cloud`.
- Replace database, Redis, OSS, Provider, and payment secrets only in local Nacos or environment variables.
- Do not write community verification data into private remote MCP, remote MySQL, or remote Nacos environments.

See [Runtime Verification Guide](runtime-verification.md) for the full checklist.
