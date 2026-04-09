import api from './api'

/**
 * 下载文件的通用方法
 * @param {string} url - 接口路径
 * @param {string} filename - 保存的文件名
 */
const downloadFile = async (url, filename) => {
  const response = await api.get(url, { responseType: 'blob' })
  const blob = new Blob([response])
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

/** 下载成绩导入 Excel 模板 */
export const downloadAcademicTemplate = () =>
  downloadFile('/export/academic-template', '成绩导入模板.xlsx')

/** 导出学生个人成长档案（Excel） */
export const exportStudentReport = (studentId) =>
  downloadFile(`/export/student-report/${studentId}`, '个人成长档案.xlsx')

/** 导出学院综合统计报表（Excel） */
export const exportCollegeReport = () =>
  downloadFile('/export/college-report', '学院综合统计报表.xlsx')

/** 导出成绩 Excel */
export const exportAcademic = (studentId) =>
  downloadFile(`/academic/export/${studentId}`, '成绩单.xlsx')
