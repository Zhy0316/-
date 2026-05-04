<template>
  <div class="msg-page">
    <el-row :gutter="0" style="height:100%">
      <!-- 左侧会话列表 -->
      <el-col :span="7" class="conv-panel">
        <div class="conv-header">消息</div>
        <div v-if="conversations.length === 0" class="empty-conv">
          <el-empty description="暂无消息" :image-size="60" />
        </div>
        <div
          v-for="conv in conversations"
          :key="conv.userId"
          class="conv-item"
          :class="{ active: activeUserId === conv.userId }"
          @click="openChat(conv)"
        >
          <el-avatar :size="40" :src="conv.avatar ? '/uploads/' + conv.avatar : ''">
            {{ conv.realName?.charAt(0) }}
          </el-avatar>
          <div class="conv-info">
            <div class="conv-name">
              {{ conv.realName }}
              <el-badge v-if="conv.unread > 0" :value="conv.unread" class="unread-badge" />
            </div>
            <div class="conv-last">{{ conv.lastMessage }}</div>
          </div>
          <div class="conv-time">{{ formatTime(conv.lastTime) }}</div>
        </div>
      </el-col>

      <!-- 右侧聊天窗口 -->
      <el-col :span="17" class="chat-panel">
        <template v-if="activeUserId">
          <div class="chat-header">{{ activeName }}</div>
          <div class="chat-body" ref="chatBodyRef">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="msg-row"
              :class="msg.fromUserId === myId ? 'mine' : 'theirs'"
            >
              <el-avatar :size="32" class="msg-avatar">
                {{ msg.fromUserId === myId ? myName?.charAt(0) : activeName?.charAt(0) }}
              </el-avatar>
              <div class="bubble">{{ msg.content }}</div>
              <span class="msg-time">{{ formatTime(msg.createTime) }}</span>
            </div>
          </div>
          <div class="chat-input">
            <el-input
              v-model="inputText"
              placeholder="输入消息，Enter 发送"
              @keyup.enter="sendMsg"
              :disabled="sending"
            />
            <el-button type="primary" @click="sendMsg" :loading="sending">发送</el-button>
          </div>
        </template>
        <template v-else>
          <el-empty description="选择一个会话开始聊天" style="margin-top:80px" />
        </template>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import messageApi from '../../services/message.js'
import { useUserStore } from '../../stores/user.js'

const userStore = useUserStore()
const myId = computed(() => userStore.userInfo?.userId)
const myName = computed(() => userStore.userInfo?.realName)

const conversations = ref([])
const activeUserId = ref(null)
const activeName = ref('')
const messages = ref([])
const inputText = ref('')
const sending = ref(false)
const chatBodyRef = ref(null)
let pollTimer = null

onMounted(() => { loadConversations() })
onUnmounted(() => { if (pollTimer) clearInterval(pollTimer) })

const loadConversations = async () => {
  try { conversations.value = await messageApi.getConversations() } catch {}
}

const openChat = async (conv) => {
  activeUserId.value = conv.userId
  activeName.value = conv.realName
  conv.unread = 0
  await loadHistory()
  startPolling()
}

const loadHistory = async () => {
  if (!activeUserId.value) return
  try {
    messages.value = await messageApi.getHistory(activeUserId.value)
    scrollToBottom()
  } catch {}
}

const sendMsg = async () => {
  const text = inputText.value.trim()
  if (!text || !activeUserId.value) return
  sending.value = true
  try {
    const msg = await messageApi.send(activeUserId.value, text)
    messages.value.push(msg)
    inputText.value = ''
    scrollToBottom()
    loadConversations()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight
  })
}

const startPolling = () => {
  if (pollTimer) clearInterval(pollTimer)
  pollTimer = setInterval(async () => {
    await loadHistory()
    await loadConversations()
  }, 5000)
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  if (d.toDateString() === now.toDateString()) return d.toTimeString().slice(0, 5)
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped>
.msg-page { height: calc(100vh - 64px); display: flex; }
.conv-panel {
  border-right: 1px solid #e4e7ed;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
}
.conv-header {
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.empty-conv { padding: 40px 0; }
.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background .15s;
  flex-shrink: 0;
}
.conv-item:hover, .conv-item.active { background: #f0f7ff; }
.conv-info { flex: 1; min-width: 0; }
.conv-name {
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}
.conv-last {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 120px;
}
.conv-time { font-size: 11px; color: #c0c4cc; white-space: nowrap; }
.chat-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
}
.chat-header {
  padding: 14px 20px;
  font-size: 15px;
  font-weight: 600;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}
.msg-row.mine { flex-direction: row-reverse; }
.bubble {
  max-width: 60%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,.08);
}
.mine .bubble { background: #409EFF; color: #fff; }
.msg-time { font-size: 11px; color: #c0c4cc; }
.chat-input {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}
.chat-input .el-input { flex: 1; }
</style>
