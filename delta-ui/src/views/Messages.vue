<!--
  消息记录页面，查看客户对话历史

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
      <el-table :data="tableData" v-loading="loading" stripe>
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
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { messageApi } from '@/api'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 20,
  userId: null,
  platform: null,
  direction: null,
  ai: null,
  keywordTriggered: null,
  keyword: ''
})

const getPlatformText = (platform) => {
  const map = {
    'wechat': '微信',
    'kook': 'KOOK',
    'yy': 'YY'
  }
  return map[platform] || platform
}

const getPlatformTagType = (platform) => {
  const map = {
    'wechat': 'primary',
    'kook': 'success',
    'yy': 'warning'
  }
  return map[platform] || 'info'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      ai: queryParams.ai === 'true' ? true : queryParams.ai === 'false' ? false : null,
      keywordTriggered: queryParams.keywordTriggered === 'true' ? true : queryParams.keywordTriggered === 'false' ? false : null
    }
    const res = await messageApi.getPage(params)
    if (res.code === 200) {
      tableData.value = res.data.records.map(item => ({
        ...item,
        ai: Boolean(item.ai),
        keywordTriggered: Boolean(item.keywordTriggered)
      }))
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
    console.error('查询失败', error)
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.pageNum = 1
  queryParams.platform = null
  queryParams.direction = null
  queryParams.ai = null
  queryParams.keywordTriggered = null
  queryParams.keyword = ''
  handleQuery()
}

const handleViewCustomer = (userId) => {
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
</style>
