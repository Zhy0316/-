<template>
  <div class="register-container">
    <div class="register-form">
      <h2>用户注册</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="0">

        <!-- 通用字段 -->
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名（账号）" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码（至少6位）"
            size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码"
            size="large" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="realName">
          <el-input v-model="form.realName" placeholder="真实姓名" size="large" prefix-icon="Edit" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="手机号" size="large" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="email">
          <el-input v-model="form.email" placeholder="邮箱" size="large" prefix-icon="Message" />
        </el-form-item>

        <!-- 角色选择 -->
        <el-form-item prop="roleKey">
          <el-select v-model="form.roleKey" placeholder="请选择注册角色" size="large" style="width:100%">
            <el-option label="学生" value="student" />
            <el-option label="导师" value="tutor" />
            <el-option label="企业HR" value="hr" />
          </el-select>
        </el-form-item>

        <!-- 学生专属字段 -->
        <template v-if="form.roleKey === 'student'">
          <el-form-item prop="studentNo">
            <el-input v-model="form.studentNo" placeholder="学号" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.college" placeholder="所属学院" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.major" placeholder="专业" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.className" placeholder="班级" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.enrollmentYear" placeholder="入学年份（如2022）"
              size="large" type="number" />
          </el-form-item>
        </template>

        <!-- 导师专属字段 -->
        <template v-if="form.roleKey === 'tutor'">
          <el-form-item prop="tutorNo">
            <el-input v-model="form.tutorNo" placeholder="工号" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.title" placeholder="职称（如教授/副教授）" size="large" />
          </el-form-item>
        </template>

        <!-- 企业HR专属字段 -->
        <template v-if="form.roleKey === 'hr'">
          <el-form-item prop="companyName">
            <el-input v-model="form.companyName" placeholder="企业名称" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.creditCode" placeholder="统一社会信用代码" size="large" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.industry" placeholder="所属行业" size="large" />
          </el-form-item>
          <el-alert type="info" :closable="false" show-icon
            title="企业账号注册后需等待管理员审核，审核通过后方可使用完整功能" />
        </template>

        <el-form-item style="margin-top: 16px">
          <el-button type="primary" size="large" style="width:100%" :loading="loading"
            @click="handleRegister">
            注册
          </el-button>
        </el-form-item>

        <div class="login-link">
          <span>已有账号？</span>
          <el-link type="primary" @click="$router.push('/login')">立即登录</el-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authAPI } from '../services/auth.js'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '', password: '', confirmPassword: '',
  realName: '', phone: '', email: '', roleKey: '',
  // 学生
  studentNo: '', college: '', major: '', className: '', enrollmentYear: '',
  // 导师
  tutorNo: '', title: '',
  // 企业
  companyName: '', creditCode: '', industry: ''
})

const validateConfirmPwd = (rule, value, callback) => {
  if (!value) return callback(new Error('请确认密码'))
  if (value !== form.password) return callback(new Error('两次密码不一致'))
  callback()
}

const rules = {
  username:        [{ required: true, message: '请输入用户名', trigger: 'blur' },
                    { min: 3, max: 20, message: '3~20个字符', trigger: 'blur' }],
  password:        [{ required: true, message: '请输入密码', trigger: 'blur' },
                    { min: 6, message: '至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPwd, trigger: 'blur' }],
  realName:        [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone:           [{ required: true, message: '请输入手机号', trigger: 'blur' },
                    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email:           [{ required: true, message: '请输入邮箱', trigger: 'blur' },
                    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  roleKey:         [{ required: true, message: '请选择角色', trigger: 'change' }],
  studentNo:       [{ required: true, message: '请输入学号', trigger: 'blur' }],
  tutorNo:         [{ required: true, message: '请输入工号', trigger: 'blur' }],
  companyName:     [{ required: true, message: '请输入企业名称', trigger: 'blur' }]
}

const handleRegister = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    // 企业注册走专用接口
    let endpoint = '/auth/register'
    if (form.roleKey === 'hr') endpoint = '/enterprise/register'

    const { confirmPassword, ...submitData } = form
    const res = await authAPI.registerWithEndpoint(endpoint, submitData)

    ElMessage.success(form.roleKey === 'hr'
      ? '企业注册成功，请等待管理员审核'
      : '注册成功，请登录')
    router.push('/login')
  } catch (e) {
    ElMessage.error('注册失败：' + (e.response?.data || e.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 40px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.register-form {
  width: 460px;
  padding: 40px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 25px rgba(0,0,0,.15);
}
.register-form h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
  font-size: 22px;
}
.login-link {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}
</style>
