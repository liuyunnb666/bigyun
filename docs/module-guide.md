# Module Guide

BigYun Cloud keeps the RuoYi-Cloud style microservice split:

- `bigyun-gateway`: gateway routing, auth filter, CORS, Swagger aggregation.
- `bigyun-auth`: login, token, captcha, and account authentication.
- `bigyun-modules/bigyun-system`: users, roles, menus, dictionaries, parameters, notices, logs.
- `bigyun-modules/bigyun-config`: Provider configuration center.
- `bigyun-modules/bigyun-file`: local/object storage abstraction.
- `bigyun-modules/bigyun-gen`: code generator.
- `bigyun-modules/bigyun-job`: Quartz scheduling.
- `bigyun-modules/bigyun-demo`: community sample module.
- `bigyun-modules/bigyun-payment`: payment skeleton.