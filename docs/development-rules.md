# Development Rules

- Keep Controller thin, Service business-focused, and Mapper persistence-only.
- Prefer DTO/VO contracts over raw `Map<String, Object>` request bodies.
- Use Service-level transactions for multi-table writes.
- Keep third-party provider behavior config driven.
- Add SQL and menu permissions for new modules.
- Run Maven compile and frontend build before delivery when practical.
- Runtime verification should use an isolated local database `dy-cloud-verify` and Nacos namespace `bigyun-cloud-verify`.
- Do not use private remote MCP, remote MySQL, or remote Nacos for community acceptance writes.
