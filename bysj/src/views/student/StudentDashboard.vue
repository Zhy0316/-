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
        <el-menu-item index="/student/profile">
          <el-icon><User /></el-icon><span>个人档案</span>
        </el-menu-item>
        <el-menu-item index="/student/academic">
          <el-icon><Reading /></el-icon><span>学业成绩</span>
        </el-menu-item>
        <el-menu-item index="/student/record">
          <el-icon><Notebook /></el-icon><span>成长记录</span>
        </el-menu-item>
        <el-menu-item index="/student/portfolio">
          <el-icon><Collection /></el-icon><span>作品集</span>
        </el-menu-item>
        <el-menu-item index="/student/growth">
          <el-icon><TrendCharts /></el-icon><span>成长评分</span>
        </el-menu-item>
        <el-menu-item index="/student/recruitment">
          <el-icon><Briefcase /></el-icon><span>招聘信息</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <span class="username">{{ userStore.userInfo?.realName }}</span>
          <el-button text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
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
              <el-card header="绩点趋势">
                <div ref="lineChartRef" style="height:260px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
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
import { useUserStore } from '../../stores/user.js'
import { getCollegeOverview, getRadarData, getMyGrowthScores } from '../../services/stats.js'
import { getAcademicRecords } from '../../services/academic.js'
import * as echarts from 'echarts'
import {
  House, User, Reading, Notebook, Collection,
  TrendCharts, Briefcase, SwitchButton, School
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const lineChartRef = ref()
const radarChartRef = ref()

const stats = ref([
  { label: '学业成绩数', value: 0, icon: 'Reading',    color: '#409EFF' },
  { label: '获奖记录',   value: 0, icon: 'Trophy',     color: '#E6A23C' },
  { label: '科研项目',   value: 0, icon: 'Aim',        color: '#67C23A' },
  { label: '综合成长分', value: 0, icon: 'TrendCharts', color: '#F56C6C' },
])

const pageTitle = computed(() => {
  const map = {
    '/student/home': '首页概览', '/student/profile': '个人档案',
    '/student/academic': '学业成绩', '/student/record': '成长记录',
    '/student/portfolio': '作品集', '/student/growth': '成长评分',
    '/student/recruitment': '招聘信息'
  }
  return map[route.path] || '学生端'
})

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
</style>
