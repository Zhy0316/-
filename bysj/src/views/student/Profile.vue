<template>
  <div class="profile-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>个人基本信息</span>
          <el-button type="primary" @click="saveProfile" :loading="loading">保存修改</el-button>
        </div>
      </template>
      <el-form :model="form" label-width="120px" :rules="rules" ref="formRef">
        <!-- 基本账户信息 (不可修改或部分可修改) -->
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="form.username" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
                <el-option label="男" value="M"></el-option>
                <el-option label="女" value="F"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="form.phone"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电子邮箱" prop="email">
              <el-input v-model="form.email"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">学籍信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="学院" prop="college">
              <el-input v-model="form.college"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="专业" prop="major">
              <el-input v-model="form.major"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级" prop="className">
              <el-input v-model="form.className"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="8">
             <el-form-item label="入学年份">
               <el-date-picker
                 v-model="form.enrollmentYear"
                 type="year"
                 value-format="YYYY"
                 placeholder="选择年份"
                 style="width: 100%"
               />
             </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">指导教师</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="选择指导教师">
              <el-select v-model="selectedTutorId" placeholder="请选择指导教师" style="width: 100%" @change="handleTutorChange">
                <el-option 
                  v-for="tutor in tutors" 
                  :key="tutor.userId" 
                  :label="tutor.realName + ' (' + tutor.title + ')'" 
                  :value="tutor.userId" 
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="绑定状态">
              <el-tag v-if="currentRelation" :type="currentRelation.status === 1 ? 'success' : 'warning'">
                {{ currentRelation.status === 1 ? '已绑定' : '待审核' }}
              </el-tag>
              <el-tag v-else type="info">未绑定</el-tag>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">详细信息</el-divider>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="政治面貌" prop="politicalStatus">
              <el-select v-model="form.politicalStatus" placeholder="请选择政治面貌" style="width: 100%">
                <el-option v-for="item in politicalOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="民族" prop="nation">
              <el-select v-model="form.nation" filterable placeholder="请选择民族" style="width: 100%">
                <el-option v-for="item in nationOptions" :key="item" :label="item" :value="item" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="籍贯" prop="nativePlace">
              <el-input v-model="form.nativePlace" placeholder="请输入籍贯 (如: xx省xx市)"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出生日期" prop="birthDate">
              <el-date-picker
                v-model="form.birthDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">附加材料</el-divider>

        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="个人简历" prop="resumeFile">
              <div style="display: flex; flex-direction: column; gap: 10px;">
                <input type="file" ref="fileInput" @change="handleFileChange" style="display: none" />
                <div>
                   <el-button type="success" size="small" @click.prevent="triggerFileInput">选择文件并上传</el-button>
                   <span v-if="selectedFile" style="margin-left: 15px; font-size: 13px; color: #666;">已选择: {{ selectedFile.name }} (将在保存时上传)</span>
                </div>
                <div v-if="form.resumeFile" style="margin-top: 10px;">
                  <a :href="getAttachmentUrl(form.resumeFile)" target="_blank" style="color: #3498db; text-decoration: none; font-size: 14px;">
                    📎 预览/下载当前简历
                  </a>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
// Make sure to import from correct path, adjust relative path as needed
import { studentAPI } from '../../services/student.js'
import { useUserStore } from '../../stores/user.js'
import axios from 'axios'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const fileInput = ref(null)
const selectedFile = ref(null)

// 指导教师相关数据
const tutors = ref([])
const selectedTutorId = ref(null)
const currentRelation = ref(null)

const form = reactive({
  userId: null,
  username: '',
  realName: '',
  studentNo: '',
  gender: '',
  phone: '',
  email: '',
  resumeFile: '',
  college: '',
  major: '',
  className: '',
  enrollmentYear: null,
  politicalStatus: '',
  nation: '',
  nativePlace: '',
  birthDate: ''
})

const politicalOptions = [
  '中共党员',
  '中共预备党员',
  '共青团员',
  '群众',
  '其它'
]

const nationOptions = [
  '汉族', '壮族', '回族', '满族', '维吾尔族', '苗族', '彝族', '土家族', '藏族', '蒙古族',
  '侗族', '布依族', '瑶族', '白族', '朝鲜族', '哈尼族', '黎族', '哈萨克族', '傣族', '畲族',
  '傈僳族', '东乡族', '仡佬族', '拉祜族', '佤族', '水族', '纳西族', '羌族', '土族', '仫佬族',
  '锡伯族', '柯尔克孜族', '景颇族', '达斡尔族', '撒拉族', '布朗族', '毛南族', '塔吉克族',
  '普米族', '阿昌族', '怒族', '鄂温克族', '京族', '基诺族', '德昂族', '保安族', '俄罗斯族',
  '裕固族', '乌孜别克族', '门巴族', '鄂伦春族', '独龙族', '塔塔尔族', '赫哲族', '珞巴族'
]

const triggerFileInput = () => {
  fileInput.value.click();
}

const handleFileChange = (e) => {
  if (e.target.files.length > 0) {
    selectedFile.value = e.target.files[0];
  }
}

const getAttachmentUrl = (path) => {
  if (!path) return '#';
  return import.meta.env.VITE_API_URL ? `${import.meta.env.VITE_API_URL}${path}` : `http://localhost:8080${path}`;
};

// 加载教师列表
const loadTutors = async () => {
  try {
    // 如果学生已经填写了学院和专业，传递这些信息进行过滤
    const params = {};
    if (form.college && form.major) {
      params.college = form.college;
      params.major = form.major;
    }
    
    const response = await studentAPI.getTutors(params);
    tutors.value = response.data || [];
  } catch (error) {
    console.error('加载教师列表失败:', error);
    ElMessage.error('加载教师列表失败');
  }
};

// 加载学生的绑定关系
const loadStudentRelations = async () => {
  if (!userStore.userInfo?.userId) return;
  
  try {
    const response = await studentAPI.getStudentRelations(userStore.userInfo.userId);
    const relations = response.data || [];
    if (relations.length > 0) {
      currentRelation.value = relations[0];
      selectedTutorId.value = currentRelation.value.tutorId;
    }
  } catch (error) {
    console.error('加载绑定关系失败:', error);
  }
};

// 处理教师选择变化
const handleTutorChange = async () => {
  if (!selectedTutorId.value || !userStore.userInfo?.userId) return;
  
  try {
    await studentAPI.applyForTutor(userStore.userInfo.userId, selectedTutorId.value);
    ElMessage.success('申请提交成功，请等待教师审核');
    // 重新加载绑定关系
    await loadStudentRelations();
  } catch (error) {
    console.error('申请绑定失败:', error);
    ElMessage.error('申请绑定失败，可能已经存在绑定关系');
  }
};

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  major: [{ required: true, message: '请输入专业', trigger: 'blur' }],
  college: [{ required: true, message: '请输入学院', trigger: 'blur' }],
}

const loadProfile = async () => {
  // Assuming user store has userId. If not, logic might differ.
  // In a real app userStore.userInfo should maintain current logged in user state
  const userId = userStore.userInfo?.userId || userStore.userInfo?.id
  
  if (!userId) {
    ElMessage.error('无法获取用户信息，请重新登录')
    return
  }

  try {
    const res = await studentAPI.getProfile(userId)
    // Merge response into form
    Object.assign(form, res)
  } catch (error) {
    console.error(error)
    ElMessage.error('加载个人信息失败')
  }
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const userId = userStore.userInfo?.userId || userStore.userInfo?.id
        
        // Handle file upload first if strictly selected
        if (selectedFile.value) {
          const fileData = new FormData();
          fileData.append('file', selectedFile.value);
          const uploadRes = await axios.post('/student/upload-resume', fileData, {
            headers: { 'Content-Type': 'multipart/form-data' },
            baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api'
          });
          form.resumeFile = uploadRes.data;
        }

        await studentAPI.updateProfile(userId, form)
        ElMessage.success('保存成功')
        selectedFile.value = null;
      } catch (error) {
        ElMessage.error('保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(async () => {
  await loadProfile()
  await loadTutors()
  await loadStudentRelations()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
