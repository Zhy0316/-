<template>
  <div class="tutor-students">
    <h3>我的学生</h3>
    
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>学生列表</span>
          <div class="filter-section">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索学生姓名或学号"
              clearable
              style="width: 200px"
              @input="handleSearch"
            >
              <template #append>
                <el-button @click="handleSearch">搜索</el-button>
              </template>
            </el-input>
          </div>
        </div>
      </template>
      
      <el-table :data="students" style="width: 100%" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="studentNo" label="学号" width="150"></el-table-column>
        <el-table-column prop="college" label="学院" width="150"></el-table-column>
        <el-table-column prop="major" label="专业" min-width="150"></el-table-column>
        <el-table-column prop="className" label="班级" width="120"></el-table-column>
        <el-table-column prop="enrollmentYear" label="入学年份" width="120"></el-table-column>
        <el-table-column prop="bindTime" label="绑定时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="primary" @click="viewStudentProfile(scope.row)">查看档案</el-button>
            <el-button size="small" type="info" @click="viewStudentRecords(scope.row)">查看记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="students.length === 0" description="暂无绑定的学生" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { api } from '@/services/api';
import { ElMessage } from 'element-plus';

const router = useRouter();
const userStore = useUserStore();

const students = ref([]);
const searchKeyword = ref('');
const filteredStudents = ref([]);

const loadStudents = async () => {
  if (!userStore.userInfo?.userId) return;
  
  try {
    // 获取绑定关系
    const relationsResponse = await api({
      url: `/relation/tutor/${userStore.userInfo.userId}`,
      method: 'get'
    });
    
    const relations = relationsResponse.data || [];
    
    // 获取学生信息（这里需要调用学生信息接口）
    // 由于没有学生信息接口，这里使用模拟数据
    students.value = relations
      .filter(r => r.status === 1)
      .map(r => ({
        id: r.id,
        studentId: r.studentId,
        studentName: '学生' + r.studentId,
        studentNo: '2024' + r.studentId,
        college: '计算机学院',
        major: '计算机科学与技术',
        className: '软件工程' + Math.floor(r.studentId % 5) + '班',
        enrollmentYear: 2024,
        bindTime: r.auditTime ? new Date(r.auditTime).toLocaleString() : ''
      }));
      
  } catch (error) {
    console.error('加载学生列表失败:', error);
    ElMessage.error('加载学生列表失败');
  }
};

const handleSearch = () => {
  if (!searchKeyword.value) {
    students.value = filteredStudents.value;
    return;
  }
  
  const keyword = searchKeyword.value.toLowerCase();
  students.value = filteredStudents.value.filter(student => 
    student.studentName.toLowerCase().includes(keyword) ||
    student.studentNo.toLowerCase().includes(keyword)
  );
};

const viewStudentProfile = (student) => {
  // 这里可以跳转到学生档案页面
  console.log('查看学生档案:', student);
};

const viewStudentRecords = (student) => {
  // 这里可以跳转到学生记录页面
  console.log('查看学生记录:', student);
};

onMounted(() => {
  loadStudents();
});
</script>

<style scoped>
.tutor-students {
  padding: 0;
}

.section-card {
  margin-bottom: 20px;
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
</style>
