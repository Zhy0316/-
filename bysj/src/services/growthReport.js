import api from './api'

export default {
  // 生成成长报告
  generateReport(data) {
    return api.post('/growth-report/generate', data)
  },

  // 快速生成当前学期报告
  generateCurrentSemester() {
    return api.post('/growth-report/generate-current-semester')
  },

  // 获取我的报告列表
  getMyReports() {
    return api.get('/growth-report/my-reports')
  },

  // 获取指定学生的报告列表
  getStudentReports(studentId) {
    return api.get(`/growth-report/student/${studentId}`)
  },

  // 获取报告详情
  getReportDetail(reportId) {
    return api.get(`/growth-report/${reportId}`)
  },

  // 删除报告
  deleteReport(reportId) {
    return api.delete(`/growth-report/${reportId}`)
  }
}
