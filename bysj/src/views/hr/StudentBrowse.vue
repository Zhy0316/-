<template>
  <div>
    <div style="margin-bottom:16px;font-size:16px;font-weight:600">浏览学生档案</div>
    <el-row :gutter="12" style="margin-bottom:16px">
      <el-col :span="6">
        <el-input v-model="keyword" placeholder="搜索姓名/学号" clearable @input="loadStudents" />
      </el-col>
      <el-col :span="5">
        <el-input v-model="college" placeholder="学院" clearable @input="loadStudents" />
      </el-col>
      <el-col :span="5">
        <el-input v-model="major" placeholder="专业" clearable @input="loadStudents" />
      </el-col>
    </el-row>

    <el-table :data="students" stripe>
      <el-table-column prop="realName"      label="姓名"   width="100" />
      <el-table-column prop="studentNo"     label="学号"   width="130" />
      <el-table-column prop="college"       label="学院"   width="140" />
      <el-table-column prop="major"         label="专业"   min-width="130" />
      <el-table-column prop="enrollmentYear" label="入学年份" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="viewDetail(row)">查看档案</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!students.length" description="暂无学生数据" />

    <!-- 学生档案弹窗（简版） -->
    <el-dialog v-model="detailVisible" :title="currentStudent?.realName + ' 的档案'" width="600px">
      <el-descriptions :column="2" border v-if="currentStudent">
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ currentStudent.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ currentStudent.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ currentStudent.className }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentStudent.enrollmentYear }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentStudent.email }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="currentStudent?.resumeFile" style="margin-top:16px">
        <a :href="'http://localhost:8083'+currentStudent.resumeFile" target="_blank">
          📎 查看个人简历
        </a>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../services/api.js'

const students = ref([])
const keyword = ref('')
const college = ref('')
const major = ref('')
const detailVisible = ref(false)
const currentStudent = ref(null)

const loadStudents = async () => {
  try {
    // 通过管理员接口获取学生列表
    const res = await api.get('/admin/users', {
      params: { roleKey: 'student', keyword: keyword.value, page: 1, size: 50 }
    })
    const list = res?.list || []
    // 为每个学生加载档案信息
    students.value = list
  } catch (e) {
    students.value = []
  }
}

const viewDetail = async (row) => {
  try {
    const profile = await api.get(`/student/profile/${row.userId}`)
    currentStudent.value = { ...row, ...profile }
    detailVisible.value = true
  } catch (e) {
    currentStudent.value = row
    detailVisible.value = true
  }
}

onMounted(loadStudents)
</script>
