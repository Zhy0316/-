<template>
  <div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="招聘列表" name="list">
        <el-row :gutter="16">
          <el-col :span="8" v-for="job in jobs" :key="job.id" style="margin-bottom:16px">
            <el-card shadow="hover">
              <div class="job-title">{{ job.title }}</div>
              <div class="job-meta">
                <el-tag size="small">{{ job.position }}</el-tag>
                <span class="salary">{{ job.salaryRange || '薪资面议' }}</span>
              </div>
              <div class="job-loc">📍 {{ job.location || '地点未填写' }}</div>
              <div class="job-req" v-if="job.requirement">{{ job.requirement.slice(0, 60) }}...</div>
              <el-button type="primary" size="small" style="margin-top:12px;width:100%"
                @click="openApply(job)">投递简历</el-button>
            </el-card>
          </el-col>
        </el-row>
        <el-empty v-if="!jobs.length" description="暂无招聘信息" />
      </el-tab-pane>

      <el-tab-pane label="我的投递" name="mine">
        <el-table :data="myApps" stripe>
          <el-table-column prop="title"       label="职位"   min-width="140" />
          <el-table-column prop="companyName" label="企业"   width="140" />
          <el-table-column prop="position"    label="岗位"   width="120" />
          <el-table-column prop="applyTime"   label="投递时间" width="160" />
          <el-table-column prop="status"      label="状态"   width="100">
            <template #default="{ row }">
              <el-tag :type="['info','success','danger'][row.status]">
                {{ ['已投递','有意向','不匹配'][row.status] }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!myApps.length" description="暂无投递记录" />
      </el-tab-pane>
    </el-tabs>

    <!-- 投递弹窗 -->
    <el-dialog v-model="applyVisible" title="投递简历" width="420px">
      <p>职位：<b>{{ currentJob?.title }}</b></p>
      <el-input v-model="applyMsg" type="textarea" :rows="3" placeholder="留言（可选）" />
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applyLoading" @click="submitApply">确认投递</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRecruitmentList, applyJob, getMyApplications } from '../../services/recruitment.js'

const activeTab = ref('list')
const jobs = ref([])
const myApps = ref([])
const applyVisible = ref(false)
const currentJob = ref(null)
const applyMsg = ref('')
const applyLoading = ref(false)

const loadJobs = async () => {
  jobs.value = await getRecruitmentList().catch(() => []) || []
}

const loadMyApps = async () => {
  myApps.value = await getMyApplications().catch(() => []) || []
}

const openApply = (job) => {
  currentJob.value = job
  applyMsg.value = ''
  applyVisible.value = true
}

const submitApply = async () => {
  applyLoading.value = true
  try {
    await applyJob(currentJob.value.id, applyMsg.value)
    ElMessage.success('投递成功')
    applyVisible.value = false
    loadMyApps()
  } catch (e) {
    ElMessage.error('投递失败，可能已投递过该职位')
  } finally {
    applyLoading.value = false
  }
}

onMounted(() => { loadJobs(); loadMyApps() })
</script>

<style scoped>
.job-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.job-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.salary { color: #E6A23C; font-weight: 600; }
.job-loc { color: #909399; font-size: 13px; margin-bottom: 6px; }
.job-req { color: #606266; font-size: 13px; line-height: 1.5; }
</style>
