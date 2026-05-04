<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="24"><Setting /></el-icon>
        <span>管理员后台</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529"
        text-color="#ffffffa0" active-text-color="#fff">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>数据概览</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon><span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/enterprise">
          <el-icon><OfficeBuilding /></el-icon><span>企业审核</span>
        </el-menu-item>
        <el-menu-item index="/admin/ranking">
          <el-icon><Trophy /></el-icon><span>成长排行</span>
        </el-menu-item>
        <el-menu-item index="/admin/report">
          <el-icon><Download /></el-icon><span>报表导出</span>
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
        <!-- 数据概览 -->
        <template v-if="$route.path === '/admin/dashboard' || $route.path === '/admin'">
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
              <el-card header="绩点分布">
                <div ref="barRef" style="height:280px"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card header="获奖级别分布">
                <div ref="pieRef" style="height:280px"></div>
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
import { useUserStore } from '../../stores/user.js'
import { getAdminDashboard, getGpaDistribution, getAwardDistribution } from '../../services/stats.js'
import * as echarts from 'echarts'
import {
  Setting, DataAnalysis, User, OfficeBuilding, Trophy, Download, SwitchButton
} from '@element-plus/icons-vue'
import UserDropdown from '../../components/Layout/UserDropdown.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const barRef = ref()
const pieRef = ref()

const stats = ref([
  { label: '学生总数', value: 0, icon: 'User',         color: '#409EFF' },
  { label: '平均绩点', value: 0, icon: 'Reading',      color: '#67C23A' },
  { label: '获奖总数', value: 0, icon: 'Trophy',       color: '#E6A23C' },
  { label: '科研项目', value: 0, icon: 'Aim',          color: '#F56C6C' },
])

const pageTitle = computed(() => {
  const map = {
    '/admin/dashboard': '数据概览', '/admin/users': '用户管理',
    '/admin/enterprise': '企业审核', '/admin/ranking': '成长排行',
    '/admin/report': '报表导出'
  }
  return map[route.path] || '管理员后台'
})

const handleLogout = () => { userStore.logout(); router.push('/login') }

const loadStats = async () => {
  try {
    const data = await getAdminDashboard()
    stats.value[0].value = data.studentCount || 0
    stats.value[1].value = data.avgGpa || 0
    stats.value[2].value = data.awardCount || 0
    stats.value[3].value = data.researchCount || 0
  } catch (e) { /* 忽略 */ }
}

const renderBar = async () => {
  if (!barRef.value) return
  try {
    const data = await getGpaDistribution()
    echarts.init(barRef.value).setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.labels },
      yAxis: { type: 'value' },
      series: [{ name: '人数', type: 'bar', data: data.data,
        itemStyle: { color: '#409EFF' }, barMaxWidth: 50 }]
    })
  } catch (e) { /* 忽略 */ }
}

const renderPie = async () => {
  if (!pieRef.value) return
  try {
    const data = await getAwardDistribution()
    echarts.init(pieRef.value).setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { bottom: 0 },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: data,
        label: { formatter: '{b}\n{d}%' }
      }]
    })
  } catch (e) { /* 忽略 */ }
}

onMounted(async () => {
  await loadStats()
  await nextTick()
  renderBar()
  renderPie()
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
