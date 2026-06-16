import { expect } from '@playwright/test'
import fs from 'node:fs'
import path from 'node:path'

// 当前 worker 内所有被 Mock 拦截到的接口调用都会先暂存在这里，afterAll 再统一写入 HTML 报告。
export const apiRecords = []

// 报告只保留排查接口形态需要的 headers，避免把 token、cookie 等敏感信息写进本地报告。
const SENSITIVE_HEADERS = ['authorization', 'cookie', 'admin-token']

export function buildSuccessResponse(data, msg = 'success') {
  return { code: 200, msg, data }
}

export function buildRowsResponse(rows, msg = 'success') {
  return { code: 200, msg, rows, total: rows.length }
}

export function buildPageData(rows, pageNum = 1, pageSize = 10) {
  return { rows, total: rows.length, pageNum, pageSize }
}

export async function mockAdminSession(page) {
  // PC 管理端通过 Admin-Token 判断登录态，测试里只写入假 token，不请求真实后端登录。
  await page.addInitScript(() => {
    document.cookie = 'Admin-Token=pc-api-e2e-token; path=/'
  })
}

export async function routeAndRecord(page, matcher, options) {
  const {
    module = 'unknown',
    name,
    method,
    response,
    assertBody,
    status = 200
  } = options

  await page.route(matcher, async (route) => {
    const request = route.request()
    // 同一个 URL 可能同时存在 GET/POST，例如就诊人列表和新增就诊人；method 不匹配时交给下一个 route。
    if (method && request.method().toUpperCase() !== method.toUpperCase()) {
      await route.fallback()
      return
    }

    // 先解析真实请求体，再生成 Mock 响应，确保报告能看到前端实际发出去的参数。
    const requestBody = parseBody(request)
    const responseBody = typeof response === 'function'
      ? await response({ request, requestBody })
      : response
    const record = {
      module,
      name,
      method: request.method(),
      url: request.url(),
      headers: summarizeHeaders(request.headers()),
      requestBody,
      responseBody,
      status,
      assertion: 'passed',
      error: ''
    }

    try {
      // assertBody 用来校验关键入参，避免接口“被调用了”但请求体字段是错的。
      if (assertBody) {
        await assertBody(requestBody)
      }
    } catch (error) {
      record.assertion = 'failed'
      record.error = error?.message || String(error)
    }

    apiRecords.push(record)
    await route.fulfill({
      status,
      contentType: 'application/json',
      body: JSON.stringify(responseBody)
    })
  })
}

export async function expectApiCalled(name) {
  // 用轮询等待接口记录出现，兼容页面初始化时的异步请求和浏览器侧 fetch。
  await expect.poll(() => apiRecords.some((item) => item.name === name)).toBeTruthy()
}

export function expectNoFailedApiAssertions() {
  // route 内部不会直接中断请求，最后统一检查所有记录中的断言失败，报告也能保留失败详情。
  const failed = apiRecords.filter((item) => item.assertion !== 'passed')
  expect(failed, failed.map((item) => `${item.name}: ${item.error}`).join('\n')).toHaveLength(0)
}

export function writeApiCallReport(projectName) {
  // 自定义接口报告和 Playwright 默认报告分目录保存，避免 Playwright 清理 outputFolder 时删掉接口报告。
  const outputDir = path.resolve('playwright-report')
  fs.mkdirSync(outputDir, { recursive: true })
  fs.writeFileSync(
    path.join(outputDir, 'api-call-report.html'),
    renderReport(projectName, apiRecords),
    'utf8'
  )
}

function parseBody(request) {
  // JSON 请求体按对象展示，非 JSON 请求体保留原始字符串，方便后续兼容表单或 multipart 场景。
  const raw = request.postData()
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    return raw
  }
}

function summarizeHeaders(headers) {
  // headers 摘要只展示 content-type、accept、x-guest-id 这类排查字段，敏感 header 默认过滤。
  return Object.fromEntries(
    Object.entries(headers)
      .filter(([key]) => !SENSITIVE_HEADERS.includes(key.toLowerCase()))
      .filter(([key]) => ['content-type', 'accept', 'x-guest-id'].includes(key.toLowerCase()))
  )
}

function renderReport(projectName, records) {
  // 报告使用纯静态 HTML，直接双击即可打开，不依赖 Playwright 服务。
  const rows = records.map((record, index) => `
    <tr class="${record.assertion === 'passed' ? 'ok' : 'fail'}">
      <td>${index + 1}</td>
      <td>${escapeHtml(record.module)}</td>
      <td>${escapeHtml(record.name)}</td>
      <td>${escapeHtml(record.method)}</td>
      <td class="url">${escapeHtml(record.url)}</td>
      <td><pre>${escapeHtml(formatJson(record.headers))}</pre></td>
      <td><pre>${escapeHtml(formatJson(record.requestBody))}</pre></td>
      <td><pre>${escapeHtml(formatJson(record.responseBody))}</pre></td>
      <td>${escapeHtml(record.assertion)}</td>
      <td>${escapeHtml(record.error)}</td>
    </tr>
  `).join('')

  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <title>${escapeHtml(projectName)} API 调用报告</title>
  <style>
    body { font-family: Arial, "Microsoft YaHei", sans-serif; margin: 24px; color: #1f2937; }
    h1 { font-size: 24px; margin-bottom: 8px; }
    .summary { color: #64748b; margin-bottom: 18px; }
    table { border-collapse: collapse; width: 100%; table-layout: fixed; }
    th, td { border: 1px solid #d8dee9; padding: 8px; vertical-align: top; font-size: 12px; }
    th { background: #f1f5f9; text-align: left; }
    pre { white-space: pre-wrap; word-break: break-word; margin: 0; max-height: 260px; overflow: auto; }
    .url { word-break: break-all; }
    .ok td:first-child { border-left: 4px solid #16a34a; }
    .fail td:first-child { border-left: 4px solid #dc2626; }
  </style>
</head>
<body>
  <h1>${escapeHtml(projectName)} API 调用报告</h1>
  <div class="summary">共记录 ${records.length} 次接口调用。报告由 Playwright Mock 拦截生成，不请求真实后端。</div>
  <table>
    <thead><tr><th>#</th><th>模块</th><th>接口</th><th>方法</th><th>URL</th><th>Headers 摘要</th><th>请求体</th><th>响应体</th><th>断言</th><th>错误</th></tr></thead>
    <tbody>${rows}</tbody>
  </table>
</body>
</html>`
}

function formatJson(value) {
  if (value == null) return ''
  return typeof value === 'string' ? value : JSON.stringify(value, null, 2)
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
