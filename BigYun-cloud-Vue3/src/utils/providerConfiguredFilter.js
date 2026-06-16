export const CONFIGURED_PROVIDER_FETCH_SIZE = 2000

export function normalizeProviderCode(value) {
  return value === undefined || value === null ? '' : String(value).trim().toLowerCase()
}

export function providerKey(configType, providerCode) {
  const type = normalizeProviderCode(configType)
  const provider = normalizeProviderCode(providerCode)
  return type && provider ? `${type}/${provider}` : ''
}

export function isEnabledProviderConfig(row) {
  if (!row) {
    return false
  }
  const key = providerKey(row.configType, row.providerCode)
  if (!key) {
    return false
  }
  return row.status === undefined || row.status === null || row.status === '' || String(row.status) === '0'
}

export function buildConfiguredProviderSet(rows = []) {
  return new Set((rows || []).filter(isEnabledProviderConfig).map(row => providerKey(row.configType, row.providerCode)))
}

export function hasConfiguredProvider(row, configuredProviderSet) {
  if (!row || !configuredProviderSet) {
    return false
  }
  return configuredProviderSet.has(providerKey(row.configType, row.providerCode))
}

export function hasConfiguredTargetProvider(row, configuredProviderSet) {
  if (!row || !configuredProviderSet) {
    return false
  }
  return configuredProviderSet.has(providerKey(row.configType, row.targetProviderCode || row.providerCode))
}

export function filterRowsByConfiguredProvider(rows = [], configuredProviderSet, showUnconfigured = false) {
  const list = rows || []
  if (showUnconfigured) {
    return list
  }
  return list.filter(row => hasConfiguredProvider(row, configuredProviderSet))
}

export function filterRowsByConfiguredTargetProvider(rows = [], configuredProviderSet, showUnconfigured = false) {
  const list = rows || []
  if (showUnconfigured) {
    return list
  }
  return list.filter(row => hasConfiguredTargetProvider(row, configuredProviderSet))
}

export function countUnconfiguredProviderRows(rows = [], configuredProviderSet) {
  return (rows || []).filter(row => !hasConfiguredProvider(row, configuredProviderSet)).length
}

export function countUnconfiguredTargetProviderRows(rows = [], configuredProviderSet) {
  return (rows || []).filter(row => !hasConfiguredTargetProvider(row, configuredProviderSet)).length
}

export function localPageRows(rows = [], pageNum = 1, pageSize = 10) {
  const currentPage = Number(pageNum) > 0 ? Number(pageNum) : 1
  const currentSize = Number(pageSize) > 0 ? Number(pageSize) : 10
  const start = (currentPage - 1) * currentSize
  return (rows || []).slice(start, start + currentSize)
}

export function safeDisplayText(value, fallback = '-') {
  if (value === undefined || value === null || String(value).trim() === '') {
    return fallback
  }
  return String(value)
}

export function templateUrlSummary(value) {
  const text = safeDisplayText(value)
  if (text === '-') {
    return text
  }
  return text.replace(/\$\{[^}]*}/g, '{变量}')
}
