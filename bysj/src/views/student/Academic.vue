<template>
  <div class="academic-record">
    <div class="header-actions">
      <h3>学业成绩管理</h3>
      <div>
        <input type="file" ref="fileInput" @change="handleImport" accept=".csv, .txt, .xlsx, .xls" style="display: none" />
        <el-button type="info" icon="TrendCharts" @click="goToHealthAnalysis">健康度分析</el-button>
        <el-button type="primary" @click="dialogVisible = true">人工录入成绩</el-button>
        <el-button type="warning" @click="triggerImport">导入成绩单(Excel/TXT/CSV)</el-button>
        <el-button type="success" @click="handleExport">导出成绩单(CSV)</el-button>
      </div>
    </div>

    <!-- 成绩数据概览和图表区 -->
    <el-row :gutter="20" class="margin-bottom-20">
      <el-col :span="5">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">总学分 / 必修</div>
          <div class="stat-value">{{ totalCredits.toFixed(1) }}<span class="sub-stat"> / {{ requiredCredits.toFixed(1) }}</span></div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">平均绩点(GPA)</div>
          <div class="stat-value gpa-value">{{ averageGpa.toFixed(2) }}</div>
        </el-card>
      </el-col>
      <el-col :span="5">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-title">及格率 / 不及格</div>
          <div class="stat-value" :class="{'text-danger': failCount > 0}">{{ passRate }}%<span class="sub-stat"> / {{ failCount }}门</span></div>
        </el-card>
      </el-col>
      <el-col :span="9">
        <el-card shadow="hover">
          <div ref="chartRef" style="height: 150px; width: 100%;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 成绩明细表 -->
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>成绩明细</span>
          <div class="filter-section">
            <el-select
              v-model="selectedSemester"
              placeholder="按学期筛选"
              clearable
              style="width: 200px"
              @change="handleSemesterChange"
            >
              <el-option
                v-for="semester in semesters"
                :key="semester"
                :label="semester"
                :value="semester"
              ></el-option>
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="filteredRecords" style="width: 100%" stripe>
        <el-table-column prop="semester" label="学期" width="150" sortable></el-table-column>
        <el-table-column prop="courseName" label="课程名称" min-width="150"></el-table-column>
        <el-table-column prop="courseType" label="课程性质" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.courseType === '必修' ? 'danger' : 'info'" size="small">
              {{ scope.row.courseType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="80"></el-table-column>
        <el-table-column prop="score" label="成绩" width="100"></el-table-column>
        <el-table-column prop="gpaPoint" label="绩点" width="100"></el-table-column>
        <el-table-column prop="isRetake" label="是否重修" width="100">
          <template #default="scope">
            <span v-if="scope.row.isRetake === 1" style="color: red;">是</span>
            <span v-else>否</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 录入成绩弹窗 -->
    <el-dialog title="录入成绩" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="例如: 2024-2025-1"></el-input>
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName"></el-input>
        </el-form-item>
        <el-form-item label="课程性质" prop="courseType">
          <el-select v-model="form.courseType" placeholder="请选择">
            <el-option label="必修" value="必修"></el-option>
            <el-option label="选修" value="选修"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :step="0.5"></el-input-number>
        </el-form-item>
        <el-form-item label="成绩" prop="score">
          <el-input v-model="form.score" placeholder="可输入数字或等级（如：良好、优秀）"></el-input>
        </el-form-item>
        <el-form-item label="是否重修">
          <el-radio-group v-model="form.isRetake">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { getAcademicRecords, addAcademicRecord, deleteAcademicRecord } from '@/services/academic';
import { api } from '@/services/api';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';

const router = useRouter();
const userStore = useUserStore();
const records = ref([]);
const dialogVisible = ref(false);
const formRef = ref(null);
const chartRef = ref(null);
const fileInput = ref(null);

// 学期筛选相关
const semesters = ref([]);
const selectedSemester = ref('');
const filteredRecords = ref([]);

let myChart = null;

const form = ref({
  semester: '',
  courseName: '',
  courseType: '必修',
  credit: 2.0,
  score: '',
  isRetake: 0
});

const syncForm = ref({
  htmlContent: ''
});

const rules = {
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }]
};

const syncRules = {
  htmlContent: [{ required: true, message: '请粘贴包含成绩表格的网页源码', trigger: 'blur' }]
};

const totalCredits = computed(() => {
  return records.value.reduce((sum, record) => sum + (record.credit || 0), 0);
});

const requiredCredits = computed(() => {
  return records.value.filter(r => r.courseType === '必修').reduce((sum, record) => sum + (record.credit || 0), 0);
});

const passRate = computed(() =>{
  if (records.value.length === 0) return 0;
  const passed = records.value.filter(r => {
    // 检查是否为数字成绩且大于等于60，或者是等级制成绩（如：优秀、良好、及格等）
    if (r.numericScore !== null) {
      return r.numericScore >= 60;
    } else if (typeof r.score === 'string' && r.score !== '') {
      // 等级制成绩认为是及格的
      return true;
    }
    return false;
  }).length;
  return ((passed / records.value.length) * 100).toFixed(1);
});

const failCount = computed(() => {
  return records.value.filter(r => {
    // 只有可以转换为数字的成绩才判断是否不及格
    return r.numericScore !== null && r.numericScore< 60;
  }).length;
});

const averageGpa = computed(() => {
  if (totalCredits.value === 0) return 0;
  const totalPoint = records.value.reduce((sum, record) => sum + ((record.gpaPoint || 0) * (record.credit || 0)), 0);
  return totalPoint / totalCredits.value;
});

const calculateGpa = (score) => {
  if (score >= 90) return 4.0;
  if (score >= 85) return 3.7;
  if (score >= 82) return 3.3;
  if (score >= 78) return 3.0;
  if (score >= 75) return 2.7;
  if (score >= 72) return 2.3;
  if (score >= 68) return 2.0;
  if (score >= 64) return 1.5;
  if (score >= 60) return 1.0;
  return 0.0;
};

const loadData = async () => {
  if (!userStore.userInfo?.userId) return;
  try {
    const res = await getAcademicRecords(userStore.userInfo.userId);
    // Map backend fields to frontend fields
    records.value = (res.data || res || []).map(record => ({
      id: record.id,
      semester: record.academicYear,
      courseName: record.courseName,
      courseType: record.courseNature,
      credit: record.credit,
      score: record.score,
      gpaPoint: record.gpa ? parseFloat(record.gpa) : 0,
      isRetake: record.isInvalidated === '是' ? 1 : 0,
      // 添加一个可计算的数值成绩字段
      numericScore: parseFloat(record.score) || null
    }));
    
    // 提取所有唯一的学期
    const semesterSet = new Set(records.value.map(r => r.semester));
    semesters.value = Array.from(semesterSet).sort((a, b) => {
      // 按学期排序，格式如：2024-2025-1
      return b.localeCompare(a);
    });
    
    // 应用筛选
    applyFilter();
    
    renderChart();
  } catch (error) {
    console.error(error);
  }
};

const applyFilter = () => {
  if (selectedSemester.value) {
    filteredRecords.value = records.value.filter(r => r.semester === selectedSemester.value);
  } else {
    filteredRecords.value = records.value;
  }
};

const handleSemesterChange = () => {
  applyFilter();
};

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 计算绩点，支持字符串形式的数字成绩
      const scoreValue = parseFloat(form.value.score);
      const payload = {
        studentId: userStore.userInfo.userId,
        ...form.value,
        gpaPoint: !isNaN(scoreValue) ? calculateGpa(scoreValue) : 0
      };
      
      try {
        await addAcademicRecord(payload);
        ElMessage.success('录入成功');
        dialogVisible.value = false;
        form.value = { semester: '', courseName: '', courseType: '必修', credit: 2.0, score: '', isRetake: 0 };
        loadData();
      } catch (error) {
        ElMessage.error('录入失败');
      }
    }
  });
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条成绩记录吗？', '警告', { type: 'warning' });
    await deleteAcademicRecord(id);
    ElMessage.success('删除成功');
    loadData();
  } catch (e) {
    // cancelled
  }
};

const triggerImport = () => {
  fileInput.value.click();
};

const handleImport = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  try {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api({
      url: `/academic/upload/${userStore.userInfo.userId}`,
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    ElMessage.success(response.message);
    loadData();
  } catch (err) {
    console.error(err);
    ElMessage.error('导入失败，请检查文件格式');
  } finally {
    if (fileInput.value) fileInput.value.value = '';
  }
};

const handleExport = () => {
  // Simple CSV export
  if (records.value.length === 0) return;
  const headers = ['学期', '课程名称', '课程性质', '学分', '成绩', '绩点', '是否重修'];
  let csvContent = "data:text/csv;charset=utf-8,\uFEFF" + headers.join(",") + "\n";
  
  records.value.forEach(row => {
    const rowData = [
      row.semester, row.courseName, row.courseType, row.credit, row.score, row.gpaPoint, row.isRetake === 1 ? '是' : '否'
    ];
    csvContent += rowData.join(",") + "\n";
  });
  
  const encodedUri = encodeURI(csvContent);
  const link = document.createElement("a");
  link.setAttribute("href", encodedUri);
  link.setAttribute("download", "student_academic_records.csv");
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

const renderChart = () => {
  if (!chartRef.value) return;
  if (!myChart) {
    myChart = echarts.init(chartRef.value);
  }

  // Aggregate average score by semester
  const semesterMap = {};
  records.value.forEach(r => {
    if (!semesterMap[r.semester]) {
      semesterMap[r.semester] = { sum: 0, count: 0 };
    }
    // 只统计可以转换为数字的成绩（numericScore不为null）
    if (r.numericScore !== null) {
      semesterMap[r.semester].sum += r.numericScore;
      semesterMap[r.semester].count += 1;
    }
  });

  const sortedSemesters = Object.keys(semesterMap).sort();
  const avgScores = sortedSemesters.map(s => {
    const semesterData = semesterMap[s];
    return semesterData.count > 0 ? (semesterData.sum / semesterData.count).toFixed(1) : 'N/A';
  });

  const option = {
    title: { text: '学期均分趋势', textStyle: { fontSize: 14 } },
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '25%', containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: sortedSemesters },
    yAxis: { type: 'value', min: 'dataMin', max: 100 },
    series: [{
      name: '平均分', type: 'line', smooth: true,
      data: avgScores,
      areaStyle: {},
      itemStyle: { color: '#409EFF' }
    }]
  };
  myChart.setOption(option);
};

const goToHealthAnalysis = () => {
  router.push('/student/academic-health');
};

onMounted(() => {
  nextTick(() => {
    loadData();
  });
});
</script>

<style scoped>
.academic-record {
  padding: 20px;
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.margin-bottom-20 {
  margin-bottom: 20px;
}
.stat-card {
  height: 150px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}
.stat-title {
  color: #909399;
  font-size: 14px;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}
.gpa-value {
  color: #67C23A;
}
.sub-stat {
  font-size: 16px;
  color: #909399;
  font-weight: normal;
}
.text-danger {
  color: #F56C6C;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-section {
  display: flex;
  gap: 10px;
  align-items: center;
}

.browser-container {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  height: 500px;
  display: flex;
  flex-direction: column;
}
.browser-header {
  padding: 8px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}
.teach-iframe {
  flex: 1;
  width: 100%;
  border: none;
  background: #fff;
}
</style>
