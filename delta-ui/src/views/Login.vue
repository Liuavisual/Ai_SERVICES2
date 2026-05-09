<!--
  登录/注册页面 - 全屏响应式布局

  @author 刘建国
-->
<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="bg-gradient"></div>
      <div class="bg-grid"></div>
      <div class="bg-orb bg-orb-1"></div>
      <div class="bg-orb bg-orb-2"></div>
      <div class="bg-orb bg-orb-3"></div>
    </div>

    <div class="login-card" :class="{ 'shake': isShaking }">
      <div class="card-glass">
        <div class="login-header">
          <div class="logo-wrap">
            <svg viewBox="0 0 40 40" fill="none" width="44" height="44">
              <rect x="2" y="2" width="36" height="36" rx="10" fill="#6366F1"/>
              <path d="M14 20h12M20 14v12" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="title">Delta Companion</h1>
          <p class="subtitle">AI-Powered Customer Service Platform</p>
        </div>

        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="登录" name="login">
            <transition name="error-fade">
              <div v-if="loginErrorMessage" class="login-error-banner">
                <svg class="error-icon" viewBox="0 0 20 20" fill="none" width="18" height="18">
                  <circle cx="10" cy="10" r="9" stroke="currentColor" stroke-width="1.5"/>
                  <path d="M10 6v4M10 13.5v.5" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/>
                </svg>
                <span class="error-text">{{ loginErrorMessage }}</span>
              </div>
            </transition>
            <el-alert v-if="registerSuccessMessage" :title="registerSuccessMessage" type="success" show-icon closable class="register-success-alert" />
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0">
              <el-form-item prop="username">
                <el-input v-model="loginForm.username" placeholder="用户名" prefix-icon="User" size="large" name="username" @input="clearLoginError" />
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" size="large" show-password @keyup.enter="handleLogin" name="password" @input="clearLoginError" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">登 录</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="0">
              <el-form-item prop="username"><el-input v-model="registerForm.username" placeholder="用户名（3-50位）" prefix-icon="User" size="large" /></el-form-item>
              <el-form-item prop="password"><el-input v-model="registerForm.password" type="password" placeholder="密码（字母+数字+特殊字符，至少8位）" prefix-icon="Lock" size="large" show-password /></el-form-item>
              <el-form-item prop="realName"><el-input v-model="registerForm.realName" placeholder="真实姓名" prefix-icon="Avatar" size="large" /></el-form-item>
              <el-form-item prop="phone"><el-input v-model="registerForm.phone" placeholder="手机号" prefix-icon="Phone" size="large" /></el-form-item>
              <el-form-item prop="email"><el-input v-model="registerForm.email" placeholder="邮箱（选填）" prefix-icon="Message" size="large" /></el-form-item>
              <el-form-item><el-button type="primary" size="large" class="submit-btn" :loading="registerLoading" @click="handleRegister">注 册</el-button></el-form-item>
              <div class="register-tip"><el-alert title="注册后需等待负责人审核通过后方可登录" type="info" :closable="false" show-icon /></div>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="login-footer">
          <span>Delta Companion &copy; 2025</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { authStorage } from '@/utils/storage'
import { authApi } from '@/api'
import type { LoginDTO, RegisterDTO, LoginVO, UserRole } from '@/types'

const router = useRouter()
const route = useRoute()
/** 当前激活的Tab页 */
const activeTab = ref<string>('login')
/** 登录加载状态 */
const loading = ref<boolean>(false)
/** 注册加载状态 */
const registerLoading = ref<boolean>(false)
/** 登录表单引用 */
const loginFormRef = ref<FormInstance | null>(null)
/** 注册表单引用 */
const registerFormRef = ref<FormInstance | null>(null)

/** 注册成功提示消息（切换回登录Tab后持久显示） */
const registerSuccessMessage = ref<string>('')

/** 登录错误提示消息 */
const loginErrorMessage = ref<string>('')

/** 登录卡片抖动动画触发 */
const isShaking = ref<boolean>(false)

/** 登录表单数据 */
const loginForm = reactive<LoginDTO>({ username: '', password: '' })
/** 注册表单数据 */
const registerForm = reactive<RegisterDTO>({ username: '', password: '', realName: '', phone: '', email: '' })

/** 登录表单校验规则 */
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 50, message: '密码长度必须在 8 到 50 个字符', trigger: 'blur' }
  ]
}

/** 注册表单校验规则 */
const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度在 3 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]{3,50}$/, message: '用户名只能包含字母、数字、下划线和中文', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 50, message: '密码长度必须在 8 到 50 个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>])/, message: '密码必须包含字母、数字和特殊字符', trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email' as const, message: '邮箱格式不正确', trigger: 'blur' }]
}

const authStore = useAuthStore()

/**
 * 处理用户登录
 * 验证表单后通过 Pinia Store 调用登录API，存储Token并跳转
 */
const handleLogin = async (): Promise<void> => {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  loginErrorMessage.value = ''
  try {
    const res = await authStore.login(loginForm)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      const redirect = route.query.redirect
      if (redirect && typeof redirect === 'string' && redirect !== '/login') {
        router.push(redirect)
      } else {
        const data = res.data as LoginVO
        router.push(authStorage.getRoleHomePage(data.role as UserRole))
      }
    } else {
      showLoginError(res.message || '登录失败，请检查用户名和密码')
    }
  } catch (error: unknown) {
    const err = error as { response?: { status?: number }; message?: string }
    if (!err.response) {
      showLoginError('网络连接异常，请检查网络后重试')
    } else {
      showLoginError(err.message || '登录失败')
    }
    console.error('登录失败', error)
  } finally { loading.value = false }
}

/**
 * 显示登录错误提示并触发抖动动画
 * @param message 错误消息
 */
const showLoginError = (message: string): void => {
  loginErrorMessage.value = message
  isShaking.value = true
  setTimeout(() => { isShaking.value = false }, 500)
}

/**
 * 清除登录错误提示
 */
const clearLoginError = (): void => {
  loginErrorMessage.value = ''
  registerSuccessMessage.value = ''
}

/**
 * 处理用户注册
 * 验证表单后调用注册API，成功后切换到登录Tab
 */
const handleRegister = async (): Promise<void> => {
  if (!registerFormRef.value) return
  try {
    await registerFormRef.value.validate()
  } catch {
    return
  }
  registerLoading.value = true
  try {
    const res = await authApi.register(registerForm)
    if (res.code === 200) {
      registerSuccessMessage.value = '账号注册成功，请等待客服负责人审核通过后即可登录'
      activeTab.value = 'login'
      registerFormRef.value?.resetFields()
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error: unknown) {
    const err = error as { response?: { status?: number } }
    if (!err.response) {
      ElMessage.error('网络连接异常，请检查网络后重试')
    }
    console.error('注册失败', error)
  } finally { registerLoading.value = false }
}
</script>

<style scoped>
.login-container {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #0F172A;
  z-index: 9999;
}

.login-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #0F172A 0%, #1E293B 50%, #0F172A 100%);
}

.bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.bg-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  animation: float 8s ease-in-out infinite;
}

.bg-orb-1 {
  width: 500px;
  height: 500px;
  background: rgba(99, 102, 241, 0.12);
  top: -150px;
  right: -100px;
  animation-delay: 0s;
}

.bg-orb-2 {
  width: 400px;
  height: 400px;
  background: rgba(139, 92, 246, 0.10);
  bottom: -120px;
  left: -100px;
  animation-delay: -3s;
}

.bg-orb-3 {
  width: 250px;
  height: 250px;
  background: rgba(249, 115, 22, 0.06);
  top: 60%;
  left: 30%;
  animation-delay: -5s;
}

.login-card {
  width: min(420px, 92vw);
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease-out;
}

.card-glass {
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: var(--gu-radius-xl);
  padding: 40px 36px 32px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrap {
  display: inline-flex;
  margin-bottom: 16px;
  animation: float 4s ease-in-out infinite;
}

.title {
  font-family: var(--gu-font-heading);
  font-size: 24px;
  font-weight: 700;
  color: var(--gu-text-primary);
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.subtitle {
  font-family: var(--gu-font-heading);
  font-size: 13px;
  color: var(--gu-text-muted);
  letter-spacing: 0.02em;
  font-weight: 400;
}

.login-tabs { margin-top: 24px; }

.submit-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.1em;
  border-radius: var(--gu-radius-lg) !important;
}

.register-tip { margin-top: 12px; }

.register-success-alert { margin-bottom: 16px; }

.login-error-banner {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--gu-radius-lg, 10px);
  background: linear-gradient(135deg, rgba(239, 68, 68, 0.10), rgba(220, 38, 38, 0.06));
  border: 1px solid rgba(239, 68, 68, 0.25);
  margin-bottom: 16px;
  animation: errorSlideIn 0.3s ease-out;
}

.error-icon {
  flex-shrink: 0;
  color: #EF4444;
}

.error-text {
  font-size: 13px;
  line-height: 1.5;
  color: #991B1B;
  font-weight: 500;
}

.shake {
  animation: shakeX 0.5s ease-in-out;
}

@keyframes shakeX {
  0%, 100% { transform: translateX(0); }
  10%, 30%, 50%, 70%, 90% { transform: translateX(-4px); }
  20%, 40%, 60%, 80% { transform: translateX(4px); }
}

@keyframes errorSlideIn {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.error-fade-enter-active { animation: errorSlideIn 0.3s ease-out; }
.error-fade-leave-active { transition: opacity 0.2s ease-in; }
.error-fade-leave-to { opacity: 0; }

.login-footer {
  margin-top: 28px;
  text-align: center;
  font-size: 11px;
  color: var(--gu-text-muted);
  font-family: var(--gu-font-heading);
  letter-spacing: 0.02em;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-8px); }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-height: 600px) {
  .card-glass {
    padding: 20px 24px 16px;
  }
  .login-header {
    margin-bottom: 16px;
  }
  .logo-wrap {
    margin-bottom: 8px;
  }
  .title {
    font-size: 20px;
  }
  .login-tabs {
    margin-top: 12px;
  }
  .login-footer {
    margin-top: 16px;
  }
}

@media (max-width: 480px) {
  .card-glass {
    padding: 28px 20px 24px;
  }
}
</style>
