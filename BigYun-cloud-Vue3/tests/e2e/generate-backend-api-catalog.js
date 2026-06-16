import fs from 'node:fs'
import path from 'node:path'

const backendRoot = path.resolve('..', 'BigYun-cloud')
const outputDir = path.resolve('playwright-report')
const outputFile = path.join(outputDir, 'backend-api-catalog.html')

const controllerFiles = walk(backendRoot)
  .filter((file) => file.endsWith('Controller.java'))
  .sort()

const rows = controllerFiles.flatMap(parseController)
const stats = {
  controllers: controllerFiles.length,
  mappings: rows.length,
  playwright: rows.filter((item) => item.coverage === 'Playwright Mock').length,
  mockMvc: rows.filter((item) => item.coverage === 'MockMvc 或 *IT').length
}

fs.mkdirSync(outputDir, { recursive: true })
fs.writeFileSync(outputFile, renderCatalog(rows, stats), 'utf8')
console.log(`Generated ${outputFile}`)
console.log(`Controllers: ${stats.controllers}, mappings: ${stats.mappings}`)

function parseController(file) {
  const source = fs.readFileSync(file, 'utf8')
  const lines = source.split(/\r?\n/)
  const packageName = source.match(/package\s+([\w.]+);/)?.[1] || ''
  const className = path.basename(file, '.java')
  const classPrefix = extractClassPrefix(source)
  const result = []

  lines.forEach((line, index) => {
    const mapping = extractMapping(line)
    if (!mapping) return
    const fullPath = joinPath(classPrefix, mapping.path)
    result.push({
      module: classifyModule(file, fullPath),
      controller: className,
      packageName,
      method: mapping.method,
      path: fullPath || '(未显式声明 path)',
      source: path.relative(path.resolve('..'), file).replace(/\\/g, '/'),
      line: index + 1,
      coverage: classifyCoverage(file, fullPath)
    })
  })

  return result
}

function extractClassPrefix(source) {
  const classIndex = source.search(/class\s+\w+Controller\b/)
  const head = classIndex > 0 ? source.slice(0, classIndex) : source
  const matches = [...head.matchAll(/@RequestMapping\s*\(([^)]*)\)/g)]
  const match = matches.at(-1)
  return match ? extractPath(match[1]) : ''
}

function extractMapping(line) {
  const annotation = line.match(/@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\s*(?:\(([^)]*)\))?/)
  if (!annotation) return null
  const [, type, args = ''] = annotation
  return {
    method: extractHttpMethod(type, args),
    path: extractPath(args)
  }
}

function extractHttpMethod(type, args) {
  const direct = {
    GetMapping: 'GET',
    PostMapping: 'POST',
    PutMapping: 'PUT',
    DeleteMapping: 'DELETE',
    PatchMapping: 'PATCH'
  }[type]
  if (direct) return direct
  const method = args.match(/RequestMethod\.(GET|POST|PUT|DELETE|PATCH)/)?.[1]
  return method || 'ANY'
}

function extractPath(args) {
  if (!args) return ''
  const named = args.match(/(?:value|path)\s*=\s*(?:\{)?\s*"([^"]+)"/)
  if (named) return named[1]
  const first = args.match(/^\s*"([^"]+)"/)
  return first ? first[1] : ''
}

function joinPath(prefix, pathValue) {
  const value = [prefix, pathValue]
    .filter(Boolean)
    .join('/')
    .replace(/\/+/g, '/')
  return value.startsWith('/') ? value : `/${value}`
}

function classifyModule(file, apiPath) {
  if (apiPath.includes('/auth') || apiPath === '/code') return 'auth'
  if (apiPath.includes('/pay') || apiPath.includes('/payment')) return 'pay'
  if (apiPath.includes('/provider')) return 'provider'
  if (apiPath.includes('/config') || file.includes('bigyun-config')) return 'config'
  if (apiPath.includes('/system')) return 'system'
  if (apiPath.includes('/monitor')) return 'monitor'
  if (apiPath.includes('/tool')) return 'tool'
  if (apiPath.includes('/demo')) return 'demo'
  if (apiPath.includes('/file')) return 'file'
  return path.relative(backendRoot, file).split(path.sep)[0] || 'unknown'
}

function classifyCoverage(file, apiPath) {
  const lower = `${file} ${apiPath}`.toLowerCase()
  if (
    lower.includes('/inner/') ||
    lower.includes('/remote') ||
    lower.includes('/callback') ||
    lower.includes('/webhook') ||
    lower.includes('/upload') ||
    lower.includes('feign')
  ) {
    return 'MockMvc 或 *IT'
  }
  return 'Playwright Mock'
}

function renderCatalog(items, summary) {
  const grouped = groupBy(items, (item) => item.module)
  const sections = Object.entries(grouped).map(([module, moduleRows]) => `
    <h2>${escapeHtml(module)} (${moduleRows.length})</h2>
    <table>
      <thead><tr><th>#</th><th>Controller</th><th>Method</th><th>Path</th><th>覆盖建议</th><th>源码</th></tr></thead>
      <tbody>
        ${moduleRows.map((item, index) => `
          <tr>
            <td>${index + 1}</td>
            <td>${escapeHtml(item.controller)}</td>
            <td>${escapeHtml(item.method)}</td>
            <td class="path">${escapeHtml(item.path)}</td>
            <td>${escapeHtml(item.coverage)}</td>
            <td class="source">${escapeHtml(`${item.source}:${item.line}`)}</td>
          </tr>
        `).join('')}
      </tbody>
    </table>
  `).join('')

  return `<!doctype html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8" />
  <title>BigYun 后端接口目录</title>
  <style>
    body { font-family: Arial, "Microsoft YaHei", sans-serif; margin: 24px; color: #1f2937; }
    h1 { font-size: 24px; margin-bottom: 8px; }
    h2 { margin-top: 28px; font-size: 18px; }
    .summary { color: #64748b; margin-bottom: 18px; }
    table { border-collapse: collapse; width: 100%; table-layout: fixed; margin-bottom: 18px; }
    th, td { border: 1px solid #d8dee9; padding: 8px; vertical-align: top; font-size: 12px; }
    th { background: #f1f5f9; text-align: left; }
    .path, .source { word-break: break-all; }
  </style>
</head>
<body>
  <h1>BigYun 后端接口目录</h1>
  <div class="summary">
    扫描 Controller ${summary.controllers} 个，Mapping ${summary.mappings} 条。
    建议 Playwright Mock 覆盖 ${summary.playwright} 条，MockMvc 或 *IT 覆盖 ${summary.mockMvc} 条。
  </div>
  ${sections}
</body>
</html>`
}

function walk(dir) {
  if (!fs.existsSync(dir)) return []
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) return walk(fullPath)
    return fullPath
  })
}

function groupBy(items, keyFn) {
  return items.reduce((acc, item) => {
    const key = keyFn(item)
    acc[key] ||= []
    acc[key].push(item)
    return acc
  }, {})
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}
