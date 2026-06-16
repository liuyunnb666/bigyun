import request from '@/utils/request'

export function listDemo(query) {
  return request({
    url: '/demo/item/list',
    method: 'get',
    params: query
  })
}

export function getDemo(id) {
  return request({
    url: `/demo/item/${id}`,
    method: 'get'
  })
}

export function saveDemo(data) {
  return request({
    url: '/demo/item',
    method: 'post',
    data
  })
}
