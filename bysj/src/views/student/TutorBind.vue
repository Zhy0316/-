<template>
  <div class="tutor-bind-page">

    <!-- 当前绑定状态 -->
    <el-card style="margin-bottom:20px">
      <template #header>我的导师</template>
      <div v-if="boundTutor">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ boundTutor.realName }}</el-descriptions-item>
          <el-descriptions-item label="职称">{{ boundTutor.title }}</el-descriptions-item>
          <el-descriptions-item label="研究方向" :span="2">{{ boundTutor.researchField }}</el-descriptions-item>
        </el-descriptions>
        <el-tag type="success" style="margin-top:12px">已绑定</el-tag>
      </div>
      <div v-else-if="pendingRelation">
        <el-alert type="warning" :closable="false"
          :title="`已向「${pendingRelation.tutorName || '导师'}」发送申请，等待审核中...`" />
      </div>
      <el-empty v-else description="尚未绑定导师，请从下方选择" />
    </el-card>

    <!-- 导师列表 -->
    <el-card v-if="!boundTutor">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>选择导师</span>
          <el-input v-model="keyword" placeholder="搜索姓名/研究方向"
            style="width:220px" clearable @input="filterTutors">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-table :data="filteredTutors" stripe v-loading="loading">
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="researchField" label="研究方向" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              type="primary" size="small"
              :disabled="!!pendingRelation"
              @click="applyBind(row)"
            >申请绑定</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredTutors.length === 0" description="暂无导师数据" />
    </el-card>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'

const userStore = useUserStore()
const loading = ref(false)
const tutors = ref([])
const keyword = ref('')
const boundTutor = ref(null)
const pendingRelation = ref(null)

const filteredTutors = computed(() => {
  if (!keyword.value) return tutors.value
  const kw = keyword.value.toLowerCase()
  return tutors.value.filter(t =>
    (t.realName || '').toLowerCase().includes(kw) ||
    (t.researchField || '').toLowerCase().includes(kw)
  )
})

const loadTutors = async () => {
  loading.value = true
  try {
    tutors.value = await api.get('/student/tutors') || []
  } catch {
    ElMessage.error('加载导师列表失败')
  } finally {
    loading.value = false
  }
}

const loadMyRelations = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    const relations = await api.get(`/relation/student/${userId}`) || []
    // status=1 已绑定
    const bound = relations.find(r => r.status === 1)
    if (bound) {
      // 查导师信息
      const tutorList = await api.get('/student/tutors') || []
      boundTutor.value = tutorList.find(t => t.userId === bound.tutorId) || { realName: '导师' + bound.tutorId }
      return
    }
    // status=0 待审核
    const pending = relations.find(r => r.status === 0)
    if (pending) {
      const tutorList = tutors.value.length ? tutors.value : (await api.get('/student/tutors') || [])
      const t = tutorList.find(t => t.userId === pending.tutorId)
      pendingRelation.value = { ...pending, tutorName: t?.realName }
    }
  } catch {
    // 忽略
  }
}

const applyBind = async (tutor) => {
  try {
    await ElMessageBox.confirm(
      `确定申请绑定导师「${tutor.realName}」吗？`,
      '申请确认', { type: 'info' }
    )
    await api.post('/relation/apply', {
      studentId: userStore.userInfo.userId,
      tutorId: tutor.userId
    })
    ElMessage.success('申请已发送，等待导师审核')
    pendingRelation.value = { tutorName: tutor.realName }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error(e?.response?.data || '申请失败')
  }
}

onMounted(async () => {
  await loadTutors()
  await loadMyRelations()
})
</script>

<style scoped>
.tutor-bind-page { padding: 4px; }
</style>
