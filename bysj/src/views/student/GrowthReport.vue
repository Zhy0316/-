<template>
  <div class="growth-report-container">
    <!-- 头部操作栏 -->
    <el-card class="header-card">
      <div class="header-content">
        <div class="title-section">
          <h2>📊 成长报告</h2>
          <p class="subtitle">自动生成多维度成长分析报告</p>
        </div>
        <div class="action-section">
          <el-button type="primary" @click="showGenerateDialog = true" :icon="Plus">
            生成新报告
          </el-button>
          <el-button type="success" @click="generateCurrentSemester" :icon="Document">
            快速生成本学期报告
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 报告列表 -->
    <el-card class="list-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>我的报告</span>
          <el-tag>共 {{ reports.length }} 份</el-tag>
        </div>
      </template>

      <el-empty v-if="reports.length === 0" description="暂无报告，点击上方按钮生成第一份报告" />

      <div class="report-list" v-else>
        <div 
          class="report-item" 
          v-for="report in reports" 
          :key="report.id"
          @click="viewReport(report.id)"
        >
          <div class="report-icon">
            <el-icon :size="40" color="#409EFF"><Document /></el-icon>
          </div>
          <div class="report-info">
            <h3>{{ report.title }}</h3>
            <p class="report-meta">
              <el-tag size="small" :type="getReportTypeTag(report.reportType)">
                {{ getReportTypeName(report.reportType) }}
              </el-tag>
              <span>{{ report.termYear }}</span>
              <span>{{ formatDate(report.startDate) }} ~ {{ formatDate(report.endDate) }}</span>
            </p>
            <p class="report-summary">{{ report.summary }}</p>
          </div>
          <div class="report-actions">
            <el-tag type="info" size="small">
              <el-icon><View /></el-icon> {{ report.viewCount }}
            </el-tag>
            <el-button type="primary" link @click.stop="viewReport(report.id)">
              查看详情
            </el-button>
            <el-button type="danger" link @click.stop="deleteReport(report.id)">
              删除
            </el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 生成报告对话框 -->
    <el-dialog 
      v-model="showGenerateDialog" 
      title="生成成长报告" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="generateForm" label-width="100px">
        <el-form-item label="报告类型">
          <el-radio-group v-model="generateForm.reportType">
            <el-radio label="semester">学期报告</el-radio>
            <el-radio label="year">学年报告</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="统计周期">
          <el-input 
            v-model="generateForm.termYear" 
            placeholder="如: 2023-2024-1"
          />
        </el-form-item>

        <el-form-item label="开始日期">
          <el-date-picker 
            v-model="generateForm.startDate" 
            type="date"
            placeholder="选择开始日期"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="结束日期">
          <el-date-picker 
            v-model="generateForm.endDate" 
            type="date"
            placeholder="选择结束日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showGenerateDialog = false">取消</el-button>
        <el-button type="primary" @click="handleGenerate" :loading="generating">
          生成报告
        </el-button>
      </template>
    </el-dialog>

    <!-- 报告详情对话框 -->
    <el-dialog 
      v-model="showDetailDialog" 
      :title="currentReport?.title" 
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <div class="report-detail" v-if="currentReport" v-loading="detailLoading">
        <!-- 报告摘要 -->
        <el-card class="summary-card">
          <template #header>
            <h3>📝 报告摘要</h3>
          </template>
          <p class="summary-text">{{ currentReport.summary }}</p>
        </el-card>

        <!-- 学业表现 -->
        <el-card class="section-card" v-if="currentReport.academicPerformance">
          <template #header>
            <h3>📚 学业表现</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.academicPerformance.currentGpa?.toFixed(2) }}</div>
                <div class="stat-label">当前GPA</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.academicPerformance.gpaTrend }}</div>
                <div class="stat-label">GPA趋势</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.academicPerformance.excellentCourses?.length || 0 }}</div>
                <div class="stat-label">优秀课程</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.academicPerformance.riskCourses?.length || 0 }}</div>
                <div class="stat-label">风险课程</div>
              </div>
            </el-col>
          </el-row>
          <p class="section-summary">{{ currentReport.academicPerformance.summary }}</p>
        </el-card>

        <!-- 能力发展 -->
        <el-card class="section-card" v-if="currentReport.abilityDevelopment">
          <template #header>
            <h3>💪 能力发展</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.abilityDevelopment.practiceCount || 0 }}</div>
                <div class="stat-label">实践活动</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.abilityDevelopment.researchCount || 0 }}</div>
                <div class="stat-label">科研项目</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.abilityDevelopment.portfolioCount || 0 }}</div>
                <div class="stat-label">作品产出</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.abilityDevelopment.newSkillCount || 0 }}</div>
                <div class="stat-label">新增技能</div>
              </div>
            </el-col>
          </el-row>
          <p class="section-summary">{{ currentReport.abilityDevelopment.summary }}</p>
        </el-card>

        <!-- 荣誉成就 -->
        <el-card class="section-card" v-if="currentReport.achievementSummary">
          <template #header>
            <h3>🏆 荣誉成就</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.achievementSummary.totalAwards || 0 }}</div>
                <div class="stat-label">获奖总数</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.achievementSummary.totalScore || 0 }}</div>
                <div class="stat-label">获奖总分</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.achievementSummary.growthRate }}</div>
                <div class="stat-label">同比增长</div>
              </div>
            </el-col>
          </el-row>
          <p class="section-summary">{{ currentReport.achievementSummary.summary }}</p>
        </el-card>

        <!-- 成长轨迹 -->
        <el-card class="section-card" v-if="currentReport.growthTrajectory">
          <template #header>
            <h3>📈 成长轨迹</h3>
          </template>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.growthTrajectory.milestones?.length || 0 }}</div>
                <div class="stat-label">关键里程碑</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-box">
                <div class="stat-value">{{ currentReport.growthTrajectory.diaryFrequency }}</div>
                <div class="stat-label">日记更新频率</div>
              </div>
            </el-col>
          </el-row>
          <p class="section-summary">{{ currentReport.growthTrajectory.summary }}</p>
        </el-card>

        <!-- 未来建议 -->
        <el-card class="section-card" v-if="currentReport.suggestions">
          <template #header>
            <h3>💡 未来建议</h3>
          </template>
          <ul class="suggestions-list">
            <li v-for="(suggestion, index) in currentReport.suggestions" :key="index">
              {{ suggestion }}
            </li>
          </ul>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Document, View } from '@element-plus/icons-vue'
import growthReportApi from '@/services/growthReport'

const loading = ref(false)
const generating = ref(false)
const detailLoading = ref(false)
const reports = ref([])
const showGenerateDialog = ref(false)
const showDetailDialog = ref(false)
const currentReport = ref(null)

const generateForm = ref({
  reportType: 'semester',
  termYear: '',
  startDate: '',
  endDate: ''
})

// 加载报告列表
const loadReports = async () => {
  loading.value = true
  try {
    const res = await growthReportApi.getMyReports()
    reports.value = res.data || []
  } catch (error) {
    ElMessage.error('加载报告列表失败')
  } finally {
    loading.value = false
  }
}

// 快速生成当前学期报告
const generateCurrentSemester = async () => {
  generating.value = true
  try {
    const res = await growthReportApi.generateCurrentSemester()
    ElMessage.success('报告生成成功')
    await loadReports()
    // 自动打开查看
    viewReport(res.data.id)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '生成报告失败')
  } finally {
    generating.value = false
  }
}

// 生成报告
const handleGenerate = async () => {
  if (!generateForm.value.termYear || !generateForm.value.startDate || !generateForm.value.endDate) {
    ElMessage.warning('请填写完整信息')
    return
  }

  generating.value = true
  try {
    const data = {
      reportType: generateForm.value.reportType,
      termYear: generateForm.value.termYear,
      startDate: formatDateToString(generateForm.value.startDate),
      endDate: formatDateToString(generateForm.value.endDate)
    }
    
    const res = await growthReportApi.generateReport(data)
    ElMessage.success('报告生成成功')
    showGenerateDialog.value = false
    await loadReports()
    // 自动打开查看
    viewReport(res.data.id)
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '生成报告失败')
  } finally {
    generating.value = false
  }
}

// 查看报告详情
const viewReport = async (reportId) => {
  detailLoading.value = true
  showDetailDialog.value = true
  try {
    const res = await growthReportApi.getReportDetail(reportId)
    currentReport.value = res.data
  } catch (error) {
    ElMessage.error('加载报告详情失败')
    showDetailDialog.value = false
  } finally {
    detailLoading.value = false
  }
}

// 删除报告
const deleteReport = async (reportId) => {
  try {
    await ElMessageBox.confirm('确定要删除这份报告吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await growthReportApi.deleteReport(reportId)
    ElMessage.success('删除成功')
    await loadReports()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateToString = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 获取报告类型名称
const getReportTypeName = (type) => {
  const map = {
    'semester': '学期报告',
    'year': '学年报告',
    'custom': '自定义报告'
  }
  return map[type] || type
}

// 获取报告类型标签
const getReportTypeTag = (type) => {
  const map = {
    'semester': 'primary',
    'year': 'success',
    'custom': 'warning'
  }
  return map[type] || 'info'
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.growth-report-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section h2 {
  margin: 0 0 5px 0;
  font-size: 24px;
  color: #303133;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.action-section {
  display: flex;
  gap: 10px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.report-item {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 1px solid #EBEEF5;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.report-item:hover {
  border-color: #409EFF;
  box-shadow: 0 2px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.report-icon {
  margin-right: 20px;
}

.report-info {
  flex: 1;
}

.report-info h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #303133;
}

.report-meta {
  display: flex;
  gap: 15px;
  margin-bottom: 10px;
  font-size: 14px;
  color: #909399;
}

.report-summary {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.report-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  align-items: flex-end;
}

.report-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.summary-card,
.section-card {
  margin-bottom: 20px;
}

.summary-text {
  font-size: 16px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
}

.stat-box {
  text-align: center;
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.section-summary {
  margin-top: 20px;
  padding: 15px;
  background: #F5F7FA;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
}

.suggestions-list {
  margin: 0;
  padding-left: 20px;
}

.suggestions-list li {
  margin-bottom: 10px;
  line-height: 1.8;
  color: #606266;
}
</style>
