import { api } from './api.js'

export default {
  // 发送消息
  send(toUserId, content) {
    return api.post('/message/send', { toUserId, content })
  },
  // 获取会话列表
  getConversations() {
    return api.get('/message/conversations')
  },
  // 获取聊天记录（同时标记已读）
  getHistory(otherId) {
    return api.get('/message/history', { params: { otherId } })
  },
  // 未读消息总数
  getUnreadCount() {
    return api.get('/message/unread-count')
  }
}
