import api from './api'

export const authAPI = {
  /** 用户登录，返回含 token 的用户信息 */
  login: (username, password) =>
    api.post('/auth/login', { username, password }),

  /** 通用注册（学生/导师） */
  register: (userData) =>
    api.post('/auth/register', userData),

  /** 指定接口注册（企业HR走 /enterprise/register） */
  registerWithEndpoint: (endpoint, userData) =>
    api.post(endpoint, userData),

  /** 获取当前登录用户信息 */
  getMe: () =>
    api.get('/auth/me'),

  /** 修改密码 */
  changePassword: (oldPassword, newPassword) =>
    api.post('/auth/change-password', { oldPassword, newPassword }),

  /** 获取所有角色列表 */
  getRoles: () =>
    api.get('/auth/roles')
}
