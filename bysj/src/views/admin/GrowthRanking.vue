<template>
  <div>
    <div style="margin-bottom:16px;font-size:16px;font-weight:600">学生成长分排行榜</div>

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="16">
        <el-card header="Top 10 成长分柱状图">
          <div ref="barRef" style="height:300px"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card header="前三名">
          <div v-for="(item, idx) in ranking.slice(0, 3)" :key="item.studentId"
            class="top-item">
            <div class="rank-badge" :class="'rank-' + (idx+1)">{{ idx + 1 }}</div>
            <div class="rank-info">
              <div class="rank-name">{{ item.realName }}</div>
              <div class="rank-meta">{{ item.college }} · {{ item.major }}</div>
            </div>
            <div class="rank-score">{{ item.totalScore }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card header="完整排行榜">
      <el-table :data="ranking" stripe>
        <el-table-column label="排名" width="70" type="index" :index="i => i + 1" />
        <el-table-column prop="realName"   label="姓名"   width="100" />
        <el-table-column prop="studentNo"  label="学号"   width="130" />
        <el-table-column prop="college"    label="学院"   width="140" />
        <el-table-column prop="major"      label="专业"   min-width="130" />
        <el-table-column prop="termYear"   label="统计学期" width="130" />
        <el-table-column prop="totalScore" label="综合成长分" width="120">
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
import { getGrowthRanking } from '../../services/stats.js'
import * as echarts from 'echarts'

const ranking = ref([])
const barRef  = ref()

const loadRanking = async () => {
  try {
    ranking.value = await getGrowthRanking() || []
    await nextTick()
    renderBar()
  } catch (e) {
    ElMessage.error('加载排行榜失败')
  }
}

const renderBar = () => {
  if (!barRef.value) return
  const top10 = ranking.value.slice(0, 10)
  echarts.init(barRef.value).setOption({
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: top10.map(d => d.realName || d.studentId),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', min: 0, max: 100 },
    series: [{
      name: '综合成长分', type: 'bar',
      data: top10.map(d => d.totalScore),
      itemStyle: {
        color: (params) => {
          const colors = ['#FFD700', '#C0C0C0', '#CD7F32']
          return colors[params.dataIndex] || '#409EFF'
        }
      },
      barMaxWidth: 50,
      label: { show: true, position: 'top' }
    }]
  })
}

onMounted(loadRanking)
</script>

<style scoped>
.top-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 0; border-bottom: 1px solid #f0f0f0;
}
.top-item:last-child { border-bottom: none; }
.rank-badge {
  width: 32px; height: 32px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  font-weight: bold; font-size: 16px; flex-shrink: 0;
}
.rank-1 { background: #FFD700; color: #fff; }
.rank-2 { background: #C0C0C0; color: #fff; }
.rank-3 { background: #CD7F32; color: #fff; }
.rank-info { flex: 1; }
.rank-name { font-weight: 600; font-size: 15px; }
.rank-meta { font-size: 12px; color: #909399; margin-top: 2px; }
.rank-score { font-size: 22px; font-weight: bold; color: #409EFF; }
</style>
