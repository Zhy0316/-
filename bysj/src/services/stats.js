import api from './api'

/** 学院整体统计概览 */
export const getCollegeOverview = () => api.get('/stats/college-overview')

/** 单个学生成长趋势 */
export const getStudentGrowthTrend = (studentId) => api.get(`/stats/student-growth/${studentId}`)

/** 获奖级别分布（饼图） */
export const getAwardDistribution = () => api.get('/stats/award-distribution')

/** 绩点分布（柱状图） */
export const getGpaDistribution = () => api.get('/stats/gpa-distribution')

/** 管理员首页统计 */
export const getAdminDashboard = () => api.get('/stats/admin-dashboard')

// ==================== 成长评分 ====================

/** 获取某学生各学期成长分 */
export const getGrowthScores = (studentId) => api.get(`/growth-score/${studentId}`)

/** 获取当前学生自己的成长分 */
export const getMyGrowthScores = () => api.get('/growth-score/my')

/** 触发重新计算成长分 */
export const calculateGrowthScore = (studentId) =>
  api.post(`/growth-score/calculate/${studentId}`)

/** 获取雷达图数据 */
export const getRadarData = (studentId) => api.get(`/growth-score/radar/${studentId}`)

/** 全院成长分排行 */
export const getGrowthRanking = () => api.get('/growth-score/ranking')
