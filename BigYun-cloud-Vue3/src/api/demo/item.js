import request from '@/utils/request'
export function listDemoItem(query) { return request({ url: '/demo/item/list', method: 'get', params: query }) }
export function getDemoItem(itemId) { return request({ url: `/demo/item/${itemId}`, method: 'get' }) }
export function addDemoItem(data) { return request({ url: '/demo/item', method: 'post', data }) }
export function updateDemoItem(data) { return request({ url: '/demo/item', method: 'put', data }) }
export function delDemoItem(itemIds) { return request({ url: `/demo/item/${itemIds}`, method: 'delete' }) }