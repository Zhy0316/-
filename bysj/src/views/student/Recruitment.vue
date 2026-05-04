<template>
  <div>
    <el-tabs v-model="activeTab">

      <!-- 招聘列表 -->
      <el-tab-pane label="招聘列表" name="list">
        <!-- 筛选栏 -->
        <div class="filter-bar">
          <el-input v-model="searchKw" placeholder="搜索职位/公司" clearable style="width:200px"
            @input="filterJobs">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-select v-model="filterType" placeholder="职位类型" clearable style="width:120px"
            @change="filterJobs">
            <el-option label="全职" value="全职" />
            <el-option label="兼职" value="兼职" />
            <el-option label="实习" value="实习" />
          </el-select>
          <el-select v-model="filterEdu" placeholder="学历要求" clearable style="width:120px"
            @change="filterJobs">
            <el-option label="不限" value="不限" />
            <el-option label="本科" value="本科" />
            <el-option label="硕士" value="硕士" />
          </el-select>
        </div>

        <el-row :gutter="16" style="margin-top:16px">
          <el-col v-for="job in filteredJobs" :key="job.id" :span="8" style="margin-bottom:16px">
            <el-card shadow="hover" class="job-card" @click="openJobDetail(job)">
              <div class="jc-header">
                <div class="jc-title">{{ job.title }}</div>
                <el-tag size="small" :type="jobTypeColor(job.jobType)">
                  {{ job.jobType || '全职' }}
                </el-tag>
              </div>
              <div class="jc-pos">{{ job.position }}</div>
              <div class="jc-salary">{{ job.salaryRange || '薪资面议' }}</div>
              <div class="jc-tags">
                <span>📍 {{ job.location || '地点未填' }}</span>
                <span>🎓 {{ job.education || '本科' }}</span>
                <span>👥 招 {{ job.headcount || 1 }} 人</span>
              </div>
              <div v-if="job.deadline" class="jc-deadline">
                ⏰ 截止 {{ job.deadline }}
              </div>
              <el-button type="primary" size="small" style="width:100%;margin-top:10px"
                @click.stop="openApply(job)">投递简历</el-button>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!filteredJobs.length" description="暂无招聘信息" />
      </el-tab-pane>

      <!-- 我的投递 -->
      <el-tab-pane label="我的投递" name="mine">
        <div v-if="myApps.length === 0">
          <el-empty description="暂无投递记录" />
        </div>
        <div v-for="app in myApps" :key="app.applicationId" class="app-card">
          <div class="app-header">
            <div>
              <span class="app-title">{{ app.title }}</span>
              <el-tag size="small" style="margin-left:8px">{{ app.position }}</el-tag>
            </div>
            <span class="app-company">{{ app.companyName }}</span>
          </div>

          <!-- 流程进度条 -->
          <el-steps :active="app.finalStatus ?? 0" finish-status="success"
            process-status="process" size="small" style="margin:12px 0">
            <el-step title="已投递" />
            <el-step title="简历通过" />
            <el-step title="面试邀请" />
            <el-step title="录用" />
          </el-steps>

          <!-- 淘汰状态特殊显示 -->
          <el-alert v-if="app.finalStatus === 4" type="error" :closable="false" show-icon
            title="很遗憾，本次申请未通过" style="margin-bottom:8px" />

          <!-- 面试信息 -->
          <div v-if="app.finalStatus === 2 && app.interviewTime" class="interview-info">
            <el-icon color="#E6A23C"><Calendar /></el-icon>
            面试时间：{{ formatDateTime(app.interviewTime) }}
            <span v-if="app.interviewNote" style="margin-left:12px;color:#606266">
              {{ app.interviewNote }}
            </span>
          </div>

          <div class="app-footer">
            <span style="font-size:12px;color:#c0c4cc">{{ formatTime(app.applyTime) }} 投递</span>
            <el-button size="small" type="primary" plain @click="contactHr(app)">联系HR</el-button>
          </div>
        </div>
      </el-tab-pane>

      <!-- AI 职位推荐 -->
      <el-tab-pane label="🤖 AI 职位推荐" name="ai">
        <div class="ai-toolbar">
          <el-button type="primary" :loading="recommending" @click="doRecommend">
            {{ recommending ? 'AI 分析中...' : aiResult ? '重新推荐' : '获取 AI 职位推荐' }}
          </el-button>
          <el-button v-if="aiResult" size="small" @click="copyText(aiResult)">复制</el-button>
          <span class="ai-tip">AI 将结合你的档案和当前招聘职位，给出匹配推荐和未来规划建议</span>
        </div>
        <div v-if="recommending" class="loading-box">
          <el-icon class="is-loading" size="36" color="#409EFF"><Loading /></el-icon>
          <div class="loading-text">AI 正在分析职位匹配度，请稍候（约15-30秒）...</div>
        </div>
        <div v-else-if="aiResult" class="ai-result" v-html="renderMarkdown(aiResult)"></div>
        <el-empty v-else description="点击「获取 AI 职位推荐」开始智能匹配" :image-size="80" />
      </el-tab-pane>

    </el-tabs>

    <!-- 职位详情弹窗 -->
    <el-dialog v-model="jobDetailVisible" :title="currentJob?.title" width="700px" class="job-detail-dialog">
      <div v-if="currentJob" class="job-detail-content">
        <!-- 职位头部信息 -->
        <div class="detail-header">
          <div class="detail-title-row">
            <h2>{{ currentJob.title }}</h2>
            <span class="detail-salary">{{ currentJob.salaryRange || '薪资面议' }}</span>
          </div>
          <div class="detail-tags">
            <el-tag size="large">{{ currentJob.position }}</el-tag>
            <el-tag size="large" :type="jobTypeColor(currentJob.jobType)">
              {{ currentJob.jobType || '全职' }}
            </el-tag>
            <el-tag size="large" type="info">{{ currentJob.education || '本科' }}</el-tag>
          </div>
        </div>

        <!-- 基本信息卡片 -->
        <el-card shadow="never" class="info-card">
          <div class="info-grid">
            <div class="info-item">
              <el-icon size="18" color="#409EFF"><Location /></el-icon>
              <div>
                <div class="info-label">工作地点</div>
                <div class="info-value">{{ currentJob.location || '未填写' }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon size="18" color="#67C23A"><User /></el-icon>
              <div>
                <div class="info-label">招聘人数</div>
                <div class="info-value">{{ currentJob.headcount || 1 }} 人</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon size="18" color="#E6A23C"><Briefcase /></el-icon>
              <div>
                <div class="info-label">工作经验</div>
                <div class="info-value">{{ currentJob.experience || '不限' }}</div>
              </div>
            </div>
            <div class="info-item">
              <el-icon size="18" color="#F56C6C"><Calendar /></el-icon>
              <div>
                <div class="info-label">截止日期</div>
                <div class="info-value">{{ currentJob.deadline || '长期有效' }}</div>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 职位描述 -->
        <div v-if="currentJob.description" class="detail-section">
          <div class="section-title">
            <el-icon><Document /></el-icon>
            职位描述
          </div>
          <div class="section-content">{{ currentJob.description }}</div>
        </div>

        <!-- 任职要求 -->
        <div v-if="currentJob.requirement" class="detail-section">
          <div class="section-title">
            <el-icon><List /></el-icon>
            任职要求
          </div>
          <div class="section-content">{{ currentJob.requirement }}</div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="jobDetailVisible = false">关闭</el-button>
          <el-button size="large" type="primary" @click="openApply(currentJob); jobDetailVisible = false">
            <el-icon><Position /></el-icon>
            立即投递
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 投递弹窗 -->
    <el-dialog v-model="applyVisible" title="投递简历" width="550px" class="apply-dialog">
      <div class="apply-content">
        <div class="apply-job-info">
          <div class="apply-job-title">{{ currentApplyJob?.title }}</div>
          <div class="apply-job-meta">
            <el-tag size="small">{{ currentApplyJob?.position }}</el-tag>
            <span class="apply-company">{{ currentApplyJob?.companyName || '企业' }}</span>
          </div>
        </div>
        
        <el-divider />
        
        <div class="apply-form">
          <div class="form-label">
            <el-icon><ChatDotRound /></el-icon>
            求职意向说明（选填）
          </div>
          <el-input 
            v-model="applyMsg" 
            type="textarea" 
            :rows="5" 
            placeholder="向HR介绍自己，说明为什么适合这个职位，展示你的优势和热情..."
            maxlength="500"
            show-word-limit
          />
          <div class="apply-tips">
            <el-icon color="#E6A23C"><InfoFilled /></el-icon>
            <span>提示：一份用心的求职说明能大大提高通过率</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button size="large" @click="applyVisible = false">取消</el-button>
          <el-button size="large" type="primary" :loading="applyLoading" @click="submitApply">
            <el-icon><Check /></el-icon>
            确认投递
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search, Loading, Calendar, Location, User, Briefcase, Document, List, Position, ChatDotRound, InfoFilled, Check } from '@element-plus/icons-vue'
import { getRecruitmentList, applyJob, getMyApplications } from '../../services/recruitment.js'
import messageApi from '../../services/message.js'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'

const userStore = useUserStore()
const router = useRouter()

const activeTab    = ref('list')
const jobs         = ref([])
const myApps       = ref([])
const searchKw     = ref('')
const filterType   = ref('')
const filterEdu    = ref('')
const applyVisible = ref(false)
const currentApplyJob = ref(null)
const applyMsg     = ref('')
const applyLoading = ref(false)
const jobDetailVisible = ref(false)
const currentJob   = ref(null)

// AI 推荐
const recommending = ref(false)
const aiResult     = ref('')

const filteredJobs = computed(() => {
  return jobs.value.filter(j => {
    const kw = searchKw.value.toLowerCase()
    const matchKw = !kw || (j.title || '').toLowerCase().includes(kw) ||
                    (j.position || '').toLowerCase().includes(kw)
    const matchType = !filterType.value || j.jobType === filterType.value
    const matchEdu  = !filterEdu.value  || j.education === filterEdu.value || j.education === '不限'
    return matchKw && matchType && matchEdu
  })
})

const filterJobs = () => {} // computed 自动处理

const loadJobs = async () => {
  jobs.value = await getRecruitmentList().catch(() => []) || []
}

const loadMyApps = async () => {
  myApps.value = await getMyApplications().catch(() => []) || []
}

const openJobDetail = (job) => {
  currentJob.value = job
  jobDetailVisible.value = true
}

const openApply = (job) => {
  currentApplyJob.value = job
  applyMsg.value = ''
  applyVisible.value = true
}

const submitApply = async () => {
  applyLoading.value = true
  try {
    await applyJob(currentApplyJob.value.id, applyMsg.value)
    ElMessage.success('投递成功')
    applyVisible.value = false
    loadMyApps()
  } catch {
    ElMessage.error('投递失败，可能已投递过该职位')
  } finally {
    applyLoading.value = false
  }
}

const contactHr = async (row) => {
  if (!row.hrUserId) { ElMessage.warning('暂无HR联系信息'); return }
  try {
    await messageApi.send(row.hrUserId, `您好，我对「${row.title}」职位很感兴趣，希望进一步了解。`)
    ElMessage.success('已发送消息，正在跳转...')
    router.push('/student/message')
  } catch { ElMessage.error('发送失败') }
}

const doRecommend = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  recommending.value = true
  aiResult.value = ''
  try {
    const res = await api.post(`/ai/job-recommend/${userId}`)
    aiResult.value = res?.result || '暂无推荐结果'
  } catch { ElMessage.error('AI 推荐失败，请稍后重试') } finally {
    recommending.value = false
  }
}

const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>')
    .replace(/\n\n/g, '<br><br>').replace(/\n/g, '<br>')
}

const copyText = (text) => {
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('已复制'))
    .catch(() => ElMessage.error('复制失败'))
}

const jobTypeColor = (t) => ({ '全职': '', '兼职': 'warning', '实习': 'success' }[t] || '')

const formatTime = (t) => t ? new Date(t).toLocaleDateString('zh-CN') : ''
const formatDateTime = (t) => t ? new Date(t).toLocaleString('zh-CN', { hour12: false }) : ''

onMounted(() => { loadJobs(); loadMyApps() })
</script>

<style scoped>
.filter-bar { display: flex; gap: 10px; flex-wrap: wrap; }

.job-card { cursor: pointer; transition: transform .15s; }
.job-card:hover { transform: translateY(-2px); }
.jc-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 6px; }
.jc-title  { font-size: 15px; font-weight: 600; color: #303133; }
.jc-pos    { font-size: 13px; color: #606266; margin-bottom: 4px; }
.jc-salary { font-size: 15px; font-weight: 600; color: #E6A23C; margin-bottom: 8px; }
.jc-tags   { display: flex; flex-wrap: wrap; gap: 10px; font-size: 12px; color: #909399; margin-bottom: 4px; }
.jc-deadline { font-size: 12px; color: #F56C6C; }

.app-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background: #fafafa;
}
.app-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.app-title  { font-size: 15px; font-weight: 600; color: #303133; }
.app-company { font-size: 13px; color: #909399; }
.app-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 8px; }
.interview-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #E6A23C;
  background: #fdf6ec;
  padding: 6px 10px;
  border-radius: 4px;
  margin-bottom: 8px;
}

.ai-toolbar { display: flex; gap: 10px; align-items: center; margin-bottom: 16px; flex-wrap: wrap; }
.ai-tip { font-size: 12px; color: #909399; }
.loading-box { text-align: center; padding: 60px 0; }
.loading-text { margin-top: 12px; color: #909399; }
.ai-result { line-height: 1.8; color: #303133; font-size: 14px; }
.ai-result :deep(h2) { color: #409EFF; border-bottom: 1px solid #e4e7ed; padding-bottom: 4px; margin: 14px 0 6px; }
.ai-result :deep(h3) { color: #606266; margin: 10px 0 4px; }
.ai-result :deep(ul) { padding-left: 20px; }
.ai-result :deep(li) { margin-bottom: 4px; }

.detail-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 4px; }
.detail-salary { font-size: 18px; font-weight: 600; color: #E6A23C; margin-left: auto; }

/* 职位详情弹窗样式 */
.job-detail-dialog :deep(.el-dialog__body) { padding: 0; }
.job-detail-content { padding: 24px; }
.detail-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.detail-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.detail-title-row h2 { margin: 0; font-size: 24px; }
.detail-header .detail-salary {
  font-size: 22px;
  font-weight: 700;
  color: #FFD700;
}
.detail-tags { display: flex; gap: 8px; }
.info-card { margin-bottom: 20px; }
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}
.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}
.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.detail-section {
  margin-bottom: 20px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}
.section-content {
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  font-size: 14px;
}

/* 投递弹窗样式 */
.apply-content { padding: 4px 0; }
.apply-job-info {
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}
.apply-job-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}
.apply-job-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.apply-company {
  font-size: 14px;
  color: #606266;
}
.apply-form { margin-top: 16px; }
.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 10px;
}
.apply-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  font-size: 12px;
  color: #E6A23C;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
