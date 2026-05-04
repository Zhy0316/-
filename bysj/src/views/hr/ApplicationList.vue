<template>
  <div>
    <div style="margin-bottom:16px;font-size:16px;font-weight:600">投递管理</div>
    <el-select v-model="selectedJobId" placeholder="选择招聘职位" style="width:260px;margin-bottom:16px"
      @change="loadApps">
      <el-option v-for="j in jobs" :key="j.id" :label="j.title" :value="j.id" />
    </el-select>

    <el-table :data="applications" stripe>
      <el-table-column prop="realName"   label="姓名"   width="100" />
      <el-table-column prop="studentNo"  label="学号"   width="130" />
      <el-table-column prop="college"    label="学院"   width="130" />
      <el-table-column prop="major"      label="专业"   min-width="120" />
      <el-table-column prop="message"    label="留言"   min-width="120" show-overflow-tooltip />
      <el-table-column prop="applyTime"  label="投递时间" width="150" />
      <el-table-column prop="status"     label="状态"   width="120">
        <template #default="{ row }">
          <el-select v-model="row.status" size="small" @change="updateStatus(row)">
            <el-option label="已投递" :value="0" />
            <el-option label="有意向" :value="1" />
            <el-option label="不匹配" :value="2" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button v-if="row.resumeFile" size="small" type="primary" link
            @click="window.open('http://localhost:8083'+row.resumeFile)">查看简历</el-button>
          <el-button size="small" type="success" link @click="contactStudent(row)">联系学生</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!applications.length" description="请先选择招聘职位" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getMyRecruitments, getApplicationsByRecruitment, updateApplicationStatus
} from '../../services/recruitment.js'
import messageApi from '../../services/message.js'

const jobs = ref([])
const selectedJobId = ref(null)
const applications = ref([])
const router = useRouter()

const loadJobs = async () => {
  jobs.value = await getMyRecruitments().catch(() => []) || []
}

const loadApps = async () => {
  if (!selectedJobId.value) return
  applications.value = await getApplicationsByRecruitment(selectedJobId.value).catch(() => []) || []
}

const updateStatus = async (row) => {
  await updateApplicationStatus(row.applicationId, row.status)
  ElMessage.success('状态已更新')
}

// 联系学生：发一条初始消息，跳转到消息页
const contactStudent = async (row) => {
  if (!row.studentUserId) {
    ElMessage.warning('暂无学生联系信息')
    return
  }
  try {
    await messageApi.send(row.studentUserId, `您好 ${row.realName}，我们对您的投递很感兴趣，希望进一步沟通。`)
    ElMessage.success('已发送消息，正在跳转...')
    router.push('/hr/message')
  } catch {
    ElMessage.error('发送失败，请稍后重试')
  }
}

onMounted(loadJobs)
</script>
