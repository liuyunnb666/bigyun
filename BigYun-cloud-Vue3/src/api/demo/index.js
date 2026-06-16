import request from '@/utils/request'

export function listDemo(query) {
  return request({
    url: '/demo/list',
    method: 'get',
    params: query
  })
}

export function getDemo(id) {
  return request({
    url: `/demo/${id}`,
    method: 'get'
  })
}

export function saveDemo(data) {
  return request({
    url: '/demo',
    method: 'post',
    data
  })
}
