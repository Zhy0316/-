import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: 'http://localhost:8083/api',
  timeout: 60000  // 文件上传可能较慢，改为 60 秒
})

// 请求拦截器：自动携带 JWT Token
api.interceptors.request.use(
  config => {
    const token = sessionStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理 Result<T> 格式和 HTTP 错误
api.interceptors.response.use(
  response => {
    const res = response.data
    // 后端返回 Result<T> 格式：{ code, message, data }
    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        // 成功：直接返回 data 字段，保持调用方简洁
        return res.data !== undefined ? res.data : res
      }
      // 业务错误：弹出提示并 reject
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    // 非 Result 格式（如文件流）直接返回原始数据
    return res
  },
  error => {
    const status = error.response?.status
    const serverMsg = error.response?.data?.message || error.response?.data

    if (status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('userInfo')
      window.location.href = '/login'
    } else if (status === 403) {
      ElMessage.error(serverMsg || '无权限访问该资源')
    } else if (status === 404) {
      ElMessage.error(serverMsg || '请求的资源不存在')
    } else if (status === 413) {
      ElMessage.error('文件大小超过限制（最大 100MB）')
    } else if (status >= 500) {
      ElMessage.error(serverMsg || '服务器异常，请稍后重试')
    } else if (error.message?.includes('timeout')) {
      ElMessage.error('请求超时，请检查网络')
    } else if (!error.response) {
      ElMessage.error('网络连接失败，请检查后端服务是否启动')
    } else {
      ElMessage.error(serverMsg || error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export { api }
export default api
