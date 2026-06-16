import request from '@/utils/request'
export function getPayDemoOrder(orderNo) { return request({ url: `/payment/demo/${orderNo}`, method: 'get' }) }
export function createPayDemoOrder(data) { return request({ url: '/payment/demo/order', method: 'post', data }) }
export function closePayDemoOrder(orderNo) { return request({ url: `/payment/demo/${orderNo}/close`, method: 'post' }) }
