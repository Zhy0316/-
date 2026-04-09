<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  indicators: { type: Array, default: () => [] }, // [{ name, max }]
  seriesData: { type: Array, default: () => [] },  // [{ name, value: [] }]
  height: { type: String, default: '300px' }
})

const chartRef = ref()
let chart = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    radar: { indicator: props.indicators },
    series: [{
      type: 'radar',
      data: props.seriesData.map(s => ({
        name: s.name,
        value: s.value,
        areaStyle: { opacity: 0.3 },
        itemStyle: { color: '#409EFF' }
      }))
    }]
  })
}

watch(() => [props.indicators, props.seriesData], render, { deep: true })
onMounted(render)
onUnmounted(() => chart?.dispose())
</script>
