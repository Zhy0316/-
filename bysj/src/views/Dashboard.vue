<template>
  <div class="dashboard-container">
    <el-container class="layout-container">
      <el-header class="header">
        <div class="header-content">
          <h1>学生档案管理系统</h1>
          <div class="user-info">
            <span>欢迎，{{ userInfo?.realName || userInfo?.username }}</span>
            <span class="role-tag">{{ userInfo?.roleName }}</span>
            <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
          </div>
        </div>
      </el-header>
      
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            router
          >
            <el-menu-item index="/dashboard">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-sub-menu index="1" v-if="userInfo?.roleKey === 'student'">
              <template #title>
                <el-icon><User /></el-icon>
                <span>学生功能</span>
              </template>
              <el-menu-item index="/dashboard/student/profile">个人主页</el-menu-item>
              <el-menu-item index="/dashboard/student/academic">学业成绩</el-menu-item>
              <el-menu-item index="/dashboard/student/record">成长记录</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="2" v-if="userInfo?.roleKey === 'tutor'">
              <template #title>
                <el-icon><School /></el-icon>
                <span>导师功能</span>
              </template>
              <el-menu-item index="2-1">学生管理</el-menu-item>
              <el-menu-item index="2-2">评语管理</el-menu-item>
              <el-menu-item index="2-3">成长评估</el-menu-item>
            </el-sub-menu>
            
            <el-sub-menu index="3" v-if="userInfo?.roleKey === 'hr'">
              <template #title>
                <el-icon><OfficeBuilding /></el-icon>
                <span>企业功能</span>
              </template>
              <el-menu-item index="3-1">学生浏览</el-menu-item>
              <el-menu-item index="3-2">招聘管理</el-menu-item>
              <el-menu-item index="3-3">申请处理</el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-aside>
        
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  House, User, School, OfficeBuilding 
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user.js'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const userInfo = computed(() => userStore.userInfo)

const formatDate = (dateString) => {
  if (!dateString) return ''
  return new Date(dateString).toLocaleString('zh-CN')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    userStore.clearUser()
    ElMessage.success('退出成功')
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}

onMounted(() => {
  userStore.loadUserFromStorage()
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.header {
  background: #409eff;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-content h1 {
  margin: 0;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.role-tag {
  background: rgba(255, 255, 255, 0.2);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.sidebar {
  background: #f5f7fa;
}

.sidebar-menu {
  border: none;
  height: 100%;
}

.main-content {
  padding: 20px;
  background: #f0f2f5;
}

.welcome-card {
  max-width: 1200px;
  margin: 0 auto;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.user-details {
  margin-bottom: 20px;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.feature-header .el-icon {
  font-size: 18px;
}
</style>