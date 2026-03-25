import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLoggedIn = ref(false)

  const setUser = (user) => {
    userInfo.value = user
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  const clearUser = () => {
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
  }

  const loadUserFromStorage = () => {
    const storedUser = localStorage.getItem('userInfo')
    if (storedUser) {
      userInfo.value = JSON.parse(storedUser)
      isLoggedIn.value = true
    }
  }

  return {
    userInfo,
    isLoggedIn,
    setUser,
    clearUser,
    loadUserFromStorage
  }
})