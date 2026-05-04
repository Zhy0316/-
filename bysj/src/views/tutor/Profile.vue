<template>
  <div class="tutor-profile">
    <h3>个人资料</h3>

    <el-card class="section-card">
      <el-form :model="profileForm" label-width="120px">
        <el-form-item label="头像">
          <div class="avatar-section">
            <el-avatar :size="80" :src="avatarUrl" class="avatar-preview">
              {{ profileForm.realName?.charAt(0) || '导师' }}
            </el-avatar>
            <div class="avatar-upload">
              <input type="file" ref="avatarInput" @change="handleAvatarChange" accept="image/*" style="display:none" />
              <el-button size="small" @click="triggerAvatarInput" :loading="uploadingAvatar">
                <el-icon><Upload /></el-icon> 上传头像
              </el-button>
              <div class="avatar-tip">支持 JPG/PNG，建议尺寸 200x200</div>
            </div>
          </div>
        </el-form-item>

        <el-divider />

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
import { ref, computed, onMounted } from 'vue';
import { useUserStore } from '@/stores/user';
import { api } from '@/services/api';
import { ElMessage } from 'element-plus';
import { Upload } from '@element-plus/icons-vue';

const userStore = useUserStore();
const avatarInput = ref(null);
const selectedAvatar = ref(null);
const uploadingAvatar = ref(false);

const profileForm = ref({
  realName: '',
  username: '',
  tutorNo: '',
  title: '',
  researchField: '',
  phone: '',
  email: '',
  avatar: ''
});

const avatarUrl = computed(() => {
  if (selectedAvatar.value) {
    return URL.createObjectURL(selectedAvatar.value)
  }
  if (profileForm.value.avatar) {
    return profileForm.value.avatar.startsWith('http')
      ? profileForm.value.avatar
      : `http://localhost:8083${profileForm.value.avatar}`
  }
  return ''
});

const triggerAvatarInput = () => avatarInput.value.click();

const handleAvatarChange = (e) => {
  if (e.target.files.length > 0) {
    const file = e.target.files[0]
    if (!file.type.startsWith('image/')) {
      ElMessage.error('请选择图片文件')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('头像大小不能超过 5MB')
      return
    }
    selectedAvatar.value = file
    uploadAvatar()
  }
};

const uploadAvatar = async () => {
  if (!selectedAvatar.value) return
  const userId = userStore.userInfo?.userId
  if (!userId) return

  uploadingAvatar.value = true
  try {
    const formData = new FormData()
    formData.append('file', selectedAvatar.value)
    const res = await api.post('/tutor-workbench/avatar/upload', formData)
    const avatarPath = res?.filePath || res?.avatar || res
    profileForm.value.avatar = avatarPath
    userStore.updateAvatar(avatarPath)
    ElMessage.success('头像上传成功')
    selectedAvatar.value = null
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
};

const loadProfile = async () => {
  if (!userStore.userInfo) return;

  profileForm.value.realName = userStore.userInfo.realName || '';
  profileForm.value.username = userStore.userInfo.username || '';
  profileForm.value.phone = userStore.userInfo.phone || '';
  profileForm.value.email = userStore.userInfo.email || '';
  profileForm.value.avatar = userStore.userInfo.avatar || '';
  profileForm.value.tutorNo = 'T' + userStore.userInfo.userId;
  profileForm.value.title = '副教授';
  profileForm.value.researchField = '人工智能、机器学习';
};

const saveProfile = async () => {
  try {
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

.avatar-section {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-preview {
  flex-shrink: 0;
  background: #409EFF;
  font-size: 32px;
  color: #fff;
}

.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.avatar-tip {
  font-size: 12px;
  color: #909399;
}
</style>
