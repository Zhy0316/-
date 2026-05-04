<template>
  <div>
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" style="margin-bottom:20px">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#ecf5ff">
            <el-icon size="32" color="#409EFF"><Trophy /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">获奖总数</div>
            <div class="stat-value">{{ list.length }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#fef0f0">
            <el-icon size="32" color="#F56C6C"><Star /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">已通过审核</div>
            <div class="stat-value">{{ approvedCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background:#f0f9ff">
            <el-icon size="32" color="#67C23A"><Medal /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">获奖总分</div>
            <div class="stat-value" style="color:#E6A23C">{{ totalScore }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div style="margin-bottom:16px;display:flex;justify-content:space-between;align-items:center">
      <span style="font-size:16px;font-weight:600">获奖记录</span>
      <el-button type="primary" @click="openDialog()">+ 新增获奖记录</el-button>
    </div>

    <!-- 获奖列表 -->
    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="awardName" label="奖项名称" min-width="150" />
      <el-table-column prop="awardLevel" label="级别" width="100">
        <template #default="{ row }">
          <el-tag :type="levelType(row.awardLevel)" size="small">{{ row.awardLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="awardScore" label="分数" width="80">
        <template #default="{ row }">
          <span style="color:#E6A23C;font-weight:600">+{{ row.awardScore || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="issuingAuthority" label="颁发单位" min-width="130" show-overflow-tooltip />
      <el-table-column prop="awardDate" label="获奖日期" width="110">
        <template #default="{ row }">{{ formatDate(row.awardDate) }}</template>
      </el-table-column>
      <el-table-column label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'warning'" size="small">
            {{ row.auditStatus === 1 ? '已通过' : row.auditStatus === 2 ? '已驳回' : '待审核' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="驳回原因" min-width="150">
        <template #default="{ row }">
          <span v-if="row.auditStatus === 2 && row.rejectNote" style="color:#F56C6C;font-size:12px">{{ row.rejectNote }}</span>
          <span v-else style="color:#c0c4cc;font-size:12px">-</span>
        </template>
      </el-table-column>
      <el-table-column label="证书" width="80">
        <template #default="{ row }">
          <el-link v-if="row.certFile" :href="fileUrl(row.certFile)" target="_blank"
            type="primary" size="small">预览</el-link>
          <span v-else style="color:#c0c4cc;font-size:12px">未上传</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length && !loading" description="暂无获奖记录" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="getDialogTitle()"
      width="520px" destroy-on-close>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="奖项名称" prop="awardName">
          <el-input v-model="form.awardName" placeholder="请输入奖项名称" />
        </el-form-item>
        <el-form-item label="奖项等级" prop="awardLevelId">
          <el-select v-model="form.awardLevelId" placeholder="请选择奖项等级" style="width:100%" @change="onLevelChange">
            <el-option 
              v-for="level in awardLevels" 
              :key="level.id" 
              :label="`${level.levelName} (+${level.score}分)`" 
              :value="level.id">
              <span style="float:left">{{ level.levelName }}</span>
              <span style="float:right;color:#8492a6;font-size:13px">+{{ level.score }}分</span>
            </el-option>
          </el-select>
          <div v-if="selectedLevel" style="font-size:12px;color:#909399;margin-top:4px">
            {{ selectedLevel.description }}
          </div>
        </el-form-item>
        <el-form-item label="颁发单位" prop="issuingAuthority">
          <el-input v-model="form.issuingAuthority" placeholder="请输入颁发单位" />
        </el-form-item>
        <el-form-item label="获奖日期" prop="awardDate">
          <el-date-picker v-model="form.awardDate" type="date"
            placeholder="选择获奖日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-form-item label="奖项描述">
          <el-input v-model="form.description" type="textarea" :rows="3"
            placeholder="简要描述获奖情况（可选）" />
        </el-form-item>
        <el-form-item label="证书文件">
          <el-upload :auto-upload="false" :on-change="onFileChange" :limit="1"
            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx" :show-file-list="true">
            <el-button size="small" type="primary" plain>选择文件</el-button>
            <template #tip>
              <div style="font-size:12px;color:#909399;margin-top:4px">
                支持 JPG、PNG、PDF、Word 格式
              </div>
            </template>
          </el-upload>
          <div v-if="form.id && !selectedFile && form.certFile"
            style="font-size:12px;color:#909399;margin-top:4px">
            已有证书文件，不选择则保留原文件
          </div>
          <div v-if="form.id && form.auditStatus === 2 && form.rejectNote"
            style="font-size:12px;color:#F56C6C;margin-top:8px;padding:8px;background:#fef0f0;border-radius:4px">
            <strong>驳回原因：</strong>{{ form.rejectNote }}
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">{{ getSaveButtonText() }}</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Trophy, Star, Medal } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user.js'
import { api } from '../../services/api.js'

const userStore = useUserStore()
const list = ref([])
const awardLevels = ref([])
const totalScore = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const selectedFile = ref(null)
const formRef = ref(null)

// 计算已通过审核的数量
const approvedCount = computed(() => list.value.filter(item => item.auditStatus === 1).length)

// 当前选中的等级
const selectedLevel = computed(() => {
  if (!form.value.awardLevelId) return null
  return awardLevels.value.find(l => l.id === form.value.awardLevelId)
})

// 文件路径转完整 URL
const fileUrl = (path) => path ? `http://localhost:8083${path}` : '#'

const form = ref({
  id: null, awardName: '', awardLevelId: null,
  issuingAuthority: '', awardDate: '', description: '', certFile: ''
})

const rules = {
  awardName:        [{ required: true, message: '请输入奖项名称', trigger: 'blur' }],
  awardLevelId:     [{ required: true, message: '请选择奖项等级', trigger: 'change' }],
  issuingAuthority: [{ required: true, message: '请输入颁发单位', trigger: 'blur' }],
  awardDate:        [{ required: true, message: '请选择获奖日期', trigger: 'change' }],
}

const levelType = (level) => {
  const map = { '国家级': 'danger', '省级': 'warning', '市级': '', '校级': 'info', '院级': 'info' }
  return map[level] || 'info'
}

const formatDate = (d) => d ? new Date(d).toLocaleDateString() : '-'

// 加载获奖等级列表
const loadAwardLevels = async () => {
  try {
    awardLevels.value = await api.get('/dict/award-level/list') || []
  } catch {
    ElMessage.error('加载获奖等级失败')
  }
}

// 加载获奖记录列表
const loadList = async () => {
  const userId = userStore.userInfo?.userId
  if (!userId) return
  loading.value = true
  try {
    list.value = await api.get(`/award/${userId}`) || []
    // 加载获奖总分
    totalScore.value = await api.get(`/award/total-score/${userId}`) || 0
  } catch { 
    ElMessage.error('加载获奖记录失败') 
  } finally {
    loading.value = false
  }
}

const openDialog = (item = null) => {
  if (item) {
    form.value = {
      id: item.id, 
      awardName: item.awardName, 
      awardLevelId: item.awardLevelId,
      issuingAuthority: item.issuingAuthority,
      awardDate: item.awardDate ? new Date(item.awardDate).toISOString().split('T')[0] : '',
      description: item.description || '', 
      certFile: item.certFile || '',
      auditStatus: item.auditStatus,
      rejectNote: item.rejectNote || ''
    }
  } else {
    form.value = { 
      id: null, 
      awardName: '', 
      awardLevelId: null, 
      issuingAuthority: '', 
      awardDate: '', 
      description: '', 
      certFile: '' 
    }
  }
  selectedFile.value = null
  dialogVisible.value = true
}

const getDialogTitle = () => {
  if (form.value.id && form.value.auditStatus === 2) {
    return '重新提交获奖记录'
  }
  return form.value.id ? '编辑获奖记录' : '新增获奖记录'
}

const getSaveButtonText = () => {
  if (form.value.id && form.value.auditStatus === 2) {
    return '重新提交'
  }
  return '保存'
}

const onLevelChange = (levelId) => {
  // 等级改变时可以显示对应的分数提示
  const level = awardLevels.value.find(l => l.id === levelId)
  if (level) {
    console.log(`选择了 ${level.levelName}, 将获得 ${level.score} 分`)
  }
}

const onFileChange = (file) => { selectedFile.value = file.raw || file }

const handleSave = async () => {
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const fd = new FormData()
      fd.append('studentId', userStore.userInfo.userId)
      fd.append('awardName', form.value.awardName)
      fd.append('awardLevelId', form.value.awardLevelId)
      
      // 根据等级ID设置等级名称
      const level = awardLevels.value.find(l => l.id === form.value.awardLevelId)
      if (level) {
        fd.append('awardLevel', level.levelName)
      }
      
      fd.append('issuingAuthority', form.value.issuingAuthority)
      fd.append('awardDate', form.value.awardDate)
      if (form.value.description) fd.append('description', form.value.description)
      if (selectedFile.value) fd.append('file', selectedFile.value)

      if (form.value.id) {
        fd.append('id', form.value.id)
        await api.put('/award', fd)
      } else {
        await api.post('/award', fd)
      }
      
      const isResubmit = form.value.id && form.value.auditStatus === 2
      ElMessage.success(isResubmit ? '重新提交成功,等待导师审核' : '保存成功,等待导师审核')
      dialogVisible.value = false
      loadList()
    } catch { /* api.js 已弹错误 */ } finally {
      saving.value = false
    }
  })
}

const handleDelete = async (id) => {
  await ElMessageBox.confirm('确定删除该获奖记录？', '提示', { type: 'warning' })
  await api.delete(`/award/${id}`)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => {
  loadAwardLevels()
  loadList()
})
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}
.stat-content {
  flex: 1;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}
.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
</style>
