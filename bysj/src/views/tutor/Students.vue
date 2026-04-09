<template>
  <div class="tutor-students">
    <h3>我的学生</h3>

    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <el-input v-model="searchKeyword" placeholder="搜索学生姓名或学号"
            clearable style="width:220px" @input="handleSearch">
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
        </div>
      </template>

      <el-table :data="students" style="width:100%" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="110" />
        <el-table-column prop="studentNo"   label="学号"     width="140" />
        <el-table-column prop="college"     label="学院"     width="130" />
        <el-table-column prop="major"       label="专业"     min-width="130" />
        <el-table-column prop="className"   label="班级"     width="110" />
        <el-table-column prop="bindTime"    label="绑定时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewStudentProfile(scope.row)">查看档案</el-button>
            <el-button size="small" type="success" @click="openQuickComment(scope.row)">写评语</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="students.length === 0" description="暂无绑定的学生" />
    </el-card>

    <!-- 快捷写评语对话框 -->
    <el-dialog
      v-model="commentDialogVisible"
      :title="`给「${commentTarget?.studentName}」写评语`"
      width="600px"
      destroy-on-close
    >
      <el-form label-width="80px" style="margin-bottom:8px">
        <el-form-item label="评语类型">
          <el-radio-group v-model="commentForm.targetType" @change="onTypeChange">
            <el-radio-button value="GENERAL">综合评价</el-radio-button>
            <el-radio-button value="DIARY">日记批注</el-radio-button>
            <el-radio-button value="PORTFOLIO">作品点评</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <!-- 日记批注：选择具体日记 -->
        <el-form-item v-if="commentForm.targetType === 'DIARY'" label="选择日记">
          <el-select v-model="commentForm.targetId" placeholder="请选择要批注的日记"
            style="width:100%" v-loading="targetLoading">
            <el-option v-for="d in diaryOptions" :key="d.id"
              :label="`${d.title}（${formatDate(d.recordDate || d.createTime)}）`"
              :value="d.id" />
            <template #empty>
              <div style="padding:12px;color:#909399;text-align:center">该学生暂无公开日记</div>
            </template>
          </el-select>
        </el-form-item>

        <!-- 作品点评：选择具体作品 -->
        <el-form-item v-if="commentForm.targetType === 'PORTFOLIO'" label="选择作品">
          <el-select v-model="commentForm.targetId" placeholder="请选择要点评的作品"
            style="width:100%" v-loading="targetLoading">
            <el-option v-for="p in portfolioOptions" :key="p.id"
              :label="`${p.title}（${p.workType}）`"
              :value="p.id" />
            <template #empty>
              <div style="padding:12px;color:#909399;text-align:center">该学生暂无作品</div>
            </template>
          </el-select>
        </el-form-item>

        <el-form-item label="评语内容">
          <WangEditor
            ref="commentEditorRef"
            v-model="commentForm.content"
            height="260px"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="commentDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="commentLoading" @click="submitQuickComment">
          提交评语
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { api } from '@/services/api'
import { saveComment } from '@/services/comment'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import WangEditor from '@/components/WangEditor.vue'

const router = useRouter()
const userStore = useUserStore()

const allStudents = ref([])
const students = ref([])
const searchKeyword = ref('')

// ===== 快捷评语 =====
const commentDialogVisible = ref(false)
const commentTarget = ref(null)
const commentLoading = ref(false)
const targetLoading = ref(false)
const diaryOptions = ref([])
const portfolioOptions = ref([])
const commentEditorRef = ref(null)
const commentForm = ref({ targetType: 'GENERAL', targetId: null, content: '' })

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : ''

const loadStudents = async () => {
  if (!userStore.userInfo?.userId) return
  try {
    const relations = await api.get(`/relation/tutor/${userStore.userInfo.userId}`) || []
    const bound = relations.filter(r => r.status === 1)
    const enriched = await Promise.all(
      bound.map(async (r) => {
        try {
          const profile = await api.get(`/student/profile/${r.studentId}`)
          return {
            id: r.id, studentId: r.studentId,
            studentName: profile?.realName || ('学生' + r.studentId),
            studentNo: profile?.studentNo || '-',
            college: profile?.college || '-',
            major: profile?.major || '-',
            className: profile?.className || '-',
            enrollmentYear: profile?.enrollmentYear || '-',
            bindTime: r.auditTime ? new Date(r.auditTime).toLocaleString() : '-'
          }
        } catch {
          return { id: r.id, studentId: r.studentId, studentName: '学生' + r.studentId,
            studentNo: '-', college: '-', major: '-', className: '-', enrollmentYear: '-', bindTime: '-' }
        }
      })
    )
    allStudents.value = enriched
    students.value = enriched
  } catch {
    ElMessage.error('加载学生列表失败')
  }
}

const handleSearch = () => {
  if (!searchKeyword.value) { students.value = allStudents.value; return }
  const kw = searchKeyword.value.toLowerCase()
  students.value = allStudents.value.filter(s =>
    s.studentName.toLowerCase().includes(kw) || s.studentNo.toLowerCase().includes(kw)
  )
}

const viewStudentProfile = (student) => router.push(`/tutor/student/${student.studentId}`)

const openQuickComment = async (student) => {
  commentTarget.value = student
  commentForm.value = { targetType: 'GENERAL', targetId: null, content: '' }
  diaryOptions.value = []
  portfolioOptions.value = []
  commentDialogVisible.value = true
}

const onTypeChange = async (type) => {
  commentForm.value.targetId = null
  if (!commentTarget.value) return
  targetLoading.value = true
  try {
    if (type === 'DIARY') {
      diaryOptions.value = await api.get('/diary/list', {
        params: { studentId: commentTarget.value.studentId }
      }) || []
    } else if (type === 'PORTFOLIO') {
      portfolioOptions.value = await api.get(`/portfolio/${commentTarget.value.studentId}`) || []
    }
  } catch { /* 忽略 */ } finally {
    targetLoading.value = false
  }
}

const submitQuickComment = async () => {
  const content = commentEditorRef.value?.getHtml() || commentForm.value.content
  if (!content || content === '<p><br></p>') {
    ElMessage.warning('请输入评语内容')
    return
  }
  // 日记/作品类型必须选择关联对象
  if (commentForm.value.targetType === 'DIARY' && !commentForm.value.targetId) {
    ElMessage.warning('请选择要批注的日记')
    return
  }
  if (commentForm.value.targetType === 'PORTFOLIO' && !commentForm.value.targetId) {
    ElMessage.warning('请选择要点评的作品')
    return
  }
  commentLoading.value = true
  try {
    await saveComment({
      studentId: commentTarget.value.studentId,
      targetType: commentForm.value.targetType,
      targetId: commentForm.value.targetId || null,
      content
    })
    ElMessage.success('评语提交成功')
    commentDialogVisible.value = false
  } catch { /* api.js 已弹错误 */ } finally {
    commentLoading.value = false
  }
}

onMounted(loadStudents)
</script>

<style scoped>
.tutor-students { padding: 0; }
.section-card { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
