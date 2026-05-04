<template>
  <div class="recruit-manage">

    <div class="page-header">
      <span class="page-title">招聘管理</span>
      <el-button type="primary" :icon="Plus" @click="openDialog()">发布招聘</el-button>
    </div>

    <!-- 职位列表 -->
    <el-row :gutter="16">
      <el-col v-for="job in jobs" :key="job.id" :span="12" style="margin-bottom:16px">
        <el-card shadow="hover" class="job-card">
          <div class="job-card-header">
            <div>
              <div class="job-name">{{ job.title }}</div>
              <div class="job-pos">{{ job.position }}
                <el-tag size="small" :type="jobTypeColor(job.jobType)" style="margin-left:6px">
                  {{ job.jobType || '全职' }}
                </el-tag>
              </div>
            </div>
            <el-tag :type="job.status === 1 ? 'success' : 'info'" size="small">
              {{ job.status === 1 ? '招聘中' : '已结束' }}
            </el-tag>
          </div>
          <div class="job-card-meta">
            <span>💰 {{ job.salaryRange || '薪资面议' }}</span>
            <span>📍 {{ job.location || '地点未填' }}</span>
            <span>🎓 {{ job.education || '本科' }}</span>
            <span>👥 招 {{ job.headcount || 1 }} 人</span>
            <span v-if="job.deadline">⏰ {{ job.deadline }}</span>
          </div>
          <div class="job-card-footer">
            <span style="font-size:12px;color:#c0c4cc">{{ formatTime(job.createTime) }} 发布</span>
            <div style="display:flex;gap:8px">
              <el-button size="small" @click="viewApps(job)">查看投递</el-button>
              <el-button size="small" @click="openDialog(job)">编辑</el-button>
              <el-button size="small" type="warning" v-if="job.status === 1"
                @click="handleClose(job.id)">关闭</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!jobs.length" description="暂无招聘信息，点击「发布招聘」开始" />

    <!-- 发布/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑招聘' : '发布招聘'" width="640px">
      <el-form :model="form" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="招聘标题" required>
              <el-input v-model="form.title" placeholder="如：2025届校园招聘-后端工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="职位类型">
              <el-select v-model="form.jobType" style="width:100%">
                <el-option label="全职" value="全职" />
                <el-option label="兼职" value="兼职" />
                <el-option label="实习" value="实习" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="职位名称" required>
              <el-input v-model="form.position" placeholder="如：Java后端工程师" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="薪资范围">
              <el-input v-model="form.salaryRange" placeholder="如：10k-15k" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工作地点">
              <el-input v-model="form.location" placeholder="如：北京·朝阳区" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="招聘人数">
              <el-input-number v-model="form.headcount" :min="1" :max="999" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="截止日期">
              <el-date-picker v-model="form.deadline" type="date" style="width:100%"
                value-format="YYYY-MM-DD" placeholder="选择日期" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学历要求">
              <el-select v-model="form.education" style="width:100%">
                <el-option label="不限"   value="不限" />
                <el-option label="大专"   value="大专" />
                <el-option label="本科"   value="本科" />
                <el-option label="硕士"   value="硕士" />
                <el-option label="博士"   value="博士" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工作经验">
              <el-select v-model="form.experience" style="width:100%">
                <el-option label="不限"     value="不限" />
                <el-option label="应届生"   value="应届生" />
                <el-option label="1年以内"  value="1年以内" />
                <el-option label="1-3年"   value="1-3年" />
                <el-option label="3-5年"   value="3-5年" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任职要求">
          <el-input v-model="form.requirement" type="textarea" :rows="3"
            placeholder="列出核心技能要求，如：熟悉Java/Spring Boot，有项目经验优先" />
        </el-form-item>
        <el-form-item label="职位描述">
          <el-input v-model="form.description" type="textarea" :rows="4"
            placeholder="详细描述工作内容、团队情况、发展空间等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handlePublish">
          {{ editingId ? '保存修改' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 投递管理弹窗 -->
    <el-dialog v-model="appsVisible" :title="'投递管理 — ' + currentJob?.title" width="860px">
      <div class="apps-toolbar">
        <el-radio-group v-model="filterStatus" @change="filterApps" size="small">
          <el-radio-button :label="-1">全部({{ applications.length }})</el-radio-button>
          <el-radio-button :label="0">待筛选</el-radio-button>
          <el-radio-button :label="1">简历通过</el-radio-button>
          <el-radio-button :label="2">面试邀请</el-radio-button>
          <el-radio-button :label="3">录用</el-radio-button>
          <el-radio-button :label="4">淘汰</el-radio-button>
        </el-radio-group>
      </div>

      <el-table :data="filteredApps" stripe size="small">
        <el-table-column prop="realName"  label="姓名"  width="90" />
        <el-table-column prop="college"   label="学院"  width="120" />
        <el-table-column prop="major"     label="专业"  min-width="110" />
        <el-table-column prop="message"   label="留言"  width="120" show-overflow-tooltip />
        <el-table-column prop="applyTime" label="投递时间" width="140">
          <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="流程状态" width="120">
          <template #default="{ row }">
            <el-tag :type="finalStatusType(row.finalStatus)" size="small">
              {{ finalStatusLabel(row.finalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.resumeFile" size="small" text type="primary"
              @click="openFile(row.resumeFile)">简历</el-button>
            <el-button size="small" text type="success"
              @click="contactStudent(row)">消息</el-button>
            <el-dropdown size="small" @command="(cmd) => handleStatusChange(row, cmd)">
              <el-button size="small" type="primary" plain>
                推进 <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :command="1">✅ 简历通过</el-dropdown-item>
                  <el-dropdown-item :command="2">📅 邀请面试</el-dropdown-item>
                  <el-dropdown-item :command="3">🎉 录用</el-dropdown-item>
                  <el-dropdown-item :command="4" divided>❌ 淘汰</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!filteredApps.length" description="暂无投递记录" />
    </el-dialog>

    <!-- 面试邀请弹窗 -->
    <el-dialog v-model="interviewVisible" title="发送面试邀请" width="420px">
      <el-form label-width="90px">
        <el-form-item label="面试时间">
          <el-date-picker v-model="interviewForm.time" type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择面试时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="面试备注">
          <el-input v-model="interviewForm.note" type="textarea" :rows="3"
            placeholder="面试地点、注意事项等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="interviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="interviewSaving" @click="confirmInterview">确认发送</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowDown } from '@element-plus/icons-vue'
import {
  publishRecruitment, getMyRecruitments, closeRecruitment,
  getApplicationsByRecruitment, updateFinalStatus
} from '../../services/recruitment.js'
import messageApi from '../../services/message.js'
import { api } from '../../services/api.js'

const router = useRouter()

const jobs = ref([])
const dialogVisible = ref(false)
const appsVisible = ref(false)
const saving = ref(false)
const editingId = ref(null)
const currentJob = ref(null)
const applications = ref([])
const filterStatus = ref(-1)
const interviewVisible = ref(false)
const interviewSaving = ref(false)
const interviewForm = ref({ time: '', note: '' })
const pendingInterviewRow = ref(null)

const form = ref({
  title: '', position: '', salaryRange: '', location: '',
  jobType: '全职', headcount: 1, deadline: '', education: '本科',
  experience: '应届生', requirement: '', description: ''
})

const filteredApps = computed(() => {
  if (filterStatus.value === -1) return applications.value
  return applications.value.filter(a => (a.finalStatus ?? 0) === filterStatus.value)
})

const loadJobs = async () => {
  jobs.value = await getMyRecruitments().catch(() => []) || []
}

const openDialog = (job = null) => {
  if (job) {
    editingId.value = job.id
    Object.assign(form.value, job)
  } else {
    editingId.value = null
    form.value = {
      title: '', position: '', salaryRange: '', location: '',
      jobType: '全职', headcount: 1, deadline: '', education: '本科',
      experience: '应届生', requirement: '', description: ''
    }
  }
  dialogVisible.value = true
}

const handlePublish = async () => {
  if (!form.value.title || !form.value.position) return ElMessage.warning('请填写标题和职位')
  saving.value = true
  try {
    if (editingId.value) {
      await api.put(`/recruitment/${editingId.value}`, form.value)
      ElMessage.success('修改成功')
    } else {
      await publishRecruitment(form.value)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    loadJobs()
  } catch { ElMessage.error('操作失败') } finally {
    saving.value = false
  }
}

const handleClose = async (id) => {
  await ElMessageBox.confirm('确定关闭该招聘？', '提示', { type: 'warning' })
  await closeRecruitment(id)
  ElMessage.success('已关闭')
  loadJobs()
}

const viewApps = async (job) => {
  currentJob.value = job
  filterStatus.value = -1
  applications.value = await getApplicationsByRecruitment(job.id).catch(() => []) || []
  appsVisible.value = true
}

const filterApps = () => {} // computed 自动处理

const handleStatusChange = async (row, cmd) => {
  if (cmd === 2) {
    // 面试邀请需要填写时间
    pendingInterviewRow.value = row
    interviewForm.value = { time: '', note: '' }
    interviewVisible.value = true
    return
  }
  await updateFinalStatus(row.applicationId, cmd, null, null)
  row.finalStatus = cmd
  ElMessage.success('状态已更新')
}

const confirmInterview = async () => {
  const row = pendingInterviewRow.value
  if (!row) return
  interviewSaving.value = true
  try {
    await updateFinalStatus(row.applicationId, 2, interviewForm.value.time, interviewForm.value.note)
    row.finalStatus = 2
    row.interviewTime = interviewForm.value.time
    row.interviewNote = interviewForm.value.note
    // 同时发消息通知学生
    if (row.studentUserId) {
      const msg = `您好！您投递的「${currentJob.value?.title}」职位已通过简历筛选，` +
        `面试时间：${interviewForm.value.time || '待定'}。${interviewForm.value.note || ''}`
      await messageApi.send(row.studentUserId, msg).catch(() => {})
    }
    ElMessage.success('面试邀请已发送')
    interviewVisible.value = false
  } catch { ElMessage.error('操作失败') } finally {
    interviewSaving.value = false
  }
}

const contactStudent = async (row) => {
  if (!row.studentUserId) { ElMessage.warning('暂无联系信息'); return }
  try {
    await messageApi.send(row.studentUserId, `您好，关于您投递的「${currentJob.value?.title}」职位，我们想进一步沟通。`)
    ElMessage.success('消息已发送')
    router.push('/hr/message')
  } catch { ElMessage.error('发送失败') }
}

const openFile = (path) => window.open('http://localhost:8083' + path, '_blank')

const finalStatusLabel = (s) => ['待筛选', '简历通过', '面试邀请', '录用', '淘汰'][s ?? 0] || '待筛选'
const finalStatusType  = (s) => ['info', 'primary', 'warning', 'success', 'danger'][s ?? 0] || 'info'
const jobTypeColor = (t) => ({ '全职': '', '兼职': 'warning', '实习': 'success' }[t] || '')

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}

onMounted(loadJobs)
</script>

<style scoped>
.recruit-manage { padding: 4px; }
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.page-title { font-size: 18px; font-weight: 600; }

.job-card { height: 100%; }
.job-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}
.job-name { font-size: 16px; font-weight: 600; color: #303133; margin-bottom: 4px; }
.job-pos  { font-size: 13px; color: #606266; }
.job-card-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 12px;
  padding: 8px 0;
  border-top: 1px solid #f5f5f5;
  border-bottom: 1px solid #f5f5f5;
}
.job-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.apps-toolbar { margin-bottom: 16px; }
</style>
