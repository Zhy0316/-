import api from './api'

/** 企业注册（公开接口） */
export const registerEnterprise = (data) => api.post('/enterprise/register', data)

/** 获取当前企业信息 */
export const getEnterpriseInfo = () => api.get('/enterprise/info')

/** 更新企业信息 */
export const updateEnterpriseInfo = (data) => api.put('/enterprise/info', data)

/** 上传营业执照 */
export const uploadLicense = (formData) => api.post('/enterprise/license/upload', formData)

/** 管理员：获取所有企业列表 */
export const listAllEnterprises = () => api.get('/admin/enterprise/list')

/** 管理员：审核企业 */
export const auditEnterprise = (userId, auditStatus) =>
  api.put('/admin/enterprise/audit', { userId, auditStatus })
