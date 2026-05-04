<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span style="font-size:16px;font-weight:600">作品集管理</span>
      <el-button type="primary" @click="openDialog()">+ 上传作品</el-button>
    </div>

    <!-- 作品卡片列表 -->
    <el-row :gutter="16">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom:16px">
        <el-card shadow="hover" class="work-card">
          <!-- 封面图 -->
          <div class="work-cover">
            <img v-if="item.coverUrl" :src="coverSrc(item.coverUrl)" alt="封面"
              @error="e => e.target.style.display='none'" />
            <div v-else class="cover-placeholder">
              <el-icon size="40" color="#c0c4cc"><FolderOpened /></el-icon>
            </div>
          </div>

          <div class="work-body">
            <div class="work-title">{{ item.title }}</div>
            <div style="display:flex;gap:6px;align-items:center;margin-bottom:8px">
              <el-tag size="small">{{ item.workType }}</el-tag>
              <span style="font-size:12px;color:#c0c4cc">
                {{ item.uploadTime ? new Date(item.uploadTime).toLocaleDateString() : '' }}
              </span>
            </div>
            <div class="work-desc">{{ stripHtml(item.description) }}</div>

            <div style="margin-top:12px;display:flex;gap:6px;flex-wrap:wrap">
              <el-button size="small" type="primary" plain @click="openDialog(item)">编辑</el-button>
              <el-button size="small" type="success" plain v-if="item.fileUrl"
                @click="handleDownload(item)">
                <el-icon><Download /></el-icon> 下载
              </el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(item.id)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无作品，快去上传吧" />

    <!-- 上传/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作品' : '上传作品'"
      width="560px" destroy-on-close>
      <el-form :model="form" label-width="90px">
        <el-form-item label="作品标题" required>
          <el-input v-model="form.title" placeholder="请输入作品标题" />
        </el-form-item>

        <el-form-item label="作品类型">
          <el-select v-model="form.workType" style="width:100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <el-form-item label="作品描述">
          <el-input v-model="form.description" type="textarea" :rows="3"
            placeholder="简要描述作品内容..." />
        </el-form-item>

        <el-form-item label="作品文件">
          <el-upload :auto-upload="false" :on-change="onFileChange" :limit="1"
            accept=".zip,.rar,.7z,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.mp4,.avi,.mov">
            <el-button size="small" type="primary" plain>
              <el-icon><Upload /></el-icon> 选择文件（小文件）
            </el-button>
          </el-upload>
          <div style="margin-top:8px;font-size:12px;color:#606266">大文件请直接填写本地路径：</div>
          <el-input v-model="form.localPath" placeholder="例：D:\projects\mywork.zip"
            style="margin-top:4px" clearable>
            <template #prefix><el-icon><FolderOpened /></el-icon></template>
          </el-input>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            填写路径后将直接保存路径，下载时从该路径读取文件
          </div>
          <div v-if="form.id && !selectedFile && !form.localPath" style="font-size:12px;color:#909399;margin-top:4px">
            不选择则保留原文件
          </div>
        </el-form-item>

        <el-form-item label="封面图片">
          <el-upload :auto-upload="false" :on-change="onCoverChange" :limit="1"
            accept=".jpg,.jpeg,.png,.gif" list-type="picture-card"
            :show-file-list="false">
            <div v-if="coverPreview" class="cover-upload-preview">
              <img :src="coverPreview" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <div v-else-if="form.id && currentCoverUrl" class="cover-upload-preview">
              <img :src="coverSrc(currentCoverUrl)" style="width:100%;height:100%;object-fit:cover" />
            </div>
            <el-icon v-else><Plus /></el-icon>
          </el-upload>
          <div style="font-size:12px;color:#909399;margin-top:4px">
            支持 JPG/PNG，建议 4:3 比例
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div>
          <el-progress v-if="saving && uploadProgress > 0"
            :percentage="uploadProgress" style="margin-bottom:12px" />
          <el-button @click="dialogVisible = false" :disabled="saving">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">
            {{ saving ? `上传中 ${uploadProgress}%` : '保存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Download, Upload, Plus, FolderOpened } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'

const userStore = useUserStore()
const list = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const uploadProgress = ref(0)  // 上传进度 0-100
const selectedFile = ref(null)
const selectedCover = ref(null)
const coverPreview = ref('')
const currentCoverUrl = ref('')

const types = ['代码项目', '设计图', '论文/文章', '视频', '其他']
const form = ref({ id: null, title: '', workType: '代码项目', description: '', localPath: '' })

// 封面图片 URL：兼容三种格式——http路径、带前缀base64、裸base64
const coverSrc = (url) => {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('/')) return `http://localhost:8083${url.startsWith('http') ? '' : url}`
  if (url.startsWith('data:')) return url
  // 裸 base64，补上 data URI 前缀
  return `data:image/jpeg;base64,${url}`
}

const stripHtml = (html) => {
  if (!html) return ''
  return html.replace(/<[^>]+>/g, '').slice(0, 80)
}

const loadList = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  list.value = await api.get(`/portfolio/${userId}`).catch(() => []) || []
}

const openDialog = (item = null) => {
  if (item) {
    form.value = { id: item.id, title: item.title, workType: item.workType, description: item.description || '', localPath: '' }
    currentCoverUrl.value = item.coverUrl || ''
  } else {
    form.value = { id: null, title: '', workType: '代码项目', description: '', localPath: '' }
    currentCoverUrl.value = ''
  }
  selectedFile.value = null
  selectedCover.value = null
  coverPreview.value = ''
  dialogVisible.value = true
}

const onFileChange = (file) => {
  // Element Plus on-change 回调参数是 UploadFile，原生 File 在 .raw 里
  selectedFile.value = file.raw || file
}

const onCoverChange = (file) => {
  const raw = file.raw || file
  selectedCover.value = raw
  // 用 createObjectURL 生成本地预览（比 FileReader 更快）
  if (coverPreview.value) URL.revokeObjectURL(coverPreview.value)
  coverPreview.value = URL.createObjectURL(raw)
}

const handleSave = async () => {
  if (!form.value.title) return ElMessage.warning('请填写作品标题')
  saving.value = true
  uploadProgress.value = 0
  try {
    // 优先使用本地路径方案（大文件）
    if (form.value.localPath && form.value.localPath.trim()) {
      const fd = new FormData()
      fd.append('studentId', userStore.userInfo.userId)
      fd.append('title', form.value.title)
      fd.append('workType', form.value.workType)
      fd.append('description', form.value.description || '')
      fd.append('filePath', form.value.localPath.trim())
      if (selectedCover.value) fd.append('cover', selectedCover.value)
      await api.post('/portfolio/path', fd)
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadList()
      return
    }

    // 普通文件上传方案
    const fd = new FormData()
    fd.append('studentId', userStore.userInfo.userId)
    fd.append('title', form.value.title)
    fd.append('workType', form.value.workType)
    fd.append('description', form.value.description || '')
    if (selectedFile.value) fd.append('file', selectedFile.value)
    if (selectedCover.value) fd.append('cover', selectedCover.value)

    if (form.value.id) {
      await api.put(`/portfolio/${form.value.id}`, {
        title: form.value.title,
        workType: form.value.workType,
        description: form.value.description || ''
      })
      if (selectedFile.value || selectedCover.value) {
        await api.delete(`/portfolio/${form.value.id}`)
        await uploadWithProgress(fd)
      }
    } else {
      await uploadWithProgress(fd)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
    console.error('Portfolio upload error:', e)
  } finally {
    saving.value = false
    uploadProgress.value = 0
  }
}

// 用 XMLHttpRequest 上传，支持进度显示，无超时限制
const uploadWithProgress = (fd) => new Promise((resolve, reject) => {
  const token = sessionStorage.getItem('token')
  const xhr = new XMLHttpRequest()
  xhr.open('POST', 'http://localhost:8083/api/portfolio')
  if (token) xhr.setRequestHeader('Authorization', `Bearer ${token}`)
  xhr.upload.onprogress = (e) => {
    if (e.lengthComputable) uploadProgress.value = Math.round(e.loaded / e.total * 100)
  }
  xhr.onload = () => {
    if (xhr.status >= 200 && xhr.status < 300) {
      resolve(JSON.parse(xhr.responseText))
    } else {
      try {
        const err = JSON.parse(xhr.responseText)
        reject(new Error(err.message || `上传失败 (${xhr.status})`))
      } catch {
        reject(new Error(`上传失败 (${xhr.status})`))
      }
    }
  }
  xhr.onerror = () => reject(new Error('网络错误，请检查后端服务'))
  xhr.send(fd)
})

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该作品？', '提示', { type: 'warning' })
  await api.delete(`/portfolio/${id}`)
  ElMessage.success('删除成功')
  loadList()
}

const handleDownload = async (item) => {
  try {
    const token = sessionStorage.getItem('token')
    const res = await fetch(`http://localhost:8083/api/portfolio/download/${item.id}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    if (!res.ok) { ElMessage.error('下载失败'); return }
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    // 从 Content-Disposition 取文件名，否则用标题
    const disposition = res.headers.get('Content-Disposition') || ''
    const match = disposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
    a.download = match ? decodeURIComponent(match[1].replace(/['"]/g, '')) : item.title
    a.click()
    URL.revokeObjectURL(url)
  } catch { ElMessage.error('下载失败') }
}

onMounted(loadList)
</script>

<style scoped>
.work-card { overflow: hidden; }
.work-cover {
  width: 100%; height: 160px; overflow: hidden;
  background: #f5f7fa; border-radius: 4px; margin-bottom: 12px;
  display: flex; align-items: center; justify-content: center;
}
.work-cover img { width: 100%; height: 100%; object-fit: cover; }
.cover-placeholder { display: flex; align-items: center; justify-content: center; width: 100%; height: 100%; }
.work-body { padding: 0 2px; }
.work-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; color: #303133; }
.work-desc { color: #606266; font-size: 13px; line-height: 1.5; min-height: 36px; }
.cover-upload-preview { width: 100%; height: 100%; overflow: hidden; }
</style>
