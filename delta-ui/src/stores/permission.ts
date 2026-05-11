import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useAuthStore } from './auth'

/**
 * 权限 Store - 管理前端权限校验和动态展示
 * 支持按钮级、菜单级、数据级权限控制
 *
 * @author 刘建国
 */
export const usePermissionStore = defineStore('permission', () => {
  const authStore = useAuthStore()

  /** 当前用户的权限编码列表 */
  const permCodes = ref<string[]>([])

  /** 权限版本号，用于触发响应式更新 */
  const permVersion = ref(0)

  /**
   * 从登录信息初始化权限列表
   * @param permissions 权限编码数组
   */
  function initPermissions(permissions: string[]) {
    permCodes.value = permissions || []
    permVersion.value++
  }

  /**
   * 检查是否拥有指定权限
   * 系统管理员拥有所有权限
   * @param permCode 权限编码，如 'customer:view'
   */
  function hasPermission(permCode: string): boolean {
    if (!permCode) return true
    if (authStore.isAdmin) return true
    return permCodes.value.includes(permCode)
  }

  /**
   * 检查是否拥有任一权限（满足一个即可）
   * @param codes 权限编码数组
   */
  function hasAnyPermission(codes: string[]): boolean {
    if (!codes || codes.length === 0) return true
    if (authStore.isAdmin) return true
    return codes.some((code) => permCodes.value.includes(code))
  }

  /**
   * 检查是否拥有全部权限（全部满足）
   * @param codes 权限编码数组
   */
  function hasAllPermissions(codes: string[]): boolean {
    if (!codes || codes.length === 0) return true
    if (authStore.isAdmin) return true
    return codes.every((code) => permCodes.value.includes(code))
  }

  /** 是否有权限管理权限 */
  const canManagePermission = computed(() => hasPermission('permission:manage'))

  /**
   * 清空权限（退出登录时调用）
   */
  function clearPermissions() {
    permCodes.value = []
    permVersion.value = 0
  }

  return {
    permCodes,
    permVersion,
    initPermissions,
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    canManagePermission,
    clearPermissions
  }
})