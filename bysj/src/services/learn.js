import { api } from './api.js'

export default {
  // 分页查询
  getList(params) {
    return api.get('/learn/list', { params })
  },
  // 发布资源（FormData，支持文件上传）
  publish(formData) {
    return api.post('/learn/publish', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  // 点赞
  like(id) {
    return api.put(`/learn/like/${id}`)
  },
  // 删除
  remove(id) {
    return api.delete(`/learn/${id}`)
  }
}
