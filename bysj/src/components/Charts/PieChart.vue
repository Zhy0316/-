<template>
  <div ref="chartRef" :style="{ height: height, width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  data:   { type: Array, default: () => [] }, // [{ name, value }]
  height: { type: String, default: '300px' },
  donut:  { type: Boolean, default: true }
})

const chartRef = ref()
let chart = null

const render = () => {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: props.donut ? ['40%', '70%'] : '65%',
      data: props.data,
      label: { formatter: '{b}\n{d}%' }
    }]
  })
}

watch(() => props.data, render, { deep: true })
onMounted(render)
onUnmounted(() => chart?.dispose())
</script>
