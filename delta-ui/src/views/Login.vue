<!--
  登录页面，支持用户名密码登录，古风印章风格设计

  @author delta
-->
<template>
  <div class="login-container">
    <div class="bg-pattern"></div>
    <div class="login-card">
      <div class="card-inner">
        <div class="login-header">
          <div class="seal">
            <svg viewBox="0 0 48 48" fill="none" width="44" height="44">
              <rect x="4" y="4" width="40" height="40" rx="4" stroke="#8B3A3A" stroke-width="2" fill="none"/>
              <path d="M14 24h20M24 14v20" stroke="#8B3A3A" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="title">三角洲行动</h1>
          <p class="subtitle">陪玩俱乐部 · 客服管理系统</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" name="username" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" name="password" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="0">
              <el-form-item prop="username"><el-input v-model="registerForm.username" placeholder="用户名（3-50位）" prefix-icon="User" size="large" /></el-form-item>
              <el-form-item prop="password"><el-input v-model="registerForm.password" type="password" placeholder="密码（至少8位）" prefix-icon="Lock" size="large" show-password /></el-form-item>
              <el-form-item prop="realName"><el-input v-model="registerForm.realName" placeholder="真实姓名" prefix-icon="Avatar" size="large" /></el-form-item>
              <el-form-item prop="phone"><el-input v-model="registerForm.phone" placeholder="手机号" prefix-icon="Phone" size="large" /></el-form-item>
              <el-form-item prop="email"><el-input v-model="registerForm.email" placeholder="邮箱（选填）" prefix-icon="Message" size="large" /></el-form-item>
              <el-form-item><el-button type="primary" size="large" class="submit-btn" :loading="registerLoading" @click="handleRegister">注 册</el-button></el-form-item>
              <div class="register-tip"><el-alert title="注册后需等待负责人审核通过后方可登录" type="info" :closable="false" show-icon /></div>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="login-footer">
          <span>Δ DELTA OPS · AUTHORIZED ACCESS ONLY</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { getRoleHomePage } from '@/router'

const router = useRouter()
const route = useRoute()
const activeTab = ref('login')
const loading = ref(false)
const registerLoading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', realName: '', phone: '', email: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, message: '密码长度至少 8 个字符', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await authApi.login(loginForm)
    if (res.code === 200) {
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('refreshToken', res.data.refreshToken)
      localStorage.setItem('expiresIn', res.data.expiresIn)
      const tokenExpiry = Date.now() + (res.data.expiresIn || 7200) * 1000
      localStorage.setItem('tokenExpiry', tokenExpiry)
      localStorage.setItem('userInfo', JSON.stringify(res.data))
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      if (redirect && redirect !== '/login') {
        router.push(redirect)
      } else {
        router.push(getRoleHomePage(res.data.role))
      }
    }
  } catch (error) {
    if (!error.response) {
      ElMessage.error('网络连接异常，请检查网络后重试')
    }
    console.error('登录失败', error)
  } finally { loading.value = false }
}

const handleRegister = async () => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }
  registerLoading.value = true
  try {
    const res = await authApi.register(registerForm)
    if (res.code === 200) { ElMessage.success('注册成功，请等待客服负责人审核'); activeTab.value = 'login' }
  } catch (error) {
    if (!error.response) {
      ElMessage.error('网络连接异常，请检查网络后重试')
    }
    console.error('注册失败', error)
  } finally { registerLoading.value = false }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--gu-bg);
  position: relative;
  overflow: hidden;
}

.bg-pattern {
  position: absolute;
  inset: 0;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(139,58,58,0.04) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(184,134,11,0.04) 0%, transparent 50%);
}

.login-card {
  width: min(400px, 90vw);
  position: relative;
  z-index: 1;
}

.card-inner {
  background: var(--gu-bg-card);
  border: 2px solid var(--gu-border);
  border-radius: var(--gu-radius-lg);
  padding: 36px 32px 28px;
  box-shadow: var(--gu-shadow-lg);
  position: relative;
}

.card-inner::before {
  content: '';
  position: absolute;
  top: -1px; left: 16%; right: 16%;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--gu-accent), transparent);
}

.login-header {
  text-align: center;
  margin-bottom: 28px;
}

.seal {
  display: inline-flex;
  margin-bottom: 12px;
  animation: sealFloat 4s ease-in-out infinite;
}

@keyframes sealFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-3px); }
}

.title {
  font-size: 22px;
  font-weight: 700;
  color: var(--gu-text-primary);
  letter-spacing: 6px;
  margin-bottom: 6px;
}

.subtitle {
  font-size: 12px;
  color: var(--gu-text-muted);
  letter-spacing: 3px;
}

.login-tabs { margin-top: 20px; }

.submit-btn {
  width: 100%;
  letter-spacing: 8px;
  font-weight: 600;
  border-radius: var(--gu-radius) !important;
}

.register-tip { margin-top: 12px; }

.login-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 10px;
  color: var(--gu-border);
  letter-spacing: 2px;
  font-family: "Courier New", monospace;
}
</style>
