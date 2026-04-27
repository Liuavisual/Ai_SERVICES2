<!--
  对话测试页面，模拟客户对话测试AI客服回复效果

  @author delta
-->
<template>
  <div class="chat-test-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>通讯终端</span>
          <el-button type="danger" size="small" plain @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </div>
      </template>

      <div class="config-bar">
        <el-form :inline="true" :model="configForm" label-width="80px">
          <el-form-item label="代号">
            <el-input
              id="chatTest-customerNickname"
              v-model="configForm.customerNickname"
              placeholder="客户昵称"
              name="customerNickname"
            />
          </el-form-item>
          <el-form-item label="频道">
            <el-select
              id="chatTest-platform"
              v-model="configForm.platform"
              placeholder="选择平台"
              style="width: 160px"
              :teleported="false"
              name="platform"
            >
              <el-option label="微信" value="wechat" />
              <el-option label="KOOK" value="kook" />
              <el-option label="YY" value="yy" />
            </el-select>
          </el-form-item>
          <el-form-item label="指派">
            <el-select
              id="chatTest-csUserId"
              v-model="configForm.csUserId"
              placeholder="可选"
              clearable
              style="width: 160px"
              :teleported="false"
              name="csUserId"
            >
              <el-option v-for="cs in csUsers" :key="cs.id" :label="cs.realName" :value="cs.id" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <div class="quick-commands">
        <span class="quick-label">快捷指令</span>
        <button
          v-for="msg in quickMessages"
          :key="msg"
          class="quick-btn"
          @click="sendQuickMessage(msg)"
        >{{ msg }}</button>
      </div>

      <div class="chat-viewport" ref="messagesContainer">
        <div v-if="messages.length === 0 && !loading" class="empty-state">
          <svg viewBox="0 0 48 48" fill="none" width="40" height="40">
            <path d="M24 4C13 4 4 13 4 24s9 20 20 20 20-9 20-20S35 4 24 4z" stroke="#2a3a52" stroke-width="1.5" stroke-dasharray="3 3"/>
            <circle cx="18" cy="22" r="1.5" fill="#5a6b82"/>
            <circle cx="30" cy="22" r="1.5" fill="#5a6b82"/>
            <path d="M17 30c0 0 3 4 7 4s7-4 7-4" stroke="#5a6b82" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
          <p>等待建立连接...</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
          <div class="msg-bubble">
            <div class="msg-meta">
              <el-tag v-if="msg.isAi" type="info" size="small">AI</el-tag>
              <el-tag v-if="msg.keyword" type="warning" size="small">{{ msg.keyword }}</el-tag>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
            <div class="msg-text">{{ msg.content }}</div>
          </div>
        </div>

        <div v-if="loading" class="typing-indicator">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="typing-label">处理中</span>
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputContent"
          type="textarea"
          :rows="2"
          placeholder="输入消息，Enter 发送..."
          @keydown.enter.prevent="($event.shiftKey) || handleSend()"
          :disabled="loading"
        />
        <div class="input-actions">
          <el-button type="primary" @click="handleSend" :loading="loading">
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { chatTestApi, sysUserApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Refresh, Promotion } from '@element-plus/icons-vue'

const messagesContainer = ref(null)
const loading = ref(false)
const inputContent = ref('')
const messages = ref([])

const configForm = ref({
  customerNickname: '测试目标',
  platform: 'wechat',
  csUserId: null
})

const csUsers = ref([])
const quickMessages = ['你好', '预约', '价格', '陪玩', '人工']

const loadCsUsers = async () => {
  try {
    const res = await sysUserApi.getPage({ pageNum: 1, pageSize: 100, role: 'CS_STAFF' })
    if (res.code === 200) {
      csUsers.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载客服列表失败')
    console.error('加载客服列表失败', error)
  }
}

const loadHistory = () => {
  const history = localStorage.getItem('chatTestHistory')
  if (history) {
    try { messages.value = JSON.parse(history) } catch (e) {}
  }
}

const saveHistory = () => {
  localStorage.setItem('chatTestHistory', JSON.stringify(messages.value))
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendQuickMessage = (msg) => {
  inputContent.value = msg
  handleSend()
}

const handleSend = async () => {
  if (!inputContent.value.trim()) return

  const content = inputContent.value.trim()
  inputContent.value = ''

  messages.value.push({
    role: 'customer',
    content,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
    isAi: false,
    keyword: null
  })
  saveHistory()
  scrollToBottom()

  loading.value = true

  try {
    const res = await chatTestApi.send({
      customerNickname: configForm.value.customerNickname,
      platform: configForm.value.platform,
      csUserId: configForm.value.csUserId,
      content
    })

    if (res.code === 200) {
      const reply = res.data
      messages.value.push({
        role: 'service',
        content: reply.replyContent,
        time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', second: '2-digit' }),
        isAi: reply.isAiReply,
        keyword: reply.keywordTriggered ? reply.matchedKeyword : null
      })
      saveHistory()
      scrollToBottom()
    }
  } catch (error) {
    ElMessage.error('发送失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  messages.value = []
  localStorage.removeItem('chatTestHistory')
  ElMessage.success('已重置')
}

onMounted(() => {
  loadCsUsers()
  loadHistory()
  scrollToBottom()
})
</script>

<style scoped>
.chat-test-container {
  padding: 0;
}

.config-bar {
  padding: 14px;
  background: var(--gu-bg-secondary);
  border-radius: var(--gu-radius-lg);
  margin-bottom: 16px;
  border: 1px solid var(--gu-border);
}

.quick-commands {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.quick-label {
  font-size: 12px;
  color: var(--gu-text-muted);
  letter-spacing: 1px;
  margin-right: 4px;
}

.quick-btn {
  padding: 4px 12px;
  font-size: 12px;
  color: var(--gu-text-secondary);
  background: transparent;
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius);
  cursor: pointer;
  transition: all 0.15s ease;
}

.quick-btn:hover {
  border-color: var(--gu-accent);
  color: var(--gu-accent);
  background: var(--gu-accent-light);
}

.chat-viewport {
  height: 420px;
  overflow-y: auto;
  padding: 16px;
  background: var(--gu-bg);
  border-radius: var(--gu-radius-lg);
  border: 1px solid var(--gu-border);
  margin-bottom: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  gap: 10px;
  opacity: 0.4;
}

.empty-state p {
  font-size: 13px;
  color: var(--gu-text-muted);
}

.msg-row {
  display: flex;
  margin-bottom: 16px;
}

.msg-row.customer {
  justify-content: flex-start;
}

.msg-row.service {
  justify-content: flex-end;
}

.msg-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: var(--gu-radius-lg);
  position: relative;
}

.msg-row.customer .msg-bubble {
  background: var(--gu-bg-card);
  border: 1px solid var(--gu-border);
  border-bottom-left-radius: 2px;
}

.msg-row.service .msg-bubble {
  background: var(--gu-accent-light);
  border: 1px solid rgba(139, 58, 58, 0.2);
  border-bottom-right-radius: 2px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
}

.msg-time {
  font-size: 11px;
  color: var(--gu-text-muted);
  margin-left: auto;
}

.msg-text {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.65;
  font-size: 14px;
  color: var(--gu-text-primary);
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  color: var(--gu-text-muted);
  font-size: 12px;
}

.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--gu-accent);
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.typing-label {
  letter-spacing: 1px;
}

.input-area {
  border-top: 1px solid var(--gu-border);
  padding-top: 14px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}
</style>
