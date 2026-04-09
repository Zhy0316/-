import api from './api'

// ==================== 招聘管理 ====================

/** 企业发布招聘 */
export const publishRecruitment = (data) => api.post('/recruitment/publish', data)

/** 公开浏览招聘列表 */
export const getRecruitmentList = () => api.get('/recruitment/list')

/** 企业查看自己发布的招聘 */
export const getMyRecruitments = () => api.get('/recruitment/my')

/** 招聘详情 */
export const getRecruitmentDetail = (id) => api.get(`/recruitment/${id}`)

/** 关闭招聘 */
export const closeRecruitment = (id) => api.put(`/recruitment/close/${id}`)

// ==================== 求职申请 ====================

/** 学生投递简历 */
export const applyJob = (recruitmentId, message) =>
  api.post('/job-application/apply', { recruitmentId, message })

/** 学生查看自己的投递记录 */
export const getMyApplications = () => api.get('/job-application/my')

/** 企业查看某职位的投递列表 */
export const getApplicationsByRecruitment = (recruitmentId) =>
  api.get(`/job-application/list/${recruitmentId}`)

/** 企业更新投递状态 */
export const updateApplicationStatus = (applicationId, status) =>
  api.put('/job-application/status', { applicationId, status })
