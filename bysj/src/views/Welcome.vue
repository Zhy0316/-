<template>
  <div class="welcome-container">
    <div class="welcome-card">
      <el-card>
        <template #header>
          <div class="card-header">
            <span>欢迎使用学生档案管理系统</span>
          </div>
        </template>
        
        <div class="user-details">
          <h3>用户信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="用户名">{{ userInfo?.username }}</el-descriptions-item>
            <el-descriptions-item label="真实姓名">{{ userInfo?.realName }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ userInfo?.phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ userInfo?.email }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ userInfo?.roleName }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatDate(userInfo?.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="system-info" style="margin-top: 20px;">
           <el-alert
             title="系统公告"
             type="info"
             description="欢迎使用学生档案管理系统 Beta 1.0 版本。请在左侧菜单选择功能模块进行操作。"
             show-icon
           />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useUserStore } from '../stores/user.js'
import { storeToRefs } from 'pinia'

const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString()
}
</script>

<style scoped>
.welcome-container {
  padding: 20px;
}
</style>
