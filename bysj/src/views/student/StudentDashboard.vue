<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="24"><School /></el-icon>
        <span>成长档案系统</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529"
        text-color="#ffffffa0" active-text-color="#fff">
        <el-menu-item index="/student/home">
          <el-icon><House /></el-icon><span>首页概览</span>
        </el-menu-item>
        <el-menu-item index="/student/academic">
          <el-icon><Reading /></el-icon><span>学业成绩</span>
        </el-menu-item>
        <el-menu-item index="/student/record">
          <el-icon><Notebook /></el-icon><span>成长日志</span>
        </el-menu-item>
        <el-menu-item index="/student/award">
          <el-icon><Trophy /></el-icon><span>获奖记录</span>
        </el-menu-item>
        <el-menu-item index="/student/portfolio">
          <el-icon><Collection /></el-icon><span>作品集</span>
        </el-menu-item>
        <el-menu-item index="/student/practice">
          <el-icon><Suitcase /></el-icon><span>实习经历</span>
        </el-menu-item>
        <el-menu-item index="/student/growth">
          <el-icon><TrendCharts /></el-icon><span>成长评分</span>
        </el-menu-item>
        <el-menu-item index="/student/growth-report">
          <el-icon><Document /></el-icon><span>成长报告</span>
        </el-menu-item>
        <el-menu-item index="/student/recruitment">
          <el-icon><Briefcase /></el-icon><span>招聘信息</span>
        </el-menu-item>
        <el-menu-item index="/student/message">
          <el-icon><ChatDotRound /></el-icon><span>消息</span>
        </el-menu-item>
        <el-menu-item index="/student/forum">
          <el-icon><Reading /></el-icon><span>学习天地</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <UserDropdown />
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main">
        <!-- 首页概览内容 -->
        <template v-if="$route.path === '/student/home' || $route.path === '/student'">
          <el-row :gutter="20" class="stat-row">
            <el-col :span="6" v-for="s in stats" :key="s.label">
              <el-card shadow="hover" class="stat-card">
                <div class="stat-icon" :style="{ background: s.color }">
                  <el-icon size="28" color="#fff"><component :is="s.icon" /></el-icon>
                </div>
                <div class="stat-info">
                  <div class="stat-num">{{ s.value }}</div>
                  <div class="stat-label">{{ s.label }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top:20px">
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
              <el-card header="绩点趋势">
                <div ref="lineChartRef" style="height:260px"></div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top:20px">
            <el-col :span="24">
              <el-card header="能力雷达图">
                <div ref="radarChartRef" style="height:260px"></div>
              </el-card>
            </el-col>
          </el-row>
        </template>

        <!-- 子路由页面 -->
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
import { getCollegeOverview, getRadarData, getMyGrowthScores } from '../../services/stats.js'
import { getAcademicRecords } from '../../services/academic.js'
import * as echarts from 'echarts'
import {
  House, User, Reading, Notebook, Collection,
  TrendCharts, Briefcase, SwitchButton, School, Suitcase, Trophy, MagicStick, ChatDotRound, Bell, Delete
} from '@element-plus/icons-vue'
import UserDropdown from '../../components/Layout/UserDropdown.vue'
import todoApi from '../../services/todo.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const lineChartRef = ref()
const radarChartRef = ref()
const unreadTodoCount = ref(0)
const todoMessages = ref([])

const stats = ref([
  { label: '学业成绩数', value: 0, icon: 'Reading',    color: '#409EFF' },
  { label: '获奖记录',   value: 0, icon: 'Trophy',     color: '#E6A23C' },
  { label: '科研项目',   value: 0, icon: 'Aim',        color: '#67C23A' },
  { label: '综合成长分', value: 0, icon: 'TrendCharts', color: '#F56C6C' },
])

const pageTitle = computed(() => {
  const map = {
    '/student/home': '首页概览', '/student/profile': '个人档案',
    '/student/academic': '学业成绩', '/student/record': '成长日志',
    '/student/portfolio': '作品集', '/student/practice': '实习经历',
    '/student/growth': '成长评分', '/student/growth-report': '成长报告',
    '/student/recruitment': '招聘信息',
    '/student/message': '消息', '/student/learn': '学习天地',
    '/student/todo': '待办消息'
  }
  return map[route.path] || '学生端'
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
    router.push('/student/profile')
  } else if (content.includes('获奖申请') || content.includes('获奖记录')) {
    router.push('/student/award')
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

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const loadStats = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    // 成绩数
    const academics = await getAcademicRecords(userId)
    stats.value[0].value = (academics || []).length

    // 成长分
    const scores = await getMyGrowthScores()
    if (scores && scores.length > 0) {
      stats.value[3].value = scores[scores.length - 1].totalScore
    }
  } catch (e) { /* 忽略加载错误 */ }
}

const renderLineChart = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId || !lineChartRef.value) return
  try {
    const scores = await getMyGrowthScores()
    const chart = echarts.init(lineChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: (scores || []).map(s => s.termYear) },
      yAxis: { type: 'value', min: 0, max: 100 },
      series: [{ name: '综合成长分', type: 'line', smooth: true,
        data: (scores || []).map(s => s.totalScore),
        itemStyle: { color: '#409EFF' }, areaStyle: {} }]
    })
  } catch (e) { /* 忽略 */ }
}

const renderRadarChart = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId || !radarChartRef.value) return
  try {
    const data = await getRadarData(userId)
    const chart = echarts.init(radarChartRef.value)
    chart.setOption({
      radar: {
        indicator: [
          { name: '学业', max: 100 }, { name: '获奖', max: 100 },
          { name: '科研', max: 100 }, { name: '实践', max: 100 },
          { name: '综合', max: 100 }
        ]
      },
      series: [{
        type: 'radar',
        data: [{
          value: [
            data?.academic || 0, data?.award || 0,
            data?.research || 0, data?.practice || 0, data?.total || 0
          ],
          name: '能力分布',
          areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#409EFF' }
        }]
      }]
    })
  } catch (e) { /* 忽略 */ }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  renderLineChart()
  renderRadarChart()
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
.stat-row { margin-bottom: 4px; }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 8px; }
.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-num { font-size: 28px; font-weight: bold; color: #303133; }
.stat-label { font-size: 13px; color: #909399; margin-top: 4px; }

.todo-list {
  max-height: 260px;
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
