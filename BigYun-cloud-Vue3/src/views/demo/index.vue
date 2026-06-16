<template>
  <div class="app-container demo-page">
    <el-card shadow="never">
      <template #header><div class="card-header"><span>基础示例模块</span><el-button type="primary" @click="handleCreate">新增示例</el-button></div></template>
      <el-form :model="queryParams" :inline="true">
        <el-form-item label="示例名称"><el-input v-model="queryParams.itemName" placeholder="请输入示例名称" clearable @keyup.enter="getList" /></el-form-item>
        <el-form-item><el-button type="primary" @click="getList">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>
      <el-table v-loading="loading" :data="demoList">
        <el-table-column label="编码" prop="itemCode" min-width="140" />
        <el-table-column label="名称" prop="itemName" min-width="160" />
        <el-table-column label="分类" prop="category" min-width="120" />
        <el-table-column label="状态" prop="status" width="100"><template #default="{ row }"><el-tag :type="row.status === '0' ? 'success' : 'info'">{{ row.status === '0' ? '启用' : '停用' }}</el-tag></template></el-table-column>
        <el-table-column label="说明" prop="remark" min-width="220" />
      </el-table>
    </el-card>
  </div>
</template>
<script setup name="DemoIndex">
import { listDemoItem, addDemoItem } from '@/api/demo/item'
const { proxy } = getCurrentInstance()
const loading = ref(false)
const demoList = ref([])
const queryParams = reactive({ itemName: '' })
function getList() { loading.value = true; listDemoItem({ ...queryParams }).then(res => { demoList.value = res.rows || res.data || [] }).finally(() => { loading.value = false }) }
function resetQuery() { queryParams.itemName = ''; getList() }
function handleCreate() { addDemoItem({ itemCode: `DEMO-${Date.now()}`, itemName: '社区版示例', category: 'framework', status: '0', remark: '用于展示菜单、前端、接口、服务、Mapper 与 SQL 的完整链路。' }).then(() => { proxy.$modal.msgSuccess('示例保存请求已发送'); getList() }) }
getList()
</script>
<style scoped lang="scss">.demo-page .el-card{border-radius:8px}.card-header{display:flex;align-items:center;justify-content:space-between;gap:12px}</style>