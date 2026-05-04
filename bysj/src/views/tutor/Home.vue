<template>
  <div class="tutor-home">
    <h3>教师首页</h3>
    
    <div class="stats-section">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">指导学生总数</div>
            <div class="stat-value">{{ totalStudents }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">待审核申请</div>
            <div class="stat-value pending-count">{{ pendingCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">已绑定学生</div>
            <div class="stat-value">{{ boundCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">待审核申请</div>
            <div class="stat-value pending-count">{{ pendingCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">今日访问</div>
            <div class="stat-value">0</div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <div class="recent-students">
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span>最近绑定的学生</span>
            <el-button type="primary" size="small" @click="goToStudents">查看全部</el-button>
          </div>
        </template>
        
        <el-table :data="recentStudents" style="width: 100%" stripe>
          <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
          <el-table-column prop="studentNo" label="学号" width="150"></el-table-column>
          <el-table-column prop="major" label="专业" min-width="150"></el-table-column>
          <el-table-column prop="bindTime" label="绑定时间" width="180"></el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button size="small" type="primary" @click="viewStudent(scope.row)">查看</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="recentStudents.length === 0" description="暂无最近绑定的学生" />
      </el-card>
    </div>
    
    <div class="pending-applications">
      <el-card class="section-card">
        <template #header>
          <div class="card-header">
            <span>待审核的绑定申请</span>
            <el-button type="warning" size="small" @click="goToPending">查看全部</el-button>
          </div>
        </template>
        
        <el-table :data="pendingApplications" style="width: 100%" stripe>
          <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
          <el-table-column prop="studentNo" label="学号" width="150"></el-table-column>
          <el-table-column prop="applyTime" label="申请时间" width="180"></el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button size="small" type="success" @click="approveApplication(scope.row)">通过</el-button>
              <el-button size="small" type="danger" @click="rejectApplication(scope.row)">拒绝</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="pendingApplications.length === 0" description="暂无待审核申请" />
      </el-card>
    </div>
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

const totalStudents = ref(0);
const pendingCount = ref(0);
const boundCount = ref(0);
const recentStudents = ref([]);
const pendingApplications = ref([]);

const loadData = async () => {
  if (!userStore.userInfo?.userId) return;
  
  try {
    // 获取绑定关系
    const relationsResponse = await api({
      url: `/relation/tutor/${userStore.userInfo.userId}`,
      method: 'get'
    });
    
    const relations = relationsResponse.data || [];
    totalStudents.value = relations.length;
    boundCount.value = relations.filter(r => r.status === 1).length;
    
    // 获取待审核申请
    const pendingResponse = await api({
      url: `/relation/tutor/${userStore.userInfo.userId}/pending`,
      method: 'get'
    });
    
    const pending = pendingResponse.data || [];
    pendingCount.value = pending.length;
    pendingApplications.value = pending.slice(0, 5);
    
    // 获取最近绑定的学生（模拟数据）
    recentStudents.value = relations
      .filter(r => r.status === 1)
      .slice(0, 5)
      .map(r => ({
        id: r.id,
        studentId: r.studentId,
        studentName: '学生姓名',
        studentNo: '2024' + r.studentId,
        major: '计算机科学与技术',
        bindTime: r.auditTime ? new Date(r.auditTime).toLocaleString() : ''
      }));
      
  } catch (error) {
    console.error('加载数据失败:', error);
    ElMessage.error('加载数据失败');
  }
};

const goToStudents = () => {
  router.push('/tutor/students');
};

const goToPending = () => {
  router.push('/tutor/pending');
};

const viewStudent = (student) => {
  // 这里可以跳转到学生详情页面
  console.log('查看学生:', student);
};

const approveApplication = async (application) => {
  try {
    await api({
      url: '/relation/audit',
      method: 'put',
      data: {
        id: application.id,
        status: 1
      }
    });
    
    ElMessage.success('审核通过');
    loadData();
  } catch (error) {
    console.error('审核失败:', error);
    ElMessage.error('审核失败');
  }
};

const rejectApplication = async (application) => {
  try {
    await api({
      url: '/relation/audit',
      method: 'put',
      data: {
        id: application.id,
        status: 2
      }
    });
    
    ElMessage.success('审核拒绝');
    loadData();
  } catch (error) {
    console.error('审核失败:', error);
    ElMessage.error('审核失败');
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped>
.tutor-home {
  padding: 0;
}

.stats-section {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
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

.pending-count {
  color: #E6A23C;
}

.section-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
