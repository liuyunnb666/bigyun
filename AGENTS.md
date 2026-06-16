# BigYun Cloud Agent Guide

- Work in `D:\IdeaProjects\bigyun` for the community framework. Do not modify the private source project.
- Keep changes small and consistent with RuoYi/BigYun module style.
- Never commit real passwords, AK/SK, tokens, private JDBC URLs, personal server addresses, phone numbers, ID cards, or private business data.
- Provider capability changes should preserve DB/config driven routing and avoid hardcoded provider behavior in business modules.
- Backend changes should compile with Maven; frontend changes should build with `npm run build:prod` when dependencies are available.
- Git commits use Chinese conventional style, for example `feat(框架): 初始化 BigYun Cloud 社区二开框架`.