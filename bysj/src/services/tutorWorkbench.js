import api from './api'

/**
 * 导师工作台相关API
 */

// 获取学生成长看板
export const getStudentDashboard = (tutorId) => {
  return api.get(`/tutor-workbench/dashboard/${tutorId}`)
}

// 获取单个学生的关键指标
export const getStudentIndicators = (studentId) => {
  return api.get(`/tutor-workbench/student-indicators/${studentId}`)
}

// 识别预警学生
export const getWarningStudents = (tutorId) => {
  return api.get(`/tutor-workbench/warning-students/${tutorId}`)
}

// 添加指导记录
export const addGuidanceRecord = (record) => {
  return api.post('/tutor-workbench/guidance-record', record)
}

// 获取指导记录列表
export const getGuidanceRecords = (tutorId, studentId = null) => {
  return api.get('/tutor-workbench/guidance-records', {
    params: { tutorId, studentId }
  })
}

// 获取指导统计
export const getGuidanceStatistics = (tutorId) => {
  return api.get(`/tutor-workbench/guidance-statistics/${tutorId}`)
}

// 创建学生目标
export const createStudentGoal = (goal) => {
  return api.post('/tutor-workbench/student-goal', goal)
}

// 更新目标进度
export const updateGoalProgress = (goalId, progress) => {
  return api.put(`/tutor-workbench/student-goal/${goalId}/progress`, null, {
    params: { progress }
  })
}

// 获取学生目标列表
export const getStudentGoals = (studentId) => {
  return api.get(`/tutor-workbench/student-goals/${studentId}`)
}

// 创建预约
export const createAppointment = (appointment) => {
  return api.post('/tutor-workbench/appointment', appointment)
}

// 确认预约
export const confirmAppointment = (appointmentId) => {
  return api.put(`/tutor-workbench/appointment/${appointmentId}/confirm`)
}

// 获取待确认的预约
export const getPendingAppointments = (tutorId) => {
  return api.get(`/tutor-workbench/appointments/pending/${tutorId}`)
}

// 获取预约列表
export const getAppointments = (tutorId, studentId = null) => {
  return api.get('/tutor-workbench/appointments', {
    params: { tutorId, studentId }
  })
}

export default {
  getStudentDashboard,
  getStudentIndicators,
  getWarningStudents,
  addGuidanceRecord,
  getGuidanceRecords,
  getGuidanceStatistics,
  createStudentGoal,
  updateGoalProgress,
  getStudentGoals,
  createAppointment,
  confirmAppointment,
  getPendingAppointments,
  getAppointments
}
