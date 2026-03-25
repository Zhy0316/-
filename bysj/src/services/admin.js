import api from './api'

/** 用户列表（分页+筛选） */
export const getUserList = (params) => api.get('/admin/users', { params })

/** 启用/禁用用户 */
export const updateUserStatus = (userId, status) =>
  api.put('/admin/user/status', { userId, status })

/** 管理员首页统计 */
export const getAdminDashboard = () => api.get('/admin/dashboard')

/** 全院成长分排行 */
export const getGrowthScores = () => api.get('/admin/growth-scores')

/** 企业列表 */
export const getEnterpriseList = () => api.get('/admin/enterprise/list')

/** 审核企业 */
export const auditEnterprise = (userId, auditStatus) =>
  api.put('/admin/enterprise/audit', { userId, auditStatus })
