<template>
  <div>
    <div style="margin-bottom:16px;font-size:16px;font-weight:600">企业认证审核</div>

    <el-tabs v-model="activeTab" @tab-change="loadList">
      <el-tab-pane label="待审核" name="0" />
      <el-tab-pane label="已认证" name="1" />
      <el-tab-pane label="已驳回" name="2" />
      <el-tab-pane label="全部"   name="" />
    </el-tabs>

    <el-table :data="list" stripe v-loading="loading">
      <el-table-column prop="userId"      label="用户ID"   width="90" />
      <el-table-column prop="companyName" label="企业名称"  min-width="150" />
      <el-table-column prop="creditCode"  label="信用代码"  width="180" />
      <el-table-column prop="industry"    label="行业"      width="120" />
      <el-table-column prop="auditStatus" label="状态"      width="100">
        <template #default="{ row }">
          <el-tag :type="['warning','success','danger'][row.auditStatus]">
            {{ ['待审核','已认证','已驳回'][row.auditStatus] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="licenseFile" label="营业执照" width="100">
        <template #default="{ row }">
          <el-button v-if="row.licenseFile" size="small" type="primary" link
            @click="openUrl('http://localhost:8083' + row.licenseFile)">查看</el-button>
          <span v-else style="color:#ccc">未上传</span>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="企业简介" min-width="160" show-overflow-tooltip />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <template v-if="row.auditStatus === 0">
            <el-button size="small" type="success" @click="audit(row.userId, 1)">通过</el-button>
            <el-button size="small" type="danger"  @click="audit(row.userId, 2)">驳回</el-button>
          </template>
          <span v-else style="color:#ccc;font-size:13px">已处理</span>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!list.length && !loading" description="暂无数据" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnterpriseList, auditEnterprise } from '../../services/admin.js'

const list       = ref([])
const loading    = ref(false)
const activeTab  = ref('0')

const openUrl = (url) => window.open(url)

const loadList = async () => {
  loading.value = true
  try {
    const all = await getEnterpriseList() || []
    list.value = activeTab.value === ''
      ? all
      : all.filter(e => String(e.auditStatus) === activeTab.value)
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const audit = async (userId, status) => {
  await auditEnterprise(userId, status)
  ElMessage.success(status === 1 ? '已通过认证' : '已驳回')
  loadList()
}

onMounted(loadList)
</script>
