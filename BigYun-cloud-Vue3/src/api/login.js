import request from '@/utils/request'

// 登录方法（旧版）
export function login(username, password, code, uuid) {
  return request({
    url: '/auth/login',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: { username, password, code, uuid }
  })
}

// 登录方法（新版 - 支持多种登录方式）
export function loginNew(loginReq) {
  return request({
    url: '/auth/loginNew',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data: loginReq
  })
}

// 创建扫码会话
export function scanCreate() {
  return request({
    url: '/auth/scan/create',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post'
  })
}

// 查询扫码会话状态
export function scanStatus(sid) {
  return request({
    url: '/auth/scan/status',
    headers: {
      isToken: false
    },
    method: 'get',
    params: { sid }
  })
}

// 小程序侧解析会话（预留）
export function scanResolve(data) {
  return request({
    url: '/auth/scan/resolve',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data
  })
}

// 小程序侧确认会话（预留）
export function scanConfirm(data) {
  return request({
    url: '/auth/scan/confirm',
    headers: {
      isToken: false,
      repeatSubmit: false
    },
    method: 'post',
    data
  })
}

// 注册方法
export function register(data) {
  return request({
    url: '/auth/register',
    headers: {
      isToken: false
    },
    method: 'post',
    data: data
  })
}

// 刷新方法
export function refreshToken() {
  return request({
    url: '/auth/refresh',
    method: 'post'
  })
}

// 获取用户详细信息
export function getInfo() {
  return request({
    url: '/system/user/getInfo',
    method: 'get'
  })
}

// 解锁屏幕
export function unlockScreen(password) {
  return request({
    url: '/auth/unlockscreen',
    method: 'post',
    data: { password }
  })
}

// 退出方法
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'delete'
  })
}

// 获取验证码
export function getCodeImg() {
  return request({
    url: '/code',
    headers: {
      isToken: false
    },
    method: 'get',
    timeout: 20000
  })
}
