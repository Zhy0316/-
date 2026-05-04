<template>
  <div class="tutor-workbench">
    <h2>导师工作台</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff;">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.studentCount || 0 }}</div>
              <div class="stat-label">指导学生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c;">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.warningCount || 0 }}</div>
              <div class="stat-label">需要关注</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a;">
              <el-icon><Check /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ dashboard.activeCount || 0 }}</div>
              <div class="stat-label">活跃学生</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c;">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ pendingAppointments.length }}</div>
              <div class="stat-label">待确认预约</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生列表 -->
    <el-card class="students-card">
      <template #header>
        <div class="card-header">
          <span>学生成长看板</span>
          <el-radio-group v-model="filterType" size="small">
            <el-radio-button value="all">全部</el-radio-button>
            <el-radio-button value="warning">需关注</el-radio-button>
            <el-radio-button value="active">活跃</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="filteredStudents" style="width: 100%">
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="major" label="专业" width="150" />
        <el-table-column prop="currentGpa" label="当前GPA" width="100">
          <template #default="scope">
            <el-tag :type="getGpaType(scope.row.currentGpa)">
              {{ scope.row.currentGpa }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="awardCount" label="获奖数" width="80" />
        <el-table-column prop="recentDiaryCount" label="近期日记" width="100" />
        <el-table-column prop="pendingCount" label="待审核" width="80" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.needsAttention" type="danger" size="small">
              需关注
            </el-tag>
            <el-tag v-else-if="scope.row.isActive" type="success" size="small">
              活跃
            </el-tag>
            <el-tag v-else type="info" size="small">
              正常
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警原因" min-width="150">
          <template #default="scope">
            <span v-if="scope.row.warningReasons && scope.row.warningReasons.length > 0">
              {{ scope.row.warningReasons.join(', ') }}
            </span>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewStudent(scope.row)">
              查看详情
            </el-button>
            <el-button size="small" @click="addGuidance(scope.row)">
              添加指导
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加指导记录对话框 -->
    <el-dialog v-model="guidanceDialogVisible" title="添加指导记录" width="600px">
      <el-form :model="guidanceForm" label-width="100px">
        <el-form-item label="学生">
          <el-input v-model="currentStudent.studentName" disabled />
        </el-form-item>
        <el-form-item label="指导类型">
          <el-select v-model="guidanceForm.guidanceType" style="width: 100%;">
            <el-option label="线下" value="线下" />
            <el-option label="线上" value="线上" />
            <el-option label="电话" value="电话" />
            <el-option label="邮件" value="邮件" />
          </el-select>
        </el-form-item>
        <el-form-item label="指导主题">
          <el-input v-model="guidanceForm.guidanceTopic" />
        </el-form-item>
        <el-form-item label="指导内容">
          <el-input 
            v-model="guidanceForm.guidanceContent" 
            type="textarea" 
            :rows="4" />
        </el-form-item>
        <el-form-item label="指导日期">
          <el-date-picker 
            v-model="guidanceForm.guidanceDate" 
            type="date"
            style="width: 100%;" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="guidanceForm.duration" :min="0" :step="15" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="guidanceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveGuidance">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { User, Warning, Check, Clock } from '@element-plus/icons-vue'
import tutorWorkbenchApi from '@/services/tutorWorkbench'

const router = useRouter()
const userStore = useUserStore()

// 数据
const dashboard = ref({})
const students = ref([])
const pendingAppointments = ref([])
const filterType = ref('all')
const guidanceDialogVisible = ref(false)
const currentStudent = ref({})

// 表单
const guidanceForm = ref({
  guidanceType: '线下',
  guidanceTopic: '',
  guidanceContent: '',
  guidanceDate: new Date(),
  duration: 60
})

// 计算属性
const filteredStudents = computed(() => {
  if (filterType.value === 'warning') {
    return students.value.filter(s => s.needsAttention)
  } else if (filterType.value === 'active') {
    return students.value.filter(s => s.isActive)
  }
  return students.value
})

// 方法
const getGpaType = (gpa) => {
  if (gpa >= 3.5) return 'success'
  if (gpa >= 3.0) return ''
  if (gpa >= 2.5) return 'warning'
  return 'danger'
}

const loadDashboard = async () => {
  try {
    const res = await tutorWorkbenchApi.getStudentDashboard(userStore.userInfo.userId)
    dashboard.value = res.data || res
    students.value = dashboard.value.students || []
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

const loadPendingAppointments = async () => {
  try {
    const res = await tutorWorkbenchApi.getPendingAppointments(userStore.userInfo.userId)
    pendingAppointments.value = res.data || res || []
  } catch (error) {
    console.error('加载预约失败', error)
  }
}

const viewStudent = (student) => {
  router.push(`/tutor/student/${student.studentId}`)
}

const addGuidance = (student) => {
  currentStudent.value = student
  guidanceForm.value = {
    guidanceType: '线下',
    guidanceTopic: '',
    guidanceContent: '',
    guidanceDate: new Date(),
    duration: 60
  }
  guidanceDialogVisible.value = true
}

const saveGuidance = async () => {
  try {
    await tutorWorkbenchApi.addGuidanceRecord({
      tutorId: userStore.userInfo.userId,
      studentId: currentStudent.value.studentId,
      ...guidanceForm.value
    })
    ElMessage.success('指导记录添加成功')
    guidanceDialogVisible.value = false
    await loadDashboard()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadDashboard(),
    loadPendingAppointments()
  ])
})
</script>

<style scoped>
.tutor-workbench {
  padding: 20px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 28px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  line-height: 1;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.students-card {
  margin-top: 20px;
}
</style>
