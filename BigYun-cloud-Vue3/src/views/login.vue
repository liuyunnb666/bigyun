<template>
  <div class="login">
    <el-form ref="loginRef" :model="loginForm" :rules="loginRules" class="login-form">
      <h3 class="title">{{ title }}</h3>

      <el-tabs v-model="loginType" class="login-tabs">
        <el-tab-pane label="账号登录" name="1" />
        <el-tab-pane label="手机登录" name="2" />
        <el-tab-pane label="邮箱登录" name="3" />
        <el-tab-pane label="扫码登录" name="4" />
      </el-tabs>

      <template v-if="loginType === '1'">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" type="text" size="large" autocomplete="off" placeholder="账号">
            <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            autocomplete="off"
            placeholder="密码"
            show-password
            @keyup.enter="handleLogin"
          >
            <template #prefix><svg-icon icon-class="password" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
      </template>

      <template v-if="loginType === '2'">
        <el-form-item prop="phone">
          <el-input v-model="loginForm.phone" type="text" size="large" autocomplete="off" placeholder="手机号" @keyup.enter="handleLogin">
            <template #prefix><svg-icon icon-class="phone" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
      </template>

      <template v-if="loginType === '3'">
        <el-form-item prop="email">
          <el-input v-model="loginForm.email" type="text" size="large" autocomplete="off" placeholder="邮箱" @keyup.enter="handleLogin">
            <template #prefix><svg-icon icon-class="email" class="el-input__icon input-icon" /></template>
          </el-input>
        </el-form-item>
      </template>

      <template v-if="loginType === '4'">
        <div class="scan-login">
          <div class="scan-canvas-wrap" :class="{ expired: scanExpired }">
            <canvas ref="scanCanvasRef" class="scan-canvas"></canvas>
            <div v-if="scanCreating" class="scan-mask">二维码生成中...</div>
            <div v-else-if="scanExpired" class="scan-mask">二维码已过期</div>
          </div>
          <div class="scan-status">{{ scanStatusText }}</div>
          <div class="scan-countdown">剩余时间：{{ formatRemain(scanRemainSeconds) }}</div>
          <div class="scan-tip">请使用已登录的移动端或门户端扫码确认登录</div>
        </div>
      </template>

      <el-form-item v-if="captchaEnabled && loginType !== '4'" prop="code">
        <el-input
          v-model="loginForm.code"
          size="large"
          autocomplete="off"
          placeholder="验证码"
          style="width: 63%"
          @keyup.enter="handleLogin"
        >
          <template #prefix><svg-icon icon-class="validCode" class="el-input__icon input-icon" /></template>
        </el-input>
        <div class="login-code">
          <img :src="codeUrl" class="login-code-img" @click="getCode" />
        </div>
      </el-form-item>

      <el-checkbox v-if="loginType === '1'" v-model="loginForm.rememberMe" style="margin:0 0 25px 0;">记住密码</el-checkbox>

      <el-form-item v-if="loginType !== '4'" style="width:100%;">
        <el-button :loading="loading" size="large" type="primary" style="width:100%;" @click.prevent="handleLogin">
          <span v-if="!loading">登录</span>
          <span v-else>登录中...</span>
        </el-button>
        <div v-if="register" style="float: right;">
          <router-link class="link-type" :to="'/register'">立即注册</router-link>
        </div>
      </el-form-item>

      <el-form-item v-else style="width:100%;">
        <el-button
          :loading="scanCreating"
          size="large"
          type="primary"
          plain
          style="width:100%;"
          :disabled="loading"
          @click.prevent="refreshScanCode"
        >
          <span v-if="!scanCreating">刷新二维码</span>
          <span v-else>生成中...</span>
        </el-button>
      </el-form-item>
    </el-form>
    <div class="el-login-footer">
      <span>{{ footerContent }}</span>
    </div>
  </div>
</template>

<script setup>
import { getCodeImg, scanCreate, scanStatus } from '@/api/login'
import Cookies from 'js-cookie'
import QRCode from 'qrcode'
import { encrypt, decrypt } from '@/utils/jsencrypt'
import useUserStore from '@/store/modules/user'
import defaultSettings from '@/settings'

const title = import.meta.env.VITE_APP_TITLE
const footerContent = defaultSettings.footerContent
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const { proxy } = getCurrentInstance()

// 登录类型：1-账号 2-手机号 3-邮箱 4-扫码。
const loginType = ref('1')

const loginForm = ref({
  username: '',
  password: '',
  phone: '',
  email: '',
  rememberMe: false,
  code: '',
  uuid: ''
})

const loginRules = {
  username: [{ required: true, trigger: 'blur', message: '请输入您的账号' }],
  password: [{ required: true, trigger: 'blur', message: '请输入您的密码' }],
  phone: [
    { required: true, trigger: 'blur', message: '请输入手机号' },
    { pattern: /^1[3-9]\d{9}$/, trigger: 'blur', message: '请输入正确的手机号' }
  ],
  email: [
    { required: true, trigger: 'blur', message: '请输入邮箱' },
    { type: 'email', trigger: 'blur', message: '请输入正确的邮箱地址' }
  ],
  code: [{ required: true, trigger: 'change', message: '请输入验证码' }]
}

const codeUrl = ref('')
const loading = ref(false)
const captchaEnabled = ref(true)
const register = ref(false)
const redirect = ref(undefined)

const scanCanvasRef = ref(null)
const scanSid = ref('')
const scanLoginCode = ref('')
const scanSessionStatus = ref('')
const scanRemainSeconds = ref(0)
const scanCreating = ref(false)
const scanLogging = ref(false)
const scanError = ref('')

let scanPollTimer = null
let scanCountdownTimer = null
let scanFlowVersion = 0

const scanExpired = computed(() => scanRemainSeconds.value <= 0)

const scanStatusText = computed(() => {
  if (scanCreating.value) {
    return '二维码生成中...'
  }
  if (scanError.value) {
    return scanError.value
  }
  if (scanExpired.value) {
    return '二维码已过期，请刷新后重试'
  }
  if (scanLogging.value) {
    return '已确认，正在登录...'
  }
  if (scanSessionStatus.value === 'RESOLVED') {
    return '已扫码，请在移动端确认登录'
  }
  if (scanSessionStatus.value === 'CONFIRMED') {
    return '登录确认中...'
  }
  return '等待扫码'
})

watch(route, (newRoute) => {
  redirect.value = newRoute.query && newRoute.query.redirect
}, { immediate: true })

watch(loginType, (newType) => {
  proxy.$refs.loginRef?.clearValidate()
  if (newType === '4') {
    refreshScanCode()
  } else {
    stopScanFlow()
  }
})

onUnmounted(() => {
  stopScanFlow()
})

function handleLogin() {
  proxy.$refs.loginRef.validate(valid => {
    if (!valid) {
      return
    }
    loading.value = true
    if (loginType.value === '1' && loginForm.value.rememberMe) {
      Cookies.set('username', loginForm.value.username, { expires: 30 })
      Cookies.set('password', encrypt(loginForm.value.password), { expires: 30 })
      Cookies.set('rememberMe', loginForm.value.rememberMe, { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
    }

    const loginData = {
      type: loginType.value,
      username: loginForm.value.username,
      password: loginForm.value.password,
      phone: loginForm.value.phone,
      email: loginForm.value.email,
      code: loginForm.value.code,
      uuid: loginForm.value.uuid
    }

    userStore.login(loginData).then(() => {
      afterLoginRedirect()
    }).catch(() => {
      loading.value = false
      if (loginType.value === '1' && captchaEnabled.value) {
        getCode()
      }
    })
  })
}

function afterLoginRedirect() {
  const query = route.query
  const otherQueryParams = Object.keys(query).reduce((acc, cur) => {
    if (cur !== 'redirect') {
      acc[cur] = query[cur]
    }
    return acc
  }, {})
  router.push({ path: redirect.value || '/', query: otherQueryParams })
}

function getCode() {
  getCodeImg().then(res => {
    captchaEnabled.value = res.captchaEnabled === undefined ? true : res.captchaEnabled
    if (captchaEnabled.value) {
      codeUrl.value = 'data:image/gif;base64,' + res.img
      loginForm.value.uuid = res.uuid
    }
  })
}

function getCookie() {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
  const rememberMe = Cookies.get('rememberMe')
  loginForm.value = {
    ...loginForm.value,
    username: username === undefined ? loginForm.value.username : username,
    password: password === undefined ? loginForm.value.password : decrypt(password),
    rememberMe: rememberMe === undefined ? false : Boolean(rememberMe)
  }
}

function clearScanPollTimer() {
  if (scanPollTimer) {
    clearTimeout(scanPollTimer)
    scanPollTimer = null
  }
}

function clearScanCountdownTimer() {
  if (scanCountdownTimer) {
    clearInterval(scanCountdownTimer)
    scanCountdownTimer = null
  }
}

function clearScanCanvas() {
  if (!scanCanvasRef.value) {
    return
  }
  const ctx = scanCanvasRef.value.getContext('2d')
  if (ctx) {
    ctx.clearRect(0, 0, scanCanvasRef.value.width, scanCanvasRef.value.height)
  }
}

function stopScanFlow() {
  scanFlowVersion += 1
  clearScanPollTimer()
  clearScanCountdownTimer()
  scanSid.value = ''
  scanLoginCode.value = ''
  scanSessionStatus.value = ''
  scanRemainSeconds.value = 0
  scanCreating.value = false
  scanLogging.value = false
  scanError.value = ''
  clearScanCanvas()
}

function startScanCountdown() {
  clearScanCountdownTimer()
  scanCountdownTimer = setInterval(() => {
    if (scanRemainSeconds.value > 0) {
      scanRemainSeconds.value -= 1
    }
    if (scanRemainSeconds.value <= 0) {
      clearScanCountdownTimer()
    }
  }, 1000)
}

function scheduleScanPoll(version) {
  clearScanPollTimer()
  if (version !== scanFlowVersion || scanExpired.value || scanLogging.value) {
    return
  }
  scanPollTimer = setTimeout(() => pollScanStatus(version), 1500)
}

function refreshScanCode() {
  const version = scanFlowVersion + 1
  stopScanFlow()
  scanFlowVersion = version
  scanCreating.value = true
  scanCreate().then(res => {
    if (version !== scanFlowVersion) {
      return
    }
    const data = res.data || res
    scanSid.value = data.sid
    scanLoginCode.value = data.loginCode
    scanSessionStatus.value = data.status
    scanRemainSeconds.value = Number(data.expiresIn || 300)
    drawScanCode(data)
    scanCreating.value = false
    startScanCountdown()
    scheduleScanPoll(version)
  }).catch(() => {
    if (version === scanFlowVersion) {
      scanCreating.value = false
      scanError.value = '二维码生成失败，请稍后重试'
    }
  })
}

function drawScanCode(data) {
  if (!scanCanvasRef.value) {
    return
  }
  const content = JSON.stringify({ sid: data.sid, loginCode: data.loginCode })
  QRCode.toCanvas(scanCanvasRef.value, content, { width: 192, margin: 1 })
}

function pollScanStatus(version) {
  if (!scanSid.value || version !== scanFlowVersion) {
    return
  }
  scanStatus(scanSid.value).then(res => {
    if (version !== scanFlowVersion) {
      return
    }
    const data = res.data || res
    scanSessionStatus.value = data.status
    if (data.expiresIn !== undefined) {
      scanRemainSeconds.value = Number(data.expiresIn)
    }
    if (data.status === 'CONFIRMED' && data.grantCode) {
      scanLogging.value = true
      userStore.login({
        type: '4',
        grantCode: data.grantCode,
        sid: scanSid.value
      }).then(() => {
        afterLoginRedirect()
      }).catch(() => {
        scanLogging.value = false
        scanError.value = '扫码登录失败，请刷新二维码后重试'
      })
      return
    }
    scheduleScanPoll(version)
  }).catch(() => {
    if (version === scanFlowVersion) {
      scanError.value = '扫码状态查询失败，请刷新二维码后重试'
    }
  })
}

function formatRemain(seconds) {
  const value = Math.max(Number(seconds) || 0, 0)
  const minute = String(Math.floor(value / 60)).padStart(2, '0')
  const second = String(value % 60).padStart(2, '0')
  return `${minute}:${second}`
}

getCode()
getCookie()
</script>

<style lang="scss" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url('../assets/images/login-background.jpg');
  background-size: cover;
}

.title {
  margin: 0 auto 24px;
  text-align: center;
  color: #1f2f46;
}

.login-form {
  width: 400px;
  padding: 28px 28px 10px;
  border-radius: 6px;
  background: #ffffff;
}

.login-tabs {
  margin-bottom: 12px;
}

.login-code {
  width: 33%;
  height: 40px;
  float: right;

  img {
    width: 100%;
    height: 40px;
    cursor: pointer;
    vertical-align: middle;
  }
}

.scan-login {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0 18px;
}

.scan-canvas-wrap {
  position: relative;
  width: 192px;
  height: 192px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.scan-canvas {
  width: 192px;
  height: 192px;
}

.scan-mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: rgba(31, 47, 70, 0.72);
}

.scan-status {
  margin-top: 12px;
  color: #1f2f46;
}

.scan-countdown,
.scan-tip {
  margin-top: 6px;
  color: #606266;
  font-size: 13px;
}

.el-login-footer {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 40px;
  line-height: 40px;
  text-align: center;
  color: #fff;
  font-size: 12px;
}
</style>
