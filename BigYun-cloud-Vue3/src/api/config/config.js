import request from '@/utils/request'

// 查询配置列表
export function listConfig(query) {
  return request({
    url: '/config/config/list',
    method: 'get',
    params: query
  })
}

// 查询配置详情，使用 query 参数，避免路径传参排查困难
export function getConfig(configId) {
  return request({
    url: '/config/config/detail',
    method: 'get',
    params: { configId }
  })
}

// 新增配置
export function addConfig(data) {
  return request({
    url: '/config/config',
    method: 'post',
    data
  })
}

// 修改配置
export function updateConfig(data) {
  return request({
    url: '/config/config',
    method: 'put',
    data
  })
}

// 删除配置，使用 query 参数
export function delConfig(configIds) {
  return request({
    url: '/config/config/delete',
    method: 'delete',
    params: { configIds: Array.isArray(configIds) ? configIds.join(',') : configIds }
  })
}

// 设为默认配置，使用 query 参数
export function setDefaultConfig(configId) {
  return request({
    url: '/config/config/default',
    method: 'put',
    params: { configId }
  })
}

// 修改配置状态，使用 query 参数
export function changeConfigStatus(configId, status) {
  return request({
    url: '/config/config/status',
    method: 'put',
    params: { configId, status }
  })
}

// 查询指定配置类型支持的 Provider 列表
export function listConfigProviders(configType) {
  return request({
    url: '/config/config/providers',
    method: 'get',
    params: { configType }
  })
}

// 查询指定配置类型的推荐 Provider
export function listConfigRecommendations(configType) {
  return request({
    url: '/config/config/recommendations',
    method: 'get',
    params: { configType }
  })
}

// 查询指定 Provider 的动态字段元数据
export function listProviderFields(providerCode) {
  return request({
    url: '/config/config/fields',
    method: 'get',
    params: { providerCode }
  })
}
