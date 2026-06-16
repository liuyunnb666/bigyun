import request from '@/utils/request'

export function listProviderConfig(query) {
  return request({
    url: '/provider/config/list',
    method: 'get',
    params: query
  })
}

export function getProviderConfig(configId) {
  return request({
    url: '/provider/config/' + configId,
    method: 'get'
  })
}

export function addProviderConfig(data) {
  return request({
    url: '/provider/config',
    method: 'post',
    data
  })
}

export function updateProviderConfig(data) {
  return request({
    url: '/provider/config',
    method: 'put',
    data
  })
}

export function delProviderConfig(configId) {
  return request({
    url: '/provider/config/' + configId,
    method: 'delete'
  })
}

export function changeProviderConfigStatus(configId, status) {
  return request({
    url: `/provider/config/${configId}/status/${status}`,
    method: 'put'
  })
}

export function setDefaultProviderConfig(configId) {
  return request({
    url: `/provider/config/${configId}/default`,
    method: 'put'
  })
}

export function listProviderCapability(query) {
  return request({
    url: '/provider/config/capability/list',
    method: 'get',
    params: query
  })
}

export function getProviderCapability(capabilityId) {
  return request({
    url: '/provider/config/capability/detail',
    method: 'get',
    params: { capabilityId }
  })
}

export function setDefaultProviderCapability(capabilityId) {
  return request({
    url: '/provider/config/capability/default',
    method: 'put',
    params: { capabilityId }
  })
}

export function addProviderCapability(data) {
  return request({
    url: '/provider/config/capability/add',
    method: 'post',
    data
  })
}

export function updateProviderCapability(data) {
  return request({
    url: '/provider/config/capability/edit',
    method: 'put',
    data
  })
}

export function changeProviderCapabilityStatus(capabilityId, status) {
  return request({
    url: '/provider/config/capability/status',
    method: 'put',
    params: { capabilityId, status }
  })
}

export function delProviderCapability(capabilityIds) {
  return request({
    url: '/provider/config/capability/delete',
    method: 'delete',
    params: { capabilityIds: Array.isArray(capabilityIds) ? capabilityIds.join(',') : capabilityIds }
  })
}

export function listProviderCapabilityLogs(query) {
  return request({
    url: '/provider/config/capability/logs',
    method: 'get',
    params: query
  })
}

export function refreshProviderRuntimeCache(params) {
  return request({
    url: '/provider/config/runtime-cache/refresh',
    method: 'post',
    params
  })
}

export function getProviderRuntimeSnapshot(params) {
  return request({
    url: '/provider/config/runtime-cache/snapshot',
    method: 'get',
    params
  })
}

export function listProviderRuntimeSnapshots(params) {
  return request({
    url: '/provider/config/runtime-cache/snapshots',
    method: 'get',
    params
  })
}
