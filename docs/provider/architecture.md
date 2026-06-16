# Provider Architecture

Provider calls are designed to be configuration driven:

`business module -> bigyun-config-remote -> bigyun-config-service -> ProviderServiceImpl -> ProviderHandlerFactory -> concrete handler`

Provider metadata, field definitions, credential records, API templates, and capability routing live in database tables and can be adjusted without changing business code. Real credentials must be filled only in local/private environments.