<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="配置类型" prop="configType">
        <el-select v-model="queryParams.configType" clearable style="width: 180px" placeholder="请选择配置类型">
          <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="Provider" prop="providerCode">
        <el-select v-model="queryParams.providerCode" clearable style="width: 180px" placeholder="请选择 Provider">
          <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="配置名称" prop="providerName">
        <el-input
          v-model="queryParams.providerName"
          placeholder="请输入配置名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" clearable style="width: 160px" placeholder="请选择状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="Plus" @click="handleAdd" v-hasPermi="['provider:config:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate" v-hasPermi="['provider:config:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete" v-hasPermi="['provider:config:remove']">删除</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="configList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="configId" width="80" />
      <el-table-column label="配置类型" align="center" prop="configType" width="110">
        <template #default="scope">
          <el-tag>{{ configTypeLabel(scope.row.configType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Provider" align="center" prop="providerCode" width="120">
        <template #default="scope">
          <span>{{ providerLabel(scope.row.providerCode) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="配置名称" align="center" prop="providerName" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="Bucket/路径" align="center" min-width="160" :show-overflow-tooltip="true">
        <template #default="scope">
          <span>{{ scope.row.bucketName || scope.row.basePath || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="访问端点" align="center" prop="endpoint" min-width="200" :show-overflow-tooltip="true" />
      <el-table-column label="访问域名" align="center" prop="domain" min-width="220" :show-overflow-tooltip="true" />
      <el-table-column label="默认" align="center" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isDefault === 'Y' ? 'success' : 'info'">{{ scope.row.isDefault === 'Y' ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="110">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            active-value="0"
            inactive-value="1"
            @change="handleStatusChange(scope.row)"
            v-hasPermi="['provider:config:edit']"
          />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="180">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="250" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="CircleCheck" @click="handleSetDefault(scope.row)" v-hasPermi="['provider:config:edit']">设为默认</el-button>
          <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)" v-hasPermi="['provider:config:edit']">修改</el-button>
          <el-button link type="primary" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['provider:config:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog :title="title" v-model="open" width="720px" append-to-body>
      <el-form ref="configRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="form.configType" style="width: 100%">
                <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Provider" prop="providerCode">
              <el-select v-model="form.providerCode" style="width: 100%" @change="handleProviderChange">
                <el-option v-for="item in providerOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置名称" prop="providerName">
              <el-input v-model="form.providerName" placeholder="请输入配置名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="showEndpoint">
            <el-form-item label="访问端点" prop="endpoint">
              <el-input v-model="form.endpoint" placeholder="如 https://oss-cn-hangzhou.aliyuncs.com" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="showRegion">
            <el-form-item label="地域" prop="region">
              <el-input v-model="form.region" placeholder="如 ap-guangzhou" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="showBucket">
            <el-form-item label="Bucket" prop="bucketName">
              <el-input v-model="form.bucketName" placeholder="请输入 Bucket 名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="isLocalProvider ? '访问域名' : '自定义域名'" prop="domain">
              <el-input v-model="form.domain" placeholder="请输入访问域名或自定义域名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="isLocalProvider ? '存储根路径' : '对象前缀'" prop="basePath">
              <el-input v-model="form.basePath" :placeholder="isLocalProvider ? '如 /data/bigyun/upload' : '如 bigyun/assets'" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="showCredential">
            <el-form-item label="AccessKey" prop="accessKey">
              <el-input v-model="form.accessKey" :placeholder="credentialPlaceholder(form.accessKeyMasked)" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="showCredential">
            <el-form-item label="SecretKey" prop="secretKey">
              <el-input v-model="form.secretKey" :placeholder="credentialPlaceholder(form.secretKeyMasked)" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认配置" prop="isDefault">
              <el-radio-group v-model="form.isDefault">
                <el-radio value="Y">是</el-radio>
                <el-radio value="N">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio v-for="item in statusOptions" :key="item.value" :value="item.value">{{ item.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="扩展参数" prop="extParamsJson">
              <el-input v-model="form.extParamsJson" type="textarea" :rows="4" placeholder='请输入 JSON，如 {"acl":"public-read"}' />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProviderConfig">
import {
  addProviderConfig,
  changeProviderConfigStatus,
  delProviderConfig,
  getProviderConfig,
  listProviderConfig,
  setDefaultProviderConfig,
  updateProviderConfig
} from "@/api/provider/config"

const { proxy } = getCurrentInstance()

const configTypeOptions = [
  { label: "对象存储", value: "storage" }
]

const providerOptions = [
  { label: "本地存储", value: "local" },
  { label: "MinIO", value: "minio" },
  { label: "阿里云 OSS", value: "aliyun-oss" },
  { label: "腾讯云 COS", value: "tencent-cos" }
]

const statusOptions = [
  { label: "启用", value: "0" },
  { label: "停用", value: "1" }
]

const configList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const open = ref(false)
const title = ref("")

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    configType: "storage",
    providerCode: undefined,
    providerName: undefined,
    status: undefined
  },
  rules: {
    configType: [{ required: true, message: "配置类型不能为空", trigger: "change" }],
    providerCode: [{ required: true, message: "Provider 不能为空", trigger: "change" }],
    providerName: [{ required: true, message: "配置名称不能为空", trigger: "blur" }]
  }
})

const { form, queryParams, rules } = toRefs(data)

const isLocalProvider = computed(() => form.value.providerCode === "local")
const showCredential = computed(() => form.value.providerCode && form.value.providerCode !== "local")
const showBucket = computed(() => form.value.providerCode && form.value.providerCode !== "local")
const showEndpoint = computed(() => ["minio", "aliyun-oss"].includes(form.value.providerCode))
const showRegion = computed(() => ["aliyun-oss", "tencent-cos"].includes(form.value.providerCode))

function providerLabel(value) {
  return providerOptions.find(item => item.value === value)?.label || value
}

function configTypeLabel(value) {
  return configTypeOptions.find(item => item.value === value)?.label || value
}

function credentialPlaceholder(masked) {
  return masked ? `留空则保留当前值 (${masked})` : "请输入密钥"
}

function getList() {
  loading.value = true
  listProviderConfig(queryParams.value).then(response => {
    configList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function cancel() {
  open.value = false
  reset()
}

function reset() {
  form.value = {
    configId: undefined,
    configType: "storage",
    providerCode: "local",
    providerName: undefined,
    endpoint: undefined,
    region: undefined,
    bucketName: undefined,
    accessKey: undefined,
    secretKey: undefined,
    accessKeyMasked: undefined,
    secretKeyMasked: undefined,
    domain: undefined,
    basePath: undefined,
    extParamsJson: undefined,
    isDefault: "N",
    status: "0",
    remark: undefined
  }
  proxy.resetForm("configRef")
}

function handleProviderChange() {
  if (isLocalProvider.value) {
    form.value.endpoint = undefined
    form.value.region = undefined
    form.value.bucketName = undefined
    form.value.accessKey = undefined
    form.value.secretKey = undefined
  }
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm("queryRef")
  queryParams.value.pageNum = 1
  queryParams.value.configType = "storage"
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.configId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

function handleAdd() {
  reset()
  open.value = true
  title.value = "新增 Provider 配置"
}

function handleUpdate(row) {
  reset()
  const configId = row.configId || ids.value[0]
  getProviderConfig(configId).then(response => {
    form.value = { ...form.value, ...response.data, accessKey: undefined, secretKey: undefined }
    open.value = true
    title.value = "修改 Provider 配置"
  })
}

function submitForm() {
  proxy.$refs["configRef"].validate(valid => {
    if (!valid) {
      return
    }
    const request = form.value.configId ? updateProviderConfig : addProviderConfig
    request(form.value).then(() => {
      proxy.$modal.msgSuccess(form.value.configId ? "修改成功" : "新增成功")
      open.value = false
      getList()
    })
  })
}

function handleDelete(row) {
  const configIds = row.configId || ids.value
  proxy.$modal.confirm(`是否确认删除 Provider 配置编号为"${configIds}"的数据项？`).then(() => {
    return delProviderConfig(configIds)
  }).then(() => {
    proxy.$modal.msgSuccess("删除成功")
    getList()
  }).catch(() => {})
}

function handleStatusChange(row) {
  const text = row.status === "0" ? "启用" : "停用"
  changeProviderConfigStatus(row.configId, row.status).then(() => {
    proxy.$modal.msgSuccess(`${text}成功`)
    getList()
  }).catch(() => {
    row.status = row.status === "0" ? "1" : "0"
  })
}

function handleSetDefault(row) {
  if (row.isDefault === "Y") {
    proxy.$modal.msgInfo("当前已经是默认配置")
    return
  }
  setDefaultProviderConfig(row.configId).then(response => {
    proxy.$modal.msgSuccess(response.msg || "默认配置已切换，缓存刷新通知已发送")
    getList()
  })
}

getList()
</script>
