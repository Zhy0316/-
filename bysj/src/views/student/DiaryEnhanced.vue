<template>
  <div class="diary-enhanced">
    <el-row :gutter="20">
      <!-- 左侧:日记列表和筛选 -->
      <el-col :span="8">
        <el-card class="filter-card">
          <template #header>
            <div class="card-header">
              <span>📝 成长日记</span>
              <el-button type="primary" size="small" @click="showEditor = true">
                <el-icon><Plus /></el-icon> 新建
              </el-button>
            </div>
          </template>

          <!-- 快捷筛选 -->
          <div class="quick-filters">
            <el-button 
              :type="filterType === 'all' ? 'primary' : ''" 
              size="small" 
              @click="filterType = 'all'">
              全部 ({{ diaries.length }})
            </el-button>
            <el-button 
              :type="filterType === 'milestone' ? 'primary' : ''" 
              size="small" 
              @click="filterType = 'milestone'">
              🎯 里程碑 ({{ milestoneDiaries.length }})
            </el-button>
            <el-button 
              :type="filterType === 'tutor' ? 'primary' : ''" 
              size="small" 
              @click="filterType = 'tutor'">
              👁️ 导师可见 ({{ tutorVisibleCount }})
            </el-button>
          </div>

          <!-- 日记列表 -->
          <div class="diary-list">
            <div 
              v-for="diary in filteredDiaries" 
              :key="diary.id"
              class="diary-item"
              :class="{ active: currentDiary?.id === diary.id }"
              @click="selectDiary(diary)">
              <div class="diary-item-header">
                <span class="diary-title">
                  <span v-if="diary.isMilestone === 1">🎯</span>
                  {{ diary.title }}
                </span>
                <span class="diary-date">{{ formatDate(diary.recordDate) }}</span>
              </div>
              <div class="diary-item-meta">
                <el-tag v-if="diary.mood" size="small" type="info">{{ diary.mood }}</el-tag>
                <el-tag v-if="diary.tutorVisible === 1" size="small" type="success">导师可见</el-tag>
              </div>
            </div>
            <el-empty v-if="filteredDiaries.length === 0" description="暂无日记" />
          </div>
        </el-card>

        <!-- 情绪统计卡片 -->
        <el-card class="stats-card" style="margin-top: 20px;">
          <template #header>
            <span>😊 情绪统计</span>
          </template>
          <div ref="moodChartRef" style="height: 200px;"></div>
        </el-card>

        <!-- 关键词云 -->
        <el-card class="stats-card" style="margin-top: 20px;">
          <template #header>
            <span>💬 关键词云</span>
          </template>
          <div class="keyword-cloud">
            <el-tag 
              v-for="(count, word) in keywords" 
              :key="word"
              :size="getTagSize(count)"
              style="margin: 5px; cursor: pointer;">
              {{ word }} ({{ count }})
            </el-tag>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧:日记详情/编辑器 -->
      <el-col :span="16">
        <!-- 编辑器 -->
        <el-card v-if="showEditor" class="editor-card">
          <template #header>
            <div class="card-header">
              <span>{{ editingDiary ? '编辑日记' : '新建日记' }}</span>
              <el-button size="small" @click="cancelEdit">取消</el-button>
            </div>
          </template>

          <el-form :model="diaryForm" label-width="100px">
            <el-form-item label="标题">
              <el-input v-model="diaryForm.title" placeholder="请输入日记标题" />
            </el-form-item>

            <el-form-item label="日期">
              <el-date-picker 
                v-model="diaryForm.recordDate" 
                type="date" 
                placeholder="选择日期"
                style="width: 100%;" />
            </el-form-item>

            <el-form-item label="情绪标签">
              <el-select v-model="diaryForm.mood" placeholder="选择情绪" clearable style="width: 100%;">
                <el-option 
                  v-for="tag in emotionTags" 
                  :key="tag.id"
                  :label="tag.tagName"
                  :value="tag.tagName">
                  <span>{{ tag.tagIcon }} {{ tag.tagName }}</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="主题标签">
              <el-select 
                v-model="diaryForm.selectedTags" 
                multiple 
                placeholder="选择主题标签"
                style="width: 100%;">
                <el-option 
                  v-for="tag in themeTags" 
                  :key="tag.id"
                  :label="tag.tagName"
                  :value="tag.id">
                  <span>{{ tag.tagIcon }} {{ tag.tagName }}</span>
                </el-option>
              </el-select>
            </el-form-item>

            <el-form-item label="里程碑">
              <el-checkbox v-model="diaryForm.isMilestone" :true-value="1" :false-value="0">
                标记为里程碑事件
              </el-checkbox>
            </el-form-item>

            <el-form-item label="导师可见">
              <el-checkbox v-model="diaryForm.tutorVisible" :true-value="1" :false-value="0">
                允许导师查看此日记
              </el-checkbox>
            </el-form-item>

            <el-form-item label="内容">
              <el-input 
                v-model="diaryForm.content" 
                type="textarea" 
                :rows="10"
                placeholder="记录你的成长故事..." />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="saveDiary">保存</el-button>
              <el-button @click="cancelEdit">取消</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 日记详情 -->
        <el-card v-else-if="currentDiary" class="detail-card">
          <template #header>
            <div class="detail-header">
              <div>
                <h2 style="margin: 0;">
                  <span v-if="currentDiary.isMilestone === 1">🎯</span>
                  {{ currentDiary.title }}
                </h2>
                <div class="detail-meta">
                  <span>{{ formatDate(currentDiary.recordDate) }}</span>
                  <el-tag v-if="currentDiary.mood" size="small" type="info" style="margin-left: 10px;">
                    {{ currentDiary.mood }}
                  </el-tag>
                  <el-tag v-if="currentDiary.tutorVisible === 1" size="small" type="success" style="margin-left: 5px;">
                    导师可见
                  </el-tag>
                </div>
              </div>
              <div>
                <el-button size="small" type="primary" @click="editDiary(currentDiary)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteDiary(currentDiary.id)">删除</el-button>
              </div>
            </div>
          </template>

          <!-- 标签展示 -->
          <div v-if="currentDiaryTags.length > 0" class="diary-tags">
            <el-tag 
              v-for="tag in currentDiaryTags" 
              :key="tag.id"
              size="small"
              :color="tag.tagColor"
              style="margin-right: 8px;">
              {{ tag.tagIcon }} {{ tag.tagName }}
            </el-tag>
          </div>

          <!-- 内容 -->
          <div class="diary-content" v-html="currentDiary.content"></div>

          <!-- 导师批注 -->
          <div v-if="currentDiary.tutorComment" class="tutor-comment">
            <el-divider content-position="left">导师批注</el-divider>
            <div class="comment-box">
              <div class="comment-time">
                {{ formatDateTime(currentDiary.tutorCommentTime) }}
              </div>
              <div class="comment-content">{{ currentDiary.tutorComment }}</div>
            </div>
          </div>
        </el-card>

        <!-- 空状态 -->
        <el-card v-else>
          <el-empty description="选择一篇日记查看详情" />
        </el-card>

        <!-- 时光轴视图 -->
        <el-card style="margin-top: 20px;">
          <template #header>
            <div class="card-header">
              <span>⏰ 成长时光轴</span>
              <el-radio-group v-model="timelineGroupBy" size="small" @change="loadTimeline">
                <el-radio-button value="month">按月</el-radio-button>
                <el-radio-button value="semester">按学期</el-radio-button>
                <el-radio-button value="year">按年</el-radio-button>
              </el-radio-group>
            </div>
          </template>

          <el-timeline>
            <el-timeline-item 
              v-for="(items, period) in timeline" 
              :key="period"
              :timestamp="period"
              placement="top">
              <el-card>
                <h4>{{ period }} ({{ items.length }}篇)</h4>
                <div class="timeline-items">
                  <div 
                    v-for="item in items.slice(0, 3)" 
                    :key="item.id"
                    class="timeline-item-mini"
                    @click="selectDiary(item)">
                    <span v-if="item.isMilestone === 1">🎯</span>
                    {{ item.title }}
                  </div>
                  <div v-if="items.length > 3" class="more-hint">
                    还有 {{ items.length - 3 }} 篇...
                  </div>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import diaryAnalysisApi from '@/services/diaryAnalysis'
import { api } from '@/services/api'

const userStore = useUserStore()

// 数据
const diaries = ref([])
const currentDiary = ref(null)
const showEditor = ref(false)
const editingDiary = ref(null)
const filterType = ref('all')
const timelineGroupBy = ref('month')

// 标签数据
const emotionTags = ref([])
const themeTags = ref([])
const milestoneTags = ref([])
const currentDiaryTags = ref([])

// 统计数据
const moodStatistics = ref({})
const keywords = ref({})
const timeline = ref({})

// 图表
const moodChartRef = ref(null)
let moodChart = null

// 表单
const diaryForm = ref({
  title: '',
  content: '',
  mood: '',
  recordDate: new Date(),
  isMilestone: 0,
  tutorVisible: 0,
  selectedTags: []
})

// 计算属性
const filteredDiaries = computed(() => {
  if (filterType.value === 'milestone') {
    return diaries.value.filter(d => d.isMilestone === 1)
  } else if (filterType.value === 'tutor') {
    return diaries.value.filter(d => d.tutorVisible === 1)
  }
  return diaries.value
})

const milestoneDiaries = computed(() => {
  return diaries.value.filter(d => d.isMilestone === 1)
})

const tutorVisibleCount = computed(() => {
  return diaries.value.filter(d => d.tutorVisible === 1).length
})

// 方法
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const getTagSize = (count) => {
  if (count >= 5) return 'large'
  if (count >= 3) return 'default'
  return 'small'
}

// 加载数据
const loadDiaries = async () => {
  try {
    const res = await api.get('/diary/list', { 
      params: { studentId: userStore.userInfo.userId } 
    })
    diaries.value = Array.isArray(res) ? res : (res?.data || [])
  } catch (error) {
    ElMessage.error('加载日记失败')
  }
}

const loadTags = async () => {
  try {
    const res = await diaryAnalysisApi.getAllTagsGrouped()
    const tags = res.data || res
    emotionTags.value = tags.emotion || []
    themeTags.value = tags.theme || []
    milestoneTags.value = tags.milestone || []
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

const loadMoodStatistics = async () => {
  try {
    const res = await diaryAnalysisApi.getMoodStatistics(userStore.userInfo.userId)
    moodStatistics.value = res.data || res
    renderMoodChart()
  } catch (error) {
    console.error('加载情绪统计失败', error)
  }
}

const loadKeywords = async () => {
  try {
    const res = await diaryAnalysisApi.getKeywords(userStore.userInfo.userId)
    keywords.value = res.data || res
  } catch (error) {
    console.error('加载关键词失败', error)
  }
}

const loadTimeline = async () => {
  try {
    const res = await diaryAnalysisApi.getTimeline(
      userStore.userInfo.userId, 
      timelineGroupBy.value
    )
    timeline.value = res.data || res
  } catch (error) {
    console.error('加载时光轴失败', error)
  }
}

const loadDiaryTags = async (diaryId) => {
  try {
    const res = await diaryAnalysisApi.getDiaryTags(diaryId)
    currentDiaryTags.value = res.data || res || []
  } catch (error) {
    console.error('加载日记标签失败', error)
    currentDiaryTags.value = []
  }
}

// 渲染情绪图表
const renderMoodChart = () => {
  if (!moodChartRef.value) return
  
  if (!moodChart) {
    moodChart = echarts.init(moodChartRef.value)
  }

  const moodCount = moodStatistics.value.moodCount || {}
  const data = Object.entries(moodCount).map(([name, value]) => ({ name, value }))

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    series: [{
      type: 'pie',
      radius: '70%',
      data: data,
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  }

  moodChart.setOption(option)
}

// 选择日记
const selectDiary = async (diary) => {
  currentDiary.value = diary
  showEditor.value = false
  await loadDiaryTags(diary.id)
}

// 编辑日记
const editDiary = (diary) => {
  editingDiary.value = diary
  diaryForm.value = {
    title: diary.title,
    content: diary.content,
    mood: diary.mood || '',
    recordDate: diary.recordDate ? new Date(diary.recordDate) : new Date(),
    isMilestone: diary.isMilestone || 0,
    tutorVisible: diary.tutorVisible || 0,
    selectedTags: []
  }
  showEditor.value = true
  loadDiaryTags(diary.id).then(() => {
    diaryForm.value.selectedTags = currentDiaryTags.value.map(t => t.id)
  })
}

// 保存日记
const saveDiary = async () => {
  if (!diaryForm.value.title) {
    ElMessage.warning('请输入标题')
    return
  }

  try {
    const formData = new FormData()
    formData.append('studentId', userStore.userInfo.userId)
    formData.append('title', diaryForm.value.title)
    formData.append('content', diaryForm.value.content)
    formData.append('mood', diaryForm.value.mood || '')
    formData.append('isMilestone', diaryForm.value.isMilestone)
    formData.append('tutorVisible', diaryForm.value.tutorVisible)
    formData.append('recordDate', diaryForm.value.recordDate.toISOString().split('T')[0])
    formData.append('visibility', 1)
    formData.append('isStarred', 0)

    if (editingDiary.value) {
      // 更新
      await api.put(`/diary/${editingDiary.value.id}`, {
        title: diaryForm.value.title,
        content: diaryForm.value.content,
        mood: diaryForm.value.mood,
        isMilestone: diaryForm.value.isMilestone,
        tutorVisible: diaryForm.value.tutorVisible
      })
      ElMessage.success('更新成功')
    } else {
      // 新建
      await api.post('/diary', formData)
      ElMessage.success('创建成功')
    }

    // 保存标签关联
    if (editingDiary.value && diaryForm.value.selectedTags.length > 0) {
      await diaryAnalysisApi.addTagsToDiary(
        editingDiary.value.id, 
        diaryForm.value.selectedTags
      )
    }

    showEditor.value = false
    editingDiary.value = null
    await loadDiaries()
    await loadMoodStatistics()
    await loadKeywords()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 取消编辑
const cancelEdit = () => {
  showEditor.value = false
  editingDiary.value = null
  diaryForm.value = {
    title: '',
    content: '',
    mood: '',
    recordDate: new Date(),
    isMilestone: 0,
    tutorVisible: 0,
    selectedTags: []
  }
}

// 删除日记
const deleteDiary = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这篇日记吗?', '警告', {
      type: 'warning'
    })
    await api.delete(`/diary/${id}`)
    ElMessage.success('删除成功')
    if (currentDiary.value?.id === id) {
      currentDiary.value = null
    }
    await loadDiaries()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 初始化
onMounted(async () => {
  await Promise.all([
    loadDiaries(),
    loadTags(),
    loadMoodStatistics(),
    loadKeywords(),
    loadTimeline()
  ])
})
</script>

<style scoped>
.diary-enhanced {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.quick-filters {
  margin-bottom: 15px;
  display: flex;
  gap: 10px;
}

.diary-list {
  max-height: 500px;
  overflow-y: auto;
}

.diary-item {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.diary-item:hover {
  background: #f5f7fa;
}

.diary-item.active {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}

.diary-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.diary-title {
  font-weight: 500;
  color: #303133;
}

.diary-date {
  font-size: 12px;
  color: #909399;
}

.diary-item-meta {
  display: flex;
  gap: 5px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.detail-meta {
  margin-top: 8px;
  color: #909399;
  font-size: 14px;
}

.diary-tags {
  margin-bottom: 20px;
}

.diary-content {
  line-height: 1.8;
  color: #303133;
  white-space: pre-wrap;
  min-height: 200px;
}

.tutor-comment {
  margin-top: 30px;
}

.comment-box {
  background: #f0f7ff;
  border-left: 3px solid #409eff;
  padding: 15px;
  border-radius: 4px;
}

.comment-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.comment-content {
  line-height: 1.6;
  color: #303133;
}

.keyword-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.timeline-items {
  margin-top: 10px;
}

.timeline-item-mini {
  padding: 5px 0;
  cursor: pointer;
  color: #606266;
  transition: color 0.3s;
}

.timeline-item-mini:hover {
  color: #409eff;
}

.more-hint {
  color: #909399;
  font-size: 12px;
  margin-top: 5px;
}
</style>
