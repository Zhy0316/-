<template>
  <div class="forum-container">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <h2>📚 学习天地</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索帖子..."
              style="width: 200px; margin-right: 10px"
              @keyup.enter="handleSearch"
              clearable
            >
              <template #prefix><el-icon><Search /></el-icon></template>
            </el-input>
            <el-button type="primary" @click="showPublishDialog = true">
              <el-icon><Plus /></el-icon> 发布帖子
            </el-button>
          </div>
        </div>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="推荐" name="recommend" />
        <el-tab-pane label="我的发布" name="my-posts" />
        <el-tab-pane label="我的收藏" name="my-collections" />
        <el-tab-pane label="我的点赞" name="my-likes" />
      </el-tabs>

      <div class="post-list" v-loading="loading">
        <el-empty v-if="!loading && postList.length === 0" description="暂无帖子" />

        <div v-for="post in postList" :key="post.id" class="post-item" @click="goToDetail(post)">
          <div class="post-cover" v-if="post.cover_image">
            <img :src="getImageUrl(post.cover_image)" alt="封面" />
          </div>
          <div class="post-content">
            <div class="post-header">
              <span class="post-category">{{ post.category || '学习分享' }}</span>
              <span v-if="post.is_top" class="post-top">置顶</span>
              <span v-if="post.is_essence" class="post-essence">精华</span>
            </div>
            <h3 class="post-title">{{ post.title }}</h3>
            <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
            <div class="post-meta">
              <div class="author-info">
                <el-avatar :size="24" :src="getAvatarUrl(post.authorAvatar)">
                  {{ post.authorName?.charAt(0) || '用户' }}
                </el-avatar>
                <span class="author-name">{{ post.authorName || '匿名用户' }}</span>
              </div>
              <div class="post-stats">
                <span><el-icon><View /></el-icon> {{ post.view_count || 0 }}</span>
                <span><el-icon><Star /></el-icon> {{ post.like_count || 0 }}</span>
                <span><el-icon><ChatDotRound /></el-icon> {{ post.comment_count || 0 }}</span>
                <span><el-icon><Collection /></el-icon> {{ post.collect_count || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="pagination" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadPosts"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="showPublishDialog"
      title="发布帖子"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form :model="publishForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="publishForm.title" placeholder="请输入帖子标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="分类">
          <el-select v-model="publishForm.category" placeholder="选择分类" style="width: 100%">
            <el-option label="学习分享" value="学习分享" />
            <el-option label="资源推荐" value="资源推荐" />
            <el-option label="经验交流" value="经验交流" />
            <el-option label="提问求助" value="提问求助" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="封面图片">
          <div class="cover-upload">
            <input type="file" ref="coverInput" @change="handleCoverChange" accept="image/*" style="display:none" />
            <div v-if="!publishForm.coverImage" class="cover-placeholder" @click="coverInput.click()">
              <el-icon><Plus /></el-icon> 添加封面
            </div>
            <div v-else class="cover-preview">
              <img :src="getImageUrl(publishForm.coverImage)" alt="封面" />
              <el-button size="small" type="danger" @click="publishForm.coverImage = ''">删除</el-button>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="正文内容" required>
          <div class="editor-wrapper">
            <div
              ref="contentEditor"
              class="content-editor"
              contenteditable="true"
              placeholder="请输入帖子内容，支持粘贴图片..."
              @paste="handlePaste"
              @input="handleContentInput"
            ></div>
            <div class="editor-actions">
              <el-button size="small" @click="insertImage">
                <el-icon><Image /></el-icon> 插入图片
              </el-button>
              <span class="word-count">{{ contentLength }}/5000</span>
            </div>
            <input type="file" ref="imageInput" @change="handleImageUpload" accept="image/*" style="display:none" />
          </div>
        </el-form-item>

        <el-form-item label="添加媒体资源">
          <div class="resource-upload">
            <el-button size="small" @click="resourceInput.click()">
              <el-icon><Upload /></el-icon> 选择文件
            </el-button>
            <span class="upload-tip">支持视频、PPT、Word、PDF等</span>
            <input type="file" ref="resourceInput" @change="handleResourceChange" multiple style="display:none" />
            
            <div v-if="resourceFiles.length > 0" class="file-list">
              <div v-for="(file, index) in resourceFiles" :key="index" class="file-item">
                <div v-if="isVideoFile(file.name)" class="video-preview">
                  <video :src="URL.createObjectURL(file)" controls width="120" height="80" />
                </div>
                <div v-else-if="isImageFile(file.name)" class="image-preview">
                  <img :src="URL.createObjectURL(file)" alt="预览" width="120" height="80" />
                </div>
                <div v-else class="file-icon">
                  <el-icon><FileText /></el-icon>
                </div>
                <div class="file-info">
                  <span class="file-name">{{ file.name }}</span>
                  <span class="file-size">{{ formatFileSize(file.size) }}</span>
                </div>
                <el-button size="small" type="danger" @click="removeResource(index)">删除</el-button>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="标签">
          <el-input v-model="publishForm.tags" placeholder="多个标签用逗号分隔" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, View, Star, ChatDotRound, Collection, Upload, Image, FileText } from '@element-plus/icons-vue'
import { forumAPI } from '@/services/forum'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('recommend')
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const postList = ref([])

const showPublishDialog = ref(false)
const publishing = ref(false)
const coverInput = ref(null)
const resourceInput = ref(null)
const contentEditor = ref(null)
const imageInput = ref(null)
const resourceFiles = ref([])

const publishForm = reactive({
  title: '',
  content: '',
  coverImage: '',
  contentType: 'article',
  category: '学习分享',
  tags: '',
  resources: []
})

const contentLength = computed(() => {
  if (!contentEditor.value) return 0
  return contentEditor.value.innerText.length
})

onMounted(() => {
  loadPosts()
})

const loadPosts = async () => {
  loading.value = true
  try {
    let res
    switch (activeTab.value) {
      case 'my-posts':
        res = await forumAPI.getUserPosts()
        break
      case 'my-collections':
        res = await forumAPI.getUserCollections()
        break
      case 'my-likes':
        res = await forumAPI.getUserLikedPosts()
        break
      default:
        res = await forumAPI.getPostList()
    }
    postList.value = res || []
    total.value = postList.value.length
  } catch (error) {
    console.error('加载帖子失败:', error)
  } finally {
    loading.value = false
  }
}

const handleTabChange = () => {
  currentPage.value = 1
  loadPosts()
}

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    loadPosts()
    return
  }
  loading.value = true
  try {
    const res = await forumAPI.searchPosts(searchKeyword.value)
    postList.value = res || []
    total.value = postList.value.length
  } catch (error) {
    ElMessage.error('搜索失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (post) => {
  const currentPath = router.currentRoute.value.path
  const basePath = currentPath.startsWith('/tutor') ? '/tutor' : '/student'
  router.push(`${basePath}/forum/detail/${post.id}`)
}

const getExcerpt = (content) => {
  if (!content) return ''
  const text = content.replace(/<[^>]*>/g, '')
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `http://localhost:8083${url}`
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  return avatar.startsWith('http') ? avatar : `http://localhost:8083${avatar}`
}

const handleCoverChange = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  try {
    const res = await forumAPI.uploadFile(file)
    publishForm.coverImage = res.filePath
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
}

const handlePaste = async (e) => {
  const items = e.clipboardData.items
  if (!items) return

  for (let i = 0; i < items.length; i++) {
    if (items[i].type.indexOf('image') !== -1) {
      e.preventDefault()
      const file = items[i].getAsFile()
      if (file) {
        try {
          const res = await forumAPI.uploadFile(file)
          insertImageToEditor(getImageUrl(res.filePath))
          ElMessage.success('图片粘贴成功')
        } catch (error) {
          ElMessage.error('图片上传失败')
        }
      }
    }
  }
}

const insertImage = () => {
  imageInput.value?.click()
}

const handleImageUpload = async (e) => {
  const file = e.target.files[0]
  if (!file) return

  try {
    const res = await forumAPI.uploadFile(file)
    insertImageToEditor(getImageUrl(res.filePath))
    ElMessage.success('图片插入成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
  e.target.value = ''
}

const insertImageToEditor = (imageUrl) => {
  if (!contentEditor.value) return
  
  const selection = window.getSelection()
  if (selection.rangeCount === 0) {
    contentEditor.value.focus()
    document.execCommand('insertHTML', false, `<img src="${imageUrl}" style="max-width: 100%; margin: 10px 0;" />`)
  } else {
    const range = selection.getRangeAt(0)
    range.deleteContents()
    const img = document.createElement('img')
    img.src = imageUrl
    img.style.maxWidth = '100%'
    img.style.margin = '10px 0'
    range.insertNode(img)
  }
}

const handleContentInput = () => {
  if (contentEditor.value) {
    publishForm.content = contentEditor.value.innerHTML
  }
}

const isVideoFile = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  return ['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm'].includes(ext)
}

const isImageFile = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  return ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)
}

const handleResourceChange = (e) => {
  const files = Array.from(e.target.files)
  resourceFiles.value.push(...files)
  e.target.value = ''
}

const removeResource = (index) => {
  resourceFiles.value.splice(index, 1)
}

const formatFileSize = (size) => {
  if (!size) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}

const handlePublish = async () => {
  if (!publishForm.title.trim()) {
    ElMessage.warning('请输入标题')
    return
  }
  
  if (contentEditor.value && !contentEditor.value.innerText.trim()) {
    ElMessage.warning('请输入内容')
    return
  }

  publishing.value = true
  try {
    const postResources = []
    for (let i = 0; i < resourceFiles.value.length; i++) {
      const file = resourceFiles.value[i]
      const res = await forumAPI.uploadFile(file)
      postResources.push({
        resourceType: getResourceType(file.name),
        fileName: file.name,
        fileUrl: res.filePath,
        fileSize: file.size,
        fileExt: file.name.split('.').pop(),
        sortOrder: i
      })
    }

    await forumAPI.createPost({
      ...publishForm,
      content: contentEditor.value?.innerHTML || '',
      resources: postResources
    })

    ElMessage.success('发布成功')
    showPublishDialog.value = false
    resetPublishForm()
    loadPosts()
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const resetPublishForm = () => {
  publishForm.title = ''
  publishForm.content = ''
  publishForm.coverImage = ''
  publishForm.contentType = 'article'
  publishForm.category = '学习分享'
  publishForm.tags = ''
  publishForm.resources = []
  resourceFiles.value = []
  if (contentEditor.value) {
    contentEditor.value.innerHTML = ''
  }
}

const getResourceType = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  if (['mp4', 'avi', 'mov', 'wmv', 'flv', 'mkv', 'webm'].includes(ext)) return 'video'
  if (['mp3', 'wav', 'flac', 'aac', 'ogg'].includes(ext)) return 'audio'
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext)) return 'image'
  if (['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'pdf', 'txt'].includes(ext)) return 'document'
  return 'other'
}
</script>

<style scoped>
.forum-container {
  padding: 20px;
}

.main-card {
  min-height: calc(100vh - 140px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.post-list {
  margin-top: 20px;
}

.post-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  transition: background-color 0.3s;
}

.post-item:hover {
  background-color: #f5f7fa;
}

.post-cover {
  width: 160px;
  height: 100px;
  margin-right: 20px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}

.post-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.post-category {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-top {
  background: #f56c6c;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-essence {
  background: #e6a23c;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.post-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.post-excerpt {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  flex: 1;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.author-name {
  font-size: 14px;
  color: #666;
}

.post-stats {
  display: flex;
  gap: 16px;
  color: #999;
  font-size: 14px;
}

.post-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.cover-upload {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.cover-placeholder {
  width: 200px;
  height: 120px;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
}

.cover-preview {
  width: 200px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.cover-preview img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
}

.editor-wrapper {
  display: flex;
  flex-direction: column;
}

.content-editor {
  min-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  outline: none;
  white-space: pre-wrap;
  word-break: break-word;
}

.content-editor:empty:before {
  content: attr(placeholder);
  color: #ccc;
}

.content-editor img {
  max-width: 100%;
  margin: 10px 0;
}

.editor-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.word-count {
  font-size: 12px;
  color: #909399;
}

.resource-upload {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
}

.file-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.video-preview, .image-preview {
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
}

.video-preview video {
  display: block;
}

.image-preview img {
  display: block;
  object-fit: cover;
}

.file-icon {
  width: 120px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  border-radius: 4px;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-size: 14px;
  color: #333;
}

.file-size {
  font-size: 12px;
  color: #909399;
}
</style>
