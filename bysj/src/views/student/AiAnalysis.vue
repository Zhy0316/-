<template>
  <div class="ai-page">
    <div class="page-title-bar">
      <span class="page-title">🤖 AI 成长分析助手</span>
      <span class="page-sub">基于你的成长档案数据，由千问 AI 生成分析报告</span>
    </div>

    <el-row :gutter="20">
      <!-- 左侧：功能入口 -->
      <el-col :span="8">

        <!-- 成长能力分析 -->
        <el-card style="margin-bottom:16px">
          <template #header><span>📊 成长能力分析</span></template>
          <div class="card-desc">
            AI 将从学业、科研、实践、综合素质四个维度分析你的成长数据，并给出个性化的发展方向建议。
          </div>
          <el-button type="primary" style="width:100%" :loading="analyzing" @click="doAnalyze">
            {{ analyzing ? '分析中...' : '开始分析' }}
          </el-button>
        </el-card>

        <!-- 智能简历生成 -->
        <el-card>
          <template #header><span>📄 智能简历生成</span></template>
          <div class="card-desc">选择简历风格，AI 将根据你的档案自动生成专业简历。</div>

          <!-- 风格选择 -->
          <div class="style-grid">
            <div
              v-for="s in styleOptions"
              :key="s.value"
              class="style-card"
              :class="{ selected: selectedStyle === s.value }"
              @click="selectedStyle = s.value"
            >
              <div class="style-icon">{{ s.icon }}</div>
              <div class="style-name">{{ s.label }}</div>
              <div class="style-desc">{{ s.desc }}</div>
            </div>
          </div>

          <el-button
            type="success"
            style="width:100%;margin-top:12px"
            :loading="generating"
            @click="doGenerateResume"
          >
            {{ generating ? '生成中...' : '生成简历' }}
          </el-button>
        </el-card>
      </el-col>

      <!-- 右侧：结果展示 -->
      <el-col :span="16">

        <!-- 分析结果 -->
        <el-card v-if="analysisResult || analyzing" style="margin-bottom:16px">
          <template #header>
            <div class="result-header">
              <span>📊 成长能力分析报告</span>
              <el-button v-if="analysisResult" size="small" @click="copyText(analysisResult)">复制</el-button>
            </div>
          </template>
          <div v-if="analyzing" class="loading-box">
            <el-icon class="is-loading" size="32" color="#409EFF"><Loading /></el-icon>
            <div class="loading-text">AI 正在分析你的成长数据，请稍候...</div>
          </div>
          <div v-else class="ai-result" v-html="renderMarkdown(analysisResult)"></div>
        </el-card>

        <!-- 简历结果 -->
        <el-card v-if="resumeResult || generating">
          <template #header>
            <div class="result-header">
              <span>
                📄 个人简历
                <el-tag size="small" style="margin-left:8px">{{ currentStyleLabel }}</el-tag>
              </span>
              <div style="display:flex;gap:8px">
                <el-button v-if="resumeResult" size="small" @click="copyText(resumeResult)">复制 Markdown</el-button>
                <el-button v-if="resumeResult" size="small" type="primary" @click="printResume">打印/导出 PDF</el-button>
              </div>
            </div>
          </template>
          <div v-if="generating" class="loading-box">
            <el-icon class="is-loading" size="32" color="#67C23A"><Loading /></el-icon>
            <div class="loading-text">AI 正在生成你的个人简历，请稍候...</div>
          </div>
          <div
            v-else
            class="ai-result resume-content"
            :class="'resume-style-' + currentStyle"
            v-html="renderMarkdown(resumeResult)"
          ></div>
        </el-card>

        <!-- 空状态 -->
        <el-card v-if="!analysisResult && !resumeResult && !analyzing && !generating">
          <el-empty description="点击左侧按钮开始 AI 分析" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'

const userStore = useUserStore()

const analyzing  = ref(false)
const generating = ref(false)
const analysisResult = ref('')
const resumeResult   = ref('')
const selectedStyle  = ref('classic')
const currentStyle   = ref('classic')

const styleOptions = [
  { value: 'classic',  label: '经典专业', icon: '📋', desc: '规范全面，适合大多数岗位' },
  { value: 'modern',   label: '现代简约', icon: '⚡', desc: '简洁有力，适合互联网行业' },
  { value: 'academic', label: '学术严谨', icon: '🎓', desc: '突出科研，适合考研/学术岗' },
  { value: 'creative', label: '创意个性', icon: '🎨', desc: '生动有趣，适合设计/创意类' },
]

const currentStyleLabel = computed(() =>
  styleOptions.find(s => s.value === currentStyle.value)?.label || ''
)

const doAnalyze = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  analyzing.value = true
  analysisResult.value = ''
  try {
    const res = await api.post(`/ai/analyze/${userId}`)
    analysisResult.value = res?.analysis || '暂无分析结果'
  } catch {
    ElMessage.error('AI 分析失败，请稍后重试')
  } finally {
    analyzing.value = false
  }
}

const doGenerateResume = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  generating.value = true
  resumeResult.value = ''
  currentStyle.value = selectedStyle.value
  try {
    const res = await api.post(`/ai/resume/${userId}`, { resumeStyle: selectedStyle.value })
    resumeResult.value = res?.resume || '暂无简历内容'
  } catch {
    ElMessage.error('简历生成失败，请稍后重试')
  } finally {
    generating.value = false
  }
}

// 简单 Markdown 渲染
const renderMarkdown = (text) => {
  if (!text) return ''
  return text
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/(<li>[\s\S]*?<\/li>)/g, '<ul>$1</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')
}

const copyText = (text) => {
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败，请手动选择文本复制'))
}

const printResume = () => {
  const content = document.querySelector('.resume-content')
  if (!content) return
  const styleMap = {
    classic:  'font-family:"SimSun",serif; color:#222;',
    modern:   'font-family:"Microsoft YaHei",sans-serif; color:#1a1a2e;',
    academic: 'font-family:"SimSun",serif; color:#1a1a1a; line-height:2;',
    creative: 'font-family:"Microsoft YaHei",sans-serif; color:#2d3436;',
  }
  const bodyStyle = styleMap[currentStyle.value] || styleMap.classic
  const win = window.open('', '_blank')
  win.document.write(`
    <html><head><title>个人简历</title>
    <style>
      body { ${bodyStyle} padding:40px; max-width:800px; margin:0 auto; }
      h1 { font-size:22px; text-align:center; margin-bottom:4px; }
      h2 { font-size:17px; border-bottom:2px solid #409EFF; padding-bottom:4px; margin-top:20px; }
      h3 { font-size:15px; margin:10px 0 4px; }
      ul { padding-left:20px; } li { margin-bottom:4px; }
      p { line-height:1.8; } strong { font-weight:bold; }
    </style></head>
    <body>${content.innerHTML}</body></html>
  `)
  win.document.close()
  win.print()
}
</script>

<style scoped>
.ai-page { padding: 4px; }
.page-title-bar { margin-bottom: 20px; }
.page-title { font-size: 16px; font-weight: 600; }
.page-sub { font-size: 13px; color: #909399; margin-left: 12px; }
.card-desc { color: #606266; font-size: 13px; margin-bottom: 16px; line-height: 1.6; }

/* 风格选择网格 */
.style-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}
.style-card {
  border: 2px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px 8px;
  cursor: pointer;
  text-align: center;
  transition: all .2s;
}
.style-card:hover { border-color: #409EFF; background: #f0f7ff; }
.style-card.selected { border-color: #409EFF; background: #ecf5ff; }
.style-icon { font-size: 22px; margin-bottom: 4px; }
.style-name { font-size: 13px; font-weight: 600; color: #303133; }
.style-desc { font-size: 11px; color: #909399; margin-top: 2px; line-height: 1.4; }

/* 结果区 */
.result-header { display: flex; justify-content: space-between; align-items: center; }
.loading-box { text-align: center; padding: 40px; }
.loading-text { margin-top: 12px; color: #909399; }

.ai-result {
  line-height: 1.8;
  color: #303133;
  font-size: 14px;
}
.ai-result :deep(h1) { font-size: 20px; margin: 16px 0 8px; }
.ai-result :deep(h2) { font-size: 17px; margin: 14px 0 6px; color: #409EFF; border-bottom: 1px solid #e4e7ed; padding-bottom: 4px; }
.ai-result :deep(h3) { font-size: 15px; margin: 10px 0 4px; color: #606266; }
.ai-result :deep(ul) { padding-left: 20px; margin: 6px 0; }
.ai-result :deep(li) { margin-bottom: 4px; }
.ai-result :deep(strong) { color: #303133; }
.ai-result :deep(p) { margin: 8px 0; }

/* 简历风格差异化样式 */
.resume-style-modern :deep(h2) { color: #1a1a2e; border-bottom: 2px solid #1a1a2e; }
.resume-style-academic :deep(h2) { color: #2c3e50; font-size: 16px; letter-spacing: 1px; }
.resume-style-creative :deep(h1) { color: #6c5ce7; }
.resume-style-creative :deep(h2) { color: #00b894; border-bottom: 2px dashed #00b894; }
</style>
