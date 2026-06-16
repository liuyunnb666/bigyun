<template>
  <div class="app-container model-catalog-page">
    <div class="page-head">
      <div>
        <div class="page-title">模型目录</div>
        <div class="page-desc">管理 Provider 下可被业务能力绑定的模型，不保存密钥和运行时请求内容。</div>
      </div>
      <div class="page-actions">
        <el-switch
          v-model="showUnconfigured"
          active-text="显示候选/未配置数据"
          inactive-text="只看已配置"
          @change="handleVisibilityChange"
        />
        <el-button icon="Refresh" @click="getList">刷新</el-button>
        <el-button type="primary" icon="Plus" @click="handleAdd" v-hasPermi="['provider:model:add']">新增模型</el-button>
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
      :title="`已隐藏 ${hiddenCandidateCount} 条未配置 Provider 支撑的候选模型，打开“显示候选/未配置数据”可查看。`"
    />

    <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="88px">
      <el-form-item label="模型编码" prop="modelCode">
        <el-input v-model="queryParams.modelCode" clearable placeholder="modelCode" style="width: 190px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="模型名称" prop="modelName">
        <el-input v-model="queryParams.modelName" clearable placeholder="modelName" style="width: 190px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="配置类型" prop="configType">
        <el-select v-model="queryParams.configType" clearable placeholder="全部" style="width: 140px">
          <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Provider" prop="providerCode">
        <el-input v-model="queryParams.providerCode" clearable placeholder="providerCode" style="width: 180px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="部署模式" prop="deploymentMode">
        <el-select v-model="queryParams.deploymentMode" clearable placeholder="全部" style="width: 130px">
          <el-option v-for="item in deploymentModeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
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
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="['provider:model:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="['provider:model:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="modelList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" prop="modelId" width="80" align="center" />
      <el-table-column label="模型编码" prop="modelCode" min-width="170" :show-overflow-tooltip="true" />
      <el-table-column label="模型名称" prop="modelName" min-width="180" :show-overflow-tooltip="true" />
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
      <el-table-column label="部署" prop="deploymentMode" width="100" align="center">
        <template #default="scope">
          <el-tag type="info">{{ deploymentModeLabel(scope.row.deploymentMode) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="能力标签" prop="capabilityTags" min-width="180" :show-overflow-tooltip="true" />
      <el-table-column label="上下文" prop="contextWindow" width="100" align="center">
        <template #default="scope">{{ scope.row.contextWindow || '-' }}</template>
      </el-table-column>
      <el-table-column label="文档" prop="docsUrl" width="90" align="center">
        <template #default="scope">
          <el-link v-if="scope.row.docsUrl" :href="scope.row.docsUrl" target="_blank" type="primary">打开</el-link>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.isEnabled"
            active-value="1"
            inactive-value="0"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['provider:model:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="排序" prop="sortOrder" width="90" align="center" />
      <el-table-column label="更新时间" prop="updateTime" width="180" align="center">
        <template #default="scope">{{ parseTime(scope.row.updateTime || scope.row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" fixed="right" align="center">
        <template #default="scope">
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['provider:model:edit']">修改</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['provider:model:remove']">删除</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <el-empty :description="showUnconfigured ? '暂无模型目录数据' : '暂无已配置 Provider 支撑的模型，打开“显示候选/未配置数据”可查看候选记录。'" />
      </template>
    </el-table>

    <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="title" v-model="open" width="860px" append-to-body>
      <el-form ref="modelRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="模型编码" prop="modelCode">
              <el-input v-model="form.modelCode" placeholder="如 qwen-plus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型名称" prop="modelName">
              <el-input v-model="form.modelName" placeholder="如 Qwen Plus" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="form.configType" style="width: 100%">
                <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Provider" prop="providerCode">
              <el-input v-model="form.providerCode" placeholder="providerCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部署模式" prop="deploymentMode">
              <el-select v-model="form.deploymentMode" clearable style="width: 100%">
                <el-option v-for="item in deploymentModeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="上下文窗口" prop="contextWindow">
              <el-input-number v-model="form.contextWindow" :min="0" :step="1024" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sortOrder">
              <el-input-number v-model="form.sortOrder" :min="0" :step="1" style="width: 100%" />
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
            <el-form-item label="能力标签" prop="capabilityTags">
              <el-input v-model="form.capabilityTags" placeholder="如 speech,tts，用英文逗号分隔" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="文档地址" prop="docsUrl">
              <el-input v-model="form.docsUrl" placeholder="https://..." />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入参说明" prop="inputSchemaJson">
              <el-input v-model="form.inputSchemaJson" type="textarea" :rows="5" placeholder='{"text":"string"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出参说明" prop="outputSchemaJson">
              <el-input v-model="form.outputSchemaJson" type="textarea" :rows="5" placeholder='{"audioUrl":"string"}' />
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
          <el-button type="primary" @click="submitForm">确定</el-button>
          <el-button @click="cancel">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProviderModelCatalog">
import {
  addProviderModelCatalog,
  changeProviderModelCatalogStatus,
  delProviderModelCatalog,
  getProviderModelCatalog,
  listProviderModelCatalog,
  updateProviderModelCatalog
} from '@/api/provider/modelCatalog'
import { listProviderConfig } from '@/api/provider/config'
import {
  buildConfiguredProviderSet,
  CONFIGURED_PROVIDER_FETCH_SIZE,
  countUnconfiguredProviderRows,
  filterRowsByConfiguredProvider,
  hasConfiguredProvider,
  localPageRows
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

const deploymentModeOptions = [
  { label: '云服务', value: 'cloud' },
  { label: '私有化', value: 'private' },
  { label: '本地', value: 'local' }
]

const loading = ref(false)
const showSearch = ref(true)
const modelList = ref([])
const allModelList = ref([])
const configuredProviderSet = ref(new Set())
const configuredProviderError = ref('')
const showUnconfigured = ref(false)
const hiddenCandidateCount = ref(0)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref('')

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    modelCode: undefined,
    modelName: undefined,
    configType: undefined,
    providerCode: undefined,
    deploymentMode: undefined,
    isEnabled: undefined
  },
  form: {},
  rules: {
    modelCode: [{ required: true, message: '模型编码不能为空', trigger: 'blur' }],
    modelName: [{ required: true, message: '模型名称不能为空', trigger: 'blur' }],
    configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
    providerCode: [{ required: true, message: 'Provider 不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

function configTypeLabel(value) {
  return configTypeOptions.find(item => item.value === value)?.label || value || '-'
}

function deploymentModeLabel(value) {
  return deploymentModeOptions.find(item => item.value === value)?.label || value || '-'
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
    configuredProviderError.value = '启用 Provider 配置加载失败，候选数据已默认隐藏；请检查 Provider 配置列表接口。'
  })
}

function applyModelVisibility() {
  const visibleRows = filterRowsByConfiguredProvider(allModelList.value, configuredProviderSet.value, showUnconfigured.value)
  hiddenCandidateCount.value = countUnconfiguredProviderRows(allModelList.value, configuredProviderSet.value)
  total.value = visibleRows.length
  modelList.value = localPageRows(visibleRows, queryParams.value.pageNum, queryParams.value.pageSize)
}

function reset() {
  form.value = {
    modelId: undefined,
    modelCode: undefined,
    modelName: undefined,
    configType: 'llm',
    providerCode: undefined,
    deploymentMode: 'cloud',
    capabilityTags: undefined,
    contextWindow: undefined,
    inputSchemaJson: undefined,
    outputSchemaJson: undefined,
    pricingJson: undefined,
    docsUrl: undefined,
    isEnabled: '1',
    sortOrder: 0,
    remark: undefined
  }
  proxy?.resetForm?.('modelRef')
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
    listProviderModelCatalog(requestParams)
  ]).then(([, response]) => {
    allModelList.value = response.rows || []
    applyModelVisibility()
  }).finally(() => {
    loading.value = false
  })
}

function handleVisibilityChange() {
  queryParams.value.pageNum = 1
  applyModelVisibility()
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
  ids.value = selection.map(item => item.modelId)
  single.value = selection.length !== 1
  multiple.value = selection.length === 0
}

function handleAdd() {
  reset()
  open.value = true
  title.value = '新增模型'
}

function handleUpdate(row) {
  reset()
  const modelId = row?.modelId || ids.value[0]
  getProviderModelCatalog(modelId).then(response => {
    form.value = { ...form.value, ...(response.data || {}) }
    open.value = true
    title.value = '修改模型'
  })
}

function submitForm() {
  proxy.$refs.modelRef.validate(valid => {
    if (!valid) {
      return
    }
    const request = form.value.modelId ? updateProviderModelCatalog : addProviderModelCatalog
    request(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.modelId ? '修改成功' : '新增成功')
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
  changeProviderModelCatalogStatus(row.modelId, row.isEnabled).then(() => {
    proxy.$modal.msgSuccess(row.isEnabled === '1' ? '已启用' : '已停用')
  }).catch(() => {
    row.isEnabled = row.isEnabled === '1' ? '0' : '1'
  })
}

function handleDelete(row) {
  const modelIds = row?.modelId || ids.value
  proxy.$modal.confirm(`确认删除模型编号为 ${modelIds} 的数据吗？`).then(() => {
    return delProviderModelCatalog(modelIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.model-catalog-page {
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
