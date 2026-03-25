<template>
  <div>
    <el-card header="企业信息管理">
      <el-alert v-if="info.auditStatus === 0" type="warning" show-icon :closable="false"
        title="企业认证待审核，请耐心等待管理员审核" style="margin-bottom:16px" />
      <el-alert v-else-if="info.auditStatus === 2" type="error" show-icon :closable="false"
        title="企业认证已被驳回，请修改信息后重新提交" style="margin-bottom:16px" />
      <el-alert v-else-if="info.auditStatus === 1" type="success" show-icon :closable="false"
        title="企业已认证，可使用全部功能" style="margin-bottom:16px" />

      <el-form :model="info" label-width="120px" style="max-width:600px">
        <el-form-item label="企业名称">
          <el-input v-model="info.companyName" />
        </el-form-item>
        <el-form-item label="社会信用代码">
          <el-input v-model="info.creditCode" />
        </el-form-item>
        <el-form-item label="所属行业">
          <el-input v-model="info.industry" />
        </el-form-item>
        <el-form-item label="企业简介">
          <el-input v-model="info.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="营业执照">
          <div v-if="info.licenseFile" style="margin-bottom:8px">
            <a :href="'http://localhost:8083'+info.licenseFile" target="_blank">查看当前执照</a>
          </div>
          <el-upload :auto-upload="false" :on-change="onLicenseChange" :limit="1">
            <el-button size="small">上传营业执照</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存信息</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnterpriseInfo, updateEnterpriseInfo, uploadLicense } from '../../services/enterprise.js'

const info = ref({ companyName: '', creditCode: '', industry: '', description: '', licenseFile: '', auditStatus: -1 })
const saving = ref(false)
const licenseFile = ref(null)

const onLicenseChange = (file) => { licenseFile.value = file.raw }

const loadInfo = async () => {
  try {
    const res = await getEnterpriseInfo()
    if (res) Object.assign(info.value, res)
  } catch (e) { /* 新用户可能没有企业信息 */ }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateEnterpriseInfo(info.value)
    if (licenseFile.value) {
      const fd = new FormData()
      fd.append('file', licenseFile.value)
      await uploadLicense(fd)
    }
    ElMessage.success('保存成功')
    loadInfo()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadInfo)
</script>
