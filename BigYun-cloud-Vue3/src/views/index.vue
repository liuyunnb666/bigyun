<template>
  <div class="home-page">
    <section class="welcome">
      <div>
        <p class="eyebrow">BigYun Cloud Community</p>
        <h1>BigYun Cloud 社区二开框架</h1>
        <p class="summary">面向微服务后台的 Vue3 管理端，保留系统管理、监控运维、代码工具和 Provider 配置中心等通用能力，方便开发者拉取后快速配置并继续二次开发。</p>
      </div>
      <el-tag effect="dark" size="large">Vue3 + Element Plus</el-tag>
    </section>

    <el-row :gutter="16" class="feature-grid">
      <el-col v-for="item in features" :key="item.title" :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="feature-card">
          <el-icon class="feature-icon"><component :is="item.icon" /></el-icon>
          <h3>{{ item.title }}</h3>
          <p>{{ item.desc }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <el-card shadow="never" class="section-card">
          <template #header><span>社区版定位</span></template>
          <el-timeline>
            <el-timeline-item timestamp="基础框架" placement="top">登录、权限、菜单、字典、参数、部门、岗位、角色和用户管理。</el-timeline-item>
            <el-timeline-item timestamp="开发工具" placement="top">在线用户、定时任务、操作日志、登录日志、代码生成和表单构建。</el-timeline-item>
            <el-timeline-item timestamp="扩展中心" placement="top">Provider 能力、模型目录、API 模板和多类型配置页面，便于接入第三方能力。</el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="never" class="section-card">
          <template #header><span>快捷入口</span></template>
          <el-space direction="vertical" alignment="stretch" fill>
            <el-button type="primary" plain @click="go('/provider/config')">Provider 配置</el-button>
            <el-button plain @click="go('/pay/demo')">支付骨架</el-button>
            <el-button plain @click="go('/demo/index')">示例模块</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Index">
import { Connection, Monitor, Setting, Tools } from '@element-plus/icons-vue'

const router = useRouter()
const features = [
  { title: '系统管理', icon: Setting, desc: '账号、权限、菜单和基础配置开箱即用。' },
  { title: '监控运维', icon: Monitor, desc: '保留在线用户、任务和日志等后台能力。' },
  { title: '开发工具', icon: Tools, desc: '保留代码生成、表单构建和通用组件。' },
  { title: 'Provider 中心', icon: Connection, desc: '统一维护模型、模板、能力和供应商配置。' }
]
function go(path) { router.push(path) }
</script>

<style scoped lang="scss">
.home-page { min-height: calc(100vh - 84px); padding: 24px; background: #f6f8fb; color: #1f2937; }
.welcome { display: flex; align-items: flex-start; justify-content: space-between; gap: 20px; padding: 28px; margin-bottom: 16px; border: 1px solid #e5e7eb; border-radius: 8px; background: #fff; }
.welcome h1 { margin: 8px 0 12px; font-size: 28px; font-weight: 700; line-height: 1.25; }
.eyebrow { margin: 0; color: #2563eb; font-size: 13px; font-weight: 700; letter-spacing: 0; }
.summary { max-width: 760px; margin: 0; color: #4b5563; font-size: 15px; line-height: 1.7; }
.feature-grid { margin-bottom: 16px; }
.feature-card, .section-card { border-radius: 8px; }
.feature-card { min-height: 156px; }
.feature-card h3 { margin: 14px 0 8px; font-size: 17px; }
.feature-card p { margin: 0; color: #64748b; line-height: 1.6; }
.feature-icon { width: 38px; height: 38px; border-radius: 8px; color: #2563eb; background: #eff6ff; }
.section-card { margin-bottom: 16px; }
@media (max-width: 768px) { .home-page { padding: 16px; } .welcome { display: block; padding: 20px; } .welcome h1 { font-size: 24px; } .welcome .el-tag { margin-top: 16px; } }
</style>