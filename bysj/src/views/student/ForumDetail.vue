<template>
  <div class="forum-detail-container">
    <el-button @click="goBack" style="margin-bottom: 20px">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <el-card v-loading="loading">
      <div class="post-detail" v-if="post">
        <div class="post-header">
          <span class="post-category">{{ post.category || '学习分享' }}</span>
          <span v-if="post.is_top" class="post-top">置顶</span>
          <span v-if="post.is_essence" class="post-essence">精华</span>
        </div>

        <h1 class="post-title">{{ post.title }}</h1>

        <div class="author-section">
          <el-avatar :size="48" :src="getAvatarUrl(post.authorAvatar)">
            {{ post.authorName?.charAt(0) || '用户' }}
          </el-avatar>
          <div class="author-info">
            <span class="author-name">{{ post.authorName || '匿名用户' }}</span>
            <span class="post-time">发布于 {{ formatTime(post.createTime) }}</span>
          </div>
        </div>

        <div class="cover-image" v-if="post.cover_image">
          <img :src="getImageUrl(post.cover_image)" alt="封面" />
        </div>

        <div class="post-content" v-html="post.content"></div>

        <div class="resources-section" v-if="resources && resources.length > 0">
          <h3>资源</h3>
          <div class="resource-list">
            <div v-for="resource in resources" :key="resource.id" class="resource-item">
              <div v-if="resource.resourceType === 'video'" class="video-player">
                <video :src="getImageUrl(resource.fileUrl)" controls class="video-content">
                  您的浏览器不支持视频播放
                </video>
                <div class="video-info">
                  <span class="resource-name">{{ resource.fileName }}</span>
                  <span class="file-size">{{ formatFileSize(resource.fileSize) }}</span>
                </div>
              </div>
              <div v-else-if="resource.resourceType === 'image'" class="image-display">
                <img :src="getImageUrl(resource.fileUrl)" :alt="resource.fileName" class="image-content" />
                <span class="resource-name">{{ resource.fileName }}</span>
              </div>
              <div v-else class="document-item">
                <el-icon><FileText /></el-icon>
                <a :href="getImageUrl(resource.fileUrl)" target="_blank" class="document-link">{{ resource.fileName }}</a>
                <span class="file-size">{{ formatFileSize(resource.fileSize) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="action-bar">
          <el-button :type="post.hasLiked ? 'primary' : 'default'" @click="handleLike">
            <el-icon><Star /></el-icon> {{ post.hasLiked ? '已点赞' : '点赞' }} ({{ post.like_count || 0 }})
          </el-button>
          <el-button :type="post.hasCollected ? 'warning' : 'default'" @click="handleCollect">
            <el-icon><Collection /></el-icon> {{ post.hasCollected ? '已收藏' : '收藏' }} ({{ post.collect_count || 0 }})
          </el-button>
          <el-button @click="showShareDialog = true">
            <el-icon><Share /></el-icon> 分享
          </el-button>
        </div>

        <el-divider />

        <div class="comments-section">
          <h3>评论 ({{ post.comment_count || 0 }})</h3>

          <div class="comment-input">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="写下你的评论..."
              maxlength="500"
              show-word-limit
            />
            <el-button type="primary" @click="submitComment" :loading="commenting" style="margin-top: 10px">
              发表评论
            </el-button>
          </div>

          <div class="comment-list" v-loading="commentLoading">
            <el-empty v-if="!commentLoading && comments.length === 0" description="暂无评论" />

            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-avatar">
                <el-avatar :size="40" :src="getAvatarUrl(comment.authorAvatar)">
                  {{ comment.authorName?.charAt(0) || '用户' }}
                </el-avatar>
              </div>
              <div class="comment-body">
                <div class="comment-header">
                  <span class="comment-author">{{ comment.authorName || '匿名用户' }}</span>
                  <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
                <div class="comment-actions">
                  <el-button size="small" text @click="handleCommentLike(comment)">
                    <el-icon><Star /></el-icon> {{ comment.like_count || 0 }}
                  </el-button>
                  <el-button size="small" text @click="showReplyInput(comment)">
                    <el-icon><ChatDotRound /></el-icon> 回复
                  </el-button>
                </div>

                <div v-if="replyInputs[comment.id]" class="reply-input">
                  <el-input
                    v-model="replyContent[comment.id]"
                    type="textarea"
                    :rows="2"
                    :placeholder="`回复 ${comment.authorName}...`"
                  />
                  <el-button size="small" type="primary" @click="submitReply(comment.id)" style="margin-top: 5px">
                    发送
                  </el-button>
                  <el-button size="small" @click="replyInputs[comment.id] = false">取消</el-button>
                </div>

                <div v-if="comment.replies && comment.replies.length > 0" class="reply-list">
                  <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                    <span class="reply-author">{{ reply.authorName }}:</span>
                    <span class="reply-content">{{ reply.content }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Star, Collection, Share, Document, ChatDotRound } from '@element-plus/icons-vue'
import { forumAPI } from '@/services/forum'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const post = ref(null)
const resources = ref([])
const comments = ref([])
const commentLoading = ref(false)
const commentContent = ref('')
const commenting = ref(false)
const replyInputs = reactive({})
const replyContent = reactive({})

const postId = parseInt(route.params.id)

onMounted(() => {
  loadPostDetail()
  loadComments()
})

const loadPostDetail = async () => {
  loading.value = true
  try {
    const res = await forumAPI.getPostDetail(postId)
    post.value = res
    resources.value = res.resources || []
  } catch (error) {
    ElMessage.error('加载帖子详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  commentLoading.value = true
  try {
    const res = await forumAPI.getComments(postId)
    comments.value = res || []
    buildCommentTree()
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    commentLoading.value = false
  }
}

const buildCommentTree = () => {
  const topLevel = []
  const map = {}
  comments.value.forEach(c => {
    c.replies = []
    map[c.id] = c
  })
  comments.value.forEach(c => {
    if (c.parentId) {
      if (map[c.parentId]) {
        map[c.parentId].replies.push(c)
      }
    } else {
      topLevel.push(c)
    }
  })
  comments.value = topLevel
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (post.value.hasLiked) {
      await forumAPI.unlikePost(postId)
      post.value.hasLiked = false
      post.value.like_count = Math.max(0, (post.value.like_count || 1) - 1)
    } else {
      await forumAPI.likePost(postId)
      post.value.hasLiked = true
      post.value.like_count = (post.value.like_count || 0) + 1
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (post.value.hasCollected) {
      await forumAPI.uncollectPost(postId)
      post.value.hasCollected = false
      post.value.collect_count = Math.max(0, (post.value.collect_count || 1) - 1)
    } else {
      await forumAPI.collectPost(postId)
      post.value.hasCollected = true
      post.value.collect_count = (post.value.collect_count || 0) + 1
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const submitComment = async () => {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  commenting.value = true
  try {
    await forumAPI.addComment(postId, commentContent.value)
    ElMessage.success('评论成功')
    commentContent.value = ''
    loadComments()
    post.value.comment_count = (post.value.comment_count || 0) + 1
  } catch (error) {
    ElMessage.error('评论失败')
  } finally {
    commenting.value = false
  }
}

const showReplyInput = (comment) => {
  replyInputs[comment.id] = true
  replyContent[comment.id] = ''
}

const submitReply = async (parentId) => {
  const content = replyContent[parentId]
  if (!content || !content.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    await forumAPI.addComment(postId, content, parentId)
    ElMessage.success('回复成功')
    replyInputs[parentId] = false
    loadComments()
    post.value.comment_count = (post.value.comment_count || 0) + 1
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const handleCommentLike = async (comment) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    await forumAPI.likeComment(comment.id)
    comment.like_count = (comment.like_count || 0) + 1
    ElMessage.success('点赞成功')
  } catch (error) {
    ElMessage.error('点赞失败')
  }
}

const goBack = () => {
  router.back()
}

const getImageUrl = (url) => {
  if (!url) return ''
  return url.startsWith('http') ? url : `http://localhost:8083${url}`
}

const getAvatarUrl = (avatar) => {
  if (!avatar) return ''
  return avatar.startsWith('http') ? avatar : `http://localhost:8083${avatar}`
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

const formatFileSize = (size) => {
  if (!size) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + ' KB'
  return (size / (1024 * 1024)).toFixed(2) + ' MB'
}
</script>

<style scoped>
.forum-detail-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.post-header {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.post-category {
  background: #409eff;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.post-top {
  background: #f56c6c;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.post-essence {
  background: #e6a23c;
  color: white;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 14px;
}

.post-title {
  font-size: 28px;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.author-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.author-info {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.post-time {
  font-size: 13px;
  color: #999;
}

.cover-image {
  margin-bottom: 20px;
  border-radius: 8px;
  overflow: hidden;
}

.cover-image img {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
}

.post-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 30px;
}

.post-content img {
  max-width: 100%;
  margin: 10px 0;
  border-radius: 8px;
}

.resources-section {
  margin-bottom: 30px;
}

.resources-section h3 {
  margin-bottom: 12px;
}

.resource-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.video-player {
  background: #000;
  border-radius: 8px;
  overflow: hidden;
}

.video-content {
  width: 100%;
  max-height: 500px;
  display: block;
}

.video-info {
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-display {
  margin-bottom: 10px;
}

.image-content {
  max-width: 100%;
  border-radius: 8px;
}

.document-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.document-link {
  color: #409eff;
  text-decoration: none;
  flex: 1;
}

.resource-name {
  color: #333;
}

.file-size {
  color: #999;
  font-size: 13px;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.comments-section {
  margin-top: 30px;
}

.comments-section h3 {
  margin-bottom: 20px;
}

.comment-input {
  margin-bottom: 30px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.comment-author {
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 13px;
  color: #999;
}

.comment-content {
  margin: 0 0 8px 0;
  color: #333;
  line-height: 1.5;
}

.comment-actions {
  display: flex;
  gap: 12px;
}

.reply-input {
  margin-top: 10px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 6px;
}

.reply-list {
  margin-top: 10px;
  padding-left: 20px;
  border-left: 2px solid #eee;
}

.reply-item {
  padding: 6px 0;
  font-size: 14px;
}

.reply-author {
  font-weight: bold;
  color: #409eff;
  margin-right: 6px;
}

.reply-content {
  color: #333;
}
</style>
