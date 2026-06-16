<template>
  <div class="app-container pay-demo">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="14"><el-card shadow="never"><template #header><span>支付骨架</span></template><el-form :model="form" label-width="96px">
        <el-form-item label="订单编号"><el-input v-model="form.orderNo" placeholder="请输入测试订单编号" /></el-form-item>
        <el-form-item label="订单标题"><el-input v-model="form.subject" placeholder="请输入订单标题" /></el-form-item>
        <el-form-item label="支付金额"><el-input-number v-model="form.amount" :precision="2" :min="0.01" :step="1" /></el-form-item>
        <el-form-item label="支付渠道"><el-radio-group v-model="form.channelCode"><el-radio-button label="alipay">支付宝</el-radio-button><el-radio-button label="wechat-pay">微信支付</el-radio-button></el-radio-group></el-form-item>
        <el-form-item><el-button type="primary" @click="handleCreate">创建支付单</el-button><el-button @click="handleQuery">查询状态</el-button><el-button @click="handleClose">关闭支付单</el-button></el-form-item>
      </el-form></el-card></el-col>
      <el-col :xs="24" :lg="10"><el-card shadow="never" class="pay-preview"><template #header><span>收款码素材</span></template><img src="@/assets/images/pay.png" alt="BigYun payment QR" /><p>社区版保留支付入口、页面结构和 API 封装。真实渠道参数请在后端配置或 Provider 配置中心中接入。</p></el-card></el-col>
    </el-row>
  </div>
</template>
<script setup name="PayDemo">
import { closePayDemoOrder, createPayDemoOrder, getPayDemoOrder } from '@/api/pay/demo'
const { proxy } = getCurrentInstance()
const form = reactive({ orderNo: '', subject: 'BigYun Cloud 社区版测试订单', amount: 1, channelCode: 'alipay' })
function handleCreate() { createPayDemoOrder({ ...form }).then(() => proxy.$modal.msgSuccess('支付单创建请求已发送')) }
function handleQuery() { if (!form.orderNo) return proxy.$modal.msgWarning('请输入订单编号'); getPayDemoOrder(form.orderNo).then(() => proxy.$modal.msgSuccess('支付状态查询请求已发送')) }
function handleClose() { if (!form.orderNo) return proxy.$modal.msgWarning('请输入订单编号'); closePayDemoOrder(form.orderNo).then(() => proxy.$modal.msgSuccess('关闭支付单请求已发送')) }
</script>
<style scoped lang="scss">.pay-demo .el-card{border-radius:8px}.pay-preview img{display:block;width:100%;max-width:320px;margin:0 auto 16px}.pay-preview p{margin:0;color:#64748b;line-height:1.7}</style>