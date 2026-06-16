import request from '@/utils/request'

export function listCapabilityCandidates(query) {
  return request({
    url: '/config/capability/model/candidates',
    method: 'get',
    params: query
  })
}

export function listCapabilityRelations(query) {
  return request({
    url: '/config/capability/model/relations',
    method: 'get',
    params: query
  })
}

export function getCapabilityRelation(relationId) {
  return request({
    url: '/config/capability/model/relation/' + relationId,
    method: 'get'
  })
}

export function compareCapabilityModels(params) {
  return request({
    url: '/config/capability/model/compare',
    method: 'get',
    params
  })
}

export function addCapabilityRelation(data) {
  return request({
    url: '/config/capability/model/relation',
    method: 'post',
    data
  })
}

export function updateCapabilityRelation(data) {
  return request({
    url: '/config/capability/model/relation',
    method: 'put',
    data
  })
}

export function delCapabilityRelation(relationIds) {
  return request({
    url: '/config/capability/model/relation/' + relationIds,
    method: 'delete'
  })
}

export function switchCapabilityDefaultModel(data) {
  return request({
    url: '/config/capability/model/switch',
    method: 'put',
    data
  })
}
