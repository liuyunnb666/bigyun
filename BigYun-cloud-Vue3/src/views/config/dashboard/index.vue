<template>
  <div class="app-container service-dashboard">
    <div class="page-head">
      <div>
        <div class="page-title">服务配置总览</div>
        <div class="page-desc">聚合查看当前服务配置规模、类型分布和最近变更，并可直接跳转到具体配置页或能力中心。</div>
      </div>
      <div class="page-actions">
        <el-button icon="Refresh" @click="loadDashboard">刷新数据</el-button>
        <el-button type="primary" icon="Connection" @click="goToCapabilityCenter">进入能力中心</el-button>
      </div>
    </div>

    <el-alert
      v-if="loadError"
      class="mb8"
      type="warning"
      :closable="false"
      show-icon
      :title="loadError"
    />

    <el-row :gutter="16" class="summary-row">
      <el-col v-for="item in summaryCards" :key="item.key" :xs="24" :sm="12" :lg="6">
        <el-card shadow="hover" class="summary-card">
          <div class="summary-label">{{ item.title }}</div>
          <div class="summary-value">{{ item.value }}</div>
          <div class="summary-desc">{{ item.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" class="type-card-wrapper">
      <template #header>
        <div class="section-head">
          <span>服务配置分布</span>
          <el-tag type="info">{{ configCards.length }} 类</el-tag>
        </div>
      </template>

      <el-empty
        v-if="!loading && configCards.length === 0"
        description="暂无服务配置数据，请先新增 Provider 配置。"
      />

      <el-row v-else :gutter="16">
        <el-col v-for="item in configCards" :key="item.type" :xs="24" :sm="12" :lg="8" class="type-col">
          <el-card shadow="never" class="type-card">
            <div class="type-head">
              <div>
                <div class="type-title">{{ item.title }}</div>
                <div class="type-desc">{{ item.description }}</div>
              </div>
              <el-tag>{{ item.type }}</el-tag>
            </div>

            <div class="type-stats">
              <div class="stat-row">
                <span>总配置数</span>
                <strong>{{ item.totalCount }}</strong>
              </div>
              <div class="stat-row">
                <span>启用配置</span>
                <strong class="success">{{ item.enabledCount }}</strong>
              </div>
              <div class="stat-row">
                <span>停用配置</span>
                <strong class="danger">{{ item.disabledCount }}</strong>
              </div>
              <div class="stat-row">
                <span>默认 Provider</span>
                <strong class="default-provider">{{ item.defaultProvider || '-' }}</strong>
              </div>
            </div>

            <el-progress
              :percentage="getPercentage(item.totalCount)"
              :color="getProgressColor(item.type)"
              :stroke-width="10"
            />

            <div class="type-actions">
              <el-button type="primary" link :disabled="!item.route" @click="goToConfig(item.type)">查看配置</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="16" class="bottom-row">
      <el-col :xs="24" :lg="15">
        <el-card shadow="hover">
          <template #header>
            <span>最近更新的配置</span>
          </template>
          <el-table v-loading="loading" :data="recentConfigs">
            <el-table-column prop="configType" label="类型" width="120">
              <template #default="scope">
                <el-tag>{{ getTypeLabel(scope.row.configType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="providerName" label="配置名称" min-width="200" :show-overflow-tooltip="true" />
            <el-table-column prop="providerCode" label="Provider" min-width="160" :show-overflow-tooltip="true" />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">
                  {{ scope.row.status === '0' ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="isDefault" label="默认" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.isDefault === 'Y' ? 'success' : 'info'">
                  {{ scope.row.isDefault === 'Y' ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="180" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="9">
        <el-card shadow="hover">
          <template #header>
            <span>快速入口</span>
          </template>
          <div class="quick-actions">
            <div class="quick-item">
              <div class="quick-title">能力中心</div>
              <div class="quick-desc">查看能力默认绑定、运行时快照和调用日志。</div>
              <el-button type="primary" link @click="goToCapabilityCenter">立即进入</el-button>
            </div>
            <div class="quick-item" v-for="item in quickRoutes" :key="item.type">
              <div class="quick-title">{{ item.title }}</div>
              <div class="quick-desc">{{ item.description }}</div>
              <el-button type="primary" link @click="goToConfig(item.type)">查看配置</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="ConfigDashboard">
import { getDashboardOverview } from '@/api/config/dashboard'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loading = ref(false)
const loadError = ref('')
const overviewStats = ref({
  totalCount: 0,
  enabledCount: 0,
  disabledCount: 0,
  typeCountMap: {}
})
const configTypeList = ref([])
const recentConfigs = ref([])

const typeLabels = {
  storage: '对象存储',
  llm: '大模型',
  tts: '语音合成',
  stt: '语音识别',
  image: '图像服务',
  image_gen: '图像生成',
  translation: '翻译服务',
  ocr: 'OCR 识别',
  face: '人脸识别',
  vision: '视觉理解',
  voiceprint: '声纹识别'
}

const typeRoutes = {
  storage: '/config/storage',
  llm: '/config/llm',
  tts: '/config/tts',
  stt: '/config/stt',
  image: '/config/image',
  image_gen: '/config/image',
  translation: '/config/translation',
  ocr: '/config/ocr',
  face: '/config/face',
  vision: '/config/vision',
  voiceprint: '/config/voiceprint'
}

const summaryCards = computed(() => [
  {
    key: 'total',
    title: '总配置数',
    value: overviewStats.value.totalCount || 0,
    desc: '当前已登记的全部服务配置'
  },
  {
    key: 'enabled',
    title: '启用配置',
    value: overviewStats.value.enabledCount || 0,
    desc: '运行时可参与路由的配置'
  },
  {
    key: 'disabled',
    title: '停用配置',
    value: overviewStats.value.disabledCount || 0,
    desc: '已保留但当前不参与路由'
  },
  {
    key: 'types',
    title: '服务类型数',
    value: configTypeList.value.length,
    desc: '已建模的配置类型'
  }
])

const configCards = computed(() => {
  return (configTypeList.value || []).map(item => ({
    type: item.configType,
    title: item.typeName || getTypeLabel(item.configType),
    description: item.typeDesc || `${getTypeLabel(item.configType)}配置`,
    totalCount: item.totalCount || 0,
    enabledCount: item.enabledCount || 0,
    disabledCount: item.disabledCount ?? Math.max((item.totalCount || 0) - (item.enabledCount || 0), 0),
    defaultProvider: item.defaultProvider || '-',
    route: typeRoutes[item.configType]
  }))
})

const quickRoutes = computed(() => {
  return configCards.value.filter(item => item.route).slice(0, 4)
})

function getTypeLabel(type) {
  return typeLabels[type] || type || '-'
}

function goToConfig(type) {
  const route = typeRoutes[type]
  if (route) {
    router.push(route)
  }
}

function goToCapabilityCenter() {
  router.push('/provider/capability')
}

function getPercentage(count) {
  const total = summaryCards.value[0].value || 0
  return total > 0 ? Math.round((count / total) * 100) : 0
}

function getProgressColor(type) {
  const colors = {
    storage: '#409EFF',
    llm: '#67C23A',
    tts: '#E6A23C',
    stt: '#F56C6C',
    image: '#909399',
    image_gen: '#909399',
    translation: '#00B2FF',
    ocr: '#9C27B0',
    face: '#8E44AD',
    vision: '#1ABC9C',
    voiceprint: '#34495E'
  }
  return colors[type] || '#409EFF'
}

function loadDashboard() {
  loading.value = true
  getDashboardOverview().then(response => {
    const data = response.data || {}
    overviewStats.value = data.overviewStats || {
      totalCount: 0,
      enabledCount: 0,
      disabledCount: 0,
      typeCountMap: {}
    }
    configTypeList.value = data.configTypeList || []
    recentConfigs.value = data.recentConfigs || []
    loadError.value = ''
  }).catch(() => {
    overviewStats.value = {
      totalCount: 0,
      enabledCount: 0,
      disabledCount: 0,
      typeCountMap: {}
    }
    configTypeList.value = []
    recentConfigs.value = []
    loadError.value = '服务配置总览加载失败，请检查后端接口或权限配置。'
  }).finally(() => {
    loading.value = false
  })
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.service-dashboard {
  min-height: calc(100vh - 84px);
}

.page-head,
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-head {
  margin-bottom: 16px;
}

.page-actions {
  display: flex;
  gap: 12px;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.page-desc {
  margin-top: 6px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.summary-row,
.bottom-row {
  margin-top: 16px;
}

.summary-card {
  min-height: 136px;
}

.summary-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}

.summary-value {
  margin-top: 14px;
  font-size: 32px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.summary-desc {
  margin-top: 10px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.type-card-wrapper {
  margin-top: 16px;
}

.type-col {
  margin-bottom: 16px;
}

.type-card {
  min-height: 250px;
  border-radius: 6px;
}

.type-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.type-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.type-desc {
  margin-top: 4px;
  font-size: 12px;
  line-height: 1.5;
  color: var(--el-text-color-secondary);
}

.type-stats {
  margin-bottom: 14px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  padding: 7px 0;
  font-size: 13px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.stat-row:last-child {
  border-bottom: none;
}

.success {
  color: #67c23a;
}

.danger {
  color: #f56c6c;
}

.default-provider {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.quick-item {
  padding-bottom: 14px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.quick-item:last-child {
  padding-bottom: 0;
  border-bottom: none;
}

.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.quick-desc {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.6;
  color: var(--el-text-color-secondary);
}
</style>
