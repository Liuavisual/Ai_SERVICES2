<template>
  <div class="chat-test-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span>通讯终端</span>
            <el-tag type="info" size="small">AI Chat</el-tag>
          </div>
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
          <svg viewBox="0 0 48 48" fill="none" width="48" height="48">
            <rect x="4" y="8" width="40" height="28" rx="6" stroke="#94A3B8" stroke-width="1.5" fill="none"/>
            <path d="M16 20h16M16 26h10" stroke="#CBD5E1" stroke-width="1.5" stroke-linecap="round"/>
            <path d="M24 36l-4 4h8l-4-4z" fill="#E2E8F0"/>
          </svg>
          <p>等待建立连接...</p>
        </div>

        <div v-for="(msg, index) in messages" :key="index" :class="['msg-row', msg.role]">
          <div v-if="msg.role === 'customer'" class="msg-avatar customer-avatar">C</div>
          <div class="msg-bubble">
            <div class="msg-meta">
              <el-tag v-if="msg.isAi" type="info" size="small">AI</el-tag>
              <el-tag v-if="msg.keyword" type="warning" size="small">{{ msg.keyword }}</el-tag>
              <span class="msg-time">{{ msg.time }}</span>
            </div>
            <div class="msg-text">{{ msg.content }}</div>
          </div>
          <div v-if="msg.role === 'service'" class="msg-avatar service-avatar">S</div>
        </div>

        <div v-if="loading" class="typing-indicator">
          <div class="msg-avatar service-avatar">S</div>
          <div class="typing-bubble">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
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

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { chatTestApi, sysUserApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Refresh, Promotion } from '@element-plus/icons-vue'
import type { Result, PageResult, SysUserVO } from '@/types'

interface ChatMessage {
  role: 'customer' | 'service'
  content: string
  time: string
  isAi: boolean
  keyword: string | null
}

const messagesContainer = ref<HTMLDivElement | null>(null)
const loading = ref<boolean>(false)
const inputContent = ref<string>('')
const messages = ref<ChatMessage[]>([])

const configForm = ref<{
  customerNickname: string
  platform: string
  csUserId: string | null
}>({
  customerNickname: '测试目标',
  platform: 'wechat',
  csUserId: null
})

const csUsers = ref<SysUserVO[]>([])
const quickMessages: string[] = ['你好', '预约', '价格', '陪玩', '人工']

const loadCsUsers = async (): Promise<void> => {
  try {
    const res: Result<PageResult<SysUserVO>> = await sysUserApi.getPage({ page: 1, size: 100, role: 'CS_STAFF' })
    if (res.code === 200) {
      csUsers.value = res.data.records || []
    }
  } catch (error) {
    ElMessage.error('加载客服列表失败')
    console.error('加载客服列表失败', error)
  }
}

const loadHistory = (): void => {
  const history = localStorage.getItem('chatTestHistory')
  if (history) {
    try { messages.value = JSON.parse(history) } catch (e) {}
  }
}

const saveHistory = (): void => {
  localStorage.setItem('chatTestHistory', JSON.stringify(messages.value))
}

const scrollToBottom = (): void => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const sendQuickMessage = (msg: string): void => {
  inputContent.value = msg
  handleSend()
}

const handleSend = async (): Promise<void> => {
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
    const res: Result<any> = await chatTestApi.send({
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

const handleReset = (): void => {
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
  padding: 16px 20px;
  background: var(--gu-bg-stripe);
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
  font-weight: 500;
  margin-right: 4px;
}

.quick-btn {
  padding: 5px 14px;
  font-size: 12px;
  color: var(--gu-text-secondary);
  background: var(--gu-bg-card);
  border: 1px solid var(--gu-border);
  border-radius: var(--gu-radius-full);
  cursor: pointer;
  transition: all var(--gu-transition);
  font-weight: 500;
}

.quick-btn:hover {
  border-color: var(--gu-primary);
  color: var(--gu-primary);
  background: var(--gu-primary-light);
  box-shadow: var(--gu-shadow-primary);
}

.chat-viewport {
  height: 440px;
  overflow-y: auto;
  padding: 20px;
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
  gap: 12px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 13px;
  color: var(--gu-text-muted);
}

.msg-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
  gap: 10px;
}

.msg-row.customer {
  justify-content: flex-start;
}

.msg-row.service {
  justify-content: flex-end;
}

.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  flex-shrink: 0;
  font-family: var(--gu-font-heading);
}

.customer-avatar {
  background: var(--gu-bg-secondary);
  color: var(--gu-text-secondary);
  border: 1px solid var(--gu-border);
}

.service-avatar {
  background: linear-gradient(135deg, var(--gu-primary), var(--gu-secondary));
  color: white;
}

.msg-bubble {
  max-width: 65%;
  padding: 10px 14px;
  border-radius: var(--gu-radius-lg);
  position: relative;
}

.msg-row.customer .msg-bubble {
  background: var(--gu-bg-card);
  border: 1px solid var(--gu-border);
  border-bottom-left-radius: 4px;
}

.msg-row.service .msg-bubble {
  background: var(--gu-primary-light);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-bottom-right-radius: 4px;
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
  font-family: var(--gu-font-mono);
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
  gap: 10px;
  padding: 4px 0;
}

.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 16px;
  background: var(--gu-primary-light);
  border: 1px solid rgba(99, 102, 241, 0.15);
  border-radius: var(--gu-radius-lg);
  border-bottom-right-radius: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--gu-primary);
  animation: bounce 1.4s infinite ease-in-out both;
}

.dot:nth-child(1) { animation-delay: -0.32s; }
.dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
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
