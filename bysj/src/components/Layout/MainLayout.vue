<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon size="22"><component :is="logoIcon" /></el-icon>
        <span>{{ title }}</span>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#ffffffa0"
        active-text-color="#ffffff"
      >
        <el-menu-item
          v-for="item in menus"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <el-header class="header">
        <span class="page-title">{{ currentTitle }}</span>
        <div class="header-right">
          <span class="username">{{ userStore.userInfo?.realName }}（{{ roleLabel }}）</span>
          <el-button text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </el-header>
      <el-main class="main">
        <slot />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../../stores/user.js'
import { SwitchButton } from '@element-plus/icons-vue'

const props = defineProps({
  menus: { type: Array, default: () => [] } // [{ path, label, icon }]
})

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const roleMap = {
  student: { label: '学生', icon: 'User', title: '学生成长档案' },
  tutor:   { label: '导师', icon: 'UserFilled', title: '导师管理平台' },
  hr:      { label: '企业HR', icon: 'OfficeBuilding', title: '企业招聘平台' },
  admin:   { label: '管理员', icon: 'Setting', title: '系统管理后台' }
}

const roleKey = computed(() => userStore.roleKey)
const roleLabel = computed(() => roleMap[roleKey.value]?.label || '')
const logoIcon = computed(() => roleMap[roleKey.value]?.icon || 'User')
const title = computed(() => roleMap[roleKey.value]?.title || '档案系统')

const currentTitle = computed(() => {
  const matched = props.menus.find(m => route.path.startsWith(m.path))
  return matched?.label || title.value
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #001529; overflow: hidden; flex-shrink: 0; }
.logo {
  height: 64px; display: flex; align-items: center; justify-content: center;
  gap: 10px; color: #fff; font-size: 15px; font-weight: bold;
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
</style>
