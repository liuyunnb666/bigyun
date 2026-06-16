import request from '@/utils/request'

export function listProviderConfig(query) {
  return request({
    url: '/config/config/list',
    method: 'get',
    params: query
  })
}

export function getProviderConfig(configId) {
  return request({
    url: '/config/config/' + configId,
    method: 'get'
  })
}

export function addProviderConfig(data) {
  return request({
    url: '/config/config',
    method: 'post',
    data
  })
}

export function updateProviderConfig(data) {
  return request({
    url: '/config/config',
    method: 'put',
    data
  })
}

export function delProviderConfig(configId) {
  return request({
    url: '/config/config/' + configId,
    method: 'delete'
  })
}

export function changeProviderConfigStatus(configId, status) {
  return request({
    url: `/config/config/${configId}/status/${status}`,
    method: 'put'
  })
}

export function setDefaultProviderConfig(configId) {
  return request({
    url: `/config/config/${configId}/default`,
    method: 'put'
  })
}

export function listProviderCapability(query) {
  return request({
    url: '/config/config/capability/list',
    method: 'get',
    params: query
  })
}

export function getProviderCapability(capabilityId) {
  return request({
    url: '/config/config/capability/detail',
    method: 'get',
    params: { capabilityId }
  })
}

export function setDefaultProviderCapability(capabilityId) {
  return request({
    url: '/config/config/capability/default',
    method: 'put',
    params: { capabilityId }
  })
}

export function addProviderCapability(data) {
  return request({
    url: '/config/config/capability/add',
    method: 'post',
    data
  })
}

export function updateProviderCapability(data) {
  return request({
    url: '/config/config/capability/edit',
    method: 'put',
    data
  })
}

export function changeProviderCapabilityStatus(capabilityId, status) {
  return request({
    url: '/config/config/capability/status',
    method: 'put',
    params: { capabilityId, status }
  })
}

export function delProviderCapability(capabilityIds) {
  return request({
    url: '/config/config/capability/delete',
    method: 'delete',
    params: { capabilityIds: Array.isArray(capabilityIds) ? capabilityIds.join(',') : capabilityIds }
  })
}

export function listProviderCapabilityLogs(query) {
  return request({
    url: '/config/config/capability/logs',
    method: 'get',
    params: query
  })
}

export function refreshProviderRuntimeCache(params) {
  return request({
    url: '/config/config/runtime-cache/refresh',
    method: 'post',
    params
  })
}

export function getProviderRuntimeSnapshot(params) {
  return request({
    url: '/config/config/runtime-cache/snapshot',
    method: 'get',
    params
  })
}

export function listProviderRuntimeSnapshots(params) {
  return request({
    url: '/config/config/runtime-cache/snapshots',
    method: 'get',
    params
  })
}
