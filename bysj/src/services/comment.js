import api from './api'

/** 导师添加评语/批注 */
export const saveComment = (data) => api.post('/comment/save', data)

/** 查询某学生的所有评语 */
export const getCommentList = (studentId) => api.get('/comment/list', { params: { studentId } })

/** 查询当前导师对某学生的评语 */
export const getMyComments = (studentId) => api.get('/comment/my', { params: { studentId } })

/** 删除评语 */
export const deleteComment = (id) => api.delete(`/comment/${id}`)

/** 编辑评语 */
export const updateComment = (id, content) => api.put(`/comment/${id}`, { content })
