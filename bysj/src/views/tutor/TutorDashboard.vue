<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="24"><School /></el-icon>
        <span>成长档案系统</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529"
        text-color="#ffffffa0" active-text-color="#fff">
        <el-menu-item index="/tutor/home">
          <el-icon><House /></el-icon><span>首页</span>
        </el-menu-item>
        <el-menu-item index="/tutor/students">
          <el-icon><User /></el-icon><span>我的学生</span>
        </el-menu-item>
        <el-menu-item index="/tutor/pending">
          <el-icon><Bell /></el-icon>
          <span>待审核申请
            <el-badge v-if="pendingCount > 0" :value="pendingCount" style="margin-left:4px" />
          </span>
        </el-menu-item>
        <el-menu-item index="/tutor/comments">
          <el-icon><ChatDotRound /></el-icon><span>我的评语</span>
        </el-menu-item>
        <el-menu-item index="/tutor/message">
          <el-icon><Message /></el-icon><span>消息</span>
        </el-menu-item>
        <el-menu-item index="/tutor/learn">
          <el-icon><Reading /></el-icon><span>学习天地</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <UserDropdown />
        </div>
      </el-header>

      <el-main class="main">
        <!-- 导师首页概览 -->
        <template v-if="$route.path === '/tutor/home' || $route.path === '/tutor'">
          <el-row :gutter="20" style="margin-bottom:20px">
            <el-col :span="6" v-for="s in stats" :key="s.label">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon" :style="{ background: s.color }">
                  <el-icon size="26" color="#fff"><component :is="s.icon" /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-num">{{ s.value }}</div>
                  <div class="stat-label">{{ s.label }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="12">
              <el-card>
                <template #header>
                  <div style="display:flex; justify-content:space-between; align-items:center">
                    <span>待办消息</span>
                    <div>
                      <el-badge v-if="unreadTodoCount > 0" :value="unreadTodoCount" style="margin-right:10px" />
                      <el-button size="small" text type="danger" @click="handleClearAll">清空</el-button>
                    </div>
                  </div>
                </template>
                <div v-if="todoMessages.length === 0" style="text-align:center; color:#909399; padding:20px">
                  暂无待办消息
                </div>
                <div v-else class="todo-list">
                  <div
                    v-for="msg in todoMessages.slice(0, 5)"
                    :key="msg.id"
                    class="todo-item"
                    :class="{ unread: msg.isRead === 0 }"
                  >
                    <div style="display:flex; justify-content:space-between; align-items:start">
                      <div style="flex:1; cursor:pointer" @click="handleTodoClick(msg)">
                        <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px">
                          <el-tag v-if="msg.isRead === 0" type="danger" size="small">未读</el-tag>
                          <el-tag v-else type="info" size="small">已读</el-tag>
                          <span class="todo-content">{{ msg.content }}</span>
                        </div>
                        <div class="todo-time">{{ formatTime(msg.createTime) }}</div>
                      </div>
                      <el-button size="small" text type="danger" @click.stop="handleDeleteMessage(msg.id)">
                        <el-icon><Delete /></el-icon>
                      </el-button>
                    </div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card header="学生成长分分布">
                <div ref="barRef" style="height:300px"></div>
              </el-card>
            </el-col>
          </el-row>
        </template>
        <router-view v-else />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'
import * as echarts from 'echarts'
import {
  House, User, UserFilled, ChatDotRound, Message, Reading,
  SwitchButton, School, Delete
} from '@element-plus/icons-vue'
import UserDropdown from '../../components/Layout/UserDropdown.vue'
import todoApi from '../../services/todo.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const barRef = ref()
const pendingCount = ref(0)
const unreadTodoCount = ref(0)
const todoMessages = ref([])

const stats = ref([
  { label: '绑定学生数', value: 0, icon: 'User',         color: '#409EFF' },
  { label: '待审核申请', value: 0, icon: 'Bell',         color: '#E6A23C' },
  { label: '已发评语数', value: 0, icon: 'ChatDotRound', color: '#67C23A' },
  { label: '待审获奖数', value: 0, icon: 'Trophy',       color: '#F56C6C' },
])

const pageTitle = computed(() => {
  const map = {
    '/tutor/home': '首页', '/tutor/students': '我的学生',
    '/tutor/pending': '待审核申请', '/tutor/comments': '我的评语',
    '/tutor/profile': '个人资料', '/tutor/message': '消息',
    '/tutor/learn': '学习天地', '/tutor/todo': '待办消息'
  }
  return map[route.path] || '导师端'
})

const loadUnreadTodoCount = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    unreadTodoCount.value = await todoApi.getUnreadCount(userId)
  } catch (e) { /* 忽略 */ }
}

const loadTodoMessages = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    todoMessages.value = await todoApi.getSystemMessages(userId)
  } catch (e) { /* 忽略 */ }
}

const handleTodoClick = async (msg) => {
  if (msg.isRead === 0) {
    try {
      await todoApi.markAsRead(msg.id)
      msg.isRead = 1
      unreadTodoCount.value = Math.max(0, unreadTodoCount.value - 1)
    } catch (e) { /* 忽略 */ }
  }

  const content = msg.content || ''
  if (content.includes('绑定申请')) {
    router.push('/tutor/pending')
  } else if (content.includes('获奖')) {
    router.push('/tutor/pending')
  }
}

const handleDeleteMessage = async (messageId) => {
  try {
    await ElMessageBox.confirm('确定删除该消息？', '提示', { type: 'warning' })
    await todoApi.deleteMessage(messageId)
    ElMessage.success('删除成功')
    const msg = todoMessages.value.find(m => m.id === messageId)
    if (msg && msg.isRead === 0) {
      unreadTodoCount.value = Math.max(0, unreadTodoCount.value - 1)
    }
    todoMessages.value = todoMessages.value.filter(m => m.id !== messageId)
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

const handleClearAll = async () => {
  try {
    await ElMessageBox.confirm('确定清空所有待办消息？', '提示', { type: 'warning' })
    const userId = userStore.userInfo?.userId
    if (userId) {
      await todoApi.deleteAll(userId)
      ElMessage.success('清空成功')
      todoMessages.value = []
      unreadTodoCount.value = 0
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('清空失败')
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

  return `${d.getMonth() + 1}/${d.getDate()}`
}

const handleLogout = () => { userStore.logout(); router.push('/login') }

const loadStats = async () => {
  const tutorId = userStore.userInfo?.userId
  if (!tutorId) return
  try {
    const relations = await api.get(`/relation/tutor/${tutorId}`).catch(() => []) || []
    const bound = (relations || []).filter(r => r.status === 1)
    const pending = (relations || []).filter(r => r.status === 0)
    stats.value[0].value = bound.length
    stats.value[1].value = pending.length
    pendingCount.value = pending.length
  } catch (e) { /* 忽略 */ }
}

const renderBar = async () => {
  if (!barRef.value) return
  try {
    const data = await api.get('/growth-score/ranking').catch(() => []) || []
    const names = (data || []).slice(0, 10).map(d => d.realName || d.studentId)
    const scores = (data || []).slice(0, 10).map(d => d.totalScore)
    echarts.init(barRef.value).setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: names, axisLabel: { rotate: 30 } },
      yAxis: { type: 'value', min: 0, max: 100 },
      series: [{ name: '综合成长分', type: 'bar', data: scores,
        itemStyle: { color: '#409EFF' }, barMaxWidth: 40 }]
    })
  } catch (e) { /* 忽略 */ }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  renderBar()
  loadUnreadTodoCount()
  loadTodoMessages()
  // 每10秒刷新未读数量和消息列表
  setInterval(() => {
    loadUnreadTodoCount()
    loadTodoMessages()
  }, 10000)
})
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; overflow: hidden; }
.logo {
  height: 64px; display: flex; align-items: center; justify-content: center;
  gap: 10px; color: #fff; font-size: 16px; font-weight: bold;
  border-bottom: 1px solid #ffffff20;
}
.header {
  background: #fff; display: flex; align-items: center;
  justify-content: space-between; border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.page-title { font-size: 16px; font-weight: 600; color: #303133; }
.header-right { display: flex; align-items: center; gap: 12px; }
.username { color: #606266; font-size: 14px; }
.main { background: #f5f7fa; padding: 20px; overflow-y: auto; }
.stat-card { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-num { font-size: 26px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.2s;
}

.todo-item:hover {
  background: #ecf5ff;
  transform: translateX(4px);
}

.todo-item.unread {
  background: #fff;
  border-left: 3px solid #409EFF;
  box-shadow: 0 2px 4px rgba(0,0,0,0.06);
}

.todo-content {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.todo-time {
  font-size: 12px;
  color: #909399;
}
</style>
