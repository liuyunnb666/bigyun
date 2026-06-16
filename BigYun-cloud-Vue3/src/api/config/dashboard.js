import request from '@/utils/request'

// 获取服务配置总览数据
export function getDashboardOverview() {
  return request({
    url: '/config/dashboard/overview',
    method: 'get'
  })
}
