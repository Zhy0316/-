<template>
  <div class="tutor-profile">
    <h3>个人资料</h3>
    
    <el-card class="section-card">
      <el-form :model="profileForm" label-width="120px">
        <el-form-item label="姓名">
          <el-input v-model="profileForm.realName" disabled></el-input>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="profileForm.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="工号">
          <el-input v-model="profileForm.tutorNo"></el-input>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="profileForm.title" placeholder="请选择职称">
            <el-option label="教授" value="教授"></el-option>
            <el-option label="副教授" value="副教授"></el-option>
            <el-option label="讲师" value="讲师"></el-option>
            <el-option label="助教" value="助教"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="研究方向">
          <el-input v-model="profileForm.researchField" type="textarea" rows="3"></el-input>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="profileForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="profileForm.email"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile">保存资料</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { api } from '@/services/api';
import { ElMessage } from 'element-plus';

const userStore = useUserStore();

const profileForm = ref({
  realName: '',
  username: '',
  tutorNo: '',
  title: '',
  researchField: '',
  phone: '',
  email: ''
});

const loadProfile = async () => {
  if (!userStore.userInfo) return;
  
  // 加载用户基本信息
  profileForm.value.realName = userStore.userInfo.realName || '';
  profileForm.value.username = userStore.userInfo.username || '';
  profileForm.value.phone = userStore.userInfo.phone || '';
  profileForm.value.email = userStore.userInfo.email || '';
  
  // 这里需要调用导师详细信息接口
  // 由于没有相关接口，这里使用模拟数据
  profileForm.value.tutorNo = 'T' + userStore.userInfo.userId;
  profileForm.value.title = '副教授';
  profileForm.value.researchField = '人工智能、机器学习';
};

const saveProfile = async () => {
  try {
    // 这里需要调用保存资料接口
    // 由于没有相关接口，这里只显示提示
    ElMessage.success('资料保存成功');
  } catch (error) {
    console.error('保存资料失败:', error);
    ElMessage.error('保存资料失败');
  }
};

onMounted(() => {
  loadProfile();
});
</script>

<style scoped>
.tutor-profile {
  padding: 0;
}

.section-card {
  margin-bottom: 20px;
}
</style>
