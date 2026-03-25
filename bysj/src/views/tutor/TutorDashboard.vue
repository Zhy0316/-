<template>
  <div class="tutor-dashboard">
    <div class="header">
      <h2>教师管理系统</h2>
      <div class="user-info">
        <span>欢迎，{{ userInfo?.realName }}</span>
        <el-button type="primary" @click="logout">退出登录</el-button>
      </div>
    </div>
    
    <div class="main-content">
      <el-container>
        <el-aside width="200px" class="sidebar">
          <el-menu
            :default-active="activeMenu"
            class="sidebar-menu"
            @select="handleMenuSelect"
          >
            <el-menu-item index="home">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="students">
              <el-icon><User /></el-icon>
              <span>我的学生</span>
            </el-menu-item>
            <el-menu-item index="pending">
              <el-icon><Bell /></el-icon>
              <span>待审核申请</span>
            </el-menu-item>
            <el-menu-item index="profile">
              <el-icon><UserFilled /></el-icon>
              <span>个人资料</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { House, User, Bell, UserFilled } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const activeMenu = ref('home');

const userInfo = userStore.userInfo;

const handleMenuSelect = (index) => {
  activeMenu.value = index;
  switch (index) {
    case 'home':
      router.push('/tutor/home');
      break;
    case 'students':
      router.push('/tutor/students');
      break;
    case 'pending':
      router.push('/tutor/pending');
      break;
    case 'profile':
      router.push('/tutor/profile');
      break;
  }
};

const logout = () => {
  userStore.clearUserInfo();
  router.push('/login');
};

onMounted(() => {
  if (!userStore.userInfo) {
    router.push('/login');
  }
});
</script>

<style scoped>
.tutor-dashboard {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
  background-color: #409EFF;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header h2 {
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.main-content {
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #f5f7fa;
}

.sidebar-menu {
  height: 100%;
  border-right: none;
}

.el-main {
  padding: 20px;
  background-color: white;
}
</style>
