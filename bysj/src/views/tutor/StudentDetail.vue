<template>
  <div>
    <el-page-header @back="$router.back()" :content="student?.realName + ' 的档案'" style="margin-bottom:20px" />

    <el-tabs v-model="activeTab">
      <!-- 基本信息 -->
      <el-tab-pane label="基本信息" name="profile">
        <el-descriptions :column="3" border v-if="profile">
          <el-descriptions-item label="姓名">{{ profile.realName }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ profile.studentNo }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.gender === 'M' ? '男' : '女' }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ profile.college }}</el-descriptions-item>
          <el-descriptions-item label="专业">{{ profile.major }}</el-descriptions-item>
          <el-descriptions-item label="班级">{{ profile.className }}</el-descriptions-item>
          <el-descriptions-item label="入学年份">{{ profile.enrollmentYear }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ profile.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>

      <!-- 学业成绩 -->
      <el-tab-pane label="学业成绩" name="academic">
        <el-table :data="academics" stripe size="small">
          <el-table-column prop="academicYear" label="学年" width="140" />
          <el-table-column prop="courseName"   label="课程名称" min-width="140" />
          <el-table-column prop="courseNature" label="性质" width="80" />
          <el-table-column prop="credit"       label="学分" width="70" />
          <el-table-column prop="score"        label="成绩" width="80" />
          <el-table-column prop="gpa"          label="绩点" width="80" />
        </el-table>
      </el-tab-pane>

      <!-- 获奖记录 -->
      <el-tab-pane label="获奖记录" name="award">
        <el-table :data="awards" stripe size="small">
          <el-table-column prop="awardName"       label="奖项名称" min-width="140" />
          <el-table-column prop="awardLevel"      label="级别" width="90" />
          <el-table-column prop="issuingAuthority" label="颁发单位" width="140" />
          <el-table-column prop="awardDate"       label="获奖日期" width="110" />
          <el-table-column prop="auditStatus"     label="审核状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.auditStatus === 1 ? 'success' : 'warning'">
                {{ row.auditStatus === 1 ? '已通过' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button v-if="row.auditStatus === 0" size="small" type="success"
                @click="auditAward(row.id, 1)">通过</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 成长记录 -->
      <el-tab-pane label="成长日记" name="diary">
        <el-timeline>
          <el-timeline-item v-for="d in diaries" :key="d.id"
            :timestamp="d.recordDate" placement="top">
            <el-card>
              <div style="font-weight:600;margin-bottom:6px">{{ d.title }}</div>
              <div style="color:#606266;font-size:13px">{{ d.content?.slice(0, 120) }}...</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!diaries.length" description="暂无成长日记" />
      </el-tab-pane>

      <!-- 添加评语 -->
      <el-tab-pane label="添加评语" name="comment">
        <el-form label-width="80px" style="max-width:600px">
          <el-form-item label="评语类型">
            <el-select v-model="commentForm.targetType" style="width:100%">
              <el-option label="综合评价" value="GENERAL" />
              <el-option label="日记批注" value="DIARY" />
              <el-option label="作品点评" value="PORTFOLIO" />
            </el-select>
          </el-form-item>
          <el-form-item label="评语内容">
            <el-input v-model="commentForm.content" type="textarea" :rows="5"
              placeholder="请输入对该学生的指导意见或评语..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="commentLoading" @click="submitComment">
              提交评语
            </el-button>
          </el-form-item>
        </el-form>

        <el-divider>历史评语</el-divider>
        <el-timeline>
          <el-timeline-item v-for="c in comments" :key="c.id"
            :timestamp="c.createTime" placement="top">
            <el-card>
              <el-tag size="small" style="margin-bottom:6px">{{ c.targetType }}</el-tag>
              <div style="color:#303133;margin-top:4px">{{ c.content }}</div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-if="!comments.length" description="暂无评语记录" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'
import { saveComment, getCommentList } from '../../services/comment.js'

const route = useRoute()
const userStore = useUserStore()
const studentId = Number(route.params.id)

const activeTab = ref('profile')
const profile = ref(null)
const academics = ref([])
const awards = ref([])
const diaries = ref([])
const comments = ref([])
const commentLoading = ref(false)
const commentForm = ref({ targetType: 'GENERAL', content: '' })

const loadAll = async () => {
  try {
    profile.value   = await api.get(`/student/profile/${studentId}`)
    academics.value = await api.get(`/academic/${studentId}`) || []
    awards.value    = await api.get(`/award/${studentId}`) || []
    const diaryRes  = await api.get('/diary/list', { params: { studentId, onlyPublic: true } })
    diaries.value   = diaryRes || []
    comments.value  = await getCommentList(studentId) || []
  } catch (e) {
    ElMessage.error('加载学生档案失败')
  }
}

const auditAward = async (id, status) => {
  await api.put(`/award/audit/${id}`, { auditStatus: status })
  ElMessage.success('审核成功')
  awards.value = await api.get(`/award/${studentId}`) || []
}

const submitComment = async () => {
  if (!commentForm.value.content.trim()) return ElMessage.warning('请输入评语内容')
  commentLoading.value = true
  try {
    await saveComment({ studentId, ...commentForm.value })
    ElMessage.success('评语提交成功')
    commentForm.value.content = ''
    comments.value = await getCommentList(studentId) || []
  } catch (e) {
    ElMessage.error('提交失败')
  } finally {
    commentLoading.value = false
  }
}

onMounted(loadAll)
</script>
