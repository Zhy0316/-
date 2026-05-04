import api from './api.js'

export default {
  /**
   * 获取用户的系统通知列表
   */
  getSystemMessages(userId) {
    return api.get(`/todo/list/${userId}`)
  },

  /**
   * 获取未读系统通知数量
   */
  getUnreadCount(userId) {
    return api.get(`/todo/unread/${userId}`)
  },

  /**
   * 标记系统通知为已读
   */
  markAsRead(messageId) {
    return api.put(`/todo/read/${messageId}`)
  },

  /**
   * 标记所有系统通知为已读
   */
  markAllAsRead(userId) {
    return api.put(`/todo/read-all/${userId}`)
  },

  /**
   * 删除单条系统通知
   */
  deleteMessage(messageId) {
    return api.delete(`/todo/delete/${messageId}`)
  },

  /**
   * 批量删除系统通知
   */
  deleteAll(userId) {
    return api.delete(`/todo/delete-batch/${userId}`)
  }
}
