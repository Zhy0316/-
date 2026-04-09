<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between">
      <span style="font-size:16px;font-weight:600">招聘管理</span>
      <el-button type="primary" @click="openDialog()">+ 发布招聘</el-button>
    </div>

    <el-table :data="jobs" stripe>
      <el-table-column prop="title"      label="招聘标题" min-width="140" />
      <el-table-column prop="position"   label="职位"     width="120" />
      <el-table-column prop="salaryRange" label="薪资"   width="110" />
      <el-table-column prop="location"   label="地点"     width="110" />
      <el-table-column prop="status"     label="状态"     width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '招聘中' : '已结束' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="160" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="viewApps(row)">查看投递</el-button>
          <el-button size="small" type="warning" v-if="row.status === 1"
            @click="handleClose(row.id)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!jobs.length" description="暂无招聘信息" />

    <!-- 发布弹窗 -->
    <el-dialog v-model="dialogVisible" title="发布招聘" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="招聘标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="职位名称" required>
          <el-input v-model="form.position" />
        </el-form-item>
        <el-form-item label="薪资范围">
          <el-input v-model="form.salaryRange" placeholder="如：10k-15k" />
        </el-form-item>
        <el-form-item label="工作地点">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="任职要求">
          <el-input v-model="form.requirement" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 投递列表弹窗 -->
    <el-dialog v-model="appsVisible" :title="'投递列表 - ' + currentJob?.title" width="700px">
      <el-table :data="applications" stripe size="small">
        <el-table-column prop="realName"   label="姓名"   width="100" />
        <el-table-column prop="studentNo"  label="学号"   width="130" />
        <el-table-column prop="college"    label="学院"   width="130" />
        <el-table-column prop="major"      label="专业"   min-width="120" />
        <el-table-column prop="applyTime"  label="投递时间" width="150" />
        <el-table-column prop="status"     label="状态"   width="100">
          <template #default="{ row }">
            <el-select v-model="row.status" size="small" @change="updateStatus(row)">
              <el-option label="已投递" :value="0" />
              <el-option label="有意向" :value="1" />
              <el-option label="不匹配" :value="2" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  publishRecruitment, getMyRecruitments, closeRecruitment,
  getApplicationsByRecruitment, updateApplicationStatus
} from '../../services/recruitment.js'

const jobs = ref([])
const dialogVisible = ref(false)
const appsVisible = ref(false)
const saving = ref(false)
const currentJob = ref(null)
const applications = ref([])
const form = ref({ title: '', position: '', salaryRange: '', location: '', requirement: '' })

const loadJobs = async () => {
  jobs.value = await getMyRecruitments().catch(() => []) || []
}

const openDialog = () => {
  form.value = { title: '', position: '', salaryRange: '', location: '', requirement: '' }
  dialogVisible.value = true
}

const handlePublish = async () => {
  if (!form.value.title || !form.value.position) return ElMessage.warning('请填写标题和职位')
  saving.value = true
  try {
    await publishRecruitment(form.value)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    loadJobs()
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    saving.value = false
  }
}

const handleClose = async (id) => {
  await closeRecruitment(id)
  ElMessage.success('已关闭')
  loadJobs()
}

const viewApps = async (job) => {
  currentJob.value = job
  applications.value = await getApplicationsByRecruitment(job.id).catch(() => []) || []
  appsVisible.value = true
}

const updateStatus = async (row) => {
  await updateApplicationStatus(row.applicationId, row.status)
  ElMessage.success('状态已更新')
}

onMounted(loadJobs)
</script>
