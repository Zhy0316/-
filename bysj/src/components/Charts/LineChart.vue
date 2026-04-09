<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  xData:   { type: Array, default: () => [] },
  series:  { type: Array, default: () => [] }, // [{ name, data, color? }]
  height:  { type: String, default: '300px' },
  yMin:    { type: Number, default: undefined },
  yMax:    { type: Number, default: undefined }
})

const chartRef = ref()
let chart = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: props.series.map(s => s.name) },
    xAxis: { type: 'category', data: props.xData },
    yAxis: { type: 'value', min: props.yMin, max: props.yMax },
    series: props.series.map(s => ({
      name: s.name, type: 'line', smooth: true, data: s.data,
      ...(s.color ? { itemStyle: { color: s.color } } : {})
    }))
  })
}

watch(() => [props.xData, props.series], render, { deep: true })
onMounted(render)
onUnmounted(() => chart?.dispose())
</script>
