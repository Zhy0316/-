<template>
  <div>
    <div style="margin-bottom:16px;font-size:16px;font-weight:600">我发表的评语</div>
    <el-select v-model="filterStudentId" placeholder="按学生筛选" clearable
      style="width:200px;margin-bottom:16px" @change="loadComments">
      <el-option v-for="s in myStudents" :key="s.studentId"
        :label="s.realName || ('学生'+s.studentId)" :value="s.studentId" />
    </el-select>

    <el-table :data="comments" stripe>
      <el-table-column prop="studentId"  label="学生ID"  width="100" />
      <el-table-column prop="targetType" label="类型"    width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="typeMap[row.targetType]?.type">
            {{ typeMap[row.targetType]?.label || row.targetType }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="content"    label="评语内容" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createTime" label="时间"    width="160" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!comments.length" description="暂无评语记录" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'
import { deleteComment } from '../../services/comment.js'

const userStore = useUserStore()
const comments = ref([])
const myStudents = ref([])
const filterStudentId = ref(null)

const typeMap = {
  GENERAL:   { label: '综合评价', type: '' },
  DIARY:     { label: '日记批注', type: 'success' },
  PORTFOLIO: { label: '作品点评', type: 'warning' }
}

const loadStudents = async () => {
  const tutorId = userStore.userInfo?.userId
  if (!tutorId) return
  const relations = await api.get(`/relation/tutor/${tutorId}`).catch(() => []) || []
  myStudents.value = (relations || []).filter(r => r.status === 1)
}

const loadComments = async () => {
  const tutorId = userStore.userInfo?.userId
  if (!tutorId) return
  try {
    if (filterStudentId.value) {
      comments.value = await api.get('/comment/my', {
        params: { studentId: filterStudentId.value }
      }) || []
    } else {
      // 加载所有学生的评语
      const all = []
      for (const s of myStudents.value) {
        const res = await api.get('/comment/my', { params: { studentId: s.studentId } }).catch(() => [])
        all.push(...(res || []))
      }
      comments.value = all.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
    }
  } catch (e) {
    ElMessage.error('加载评语失败')
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该评语？', '提示', { type: 'warning' })
  await deleteComment(id)
  ElMessage.success('删除成功')
  loadComments()
}

onMounted(async () => {
  await loadStudents()
  loadComments()
})
</script>
