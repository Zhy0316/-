<template>
  <div class="academic-health">
    <el-page-header @back="$router.back()" title="返回">
      <template #content>
        <span style="font-size:18px;font-weight:600">学业健康度分析</span>
      </template>
    </el-page-header>

    <div v-loading="loading" style="margin-top:20px">
      <!-- GPA统计卡片 -->
      <el-row :gutter="16" style="margin-bottom:20px">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background:#ecf5ff">
              <el-icon size="32" color="#409EFF"><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">当前学期GPA</div>
              <div class="stat-value" :style="{color: gpaColor(report.currentGPA)}">
                {{ report.currentGPA || '0.00' }}
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background:#f0f9ff">
              <el-icon size="32" color="#67C23A"><DataLine /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">历史平均GPA</div>
              <div class="stat-value">{{ report.avgGPA || '0.00' }}</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background:#fef0f0">
              <el-icon size="32" color="#F56C6C"><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">风险课程</div>
              <div class="stat-value" style="color:#F56C6C">
                {{ report.riskCourses?.length || 0 }}
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-icon" style="background:#f4f4f5">
              <el-icon size="32" color="#909399"><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-label">学分进度</div>
              <div class="stat-value">
                {{ report.creditProgress?.percentage || 0 }}%
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 学分进度条 -->
      <el-card style="margin-bottom:20px">
        <template #header>
          <div style="display:flex;justify-content:space-between;align-items:center">
            <span>学分完成进度</span>
            <span style="font-size:14px;color:#909399">
              已修 {{ report.creditProgress?.completed || 0 }} / 
              要求 {{ report.creditProgress?.required || 160 }} 学分
            </span>
          </div>
        </template>
        <el-progress 
          :percentage="parseFloat(report.creditProgress?.percentage || 0)" 
          :color="progressColor"
          :stroke-width="20"
        >
          <span style="font-size:14px">{{ report.creditProgress?.percentage || 0 }}%</span>
        </el-progress>
      </el-card>

      <el-row :gutter="16">
        <!-- GPA趋势图 -->
        <el-col :span="12">
          <el-card>
            <template #header>GPA趋势分析</template>
            <div ref="gpaTrendChart" style="height:300px"></div>
          </el-card>
        </el-col>

        <!-- 课程类型分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>课程类型分布</template>
            <div ref="courseTypeChart" style="height:300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 风险课程列表 -->
      <el-card style="margin-top:20px" v-if="report.riskCourses && report.riskCourses.length > 0">
        <template #header>
          <span style="color:#F56C6C">⚠️ 风险课程预警</span>
        </template>
        <el-table :data="report.riskCourses" size="small">
          <el-table-column prop="courseName" label="课程名称" />
          <el-table-column prop="academicYear" label="学年" width="120" />
          <el-table-column prop="gpa" label="绩点" width="80">
            <template #default="{row}">
              <span style="color:#F56C6C;font-weight:600">{{ row.gpa }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="成绩" width="80" />
        </el-table>
      </el-card>

      <!-- 学业建议 -->
      <el-card style="margin-top:20px" v-if="report.suggestions && report.suggestions.length > 0">
        <template #header>💡 学业建议</template>
        <el-timeline>
          <el-timeline-item 
            v-for="(suggestion, index) in report.suggestions" 
            :key="index"
            :type="index === 0 ? 'primary' : 'info'"
          >
            {{ suggestion }}
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { TrendCharts, DataLine, Warning, Document } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { api } from '../../services/api.js'

const loading = ref(false)
const report = ref({})
const gpaTrendChart = ref(null)
const courseTypeChart = ref(null)

const progressColor = [
  { color: '#f56c6c', percentage: 40 },
  { color: '#e6a23c', percentage: 60 },
  { color: '#5cb87a', percentage: 80 },
  { color: '#1989fa', percentage: 100 }
]

const gpaColor = (gpa) => {
  if (!gpa) return '#909399'
  const val = parseFloat(gpa)
  if (val >= 3.5) return '#67C23A'
  if (val >= 3.0) return '#409EFF'
  if (val >= 2.5) return '#E6A23C'
  return '#F56C6C'
}

const loadReport = async () => {
  loading.value = true
  try {
    report.value = await api.get('/academic/health-report') || {}
    await nextTick()
    initCharts()
  } catch {
    ElMessage.error('加载学业报告失败')
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  initGPATrendChart()
  initCourseTypeChart()
}

const initGPATrendChart = () => {
  if (!gpaTrendChart.value || !report.value.gpaTrends) return
  
  const chart = echarts.init(gpaTrendChart.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' }
    },
    legend: {
      data: ['GPA', '学分']
    },
    xAxis: {
      type: 'category',
      data: report.value.gpaTrends.map(t => t.term),
      axisLabel: { rotate: 45 }
    },
    yAxis: [
      {
        type: 'value',
        name: 'GPA',
        min: 0,
        max: 4,
        axisLabel: { formatter: '{value}' }
      },
      {
        type: 'value',
        name: '学分',
        axisLabel: { formatter: '{value}' }
      }
    ],
    series: [
      {
        name: 'GPA',
        type: 'line',
        data: report.value.gpaTrends.map(t => t.gpa),
        smooth: true,
        itemStyle: { color: '#409EFF' },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        }
      },
      {
        name: '学分',
        type: 'bar',
        yAxisIndex: 1,
        data: report.value.gpaTrends.map(t => t.credit),
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

const initCourseTypeChart = () => {
  if (!courseTypeChart.value || !report.value.courseTypeStats) return
  
  const chart = echarts.init(courseTypeChart.value)
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}门课程<br/>平均GPA: {d}'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{c}门'
        },
        data: report.value.courseTypeStats.map(s => ({
          name: s.courseType,
          value: s.count,
          avgGPA: s.avgGPA
        }))
      }
    ]
  }
  chart.setOption(option)
  window.addEventListener('resize', () => chart.resize())
}

onMounted(loadReport)
</script>

<style scoped>
.academic-health { padding: 4px; }

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover { transform: translateY(-2px); }

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}

.stat-content { flex: 1; }

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
</style>
