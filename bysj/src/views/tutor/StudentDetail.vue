<template>
  <div>
    <el-page-header @back="$router.back()" style="margin-bottom:20px">
      <template #content>
        <span>{{ profile?.realName || '学生' }} 的档案</span>
      </template>
    </el-page-header>

    <el-tabs v-model="activeTab">

      <!-- ===== 基本信息 ===== -->
      <el-tab-pane label="基本信息" name="profile">
        <el-descriptions :column="3" border v-if="profile" style="margin-bottom:20px">
          <el-descriptions-item label="姓名">{{ profile.realName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ profile.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.gender === 'M' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ profile.college }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ profile.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ profile.className }}</el-descriptions-item>
          <el-descriptions-item label="入学年份">{{ profile.enrollmentYear }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ profile.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ profile.politicalStatus || '-' }}</el-descriptions-item>
          <el-descriptions-item label="民族">{{ profile.nation || '-' }}</el-descriptions-item>
          <el-descriptions-item label="籍贯">{{ profile.nativePlace || '-' }}</el-descriptions-item>
          <el-descriptions-item label="出生日期">{{ profile.birthDate || '-' }}</el-descriptions-item>
          <el-descriptions-item label="个人简历" :span="2">
            <el-link v-if="profile.resumeFile" :href="fileUrl(profile.resumeFile)" target="_blank" type="primary">
              查看简历
            </el-link>
            <span v-else>未上传</span>
          </el-descriptions-item>
        </el-descriptions>
        <el-empty v-else description="暂无档案信息" />
      </el-tab-pane>

      <!-- ===== 学业成绩 ===== -->
      <el-tab-pane label="学业成绩" name="academic">
        <!-- 统计卡片 -->
        <el-row :gutter="16" style="margin-bottom:16px">
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">总学分</div>
              <div class="stat-val">{{ totalCredits.toFixed(1) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">平均绩点</div>
              <div class="stat-val" style="color:#67C23A">{{ avgGpa.toFixed(2) }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">课程总数</div>
              <div class="stat-val">{{ academics.length }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover" class="stat-card">
              <div class="stat-label">不及格门数</div>
              <div class="stat-val" :style="failCount > 0 ? 'color:#F56C6C' : ''">{{ failCount }}</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 绩点趋势图 -->
        <el-card style="margin-bottom:16px">
          <div ref="gpaChartRef" style="height:220px"></div>
        </el-card>

        <!-- 成绩表格（只读，无操作列） -->
        <el-table :data="academics" stripe size="small">
          <el-table-column prop="academicYear" label="学年/学期" width="150" />
          <el-table-column prop="courseName"   label="课程名称" min-width="140" />
          <el-table-column prop="courseNature" label="性质" width="80" />
          <el-table-column prop="credit"       label="学分" width="70" />
          <el-table-column prop="score"        label="成绩" width="80" />
          <el-table-column prop="gpa"          label="绩点" width="80" />
          <el-table-column prop="isInvalidated" label="是否作废" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.isInvalidated === '是'" type="danger" size="small">作废</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!academics.length" description="暂无成绩记录" />
      </el-tab-pane>

      <!-- ===== 获奖记录 ===== -->
      <el-tab-pane label="获奖记录" name="award">
        <el-table :data="awards" stripe size="small">
          <el-table-column prop="awardName"        label="奖项名称" min-width="140" />
          <el-table-column prop="awardLevel"       label="级别" width="90" />
          <el-table-column prop="issuingAuthority" label="颁发单位" width="140" />
          <el-table-column prop="awardDate"        label="获奖日期" width="110" />
          <el-table-column label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'" size="small">
                {{ row.auditStatus === 1 ? '已通过' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="证书" width="80">
            <template #default="{ row }">
              <el-link v-if="row.certFile" :href="fileUrl(row.certFile)" target="_blank" type="primary" size="small">查看</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <!-- 导师审核操作 -->
          <el-table-column label="审核" width="100">
            <template #default="{ row }">
              <el-button v-if="row.auditStatus === 0" size="small" type="success"
                @click="auditAward(row.id, 1)">通过</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!awards.length" description="暂无获奖记录" />
      </el-tab-pane>

      <!-- ===== 科研项目 ===== -->
      <el-tab-pane label="科研项目" name="research">
        <el-table :data="researches" stripe size="small">
          <el-table-column prop="projectName" label="项目名称" min-width="150" />
          <el-table-column prop="role"        label="角色" width="100" />
          <el-table-column prop="startDate"   label="开始" width="110" />
          <el-table-column prop="endDate"     label="结束" width="110" />
          <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
          <el-table-column label="成果附件" width="80">
            <template #default="{ row }">
              <el-link v-if="row.resultFile" :href="fileUrl(row.resultFile)" target="_blank" type="primary" size="small">查看</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!researches.length" description="暂无科研项目" />
      </el-tab-pane>

      <!-- ===== 实践记录 ===== -->
      <el-tab-pane label="实践记录" name="practice">
        <el-table :data="practices" stripe size="small">
          <el-table-column prop="activityName" label="实践主题" min-width="150" />
          <el-table-column prop="organization" label="单位/组织" width="140" />
          <el-table-column prop="startDate"    label="开始" width="110" />
          <el-table-column prop="endDate"      label="结束" width="110" />
          <el-table-column prop="content"      label="内容" min-width="160" show-overflow-tooltip />
          <el-table-column label="证明材料" width="80">
            <template #default="{ row }">
              <el-link v-if="row.proofFile" :href="fileUrl(row.proofFile)" target="_blank" type="primary" size="small">查看</el-link>
              <span v-else>-</span>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!practices.length" description="暂无实践记录" />
      </el-tab-pane>

      <!-- ===== 成长日记 ===== -->
      <el-tab-pane label="成长日记" name="diary">
        <el-timeline v-if="diaries.length">
          <el-timeline-item v-for="d in diaries" :key="d.id"
            :timestamp="d.recordDate || d.createTime" placement="top">
            <el-card>
              <div style="font-weight:600;margin-bottom:6px">
                {{ d.title }}
                <el-tag v-if="d.mood" size="small" style="margin-left:8px">{{ d.mood }}</el-tag>
              </div>
              <div style="color:#606266;font-size:13px" v-html="d.content?.slice(0,200)"></div>
              <el-link v-if="d.attachmentPath" :href="fileUrl(d.attachmentPath)"
                target="_blank" type="primary" size="small" style="margin-top:6px">
                查看附件
              </el-link>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无公开日记" />
      </el-tab-pane>

      <!-- ===== 作品集 ===== -->
      <el-tab-pane label="作品集" name="portfolio">
        <el-row :gutter="16">
          <el-col :span="8" v-for="p in portfolios" :key="p.id" style="margin-bottom:16px">
            <el-card shadow="hover">
              <div style="font-weight:600;margin-bottom:6px">{{ p.title }}</div>
              <el-tag size="small">{{ p.workType }}</el-tag>
              <div style="color:#909399;font-size:12px;margin-top:6px">
                {{ p.uploadTime ? new Date(p.uploadTime).toLocaleDateString() : '' }}
              </div>
              <el-link v-if="p.fileUrl" :href="fileUrl(p.fileUrl)"
                target="_blank" type="primary" size="small" style="margin-top:8px;display:block">
                查看/下载作品
              </el-link>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!portfolios.length" description="暂无作品" />
      </el-tab-pane>

      <!-- ===== 成长评分 ===== -->
      <el-tab-pane label="成长评分" name="growth">
        <el-row :gutter="20" style="margin-bottom:16px">
          <el-col :span="10">
            <el-card header="能力雷达图">
              <div ref="radarRef" style="height:280px"></div>
            </el-card>
          </el-col>
          <el-col :span="14">
            <el-card header="综合成长分趋势">
              <div ref="lineRef" style="height:280px"></div>
            </el-card>
          </el-col>
        </el-row>
        <el-table :data="growthScores" stripe size="small">
          <el-table-column prop="termYear"      label="学期"   width="160" />
          <el-table-column prop="academicScore" label="学业分" width="100" />
          <el-table-column prop="awardScore"    label="获奖分" width="100" />
          <el-table-column prop="researchScore" label="科研分" width="100" />
          <el-table-column prop="practiceScore" label="实践分" width="100" />
          <el-table-column prop="totalScore"    label="综合成长分" width="120">
            <template #default="{ row }">
              <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
                {{ row.totalScore }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!growthScores.length" description="暂无成长评分数据" />
      </el-tab-pane>

      <!-- ===== 添加评语 ===== -->
      <el-tab-pane label="添加评语" name="comment">

        <!-- 新建/编辑评语表单 -->
        <el-card style="margin-bottom:20px">
          <template #header>
            <span>{{ editingCommentId ? '编辑评语' : '撰写新评语' }}</span>
          </template>

          <el-form label-width="80px">
            <el-form-item label="评语类型">
              <el-radio-group v-model="commentForm.targetType" @change="onCommentTypeChange">
                <el-radio-button value="GENERAL">综合评价</el-radio-button>
                <el-radio-button value="DIARY">日记批注</el-radio-button>
                <el-radio-button value="PORTFOLIO">作品点评</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <!-- 日记批注：选择具体日记 -->
            <el-form-item v-if="commentForm.targetType === 'DIARY'" label="选择日记">
              <el-select v-model="commentForm.targetId" placeholder="请选择要批注的日记"
                style="width:100%" v-loading="targetLoading">
                <el-option v-for="d in diaries" :key="d.id"
                  :label="`${d.title}（${d.recordDate ? new Date(d.recordDate).toLocaleDateString() : ''}）`"
                  :value="d.id" />
                <template #empty>
                  <div style="padding:12px;color:#909399;text-align:center">暂无公开日记</div>
                </template>
              </el-select>
            </el-form-item>

            <!-- 作品点评：选择具体作品 -->
            <el-form-item v-if="commentForm.targetType === 'PORTFOLIO'" label="选择作品">
              <el-select v-model="commentForm.targetId" placeholder="请选择要点评的作品"
                style="width:100%" v-loading="targetLoading">
                <el-option v-for="p in portfolios" :key="p.id"
                  :label="`${p.title}（${p.workType}）`"
                  :value="p.id" />
                <template #empty>
                  <div style="padding:12px;color:#909399;text-align:center">暂无作品</div>
                </template>
              </el-select>
            </el-form-item>

            <el-form-item label="评语内容">
              <WangEditor
                ref="commentEditorRef"
                v-model="commentForm.htmlContent"
                height="260px"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="commentLoading" @click="submitComment">
                {{ editingCommentId ? '保存修改' : '提交评语' }}
              </el-button>
              <el-button v-if="editingCommentId" @click="cancelEditComment">取消编辑</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 历史评语列表 -->
        <el-divider content-position="left">历史评语（{{ comments.length }} 条）</el-divider>
        <div v-if="comments.length">
          <el-card v-for="c in comments" :key="c.id" style="margin-bottom:12px" shadow="hover">
            <div style="display:flex;justify-content:space-between;align-items:flex-start">
              <div style="flex:1">
                <div style="display:flex;gap:8px;align-items:center;margin-bottom:8px">
                  <el-tag size="small" :type="commentTypeMap[c.targetType]?.type">
                    {{ commentTypeMap[c.targetType]?.label || c.targetType }}
                  </el-tag>
                  <!-- 关联的日记/作品标题 -->
                  <span v-if="c.targetType === 'DIARY' && c.targetId" style="font-size:12px;color:#909399">
                    批注日记：{{ diaries.find(d=>d.id===c.targetId)?.title || '#'+c.targetId }}
                  </span>
                  <span v-if="c.targetType === 'PORTFOLIO' && c.targetId" style="font-size:12px;color:#909399">
                    点评作品：{{ portfolios.find(p=>p.id===c.targetId)?.title || '#'+c.targetId }}
                  </span>
                  <span style="font-size:12px;color:#c0c4cc;margin-left:auto">
                    {{ c.createTime ? new Date(c.createTime).toLocaleString() : '' }}
                  </span>
                </div>
                <div class="comment-body" v-html="c.content"></div>
              </div>
              <div style="display:flex;gap:6px;margin-left:16px;flex-shrink:0">
                <el-button size="small" @click="startEditComment(c)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDeleteComment(c.id)">删除</el-button>
              </div>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="暂无评语记录" />
      </el-tab-pane>

    </el-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'
import { saveComment, getCommentList, updateComment, deleteComment } from '../../services/comment.js'
import * as echarts from 'echarts'
import WangEditor from '../../components/WangEditor.vue'

const route = useRoute()
const userStore = useUserStore()
const studentId = Number(route.params.id)

const activeTab = ref('profile')
const profile     = ref(null)
const academics   = ref([])
const awards      = ref([])
const researches  = ref([])
const practices   = ref([])
const diaries     = ref([])
const portfolios  = ref([])
const growthScores = ref([])
const comments    = ref([])
const commentLoading = ref(false)
const commentForm = ref({ targetType: 'GENERAL', targetId: null, htmlContent: '' })
const editingCommentId = ref(null)
const targetLoading = ref(false)
const commentEditorRef = ref(null)

const commentTypeMap = {
  GENERAL:   { label: '综合评价', type: '' },
  DIARY:     { label: '日记批注', type: 'success' },
  PORTFOLIO: { label: '作品点评', type: 'warning' }
}

const radarRef  = ref()
const lineRef   = ref()
const gpaChartRef = ref()

// ===== 计算属性 =====
const totalCredits = computed(() =>
  academics.value.reduce((s, r) => s + (parseFloat(r.credit) || 0), 0)
)
const avgGpa = computed(() => {
  const total = academics.value.reduce((s, r) => s + (parseFloat(r.creditGpa) || 0), 0)
  return totalCredits.value > 0 ? total / totalCredits.value : 0
})
const failCount = computed(() =>
  academics.value.filter(r => {
    const n = parseFloat(r.score)
    return !isNaN(n) && n < 60
  }).length
)

const fileUrl = (path) => path ? `http://localhost:8083${path}` : '#'

// ===== 数据加载 =====
const loadAll = async () => {
  const load = async (fn, fallback = []) => { try { return await fn() || fallback } catch { return fallback } }
  try {
    profile.value      = await api.get(`/student/profile/${studentId}`)
  } catch (e) {
    ElMessage.error('加载学生基本信息失败')
    return
  }
  academics.value    = await load(() => api.get(`/academic/${studentId}`))
  awards.value       = await load(() => api.get(`/award/${studentId}`))
  researches.value   = await load(() => api.get(`/research/list/${studentId}`))
  practices.value    = await load(() => api.get(`/practice/list/${studentId}`))
  diaries.value      = await load(() => api.get('/diary/list', { params: { studentId } }))
  portfolios.value   = await load(() => api.get(`/portfolio/${studentId}`))
  growthScores.value = await load(() => api.get(`/growth-score/${studentId}`))
  comments.value     = await load(() => getCommentList(studentId))
}

// ===== 图表渲染 =====
const renderCharts = async () => {
  await nextTick()
  // 绩点趋势图
  if (gpaChartRef.value && academics.value.length) {
    const semMap = {}
    academics.value.forEach(r => {
      if (!semMap[r.academicYear]) semMap[r.academicYear] = { sum: 0, cnt: 0 }
      const g = parseFloat(r.gpa)
      if (!isNaN(g)) { semMap[r.academicYear].sum += g; semMap[r.academicYear].cnt++ }
    })
    const sems = Object.keys(semMap).sort()
    echarts.init(gpaChartRef.value).setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: sems },
      yAxis: { type: 'value', min: 0, max: 4 },
      series: [{ name: '平均绩点', type: 'line', smooth: true,
        data: sems.map(s => semMap[s].cnt ? (semMap[s].sum / semMap[s].cnt).toFixed(2) : 0),
        itemStyle: { color: '#67C23A' }, areaStyle: {} }]
    })
  }
  // 雷达图
  if (radarRef.value) {
    try {
      const data = await api.get(`/growth-score/radar/${studentId}`)
      echarts.init(radarRef.value).setOption({
        radar: { indicator: [
          { name: '学业(40%)', max: 100 }, { name: '获奖(20%)', max: 100 },
          { name: '科研(20%)', max: 100 }, { name: '实践(20%)', max: 100 },
          { name: '综合', max: 100 }
        ]},
        series: [{ type: 'radar', data: [{
          value: [data?.academic||0, data?.award||0, data?.research||0, data?.practice||0, data?.total||0],
          name: '能力分布', areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' }
        }]}]
      })
    } catch { /* 忽略 */ }
  }
  // 成长分趋势
  if (lineRef.value && growthScores.value.length) {
    echarts.init(lineRef.value).setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['综合成长分'] },
      xAxis: { type: 'category', data: growthScores.value.map(s => s.termYear) },
      yAxis: { type: 'value', min: 0, max: 100 },
      series: [{ name: '综合成长分', type: 'line', smooth: true,
        data: growthScores.value.map(s => s.totalScore),
        itemStyle: { color: '#409EFF' }, areaStyle: {} }]
    })
  }
}

// 切换到图表相关 tab 时渲染
watch(activeTab, (tab) => {
  if (['academic', 'growth'].includes(tab)) renderCharts()
})

// ===== 获奖审核 =====
const auditAward = async (id, status) => {
  try {
    await api.put(`/award/audit/${id}`, { auditStatus: status })
    ElMessage.success('审核成功')
    awards.value = await api.get(`/award/${studentId}`) || []
  } catch {
    ElMessage.error('审核失败')
  }
}

// ===== 评语类型切换 =====
const onCommentTypeChange = () => { commentForm.value.targetId = null }

// ===== 开始编辑评语 =====
const startEditComment = (c) => {
  editingCommentId.value = c.id
  commentForm.value.targetType = c.targetType
  commentForm.value.targetId = c.targetId || null
  commentForm.value.htmlContent = c.content || ''
  nextTick(() => { if (commentEditorRef.value) commentEditorRef.value.setHtml(c.content || '') })
  // 滚动到表单
  document.querySelector('.wang-editor-wrapper')?.scrollIntoView({ behavior: 'smooth' })
}

const cancelEditComment = () => {
  editingCommentId.value = null
  commentForm.value = { targetType: 'GENERAL', targetId: null, htmlContent: '' }
  nextTick(() => { if (commentEditorRef.value) commentEditorRef.value.setHtml('') })
}

// ===== 提交评语（新建或编辑） =====
const submitComment = async () => {
  const content = commentEditorRef.value?.getHtml() || commentForm.value.htmlContent
  if (!content || content === '<p><br></p>') return ElMessage.warning('请输入评语内容')
  if (commentForm.value.targetType === 'DIARY' && !commentForm.value.targetId) {
    return ElMessage.warning('请选择要批注的日记')
  }
  if (commentForm.value.targetType === 'PORTFOLIO' && !commentForm.value.targetId) {
    return ElMessage.warning('请选择要点评的作品')
  }
  commentLoading.value = true
  try {
    if (editingCommentId.value) {
      await updateComment(editingCommentId.value, content)
      ElMessage.success('评语修改成功')
      editingCommentId.value = null
    } else {
      await saveComment({
        studentId,
        targetType: commentForm.value.targetType,
        targetId: commentForm.value.targetId || null,
        content
      })
      ElMessage.success('评语提交成功')
    }
    commentForm.value = { targetType: 'GENERAL', targetId: null, htmlContent: '' }
    nextTick(() => { if (commentEditorRef.value) commentEditorRef.value.setHtml('') })
    comments.value = await getCommentList(studentId) || []
  } catch { /* api.js 已弹错误 */ } finally {
    commentLoading.value = false
  }
}

// ===== 删除评语 =====
const handleDeleteComment = async (id) => {
  await ElMessageBox.confirm('确定删除该评语？', '提示', { type: 'warning' })
  await deleteComment(id)
  ElMessage.success('删除成功')
  comments.value = await getCommentList(studentId) || []
}

onMounted(async () => {
  await loadAll()
  renderCharts()
})
</script>

<style scoped>
.stat-card { text-align: center; padding: 8px 0; }
.stat-label { color: #909399; font-size: 13px; margin-bottom: 6px; }
.stat-val { font-size: 28px; font-weight: bold; color: #303133; }
.comment-body { line-height: 1.7; color: #303133; font-size: 14px; }
.comment-body img { max-width: 100%; border-radius: 4px; }
</style>
