<template>
  <div class="student-browse">

    <!-- 未认证拦截提示 -->
    <div v-if="auditStatus !== 1" class="no-cert-wall">
      <el-result
        :icon="auditStatus === 0 ? 'warning' : 'error'"
        :title="auditStatus === 0 ? '企业认证审核中' : '需要企业认证'"
        :sub-title="auditStatus === 0
          ? '您的企业认证申请正在审核中，通过后即可浏览学生档案。通常1-3个工作日内完成。'
          : '浏览学生档案需要完成企业认证。请前往「企业信息」页面完善资料并上传营业执照。'"
      >
        <template #extra>
          <el-button type="primary" @click="$router.push('/hr/profile')">
            {{ auditStatus === 0 ? '查看认证进度' : '去完成认证' }}
          </el-button>
        </template>
      </el-result>
    </div>

    <!-- 已认证：正常显示 -->
    <template v-else>
      <el-tabs v-model="activeTab">

        <!-- 搜索浏览 Tab -->
        <el-tab-pane label="浏览学生" name="browse">
          <div class="search-bar">
            <el-input v-model="keyword" placeholder="搜索姓名/学号" clearable
              style="width:200px" @input="loadStudents">
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-input v-model="college" placeholder="学院" clearable
              style="width:160px" @input="loadStudents" />
            <el-input v-model="major" placeholder="专业" clearable
              style="width:160px" @input="loadStudents" />
            <el-select v-model="minGpa" placeholder="最低GPA" clearable
              style="width:130px" @change="loadStudents">
              <el-option label="3.5以上" :value="3.5" />
              <el-option label="3.0以上" :value="3.0" />
              <el-option label="2.5以上" :value="2.5" />
            </el-select>
          </div>

          <el-row :gutter="16" v-loading="loading" style="margin-top:16px">
            <el-col v-for="s in students" :key="s.userId" :span="8" style="margin-bottom:16px">
              <el-card shadow="hover" class="student-card" @click="viewDetail(s)">
                <div class="sc-header">
                  <el-avatar :size="48" :src="s.avatar ? 'http://localhost:8083/uploads/' + s.avatar : ''">
                    {{ s.realName?.charAt(0) }}
                  </el-avatar>
                  <div class="sc-info">
                    <div class="sc-name">{{ s.realName }}</div>
                    <div class="sc-sub">{{ s.college }} · {{ s.major }}</div>
                  </div>
                </div>
                <div class="sc-tags">
                  <el-tag size="small" type="info">{{ s.enrollmentYear }}级</el-tag>
                  <el-tag size="small" type="success" v-if="s.avgGpa">GPA {{ s.avgGpa }}</el-tag>
                  <el-tag size="small" type="warning" v-if="s.awardCount > 0">获奖 {{ s.awardCount }}</el-tag>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="!loading && students.length === 0" description="暂无学生数据" />
        </el-tab-pane>

        <!-- AI 推荐 Tab -->
        <el-tab-pane label="🤖 AI 智能推荐" name="ai">
          <div class="ai-toolbar">
            <el-select v-model="selectedJobId" placeholder="选择职位进行推荐" style="width:260px">
              <el-option v-for="j in myJobs" :key="j.id" :label="j.title" :value="j.id" />
            </el-select>
            <el-button type="primary" :loading="recommending" :disabled="!selectedJobId"
              @click="doAiRecommend">AI 推荐匹配学生</el-button>
          </div>
          <div v-if="recommending" class="loading-box">
            <el-icon class="is-loading" size="36" color="#409EFF"><Loading /></el-icon>
            <div style="margin-top:12px;color:#909399">AI 正在分析学生档案，匹配最合适的候选人...</div>
          </div>
          <div v-else-if="aiRecommendResult" class="ai-result" v-html="renderMarkdown(aiRecommendResult)"></div>
          <el-empty v-else description="选择职位后点击「AI 推荐」" :image-size="80" />
        </el-tab-pane>

      </el-tabs>
    </template>

    <!-- 学生档案弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentStudent?.realName + ' 的档案'" width="640px">
      <el-descriptions :column="2" border v-if="currentStudent">
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentStudent.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentStudent.enrollmentYear }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentStudent.email }}</el-descriptions-item>
        <el-descriptions-item label="平均GPA">
          <el-tag type="success">{{ currentStudent.avgGpa || '-' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="获奖数">{{ currentStudent.awardCount || 0 }} 项</el-descriptions-item>
      </el-descriptions>
      <div style="margin-top:16px;display:flex;gap:10px">
        <el-button v-if="currentStudent?.resumeFile" type="primary" plain size="small"
          @click="openFile(currentStudent.resumeFile)">📎 查看简历</el-button>
        <el-button type="success" plain size="small" @click="contactStudent(currentStudent)">
          💬 发消息
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { api } from '../../services/api.js'
import { getAuditStatus } from '../../services/enterprise.js'
import { getMyRecruitments } from '../../services/recruitment.js'
import messageApi from '../../services/message.js'
import { useUserStore } from '../../stores/user.js'

const router = useRouter()
const userStore = useUserStore()

const auditStatus = ref(-1)
const activeTab   = ref('browse')
const loading     = ref(false)
const students    = ref([])
const keyword     = ref('')
const college     = ref('')
const major       = ref('')
const minGpa      = ref(null)
const detailVisible   = ref(false)
const currentStudent  = ref(null)

// AI 推荐
const myJobs          = ref([])
const selectedJobId   = ref(null)
const recommending    = ref(false)
const aiRecommendResult = ref('')

onMounted(async () => {
  try {
    auditStatus.value = await getAuditStatus()
  } catch { auditStatus.value = -1 }
  if (auditStatus.value === 1) {
    loadStudents()
    loadMyJobs()
  }
})

const loadStudents = async () => {
  loading.value = true
  try {
    const res = await api.get('/admin/users', {
      params: { roleKey: 'student', keyword: keyword.value, page: 1, size: 50 }
    })
    let list = res?.list || []
    // 客户端 GPA 过滤
    if (minGpa.value) list = list.filter(s => s.avgGpa && parseFloat(s.avgGpa) >= minGpa.value)
    students.value = list
  } catch { students.value = [] } finally {
    loading.value = false
  }
}

const loadMyJobs = async () => {
  myJobs.value = await getMyRecruitments().catch(() => []) || []
}

const viewDetail = async (row) => {
  try {
    const profile = await api.get(`/student/profile/${row.userId}`)
    currentStudent.value = { ...row, ...profile }
    detailVisible.value = true
  } catch {
    currentStudent.value = row
    detailVisible.value = true
  }
}

const contactStudent = async (student) => {
  if (!student?.userId) return
  try {
    await messageApi.send(student.userId, `您好 ${student.realName}，我们对您的档案很感兴趣，希望进一步沟通。`)
    ElMessage.success('消息已发送')
    router.push('/hr/message')
  } catch { ElMessage.error('发送失败') }
}

const doAiRecommend = async () => {
  if (!selectedJobId.value) return
  recommending.value = true
  aiRecommendResult.value = ''
  try {
    const res = await api.post(`/ai/student-recommend/${selectedJobId.value}`)
    aiRecommendResult.value = res?.result || '暂无推荐结果'
  } catch { ElMessage.error('AI 推荐失败，请稍后重试') } finally {
    recommending.value = false
  }
}

const openFile = (path) => window.open('http://localhost:8083' + path, '_blank')

const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>')
    .replace(/\n\n/g, '<br><br>')
    .replace(/\n/g, '<br>')
}
</script>

<style scoped>
.student-browse { padding: 4px; }
.no-cert-wall { padding: 40px 0; }
.search-bar { display: flex; gap: 10px; flex-wrap: wrap; }

.student-card { cursor: pointer; transition: transform .15s; }
.student-card:hover { transform: translateY(-2px); }
.sc-header { display: flex; align-items: center; gap: 12px; margin-bottom: 10px; }
.sc-name { font-size: 15px; font-weight: 600; color: #303133; }
.sc-sub  { font-size: 12px; color: #909399; margin-top: 2px; }
.sc-tags { display: flex; gap: 6px; flex-wrap: wrap; }

.ai-toolbar { display: flex; gap: 12px; align-items: center; margin-bottom: 16px; }
.loading-box { text-align: center; padding: 60px 0; }
.ai-result { line-height: 1.8; color: #303133; font-size: 14px; }
.ai-result :deep(h2) { color: #409EFF; border-bottom: 1px solid #e4e7ed; padding-bottom: 4px; margin: 14px 0 6px; }
.ai-result :deep(h3) { color: #606266; margin: 10px 0 4px; }
.ai-result :deep(ul) { padding-left: 20px; }
.ai-result :deep(li) { margin-bottom: 4px; }
</style>
