import { createServer } from 'vite'
import { fileURLToPath } from 'node:url'

export default async function globalSetup() {
  // Playwright 自己拉起 Vite，保证测试命令不依赖你手动先开 dev server。
  const port = Number(process.env.E2E_PORT || 8188)
  // 端口可用 E2E_PORT 临时覆盖，避免和本地正在运行的前端调试服务冲突。
  const configFile = fileURLToPath(new URL('../../vite.config.js', import.meta.url))
  const server = await createServer({
    configFile,
    server: {
      host: '127.0.0.1',
      port,
      strictPort: true,
      open: false
    }
  })

  await server.listen()

  return async function globalTeardown() {
    // 测试结束主动关闭 Vite，避免端口被占用影响下一次执行。
    await server.close()
  }
}
