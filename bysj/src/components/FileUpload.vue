<template>
  <div class="file-upload">
    <el-upload
      :action="uploadUrl"
      :headers="headers"
      :name="fieldName"
      :accept="accept"
      :limit="1"
      :show-file-list="true"
      :on-success="onSuccess"
      :on-error="onError"
      :on-progress="onProgress"
      :before-upload="beforeUpload"
      :disabled="uploading"
    >
      <el-button :loading="uploading" type="primary" plain size="small">
        <el-icon><Upload /></el-icon>
        {{ uploading ? `上传中 ${percent}%` : buttonText }}
      </el-button>
      <template #tip>
        <div class="tip" v-if="tip">{{ tip }}</div>
      </template>
    </el-upload>

    <div v-if="modelValue" class="preview">
      <el-link :href="fileUrl" target="_blank" type="primary">
        <el-icon><Link /></el-icon> 查看已上传文件
      </el-link>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload, Link } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: { type: String, default: '' },          // 已上传文件路径
  uploadPath:  { type: String, required: true },       // 后端上传接口路径（相对 /api）
  fieldName:   { type: String, default: 'file' },      // 表单字段名
  accept:      { type: String, default: '' },
  maxSizeMb:   { type: Number, default: 10 },
  buttonText:  { type: String, default: '点击上传' },
  tip:         { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue', 'uploaded'])

const uploading = ref(false)
const percent = ref(0)

const uploadUrl = computed(() => `http://localhost:8083/api${props.uploadPath}`)
const headers = computed(() => {
  const token = localStorage.getItem('token')
  return token ? { Authorization: `Bearer ${token}` } : {}
})
const fileUrl = computed(() =>
  props.modelValue ? `http://localhost:8083${props.modelValue}` : ''
)

const beforeUpload = (file) => {
  if (props.maxSizeMb && file.size / 1024 / 1024 > props.maxSizeMb) {
    ElMessage.error(`文件大小不能超过 ${props.maxSizeMb}MB`)
    return false
  }
  return true
}

const onProgress = (e) => {
  uploading.value = true
  percent.value = Math.round(e.percent || 0)
}

const onSuccess = (res) => {
  uploading.value = false
  percent.value = 0
  const path = res?.filePath || res?.data?.filePath || ''
  emit('update:modelValue', path)
  emit('uploaded', path)
  ElMessage.success('上传成功')
}

const onError = () => {
  uploading.value = false
  percent.value = 0
  ElMessage.error('上传失败，请重试')
}
</script>

<style scoped>
.file-upload { display: inline-block; }
.tip { font-size: 12px; color: #909399; margin-top: 4px; }
.preview { margin-top: 8px; font-size: 13px; }
</style>
