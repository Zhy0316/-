<template>
  <div class="tutor-pending">

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <el-tabs v-model="activeTab" @tab-change="handleTabChange">
            <el-tab-pane label="绑定申请" name="relation">
              <template #label>
                <span>绑定申请 <el-badge :value="relationCount" :hidden="relationCount === 0" type="warning" /></span>
              </template>
            </el-tab-pane>
            <el-tab-pane label="获奖审核" name="award">
              <template #label>
                <span>获奖审核 <el-badge :value="awardCount" :hidden="awardCount === 0" type="warning" /></span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>

      <!-- 绑定申请列表 -->
      <div v-if="activeTab === 'relation'">
        <div v-if="relationList.length === 0">
          <el-empty description="暂无待审核申请" />
        </div>

        <div v-for="item in relationList" :key="item.id" class="apply-card">
          <div class="apply-left">
            <el-avatar :size="52" :src="item.avatar ? 'http://localhost:8083/uploads/' + item.avatar : ''">
              {{ item.studentName?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="apply-body">
            <div class="apply-header">
              <span class="apply-name">{{ item.studentName }}</span>
              <el-tag size="small" type="info">{{ item.college }}</el-tag>
              <el-tag size="small">{{ item.major }}</el-tag>
              <span class="apply-time">{{ formatTime(item.applyTime) }}</span>
            </div>
            <div class="apply-meta">
              <span>学号：{{ item.studentNo || '-' }}</span>
              <span>班级：{{ item.className || '-' }}</span>
              <span>入学：{{ item.enrollmentYear || '-' }}</span>
              <el-button
                v-if="item.resumeFile"
                size="small" text type="primary"
                @click="openFile(item.resumeFile)"
              >📎 查看简历</el-button>
              <el-button
                v-if="item.attachFile"
                size="small" text type="success"
                @click="openFile(item.attachFile)"
              >📎 查看附件</el-button>
            </div>

            <div v-if="item.applyNote" class="apply-note">
              <div class="note-label">自荐信：</div>
              <div class="note-content" :class="{ collapsed: !item.expanded }">
                {{ item.applyNote }}
              </div>
              <el-button
                v-if="item.applyNote.length > 100"
                size="small" text
                @click="item.expanded = !item.expanded"
              >{{ item.expanded ? '收起' : '展开全文' }}</el-button>
            </div>
            <div v-else class="apply-note-empty">未填写自荐信</div>
          </div>

          <div class="apply-actions">
            <el-button type="primary" size="small" @click="viewStudentDetail(item)">查看档案</el-button>
            <el-button type="success" size="small" @click="approveRelation(item)">通过</el-button>
            <el-button type="danger"  size="small" @click="rejectRelation(item)">拒绝</el-button>
          </div>
        </div>
      </div>

      <!-- 获奖审核列表 -->
      <div v-if="activeTab === 'award'">
        <div v-if="awardList.length === 0">
          <el-empty description="暂无待审核获奖记录" />
        </div>

        <div v-for="item in awardList" :key="item.id" class="award-card">
          <div class="award-header">
            <div class="student-info">
              <el-avatar :size="48">{{ item.studentName?.charAt(0) || '?' }}</el-avatar>
              <div class="student-detail">
                <span class="student-name">{{ item.studentName || '未知' }}</span>
                <span class="student-no">{{ item.studentNo }}</span>
              </div>
            </div>
            <span class="submit-time">获奖日期：{{ formatDate(item.awardDate) }}</span>
          </div>

          <div class="award-content">
            <div class="award-info">
              <div class="award-name-row">
                <span class="award-name">{{ item.awardName }}</span>
                <span class="level-tag" :class="getLevelClass(item.awardLevel)">{{ item.awardLevel }}</span>
                <span class="score-tag">+{{ item.awardScore || 0 }}分</span>
              </div>
              <div class="award-meta">
                <span>📅 {{ formatDate(item.awardDate) }}</span>
                <span>🏢 {{ item.issuingAuthority }}</span>
              </div>
              <div v-if="item.description" class="award-desc">
                <div class="award-desc-content" :class="{ collapsed: !item.showDescription }">
                  {{ item.description }}
                </div>
                <el-button
                  v-if="item.description.length > 100"
                  size="small" text
                  @click="item.showDescription = !item.showDescription"
                >{{ item.showDescription ? '收起' : '展开全部' }}</el-button>
              </div>
            </div>

            <div class="cert-section" v-if="item.certFile">
              <div class="cert-label">证书附件</div>
              <el-button size="small" type="primary" plain @click="previewCert(item)">
                <el-icon><ZoomIn /></el-icon>
                查看证书
              </el-button>
            </div>
          </div>

          <div class="award-actions">
            <el-button type="success" @click="approveAward(item)">通过</el-button>
            <el-button type="danger" @click="rejectAward(item)">驳回</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 学生档案预览弹窗 -->
    <el-dialog v-model="previewVisible" :title="previewData?.realName + ' 的基本信息'" width="560px">
      <el-descriptions :column="2" border v-if="previewData">
        <el-descriptions-item label="姓名">{{ previewData.realName }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ previewData.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ previewData.college }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ previewData.major }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ previewData.className }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ previewData.enrollmentYear }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ previewData.phone }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ previewData.email }}</el-descriptions-item>
        <el-descriptions-item label="政治面貌">{{ previewData.politicalStatus || '-' }}</el-descriptions-item>
        <el-descriptions-item label="民族">{{ previewData.nation || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="previewData?.resumeFile" style="margin-top:12px">
        <el-button type="primary" plain size="small"
          @click="openFile(previewData.resumeFile)">📎 查看个人简历</el-button>
      </div>
    </el-dialog>

    <!-- 证书预览弹窗 -->
    <el-dialog v-model="certPreviewVisible" title="证书预览" width="80%">
      <div class="cert-preview-dialog">
        <!-- 图片文件 -->
        <img v-if="isImageFile(certPreviewUrl)" :src="certPreviewUrl" alt="证书" class="cert-full" />
        <!-- PDF文件 -->
        <iframe v-else-if="isPDFFile(certPreviewUrl)" :src="certPreviewUrl" class="pdf-preview"></iframe>
        <!-- 其他文件 -->
        <div v-else class="file-preview">
          <el-icon class="file-icon"><Document /></el-icon>
          <div class="file-name">{{ currentFileName }}</div>
          <el-button type="primary" @click="downloadFile()">下载文件</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 驳回备注弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回申请" width="500px">
      <el-form>
        <el-form-item label="驳回原因">
          <el-input
            v-model="rejectNote"
            type="textarea"
            :rows="4"
            placeholder="请填写驳回原因（选填）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'
import { ZoomIn, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()

const activeTab = ref('relation')
const relationList = ref([])
const awardList = ref([])
const relationCount = ref(0)
const awardCount = ref(0)
const previewVisible = ref(false)
const previewData = ref(null)
const certPreviewVisible = ref(false)
const certPreviewUrl = ref('')
const currentFileName = ref('')
const currentFileItem = ref(null)
const rejectDialogVisible = ref(false)
const rejectNote = ref('')
const currentRejectItem = ref(null)
const currentRejectType = ref('')

const loadRelation = async () => {
  const tutorId = userStore.userInfo?.userId
  if (!tutorId) return
  try {
    const list = await api.get(`/relation/tutor/${tutorId}/pending-detail`) || []
    relationList.value = list.map(item => ({ ...item, expanded: false }))
    relationCount.value = list.length
  } catch {
    ElMessage.error('加载绑定申请失败')
  }
}

const loadAward = async () => {
  const tutorId = userStore.userInfo?.userId
  if (!tutorId) return
  try {
    const list = await api.get(`/award/tutor-pending/${tutorId}`) || []
    awardList.value = list.map(item => ({ ...item, showDescription: false }))
    awardCount.value = list.length
  } catch {
    ElMessage.error('加载获奖审核失败')
  }
}

const handleTabChange = (tab) => {
  if (tab === 'relation') {
    loadRelation()
  } else {
    loadAward()
  }
}

const approveRelation = async (item) => {
  await ElMessageBox.confirm(`确定通过「${item.studentName}」的绑定申请？`, '确认', { type: 'success' })
  await api.put('/relation/audit', { id: item.id, status: 1 })
  ElMessage.success('已通过')
  loadRelation()
}

const rejectRelation = async (item) => {
  currentRejectItem.value = item
  currentRejectType.value = 'relation'
  rejectNote.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  const item = currentRejectItem.value
  if (!item) return
  
  if (currentRejectType.value === 'relation') {
    await ElMessageBox.confirm(`确定拒绝「${item.studentName}」的绑定申请？`, '确认', { type: 'warning' })
    await api.put('/relation/audit', { id: item.id, status: 2, rejectNote: rejectNote.value })
    ElMessage.success('已拒绝')
    loadRelation()
  } else {
    await ElMessageBox.confirm(`确定驳回「${item.studentName}」的获奖记录「${item.awardName}」？`, '确认审核', { type: 'warning' })
    await api.put(`/award/audit/${item.id}`, { auditStatus: 2, rejectNote: rejectNote.value })
    ElMessage.success('已驳回')
    loadAward()
  }
  
  rejectDialogVisible.value = false
}

const approveAward = async (item) => {
  await ElMessageBox.confirm(`确定通过「${item.studentName}」的获奖记录「${item.awardName}」？`, '确认审核', { type: 'success' })
  await api.put(`/award/audit/${item.id}`, { auditStatus: 1 })
  ElMessage.success('审核通过')
  loadAward()
}

const rejectAward = async (item) => {
  currentRejectItem.value = item
  currentRejectType.value = 'award'
  rejectNote.value = ''
  rejectDialogVisible.value = true
}

const viewStudentDetail = async (item) => {
  try {
    const data = await api.get(`/relation/student-preview/${item.studentId}`)
    previewData.value = data
    previewVisible.value = true
  } catch {
    ElMessage.error('加载学生信息失败')
  }
}

const getCertPreviewUrl = (item) => {
  if (!item.certFile) return ''
  const url = item.certFile
  return url.startsWith('http') ? url : `http://localhost:8083${url}`
}

const previewCert = (item) => {
  certPreviewUrl.value = getCertPreviewUrl(item)
  currentFileItem.value = item
  currentFileName.value = item.certFile.split('/').pop() || '证书文件'
  certPreviewVisible.value = true
}

const isImageFile = (url) => {
  if (!url) return false
  const ext = url.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
}

const isPDFFile = (url) => {
  if (!url) return false
  const ext = url.split('.').pop().toLowerCase()
  return ext === 'pdf'
}

const downloadFile = () => {
  if (!currentFileItem.value?.certFile) return
  window.open(getCertPreviewUrl(currentFileItem.value), '_blank')
}

const openFile = (path) => {
  window.open('http://localhost:8083' + path, '_blank')
}

const formatTime = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleString('zh-CN', { hour12: false })
}

const formatDate = (d) => {
  if (!d) return ''
  return new Date(d).toLocaleDateString('zh-CN')
}

const getLevelClass = (level) => {
  const map = { '国家级': 'danger', '省级': 'warning', '市级': 'success', '校级': 'info', '院级': 'info' }
  return map[level] || 'info'
}

onMounted(() => {
  loadRelation()
  loadAward()
})
</script>

<style scoped>
.tutor-pending { padding: 4px; }

:deep(.el-tabs__header) {
  margin-bottom: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.apply-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  background: #fafafa;
  transition: box-shadow .2s;
}
.apply-card:hover { box-shadow: 0 2px 8px rgba(0,0,0,.08); }

.apply-left { flex-shrink: 0; }

.apply-body { flex: 1; min-width: 0; }

.apply-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  flex-wrap: wrap;
}
.apply-name { font-size: 16px; font-weight: 600; color: #303133; }
.apply-time { font-size: 12px; color: #c0c4cc; margin-left: auto; }

.apply-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
  flex-wrap: wrap;
}

.apply-note {
  background: #f0f7ff;
  border-left: 3px solid #409EFF;
  border-radius: 4px;
  padding: 8px 12px;
  margin-top: 6px;
}
.note-label { font-size: 12px; color: #909399; margin-bottom: 4px; }
.note-content {
  font-size: 13px;
  color: #303133;
  line-height: 1.7;
  white-space: pre-wrap;
}
.note-content.collapsed {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.apply-note-empty { font-size: 12px; color: #c0c4cc; margin-top: 6px; }

.apply-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex-shrink: 0;
  justify-content: center;
}

/* 获奖卡片样式 */
.award-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
}

.award-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e4e7ed;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.student-detail {
  display: flex;
  flex-direction: column;
}

.student-name {
  font-weight: bold;
  color: #303133;
}

.student-no {
  font-size: 13px;
  color: #909399;
}

.submit-time {
  font-size: 13px;
  color: #909399;
}

.award-content {
  padding: 20px;
}

.award-info {
  margin-bottom: 15px;
}

.award-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.award-name {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.level-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  background: #ecf5ff;
  color: #409eff;
}
.level-tag.danger { background: #fef0f0; color: #f56c6c; }
.level-tag.warning { background: #fdf6ec; color: #e6a23c; }
.level-tag.success { background: #f0f9eb; color: #67c23a; }
.level-tag.info { background: #ecf5ff; color: #909399; }

.score-tag {
  font-size: 14px;
  font-weight: bold;
  color: #E6A23C;
}

.award-meta {
  display: flex;
  gap: 20px;
  color: #606266;
  font-size: 14px;
  margin-bottom: 10px;
}

.award-desc {
  padding: 10px 15px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-top: 10px;
}

.award-desc-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
}

.award-desc-content.collapsed {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.cert-section {
  border-top: 1px dashed #e4e7ed;
  padding-top: 15px;
  margin-top: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.cert-label {
  font-size: 14px;
  color: #909399;
}

.pdf-preview {
  width: 100%;
  height: 70vh;
  border: none;
  border-radius: 8px;
}

.file-preview {
  padding: 50px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.file-icon {
  font-size: 64px;
  color: #909399;
}

.file-name {
  font-size: 16px;
  color: #303133;
}

.award-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 15px 20px;
  background: #fafafa;
  border-top: 1px solid #e4e7ed;
}

.cert-preview-dialog {
  text-align: center;
  padding: 20px;
}

.cert-full {
  max-width: 100%;
  max-height: 70vh;
  border-radius: 8px;
}

.no-cert {
  padding: 50px;
  color: #909399;
}
</style>
