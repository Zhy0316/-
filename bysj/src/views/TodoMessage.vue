<template>
  <div class="todo-page">
    <div class="page-header">
      <h2>待办消息</h2>
      <el-button type="primary" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
        全部已读
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="messages.length === 0" class="empty-container">
      <el-empty description="暂无待办消息" />
    </div>

    <div v-else class="message-list">
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="message-item"
        :class="{ unread: msg.isRead === 0 }"
        @click="handleMessageClick(msg)"
      >
        <div class="message-avatar">
          <el-avatar :size="48" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png">
            机
          </el-avatar>
        </div>
        <div class="message-content">
          <div class="message-header">
            <span class="sender-name">待办消息</span>
            <span class="message-time">{{ formatTime(msg.createTime) }}</span>
          </div>
          <div class="message-text">{{ msg.content }}</div>
        </div>
        <div v-if="msg.isRead === 0" class="unread-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import todoApi from '../services/todo.js'
import { useUserStore } from '../stores/user.js'

const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.userId)

const messages = ref([])
const unreadCount = ref(0)
const loading = ref(false)

onMounted(() => {
  loadMessages()
  loadUnreadCount()
  // 定时刷新未读数量
  setInterval(loadUnreadCount, 10000)
})

const loadMessages = async () => {
  if (!userId.value) return
  loading.value = true
  try {
    messages.value = await todoApi.getSystemMessages(userId.value)
  } catch (err) {
    console.error('加载消息失败', err)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  if (!userId.value) return
  try {
    unreadCount.value = await todoApi.getUnreadCount(userId.value)
  } catch (err) {
    console.error('加载未读数量失败', err)
  }
}

const handleMessageClick = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await todoApi.markAsRead(msg.id)
      msg.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (err) {
      console.error('标记已读失败', err)
    }
  }
}

const handleMarkAllRead = async () => {
  try {
    await todoApi.markAllAsRead(userId.value)
    messages.value.forEach(msg => {
      msg.isRead = 1
    })
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}
</script>

<style scoped>
.todo-page {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 60px 20px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.message-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.message-item.unread {
  background: linear-gradient(135deg, #f0f7ff 0%, #fff 100%);
  border-left: 4px solid #409EFF;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.sender-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.unread-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #f56c6c;
  position: absolute;
  top: 16px;
  right: 16px;
}
</style>
