<template>
  <div>
    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span style="font-size:16px;font-weight:600">作品集管理</span>
      <el-button type="primary" @click="openDialog()">+ 上传作品</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="8" v-for="item in list" :key="item.id" style="margin-bottom:16px">
        <el-card shadow="hover">
          <div class="work-title">{{ item.title }}</div>
          <el-tag size="small" style="margin-bottom:8px">{{ item.workType }}</el-tag>
          <div class="work-desc">{{ item.description }}</div>
          <div style="margin-top:12px;display:flex;gap:8px">
            <el-button size="small" @click="openDialog(item)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(item.id)">删除</el-button>
            <el-button size="small" type="primary" v-if="item.fileUrl"
              @click="window.open('http://localhost:8083'+item.fileUrl)">查看</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-empty v-if="!list.length" description="暂无作品，快去上传吧" />

    <!-- 上传/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作品' : '上传作品'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="作品标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作品类型">
          <el-select v-model="form.workType" style="width:100%">
            <el-option v-for="t in types" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="作品描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="作品文件">
          <el-upload :auto-upload="false" :on-change="onFileChange" :limit="1">
            <el-button size="small">选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../../stores/user.js'
import api from '../../services/api.js'

const userStore = useUserStore()
const list = ref([])
const dialogVisible = ref(false)
const saving = ref(false)
const selectedFile = ref(null)
const types = ['代码项目', '设计图', '论文/文章', '视频', '其他']

const form = ref({ id: null, title: '', workType: '代码项目', description: '' })

const loadList = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  list.value = await api.get(`/portfolio/${userId}`).catch(() => []) || []
}

const openDialog = (item = null) => {
  form.value = item
    ? { id: item.id, title: item.title, workType: item.workType, description: item.description }
    : { id: null, title: '', workType: '代码项目', description: '' }
  selectedFile.value = null
  dialogVisible.value = true
}

const onFileChange = (file) => { selectedFile.value = file.raw }

const handleSave = async () => {
  if (!form.value.title) return ElMessage.warning('请填写作品标题')
  saving.value = true
  try {
    const fd = new FormData()
    const userId = userStore.userInfo.userId
    fd.append('studentId', userId)
    fd.append('title', form.value.title)
    fd.append('workType', form.value.workType)
    fd.append('description', form.value.description || '')
    if (selectedFile.value) fd.append('file', selectedFile.value)

    if (form.value.id) {
      fd.append('id', form.value.id)
      await api.put('/portfolio', fd)
    } else {
      await api.post('/portfolio', fd)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该作品？', '提示', { type: 'warning' })
  await api.delete(`/portfolio/${id}`)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.work-title { font-size: 15px; font-weight: 600; margin-bottom: 6px; }
.work-desc { color: #606266; font-size: 13px; line-height: 1.5; min-height: 40px; }
</style>
