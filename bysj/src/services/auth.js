import { api } from './api'

export const authAPI = {
  // 用户登录
  login: (username, password) => {
    return api.post('/auth/login', {
      username,
      password
    })
  },

  // 用户注册
  register: (userData) => {
    return api.post('/auth/register', userData)
  },

  // 获取所有角色
  getRoles: () => {
    return api.get('/auth/roles')
  }
}