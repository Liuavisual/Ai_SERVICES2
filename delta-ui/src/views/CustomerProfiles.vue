<!--
  客户画像页面 - 基于RFM模型和消费行为分析
  数据来源：仅店内消费记录 + 客服/陪玩交互数据，不涉及客户隐私信息

  @author delta
-->
<template>
  <div class="profile-page">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="RFM分群">
          <el-select v-model="queryForm.rfmSegment" placeholder="全部分群" clearable style="width: 140px" :teleported="false">
            <el-option label="重要价值" value="CHAMPION" />
            <el-option label="忠诚客户" value="LOYAL" />
            <el-option label="潜力客户" value="POTENTIAL" />
            <el-option label="新客户" value="NEW" />
            <el-option label="流失预警" value="AT_RISK" />
            <el-option label="休眠高价值" value="HIBERNATE" />
            <el-option label="流失客户" value="LOST" />
          </el-select>
        </el-form-item>
        <el-form-item label="生命周期">
          <el-select v-model="queryForm.lifecycleStage" placeholder="全部阶段" clearable style="width: 120px" :teleported="false">
            <el-option label="新客户" value="NEW" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="沉默" value="SILENT" />
            <el-option label="流失" value="CHURNED" />
            <el-option label="回流" value="REACTIVATED" />
          </el-select>
        </el-form-item>
        <el-form-item label="会员等级">
          <el-select v-model="queryForm.memberLevel" placeholder="全部等级" clearable style="width: 120px" :teleported="false">
            <el-option label="普通" value="NORMAL" />
            <el-option label="青铜" value="BRONZE" />
            <el-option label="白银" value="SILVER" />
            <el-option label="黄金" value="GOLD" />
            <el-option label="铂金" value="PLATINUM" />
            <el-option label="钻石" value="DIAMOND" />
          </el-select>
        </el-form-item>
        <el-form-item label="风险等级">
          <el-select v-model="queryForm.riskLevel" placeholder="全部" clearable style="width: 100px" :teleported="false">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="搜索">
          <el-input v-model="queryForm.keyword" placeholder="昵称/标签" clearable style="width: 140px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userId" label="ID" width="60" />
        <el-table-column label="客户" min-width="140">
          <template #default="{ row }">
            <div style="display:flex;align-items:center;gap:6px">
              <el-avatar :size="28" :src="row.avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
              <span>{{ row.nickname || '-' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="RFM分群" width="100">
          <template #default="{ row }">
            <el-tag :type="getRfmTagType(row.rfmSegment)" size="small">{{ getRfmLabel(row.rfmSegment) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生命周期" width="80">
          <template #default="{ row }">
            <el-tag :type="getLifecycleTagType(row.lifecycleStage)" size="small" effect="plain">{{ getLifecycleLabel(row.lifecycleStage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会员" width="70">
          <template #default="{ row }">
            <el-tag :type="getMemberTagType(row.memberLevel)" size="small">{{ getMemberLabel(row.memberLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="下单" width="60" />
        <el-table-column label="累计消费" width="90">
          <template #default="{ row }">¥{{ formatMoney(row.totalSpent) }}</template>
        </el-table-column>
        <el-table-column label="LTV" width="90">
          <template #default="{ row }">¥{{ formatMoney(row.estimatedLtv) }}</template>
        </el-table-column>
        <el-table-column label="需求" width="80">
          <template #default="{ row }">
            <el-tag :type="getNeedTagType(row.primaryNeedType)" size="small">{{ getNeedLabel(row.primaryNeedType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流失风险" width="80">
          <template #default="{ row }">
            <el-tag :type="getChurnTagType(row.churnRiskScore)" size="small">{{ row.churnRiskScore || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="趋势" width="70">
          <template #default="{ row }">
            <span v-if="row.spendingTrend === 'INCREASING'" style="color:var(--gu-success)">↑</span>
            <span v-else-if="row.spendingTrend === 'DECREASING'" style="color:var(--gu-danger)">↓</span>
            <span v-else style="color:var(--gu-text-muted)">→</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleViewProfile(row)">画像</el-button>
            <el-button link type="warning" size="small" @click="handleAddOrder(row)">下单</el-button>
            <el-button link size="small" @click="handleRefresh(row)">刷新</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryForm.pageNum"
        v-model:page-size="queryForm.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="() => { queryForm.pageNum = 1; handleQuery() }"
        @current-change="handleQuery"
      />
    </el-card>

    <el-dialog v-model="profileDialogVisible" title="客户画像详情" width="900px" top="3vh" :close-on-click-modal="false">
      <div v-if="currentProfile" class="profile-detail">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="profile-avatar-section">
              <el-avatar :size="72" :src="currentProfile.avatar">{{ currentProfile.nickname?.charAt(0) }}</el-avatar>
              <h3>{{ currentProfile.nickname }}</h3>
              <div class="tag-row">
                <el-tag :type="getMemberTagType(currentProfile.memberLevel)" size="small">{{ getMemberLabel(currentProfile.memberLevel) }}</el-tag>
                <el-tag :type="getLifecycleTagType(currentProfile.lifecycleStage)" size="small" effect="plain">{{ getLifecycleLabel(currentProfile.lifecycleStage) }}</el-tag>
              </div>
              <div class="tag-row">
                <el-tag size="small" :type="getPlatformTagType(currentProfile.platform)">{{ getPlatformLabel(currentProfile.platform) }}</el-tag>
              </div>
              <div v-if="currentProfile.assignedCsUserName" class="cs-info">
                专属客服：{{ currentProfile.assignedCsUserName }}
              </div>
              <div v-if="currentProfile.primaryNeedType" class="need-type-box">
                <div class="need-type-label">核心需求</div>
                <el-tag :type="getNeedTagType(currentProfile.primaryNeedType)">{{ getNeedLabel(currentProfile.primaryNeedType) }}</el-tag>
              </div>
              <div v-if="currentProfile.needTags" class="need-tags-box">
                <el-tag v-for="tag in currentProfile.needTags.split(',')" :key="tag" size="small" type="info" style="margin:2px">{{ tag.trim() }}</el-tag>
              </div>
            </div>
          </el-col>
          <el-col :span="18">
            <!-- RFM价值评估 -->
            <div class="section-title">RFM价值评估</div>
            <el-row :gutter="12" class="rfm-row">
              <el-col :span="8">
                <div class="rfm-card">
                  <div class="rfm-label">R 最近消费</div>
                  <div class="rfm-score">{{ currentProfile.rfmRecencyScore || 1 }}</div>
                  <el-progress :percentage="(currentProfile.rfmRecencyScore || 1) * 20" :stroke-width="6" :show-text="false" :color="getRfmColor(currentProfile.rfmRecencyScore)" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="rfm-card">
                  <div class="rfm-label">F 消费频率</div>
                  <div class="rfm-score">{{ currentProfile.rfmFrequencyScore || 1 }}</div>
                  <el-progress :percentage="(currentProfile.rfmFrequencyScore || 1) * 20" :stroke-width="6" :show-text="false" :color="getRfmColor(currentProfile.rfmFrequencyScore)" />
                </div>
              </el-col>
              <el-col :span="8">
                <div class="rfm-card">
                  <div class="rfm-label">M 消费金额</div>
                  <div class="rfm-score">{{ currentProfile.rfmMonetaryScore || 1 }}</div>
                  <el-progress :percentage="(currentProfile.rfmMonetaryScore || 1) * 20" :stroke-width="6" :show-text="false" :color="getRfmColor(currentProfile.rfmMonetaryScore)" />
                </div>
              </el-col>
            </el-row>
            <div class="rfm-summary">
              <span>综合评分：<b>{{ currentProfile.rfmTotalScore || 3 }}/15</b></span>
              <el-tag :type="getRfmTagType(currentProfile.rfmSegment)" size="small" style="margin-left:8px">{{ getRfmLabel(currentProfile.rfmSegment) }}</el-tag>
            </div>

            <!-- 消费行为 -->
            <div class="section-title">消费行为</div>
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="累计下单">{{ currentProfile.totalOrders }} 次</el-descriptions-item>
              <el-descriptions-item label="累计消费">¥{{ formatMoney(currentProfile.totalSpent) }}</el-descriptions-item>
              <el-descriptions-item label="平均客单价">¥{{ formatMoney(currentProfile.avgOrderAmount) }}</el-descriptions-item>
              <el-descriptions-item label="最高单笔">¥{{ formatMoney(currentProfile.maxOrderAmount) }}</el-descriptions-item>
              <el-descriptions-item label="消费趋势">
                <span v-if="currentProfile.spendingTrend === 'INCREASING'" style="color:var(--gu-success)">上升</span>
                <span v-else-if="currentProfile.spendingTrend === 'DECREASING'" style="color:var(--gu-danger)">下降</span>
                <span v-else>平稳</span>
              </el-descriptions-item>
              <el-descriptions-item label="复购率">{{ currentProfile.repurchaseRate != null ? (currentProfile.repurchaseRate * 100).toFixed(0) + '%' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="LTV估算">¥{{ formatMoney(currentProfile.estimatedLtv) }}</el-descriptions-item>
              <el-descriptions-item label="平均时长">{{ currentProfile.avgServiceDuration ? currentProfile.avgServiceDuration + 'h' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="最近下单">{{ currentProfile.lastOrderAt || '-' }}</el-descriptions-item>
            </el-descriptions>

            <!-- 服务偏好 -->
            <div class="section-title">服务偏好</div>
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="偏好游戏">{{ currentProfile.favoriteGameType || '-' }}</el-descriptions-item>
              <el-descriptions-item label="偏好时段">{{ currentProfile.preferredTimeSlot || '-' }}</el-descriptions-item>
              <el-descriptions-item label="偏好等级">{{ currentProfile.preferredCompanionLevel || '-' }}</el-descriptions-item>
              <el-descriptions-item label="偏好类型">{{ getOrderTypeLabel(currentProfile.preferredOrderType) }}</el-descriptions-item>
              <el-descriptions-item label="最爱陪玩">{{ currentProfile.favoriteCompanionName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="陪玩师多样性">{{ currentProfile.companionDiversity || 0 }} 人</el-descriptions-item>
            </el-descriptions>

            <!-- 交互行为 -->
            <div class="section-title">交互行为</div>
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="消息总数">{{ currentProfile.totalMessages }}</el-descriptions-item>
              <el-descriptions-item label="AI交互">{{ currentProfile.aiInteractionCount }} 次</el-descriptions-item>
              <el-descriptions-item label="人工交互">{{ currentProfile.manualInteractionCount }} 次</el-descriptions-item>
              <el-descriptions-item label="AI占比">{{ currentProfile.aiRatio != null ? (currentProfile.aiRatio * 100).toFixed(0) + '%' : '-' }}</el-descriptions-item>
              <el-descriptions-item label="转人工次数">{{ currentProfile.humanHandoffCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="主要转人工原因">{{ currentProfile.topHandoffReason || '-' }}</el-descriptions-item>
              <el-descriptions-item label="情绪触发">{{ currentProfile.emotionTriggerCount || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="下单意图">{{ currentProfile.orderIntentCount || 0 }} 次</el-descriptions-item>
              <el-descriptions-item label="活跃天数">{{ currentProfile.activeDays || 0 }} 天</el-descriptions-item>
            </el-descriptions>

            <!-- 满意度与风险 -->
            <div class="section-title">满意度与风险</div>
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="满意度">{{ currentProfile.satisfactionScore || '-' }}/5</el-descriptions-item>
              <el-descriptions-item label="满意度趋势">
                <span v-if="currentProfile.satisfactionTrend === 'IMPROVING'" style="color:var(--gu-success)">上升</span>
                <span v-else-if="currentProfile.satisfactionTrend === 'DECLINING'" style="color:var(--gu-danger)">下降</span>
                <span v-else>平稳</span>
              </el-descriptions-item>
              <el-descriptions-item label="平均评价">{{ currentProfile.avgRating || '-' }} 星</el-descriptions-item>
              <el-descriptions-item label="投诉次数">{{ currentProfile.complaintCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="退款次数">{{ currentProfile.refundCount || 0 }}</el-descriptions-item>
              <el-descriptions-item label="流失风险">
                <el-tag :type="getChurnTagType(currentProfile.churnRiskScore)" size="small">{{ currentProfile.churnRiskScore || 0 }}/10</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="currentProfile.riskLevel === 'HIGH' ? 'danger' : currentProfile.riskLevel === 'MEDIUM' ? 'warning' : 'success'" size="small">{{ currentProfile.riskLevel }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="首次接触">{{ currentProfile.firstContactAt || '-' }}</el-descriptions-item>
              <el-descriptions-item label="最近活跃">{{ currentProfile.lastActiveAt || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div v-if="currentProfile.tags" class="tags-section">
              <span class="tags-label">标签：</span>
              <el-tag v-for="tag in currentProfile.tags.split(',')" :key="tag" size="small" style="margin-right:4px">{{ tag.trim() }}</el-tag>
            </div>
            <div v-if="currentProfile.remark" class="remark-section">
              <span class="remark-label">备注：</span>{{ currentProfile.remark }}
            </div>
          </el-col>
        </el-row>

        <el-divider content-position="left">消费记录</el-divider>
        <el-table :data="orderRecords" stripe size="small" max-height="280">
          <el-table-column label="下单时间" width="160">
            <template #default="{ row }">{{ row.orderTime }}</template>
          </el-table-column>
          <el-table-column label="类型" width="80">
            <template #default="{ row }">{{ getOrderTypeLabel(row.orderType) }}</template>
          </el-table-column>
          <el-table-column prop="companionName" label="陪玩师" width="90" />
          <el-table-column label="金额" width="80">
            <template #default="{ row }">¥{{ row.amount?.toFixed(2) }}</template>
          </el-table-column>
          <el-table-column label="时长" width="60">
            <template #default="{ row }">{{ row.durationHours ? row.durationHours + 'h' : '-' }}</template>
          </el-table-column>
          <el-table-column label="评价" width="60">
            <template #default="{ row }">{{ row.rating ? row.rating + '星' : '-' }}</template>
          </el-table-column>
          <el-table-column prop="reviewContent" label="评价内容" min-width="120" show-overflow-tooltip />
          <el-table-column label="状态" width="70">
            <template #default="{ row }">
              <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'REFUNDED' ? 'danger' : 'warning'" size="small">{{ row.status === 'COMPLETED' ? '完成' : row.status === 'REFUNDED' ? '退款' : '取消' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <el-dialog v-model="orderDialogVisible" title="添加消费记录" width="550px">
      <el-form :model="orderForm" label-width="100px">
        <el-form-item label="订单类型" required>
          <el-select v-model="orderForm.orderType" placeholder="请选择" style="width: 100%" :teleported="false">
            <el-option label="陪玩" value="ACCOMPANY_PLAY" />
            <el-option label="包夜" value="NIGHT_PACKAGE" />
            <el-option label="指定游戏" value="SPECIFIC_GAME" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="陪玩师">
          <el-select v-model="orderForm.companionId" placeholder="请选择" clearable filterable style="width: 100%" :teleported="false">
            <el-option v-for="c in companionList" :key="c.id" :label="c.nickname" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="消费金额" required>
          <el-input-number v-model="orderForm.amount" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="服务时长">
          <el-input-number v-model="orderForm.durationHours" :min="0" :precision="1" :step="0.5" style="width: 100%" />
        </el-form-item>
        <el-form-item label="游戏类型">
          <el-input v-model="orderForm.gameType" placeholder="如：三角洲行动" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="orderForm.timeSlot" placeholder="请选择" clearable style="width: 100%" :teleported="false">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="晚上" value="晚上" />
            <el-option label="通宵" value="通宵" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户评价">
          <el-rate v-model="orderForm.rating" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input v-model="orderForm.reviewContent" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="orderDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmOrder">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { customerProfileApi, companionApi } from '@/api'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryForm = reactive({
  pageNum: 1,
  pageSize: 10,
  rfmSegment: null,
  lifecycleStage: null,
  memberLevel: null,
  riskLevel: null,
  keyword: ''
})

const profileDialogVisible = ref(false)
const currentProfile = ref(null)
const orderRecords = ref([])

const orderDialogVisible = ref(false)
const orderForm = reactive({
  userId: null,
  companionId: null,
  orderType: 'ACCOMPANY_PLAY',
  amount: 0,
  durationHours: null,
  gameType: '',
  timeSlot: null,
  rating: 0,
  reviewContent: '',
  remark: ''
})

const companionList = ref([])

const formatMoney = (val) => {
  if (val == null) return '0.00'
  return Number(val).toFixed(2)
}

const getMemberLabel = (level) => {
  const map = { NORMAL: '普通', BRONZE: '青铜', SILVER: '白银', GOLD: '黄金', PLATINUM: '铂金', DIAMOND: '钻石' }
  return map[level] || level
}

const getMemberTagType = (level) => {
  const map = { NORMAL: 'info', BRONZE: '', SILVER: '', GOLD: 'warning', PLATINUM: '', DIAMOND: 'danger' }
  return map[level] || 'info'
}

const getRfmLabel = (segment) => {
  const map = { CHAMPION: '重要价值', LOYAL: '忠诚', POTENTIAL: '潜力', NEW: '新客', AT_RISK: '流失预警', HIBERNATE: '休眠', LOST: '流失' }
  return map[segment] || segment
}

const getRfmTagType = (segment) => {
  const map = { CHAMPION: 'danger', LOYAL: 'warning', POTENTIAL: '', NEW: 'info', AT_RISK: 'warning', HIBERNATE: 'info', LOST: 'info' }
  return map[segment] || 'info'
}

const getRfmColor = (score) => {
  if (score >= 4) return 'var(--gu-success)'
  if (score >= 3) return 'var(--gu-warning)'
  return 'var(--gu-danger)'
}

const getLifecycleLabel = (stage) => {
  const map = { NEW: '新客', ACTIVE: '活跃', SILENT: '沉默', CHURNED: '流失', REACTIVATED: '回流' }
  return map[stage] || stage
}

const getLifecycleTagType = (stage) => {
  const map = { NEW: 'info', ACTIVE: 'success', SILENT: 'warning', CHURNED: 'danger', REACTIVATED: '' }
  return map[stage] || 'info'
}

const getNeedLabel = (type) => {
  const map = { EMOTIONAL: '情感', SKILL: '技能', SOCIAL: '社交', ENTERTAINMENT: '娱乐' }
  return map[type] || type
}

const getNeedTagType = (type) => {
  const map = { EMOTIONAL: 'danger', SKILL: 'warning', SOCIAL: 'success', ENTERTAINMENT: 'info' }
  return map[type] || 'info'
}

const getPlatformLabel = (platform) => {
  const map = { wechat: '微信', kook: 'KOOK', yy: 'YY' }
  return map[platform] || platform
}

const getPlatformTagType = (platform) => {
  const map = { wechat: 'primary', kook: 'success', yy: 'warning' }
  return map[platform] || 'info'
}

const getChurnTagType = (score) => {
  if (!score) return 'success'
  if (score >= 7) return 'danger'
  if (score >= 4) return 'warning'
  return 'success'
}

const getOrderTypeLabel = (type) => {
  const map = { ACCOMPANY_PLAY: '陪玩', NIGHT_PACKAGE: '包夜', SPECIFIC_GAME: '指定游戏', OTHER: '其他' }
  return map[type] || type
}

const handleQuery = async () => {
  loading.value = true
  try {
    const res = await customerProfileApi.getPage(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.pageNum = 1
  queryForm.rfmSegment = null
  queryForm.lifecycleStage = null
  queryForm.memberLevel = null
  queryForm.riskLevel = null
  queryForm.keyword = ''
  handleQuery()
}

const handleViewProfile = async (row) => {
  try {
    const res = await customerProfileApi.getByUserId(row.userId)
    if (res.code === 200) {
      currentProfile.value = res.data
      profileDialogVisible.value = true
      loadOrderRecords(row.userId)
    }
  } catch (error) {
    ElMessage.error('获取画像失败')
  }
}

const loadOrderRecords = async (userId) => {
  try {
    const res = await customerProfileApi.getOrderPage({ pageNum: 1, pageSize: 20, userId })
    if (res.code === 200) {
      orderRecords.value = res.data.records
    }
  } catch (error) {
    ElMessage.error('加载消费记录失败')
    console.error('加载消费记录失败', error)
  }
}

const handleAddOrder = (row) => {
  orderForm.userId = row.userId
  orderForm.companionId = null
  orderForm.orderType = 'ACCOMPANY_PLAY'
  orderForm.amount = 0
  orderForm.durationHours = null
  orderForm.gameType = ''
  orderForm.timeSlot = null
  orderForm.rating = 0
  orderForm.reviewContent = ''
  orderForm.remark = ''
  orderDialogVisible.value = true
}

const handleConfirmOrder = async () => {
  if (!orderForm.orderType || orderForm.amount <= 0) {
    ElMessage.warning('请填写订单类型和消费金额')
    return
  }
  try {
    const data = {
      ...orderForm,
      orderTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
      rating: orderForm.rating || null,
      status: 'COMPLETED'
    }
    const res = await customerProfileApi.addOrder(data)
    if (res.code === 200) {
      ElMessage.success('添加成功')
      orderDialogVisible.value = false
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const handleRefresh = async (row) => {
  try {
    const res = await customerProfileApi.refresh(row.userId)
    if (res.code === 200) {
      ElMessage.success('刷新成功')
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('刷新失败')
  }
}

const loadCompanions = async () => {
  try {
    const res = await companionApi.getAll()
    if (res.code === 200) {
      companionList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载陪玩师列表失败')
    console.error('加载陪玩师列表失败', error)
  }
}

onMounted(() => {
  handleQuery()
  loadCompanions()
})
</script>

<style scoped>
.filter-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 20px;
}

.profile-detail {
  padding: 0 10px;
}

.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 0;
}

.profile-avatar-section h3 {
  margin: 6px 0 4px;
  font-size: 16px;
}

.tag-row {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}

.cs-info {
  margin-top: 6px;
  font-size: 13px;
  color: var(--gu-text-secondary);
}

.need-type-box {
  margin-top: 12px;
  text-align: center;
}

.need-type-label {
  font-size: 12px;
  color: var(--gu-text-muted);
  margin-bottom: 4px;
}

.need-tags-box {
  margin-top: 6px;
  text-align: center;
}

.section-title {
  font-size: 14px;
  font-weight: bold;
  color: var(--gu-text-primary);
  margin: 16px 0 8px;
  padding-left: 8px;
  border-left: 3px solid var(--gu-accent);
}

.rfm-row {
  margin-bottom: 8px;
}

.rfm-card {
  text-align: center;
  padding: 10px;
  background: var(--gu-bg-stripe);
  border-radius: var(--gu-radius-lg);
}

.rfm-label {
  font-size: 12px;
  color: var(--gu-text-muted);
  margin-bottom: 4px;
}

.rfm-score {
  font-size: 24px;
  font-weight: bold;
  color: var(--gu-text-primary);
  margin-bottom: 6px;
}

.rfm-summary {
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--gu-text-secondary);
}

.tags-section {
  margin-top: 12px;
}

.tags-label {
  font-weight: bold;
  margin-right: 4px;
}

.remark-section {
  margin-top: 8px;
  font-size: 13px;
  color: var(--gu-text-secondary);
}

.remark-label {
  font-weight: bold;
}
</style>
