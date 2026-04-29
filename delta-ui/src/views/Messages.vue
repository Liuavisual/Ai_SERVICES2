<!--
  消息记录页面，查看客户对话历史
  支持虚拟滚动模式（el-table-v2），大数据量场景下可切换以提升性能

  @author delta
-->
<template>
  <div class="page-container">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="平台">
          <el-select
            id="messages-platform"
            v-model="queryParams.platform"
            placeholder="请选择平台"
            clearable
            style="width: 180px"
            :teleported="false"
            name="platform"
          >
            <el-option label="微信" value="wechat" />
            <el-option label="KOOK" value="kook" />
            <el-option label="YY" value="yy" />
          </el-select>
        </el-form-item>
        <el-form-item label="方向">
          <el-select
            id="messages-direction"
            v-model="queryParams.direction"
            placeholder="请选择方向"
            clearable
            style="width: 180px"
            :teleported="false"
            name="direction"
          >
            <el-option label="接收" value="in" />
            <el-option label="发送" value="out" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI发送">
          <el-select
            id="messages-ai"
            v-model="queryParams.ai"
            placeholder="请选择"
            clearable
            style="width: 180px"
            :teleported="false"
            name="ai"
          >
            <el-option label="是" value="true" />
            <el-option label="否" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发关键词">
          <el-select
            id="messages-keywordTriggered"
            v-model="queryParams.keywordTriggered"
            placeholder="请选择"
            clearable
            style="width: 180px"
            :teleported="false"
            name="keywordTriggered"
          >
            <el-option label="是" value="true" />
            <el-option label="否" value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            id="messages-keyword"
            v-model="queryParams.keyword"
            placeholder="请输入关键词"
            clearable
            name="keyword"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <!-- 虚拟滚动模式切换按钮 -->
      <div class="table-toolbar">
        <el-switch
          v-model="virtualMode"
          active-text="虚拟滚动"
          inactive-text="普通模式"
          :disabled="loading"
        />
        <el-alert
          v-if="total > 100 && !virtualMode"
          title="数据量较大，建议开启虚拟滚动模式以提升性能"
          type="info"
          :closable="false"
          show-icon
          class="virtual-hint"
        />
      </div>

      <!-- 普通表格模式 -->
      <el-table v-if="!virtualMode" :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userNickname" label="用户昵称" width="150" />
        <el-table-column prop="userPlatform" label="平台" width="100">
          <template #default="{ row }">
            <el-tag :type="getPlatformTagType(row.userPlatform)">{{ getPlatformText(row.userPlatform) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="direction" label="方向" width="100">
          <template #default="{ row }">
            <el-tag :type="row.direction === 'in' ? 'primary' : 'success'">
              {{ row.direction === 'in' ? '接收' : '发送' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="消息内容" show-overflow-tooltip min-width="200" />
        <el-table-column prop="ai" label="AI发送" width="100">
          <template #default="{ row }">
            <el-tag :type="row.ai ? 'primary' : 'info'">{{ row.ai ? 'AI' : '人工' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="keywordTriggered" label="触发关键词" width="120">
          <template #default="{ row }">
            <el-tag :type="row.keywordTriggered ? 'warning' : 'info'">
              {{ row.keywordTriggered ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewCustomer(row.userId)">查看客户</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 虚拟滚动表格模式 -->
      <div v-else class="virtual-table-wrapper" v-loading="loading">
        <el-auto-resizer>
          <template #default="{ height, width }">
            <el-table-v2
              :columns="v2Columns"
              :data="tableData"
              :width="width"
              :height="virtualTableHeight"
              :row-height="50"
              fixed
            />
          </template>
        </el-auto-resizer>
      </div>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
      <!-- 数据量较大时的提示信息 -->
      <el-alert
        v-if="total > 100"
        title="数据量较大，请使用筛选条件缩小范围"
        type="warning"
        :closable="false"
        show-icon
        class="data-hint"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { messageApi } from '@/api'
import { ElMessage, ElTag, ElButton } from 'element-plus'
import { useRouter } from 'vue-router'
import type { Result, PageResult, MessageVO } from '@/types'

const router = useRouter()
const loading = ref<boolean>(false)
const tableData = ref<MessageVO[]>([])
const total = ref<number>(0)
const virtualMode = ref<boolean>(false)
const virtualTableHeight = ref<number>(500)

const queryParams = reactive<{
  pageNum: number
  pageSize: number
  userId: string | null
  platform: string | null
  direction: string | null
  ai: string | null
  keywordTriggered: string | null
  keyword: string
}>({
  pageNum: 1,
  pageSize: 20,
  userId: null,
  platform: null,
  direction: null,
  ai: null,
  keywordTriggered: null,
  keyword: ''
})

const getPlatformText = (platform: string): string => {
  const map: Record<string, string> = {
    'wechat': '微信',
    'kook': 'KOOK',
    'yy': 'YY'
  }
  return map[platform] || platform
}

const getPlatformTagType = (platform: string): string => {
  const map: Record<string, string> = {
    'wechat': 'primary',
    'kook': 'success',
    'yy': 'warning'
  }
  return map[platform] || 'info'
}

interface MessageRow extends MessageVO {
  ai: boolean
  keywordTriggered: boolean
  userPlatform: string
  direction: string
}

const v2Columns = computed(() => [
  { key: 'id', dataKey: 'id', title: 'ID', width: 80 },
  { key: 'userNickname', dataKey: 'userNickname', title: '用户昵称', width: 150 },
  {
    key: 'userPlatform',
    dataKey: 'userPlatform',
    title: '平台',
    width: 100,
    cellRenderer: ({ cellData }: { cellData: string }) => h(ElTag, { type: getPlatformTagType(cellData) }, () => getPlatformText(cellData))
  },
  {
    key: 'direction',
    dataKey: 'direction',
    title: '方向',
    width: 100,
    cellRenderer: ({ cellData }: { cellData: string }) => h(ElTag, { type: cellData === 'in' ? 'primary' : 'success' }, () => cellData === 'in' ? '接收' : '发送')
  },
  { key: 'content', dataKey: 'content', title: '消息内容', width: 200 },
  {
    key: 'ai',
    dataKey: 'ai',
    title: 'AI发送',
    width: 100,
    cellRenderer: ({ cellData }: { cellData: boolean }) => h(ElTag, { type: cellData ? 'primary' : 'info' }, () => cellData ? 'AI' : '人工')
  },
  {
    key: 'keywordTriggered',
    dataKey: 'keywordTriggered',
    title: '触发关键词',
    width: 120,
    cellRenderer: ({ cellData }: { cellData: boolean }) => h(ElTag, { type: cellData ? 'warning' : 'info' }, () => cellData ? '是' : '否')
  },
  { key: 'createdAt', dataKey: 'createdAt', title: '时间', width: 180 },
  {
    key: 'actions',
    dataKey: 'userId',
    title: '操作',
    width: 100,
    fixed: 'right',
    cellRenderer: ({ cellData: userId }: { cellData: string }) => h(ElButton, {
      size: 'small',
      type: 'primary',
      link: true,
      onClick: () => handleViewCustomer(userId)
    }, () => '查看客户')
  }
])

const handleQuery = async (): Promise<void> => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      ai: queryParams.ai === 'true' ? true : queryParams.ai === 'false' ? false : null,
      keywordTriggered: queryParams.keywordTriggered === 'true' ? true : queryParams.keywordTriggered === 'false' ? false : null
    }
    const res: Result<PageResult<MessageRow>> = await messageApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        ai: Boolean(item.ai),
        keywordTriggered: Boolean(item.keywordTriggered)
      }))
      total.value = res.data.total
      if (total.value > 100 && !virtualMode.value) {
        ElMessage.info('数据量较大，可开启虚拟滚动模式提升性能')
      }
    }
  } catch (error) {
    ElMessage.error('查询失败')
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

const handleReset = (): void => {
  queryParams.pageNum = 1
  queryParams.platform = null
  queryParams.direction = null
  queryParams.ai = null
  queryParams.keywordTriggered = null
  queryParams.keyword = ''
  handleQuery()
}

const handleViewCustomer = (userId: string): void => {
  router.push({
    path: '/customers',
    query: { keyword: '' }
  })
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}

/* 数据量提示样式 */
.data-hint {
  margin-top: 12px;
}

/* 表格工具栏样式 */
.table-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 12px;
}

/* 虚拟滚动提示样式 */
.virtual-hint {
  flex: 1;
  padding: 4px 8px;
}

/* 虚拟表格容器样式 */
.virtual-table-wrapper {
  height: 500px;
}
</style>
