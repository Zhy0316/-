<template>
  <div class="growth-page">
    <el-row :gutter="20">
      <!-- 雷达图 -->
      <el-col :span="10">
        <el-card header="能力雷达图">
          <div ref="radarRef" style="height:320px"></div>
        </el-card>
      </el-col>
      <!-- 综合成长分趋势 -->
      <el-col :span="14">
        <el-card header="综合成长分趋势">
          <div ref="lineRef" style="height:320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top:20px" header="各学期成长分明细">
      <el-button type="primary" size="small" @click="recalculate" :loading="calcLoading" style="margin-bottom:12px">
        重新计算当前学期
      </el-button>
      <el-table :data="scores" stripe>
        <el-table-column prop="termYear"      label="学期"     width="160" />
        <el-table-column prop="academicScore" label="学业分"   width="100" />
        <el-table-column prop="awardScore"    label="获奖分"   width="100" />
        <el-table-column prop="researchScore" label="科研分"   width="100" />
        <el-table-column prop="practiceScore" label="实践分"   width="100" />
        <el-table-column prop="totalScore"    label="综合成长分" width="120">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 80 ? 'success' : row.totalScore >= 60 ? 'warning' : 'danger'">
              {{ row.totalScore }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import { getMyGrowthScores, calculateGrowthScore, getRadarData } from '../../services/stats.js'
import * as echarts from 'echarts'

const userStore = useUserStore()
const radarRef = ref()
const lineRef = ref()
const scores = ref([])
const calcLoading = ref(false)

const loadData = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  try {
    scores.value = await getMyGrowthScores() || []
    await nextTick()
    renderRadar(userId)
    renderLine()
  } catch (e) {
    ElMessage.error('加载成长评分失败')
  }
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
      series: [{
        type: 'radar',
        data: [{
          value: [data?.academic||0, data?.award||0, data?.research||0, data?.practice||0, data?.total||0],
          name: '能力分布',
          areaStyle: { opacity: 0.3 },
          itemStyle: { color: '#409EFF' }
        }]
      }]
    })
  } catch (e) { /* 忽略 */ }
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
  } catch (e) {
    ElMessage.error('计算失败')
  } finally {
    calcLoading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.growth-page { padding: 4px; }
</style>
