<template>
  <div class="app-container provider-template-page">
    <div class="page-head">
      <div>
        <div class="page-title">API 模板</div>
        <div class="page-desc">管理通用模板驱动的 Provider 调用配置，模板测试不会展示密钥内容。</div>
      </div>
      <div class="page-actions">
        <el-switch
          v-model="showUnconfigured"
          active-text="显示候选/未配置数据"
          inactive-text="只看已配置"
          @change="handleVisibilityChange"
        />
        <el-button icon="Refresh" @click="getList">刷新</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['config:template:add']">新增模板</el-button>
      </div>
    </div>

    <el-alert
      v-if="configuredProviderError"
      class="mb8"
      type="warning"
      :closable="false"
      show-icon
      :title="configuredProviderError"
    />
    <el-alert
      v-if="!showUnconfigured && hiddenCandidateCount > 0"
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      :title="`已隐藏 ${hiddenCandidateCount} 条未配置 Provider 支撑的候选模板，打开“显示候选/未配置数据”可查看。`"
    />

    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="配置类型" prop="configType">
        <el-select v-model="queryParams.configType" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Provider" prop="providerCode">
        <el-input v-model="queryParams.providerCode" clearable placeholder="providerCode" style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="Operation" prop="operation">
        <el-input v-model="queryParams.operation" clearable placeholder="operation" style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="isEnabled">
        <el-select v-model="queryParams.isEnabled" clearable placeholder="全部" style="width: 120px">
          <el-option label="启用" value="1" />
          <el-option label="停用" value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['config:template:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['config:template:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="templateList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="templateId" width="80" align="center" />
      <el-table-column label="类型" prop="configType" width="100" align="center">
        <template #default="scope">
          <el-tag>{{ configTypeLabel(scope.row.configType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Provider" prop="providerCode" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column v-if="showUnconfigured" label="配置支撑" width="110" align="center">
        <template #default="scope">
          <el-tag :type="isRowConfigured(scope.row) ? 'success' : 'warning'">
            {{ isRowConfigured(scope.row) ? '已配置' : '未配置' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Operation" prop="operation" min-width="150" :show-overflow-tooltip="true" />
      <el-table-column label="Method" prop="httpMethod" width="100" align="center" />
      <el-table-column label="URL 模板" min-width="260" :show-overflow-tooltip="true">
        <template #default="scope">{{ templateUrlSummary(scope.row.urlTemplate) }}</template>
      </el-table-column>
      <el-table-column label="认证" prop="authType" width="100" align="center">
        <template #default="scope">
          <el-tag type="info">{{ scope.row.authType || 'none' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="超时" prop="timeout" width="100" align="center" />
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.isEnabled"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['config:template:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="更新时间" prop="updateTime" width="180" align="center">
        <template #default="scope">{{ parseTime(scope.row.updateTime || scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handlePreview(scope.row)" v-hasPermi="['config:template:query']">查看</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['config:template:edit']">修改</el-button>
          <el-button link type="warning" icon="Operation" @click="handleTest(scope.row)" v-hasPermi="['config:template:test']">测试</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['config:template:remove']">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty :description="showUnconfigured ? '暂无 API 模板数据' : '暂无已配置 Provider 支撑的 API 模板，打开“显示候选/未配置数据”可查看候选记录。'" />
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="980px" append-to-body>
      <el-form ref="templateRef" :model="form" :rules="rules" label-width="110px" :disabled="readonly">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="form.configType" style="width: 100%">
                <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Provider" prop="providerCode">
              <el-input v-model="form.providerCode" placeholder="providerCode" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Operation" prop="operation">
              <el-input v-model="form.operation" placeholder="operation" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="HTTP 方法" prop="httpMethod">
              <el-select v-model="form.httpMethod" style="width: 100%">
                <el-option v-for="item in methodOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Body 类型" prop="bodyType">
              <el-select v-model="form.bodyType" clearable style="width: 100%">
                <el-option v-for="item in bodyTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="响应类型" prop="responseType">
              <el-select v-model="form.responseType" clearable style="width: 100%">
                <el-option label="json" value="json" />
                <el-option label="text" value="text" />
                <el-option label="xml" value="xml" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="认证类型" prop="authType">
              <el-select v-model="form.authType" clearable style="width: 100%">
                <el-option v-for="item in authTypeOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="超时毫秒" prop="timeout">
              <el-input-number v-model="form.timeout" :min="1000" :step="1000" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重试次数" prop="retryTimes">
              <el-input-number v-model="form.retryTimes" :min="0" :max="5" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="URL 模板" prop="urlTemplate">
              <el-input v-model="form.urlTemplate" placeholder="${endpoint}/chat/completions" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Headers JSON" prop="headersJson">
              <el-input v-model="form.headersJson" type="textarea" :rows="5" placeholder='{"Content-Type":"application/json"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证 JSON" prop="authConfigJson">
              <el-input v-model="form.authConfigJson" type="textarea" :rows="5" placeholder='{"header":"X-API-Key","value":"${accessKey}"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Body 模板" prop="bodyTemplate">
              <el-input v-model="form.bodyTemplate" type="textarea" :rows="8" placeholder='{"text":"${text}"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="响应映射" prop="responseMapping">
              <el-input v-model="form.responseMapping" type="textarea" :rows="8" placeholder='{"content":"$.choices[0].message.content"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="isEnabled">
              <el-radio-group v-model="form.isEnabled">
                <el-radio label="1">启用</el-radio>
                <el-radio label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="!readonly" type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">{{ readonly ? '关闭' : '取消' }}</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="模板测试" v-model="testOpen" width="820px" append-to-body>
      <el-alert class="mb8" type="info" :closable="false" show-icon title="测试上下文用于渲染模板，页面不会要求或展示真实密钥。" />
      <el-input v-model="testContextText" type="textarea" :rows="10" placeholder='{"text":"你好","audioUrl":"https://example.com/a.wav"}' />
      <el-divider />
      <el-input v-model="testResult" type="textarea" :rows="6" readonly placeholder="测试结果会显示在这里" />
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitTest">执行测试</el-button>
          <el-button @click="testOpen = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProviderApiTemplate">
import {
  addProviderApiTemplate,
  changeProviderApiTemplateStatus,
  delProviderApiTemplate,
  getProviderApiTemplate,
  listProviderApiTemplate,
  testProviderApiTemplate,
  updateProviderApiTemplate
} from '@/api/provider/template'
import { listProviderConfig } from '@/api/provider/config'
import {
  buildConfiguredProviderSet,
  CONFIGURED_PROVIDER_FETCH_SIZE,
  countUnconfiguredProviderRows,
  filterRowsByConfiguredProvider,
  hasConfiguredProvider,
  localPageRows,
  templateUrlSummary
} from '@/utils/providerConfiguredFilter'
import { getCurrentInstance, reactive, ref, toRefs } from 'vue'

const { proxy } = getCurrentInstance()

const configTypeOptions = [
  { label: 'LLM', value: 'llm' },
  { label: 'TTS', value: 'tts' },
  { label: 'STT', value: 'stt' },
  { label: 'OCR', value: 'ocr' },
  { label: '人脸', value: 'face' },
  { label: '视觉', value: 'vision' },
  { label: '存储', value: 'storage' }
]
const methodOptions = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH']
const bodyTypeOptions = ['json', 'form', 'multipart', 'binary', 'text']
const authTypeOptions = ['none', 'basic', 'bearer', 'apikey', 'custom']

const loading = ref(false)
const showSearch = ref(true)
const templateList = ref([])
const allTemplateList = ref([])
const configuredProviderSet = ref(new Set())
const configuredProviderError = ref('')
const showUnconfigured = ref(false)
const hiddenCandidateCount = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const readonly = ref(false)
const title = ref('')
const testOpen = ref(false)
const testTemplate = ref(null)
const testContextText = ref('{\n  "text": "你好"\n}')
const testResult = ref('')

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configType: undefined,
    providerCode: undefined,
    operation: undefined,
    isEnabled: undefined
  },
  form: {},
  rules: {
    configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
    providerCode: [{ required: true, message: 'Provider 不能为空', trigger: 'blur' }],
    operation: [{ required: true, message: 'Operation 不能为空', trigger: 'blur' }],
    httpMethod: [{ required: true, message: '请选择 HTTP 方法', trigger: 'change' }],
    urlTemplate: [{ required: true, message: 'URL 模板不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function configTypeLabel(value) {
  return configTypeOptions.find(item => item.value === value)?.label || value || '-'
}

function isRowConfigured(row) {
  return hasConfiguredProvider(row, configuredProviderSet.value)
}

function loadConfiguredProviders() {
  configuredProviderError.value = ''
  return listProviderConfig({
    pageNum: 1,
    pageSize: CONFIGURED_PROVIDER_FETCH_SIZE,
    status: '0'
  }).then(response => {
    configuredProviderSet.value = buildConfiguredProviderSet(response.rows || [])
  }).catch(() => {
    configuredProviderSet.value = new Set()
    configuredProviderError.value = '启用 Provider 配置加载失败，候选模板已默认隐藏；请检查 Provider 配置列表接口。'
  })
}

function applyTemplateVisibility() {
  const visibleRows = filterRowsByConfiguredProvider(allTemplateList.value, configuredProviderSet.value, showUnconfigured.value)
  hiddenCandidateCount.value = countUnconfiguredProviderRows(allTemplateList.value, configuredProviderSet.value)
  total.value = visibleRows.length
  templateList.value = localPageRows(visibleRows, queryParams.value.pageNum, queryParams.value.pageSize)
}

function reset() {
  form.value = {
    templateId: undefined,
    configType: 'llm',
    providerCode: undefined,
    operation: 'chat',
    httpMethod: 'POST',
    urlTemplate: undefined,
    headersJson: '{}',
    bodyType: 'json',
    bodyTemplate: '{}',
    responseType: 'json',
    responseMapping: '{}',
    authType: 'apikey',
    authConfigJson: '{}',
    timeout: 30000,
    retryTimes: 0,
    isEnabled: '1',
    remark: undefined
  }
  proxy?.resetForm?.('templateRef')
}

function getList() {
  loading.value = true
  const requestParams = {
    ...queryParams.value,
    pageNum: 1,
    pageSize: CONFIGURED_PROVIDER_FETCH_SIZE
  }
  Promise.all([
    loadConfiguredProviders(),
    listProviderApiTemplate(requestParams)
  ]).then(([, response]) => {
    allTemplateList.value = response.rows || []
    applyTemplateVisibility()
  }).finally(() => {
    loading.value = false
  })
}

function handleVisibilityChange() {
  queryParams.value.pageNum = 1
  applyTemplateVisibility()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy?.resetForm?.('queryRef')
  queryParams.value.pageNum = 1
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.templateId)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}

function handleAdd() {
  reset()
  readonly.value = false
  title.value = '新增 API 模板'
  open.value = true
}

function openDetail(templateId, readOnly) {
  reset()
  getProviderApiTemplate(templateId).then(response => {
    form.value = { ...form.value, ...(response.data || {}) }
    readonly.value = readOnly
    title.value = readOnly ? '查看 API 模板' : '修改 API 模板'
    open.value = true
  })
}

function handlePreview(row) {
  openDetail(row.templateId, true)
}

function handleUpdate(row) {
  openDetail(row?.templateId || ids.value[0], false)
}

function submitForm() {
  proxy.$refs.templateRef.validate(valid => {
    if (!valid) {
      return
    }
    const request = form.value.templateId ? updateProviderApiTemplate : addProviderApiTemplate
    request(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.templateId ? '修改成功' : '新增成功')
      open.value = false
      getList()
    })
  })
}

function cancel() {
  open.value = false
  reset()
}

function handleStatusChange(row) {
  changeProviderApiTemplateStatus(row.templateId, row.isEnabled).then(() => {
    proxy.$modal.msgSuccess(row.isEnabled === '1' ? '已启用' : '已停用')
  }).catch(() => {
    row.isEnabled = row.isEnabled === '1' ? '0' : '1'
  })
}

function handleDelete(row) {
  const templateIds = row?.templateId || ids.value
  proxy.$modal.confirm(`确认删除模板编号为 ${templateIds} 的数据吗？`).then(() => {
    return delProviderApiTemplate(templateIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

function handleTest(row) {
  testTemplate.value = { ...row }
  testResult.value = ''
  testOpen.value = true
}

function submitTest() {
  let testContext = {}
  try {
    testContext = testContextText.value ? JSON.parse(testContextText.value) : {}
  } catch (e) {
    proxy.$modal.msgError('测试上下文不是合法 JSON')
    return
  }
  testProviderApiTemplate({
    template: testTemplate.value,
    testContext
  }).then(response => {
    testResult.value = typeof response.data === 'string' ? response.data : JSON.stringify(response.data, null, 2)
  })
}

getList()
</script>

<style scoped>
.provider-template-page {
  min-height: calc(100vh - 84px);
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.page-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
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
}
</style>
