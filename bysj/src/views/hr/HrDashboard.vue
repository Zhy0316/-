<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="24"><OfficeBuilding /></el-icon>
        <span>企业HR端</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529"
        text-color="#ffffffa0" active-text-color="#fff">
        <el-menu-item index="/hr/home">
          <el-icon><House /></el-icon><span>首页</span>
        </el-menu-item>
        <el-menu-item index="/hr/profile">
          <el-icon><OfficeBuilding /></el-icon><span>企业信息</span>
        </el-menu-item>
        <el-menu-item index="/hr/recruitment">
          <el-icon><Briefcase /></el-icon><span>招聘管理</span>
        </el-menu-item>
        <el-menu-item index="/hr/students">
          <el-icon><User /></el-icon><span>浏览学生</span>
        </el-menu-item>
        <el-menu-item index="/hr/applications">
          <el-icon><Document /></el-icon><span>投递管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <span class="page-title">{{ pageTitle }}</span>
        <div class="header-right">
          <el-tag v-if="auditStatus === 0" type="warning" size="small">待审核</el-tag>
          <el-tag v-else-if="auditStatus === 1" type="success" size="small">已认证</el-tag>
          <el-tag v-else-if="auditStatus === 2" type="danger" size="small">已驳回</el-tag>
          <span class="username">{{ userStore.userInfo?.realName }}</span>
          <el-button text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </el-header>

      <el-main class="main">
        <!-- HR首页 -->
        <template v-if="$route.path === '/hr/home' || $route.path === '/hr'">
          <el-alert v-if="auditStatus !== 1" type="warning" show-icon :closable="false"
            title="企业认证尚未通过，部分功能受限。请前往「企业信息」页面完善资料并等待管理员审核。"
            style="margin-bottom:20px" />
          <el-row :gutter="20">
            <el-col :span="8" v-for="s in stats" :key="s.label">
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
        </template>
        <router-view v-else />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user.js'
import { getEnterpriseInfo } from '../../services/enterprise.js'
import { getMyRecruitments } from '../../services/recruitment.js'
import {
  House, OfficeBuilding, Briefcase, User, Document, SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const auditStatus = ref(-1)

const stats = ref([
  { label: '发布招聘数', value: 0, icon: 'Briefcase',     color: '#409EFF' },
  { label: '收到投递数', value: 0, icon: 'Document',      color: '#67C23A' },
  { label: '认证状态',   value: '待审核', icon: 'OfficeBuilding', color: '#E6A23C' },
])

const pageTitle = computed(() => {
  const map = {
    '/hr/home': '首页', '/hr/profile': '企业信息',
    '/hr/recruitment': '招聘管理', '/hr/students': '浏览学生',
    '/hr/applications': '投递管理'
  }
  return map[route.path] || '企业HR端'
})

const handleLogout = () => { userStore.logout(); router.push('/login') }

const loadStats = async () => {
  try {
    const info = await getEnterpriseInfo()
    auditStatus.value = info?.auditStatus ?? -1
    const statusText = ['待审核', '已认证', '已驳回']
    stats.value[2].value = statusText[auditStatus.value] || '未知'

    const jobs = await getMyRecruitments().catch(() => []) || []
    stats.value[0].value = jobs.length
  } catch (e) { /* 忽略 */ }
}

onMounted(loadStats)
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
.header-right { display: flex; align-items: center; gap: 10px; }
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
