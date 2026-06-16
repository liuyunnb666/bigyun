import { expect, test } from '@playwright/test'
import {
  buildRowsResponse,
  buildSuccessResponse,
  expectApiCalled,
  expectNoFailedApiAssertions,
  mockAdminSession,
  routeAndRecord,
  writeApiCallReport
} from './helpers.js'

test.describe.configure({ mode: 'serial' })

test.afterAll(() => {
  writeApiCallReport('BigYun PC 社区管理端')
})

test('PC 社区管理端接口通测记录 auth、system、config、provider、pay、demo、file', async ({ page }) => {
  await mockAdminSession(page)
  await registerPcRoutes(page)
  await page.goto('/')

  const calls = [
    {
      name: '验证码',
      url: '/dev-api/code',
      options: { method: 'GET' }
    },
    {
      name: '账号登录',
      url: '/dev-api/auth/loginNew',
      options: {
        method: 'POST',
        body: { type: '1', userName: 'api-e2e-admin', password: 'api-e2e-password', code: '0000', uuid: 'uuid-api-e2e' }
      }
    },
    {
      name: '扫码创建',
      url: '/dev-api/auth/scan/create',
      options: { method: 'POST' }
    },
    {
      name: '扫码状态',
      url: '/dev-api/auth/scan/status?sid=scan-api-e2e',
      options: { method: 'GET' }
    },
    {
      name: '用户信息',
      url: '/dev-api/system/user/getInfo',
      options: { method: 'GET' }
    },
    {
      name: '路由菜单',
      url: '/dev-api/system/menu/getRouters',
      options: { method: 'GET' }
    },
    {
      name: 'Provider 配置列表',
      url: '/dev-api/provider/config/list?configType=llm',
      options: { method: 'GET' }
    },
    {
      name: 'Provider 推荐列表',
      url: '/dev-api/provider/config/recommendations?configType=llm',
      options: { method: 'GET' }
    },
    {
      name: 'Provider 字段',
      url: '/dev-api/provider/config/fields?providerCode=deepseek',
      options: { method: 'GET' }
    },
    {
      name: 'Provider 设为默认',
      url: '/dev-api/provider/config/default?configId=1001',
      options: { method: 'PUT' }
    },
    {
      name: '支付骨架创建',
      url: '/dev-api/payment/demo/order',
      options: {
        method: 'POST',
        body: { orderNo: 'PAY-PC-001', subject: 'BigYun Cloud 社区版测试商品', amount: 1, channelCode: 'alipay' }
      }
    },
    {
      name: '支付骨架查询',
      url: '/dev-api/payment/demo/PAY-PC-001',
      options: { method: 'GET' }
    },
    {
      name: '示例模块列表',
      url: '/dev-api/demo/item/list?pageNum=1&pageSize=10',
      options: { method: 'GET' }
    },
    {
      name: '示例模块保存',
      url: '/dev-api/demo/item',
      options: {
        method: 'POST',
        body: { itemCode: 'DEMO-PC-001', itemName: '社区版示例', status: '0' }
      }
    },
    {
      name: '文件上传',
      url: '/dev-api/file/upload',
      options: {
        method: 'POST',
        body: { filename: 'api-e2e.txt', contentType: 'text/plain' }
      }
    }
  ]

  for (const call of calls) {
    const body = await callApi(page, call.url, call.options)
    expect([0, 200]).toContain(body.code)
    await expectApiCalled(call.name)
  }

  expectNoFailedApiAssertions()
})

async function registerPcRoutes(page) {
  await mockUnhandledDevApi(page)

  await routeAndRecord(page, '**/dev-api/code', {
    module: 'auth',
    name: '验证码',
    method: 'GET',
    response: buildSuccessResponse({ captchaEnabled: false, uuid: 'uuid-api-e2e', img: '' })
  })
  await routeAndRecord(page, '**/dev-api/auth/loginNew', {
    module: 'auth',
    name: '账号登录',
    method: 'POST',
    response: buildSuccessResponse({ access_token: 'pc-api-e2e-token' }),
    assertBody: async (body) => {
      expect(body.type).toBe('1')
      expect(body.userName).toBeTruthy()
      expect(body.password).toBeTruthy()
    }
  })
  await routeAndRecord(page, '**/dev-api/auth/scan/create', {
    module: 'auth',
    name: '扫码创建',
    method: 'POST',
    response: buildSuccessResponse({ sid: 'scan-api-e2e', expireSeconds: 120 })
  })
  await routeAndRecord(page, '**/dev-api/auth/scan/status**', {
    module: 'auth',
    name: '扫码状态',
    method: 'GET',
    response: buildSuccessResponse({ sid: 'scan-api-e2e', status: 'WAITING' })
  })
  await routeAndRecord(page, '**/dev-api/system/user/getInfo', {
    module: 'system',
    name: '用户信息',
    method: 'GET',
    response: {
      code: 200,
      msg: 'success',
      user: { userId: 1, userName: 'admin', nickName: '超级管理员', avatar: '' },
      roles: ['admin'],
      permissions: ['*:*:*']
    }
  })
  await routeAndRecord(page, '**/dev-api/system/menu/getRouters', {
    module: 'system',
    name: '路由菜单',
    method: 'GET',
    response: buildSuccessResponse([])
  })
  await routeAndRecord(page, '**/dev-api/provider/config/list**', {
    module: 'config',
    name: 'Provider 配置列表',
    method: 'GET',
    response: buildRowsResponse([
      { configId: 1001, configType: 'llm', providerCode: 'deepseek', providerName: 'DeepSeek', status: '0' }
    ])
  })
  await routeAndRecord(page, '**/dev-api/provider/config/recommendations**', {
    module: 'config',
    name: 'Provider 推荐列表',
    method: 'GET',
    response: buildSuccessResponse([
      { providerCode: 'deepseek', providerName: 'DeepSeek', integrationStatus: 'READY' }
    ])
  })
  await routeAndRecord(page, '**/dev-api/provider/config/fields**', {
    module: 'config',
    name: 'Provider 字段',
    method: 'GET',
    response: buildSuccessResponse([
      { fieldKey: 'apiKey', fieldLabel: 'API Key', required: true }
    ])
  })
  await routeAndRecord(page, '**/dev-api/provider/config/default**', {
    module: 'provider',
    name: 'Provider 设为默认',
    method: 'PUT',
    response: buildSuccessResponse(true)
  })
  await routeAndRecord(page, '**/dev-api/payment/demo/order', {
    module: 'pay',
    name: '支付骨架创建',
    method: 'POST',
    response: buildSuccessResponse({ orderNo: 'PAY-PC-001', status: 'WAITING' }),
    assertBody: async (body) => {
      expect(body.subject).toBeTruthy()
      expect(body.amount).toBeGreaterThan(0)
    }
  })
  await routeAndRecord(page, '**/dev-api/payment/demo/PAY-PC-001', {
    module: 'pay',
    name: '支付骨架查询',
    method: 'GET',
    response: buildSuccessResponse({ orderNo: 'PAY-PC-001', status: 'WAITING' })
  })
  await routeAndRecord(page, '**/dev-api/demo/item/list**', {
    module: 'demo',
    name: '示例模块列表',
    method: 'GET',
    response: buildRowsResponse([
      { itemId: 1, itemCode: 'DEMO-PC-001', itemName: '社区版示例', status: '0', remark: '基础示例数据' }
    ])
  })
  await routeAndRecord(page, '**/dev-api/demo/item', {
    module: 'demo',
    name: '示例模块保存',
    method: 'POST',
    response: buildSuccessResponse({ id: 1 }),
    assertBody: async (body) => {
      expect(body.itemCode).toBeTruthy()
      expect(body.itemName).toBeTruthy()
    }
  })
  await routeAndRecord(page, '**/dev-api/file/upload', {
    module: 'file',
    name: '文件上传',
    method: 'POST',
    response: buildSuccessResponse({ url: '/dev-api/profile/api-e2e.txt', fileName: 'api-e2e.txt' }),
    assertBody: async (body) => {
      expect(body.filename).toBe('api-e2e.txt')
    }
  })
}

async function mockUnhandledDevApi(page) {
  await page.route('**/dev-api/**', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ code: 200, msg: 'success', data: {}, rows: [] })
    })
  })
}

async function callApi(page, url, options = {}) {
  return page.evaluate(async ({ url: requestUrl, options: requestOptions }) => {
    const fetchOptions = {
      method: requestOptions.method || 'GET',
      headers: { Accept: 'application/json' }
    }
    if (requestOptions.body) {
      fetchOptions.headers['Content-Type'] = 'application/json'
      fetchOptions.body = JSON.stringify(requestOptions.body)
    }
    const response = await fetch(requestUrl, fetchOptions)
    return response.json()
  }, { url, options })
}
