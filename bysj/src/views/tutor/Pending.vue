<template>
  <div class="tutor-pending">
    <h3>待审核申请</h3>
    
    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>学生绑定申请</span>
        </div>
      </template>
      
      <el-table :data="pendingApplications" style="width: 100%" stripe>
        <el-table-column prop="studentName" label="学生姓名" width="120"></el-table-column>
        <el-table-column prop="studentNo" label="学号" width="150"></el-table-column>
        <el-table-column prop="college" label="学院" width="150"></el-table-column>
        <el-table-column prop="major" label="专业" min-width="150"></el-table-column>
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
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { api } from '@/services/api';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();

const pendingApplications = ref([]);

const loadPendingApplications = async () => {
  if (!userStore.userInfo?.userId) return;
  
  try {
    // 获取待审核申请
    const response = await api({
      url: `/relation/tutor/${userStore.userInfo.userId}/pending`,
      method: 'get'
    });
    
    const applications = response.data || [];
    
    // 获取学生信息（这里需要调用学生信息接口）
    // 由于没有学生信息接口，这里使用模拟数据
    pendingApplications.value = applications.map(r => ({
      id: r.id,
      studentId: r.studentId,
      studentName: '学生' + r.studentId,
      studentNo: '2024' + r.studentId,
      college: '计算机学院',
      major: '计算机科学与技术',
      applyTime: r.applyTime ? new Date(r.applyTime).toLocaleString() : ''
    }));
      
  } catch (error) {
    console.error('加载待审核申请失败:', error);
    ElMessage.error('加载待审核申请失败');
  }
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
    loadPendingApplications();
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
    loadPendingApplications();
  } catch (error) {
    console.error('审核失败:', error);
    ElMessage.error('审核失败');
  }
};

onMounted(() => {
  loadPendingApplications();
});
</script>

<style scoped>
.tutor-pending {
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
</style>
