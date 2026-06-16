import request from '@/utils/request'

export function listProviderModelCatalog(query) {
  return request({
    url: '/provider/model-catalog/list',
    method: 'get',
    params: query
  })
}

export function getProviderModelCatalog(modelId) {
  return request({
    url: '/provider/model-catalog/detail',
    method: 'get',
    params: { modelId }
  })
}

export function addProviderModelCatalog(data) {
  return request({
    url: '/provider/model-catalog/add',
    method: 'post',
    data
  })
}

export function updateProviderModelCatalog(data) {
  return request({
    url: '/provider/model-catalog/edit',
    method: 'put',
    data
  })
}

export function changeProviderModelCatalogStatus(modelId, isEnabled) {
  return request({
    url: '/provider/model-catalog/status',
    method: 'put',
    params: { modelId, isEnabled }
  })
}

export function delProviderModelCatalog(modelIds) {
  return request({
    url: '/provider/model-catalog/delete',
    method: 'delete',
    params: { modelIds: Array.isArray(modelIds) ? modelIds.join(',') : modelIds }
  })
}
