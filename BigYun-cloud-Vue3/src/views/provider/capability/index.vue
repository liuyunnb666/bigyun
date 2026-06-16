<template>
  <div class="app-container provider-capability-page">
    <div class="page-head">
      <div>
        <div class="page-title">Provider 能力中心</div>
        <div class="page-desc">管理业务能力、模型关系、运行时快照和调用日志，业务侧只需要关心 capabilityCode。</div>
      </div>
      <div class="page-actions">
        <el-switch
          v-model="showUnconfigured"
          active-text="显示候选/未配置数据"
          inactive-text="只看已配置"
          @change="handleVisibilityChange"
        />
        <el-button icon="Refresh" @click="refreshCurrent">刷新</el-button>
        <el-button type="primary" icon="RefreshRight" @click="handleRefreshRuntime()" v-hasPermi="['provider:config:edit']">刷新运行时</el-button>
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
      v-if="!showUnconfigured && activeTab === 'capability' && hiddenCapabilityCount > 0"
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      :title="`已隐藏 ${hiddenCapabilityCount} 条未配置 Provider 支撑的能力定义，打开“显示候选/未配置数据”可查看。`"
    />
    <el-alert
      v-if="!showUnconfigured && activeTab === 'relation' && hiddenRelationCount > 0"
      class="mb8"
      type="info"
      :closable="false"
      show-icon
      :title="`已隐藏 ${hiddenRelationCount} 条未配置 Provider 支撑的模型关系，打开“显示候选/未配置数据”可查看。`"
    />

    <el-tabs v-model="activeTab">
      <el-tab-pane label="能力定义" name="capability">
        <el-form ref="queryRef" :model="queryParams" :inline="true" v-show="showSearch" label-width="88px">
          <el-form-item label="能力编码" prop="capabilityCode">
            <el-input v-model="queryParams.capabilityCode" clearable placeholder="capabilityCode" style="width: 210px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="业务场景" prop="businessScene">
            <el-input v-model="queryParams.businessScene" clearable placeholder="businessScene" style="width: 180px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="能力层级" prop="capabilityLayer">
            <el-select v-model="queryParams.capabilityLayer" clearable placeholder="全部" style="width: 140px">
              <el-option v-for="item in capabilityLayerOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="配置类型" prop="configType">
            <el-select v-model="queryParams.configType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="Provider" prop="providerCode">
            <el-input v-model="queryParams.providerCode" clearable placeholder="providerCode" style="width: 170px" @keyup.enter="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" clearable placeholder="全部" style="width: 120px">
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
            <el-button type="primary" plain icon="Plus" @click="handleAddCapability" v-hasPermi="['provider:config:add']">新增能力</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdateCapability()" v-hasPermi="['provider:config:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="CircleCheck" :disabled="single" @click="handleSetDefault()" v-hasPermi="['provider:config:edit']">设为默认</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDeleteCapability()" v-hasPermi="['provider:config:remove']">删除</el-button>
          </el-col>
          <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
        </el-row>

        <el-table v-loading="loading" :data="capabilityList" :row-class-name="capabilityRowClassName" @selection-change="handleSelectionChange">
          <el-table-column type="selection" width="55" align="center" />
          <el-table-column label="ID" prop="capabilityId" width="80" align="center" />
          <el-table-column label="能力编码" prop="capabilityCode" min-width="180" :show-overflow-tooltip="true" />
          <el-table-column label="能力名称" prop="capabilityName" min-width="160" :show-overflow-tooltip="true" />
          <el-table-column label="业务场景" prop="businessScene" min-width="130" :show-overflow-tooltip="true" />
          <el-table-column label="层级" prop="capabilityLayer" width="110" align="center">
            <template #default="scope">
              <el-tag type="info">{{ capabilityLayerLabel(scope.row.capabilityLayer) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="类型" prop="configType" width="90" align="center">
            <template #default="scope">
              <el-tag>{{ configTypeLabel(scope.row.configType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Provider" prop="providerCode" min-width="140" :show-overflow-tooltip="true" />
          <el-table-column v-if="showUnconfigured" label="配置支撑" width="110" align="center">
            <template #default="scope">
              <el-tag :type="isRowConfigured(scope.row) ? 'success' : 'warning'">
                {{ isRowConfigured(scope.row) ? '已配置' : '未配置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Operation" prop="operation" min-width="120" :show-overflow-tooltip="true" />
          <el-table-column label="模型" prop="modelCode" min-width="150" :show-overflow-tooltip="true" />
          <el-table-column label="默认" prop="isDefault" width="132" align="center">
            <template #default="scope">
              <el-tag :type="isDefault(scope.row) ? 'success' : 'info'">{{ isDefault(scope.row) ? '是' : '否' }}</el-tag>
              <el-tag v-if="isPendingDefaultCapability(scope.row)" class="support-warning-tag" type="warning" effect="plain">待发布默认项</el-tag>
              <el-tag v-if="showUnconfigured && isDefault(scope.row) && !isRowConfigured(scope.row)" class="support-warning-tag" type="danger" effect="plain">默认无配置</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="0"
                inactive-value="1"
                @change="handleCapabilityStatusChange(scope.row)"
                v-hasPermi="['provider:config:edit']"
              />
            </template>
          </el-table-column>
          <el-table-column label="优先级" prop="priority" width="90" align="center" />
          <el-table-column label="更新时间" prop="updateTime" width="180" align="center">
            <template #default="scope">{{ parseTime(scope.row.updateTime || scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="280" fixed="right" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleUpdateCapability(scope.row)" v-hasPermi="['provider:config:edit']">修改</el-button>
              <el-button link type="primary" icon="CircleCheck" :disabled="isDefault(scope.row) || scope.row.status !== '0'" @click="handleSetDefault(scope.row)" v-hasPermi="['provider:config:edit']">默认</el-button>
              <el-button link type="warning" icon="Link" @click="openRelations(scope.row)">关系</el-button>
              <el-button link type="info" icon="Monitor" @click="openSnapshot(scope.row)">快照</el-button>
              <el-button link type="info" icon="Document" @click="openLogs(scope.row)">日志</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :description="showUnconfigured ? '暂无能力定义数据' : '暂无已配置 Provider 支撑的能力定义，打开“显示候选/未配置数据”可查看候选记录。'" />
          </template>
        </el-table>

        <pagination v-show="total > 0" :total="total" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" @pagination="getList" />
      </el-tab-pane>

      <el-tab-pane label="模型关系" name="relation">
        <el-form ref="relationQueryRef" :model="relationQueryParams" :inline="true" label-width="88px">
          <el-form-item label="能力编码" prop="capabilityCode">
            <el-input v-model="relationQueryParams.capabilityCode" clearable placeholder="capabilityCode" style="width: 210px" @keyup.enter="handleRelationQuery" />
          </el-form-item>
          <el-form-item label="模型编码" prop="modelCode">
            <el-input v-model="relationQueryParams.modelCode" clearable placeholder="modelCode" style="width: 180px" @keyup.enter="handleRelationQuery" />
          </el-form-item>
          <el-form-item label="关系类型" prop="relationType">
            <el-select v-model="relationQueryParams.relationType" clearable placeholder="全部" style="width: 150px">
              <el-option v-for="item in relationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="relationQueryParams.status" clearable placeholder="全部" style="width: 120px">
              <el-option label="启用" value="0" />
              <el-option label="停用" value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleRelationQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetRelationQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="Plus" @click="handleAddRelation" v-hasPermi="['provider:config:edit']">新增关系</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="warning" plain icon="Refresh" @click="refreshRelations">刷新关系</el-button>
          </el-col>
        </el-row>

        <el-alert
          v-if="!relationCapabilityCode"
          class="mb8"
          type="info"
          :closable="false"
          show-icon
          title="请输入能力编码后查看当前发布模型；模型关系是候选切换关系，不是同时调用列表。"
        />

        <div v-else class="relation-runtime-panel" v-loading="relationRuntimeLoading">
          <div class="relation-runtime-head">
            <div>
              <div class="relation-runtime-title">当前发布模型</div>
              <div class="relation-runtime-subtitle">能力编码：{{ relationCapabilityCode }}。当前发布以 Redis 运行时快照为准，数据库默认项表示待发布版本。</div>
            </div>
            <el-button
              type="primary"
              plain
              icon="RefreshRight"
              :disabled="!relationDefaultCapability"
              @click="publishRelationRuntimeSnapshot"
              v-hasPermi="['provider:config:edit']"
            >
              发布默认项到运行时快照
            </el-button>
          </div>

          <el-alert
            v-if="relationRuntimeMismatch"
            class="relation-runtime-alert"
            type="warning"
            :closable="false"
            show-icon
            title="Redis 当前发布模型与数据库默认项不一致；当前调用仍走 Redis 快照，点击发布按钮后才会切到数据库默认项。"
          />

          <div class="relation-runtime-grid">
            <div class="relation-runtime-item relation-runtime-current">
              <div class="relation-runtime-label">Redis 当前发布使用</div>
              <div v-if="relationRuntimePublished" class="relation-runtime-main">
                {{ relationRuntimeSnapshot.providerCode || '-' }} / {{ relationRuntimeSnapshot.modelCode || '-' }}
              </div>
              <div v-else class="relation-runtime-main relation-runtime-empty">未发布快照</div>
              <div class="relation-runtime-meta">
                Operation：{{ relationRuntimeSnapshot?.operation || '-' }}
                <span class="meta-divider">|</span>
                发布时间：{{ relationRuntimeSnapshot?.publishedAt ? parseTime(relationRuntimeSnapshot.publishedAt) : '-' }}
              </div>
            </div>
            <div class="relation-runtime-item relation-runtime-default">
              <div class="relation-runtime-label">数据库待发布默认项</div>
              <div v-if="relationDefaultCapability" class="relation-runtime-main">
                {{ relationDefaultCapability.providerCode || '-' }} / {{ relationDefaultCapability.modelCode || '-' }}
              </div>
              <div v-else class="relation-runtime-main relation-runtime-empty">无启用默认项</div>
              <div class="relation-runtime-meta">
                Operation：{{ relationDefaultCapability?.operation || '-' }}
                <span class="meta-divider">|</span>
                状态：{{ relationDefaultCapability ? '启用默认' : '请先在能力定义设置默认项' }}
              </div>
            </div>
          </div>
        </div>

        <el-table v-loading="relationLoading" :data="relationList">
          <el-table-column label="ID" prop="relationId" width="80" align="center" />
          <el-table-column label="源能力" min-width="190" :show-overflow-tooltip="true">
            <template #default="scope">
              <div class="cell-title">
                {{ scope.row.bindingDisplayName || scope.row.capabilityName || scope.row.capabilityCode }}
                <el-tag v-if="isRelationSourcePublished(scope.row)" class="support-warning-tag" type="success" effect="dark">当前发布</el-tag>
                <el-tag v-if="isRelationSourceDefault(scope.row)" class="support-warning-tag" type="warning">待发布默认项</el-tag>
              </div>
              <div class="cell-subtitle">{{ relationSourceSubtitle(scope.row) }}</div>
            </template>
          </el-table-column>
          <el-table-column label="目标模型" min-width="190" :show-overflow-tooltip="true">
            <template #default="scope">
              <div class="cell-title">
                {{ scope.row.targetModelDisplayName || scope.row.modelName || scope.row.modelCode }}
                <el-tag class="support-warning-tag" type="info" effect="plain">候选替换项</el-tag>
                <el-tag v-if="isRelationTargetPublished(scope.row)" class="support-warning-tag" type="success" effect="dark">当前发布</el-tag>
                <el-tag v-if="isRelationTargetDefault(scope.row)" class="support-warning-tag" type="warning">待发布默认项</el-tag>
              </div>
              <div class="cell-subtitle">{{ relationTargetSubtitle(scope.row) }}</div>
            </template>
          </el-table-column>
          <el-table-column v-if="showUnconfigured" label="配置支撑" width="110" align="center">
            <template #default="scope">
              <el-tag :type="isRelationConfigured(scope.row) ? 'success' : 'warning'">
                {{ isRelationConfigured(scope.row) ? '已配置' : '未配置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="关系" prop="relationType" width="130" align="center">
            <template #default="scope">
              <el-tag :type="relationTypeTag(scope.row.relationType)">{{ relationTypeLabel(scope.row.relationType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="同业务" prop="sameBusinessFlag" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.sameBusinessFlag === '1' ? 'success' : 'info'">{{ scope.row.sameBusinessFlag === '1' ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === '0' ? 'success' : 'info'">{{ scope.row.status === '0' ? '启用' : '停用' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="说明" prop="relationReason" min-width="220" :show-overflow-tooltip="true" />
          <el-table-column label="操作" width="230" fixed="right" align="center">
            <template #default="scope">
              <el-button link type="primary" icon="Edit" @click="handleEditRelation(scope.row)" v-hasPermi="['provider:config:edit']">修改</el-button>
              <el-button link type="warning" icon="CircleCheck" :disabled="!scope.row.targetCapabilityId" @click="handleSwitchRelation(scope.row)" v-hasPermi="['provider:config:edit']">切默认</el-button>
              <el-button link type="danger" icon="Delete" @click="handleDeleteRelation(scope.row)" v-hasPermi="['provider:config:edit']">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty :description="showUnconfigured ? '暂无模型关系数据' : '暂无已配置 Provider 支撑的模型关系，打开“显示候选/未配置数据”可查看候选记录。'" />
          </template>
        </el-table>

        <pagination v-show="relationTotal > 0" :total="relationTotal" v-model:page="relationQueryParams.pageNum" v-model:limit="relationQueryParams.pageSize" @pagination="getRelations" />
      </el-tab-pane>

      <el-tab-pane label="运行时快照" name="snapshot">
        <el-alert class="mb8" type="info" :closable="false" show-icon title="列表展示 Redis 中已发布的运行时快照；能力定义中“默认=是、状态=启用”的行是数据库待发布默认项，当前调用以 Redis 快照为准。" />
        <el-form ref="snapshotQueryRef" :model="snapshotQuery" :inline="true" label-width="104px">
          <el-form-item label="能力编码" prop="capabilityCode">
            <el-input v-model="snapshotQuery.capabilityCode" clearable placeholder="capabilityCode" style="width: 230px" />
          </el-form-item>
          <el-form-item label="配置类型" prop="configType">
            <el-select v-model="snapshotQuery.configType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="Provider" prop="providerCode">
            <el-input v-model="snapshotQuery.providerCode" clearable placeholder="providerCode" style="width: 180px" />
          </el-form-item>
          <el-form-item label="Operation" prop="operation">
            <el-input v-model="snapshotQuery.operation" clearable placeholder="operation" style="width: 160px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="loadSnapshotList">查询列表</el-button>
            <el-button icon="View" @click="querySnapshot">查询详情</el-button>
            <el-button icon="Refresh" @click="resetSnapshotQuery">重置</el-button>
            <el-button icon="RefreshRight" @click="handleRefreshRuntime(snapshotQuery)" v-hasPermi="['provider:config:edit']">刷新快照</el-button>
          </el-form-item>
        </el-form>

        <el-table
          v-loading="snapshotLoading"
          :data="snapshotList"
          highlight-current-row
          @row-click="handleSnapshotRowClick"
        >
          <el-table-column label="能力编码" prop="capabilityCode" min-width="180" :show-overflow-tooltip="true" />
          <el-table-column label="类型" prop="configType" width="90" align="center">
            <template #default="scope">
              <el-tag>{{ configTypeLabel(scope.row.configType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="Provider" prop="providerCode" min-width="140" :show-overflow-tooltip="true" />
          <el-table-column label="Operation" prop="operation" min-width="120" :show-overflow-tooltip="true" />
          <el-table-column label="模型" prop="modelCode" min-width="150" :show-overflow-tooltip="true" />
          <el-table-column label="来源" prop="runtimeSource" width="130" align="center">
            <template #default="scope">
              <el-tag :type="snapshotSourceTagType(scope.row.runtimeSource)">{{ snapshotSourceLabel(scope.row.runtimeSource) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="发布时间" prop="publishedAt" width="180" align="center">
            <template #default="scope">{{ scope.row.publishedAt ? parseTime(scope.row.publishedAt) : '-' }}</template>
          </el-table-column>
          <el-table-column label="Provider 配置" width="120" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.hasProviderConfig ? 'success' : 'danger'">{{ scope.row.hasProviderConfig ? '已包含' : '未包含' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="API 模板" width="110" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.hasApiTemplate ? 'success' : 'danger'">{{ scope.row.hasApiTemplate ? '已包含' : '未包含' }}</el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <el-empty description="暂无已发布快照，请刷新运行时，或检查默认能力的 Provider 配置和 API 模板。" />
          </template>
        </el-table>

        <el-alert
          v-if="snapshotResult && snapshotResult.runtimeSource === 'not-found'"
          class="snapshot-box"
          type="warning"
          show-icon
          :closable="false"
          title="未发布快照：请检查该能力是否存在启用默认项、Provider 配置是否启用、API 模板是否完整，然后刷新运行时。"
        />
        <el-descriptions v-if="snapshotResult" :column="2" border class="snapshot-box">
          <el-descriptions-item label="生效来源">
            <el-tag :type="snapshotSourceTagType(snapshotResult.runtimeSource)">{{ snapshotSourceLabel(snapshotResult.runtimeSource) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本">{{ snapshotResult.version || '-' }}</el-descriptions-item>
          <el-descriptions-item label="能力编码">{{ snapshotResult.capabilityCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="配置类型">{{ snapshotResult.configType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Provider">{{ snapshotResult.providerCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="Operation">{{ snapshotResult.operation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ snapshotResult.modelCode || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ snapshotResult.publishedAt ? parseTime(snapshotResult.publishedAt) : '-' }}</el-descriptions-item>
          <el-descriptions-item label="Provider 配置">{{ snapshotResult.hasProviderConfig ? '已包含' : '未包含' }}</el-descriptions-item>
          <el-descriptions-item label="API 模板">{{ snapshotResult.hasApiTemplate ? '已包含' : '未包含' }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <el-tab-pane label="调用日志" name="logs">
        <el-form ref="logQueryRef" :model="logQueryParams" :inline="true" label-width="88px">
          <el-form-item label="能力编码" prop="capabilityCode">
            <el-input v-model="logQueryParams.capabilityCode" clearable placeholder="capabilityCode" style="width: 210px" @keyup.enter="handleLogQuery" />
          </el-form-item>
          <el-form-item label="配置类型" prop="configType">
            <el-select v-model="logQueryParams.configType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="Provider" prop="providerCode">
            <el-input v-model="logQueryParams.providerCode" clearable placeholder="providerCode" style="width: 170px" @keyup.enter="handleLogQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="logQueryParams.status" clearable placeholder="全部" style="width: 120px">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="fail" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="Search" @click="handleLogQuery">搜索</el-button>
            <el-button icon="Refresh" @click="resetLogQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="logLoading" :data="logList">
          <el-table-column label="时间" prop="createTime" width="180" align="center">
            <template #default="scope">{{ parseTime(scope.row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="能力编码" prop="capabilityCode" min-width="180" :show-overflow-tooltip="true" />
          <el-table-column label="类型" prop="configType" width="90" align="center" />
          <el-table-column label="Provider" prop="providerCode" min-width="140" :show-overflow-tooltip="true" />
          <el-table-column label="Operation" prop="operation" min-width="120" :show-overflow-tooltip="true" />
          <el-table-column label="模型" prop="modelCode" min-width="140" :show-overflow-tooltip="true" />
          <el-table-column label="来源" prop="runtimeSource" min-width="130" :show-overflow-tooltip="true" />
          <el-table-column label="耗时" prop="durationMs" width="100" align="center">
            <template #default="scope">{{ scope.row.durationMs ?? '-' }} ms</template>
          </el-table-column>
          <el-table-column label="状态" prop="status" width="90" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'success' ? 'success' : 'danger'">{{ scope.row.status === 'success' ? '成功' : '失败' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="错误摘要" prop="errorMessage" min-width="220" :show-overflow-tooltip="true">
            <template #default="scope">{{ scope.row.errorMessage || '-' }}</template>
          </el-table-column>
        </el-table>

        <pagination v-show="logTotal > 0" :total="logTotal" v-model:page="logQueryParams.pageNum" v-model:limit="logQueryParams.pageSize" @pagination="getLogs" />
      </el-tab-pane>
    </el-tabs>

    <el-dialog :title="capabilityDialogTitle" v-model="capabilityDialogOpen" width="920px" append-to-body>
      <el-form ref="capabilityRef" :model="capabilityForm" :rules="capabilityRules" label-width="112px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="能力编码" prop="capabilityCode">
              <el-input v-model="capabilityForm.capabilityCode" placeholder="如 speech_tts_synthesize" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="能力名称" prop="capabilityName">
              <el-input v-model="capabilityForm.capabilityName" placeholder="如 语音合成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="业务场景" prop="businessScene">
              <el-input v-model="capabilityForm.businessScene" placeholder="如 speech_tts" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="能力层级" prop="capabilityLayer">
              <el-select v-model="capabilityForm.capabilityLayer" clearable style="width: 100%">
                <el-option v-for="item in capabilityLayerOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="配置类型" prop="configType">
              <el-select v-model="capabilityForm.configType" style="width: 100%">
                <el-option v-for="item in configTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Provider" prop="providerCode">
              <el-input v-model="capabilityForm.providerCode" placeholder="providerCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Operation" prop="operation">
              <el-input v-model="capabilityForm.operation" placeholder="如 chat/synthesize/file_transcribe" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模型编码" prop="modelCode">
              <el-input v-model="capabilityForm.modelCode" placeholder="modelCode" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="优先级" prop="priority">
              <el-input-number v-model="capabilityForm.priority" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="默认项" prop="isDefault">
              <el-radio-group v-model="capabilityForm.isDefault">
                <el-radio label="1">是</el-radio>
                <el-radio label="0">否</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="capabilityForm.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="路由建议" prop="routingHint">
              <el-input v-model="capabilityForm.routingHint" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入参说明" prop="inputSchemaJson">
              <el-input v-model="capabilityForm.inputSchemaJson" type="textarea" :rows="5" placeholder='{"text":"string"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出参说明" prop="outputSchemaJson">
              <el-input v-model="capabilityForm.outputSchemaJson" type="textarea" :rows="5" placeholder='{"audioUrl":"string"}' />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="降级能力" prop="fallbackCapabilityCode">
              <el-input v-model="capabilityForm.fallbackCapabilityCode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="备注" prop="remark">
              <el-input v-model="capabilityForm.remark" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitCapabilityForm">确定</el-button>
          <el-button @click="capabilityDialogOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog :title="relationDialogTitle" v-model="relationDialogOpen" width="760px" append-to-body>
      <el-form ref="relationRef" :model="relationForm" :rules="relationRules" label-width="110px">
        <el-form-item label="源能力" prop="capabilityId">
          <el-select v-model="relationForm.capabilityId" filterable style="width: 100%" placeholder="请选择源能力">
            <el-option
              v-for="item in candidateOptions"
              :key="item.capabilityId"
              :label="candidateLabel(item)"
              :value="item.capabilityId"
              :disabled="item.capabilityId === relationForm.targetCapabilityId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标能力" prop="targetCapabilityId">
          <el-select v-model="relationForm.targetCapabilityId" filterable style="width: 100%" placeholder="请选择目标能力">
            <el-option
              v-for="item in candidateOptions"
              :key="item.capabilityId"
              :label="candidateLabel(item)"
              :value="item.capabilityId"
              :disabled="item.capabilityId === relationForm.capabilityId"
            />
          </el-select>
        </el-form-item>
        <el-alert
          v-if="relationCompareResult"
          class="relation-compare-alert"
          :type="relationCompareResult.compatibleFlag === '1' ? 'success' : 'warning'"
          :closable="false"
          show-icon
        >
          <template #title>
            <span>{{ relationCompareResult.comparisonSummary || '已完成能力模型比较' }}</span>
          </template>
          <div class="relation-compare-body">
            <el-tag :type="relationCompareResult.sameBusinessFlag === '1' ? 'success' : 'info'">
              {{ relationCompareResult.sameBusinessFlag === '1' ? '同业务' : '非同业务' }}
            </el-tag>
            <el-tag :type="relationCompareResult.compatibleFlag === '1' ? 'success' : 'warning'">
              {{ relationCompareResult.compatibleFlag === '1' ? '可兼容' : '需谨慎' }}
            </el-tag>
            <el-tag type="primary">建议：{{ relationTypeLabel(relationCompareResult.relationTypeSuggestion) }}</el-tag>
          </div>
          <div v-if="relationCompareResult.mismatches?.length" class="relation-compare-detail">
            {{ relationCompareResult.mismatches.join('；') }}
          </div>
        </el-alert>
        <el-alert
          v-else-if="relationCompareLoading"
          class="relation-compare-alert"
          type="info"
          :closable="false"
          show-icon
          title="正在比较源能力和目标能力..."
        />
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="关系类型" prop="relationType">
              <el-select v-model="relationForm.relationType" style="width: 100%">
                <el-option v-for="item in relationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="relationForm.status">
                <el-radio label="0">启用</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="说明" prop="relationReason">
          <el-input v-model="relationForm.relationReason" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="relationForm.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="relationForm.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitRelationForm">确定</el-button>
          <el-button @click="relationDialogOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProviderCapability">
import {
  addProviderCapability,
  changeProviderCapabilityStatus,
  delProviderCapability,
  getProviderCapability,
  getProviderRuntimeSnapshot,
  listProviderConfig,
  listProviderCapability,
  listProviderCapabilityLogs,
  listProviderRuntimeSnapshots,
  refreshProviderRuntimeCache,
  setDefaultProviderCapability,
  updateProviderCapability
} from '@/api/provider/config'
import {
  addCapabilityRelation,
  compareCapabilityModels,
  delCapabilityRelation,
  getCapabilityRelation,
  listCapabilityCandidates,
  listCapabilityRelations,
  switchCapabilityDefaultModel,
  updateCapabilityRelation
} from '@/api/provider/capability'
import {
  buildConfiguredProviderSet,
  CONFIGURED_PROVIDER_FETCH_SIZE,
  countUnconfiguredProviderRows,
  countUnconfiguredTargetProviderRows,
  filterRowsByConfiguredProvider,
  filterRowsByConfiguredTargetProvider,
  hasConfiguredProvider,
  hasConfiguredTargetProvider,
  localPageRows
} from '@/utils/providerConfiguredFilter'
import { computed, getCurrentInstance, onMounted, reactive, ref, toRefs, watch } from 'vue'

const { proxy } = getCurrentInstance()

const configTypeOptions = [
  { label: 'LLM', value: 'llm' },
  { label: 'TTS', value: 'tts' },
  { label: 'STT', value: 'stt' },
  { label: 'OCR', value: 'ocr' },
  { label: '人脸', value: 'face' },
  { label: '视觉', value: 'vision' },
  { label: '存储', value: 'storage' },
  { label: '声纹', value: 'voiceprint' }
]

const capabilityLayerOptions = [
  { label: '抽取', value: 'extract' },
  { label: '校验', value: 'verify' },
  { label: '理解', value: 'understand' },
  { label: '生成', value: 'generate' },
  { label: '分析', value: 'analyze' },
  { label: '注册', value: 'enroll' }
]

const relationTypeOptions = [
  { label: '同业务', value: 'same_business' },
  { label: '可替换', value: 'replaceable' },
  { label: '主备', value: 'primary_backup' },
  { label: 'A/B', value: 'ab_test' },
  { label: '互斥', value: 'mutual_exclusive' }
]

const activeTab = ref('capability')
const showSearch = ref(true)
const loading = ref(false)
const relationLoading = ref(false)
const snapshotLoading = ref(false)
const logLoading = ref(false)
const capabilityDialogOpen = ref(false)
const relationDialogOpen = ref(false)
const relationCompareLoading = ref(false)
const capabilityList = ref([])
const allCapabilityList = ref([])
const relationList = ref([])
const allRelationList = ref([])
const logList = ref([])
const candidateOptions = ref([])
const allCandidateOptions = ref([])
const configuredProviderSet = ref(new Set())
const configuredProviderError = ref('')
const showUnconfigured = ref(false)
const hiddenCapabilityCount = ref(0)
const hiddenRelationCount = ref(0)
const relationSourceCapabilityId = ref(undefined)
const relationRuntimeLoading = ref(false)
const relationRuntimeSnapshot = ref(null)
const relationDefaultCapability = ref(null)
const relationCapabilityCandidates = ref([])
const selectedRows = ref([])
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const relationTotal = ref(0)
const logTotal = ref(0)
const snapshotList = ref([])
const snapshotResult = ref(null)
const relationCompareResult = ref(null)

const createQueryParams = () => ({
  pageNum: 1,
  pageSize: 10,
  capabilityCode: undefined,
  businessScene: undefined,
  capabilityLayer: undefined,
  configType: undefined,
  providerCode: undefined,
  status: undefined
})

const createCapabilityForm = () => ({
  capabilityId: undefined,
  capabilityCode: undefined,
  capabilityName: undefined,
  businessScene: undefined,
  capabilityLayer: undefined,
  configType: 'llm',
  providerCode: undefined,
  operation: 'chat',
  modelCode: undefined,
  priority: 100,
  isDefault: '0',
  status: '0',
  inputSchemaJson: undefined,
  outputSchemaJson: undefined,
  routingHint: undefined,
  fallbackCapabilityCode: undefined,
  remark: undefined
})

const createRelationForm = () => ({
  relationId: undefined,
  capabilityId: undefined,
  targetCapabilityId: undefined,
  relationType: 'replaceable',
  sameBusinessFlag: undefined,
  relationReason: undefined,
  status: '0',
  sortOrder: 0,
  remark: undefined
})

const data = reactive({
  queryParams: createQueryParams(),
  capabilityForm: createCapabilityForm(),
  relationQueryParams: {
    pageNum: 1,
    pageSize: 10,
    capabilityCode: undefined,
    modelCode: undefined,
    relationType: undefined,
    status: undefined
  },
  relationForm: createRelationForm(),
  snapshotQuery: {
    capabilityCode: undefined,
    configType: undefined,
    providerCode: undefined,
    operation: undefined
  },
  logQueryParams: {
    pageNum: 1,
    pageSize: 10,
    capabilityCode: undefined,
    configType: undefined,
    providerCode: undefined,
    status: undefined
  },
  capabilityRules: {
    capabilityCode: [{ required: true, message: '能力编码不能为空', trigger: 'blur' }],
    capabilityName: [{ required: true, message: '能力名称不能为空', trigger: 'blur' }],
    configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
    providerCode: [{ required: true, message: 'Provider 不能为空', trigger: 'blur' }],
    operation: [{ required: true, message: 'Operation 不能为空', trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  },
  relationRules: {
    capabilityId: [{ required: true, message: '请选择源能力', trigger: 'change' }],
    targetCapabilityId: [{ required: true, message: '请选择目标能力', trigger: 'change' }],
    relationType: [{ required: true, message: '请选择关系类型', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  }
})

const {
  queryParams,
  capabilityForm,
  relationQueryParams,
  relationForm,
  snapshotQuery,
  logQueryParams,
  capabilityRules,
  relationRules
} = toRefs(data)

const capabilityDialogTitle = computed(() => (capabilityForm.value.capabilityId ? '修改能力' : '新增能力'))
const relationDialogTitle = computed(() => (relationForm.value.relationId ? '修改关系' : '新增关系'))
const relationCapabilityCode = computed(() => normalizeText(relationQueryParams.value.capabilityCode))
const relationRuntimePublished = computed(() => isPublishedSnapshot(relationRuntimeSnapshot.value))
const relationCapabilityMap = computed(() => {
  const map = new Map()
  relationCapabilityCandidates.value.forEach(item => {
    if (item?.capabilityId) {
      map.set(String(item.capabilityId), item)
    }
  })
  return map
})
const relationRuntimeMismatch = computed(() => {
  if (!relationRuntimePublished.value || !relationDefaultCapability.value) {
    return false
  }
  return !sameRuntimeIdentity(relationRuntimeSnapshot.value, relationDefaultCapability.value, true)
})

function configTypeLabel(value) {
  return configTypeOptions.find(item => item.value === value)?.label || value || '-'
}

function capabilityLayerLabel(value) {
  return capabilityLayerOptions.find(item => item.value === value)?.label || value || '-'
}

function relationTypeLabel(value) {
  return relationTypeOptions.find(item => item.value === value)?.label || value || '-'
}

function relationTypeTag(value) {
  const map = {
    same_business: 'success',
    replaceable: 'primary',
    primary_backup: 'warning',
    ab_test: 'info',
    mutual_exclusive: 'danger'
  }
  return map[value] || 'info'
}

function normalizeText(value) {
  return value === undefined || value === null ? '' : String(value).trim()
}

function compareText(value) {
  return normalizeText(value).toLowerCase()
}

function isPublishedSnapshot(snapshot) {
  return !!snapshot && snapshot.runtimeSource !== 'not-found'
}

function sameRuntimeIdentity(left, right, compareOperation = false) {
  if (!left || !right) {
    return false
  }
  const providerSame = compareText(left.providerCode) === compareText(right.providerCode)
  const modelSame = compareText(left.modelCode) === compareText(right.modelCode)
  const operationSame = !compareOperation || compareText(left.operation) === compareText(right.operation)
  return providerSame && modelSame && operationSame
}

function identityMatches(left, right) {
  if (!left || !right) {
    return false
  }
  if (right.runtimeSource && !isPublishedSnapshot(right)) {
    return false
  }
  const leftCapability = compareText(left.capabilityCode)
  const rightCapability = compareText(right.capabilityCode)
  if (leftCapability && rightCapability && leftCapability !== rightCapability) {
    return false
  }
  const leftProvider = compareText(left.providerCode)
  const rightProvider = compareText(right.providerCode)
  const leftModel = compareText(left.modelCode)
  const rightModel = compareText(right.modelCode)
  if (!leftProvider || !rightProvider || !leftModel || !rightModel) {
    return false
  }
  if (leftProvider !== rightProvider || leftModel !== rightModel) {
    return false
  }
  const leftOperation = compareText(left.operation)
  const rightOperation = compareText(right.operation)
  return !leftOperation || !rightOperation || leftOperation === rightOperation
}

function isDefault(row) {
  return row?.isDefault === '1' || row?.isDefault === 'Y'
}

function isPendingDefaultCapability(row) {
  return isDefault(row) && row?.status === '0'
}

function capabilityRowClassName({ row }) {
  return isPendingDefaultCapability(row) ? 'pending-default-row' : ''
}

function isRowConfigured(row) {
  return hasConfiguredProvider(row, configuredProviderSet.value)
}

function isRelationConfigured(row) {
  return hasConfiguredTargetProvider(row, configuredProviderSet.value)
}

function relationSourceCapability(row) {
  return row?.capabilityId ? relationCapabilityMap.value.get(String(row.capabilityId)) : null
}

function relationTargetIdentity(row) {
  return {
    capabilityCode: row?.targetCapabilityCode || row?.capabilityCode,
    providerCode: row?.targetProviderCode || row?.providerCode,
    modelCode: row?.modelCode,
    operation: row?.operation
  }
}

function isRelationSourcePublished(row) {
  return identityMatches(relationSourceCapability(row), relationRuntimeSnapshot.value)
}

function isRelationSourceDefault(row) {
  const source = relationSourceCapability(row)
  return !!source && !!relationDefaultCapability.value
    && String(source.capabilityId) === String(relationDefaultCapability.value.capabilityId)
}

function isRelationTargetPublished(row) {
  return identityMatches(relationTargetIdentity(row), relationRuntimeSnapshot.value)
}

function isRelationTargetDefault(row) {
  if (row?.targetCapabilityId && relationDefaultCapability.value?.capabilityId
    && String(row.targetCapabilityId) === String(relationDefaultCapability.value.capabilityId)) {
    return true
  }
  return identityMatches(relationTargetIdentity(row), relationDefaultCapability.value)
}

function relationSourceSubtitle(row) {
  const source = relationSourceCapability(row)
  if (source) {
    return `${source.capabilityCode || '-'} / ${source.providerCode || '-'} / ${source.modelCode || '-'}`
  }
  return `${row?.capabilityCode || '-'} / ${row?.providerCode || '-'}`
}

function relationTargetSubtitle(row) {
  const target = relationTargetIdentity(row)
  return `${target.capabilityCode || '-'} / ${target.providerCode || '-'} / ${target.modelCode || '-'}`
}

function candidateLabel(item) {
  const suffix = showUnconfigured.value && !isRowConfigured(item) ? ' / 未配置' : ''
  return `${item.candidateDisplayName || item.capabilityName || item.capabilityCode}${suffix}`
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
    configuredProviderError.value = '启用 Provider 配置加载失败，候选能力和关系已默认隐藏；请检查 Provider 配置列表接口。'
  })
}

function applyCapabilityVisibility() {
  const visibleRows = filterRowsByConfiguredProvider(allCapabilityList.value, configuredProviderSet.value, showUnconfigured.value)
  hiddenCapabilityCount.value = countUnconfiguredProviderRows(allCapabilityList.value, configuredProviderSet.value)
  total.value = visibleRows.length
  capabilityList.value = localPageRows(visibleRows, queryParams.value.pageNum, queryParams.value.pageSize)
}

function applyCandidateVisibility() {
  candidateOptions.value = filterRowsByConfiguredProvider(allCandidateOptions.value, configuredProviderSet.value, showUnconfigured.value)
}

function applyRelationVisibility() {
  const visibleRows = filterRowsByConfiguredTargetProvider(allRelationList.value, configuredProviderSet.value, showUnconfigured.value)
  hiddenRelationCount.value = countUnconfiguredTargetProviderRows(allRelationList.value, configuredProviderSet.value)
  relationTotal.value = visibleRows.length
  relationList.value = localPageRows(visibleRows, relationQueryParams.value.pageNum, relationQueryParams.value.pageSize)
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
    listProviderCapability(requestParams)
  ]).then(([, response]) => {
    allCapabilityList.value = response.rows || []
    applyCapabilityVisibility()
  }).finally(() => {
    loading.value = false
  })
}

function handleVisibilityChange() {
  queryParams.value.pageNum = 1
  relationQueryParams.value.pageNum = 1
  applyCapabilityVisibility()
  applyCandidateVisibility()
  applyRelationVisibility()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
  loadCandidates()
}

function resetQuery() {
  proxy?.resetForm?.('queryRef')
  Object.assign(queryParams.value, createQueryParams())
  handleQuery()
}

function handleSelectionChange(selection) {
  selectedRows.value = selection || []
  ids.value = selectedRows.value.map(item => item.capabilityId)
  single.value = selectedRows.value.length !== 1
  multiple.value = selectedRows.value.length === 0
}

function resetCapabilityForm() {
  Object.assign(capabilityForm.value, createCapabilityForm())
  proxy?.resetForm?.('capabilityRef')
}

function handleAddCapability() {
  resetCapabilityForm()
  capabilityDialogOpen.value = true
}

function handleUpdateCapability(row) {
  const capabilityId = row?.capabilityId || ids.value[0]
  if (!capabilityId) {
    proxy.$modal.msgWarning('请先选择一条能力记录')
    return
  }
  resetCapabilityForm()
  getProviderCapability(capabilityId).then(response => {
    Object.assign(capabilityForm.value, response.data || {})
    capabilityDialogOpen.value = true
  })
}

function submitCapabilityForm() {
  proxy.$refs.capabilityRef.validate(valid => {
    if (!valid) {
      return
    }
    const request = capabilityForm.value.capabilityId ? updateProviderCapability : addProviderCapability
    request(capabilityForm.value).then(() => {
      proxy.$modal.msgSuccess(capabilityForm.value.capabilityId ? '修改成功' : '新增成功')
      capabilityDialogOpen.value = false
      getList()
      loadCandidates()
    })
  })
}

function handleCapabilityStatusChange(row) {
  changeProviderCapabilityStatus(row.capabilityId, row.status).then(() => {
    proxy.$modal.msgSuccess(row.status === '0' ? '已启用' : '已停用')
    getList()
  }).catch(() => {
    row.status = row.status === '0' ? '1' : '0'
  })
}

function handleDeleteCapability(row) {
  const capabilityIds = row?.capabilityId || ids.value
  proxy.$modal.confirm(`确认删除能力编号为 ${capabilityIds} 的数据吗？`).then(() => {
    return delProviderCapability(capabilityIds)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadCandidates()
  }).catch(() => {})
}

function handleSetDefault(row) {
  const current = row || selectedRows.value[0]
  if (!current) {
    proxy.$modal.msgWarning('请先选择一条能力记录')
    return
  }
  if (current.status !== '0') {
    proxy.$modal.msgWarning('停用能力不能设为默认')
    return
  }
  if (isDefault(current)) {
    proxy.$modal.msgInfo('当前能力已经是默认项')
    return
  }
  proxy.$modal.confirm(`确认将 ${current.capabilityCode} 切换到 ${current.providerCode} / ${current.modelCode || '-'} 吗？`).then(() => {
    return setDefaultProviderCapability(current.capabilityId)
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || '默认能力已切换')
    getList()
  }).catch(() => {})
}

function loadCandidates() {
  return Promise.all([
    loadConfiguredProviders(),
    listCapabilityCandidates({ status: '0' })
  ]).then(([, response]) => {
    allCandidateOptions.value = response.data || []
    applyCandidateVisibility()
  }).catch(() => {
    allCandidateOptions.value = []
    candidateOptions.value = []
  })
}

function getRelations() {
  relationLoading.value = true
  const requestParams = {
    ...relationQueryParams.value,
    pageNum: 1,
    pageSize: CONFIGURED_PROVIDER_FETCH_SIZE
  }
  Promise.all([
    loadConfiguredProviders(),
    listCapabilityRelations(requestParams)
  ]).then(([, response]) => {
    allRelationList.value = response.rows || []
    applyRelationVisibility()
  }).finally(() => {
    relationLoading.value = false
  })
}

function handleRelationQuery() {
  relationQueryParams.value.pageNum = 1
  getRelations()
  loadRelationRuntimeSummary()
}

function resetRelationQuery() {
  proxy?.resetForm?.('relationQueryRef')
  relationQueryParams.value.pageNum = 1
  relationQueryParams.value.capabilityCode = undefined
  relationQueryParams.value.modelCode = undefined
  relationQueryParams.value.relationType = undefined
  relationQueryParams.value.status = undefined
  resetRelationRuntimeSummary()
  handleRelationQuery()
}

function refreshRelations() {
  loadCandidates()
  getRelations()
  loadRelationRuntimeSummary()
}

function resetRelationRuntimeSummary() {
  relationRuntimeSnapshot.value = null
  relationDefaultCapability.value = null
  relationCapabilityCandidates.value = []
}

function loadRelationRuntimeSummary() {
  const capabilityCode = relationCapabilityCode.value
  if (!capabilityCode) {
    resetRelationRuntimeSummary()
    return Promise.resolve()
  }
  relationRuntimeLoading.value = true
  return Promise.allSettled([
    getProviderRuntimeSnapshot({ capabilityCode }),
    listProviderCapability({
      pageNum: 1,
      pageSize: CONFIGURED_PROVIDER_FETCH_SIZE,
      capabilityCode,
      status: '0'
    })
  ]).then(results => {
    const snapshotResponse = results[0].status === 'fulfilled' ? results[0].value : null
    const capabilityResponse = results[1].status === 'fulfilled' ? results[1].value : null
    relationRuntimeSnapshot.value = snapshotResponse?.data || {
      capabilityCode,
      runtimeSource: 'not-found'
    }
    const rows = (capabilityResponse?.rows || [])
      .filter(item => compareText(item.capabilityCode) === compareText(capabilityCode))
    relationCapabilityCandidates.value = rows
    relationDefaultCapability.value = rows.find(item => isDefault(item) && item.status === '0') || null
  }).finally(() => {
    relationRuntimeLoading.value = false
  })
}

function publishRelationRuntimeSnapshot() {
  const capabilityCode = relationCapabilityCode.value
  if (!capabilityCode) {
    proxy.$modal.msgWarning('请先输入能力编码')
    return
  }
  refreshProviderRuntimeCache({ capabilityCode }).then(response => {
    proxy.$modal.msgSuccess(response.msg || '运行时快照已发布')
    loadRelationRuntimeSummary()
    if (activeTab.value === 'snapshot') {
      loadSnapshotList()
    }
  })
}

function resetRelationForm() {
  Object.assign(relationForm.value, createRelationForm())
  relationCompareResult.value = null
  proxy?.resetForm?.('relationRef')
}

function openRelations(row) {
  activeTab.value = 'relation'
  relationQueryParams.value.capabilityCode = row.capabilityCode
  relationQueryParams.value.pageNum = 1
  relationSourceCapabilityId.value = row.capabilityId
  loadCandidates()
  getRelations()
  loadRelationRuntimeSummary()
}

function handleAddRelation() {
  resetRelationForm()
  if (relationSourceCapabilityId.value) {
    relationForm.value.capabilityId = relationSourceCapabilityId.value
  } else if (selectedRows.value[0]) {
    relationForm.value.capabilityId = selectedRows.value[0].capabilityId
  }
  relationDialogOpen.value = true
  loadCandidates()
}

function handleEditRelation(row) {
  resetRelationForm()
  getCapabilityRelation(row.relationId).then(response => {
    Object.assign(relationForm.value, response.data || {})
    relationDialogOpen.value = true
    loadCandidates()
    refreshRelationCompare(false)
  })
}

function refreshRelationCompare(applySuggestion = true) {
  const leftCapabilityId = relationForm.value.capabilityId
  const rightCapabilityId = relationForm.value.targetCapabilityId
  relationCompareResult.value = null
  if (!leftCapabilityId || !rightCapabilityId || leftCapabilityId === rightCapabilityId) {
    return
  }
  relationCompareLoading.value = true
  compareCapabilityModels({
    leftCapabilityId,
    rightCapabilityId
  }).then(response => {
    relationCompareResult.value = response.data || null
    if (applySuggestion && relationCompareResult.value) {
      if (relationCompareResult.value.relationTypeSuggestion) {
        relationForm.value.relationType = relationCompareResult.value.relationTypeSuggestion
      }
      relationForm.value.sameBusinessFlag = relationCompareResult.value.sameBusinessFlag
      if (!relationForm.value.relationReason && relationCompareResult.value.comparisonSummary) {
        relationForm.value.relationReason = relationCompareResult.value.comparisonSummary
      }
    }
  }).catch(() => {
    relationCompareResult.value = null
  }).finally(() => {
    relationCompareLoading.value = false
  })
}

function submitRelationForm() {
  proxy.$refs.relationRef.validate(valid => {
    if (!valid) {
      return
    }
    if (relationForm.value.capabilityId === relationForm.value.targetCapabilityId) {
      proxy.$modal.msgWarning('源能力和目标能力不能相同')
      return
    }
    const request = relationForm.value.relationId ? updateCapabilityRelation : addCapabilityRelation
    request(relationForm.value).then(() => {
      proxy.$modal.msgSuccess(relationForm.value.relationId ? '关系已更新' : '关系已新增')
      relationDialogOpen.value = false
      getRelations()
    })
  })
}

function handleDeleteRelation(row) {
  proxy.$modal.confirm(`确认删除关系编号为 ${row.relationId} 的数据吗？`).then(() => {
    return delCapabilityRelation(row.relationId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getRelations()
  }).catch(() => {})
}

function handleSwitchRelation(row) {
  if (!row.targetCapabilityId) {
    proxy.$modal.msgWarning('当前关系无法定位目标能力')
    return
  }
  proxy.$modal.confirm(`确认将 ${row.capabilityCode} 切换到模型 ${row.modelCode || '-'} 吗？`).then(() => {
    return switchCapabilityDefaultModel({
      sourceCapabilityId: row.capabilityId,
      targetCapabilityId: row.targetCapabilityId,
      reason: row.relationReason
    })
  }).then(response => {
    proxy.$modal.msgSuccess(response.msg || '默认模型已切换')
    getList()
    getRelations()
    loadRelationRuntimeSummary()
  }).catch(() => {})
}

function openSnapshot(row) {
  activeTab.value = 'snapshot'
  snapshotQuery.value.capabilityCode = row.capabilityCode
  snapshotQuery.value.configType = row.configType
  snapshotQuery.value.providerCode = row.providerCode
  snapshotQuery.value.operation = row.operation
  loadSnapshotList()
  querySnapshot()
}

function querySnapshot() {
  return getProviderRuntimeSnapshot(snapshotQuery.value).then(response => {
    snapshotResult.value = response.data || null
  })
}

function loadSnapshotList() {
  snapshotLoading.value = true
  return listProviderRuntimeSnapshots(snapshotQuery.value).then(response => {
    snapshotList.value = response.data || []
  }).finally(() => {
    snapshotLoading.value = false
  })
}

function resetSnapshotQuery() {
  proxy?.resetForm?.('snapshotQueryRef')
  snapshotQuery.value.capabilityCode = undefined
  snapshotQuery.value.configType = undefined
  snapshotQuery.value.providerCode = undefined
  snapshotQuery.value.operation = undefined
  snapshotResult.value = null
  loadSnapshotList()
}

function handleSnapshotRowClick(row) {
  snapshotQuery.value.capabilityCode = row.capabilityCode
  snapshotQuery.value.configType = row.configType
  snapshotQuery.value.providerCode = row.providerCode
  snapshotQuery.value.operation = row.operation
  snapshotResult.value = row
}

function snapshotSourceTagType(source) {
  if (source === 'not-found') {
    return 'danger'
  }
  return source === 'redis-capability' ? 'success' : 'primary'
}

function snapshotSourceLabel(source) {
  const map = {
    'redis-capability': '能力快照',
    'redis-provider': 'Provider 快照',
    'redis-runtime': '运行时快照',
    'not-found': '未发布快照'
  }
  return map[source] || source || '-'
}

function hasSnapshotQuery() {
  return Boolean(snapshotQuery.value.capabilityCode
    || snapshotQuery.value.configType
    || snapshotQuery.value.providerCode
    || snapshotQuery.value.operation)
}

function handleRefreshRuntime(row) {
  const params = {}
  const source = row && row.value ? row.value : row
  if (source?.capabilityCode) {
    params.capabilityCode = source.capabilityCode
  } else if (queryParams.value.configType) {
    params.configType = queryParams.value.configType
  } else if (snapshotQuery.value.configType) {
    params.configType = snapshotQuery.value.configType
  }
  refreshProviderRuntimeCache(params).then(response => {
    proxy.$modal.msgSuccess(response.msg || '运行时快照已刷新')
    if (activeTab.value === 'snapshot') {
      loadSnapshotList()
      if (hasSnapshotQuery()) {
        querySnapshot()
      } else {
        snapshotResult.value = null
      }
    }
  })
}

function getLogs() {
  logLoading.value = true
  listProviderCapabilityLogs(logQueryParams.value).then(response => {
    logList.value = response.rows || []
    logTotal.value = response.total || 0
  }).finally(() => {
    logLoading.value = false
  })
}

function openLogs(row) {
  activeTab.value = 'logs'
  logQueryParams.value.pageNum = 1
  logQueryParams.value.capabilityCode = row.capabilityCode
  logQueryParams.value.configType = row.configType
  logQueryParams.value.providerCode = row.providerCode
  getLogs()
}

function handleLogQuery() {
  logQueryParams.value.pageNum = 1
  getLogs()
}

function resetLogQuery() {
  proxy?.resetForm?.('logQueryRef')
  logQueryParams.value.pageNum = 1
  logQueryParams.value.capabilityCode = undefined
  logQueryParams.value.configType = undefined
  logQueryParams.value.providerCode = undefined
  logQueryParams.value.status = undefined
  getLogs()
}

function refreshCurrent() {
  if (activeTab.value === 'relation') {
    refreshRelations()
    return
  }
  if (activeTab.value === 'snapshot') {
    loadSnapshotList()
    if (hasSnapshotQuery()) {
      querySnapshot()
    } else {
      snapshotResult.value = null
    }
    return
  }
  if (activeTab.value === 'logs') {
    getLogs()
    return
  }
  getList()
}

watch(activeTab, value => {
  if (value === 'relation' && relationList.value.length === 0) {
    refreshRelations()
  }
  if (value === 'relation' && relationCapabilityCode.value && !relationRuntimeSnapshot.value) {
    loadRelationRuntimeSummary()
  }
  if (value === 'logs' && logList.value.length === 0) {
    getLogs()
  }
  if (value === 'snapshot' && snapshotList.value.length === 0) {
    loadSnapshotList()
  }
})

watch(
  () => [relationForm.value.capabilityId, relationForm.value.targetCapabilityId],
  () => {
    if (relationDialogOpen.value) {
      refreshRelationCompare(true)
    }
  }
)

onMounted(() => {
  getList()
  loadCandidates()
})
</script>

<style scoped>
.provider-capability-page {
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

.cell-title {
  font-size: 13px;
  color: var(--el-text-color-primary);
  line-height: 20px;
}

.cell-subtitle {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 18px;
}

.relation-runtime-panel {
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  padding: 12px 14px;
  margin-bottom: 12px;
  background: var(--el-fill-color-blank);
}

.relation-runtime-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.relation-runtime-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 22px;
}

.relation-runtime-subtitle {
  margin-top: 2px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.relation-runtime-alert {
  margin-bottom: 10px;
}

.relation-runtime-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.relation-runtime-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 10px 12px;
  min-width: 0;
}

.relation-runtime-current {
  border-left: 3px solid var(--el-color-success);
}

.relation-runtime-default {
  border-left: 3px solid var(--el-color-warning);
}

.relation-runtime-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 18px;
}

.relation-runtime-main {
  margin-top: 4px;
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  line-height: 22px;
  word-break: break-all;
}

.relation-runtime-empty {
  color: var(--el-color-danger);
}

.relation-runtime-meta {
  margin-top: 4px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 18px;
}

.meta-divider {
  margin: 0 6px;
  color: var(--el-border-color);
}

@media (max-width: 960px) {
  .relation-runtime-head {
    flex-direction: column;
  }

  .relation-runtime-grid {
    grid-template-columns: 1fr;
  }
}

:deep(.pending-default-row > .el-table__cell) {
  background-color: #fdf6ec !important;
}

:deep(.pending-default-row:hover > .el-table__cell) {
  background-color: #faecd8 !important;
}

.snapshot-box {
  margin-top: 12px;
}

.support-warning-tag {
  margin-left: 4px;
}

.relation-compare-alert {
  margin-bottom: 14px;
}

.relation-compare-body {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 8px;
}

.relation-compare-detail {
  margin-top: 8px;
  color: var(--el-text-color-secondary);
  line-height: 20px;
}
</style>
