<template>
  <div class="app-container provider-config-page">
    <div class="page-head">
      <div>
        <div class="page-title">{{ configName }}配置</div>
        <div class="page-desc">{{ pageDescription }}</div>
      </div>
      <el-tag type="info">{{ configType }}</el-tag>
    </div>

    <el-alert
      v-if="loadError"
      class="mb8"
      type="error"
      :closable="false"
      show-icon
      :title="loadError"
    />

    <section class="recommend-section">
      <div class="section-title">
        <div>
          <span>推荐接入</span>
          <span class="recommend-summary">{{ recommendSummary }}</span>
        </div>
        <div class="recommend-tools">
          <el-switch
            v-model="showUnconfiguredRecommendations"
            active-text="显示候选/未配置推荐"
            inactive-text="只看已配置推荐"
            @change="handleRecommendationVisibilityChange"
          />
          <el-button link type="primary" icon="Refresh" @click="handleRefreshRecommendations">刷新推荐</el-button>
          <el-button
            link
            type="primary"
            :icon="recommendExpanded ? 'ArrowUp' : 'ArrowDown'"
            :disabled="!hasRecommendationBody"
            @click="toggleRecommendations"
          >
            {{ recommendExpanded ? '收起' : '展开' }}
          </el-button>
        </div>
      </div>

      <el-alert
        v-if="recommendExpanded && recommendError"
        class="mb8"
        type="warning"
        :closable="false"
        show-icon
        :title="recommendError"
      />

      <el-row v-if="recommendExpanded && recommendations.length > 0" :gutter="12">
        <el-col
          v-for="item in recommendations"
          :key="item.providerCode"
          :xs="24"
          :sm="12"
          :lg="8"
          class="recommend-col"
        >
          <el-card shadow="never" class="recommend-card">
            <div class="recommend-head">
              <div>
                <div class="recommend-name">{{ item.providerName || item.providerCode }}</div>
                <div class="recommend-vendor">{{ joinText(item.vendorName, item.serviceName) }}</div>
              </div>
              <el-tag size="small" :type="statusTagType(item.integrationStatus)">
                {{ statusText(item.integrationStatus) }}
              </el-tag>
              <el-tag v-if="showUnconfiguredRecommendations && !isRecommendationConfigured(item)" size="small" type="warning" effect="plain">未配置</el-tag>
            </div>

            <div class="recommend-meta">
              <span v-if="item.authType">鉴权：{{ item.authType }}</span>
              <span v-if="item.endpointHint">端点：{{ templateUrlSummary(item.endpointHint) }}</span>
            </div>

            <div v-if="item.models && item.models.length" class="recommend-models">
              <el-tag
                v-for="model in item.models"
                :key="model.modelCode"
                size="small"
                :type="enabledValue(model.isEnabled) ? 'success' : 'info'"
                effect="plain"
              >
                {{ model.modelName || model.modelCode }}
              </el-tag>
            </div>

            <div v-if="item.adapterNote" class="recommend-note">{{ item.adapterNote }}</div>

            <div class="recommend-actions">
              <el-button type="primary" icon="Plus" @click="handleAdd(item.providerCode)">去配置</el-button>
              <el-link v-if="item.docsUrl" :href="item.docsUrl" target="_blank" type="primary">文档</el-link>
              <el-link v-if="item.consoleUrl" :href="item.consoleUrl" target="_blank" type="primary">控制台</el-link>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </section>

    <el-alert
      v-if="!loadingProviders && providerOptions.length === 0"
      class="mb8"
      type="warning"
      :closable="false"
      show-icon
      title="当前配置类型暂无可用 Provider，请检查后端枚举和种子数据。"
    />

    <el-form
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      v-show="showSearch"
      label-width="82px"
    >
      <el-form-item label="配置名称" prop="providerName">
        <el-input
          v-model="queryParams.providerName"
          placeholder="请输入配置名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="Provider" prop="providerCode">
        <el-select
          v-model="queryParams.providerCode"
          placeholder="请选择 Provider"
          clearable
          style="width: 220px"
        >
          <el-option v-for="item in providerOptions" :key="item.code" :label="item.name" :value="item.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择状态"
          clearable
          style="width: 160px"
        >
          <el-option label="启用" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="addPermi">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="editPermi">
          修改
        </el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="removePermi">
          删除
        </el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="configId" width="80" />
      <el-table-column label="Provider" align="center" prop="providerCode" min-width="150">
        <template #default="scope">
          <span>{{ providerLabel(scope.row.providerCode) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="配置名称" align="center" prop="providerName" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="访问端点" align="center" prop="endpoint" min-width="240" :show-overflow-tooltip="true" />
      <el-table-column label="默认" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isDefault === 'Y' ? 'success' : 'info'">
            {{ scope.row.isDefault === 'Y' ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="110">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="editPermi"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="CircleCheck"
            @click="handleSetDefault(scope.row)"
            v-hasPermi="editPermi"
          >
            设为默认
          </el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="editPermi"
          >
            修改
          </el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="removePermi"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty description="暂无配置，可手动新增，或从推荐接入快速创建 Provider 配置。" />
      </template>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="780px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="112px">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="Provider" prop="providerCode">
              <el-select
                v-model="form.providerCode"
                placeholder="请选择 Provider"
                filterable
                style="width: 100%"
                :disabled="form.configId !== undefined"
                @change="handleProviderChange"
              >
                <el-option v-for="item in providerOptions" :key="item.code" :label="item.name" :value="item.code" />
              </el-select>
            </el-form-item>
          </el-col>

          <el-col :span="24">
            <el-form-item label="配置名称" prop="providerName">
              <el-input v-model="form.providerName" placeholder="请输入配置名称" maxlength="100" />
            </el-form-item>
          </el-col>

          <el-col :span="24" v-if="form.providerCode && !loadingFields && visibleFields.length === 0">
            <el-alert
              class="dialog-alert"
              type="warning"
              :closable="false"
              show-icon
              title="当前 Provider 暂无字段配置，请检查 provider_field_config。"
            />
          </el-col>

          <el-col :span="24" v-if="selectedRecommendation && selectedRecommendation.integrationStatus !== 'active'">
            <el-alert class="dialog-alert" type="warning" :closable="false" show-icon :title="candidateTip" />
          </el-col>

          <el-col v-for="field in visibleFields" :key="field.key" :span="24">
            <el-form-item :label="field.label" :prop="field.key">
              <el-select
                v-if="isSelectField(field)"
                v-model="form[field.key]"
                filterable
                allow-create
                default-first-option
                clearable
                style="width: 100%"
                :placeholder="field.placeholder || '请选择或手动输入'"
              >
                <el-option v-for="option in field.options" :key="option.value" :label="option.label" :value="option.value">
                  <span>{{ option.label }}</span>
                  <span v-if="option.helpText" class="option-tip">{{ option.helpText }}</span>
                </el-option>
              </el-select>
              <el-input
                v-else-if="field.type === 'password'"
                v-model="form[field.key]"
                :placeholder="secretPlaceholder(field)"
                show-password
              />
              <el-input-number
                v-else-if="field.type === 'number'"
                v-model="form[field.key]"
                :min="0"
                :step="numberStep(field)"
                style="width: 100%"
              />
              <el-input
                v-else-if="field.type === 'textarea'"
                v-model="form[field.key]"
                type="textarea"
                :rows="3"
                :placeholder="field.placeholder || '请输入内容'"
              />
              <el-input
                v-else
                v-model="form[field.key]"
                :placeholder="field.placeholder || '请输入内容'"
              />
              <div v-if="field.helpText" class="form-tip">{{ field.helpText }}</div>
            </el-form-item>
          </el-col>

          <el-col :span="12">
            <el-form-item label="默认配置" prop="isDefault">
              <el-radio-group v-model="form.isDefault">
                <el-radio label="Y">是</el-radio>
                <el-radio label="N">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProviderConfigPage">
import {
  addConfig,
  changeConfigStatus,
  delConfig,
  getConfig,
  listConfig,
  listConfigProviders,
  listConfigRecommendations,
  listProviderFields,
  setDefaultConfig,
  updateConfig
} from '@/api/config/config'
import {
  buildConfiguredProviderSet,
  CONFIGURED_PROVIDER_FETCH_SIZE,
  hasConfiguredProvider,
  templateUrlSummary
} from '@/utils/providerConfiguredFilter'
import { computed, getCurrentInstance, onMounted, reactive, ref } from 'vue'

const props = defineProps({
  configType: {
    type: String,
    required: true
  },
  configName: {
    type: String,
    required: true
  }
})

const { proxy } = getCurrentInstance()

const TOP_LEVEL_FIELDS = ['endpoint', 'region', 'bucketName', 'accessKey', 'secretKey', 'domain', 'basePath', 'modelType']
const BASIC_FORM_KEYS = ['configId', 'configType', 'providerCode', 'providerName', 'endpoint', 'region', 'bucketName', 'accessKey', 'secretKey', 'domain', 'basePath', 'modelType', 'isDefault', 'status', 'remark', 'extParamsJson']

const TYPE_DESCRIPTIONS = {
  llm: '用于管理导诊、问诊、分析等大模型接入配置，支持能力中心按业务切换默认 Provider。',
  ocr: '用于管理证件、票据、报告等 OCR 能力接入配置，支持从推荐清单快速创建。',
  face: '用于管理人脸检测、比对、活体和 FaceID H5 等能力配置。',
  vision: '用于管理图片识别、报告分析等视觉理解能力配置。',
  stt: '用于管理语音识别服务配置，适配文件转写和实时语音场景。',
  tts: '用于管理语音合成服务配置，适配播报、对话回声等场景。',
  voiceprint: '用于管理声纹注册、校验和登录识别等服务配置。',
  storage: '用于管理对象存储或文件服务配置，支持运行时切换默认存储。',
  translation: '用于管理机器翻译服务配置。',
  image: '用于管理图像生成或图像相关服务配置。'
}

const configList = ref([])
const providerOptions = ref([])
const rawRecommendations = ref([])
const configuredProviderSet = ref(new Set())
const showUnconfiguredRecommendations = ref(false)
const recommendExpanded = ref(false)
const recommendManuallyToggled = ref(false)
const fieldDefs = ref([])
const preservedExtParams = ref({})
const open = ref(false)
const loading = ref(true)
const loadingProviders = ref(false)
const loadingRecommendations = ref(false)
const loadingFields = ref(false)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref('')
const loadError = ref('')
const configuredProviderError = ref('')
const recommendError = ref('')
const rules = ref(baseRules())
const form = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  providerName: undefined,
  providerCode: undefined,
  configType: props.configType,
  status: undefined
})

const addPermi = computed(() => [`config:${props.configType}:add`, 'provider:config:add'])
const editPermi = computed(() => [`config:${props.configType}:edit`, 'provider:config:edit'])
const removePermi = computed(() => [`config:${props.configType}:remove`, 'provider:config:remove'])

const pageDescription = computed(() => TYPE_DESCRIPTIONS[props.configType] || '用于维护第三方 Provider 配置和运行时连接信息。')
const visibleFields = computed(() => fieldDefs.value.filter(field => field.key !== 'apiKey'))
const hasConfigs = computed(() => total.value > 0)
const recommendations = computed(() => {
  const list = rawRecommendations.value || []
  if (showUnconfiguredRecommendations.value) {
    return list
  }
  return list.filter(item => isRecommendationConfigured(item))
})
const hiddenRecommendationCount = computed(() => rawRecommendations.value.length - recommendations.value.length)
const hasRecommendationBody = computed(() => recommendations.value.length > 0 || !!recommendError.value)
const recommendSummary = computed(() => {
  if (loadingRecommendations.value) {
    return '加载中'
  }
  if (recommendError.value) {
    return '推荐加载失败，可刷新重试'
  }
  if (configuredProviderError.value) {
    return configuredProviderError.value
  }
  if (!showUnconfiguredRecommendations.value && hiddenRecommendationCount.value > 0 && recommendations.value.length === 0) {
    return `暂无已配置推荐，已隐藏 ${hiddenRecommendationCount.value} 个候选推荐`
  }
  if (recommendations.value.length === 0) {
    return '暂无推荐，可手动新增配置'
  }
  if (hasConfigs.value && !recommendExpanded.value) {
    const hiddenText = hiddenRecommendationCount.value > 0 ? `，已隐藏 ${hiddenRecommendationCount.value} 个候选` : ''
    return `已有配置，${recommendations.value.length} 个推荐已收起${hiddenText}`
  }
  const hiddenText = hiddenRecommendationCount.value > 0 ? `，另隐藏 ${hiddenRecommendationCount.value} 个候选` : ''
  return `${recommendations.value.length} 个推荐接入${hiddenText}`
})
const selectedRecommendation = computed(() => rawRecommendations.value.find(item => item.providerCode === form.value.providerCode))
const candidateTip = computed(() => {
  const item = selectedRecommendation.value
  if (!item) {
    return ''
  }
  return `${item.providerName || item.providerCode} 当前是“${statusText(item.integrationStatus)}”状态，可以先保存配置；真实业务调用仍需后端适配或连通性验证。`
})

function baseRules() {
  return {
    providerCode: [{ required: true, message: '请选择 Provider', trigger: 'change' }],
    providerName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }]
  }
}

function statusText(status) {
  const map = {
    active: '可用',
    candidate: '候选',
    needs_adapter: '待适配'
  }
  return map[status] || '候选'
}

function statusTagType(status) {
  const map = {
    active: 'success',
    candidate: 'info',
    needs_adapter: 'warning'
  }
  return map[status] || 'info'
}

function joinText(...values) {
  return values.filter(Boolean).join(' / ')
}

function enabledValue(value) {
  return value === true || value === 1 || value === '1'
}

function isRecommendationConfigured(item) {
  return hasConfiguredProvider({
    configType: props.configType,
    providerCode: item?.providerCode
  }, configuredProviderSet.value)
}

function providerLabel(value) {
  const matched = providerOptions.value.find(item => item.code === value)
  return matched ? matched.name : value
}

function isSelectField(field) {
  return field.type === 'select' || (field.options && field.options.length > 0)
}

function numberStep(field) {
  return field.key === 'temperature' ? 0.1 : 1
}

function secretPlaceholder(field) {
  const masked = form.value[`${field.key}Masked`]
  return masked ? `留空则保留当前值（${masked}）` : (field.placeholder || '请输入密钥或令牌')
}

function applyRecommendationVisibility(options = {}) {
  if (!hasRecommendationBody.value) {
    recommendExpanded.value = false
    recommendManuallyToggled.value = false
    return
  }
  if (options.openWhenNoConfig && !hasConfigs.value && recommendations.value.length > 0) {
    recommendExpanded.value = true
    recommendManuallyToggled.value = false
    return
  }
  if (!recommendManuallyToggled.value) {
    recommendExpanded.value = recommendations.value.length > 0 && !hasConfigs.value
  }
}

function handleRecommendationVisibilityChange() {
  recommendManuallyToggled.value = false
  applyRecommendationVisibility({ openWhenNoConfig: true })
  if (showUnconfiguredRecommendations.value && recommendations.value.length > 0) {
    recommendExpanded.value = true
    recommendManuallyToggled.value = true
  }
}

function toggleRecommendations() {
  if (!hasRecommendationBody.value) {
    return
  }
  recommendExpanded.value = !recommendExpanded.value
  recommendManuallyToggled.value = true
}

function getList() {
  loading.value = true
  return listConfig(queryParams).then(response => {
    configList.value = response.rows || []
    total.value = response.total || 0
    loadError.value = ''
  }).catch(() => {
    configList.value = []
    total.value = 0
    loadError.value = `${props.configName}配置列表加载失败，请检查后端服务或菜单权限。`
  }).finally(() => {
    loading.value = false
    loadConfiguredProviders().finally(() => applyRecommendationVisibility())
  })
}

function loadConfiguredProviders() {
  configuredProviderError.value = ''
  return listConfig({
    pageNum: 1,
    pageSize: CONFIGURED_PROVIDER_FETCH_SIZE,
    configType: props.configType,
    status: '0'
  }).then(response => {
    configuredProviderSet.value = buildConfiguredProviderSet(response.rows || [])
  }).catch(() => {
    configuredProviderSet.value = new Set()
    configuredProviderError.value = '启用配置加载失败，候选推荐已默认隐藏'
  })
}

function loadProviders() {
  loadingProviders.value = true
  return listConfigProviders(props.configType).then(response => {
    const items = response.data || []
    providerOptions.value = items
      .map(item => ({
        code: item.code || item.providerCode,
        name: item.name || item.providerName || item.providerCode || item.code
      }))
      .filter(item => item.code)
  }).catch(() => {
    providerOptions.value = []
    loadError.value = `${props.configName} Provider 元数据加载失败，请检查 providers 接口。`
  }).finally(() => {
    loadingProviders.value = false
  })
}

function loadRecommendations(options = {}) {
  loadingRecommendations.value = true
  recommendError.value = ''
  return listConfigRecommendations(props.configType).then(response => {
    rawRecommendations.value = (response.data || []).map(item => ({
      ...item,
      models: Array.isArray(item.models) ? item.models : []
    }))
  }).catch(() => {
    rawRecommendations.value = []
    recommendError.value = `${props.configName}推荐接入数据加载失败，请检查 recommendations 接口。`
  }).finally(() => {
    loadingRecommendations.value = false
    applyRecommendationVisibility(options)
  })
}

function handleRefreshRecommendations() {
  return loadConfiguredProviders().finally(() => loadRecommendations({ openWhenNoConfig: true }))
}

function loadProviderFields(providerCode) {
  if (!providerCode) {
    fieldDefs.value = []
    syncDynamicRules()
    return Promise.resolve([])
  }
  loadingFields.value = true
  return listProviderFields(providerCode).then(response => {
    fieldDefs.value = (response.data || []).map(field => ({
      ...field,
      options: Array.isArray(field.options) ? field.options : []
    }))
    syncDynamicRules()
    return fieldDefs.value
  }).catch(() => {
    fieldDefs.value = []
    syncDynamicRules()
    loadError.value = `${providerCode} 字段配置加载失败，请检查 provider_field_config。`
    return []
  }).finally(() => {
    loadingFields.value = false
  })
}

function syncDynamicRules() {
  const nextRules = baseRules()
  fieldDefs.value.forEach(field => {
    if (field.required && field.key !== 'apiKey') {
      nextRules[field.key] = [{
        required: true,
        message: `${field.label || field.key}不能为空`,
        trigger: isSelectField(field) ? 'change' : 'blur'
      }]
    }
  })
  rules.value = nextRules
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    configId: undefined,
    configType: props.configType,
    providerCode: undefined,
    providerName: undefined,
    endpoint: undefined,
    accessKey: undefined,
    secretKey: undefined,
    isDefault: 'N',
    status: '0',
    remark: undefined,
    extParamsJson: undefined
  }
  fieldDefs.value = []
  preservedExtParams.value = {}
  rules.value = baseRules()
  proxy?.resetForm?.('configRef')
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  proxy?.resetForm?.('queryRef')
  queryParams.pageNum = 1
  queryParams.configType = props.configType
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd(providerCode) {
  reset()
  open.value = true
  title.value = `新增${props.configName}配置`
  if (!providerCode) {
    return
  }
  form.value.providerCode = providerCode
  const matched = providerOptions.value.find(item => item.code === providerCode)
  const recommended = rawRecommendations.value.find(item => item.providerCode === providerCode)
  form.value.providerName = (matched && matched.name) || (recommended && recommended.providerName) || providerCode
  loadProviderFields(providerCode).then(applyDefaultFieldValues)
}

function handleUpdate(row = {}) {
  reset()
  const configId = row.configId || ids.value[0]
  getConfig(configId).then(response => {
    hydrateConfig(response.data || {})
    open.value = true
    title.value = `修改${props.configName}配置`
  }).catch(() => {
    loadError.value = '配置详情加载失败，请稍后重试。'
  })
}

function handleProviderChange(providerCode) {
  const matched = providerOptions.value.find(item => item.code === providerCode)
  form.value.providerCode = providerCode
  if (!form.value.providerName && matched) {
    form.value.providerName = matched.name
  }
  preservedExtParams.value = {}
  loadProviderFields(providerCode).then(applyDefaultFieldValues)
}

function applyDefaultFieldValues(fields = []) {
  fields.forEach(field => {
    if ((form.value[field.key] === undefined || form.value[field.key] === '') && field.defaultValue !== undefined && field.defaultValue !== null) {
      form.value[field.key] = castFieldValue(field, field.defaultValue)
    }
  })
}

function hydrateConfig(config) {
  form.value = {
    ...form.value,
    ...config,
    accessKey: undefined,
    secretKey: undefined
  }
  loadProviderFields(config.providerCode).then(fields => {
    const extParams = parseJsonObject(config.extParamsJson)
    const remaining = { ...extParams }
    fields.forEach(field => {
      if (TOP_LEVEL_FIELDS.includes(field.key)) {
        form.value[field.key] = field.type === 'password' ? undefined : config[field.key]
        return
      }
      if (extParams[field.key] !== undefined) {
        form.value[field.key] = castFieldValue(field, extParams[field.key])
        delete remaining[field.key]
      } else if (field.defaultValue !== undefined && field.defaultValue !== null) {
        form.value[field.key] = castFieldValue(field, field.defaultValue)
      }
    })
    preservedExtParams.value = remaining
  })
}

function castFieldValue(field, value) {
  if (field.type === 'number' && value !== undefined && value !== null && value !== '') {
    const parsed = Number(value)
    return Number.isNaN(parsed) ? value : parsed
  }
  return value
}

function parseJsonObject(jsonText) {
  if (!jsonText) {
    return {}
  }
  try {
    const parsed = JSON.parse(jsonText)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch (error) {
    return {}
  }
}

function buildSubmitPayload() {
  const extParams = { ...preservedExtParams.value }
  fieldDefs.value.forEach(field => {
    if (TOP_LEVEL_FIELDS.includes(field.key) || field.key === 'apiKey') {
      return
    }
    const value = form.value[field.key]
    if (value !== undefined && value !== null && value !== '') {
      extParams[field.key] = value
    }
  })

  const payload = {}
  BASIC_FORM_KEYS.forEach(key => {
    if (form.value[key] !== undefined) {
      payload[key] = form.value[key]
    }
  })
  payload.configType = props.configType
  payload.extParamsJson = Object.keys(extParams).length ? JSON.stringify(extParams) : null
  return payload
}

function submitForm() {
  proxy.$refs.configRef.validate(valid => {
    if (!valid) {
      return
    }
    const submitData = buildSubmitPayload()
    const request = submitData.configId !== undefined ? updateConfig : addConfig
    request(submitData).then(() => {
      proxy.$modal.msgSuccess(submitData.configId !== undefined ? '修改成功' : '新增成功')
      open.value = false
      getList()
    }).catch(() => {
      loadError.value = '保存失败，请检查必填字段和后端服务。'
    })
  })
}

function handleDelete(row = {}) {
  const configIds = row.configId || ids.value
  const displayIds = Array.isArray(configIds) ? configIds.join(',') : configIds
  if (!displayIds) {
    proxy.$modal.msgWarning('请先选择要删除的配置。')
    return
  }
  proxy.$modal.confirm(`是否确认删除配置编号为“${displayIds}”的数据项？`).then(() => {
    return delConfig(configIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  changeConfigStatus(row.configId, row.status).then(() => {
    proxy.$modal.msgSuccess(`${text}成功`)
    getList()
  }).catch(() => {
    row.status = row.status === '0' ? '1' : '0'
  })
}

function handleSetDefault(row) {
  if (row.isDefault === 'Y') {
    proxy.$modal.msgInfo('当前已经是默认配置')
    return
  }
  proxy.$modal.confirm(`确认将“${row.providerName}”设为默认配置吗？`).then(() => {
    return setDefaultConfig(row.configId)
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || '默认配置已切换')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  loadProviders().finally(() => {
    getList().finally(() => loadRecommendations({ openWhenNoConfig: true }))
  })
})
</script>

<style scoped>
.page-head {
  align-items: center;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  padding-bottom: 12px;
}

.page-title {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
  line-height: 28px;
}

.page-desc {
  color: #606266;
  font-size: 13px;
  line-height: 22px;
}

.recommend-section {
  margin-bottom: 16px;
}

.section-title {
  align-items: center;
  color: #303133;
  display: flex;
  font-size: 15px;
  font-weight: 600;
  justify-content: space-between;
  margin-bottom: 8px;
}

.recommend-summary {
  color: #909399;
  font-size: 12px;
  font-weight: 400;
  margin-left: 10px;
}

.recommend-tools {
  align-items: center;
  display: flex;
  gap: 8px;
}

.recommend-col {
  margin-bottom: 12px;
}

.recommend-card {
  border-radius: 6px;
  min-height: 190px;
}

.recommend-head {
  align-items: flex-start;
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.recommend-name {
  color: #303133;
  font-size: 15px;
  font-weight: 600;
  line-height: 22px;
}

.recommend-vendor,
.recommend-meta,
.recommend-note,
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 20px;
}

.recommend-meta {
  display: flex;
  flex-direction: column;
  min-height: 42px;
}

.recommend-models {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 8px 0;
}

.recommend-actions {
  align-items: center;
  display: flex;
  gap: 12px;
  margin-top: 10px;
}

.dialog-alert {
  margin-bottom: 16px;
}

.option-tip {
  color: #909399;
  float: right;
  font-size: 12px;
  max-width: 360px;
  overflow: hidden;
  padding-left: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
