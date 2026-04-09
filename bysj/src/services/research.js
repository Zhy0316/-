import api from './api'

/** 获取科研项目列表 */
export const getResearchList = (studentId) => api.get(`/research/list/${studentId}`)

/** 上传科研项目（含成果附件） */
export const uploadResearch = (formData) => api.post('/research/upload', formData)

/** 更新科研项目 */
export const updateResearch = (data) => api.put('/research/update', data)

/** 删除科研项目 */
export const deleteResearch = (id) => api.delete(`/research/delete/${id}`)
