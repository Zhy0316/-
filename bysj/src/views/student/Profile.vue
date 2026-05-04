<template>
  <div class="profile-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人基本信息</span>
          <el-button type="primary" @click="saveProfile" :loading="loading">保存修改</el-button>
        </div>
      </template>
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="头像">
              <div class="avatar-section">
                <el-avatar :size="80" :src="avatarUrl" class="avatar-preview">
                  {{ form.realName?.charAt(0) || '用户' }}
                </el-avatar>
                <div class="avatar-upload">
                  <input type="file" ref="avatarInput" @change="handleAvatarChange" accept="image/*" style="display:none" />
                  <el-button size="small" @click="triggerAvatarInput" :loading="uploadingAvatar">
                    <el-icon><Upload /></el-icon> 上传头像
                  </el-button>
                  <div class="avatar-tip">支持 JPG/PNG，建议尺寸 200x200</div>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider />

        <!-- 基本账户信息 (不可修改或部分可修改) -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" disabled></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="M"></el-option>
                <el-option label="女" value="F"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="form.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">学籍信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="学院" prop="college">
              <el-input v-model="form.college"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="className">
              <el-input v-model="form.className"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
             <el-form-item label="入学年份">
               <el-date-picker
                 v-model="form.enrollmentYear"
                 type="year"
                 value-format="YYYY"
                 placeholder="选择年份"
                 style="width: 100%"
               />
             </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">指导教师</el-divider>

        <!-- 已绑定状态 -->
        <el-row v-if="boundTutor" :gutter="20" style="margin-bottom:8px">
          <el-col :span="24">
            <el-form-item label="当前导师">
              <el-descriptions :column="3" border size="small" style="width:100%">
                <el-descriptions-item label="姓名">{{ boundTutor.realName }}</el-descriptions-item>
                <el-descriptions-item label="职称">{{ boundTutor.title }}</el-descriptions-item>
                <el-descriptions-item label="研究方向">{{ boundTutor.researchField }}</el-descriptions-item>
              </el-descriptions>
              <el-tag type="success" style="margin-top:8px">已绑定</el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 待审核状态 -->
        <el-row v-else-if="pendingRelation" :gutter="20" style="margin-bottom:8px">
          <el-col :span="24">
            <el-form-item label="绑定状态">
              <el-alert type="warning" :closable="false"
                :title="`已向「${pendingRelation.tutorName || '导师'}」发送申请，等待审核中...`" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 被驳回状态 -->
        <el-row v-else-if="rejectedRelation" :gutter="20" style="margin-bottom:8px">
          <el-col :span="24">
            <el-form-item label="绑定状态">
              <el-alert type="error" :closable="false" title="申请被驳回">
                <template #default>
                  <div v-if="rejectedRelation.rejectNote" style="margin-top:8px">
                    <strong>驳回原因：</strong>{{ rejectedRelation.rejectNote }}
                  </div>
                  <div style="margin-top:10px">
                    <el-button type="primary" @click="showResubmitDialog">重新提交申请</el-button>
                  </div>
                </template>
              </el-alert>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 未绑定：展示导师列表供选择 -->
        <template v-else>
          <el-row :gutter="20" style="margin-bottom:8px">
            <el-col :span="24">
              <el-form-item label="选择导师">
                <el-input v-model="tutorKeyword" placeholder="搜索姓名/研究方向" clearable style="width:220px;margin-bottom:10px">
                  <template #prefix><el-icon><Search /></el-icon></template>
                </el-input>
                <el-table :data="filteredTutors" stripe size="small" style="width:100%" v-loading="tutorLoading">
                  <el-table-column prop="realName" label="姓名" width="100" />
                  <el-table-column prop="title" label="职称" width="110" />
                  <el-table-column prop="researchField" label="研究方向" />
                  <el-table-column label="操作" width="100">
                    <template #default="{ row }">
                      <el-button size="small" type="primary" @click="applyBind(row)">申请绑定</el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <el-empty v-if="!tutorLoading && filteredTutors.length === 0" description="暂无导师数据" :image-size="60" />
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <el-divider content-position="left">详细信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政治面貌" prop="politicalStatus">
              <el-select v-model="form.politicalStatus" placeholder="请选择政治面貌" style="width: 100%">
                <el-option v-for="item in politicalOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族" prop="nation">
              <el-select v-model="form.nation" filterable placeholder="请选择民族" style="width: 100%">
                <el-option v-for="item in nationOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="籍贯" prop="nativePlace">
              <el-input v-model="form.nativePlace" placeholder="请输入籍贯 (如: xx省xx市)"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">附加材料</el-divider>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="个人简历" prop="resumeFile">
              <div style="display: flex; flex-direction: column; gap: 10px;">
                <input type="file" ref="fileInput" @change="handleFileChange" style="display: none" />
                <div>
                   <el-button type="success" size="small" @click.prevent="triggerFileInput">选择文件并上传</el-button>
                   <span v-if="selectedFile" style="margin-left: 15px; font-size: 13px; color: #666;">已选择: {{ selectedFile.name }} (将在保存时上传)</span>
                </div>
                <div v-if="form.resumeFile" style="margin-top: 10px;">
                  <a :href="getAttachmentUrl(form.resumeFile)" target="_blank" style="color: #3498db; text-decoration: none; font-size: 14px;">
                    📎 预览/下载当前简历
                  </a>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>
    </el-card>

    <!-- 申请绑定导师对话框 -->
    <el-dialog
      v-model="applyDialogVisible"
      :title="rejectedRelation ? '重新提交申请' : '申请绑定导师'"
      width="600px"
      :before-close="handleApplyDialogClose"
    >
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="导师">
          <el-input :value="selectedTutor ? selectedTutor.realName : ''" disabled></el-input>
        </el-form-item>
        <el-form-item label="自荐信" prop="applyNote">
          <el-input
            v-model="applyForm.applyNote"
            type="textarea"
            :rows="6"
            placeholder="请描述您申请该导师的原因、目标、学习计划等"
            maxlength="1000"
            show-word-limit
          ></el-input>
        </el-form-item>
        <el-form-item label="附件">
          <div style="display: flex; flex-direction: column; gap: 10px;">
            <input type="file" ref="attachFileInput" @change="handleAttachFileChange" style="display: none" />
            <div>
               <el-button type="primary" size="small" @click="triggerAttachFileInput">选择附件</el-button>
               <span v-if="selectedAttachFile" style="margin-left: 15px; font-size: 13px; color: #666;">
                 已选择: {{ selectedAttachFile.name }}
               </span>
            </div>
            <div v-if="rejectedRelation && rejectedRelation.attachFile && !selectedAttachFile" style="font-size: 12px; color: #909399;">
              当前附件: <a :href="getAttachmentUrl(rejectedRelation.attachFile)" target="_blank" style="color: #409EFF;">预览/下载</a>
            </div>
            <div v-if="selectedAttachFile" style="font-size: 12px; color: #909399;">
              文件大小: {{ (selectedAttachFile.size / 1024).toFixed(2) }} KB
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="rejectedRelation ? submitResubmit() : submitApply()" :loading="applySubmitting">
          {{ rejectedRelation ? '重新提交' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Upload } from '@element-plus/icons-vue'
import { studentAPI } from '../../services/student.js'
import { api } from '../../services/api.js'
import { useUserStore } from '../../stores/user.js'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const fileInput = ref(null)
const selectedFile = ref(null)
const avatarInput = ref(null)
const selectedAvatar = ref(null)
const uploadingAvatar = ref(false)

// 申请绑定对话框相关
const applyDialogVisible = ref(false)
const applySubmitting = ref(false)
const selectedTutor = ref(null)
const attachFileInput = ref(null)
const selectedAttachFile = ref(null)
const applyForm = reactive({
  applyNote: ''
})

// 头像相关
const triggerAvatarInput = () => avatarInput.value.click()

const handleAvatarChange = (e) => {
  if (e.target.files.length > 0) {
    const file = e.target.files[0]
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('头像大小不能超过 5MB')
      return
    }
    selectedAvatar.value = file
    uploadAvatar()
  }
}

const uploadAvatar = async () => {
  if (!selectedAvatar.value) return
  const userId = userStore.userInfo?.userId
  if (!userId) return

  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedAvatar.value)
    const res = await studentAPI.uploadAvatar(formData)
    const avatarPath = res?.filePath || res?.avatar || res
    form.avatar = avatarPath
    userStore.updateAvatar(avatarPath)
    ElMessage.success('头像上传成功')
    selectedAvatar.value = null
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

const avatarUrl = computed(() => {
  if (selectedAvatar.value) {
    return URL.createObjectURL(selectedAvatar.value)
  }
  if (form.avatar) {
    return form.avatar.startsWith('http') ? form.avatar : `http://localhost:8083${form.avatar}`
  }
  return ''
})

// ===== 导师绑定相关 =====
const tutors = ref([])
const tutorLoading = ref(false)
const tutorKeyword = ref('')
const boundTutor = ref(null)       // 已绑定的导师信息
const pendingRelation = ref(null)  // 待审核的申请
const rejectedRelation = ref(null) // 被驳回的申请

const filteredTutors = computed(() => {
  if (!tutorKeyword.value) return tutors.value
  const kw = tutorKeyword.value.toLowerCase()
  return tutors.value.filter(t =>
    (t.realName || '').toLowerCase().includes(kw) ||
    (t.researchField || '').toLowerCase().includes(kw)
  )
})

const form = reactive({
  userId: null,
  username: '',
  realName: '',
  studentNo: '',
  gender: '',
  phone: '',
  email: '',
  resumeFile: '',
  college: '',
  major: '',
  className: '',
  enrollmentYear: null,
  politicalStatus: '',
  nation: '',
  nativePlace: '',
  birthDate: '',
  avatar: ''
})

const politicalOptions = ['中共党员', '中共预备党员', '共青团员', '群众', '其它']

const nationOptions = [
  '汉族', '壮族', '回族', '满族', '维吾尔族', '苗族', '彝族', '土家族', '藏族', '蒙古族',
  '侗族', '布依族', '瑶族', '白族', '朝鲜族', '哈尼族', '黎族', '哈萨克族', '傣族', '畲族',
  '傈僳族', '东乡族', '仡佬族', '拉祜族', '佤族', '水族', '纳西族', '羌族', '土族', '仫佬族',
  '锡伯族', '柯尔克孜族', '景颇族', '达斡尔族', '撒拉族', '布朗族', '毛南族', '塔吉克族',
  '普米族', '阿昌族', '怒族', '鄂温克族', '京族', '基诺族', '德昂族', '保安族', '俄罗斯族',
  '裕固族', '乌孜别克族', '门巴族', '鄂伦春族', '独龙族', '塔塔尔族', '赫哲族', '珞巴族'
]

const triggerFileInput = () => fileInput.value.click()

const handleFileChange = (e) => {
  if (e.target.files.length > 0) selectedFile.value = e.target.files[0]
}

const getAttachmentUrl = (path) => {
  if (!path) return '#'
  if (path.startsWith('http')) return path
  return `http://localhost:8083${path}`
}

// ===== 加载导师列表 =====
const loadTutors = async () => {
  tutorLoading.value = true
  try {
    tutors.value = await api.get('/student/tutors') || []
  } catch {
    ElMessage.error('加载导师列表失败')
  } finally {
    tutorLoading.value = false
  }
}

// ===== 加载绑定关系 =====
const loadRelations = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    const relations = await api.get(`/relation/student/${userId}`) || []
    const bound = relations.find(r => r.status === 1)
    if (bound) {
      const t = tutors.value.find(t => t.userId === bound.tutorId)
      boundTutor.value = t || { realName: '导师' + bound.tutorId, title: '', researchField: '' }
      pendingRelation.value = null
      rejectedRelation.value = null
      return
    }
    const pending = relations.find(r => r.status === 0)
    if (pending) {
      const t = tutors.value.find(t => t.userId === pending.tutorId)
      pendingRelation.value = { ...pending, tutorName: t?.realName }
      rejectedRelation.value = null
      return
    }
    const rejected = relations.find(r => r.status === 2)
    if (rejected) {
      const t = tutors.value.find(t => t.userId === rejected.tutorId)
      rejectedRelation.value = { ...rejected, tutorName: t?.realName }
      pendingRelation.value = null
    }
  } catch { /* 忽略 */ }
}

// ===== 显示重新提交对话框 =====
const showResubmitDialog = () => {
  selectedTutor.value = tutors.value.find(t => t.userId === rejectedRelation.value.tutorId)
  applyForm.applyNote = rejectedRelation.value.applyNote || ''
  selectedAttachFile.value = null
  applyDialogVisible.value = true
}

// ===== 重新提交申请 =====
const submitResubmit = async () => {
  applySubmitting.value = true
  try {
    const formData = new FormData()
    if (applyForm.applyNote) {
      formData.append('applyNote', applyForm.applyNote)
    }
    if (selectedAttachFile.value) {
      formData.append('attachFile', selectedAttachFile.value)
    }
    
    await api.put(`/relation/resubmit/${rejectedRelation.value.id}`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    ElMessage.success('重新提交成功，等待导师审核')
    pendingRelation.value = { tutorName: rejectedRelation.value.tutorName }
    rejectedRelation.value = null
    applyDialogVisible.value = false
    handleApplyDialogClose()
  } catch (e) {
    ElMessage.error(e?.response?.data || '重新提交失败')
  } finally {
    applySubmitting.value = false
  }
}

// ===== 申请绑定导师 =====
const applyBind = (tutor) => {
  selectedTutor.value = tutor
  applyForm.applyNote = ''
  selectedAttachFile.value = null
  applyDialogVisible.value = true
}

const handleApplyDialogClose = () => {
  applyDialogVisible.value = false
  selectedTutor.value = null
  selectedAttachFile.value = null
  applyForm.applyNote = ''
}

const triggerAttachFileInput = () => attachFileInput.value.click()

const handleAttachFileChange = (e) => {
  if (e.target.files.length > 0) {
    selectedAttachFile.value = e.target.files[0]
  }
}

const submitApply = async () => {
  applySubmitting.value = true
  try {
    const formData = new FormData()
    formData.append('tutorId', selectedTutor.value.userId)
    if (applyForm.applyNote) {
      formData.append('applyNote', applyForm.applyNote)
    }
    if (selectedAttachFile.value) {
      formData.append('attachFile', selectedAttachFile.value)
    }
    
    await api.post('/relation/apply', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
    
    ElMessage.success('申请已发送，等待导师审核')
    pendingRelation.value = { tutorName: selectedTutor.value.realName }
    applyDialogVisible.value = false
    handleApplyDialogClose()
  } catch (e) {
    ElMessage.error(e?.response?.data || '申请失败，可能已存在绑定关系')
  } finally {
    applySubmitting.value = false
  }
}

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  major:     [{ required: true, message: '请输入专业', trigger: 'blur' }],
  college:   [{ required: true, message: '请输入学院', trigger: 'blur' }],
}

const loadProfile = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) { ElMessage.error('无法获取用户信息，请重新登录'); return }
  try {
    const res = await studentAPI.getProfile(userId)
    Object.assign(form, res)
    // el-date-picker type="year" 需要字符串，后端可能返回数字
    if (form.enrollmentYear != null) {
      form.enrollmentYear = String(form.enrollmentYear)
    }
  } catch {
    ElMessage.error('加载个人信息失败')
  }
}

const saveProfile = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const userId = userStore.userInfo?.userId
      // 简历上传
      if (selectedFile.value) {
        const fileData = new FormData()
        fileData.append('file', selectedFile.value)
        const res = await api.post('/student/resume/upload', fileData)
        form.resumeFile = res?.filePath || form.resumeFile
        selectedFile.value = null
      }
      await studentAPI.updateProfile(userId, form)
      ElMessage.success('保存成功')
    } catch {
      ElMessage.error('保存失败')
    } finally {
      loading.value = false
    }
  })
}

onMounted(async () => {
  await loadProfile()
  await loadTutors()
  await loadRelations()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}
.avatar-preview {
  flex-shrink: 0;
  background: #409EFF;
  font-size: 32px;
  color: #fff;
}
.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.avatar-tip {
  font-size: 12px;
  color: #909399;
}
</style>
