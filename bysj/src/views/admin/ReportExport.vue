<template>
  <div>
    <div style="margin-bottom:20px;font-size:16px;font-weight:600">报表导出</div>

    <el-row :gutter="20">
      <!-- 学院综合报表 -->
      <el-col :span="8">
        <el-card class="export-card">
          <div class="export-icon" style="background:#409EFF">
            <el-icon size="36" color="#fff"><DataAnalysis /></el-icon>
          </div>
          <div class="export-title">学院综合统计报表</div>
          <div class="export-desc">包含全院学生信息及成长分排行，Excel 格式</div>
          <el-button type="primary" style="width:100%;margin-top:16px"
            :loading="loading.college" @click="handleExport('college')">
            下载报表
          </el-button>
        </el-card>
      </el-col>

      <!-- 成绩导入模板 -->
      <el-col :span="8">
        <el-card class="export-card">
          <div class="export-icon" style="background:#67C23A">
            <el-icon size="36" color="#fff"><Document /></el-icon>
          </div>
          <div class="export-title">成绩导入 Excel 模板</div>
          <div class="export-desc">标准成绩导入模板，学生可按此格式填写后批量导入</div>
          <el-button type="success" style="width:100%;margin-top:16px"
            :loading="loading.template" @click="handleExport('template')">
            下载模板
          </el-button>
        </el-card>
      </el-col>

      <!-- 学生个人档案 -->
      <el-col :span="8">
        <el-card class="export-card">
          <div class="export-icon" style="background:#E6A23C">
            <el-icon size="36" color="#fff"><User /></el-icon>
          </div>
          <div class="export-title">学生个人成长档案</div>
          <div class="export-desc">输入学生ID，导出该学生的完整成长档案（多Sheet Excel）</div>
          <el-input v-model="studentId" placeholder="请输入学生用户ID"
            style="margin-top:12px" type="number" />
          <el-button type="warning" style="width:100%;margin-top:8px"
            :loading="loading.student" @click="handleExport('student')">
            导出档案
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 导出记录提示 -->
    <el-card style="margin-top:20px" header="使用说明">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="学院综合报表">
          包含 Sheet1（学生基本信息）和 Sheet2（成长分排行），适合学院管理人员使用
        </el-descriptions-item>
        <el-descriptions-item label="成绩导入模板">
          包含标准表头：学年、课程性质、课程名称、学分、绩点、成绩、是否成绩作废、学分绩点
        </el-descriptions-item>
        <el-descriptions-item label="学生个人档案">
          包含 5 个 Sheet：学业成绩、获奖记录、科研项目、实践记录、成长评分
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  downloadAcademicTemplate, exportStudentReport, exportCollegeReport
} from '../../services/export.js'
import { DataAnalysis, Document, User } from '@element-plus/icons-vue'

const studentId = ref('')
const loading = ref({ college: false, template: false, student: false })

const handleExport = async (type) => {
  loading.value[type] = true
  try {
    if (type === 'college') {
      await exportCollegeReport()
      ElMessage.success('学院报表下载成功')
    } else if (type === 'template') {
      await downloadAcademicTemplate()
      ElMessage.success('模板下载成功')
    } else if (type === 'student') {
      if (!studentId.value) return ElMessage.warning('请输入学生用户ID')
      await exportStudentReport(studentId.value)
      ElMessage.success('档案导出成功')
    }
  } catch (e) {
    ElMessage.error('导出失败，请稍后重试')
  } finally {
    loading.value[type] = false
  }
}
</script>

<style scoped>
.export-card { text-align: center; padding: 8px; }
.export-icon {
  width: 72px; height: 72px; border-radius: 16px;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
}
.export-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.export-desc { font-size: 13px; color: #909399; line-height: 1.6; min-height: 40px; }
</style>
