import request from '@/utils/request'

export function listProviderApiTemplate(query) {
  return request({
    url: '/provider/config/api-template/list',
    method: 'get',
    params: query
  })
}

export function getProviderApiTemplate(templateId) {
  return request({
    url: `/provider/config/api-template/${templateId}`,
    method: 'get'
  })
}

export function addProviderApiTemplate(data) {
  return request({
    url: '/provider/config/api-template',
    method: 'post',
    data
  })
}

export function updateProviderApiTemplate(data) {
  return request({
    url: '/provider/config/api-template',
    method: 'put',
    data
  })
}

export function delProviderApiTemplate(templateIds) {
  return request({
    url: `/provider/config/api-template/${templateIds}`,
    method: 'delete'
  })
}

export function changeProviderApiTemplateStatus(templateId, isEnabled) {
  return request({
    url: `/provider/config/api-template/status/${templateId}/${isEnabled}`,
    method: 'put'
  })
}

export function testProviderApiTemplate(data) {
  return request({
    url: '/provider/config/api-template/test',
    method: 'post',
    data
  })
}
