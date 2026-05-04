<template>
  <div class="hr-profile">

    <!-- 认证状态横幅 -->
    <div class="cert-banner" :class="bannerClass">
      <div class="cert-banner-left">
        <el-icon size="28"><component :is="bannerIcon" /></el-icon>
        <div>
          <div class="cert-title">{{ bannerTitle }}</div>
          <div class="cert-sub">{{ bannerSub }}</div>
        </div>
      </div>
      <el-steps :active="certStep" finish-status="success" class="cert-steps">
        <el-step title="注册账号" />
        <el-step title="完善信息" />
        <el-step title="上传执照" />
        <el-step title="等待审核" />
        <el-step title="认证通过" />
      </el-steps>
    </div>

    <el-row :gutter="20" style="margin-top:20px">
      <!-- 左侧：企业信息表单 -->
      <el-col :span="14">
        <el-card header="企业基本信息">
          <el-form :model="info" :rules="rules" ref="formRef" label-width="120px">
            <el-form-item label="企业名称" prop="companyName">
              <el-input v-model="info.companyName" placeholder="请输入企业全称" />
            </el-form-item>
            <el-form-item label="社会信用代码" prop="creditCode">
              <el-input v-model="info.creditCode" placeholder="18位统一社会信用代码" maxlength="18" />
            </el-form-item>
            <el-form-item label="所属行业">
              <el-select v-model="info.industry" placeholder="请选择行业" style="width:100%">
                <el-option v-for="i in industries" :key="i" :label="i" :value="i" />
              </el-select>
            </el-form-item>
            <el-form-item label="企业规模">
              <el-select v-model="info.companySize" placeholder="请选择规模" style="width:100%">
                <el-option label="少于50人"    value="少于50人" />
                <el-option label="50-200人"   value="50-200人" />
                <el-option label="200-1000人" value="200-1000人" />
                <el-option label="1000人以上" value="1000人以上" />
              </el-select>
            </el-form-item>
            <el-form-item label="企业官网">
              <el-input v-model="info.website" placeholder="https://..." />
            </el-form-item>
            <el-form-item label="企业简介">
              <el-input v-model="info.description" type="textarea" :rows="4"
                placeholder="介绍企业主营业务、文化氛围等" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSave">保存信息</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <!-- 右侧：认证材料 + 状态 -->
      <el-col :span="10">
        <el-card header="认证材料" style="margin-bottom:16px">
          <div class="license-area">
            <div v-if="info.licenseFile" class="license-preview">
              <el-image
                v-if="isImage(info.licenseFile)"
                :src="'http://localhost:8083' + info.licenseFile"
                fit="contain"
                style="width:100%;max-height:180px;border-radius:6px"
              />
              <div v-else class="license-file-link">
                <el-icon size="32" color="#409EFF"><Document /></el-icon>
                <a :href="'http://localhost:8083' + info.licenseFile" target="_blank">
                  查看已上传执照
                </a>
              </div>
              <el-tag type="success" size="small" style="margin-top:8px">已上传</el-tag>
            </div>
            <el-empty v-else description="尚未上传营业执照" :image-size="60" />
          </div>

          <el-upload
            :auto-upload="false"
            :limit="1"
            :on-change="onLicenseChange"
            accept=".jpg,.jpeg,.png,.pdf"
            style="margin-top:12px"
          >
            <el-button type="primary" plain style="width:100%">
              {{ info.licenseFile ? '重新上传执照' : '上传营业执照' }}
            </el-button>
            <template #tip>
              <div style="color:#909399;font-size:12px;margin-top:4px">
                支持 JPG/PNG/PDF，文件大小不超过 10MB
              </div>
            </template>
          </el-upload>

          <el-button
            v-if="licenseFile"
            type="success"
            style="width:100%;margin-top:8px"
            :loading="uploadingLicense"
            @click="handleUploadLicense"
          >确认上传</el-button>
        </el-card>

        <!-- 审核状态卡片 -->
        <el-card header="审核状态">
          <el-timeline>
            <el-timeline-item
              timestamp="账号注册"
              placement="top"
              type="success"
              :hollow="false"
            >账号已创建</el-timeline-item>

            <el-timeline-item
              timestamp="信息完善"
              placement="top"
              :type="info.companyName && info.creditCode ? 'success' : 'warning'"
            >
              {{ info.companyName && info.creditCode ? '基本信息已填写' : '请完善企业名称和信用代码' }}
            </el-timeline-item>

            <el-timeline-item
              timestamp="执照上传"
              placement="top"
              :type="info.licenseFile ? 'success' : 'warning'"
            >
              {{ info.licenseFile ? '营业执照已上传' : '请上传营业执照' }}
            </el-timeline-item>

            <el-timeline-item
              timestamp="管理员审核"
              placement="top"
              :type="auditTimelineType"
            >{{ auditTimelineText }}</el-timeline-item>
          </el-timeline>

          <div v-if="info.auditStatus === 2" style="margin-top:12px">
            <el-alert type="error" :closable="false" show-icon
              title="认证被驳回，请修改信息后联系管理员重新审核" />
          </div>
          <div v-if="info.auditStatus === 1" style="margin-top:12px">
            <el-alert type="success" :closable="false" show-icon
              title="认证已通过，您可以浏览学生档案和发布招聘" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, SuccessFilled, WarningFilled, CircleCloseFilled, Loading } from '@element-plus/icons-vue'
import { getEnterpriseInfo, updateEnterpriseInfo, uploadLicense } from '../../services/enterprise.js'

const info = ref({
  companyName: '', creditCode: '', industry: '', companySize: '',
  website: '', description: '', licenseFile: '', auditStatus: -1
})
const saving = ref(false)
const licenseFile = ref(null)
const uploadingLicense = ref(false)
const formRef = ref(null)

const industries = [
  '互联网/软件', '金融/银行', '制造业', '教育培训', '医疗健康',
  '房地产/建筑', '零售/电商', '物流/运输', '咨询/服务', '其他'
]

const rules = {
  companyName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  creditCode:  [{ required: true, message: '请输入社会信用代码', trigger: 'blur' },
                { len: 18, message: '信用代码为18位', trigger: 'blur' }]
}

// 认证步骤（更精确的步骤判断）
const certStep = computed(() => {
  // 步骤5: 认证通过
  if (info.value.auditStatus === 1) return 5
  // 步骤4: 等待审核（已上传执照且信息完整）
  if (info.value.auditStatus === 0 && info.value.licenseFile && 
      info.value.companyName && info.value.creditCode) return 4
  // 步骤3: 已上传执照但信息不完整
  if (info.value.licenseFile) return 3
  // 步骤2: 已填写基本信息
  if (info.value.companyName && info.value.creditCode) return 2
  // 步骤1: 刚注册
  return 1
})

const bannerClass = computed(() => ({
  'banner-success': info.value.auditStatus === 1,
  'banner-warning': info.value.auditStatus === 0,
  'banner-danger':  info.value.auditStatus === 2,
  'banner-info':    info.value.auditStatus === -1,
}))

const bannerIcon = computed(() => {
  if (info.value.auditStatus === 1) return 'SuccessFilled'
  if (info.value.auditStatus === 2) return 'CircleCloseFilled'
  if (info.value.auditStatus === 0) return 'Loading'
  return 'WarningFilled'
})

const bannerTitle = computed(() => {
  const map = { 1: '企业认证已通过', 0: '认证审核中', 2: '认证被驳回', '-1': '尚未提交认证' }
  return map[info.value.auditStatus] || '认证状态未知'
})

const bannerSub = computed(() => {
  const map = {
    1:  '您已获得完整权限，可浏览学生档案、发布招聘',
    0:  '请耐心等待管理员审核，通常1-3个工作日内完成',
    2:  '请修改企业信息并重新上传营业执照，联系管理员重审',
    '-1': '请完善企业信息并上传营业执照，提交认证申请'
  }
  return map[info.value.auditStatus] || ''
})

const auditTimelineType = computed(() => {
  if (info.value.auditStatus === 1) return 'success'
  if (info.value.auditStatus === 2) return 'danger'
  if (info.value.auditStatus === 0) return 'warning'
  return 'info'
})

const auditTimelineText = computed(() => {
  if (info.value.auditStatus === 1) return '审核通过 ✓'
  if (info.value.auditStatus === 2) return '审核驳回，请修改后重新提交'
  if (info.value.auditStatus === 0) return '等待管理员审核中...'
  return '待提交'
})

const isImage = (path) => /\.(jpg|jpeg|png|gif)$/i.test(path)

const onLicenseChange = (file) => { licenseFile.value = file.raw }

const loadInfo = async () => {
  try {
    const res = await getEnterpriseInfo()
    if (res) Object.assign(info.value, res)
  } catch { /* 新用户可能没有企业信息 */ }
}

const handleSave = async () => {
  await formRef.value?.validate()
  saving.value = true
  try {
    await updateEnterpriseInfo(info.value)
    ElMessage.success('保存成功')
    loadInfo()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleUploadLicense = async () => {
  if (!licenseFile.value) return
  
  // 检查是否已填写必要信息
  if (!info.value.companyName || !info.value.creditCode) {
    ElMessage.warning('请先完善企业名称和社会信用代码')
    return
  }
  
  uploadingLicense.value = true
  try {
    const fd = new FormData()
    fd.append('file', licenseFile.value)
    await uploadLicense(fd)
    ElMessage.success('执照上传成功，已提交审核申请，请等待管理员审核')
    licenseFile.value = null
    loadInfo()
  } catch {
    ElMessage.error('上传失败')
  } finally {
    uploadingLicense.value = false
  }
}

onMounted(loadInfo)
</script>

<style scoped>
.hr-profile { padding: 4px; }

.cert-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-radius: 10px;
  gap: 20px;
  flex-wrap: wrap;
}
.banner-success { background: linear-gradient(135deg, #f0f9eb, #e1f3d8); border: 1px solid #b3e19d; }
.banner-warning { background: linear-gradient(135deg, #fdf6ec, #faecd8); border: 1px solid #f5dab1; }
.banner-danger  { background: linear-gradient(135deg, #fef0f0, #fde2e2); border: 1px solid #fbc4c4; }
.banner-info    { background: linear-gradient(135deg, #f4f4f5, #e9e9eb); border: 1px solid #d3d4d6; }

.cert-banner-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
.cert-title { font-size: 16px; font-weight: 600; color: #303133; }
.cert-sub   { font-size: 13px; color: #606266; margin-top: 2px; }
.cert-steps { flex: 1; min-width: 300px; }

.license-area {
  min-height: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.license-file-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: #409EFF;
}
</style>
