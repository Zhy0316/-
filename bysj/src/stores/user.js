import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const token = ref(sessionStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const roleKey = computed(() => userInfo.value?.roleKey || '')

  /** 登录成功后保存用户信息和 token */
  const login = (user) => {
    userInfo.value = user
    token.value = user.token
    sessionStorage.setItem('token', user.token)
    sessionStorage.setItem('userInfo', JSON.stringify(user))
  }

  /** 退出登录，清除所有状态 */
  const logout = () => {
    userInfo.value = null
    token.value = ''
    sessionStorage.removeItem('token')
    sessionStorage.removeItem('userInfo')
  }

  /** 更新用户头像 */
  const updateAvatar = (avatarUrl) => {
    if (userInfo.value) {
      userInfo.value.avatar = avatarUrl
      sessionStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  /** 应用启动时从 sessionStorage 恢复状态（刷新时保持登录） */
  const loadFromStorage = () => {
    const stored = sessionStorage.getItem('userInfo')
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
    updateAvatar,
    loadFromStorage,
    setUser,
    clearUser,
    loadUserFromStorage
  }
})
