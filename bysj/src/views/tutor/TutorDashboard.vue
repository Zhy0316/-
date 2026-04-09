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
        <el-menu-item index="/tutor/profile">
          <el-icon><UserFilled /></el-icon><span>个人资料</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <span class="username">{{ userStore.userInfo?.realName }}（导师）</span>
          <el-button text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
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
          <el-card header="学生成长分分布">
            <div ref="barRef" style="height:300px"></div>
          </el-card>
        </template>
        <router-view v-else />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'
import * as echarts from 'echarts'
import {
  House, User, Bell, UserFilled, ChatDotRound,
  SwitchButton, School
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const barRef = ref()
const pendingCount = ref(0)

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
    '/tutor/profile': '个人资料'
  }
  return map[route.path] || '导师端'
})

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
</style>
