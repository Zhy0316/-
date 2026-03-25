import axios from 'axios'
import { api } from './api.js'

export const studentAPI = {
  // 获取学生个人档案
  getProfile(userId) {
    return api.get(`/student/profile/${userId}`)
  },
  
  // 更新学生个人档案
  updateProfile(userId, data) {
    return api.post(`/student/profile/${userId}`, data)
  },
  
  // 获取所有教师列表
  getTutors(params) {
    return api.get('/student/tutors', { params })
  },
  
  // 申请绑定教师
  applyForTutor(studentId, tutorId) {
    return api.post('/relation/apply', { studentId, tutorId })
  },
  
  // 获取学生的绑定关系
  getStudentRelations(studentId) {
    return api.get(`/relation/student/${studentId}`)
  }
}
