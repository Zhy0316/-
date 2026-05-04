import api from './api'

/**
 * 日记分析相关API
 */

// 获取所有标签(按类型分组)
export const getAllTagsGrouped = () => {
  return api.get('/diary-analysis/tags/grouped')
}

// 根据类型获取标签列表
export const getTagsByType = (tagType) => {
  return api.get(`/diary-analysis/tags/type/${tagType}`)
}

// 获取系统标签
export const getSystemTags = () => {
  return api.get('/diary-analysis/tags/system')
}

// 为日记添加标签
export const addTagsToDiary = (diaryId, tagIds) => {
  return api.post('/diary-analysis/tags/add', { diaryId, tagIds })
}

// 获取日记的标签列表
export const getDiaryTags = (diaryId) => {
  return api.get(`/diary-analysis/tags/diary/${diaryId}`)
}

// 情绪统计分析
export const getMoodStatistics = (studentId) => {
  return api.get(`/diary-analysis/mood-statistics/${studentId}`)
}

// 时光轴数据生成
export const getTimeline = (studentId, groupBy = 'month') => {
  return api.get(`/diary-analysis/timeline/${studentId}`, { params: { groupBy } })
}

// 关键词统计
export const getKeywords = (studentId) => {
  return api.get(`/diary-analysis/keywords/${studentId}`)
}

// 添加导师批注
export const addTutorComment = (diaryId, comment) => {
  return api.post('/diary-analysis/tutor-comment', { diaryId, comment })
}

// 获取里程碑日记列表
export const getMilestoneDiaries = (studentId) => {
  return api.get(`/diary-analysis/milestones/${studentId}`)
}

// 获取导师可见的日记列表
export const getTutorVisibleDiaries = (studentId) => {
  return api.get(`/diary-analysis/tutor-visible/${studentId}`)
}

export default {
  getAllTagsGrouped,
  getTagsByType,
  getSystemTags,
  addTagsToDiary,
  getDiaryTags,
  getMoodStatistics,
  getTimeline,
  getKeywords,
  addTutorComment,
  getMilestoneDiaries,
  getTutorVisibleDiaries
}
