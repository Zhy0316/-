import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const roleKey = computed(() => userInfo.value?.roleKey || '')

  /** 登录成功后保存用户信息和 token */
  const login = (user) => {
    userInfo.value = user
    token.value = user.token
    localStorage.setItem('token', user.token)
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  /** 退出登录，清除所有状态 */
  const logout = () => {
    userInfo.value = null
    token.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /** 应用启动时从 localStorage 恢复状态 */
  const loadFromStorage = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch {
        logout()
      }
    }
  }

  // 兼容旧代码
  const setUser = login
  const clearUser = logout
  const loadUserFromStorage = loadFromStorage

  return {
    userInfo,
    token,
    isLoggedIn,
    roleKey,
    login,
    logout,
    loadFromStorage,
    setUser,
    clearUser,
    loadUserFromStorage
  }
})
