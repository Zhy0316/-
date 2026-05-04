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
        <el-table-column prop="realName"      label="姓名"   width="110" />
        <el-table-column prop="title"         label="职称"   width="120" />
        <el-table-column prop="researchField" label="研究方向" />
        <el-table-column prop="college"       label="学院"   width="130" />
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button size="small" @click="viewTutor(row)">查看详情</el-button>
            <el-button
              type="primary" size="small"
              :disabled="!!pendingRelation"
              @click="openApply(row)"
            >申请绑定</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && filteredTutors.length === 0" description="暂无导师数据" />
    </el-card>

    <!-- 导师详情弹窗 -->
    <el-dialog v-model="tutorDetailVisible" :title="currentTutor?.realName + ' 导师'" width="500px">
      <el-descriptions :column="1" border v-if="currentTutor">
        <el-descriptions-item label="姓名">{{ currentTutor.realName }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentTutor.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentTutor.college || '-' }}</el-descriptions-item>
        <el-descriptions-item label="研究方向">{{ currentTutor.researchField || '-' }}</el-descriptions-item>
        <el-descriptions-item label="个人简介">{{ currentTutor.bio || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="tutorDetailVisible = false">关闭</el-button>
        <el-button type="primary" :disabled="!!pendingRelation" @click="openApply(currentTutor); tutorDetailVisible = false">
          申请绑定
        </el-button>
      </template>
    </el-dialog>

    <!-- 申请绑定弹窗 -->
    <el-dialog v-model="applyVisible" :title="'申请绑定 — ' + applyTarget?.realName" width="520px">
      <el-form label-width="80px">
        <el-form-item label="自荐信">
          <el-input
            v-model="applyNote"
            type="textarea"
            :rows="5"
            placeholder="向导师介绍自己，说明申请原因、研究兴趣、未来规划等（建议200字以上）"
            maxlength="1000"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="() => attachFile = null"
            accept=".pdf,.doc,.docx,.jpg,.png"
          >
            <el-button size="small">上传证明材料</el-button>
            <template #tip>
              <div style="color:#909399;font-size:12px">可上传成绩单、获奖证书等（PDF/Word/图片）</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

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

const tutorDetailVisible = ref(false)
const currentTutor = ref(null)
const applyVisible = ref(false)
const applyTarget = ref(null)
const applyNote = ref('')
const attachFile = ref(null)
const applying = ref(false)
const uploadRef = ref(null)

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
    const bound = relations.find(r => r.status === 1)
    if (bound) {
      const tutorList = tutors.value.length ? tutors.value : (await api.get('/student/tutors') || [])
      boundTutor.value = tutorList.find(t => t.userId === bound.tutorId) || { realName: '导师' }
      return
    }
    const pending = relations.find(r => r.status === 0)
    if (pending) {
      const tutorList = tutors.value.length ? tutors.value : (await api.get('/student/tutors') || [])
      const t = tutorList.find(t => t.userId === pending.tutorId)
      pendingRelation.value = { ...pending, tutorName: t?.realName }
    }
  } catch { /* 忽略 */ }
}

const viewTutor = (tutor) => {
  currentTutor.value = tutor
  tutorDetailVisible.value = true
}

const openApply = (tutor) => {
  applyTarget.value = tutor
  applyNote.value = ''
  attachFile.value = null
  uploadRef.value?.clearFiles()
  applyVisible.value = true
}

const onFileChange = (file) => { attachFile.value = file.raw }

const submitApply = async () => {
  if (!applyNote.value.trim()) {
    return ElMessage.warning('请填写自荐信，让导师了解你')
  }
  applying.value = true
  try {
    const fd = new FormData()
    fd.append('tutorId', applyTarget.value.userId)
    fd.append('applyNote', applyNote.value)
    if (attachFile.value) fd.append('attachFile', attachFile.value)

    await api.post('/relation/apply', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    ElMessage.success('申请已发送，等待导师审核')
    applyVisible.value = false
    pendingRelation.value = { tutorName: applyTarget.value.realName }
  } catch (e) {
    ElMessage.error(e?.message || '申请失败')
  } finally {
    applying.value = false
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
