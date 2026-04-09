<template>
  <el-button
    :type="btnType"
    :size="size"
    :loading="loading"
    @click="handleExport"
  >
    <el-icon><Download /></el-icon>
    {{ loading ? '导出中...' : buttonText }}
  </el-button>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'

const props = defineProps({
  studentId: { type: [Number, String], required: true },
  buttonText: { type: String, default: '导出成长档案' },
  btnType:    { type: String, default: 'primary' },
  size:       { type: String, default: 'default' }
})

const loading = ref(false)

const handleExport = async () => {
  if (!props.studentId) {
    ElMessage.warning('学生ID不能为空')
    return
  }
  loading.value = true
  try {
    const token = localStorage.getItem('token')
    const res = await fetch(
      `http://localhost:8083/api/export/student-report/${props.studentId}`,
      { headers: token ? { Authorization: `Bearer ${token}` } : {} }
    )
    if (!res.ok) throw new Error('导出失败')
    const blob = await res.blob()
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `成长档案_${props.studentId}.xlsx`
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>
