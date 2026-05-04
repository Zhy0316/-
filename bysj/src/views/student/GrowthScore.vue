<template>
  <div class="growth-page">
    <!-- 图表区 -->
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="10">
        <el-card header="能力雷达图">
          <div ref="radarRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card header="综合成长分趋势">
          <div ref="lineRef" style="height:300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成长分明细 -->
    <el-card style="margin-bottom:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>各学期成长分明细</span>
          <el-button type="primary" size="small" @click="recalculate" :loading="calcLoading">
            重新计算
          </el-button>
        </div>
      </template>
      <el-table :data="scores" stripe size="small">
        <el-table-column prop="termYear"      label="学期"   width="160" />
        <el-table-column prop="academicScore" label="学业分" width="100" />
        <el-table-column prop="awardScore"    label="获奖分" width="100" />
        <el-table-column prop="researchScore" label="科研分" width="100" />
        <el-table-column prop="practiceScore" label="实践分" width="100" />
        <el-table-column prop="totalScore"    label="综合成长分" width="120">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
              {{ row.totalScore }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!scores.length" description="暂无成长分数据，请先计算" :image-size="60" />
    </el-card>

    <!-- AI 分析区 -->
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>🤖 AI 成长分析</span>
          <span v-if="aiUpdateTime" style="font-size:12px;color:#c0c4cc">
            上次分析：{{ new Date(aiUpdateTime).toLocaleString() }}
          </span>
        </div>
      </template>

      <el-tabs v-model="aiTab">
        <!-- 成长分析 -->
        <el-tab-pane label="📊 成长能力分析" name="analysis">
          <div style="margin-bottom:12px;display:flex;gap:10px;align-items:center">
            <el-button type="primary" :loading="analyzing" @click="doAnalyze">
              {{ analyzing ? 'AI 分析中...' : aiAnalysis ? '重新分析' : '开始 AI 分析' }}
            </el-button>
            <el-button v-if="aiAnalysis" size="small" @click="copyText(aiAnalysis)">复制</el-button>
            <span style="font-size:12px;color:#909399">
              AI 将从学业、科研、实践、综合素质四个维度分析，并给出发展建议
            </span>
          </div>

          <div v-if="analyzing" style="text-align:center;padding:40px">
            <el-icon class="is-loading" size="32" color="#409EFF"><Loading /></el-icon>
            <div style="margin-top:12px;color:#909399">AI 正在分析你的成长数据，请稍候（约10-30秒）...</div>
          </div>
          <div v-else-if="aiAnalysis" class="ai-result" v-html="renderMarkdown(aiAnalysis)"></div>
          <el-empty v-else description="点击「开始 AI 分析」生成个性化成长报告" :image-size="60" />
        </el-tab-pane>

        <!-- 个性化学习建议 -->
        <el-tab-pane label="💡 个性化学习建议" name="advice">
          <div style="margin-bottom:12px;display:flex;gap:10px;align-items:center">
            <el-button type="warning" :loading="advising" @click="doGetAdvice">
              {{ advising ? '生成中...' : aiAdvice ? '重新生成' : '获取学习建议' }}
            </el-button>
            <el-button v-if="aiAdvice" size="small" @click="copyText(aiAdvice)">复制</el-button>
            <span style="font-size:12px;color:#909399">
              AI 将识别薄弱项，给出短期目标和长期规划建议
            </span>
          </div>

          <div v-if="advising" style="text-align:center;padding:40px">
            <el-icon class="is-loading" size="32" color="#E6A23C"><Loading /></el-icon>
            <div style="margin-top:12px;color:#909399">AI 正在生成个性化学习建议，请稍候...</div>
          </div>
          <div v-else-if="aiAdvice" class="ai-result advice-result" v-html="renderMarkdown(aiAdvice)"></div>
          <el-empty v-else description="点击「获取学习建议」生成专属学习规划" :image-size="60" />
        </el-tab-pane>

        <!-- 简历生成 -->
        <el-tab-pane label="📄 智能简历生成" name="resume">
          <!-- 风格选择 -->
          <div style="margin-bottom:14px">
            <div style="font-size:13px;color:#606266;margin-bottom:8px">选择简历风格：</div>
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
          </div>

          <div style="margin-bottom:12px;display:flex;gap:10px;align-items:center">
            <el-button type="success" :loading="generating" @click="doGenerateResume">
              {{ generating ? '生成中...' : aiResume ? '重新生成' : '生成个人简历' }}
            </el-button>
            <el-tag v-if="aiResume" size="small">{{ currentStyleLabel }}</el-tag>
            <el-button v-if="aiResume" size="small" @click="copyText(aiResume)">复制 Markdown</el-button>
            <el-button v-if="aiResume" size="small" type="primary" @click="printResume">打印/导出 PDF</el-button>
          </div>

          <div v-if="generating" style="text-align:center;padding:40px">
            <el-icon class="is-loading" size="32" color="#67C23A"><Loading /></el-icon>
            <div style="margin-top:12px;color:#909399">AI 正在生成你的个人简历，请稍候...</div>
          </div>
          <div
            v-else-if="aiResume"
            class="ai-result resume-content"
            :class="'resume-style-' + currentStyle"
            v-html="renderMarkdown(aiResume)"
          ></div>
          <el-empty v-else description="选择风格后点击「生成个人简历」" :image-size="60" />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user.js'
import { getMyGrowthScores, calculateGrowthScore, getRadarData } from '../../services/stats.js'
import { api } from '../../services/api.js'
import * as echarts from 'echarts'

const userStore = useUserStore()
const radarRef = ref()
const lineRef = ref()
const scores = ref([])
const calcLoading = ref(false)

// AI 相关
const aiTab = ref('analysis')
const analyzing = ref(false)
const generating = ref(false)
const advising   = ref(false)
const aiAnalysis = ref('')
const aiResume   = ref('')
const aiAdvice   = ref('')
const aiUpdateTime = ref(null)

// 简历风格
const selectedStyle = ref('classic')
const currentStyle  = ref('classic')
const styleOptions = [
  { value: 'classic',  label: '经典专业', icon: '📋', desc: '规范全面，适合大多数岗位' },
  { value: 'modern',   label: '现代简约', icon: '⚡', desc: '简洁有力，适合互联网行业' },
  { value: 'academic', label: '学术严谨', icon: '🎓', desc: '突出科研，适合考研/学术岗' },
  { value: 'creative', label: '创意个性', icon: '🎨', desc: '生动有趣，适合设计/创意类' },
]
const currentStyleLabel = computed(() =>
  styleOptions.find(s => s.value === currentStyle.value)?.label || ''
)

const loadData = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    scores.value = await getMyGrowthScores() || []
    await nextTick()
    renderRadar(userId)
    renderLine()
    // 加载已保存的 AI 分析结果
    loadAiResult(userId)
  } catch (e) {
    ElMessage.error('加载成长评分失败')
  }
}

const loadAiResult = async (userId) => {
  try {
    const res = await api.get(`/ai/result/${userId}`)
    if (res) {
      aiAnalysis.value  = res.aiAnalysis       || ''
      aiResume.value    = res.aiResume         || ''
      aiAdvice.value    = res.aiLearningAdvice || ''
      aiUpdateTime.value = res.aiUpdateTime    || null
    }
  } catch { /* 忽略，首次没有数据 */ }
}

const renderRadar = async (userId) => {
  if (!radarRef.value) return
  try {
    const data = await getRadarData(userId)
    echarts.init(radarRef.value).setOption({
      radar: {
        indicator: [
          { name: '学业(40%)', max: 100 }, { name: '获奖(20%)', max: 100 },
          { name: '科研(20%)', max: 100 }, { name: '实践(20%)', max: 100 },
          { name: '综合', max: 100 }
        ]
      },
      series: [{ type: 'radar', data: [{
        value: [data?.academic||0, data?.award||0, data?.research||0, data?.practice||0, data?.total||0],
        name: '能力分布', areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' }
      }]}]
    })
  } catch { /* 忽略 */ }
}

const renderLine = () => {
  if (!lineRef.value || !scores.value.length) return
  echarts.init(lineRef.value).setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['综合成长分', '学业分', '获奖分', '科研分', '实践分'] },
    xAxis: { type: 'category', data: scores.value.map(s => s.termYear) },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [
      { name: '综合成长分', type: 'line', smooth: true, data: scores.value.map(s => s.totalScore) },
      { name: '学业分',    type: 'line', smooth: true, data: scores.value.map(s => s.academicScore) },
      { name: '获奖分',    type: 'line', smooth: true, data: scores.value.map(s => s.awardScore) },
      { name: '科研分',    type: 'line', smooth: true, data: scores.value.map(s => s.researchScore) },
      { name: '实践分',    type: 'line', smooth: true, data: scores.value.map(s => s.practiceScore) },
    ]
  })
}

const recalculate = async () => {
  calcLoading.value = true
  try {
    await calculateGrowthScore(userStore.userInfo.userId)
    ElMessage.success('计算完成')
    await loadData()
  } catch { ElMessage.error('计算失败') } finally {
    calcLoading.value = false
  }
}

const doAnalyze = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  analyzing.value = true
  try {
    const res = await api.post(`/ai/analyze/${userId}`)
    aiAnalysis.value = res?.analysis || '暂无分析结果'
    aiUpdateTime.value = new Date().toISOString()
    ElMessage.success('AI 分析完成，已自动保存')
  } catch { ElMessage.error('AI 分析失败，请稍后重试') } finally {
    analyzing.value = false
  }
}

const doGenerateResume = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  generating.value = true
  currentStyle.value = selectedStyle.value
  try {
    const res = await api.post(`/ai/resume/${userId}`, { resumeStyle: selectedStyle.value })
    aiResume.value = res?.resume || '暂无简历内容'
    aiUpdateTime.value = new Date().toISOString()
    ElMessage.success('简历生成完成，已自动保存')
  } catch { ElMessage.error('简历生成失败，请稍后重试') } finally {
    generating.value = false
  }
}

const doGetAdvice = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  advising.value = true
  try {
    const res = await api.post(`/ai/learning-advice/${userId}`)
    aiAdvice.value = res?.advice || '暂无建议内容'
    aiUpdateTime.value = new Date().toISOString()
    ElMessage.success('学习建议生成完成，已自动保存')
  } catch { ElMessage.error('生成失败，请稍后重试') } finally {
    advising.value = false
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
    .replace(/\n\n/g, '<br><br>')
    .replace(/\n/g, '<br>')
}

const copyText = (text) => {
  navigator.clipboard.writeText(text)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.error('复制失败，请手动选择文本'))
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
  win.document.write(`<html><head><title>个人简历</title>
    <style>body{${bodyStyle}padding:40px;max-width:800px;margin:0 auto}
    h1{font-size:22px;text-align:center;margin-bottom:4px}
    h2{font-size:17px;border-bottom:2px solid #409EFF;padding-bottom:4px;margin-top:20px}
    h3{font-size:15px;margin:10px 0 4px}
    ul{padding-left:20px}li{margin-bottom:4px}p{line-height:1.8}strong{font-weight:bold}
    </style></head><body>${content.innerHTML}</body></html>`)
  win.document.close()
  win.print()
}

onMounted(loadData)
</script>

<style scoped>
.growth-page { padding: 4px; }
.ai-result { line-height: 1.8; color: #303133; font-size: 14px; }
.ai-result :deep(h1) { font-size: 20px; margin: 16px 0 8px; }
.ai-result :deep(h2) { font-size: 17px; margin: 14px 0 6px; color: #409EFF; border-bottom: 1px solid #e4e7ed; padding-bottom: 4px; }
.ai-result :deep(h3) { font-size: 15px; margin: 10px 0 4px; color: #606266; }
.ai-result :deep(ul) { padding-left: 20px; margin: 6px 0; }
.ai-result :deep(li) { margin-bottom: 4px; }
.ai-result :deep(strong) { color: #303133; }
/* 学习建议特殊样式 */
.advice-result :deep(h2) { color: #E6A23C; border-bottom-color: #fdf6ec; }
.advice-result :deep(li) { background: #fdf6ec; border-radius: 4px; padding: 4px 8px; margin-bottom: 6px; }

/* 风格选择卡片 */
.style-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
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
.style-icon { font-size: 20px; margin-bottom: 4px; }
.style-name { font-size: 13px; font-weight: 600; color: #303133; }
.style-desc { font-size: 11px; color: #909399; margin-top: 2px; line-height: 1.4; }

/* 简历风格差异化 */
.resume-style-modern  :deep(h2) { color: #1a1a2e; border-bottom: 2px solid #1a1a2e; }
.resume-style-academic :deep(h2) { color: #2c3e50; letter-spacing: 1px; }
.resume-style-creative :deep(h1) { color: #6c5ce7; }
.resume-style-creative :deep(h2) { color: #00b894; border-bottom: 2px dashed #00b894; }
</style>
