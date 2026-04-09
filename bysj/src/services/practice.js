import api from './api'

/** 获取实践记录列表 */
export const getPracticeList = (studentId) => api.get(`/practice/list/${studentId}`)

/** 上传实践记录（含证明材料） */
export const uploadPractice = (formData) => api.post('/practice/upload', formData)

/** 更新实践记录 */
export const updatePractice = (data) => api.put('/practice/update', data)

/** 删除实践记录 */
export const deletePractice = (id) => api.delete(`/practice/delete/${id}`)
