<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span style="font-size:16px;font-weight:600">实习经历</span>
      <el-button type="primary" @click="practiceDialogVisible = true">+ 添加实习经历</el-button>
    </div>

    <div class="practice-list" v-if="practices.length > 0">
      <div class="practice-item" v-for="item in practices" :key="item.id">
        <div class="practice-header">
          <h5>{{ item.activityName }}</h5>
          <span class="practice-organization">{{ item.organization }}</span>
        </div>
        <div class="practice-meta">
          <span>{{ formatDate(item.startDate) }} 至 {{ formatDate(item.endDate) }}</span>
        </div>
        <div class="practice-content" v-html="item.content"></div>
        <div v-if="item.proofFile" class="practice-attachment">
          <el-link :href="`http://localhost:8083${item.proofFile}`"
            target="_blank" type="primary">
            📎 在线查看证明材料
          </el-link>
        </div>
        <div class="practice-actions">
          <el-button size="small" type="danger" @click="deletePractice(item.id)">删除</el-button>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无实习经历，快去添加吧" />

    <!-- 添加实习经历弹窗 -->
    <el-dialog title="添加实习经历" v-model="practiceDialogVisible" width="600px" destroy-on-close>
      <el-form :model="practiceForm" label-width="100px">
        <el-form-item label="实习主题" required>
          <el-input v-model="practiceForm.activityName" placeholder="请输入实习主题名称" />
        </el-form-item>
        <el-form-item label="实习单位" required>
          <el-input v-model="practiceForm.organization" placeholder="请输入实习单位/组织" />
        </el-form-item>
        <el-form-item label="实习时间">
          <div style="display:flex;gap:10px;align-items:center">
            <el-date-picker v-model="practiceForm.startDate" type="date"
              placeholder="开始日期" value-format="YYYY-MM-DD" style="width:180px" />
            <span>至</span>
            <el-date-picker v-model="practiceForm.endDate" type="date"
              placeholder="结束日期" value-format="YYYY-MM-DD" style="width:180px" />
          </div>
        </el-form-item>
        <el-form-item label="实习内容">
          <el-input v-model="practiceForm.content" type="textarea" :rows="4"
            placeholder="请输入实习内容和心得..." />
        </el-form-item>
        <el-form-item label="证明材料">
          <el-upload action="#" :auto-upload="false" :on-change="handlePracticeFileChange"
            :show-file-list="true" accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" :limit="1">
            <el-button size="small" type="primary">选择文件</el-button>
            <template #tip>
              <div style="font-size:12px;color:#909399;margin-top:4px">支持 JPG/PNG/PDF/Word，可不上传</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="practiceDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitPractice">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'

const userStore = useUserStore()
const practices = ref([])
const practiceDialogVisible = ref(false)
const saving = ref(false)
const practiceFile = ref(null)

const practiceForm = ref({
  activityName: '', organization: '',
  startDate: '', endDate: '', content: ''
})

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : ''

const fetchPractices = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  practices.value = await api.get(`/practice/list/${userId}`).catch(() => []) || []
}

const handlePracticeFileChange = (file) => { practiceFile.value = file.raw }

const submitPractice = async () => {
  if (!practiceForm.value.activityName || !practiceForm.value.organization) {
    ElMessage.warning('请填写实习主题和实习单位')
    return
  }
  saving.value = true
  try {
    const fd = new FormData()
    fd.append('studentId', userStore.userInfo.userId)
    fd.append('activityName', practiceForm.value.activityName)
    fd.append('organization', practiceForm.value.organization)
    fd.append('startDate', practiceForm.value.startDate || new Date().toISOString().split('T')[0])
    fd.append('endDate', practiceForm.value.endDate || new Date().toISOString().split('T')[0])
    fd.append('content', practiceForm.value.content || '')
    if (practiceFile.value) fd.append('file', practiceFile.value)

    await api.post('/practice/upload', fd)
    ElMessage.success('实习经历上传成功')
    practiceDialogVisible.value = false
    practiceForm.value = { activityName: '', organization: '', startDate: '', endDate: '', content: '' }
    practiceFile.value = null
    fetchPractices()
  } catch { /* api.js 已弹错误 */ } finally {
    saving.value = false
  }
}

const deletePractice = async (id) => {
  await ElMessageBox.confirm('确定要删除这条实习经历吗？', '警告', { type: 'warning' })
  await api.delete(`/practice/delete/${id}`)
  ElMessage.success('删除成功')
  fetchPractices()
}

onMounted(fetchPractices)
</script>

<style scoped>
.practice-list { display: flex; flex-direction: column; gap: 16px; }
.practice-item {
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 20px;
}
.practice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.practice-header h5 { margin: 0; color: #2c3e50; font-size: 16px; }
.practice-organization {
  background: #e3f2fd; color: #1976d2;
  padding: 3px 8px; border-radius: 4px; font-size: 12px;
}
.practice-meta { color: #7f8c8d; font-size: 13px; margin-bottom: 12px; }
.practice-content { line-height: 1.6; color: #2c3e50; margin-bottom: 12px; }
.practice-attachment { margin-bottom: 12px; }
.practice-actions { display: flex; justify-content: flex-end; }
</style>
