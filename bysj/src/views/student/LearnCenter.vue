<template>
  <div class="learn-page">
    <!-- 顶部操作栏 -->
    <div class="toolbar">
      <el-tabs v-model="activeCategory" @tab-change="onCategoryChange" class="cat-tabs">
        <el-tab-pane label="全部"     name="" />
        <el-tab-pane label="课程资料" name="课程资料" />
        <el-tab-pane label="竞赛资源" name="竞赛资源" />
        <el-tab-pane label="就业指导" name="就业指导" />
        <el-tab-pane label="其他"     name="其他" />
      </el-tabs>
      <div class="toolbar-right">
        <el-input
          v-model="keyword"
          placeholder="搜索资源标题"
          clearable
          style="width:220px"
          @input="onSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="publishVisible = true">发布资源</el-button>
      </div>
    </div>

    <!-- 资源卡片列表 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col
        v-for="item in resources"
        :key="item.id"
        :span="6"
        style="margin-bottom:16px"
      >
        <el-card shadow="hover" class="res-card">
          <div class="res-header">
            <el-tag size="small" :type="categoryType(item.category)">{{ item.category }}</el-tag>
            <el-button
              v-if="canDelete(item)"
              size="small"
              text
              type="danger"
              :icon="Delete"
              @click="handleDelete(item.id)"
            />
          </div>
          <div class="res-title" :title="item.title">{{ item.title }}</div>
          <div class="res-desc" v-if="item.description">{{ item.description }}</div>
          <div class="res-meta">
            <span>{{ item.uploaderName }}</span>
            <span>{{ formatDate(item.createTime) }}</span>
          </div>
          <div class="res-footer">
            <div class="res-stats">
              <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
              <span
                class="like-btn"
                :class="{ liked: likedSet.has(item.id) }"
                @click="handleLike(item)"
              >
                <el-icon><Star /></el-icon> {{ item.likeCount }}
              </span>
            </div>
            <div class="res-actions">
              <el-button
                v-if="item.fileUrl"
                size="small"
                type="primary"
                plain
                @click="openFile(item)"
              >下载</el-button>
              <el-button
                v-if="item.linkUrl"
                size="small"
                type="success"
                plain
                @click="openLink(item)"
              >访问链接</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && resources.length === 0" description="暂无资源" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="loadList"
      />
    </div>

    <!-- 发布资源弹窗 -->
    <el-dialog v-model="publishVisible" title="发布学习资源" width="500px" @close="resetForm">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="资源标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width:100%">
            <el-option label="课程资料" value="课程资料" />
            <el-option label="竞赛资源" value="竞赛资源" />
            <el-option label="就业指导" value="就业指导" />
            <el-option label="其他"     value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="简单描述一下这个资源" />
        </el-form-item>
        <el-form-item label="外链">
          <el-input v-model="form.linkUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :on-change="onFileChange"
            :on-remove="() => form.file = null"
          >
            <el-button size="small">选择文件</el-button>
            <template #tip>
              <div style="color:#909399;font-size:12px">支持文档、压缩包等常见格式</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishing" @click="submitPublish">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Delete, View, Star } from '@element-plus/icons-vue'
import learnApi from '../../services/learn.js'
import { useUserStore } from '../../stores/user.js'

const userStore = useUserStore()
const myId = () => userStore.userInfo?.userId
const myRole = () => userStore.roleKey

const resources    = ref([])
const loading      = ref(false)
const total        = ref(0)
const currentPage  = ref(1)
const pageSize     = 12
const activeCategory = ref('')
const keyword      = ref('')
const likedSet     = ref(new Set())

const publishVisible = ref(false)
const publishing     = ref(false)
const uploadRef      = ref(null)
const form = ref({
  title: '', category: '其他', description: '', linkUrl: '', file: null
})

let searchTimer = null

onMounted(loadList)

async function loadList() {
  loading.value = true
  try {
    const res = await learnApi.getList({
      page: currentPage.value,
      size: pageSize,
      category: activeCategory.value || undefined,
      keyword: keyword.value || undefined
    })
    resources.value = res.records || []
    total.value = res.total || 0
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const onCategoryChange = () => { currentPage.value = 1; loadList() }
const onSearch = () => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(() => { currentPage.value = 1; loadList() }, 400)
}

const onFileChange = (file) => { form.value.file = file.raw }

const resetForm = () => {
  form.value = { title: '', category: '其他', description: '', linkUrl: '', file: null }
  uploadRef.value?.clearFiles()
}

const submitPublish = async () => {
  if (!form.value.title.trim()) return ElMessage.warning('请填写资源标题')
  if (!form.value.file && !form.value.linkUrl) return ElMessage.warning('请上传文件或填写外链')
  publishing.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.value.title)
    fd.append('category', form.value.category)
    if (form.value.description) fd.append('description', form.value.description)
    if (form.value.linkUrl)     fd.append('linkUrl', form.value.linkUrl)
    if (form.value.file)        fd.append('file', form.value.file)
    await learnApi.publish(fd)
    ElMessage.success('发布成功')
    publishVisible.value = false
    resetForm()
    currentPage.value = 1
    loadList()
  } catch {
    ElMessage.error('发布失败')
  } finally {
    publishing.value = false
  }
}

const handleLike = async (item) => {
  if (likedSet.value.has(item.id)) return
  await learnApi.like(item.id)
  item.likeCount++
  likedSet.value = new Set([...likedSet.value, item.id])
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该资源？', '提示', { type: 'warning' })
  await learnApi.remove(id)
  ElMessage.success('删除成功')
  loadList()
}

const openFile = (item) => {
  window.open('http://localhost:8083' + item.fileUrl, '_blank')
}
const openLink = (item) => {
  const url = item.linkUrl.startsWith('http') ? item.linkUrl : 'https://' + item.linkUrl
  window.open(url, '_blank')
}

const canDelete = (item) => {
  return item.uploaderId === myId() || myRole() === 'admin'
}

const categoryType = (cat) => {
  const map = { '课程资料': '', '竞赛资源': 'success', '就业指导': 'warning', '其他': 'info' }
  return map[cat] || 'info'
}

const formatDate = (t) => {
  if (!t) return ''
  return new Date(t).toLocaleDateString('zh-CN')
}
</script>

<style scoped>
.learn-page { padding: 4px; }
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.cat-tabs { margin-bottom: 0; }
.cat-tabs :deep(.el-tabs__header) { margin-bottom: 0; }

.res-card { height: 100%; display: flex; flex-direction: column; }
.res-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.res-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.res-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.5;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.res-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
}
.res-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top: 1px solid #f0f0f0;
  padding-top: 8px;
  margin-top: auto;
}
.res-stats {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: #909399;
}
.res-stats span { display: flex; align-items: center; gap: 3px; }
.like-btn { cursor: pointer; transition: color .2s; }
.like-btn:hover, .like-btn.liked { color: #E6A23C; }
.res-actions { display: flex; gap: 6px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
