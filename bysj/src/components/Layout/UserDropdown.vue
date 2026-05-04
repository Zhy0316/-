<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <div class="user-trigger">
      <el-avatar
        :size="36"
        :src="avatarUrl"
        class="avatar"
      >
        {{ userStore.userInfo?.realName?.charAt(0) || userStore.userInfo?.username?.charAt(0) || '用户' }}
      </el-avatar>
      <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
      <el-icon class="arrow"><ArrowDown /></el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="editProfile">
          <el-icon><Edit /></el-icon>
          <span>编辑个人信息</span>
        </el-dropdown-item>
        <el-dropdown-item divided command="logout">
          <el-icon><SwitchButton /></el-icon>
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user.js'
import { ElMessageBox, ElMessage } from 'element-plus'
import { ArrowDown, Edit, SwitchButton } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const avatarUrl = computed(() => {
  return userStore.userInfo?.avatar || ''
})

const profilePath = computed(() => {
  const roleMap = {
    student: '/student/profile',
    tutor: '/tutor/profile',
    hr: '/hr/profile',
    admin: '/admin/profile'
  }
  return roleMap[userStore.roleKey] || '/student/profile'
})

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push(profilePath.value)
      break
    case 'editProfile':
      router.push(profilePath.value)
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        ElMessage.success('退出成功')
        router.push('/login')
      } catch {
      }
      break
  }
}
</script>

<style scoped>
.user-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-trigger:hover {
  background-color: #f5f7fa;
}

.avatar {
  flex-shrink: 0;
}

.username {
  font-size: 14px;
  color: #606266;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.arrow {
  font-size: 12px;
  color: #909399;
}
</style>
