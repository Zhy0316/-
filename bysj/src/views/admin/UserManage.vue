<template>
  <div>
    <!-- 搜索栏 -->
    <el-row :gutter="12" style="margin-bottom:16px">
      <el-col :span="5">
        <el-input v-model="keyword" placeholder="搜索姓名/用户名" clearable @input="loadUsers" />
      </el-col>
      <el-col :span="4">
        <el-select v-model="roleKey" placeholder="角色筛选" clearable @change="loadUsers" style="width:100%">
          <el-option label="学生"    value="student" />
          <el-option label="导师"    value="tutor" />
          <el-option label="企业HR"  value="hr" />
          <el-option label="管理员"  value="admin" />
        </el-select>
      </el-col>
    </el-row>

    <el-table :data="users" stripe v-loading="loading">
      <el-table-column prop="userId"     label="ID"     width="80" />
      <el-table-column prop="username"   label="用户名"  width="130" />
      <el-table-column prop="realName"   label="姓名"    width="110" />
      <el-table-column prop="roleKey"    label="角色"    width="100">
        <template #default="{ row }">
          <el-tag :type="roleTagType(row.roleKey)" size="small">
            {{ roleLabel(row.roleKey) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone"      label="手机号"  width="130" />
      <el-table-column prop="email"      label="邮箱"    min-width="160" show-overflow-tooltip />
      <el-table-column prop="createTime" label="注册时间" width="160" />
      <el-table-column prop="status"     label="状态"    width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small"
            :type="row.status === 1 ? 'danger' : 'success'"
            @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top:16px;justify-content:flex-end;display:flex"
      @current-change="loadUsers"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, updateUserStatus } from '../../services/admin.js'

const users   = ref([])
const loading = ref(false)
const keyword = ref('')
const roleKey = ref('')
const page    = ref(1)
const size    = ref(15)
const total   = ref(0)

const roleLabel = (key) => ({ student: '学生', tutor: '导师', hr: '企业HR', admin: '管理员' }[key] || key)
const roleTagType = (key) => ({ student: '', tutor: 'success', hr: 'warning', admin: 'danger' }[key] || 'info')

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: size.value, roleKey: roleKey.value, keyword: keyword.value })
    users.value = res?.list || []
    total.value = res?.total || 0
  } catch (e) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  await ElMessageBox.confirm(`确定要${action}用户「${row.realName}」吗？`, '提示', { type: 'warning' })
  await updateUserStatus(row.userId, newStatus)
  ElMessage.success(`${action}成功`)
  loadUsers()
}

onMounted(loadUsers)
</script>
