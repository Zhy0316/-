<template>
  <div class="wang-editor-wrapper">
    <div ref="toolbarRef" class="wang-toolbar"></div>
    <div ref="editorRef" class="wang-editor-body" :style="{ height: height }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { createEditor, createToolbar } from '@wangeditor/editor'
import '@wangeditor/editor/dist/css/style.css'

const props = defineProps({
  modelValue: { type: String, default: '' },
  height:     { type: String, default: '380px' },
  uploadImageUrl: { type: String, default: 'http://localhost:8083/api/diary/upload-image' },
  uploadImageFieldName: { type: String, default: 'file' },
  extraFormData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue'])

const toolbarRef = ref(null)
const editorRef = ref(null)
let editor = null

onMounted(() => {
  editor = createEditor({
    selector: editorRef.value,
    html: props.modelValue || '',
    config: {
      placeholder: '记录今天的学习感悟...',
      onChange(e) {
        emit('update:modelValue', e.getHtml())
      },
      MENU_CONF: {
        uploadImage: {
          server: props.uploadImageUrl,
          fieldName: props.uploadImageFieldName,
          // 动态获取 token，避免 onMounted 时 token 未就绪
          customUpload: async (file, insertFn) => {
            const token = sessionStorage.getItem('token')
            const fd = new FormData()
            fd.append(props.uploadImageFieldName, file)
            // 附加额外参数（如 studentId）
            Object.entries(props.extraFormData).forEach(([k, v]) => fd.append(k, v))
            try {
              const res = await fetch(props.uploadImageUrl, {
                method: 'POST',
                headers: token ? { Authorization: `Bearer ${token}` } : {},
                body: fd
              })
              const json = await res.json()
              // 兼容 Result<String> 格式和纯字符串
              let url = json?.data || json?.filePath || (typeof json === 'string' ? json : '')
              if (url && !url.startsWith('http')) url = `http://localhost:8083${url}`
              if (url) insertFn(url, '', url)
            } catch (e) {
              console.error('图片上传失败', e)
            }
          }
        }
      }
    },
    mode: 'default'
  })

  createToolbar({
    editor,
    selector: toolbarRef.value,
    config: {},
    mode: 'default'
  })
})

// 外部设置内容（编辑模式回填）
const setHtml = (html) => {
  if (editor) editor.setHtml(html || '')
}

const getHtml = () => editor ? editor.getHtml() : ''

// 监听外部 modelValue 变化（编辑时回填）
watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getHtml()) {
    editor.setHtml(val || '')
  }
})

onBeforeUnmount(() => {
  if (editor) { editor.destroy(); editor = null }
})

defineExpose({ setHtml, getHtml })
</script>

<style scoped>
.wang-editor-wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}
.wang-toolbar {
  border-bottom: 1px solid #dcdfe6;
}
.wang-editor-body {
  overflow-y: auto;
}
</style>
