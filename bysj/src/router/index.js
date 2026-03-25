import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login',    name: 'Login',    component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue') },

  // ==================== 学生端 ====================
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../views/student/StudentDashboard.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      { path: '',        redirect: '/student/home' },
      { path: 'home',    name: 'StudentHome',       component: () => import('../views/student/StudentDashboard.vue') },
      { path: 'profile', name: 'StudentProfile',    component: () => import('../views/student/Profile.vue') },
      { path: 'academic',name: 'StudentAcademic',   component: () => import('../views/student/Academic.vue') },
      { path: 'record',  name: 'StudentRecord',     component: () => import('../views/student/Record.vue') },
      { path: 'portfolio',name: 'StudentPortfolio', component: () => import('../views/student/Portfolio.vue') },
      { path: 'growth',  name: 'StudentGrowth',     component: () => import('../views/student/GrowthScore.vue') },
      { path: 'recruitment', name: 'StudentRecruitment', component: () => import('../views/student/Recruitment.vue') },
    ]
  },

  // ==================== 导师端 ====================
  {
    path: '/tutor',
    name: 'TutorLayout',
    component: () => import('../views/tutor/TutorDashboard.vue'),
    meta: { requiresAuth: true, role: 'tutor' },
    children: [
      { path: '',        redirect: '/tutor/home' },
      { path: 'home',    name: 'TutorHome',         component: () => import('../views/tutor/Home.vue') },
      { path: 'students',name: 'TutorStudents',     component: () => import('../views/tutor/Students.vue') },
      { path: 'student/:id', name: 'StudentDetail', component: () => import('../views/tutor/StudentDetail.vue') },
      { path: 'pending', name: 'TutorPending',      component: () => import('../views/tutor/Pending.vue') },
      { path: 'profile', name: 'TutorProfile',      component: () => import('../views/tutor/Profile.vue') },
      { path: 'comments',name: 'TutorComments',     component: () => import('../views/tutor/CommentList.vue') },
    ]
  },

  // ==================== 企业HR端 ====================
  {
    path: '/hr',
    name: 'HrLayout',
    component: () => import('../views/hr/HrDashboard.vue'),
    meta: { requiresAuth: true, role: 'hr' },
    children: [
      { path: '',           redirect: '/hr/home' },
      { path: 'home',       name: 'HrHome',          component: () => import('../views/hr/HrDashboard.vue') },
      { path: 'profile',    name: 'HrProfile',       component: () => import('../views/hr/HrProfile.vue') },
      { path: 'recruitment',name: 'HrRecruitment',   component: () => import('../views/hr/RecruitmentManage.vue') },
      { path: 'students',   name: 'HrStudents',      component: () => import('../views/hr/StudentBrowse.vue') },
      { path: 'applications',name: 'HrApplications', component: () => import('../views/hr/ApplicationList.vue') },
    ]
  },

  // ==================== 管理员端 ====================
  {
    path: '/admin',
    name: 'AdminLayout',
    component: () => import('../views/admin/AdminDashboard.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    children: [
      { path: '',          redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard',   component: () => import('../views/admin/AdminDashboard.vue') },
      { path: 'users',     name: 'AdminUsers',       component: () => import('../views/admin/UserManage.vue') },
      { path: 'enterprise',name: 'EnterpriseAudit',  component: () => import('../views/admin/EnterpriseAudit.vue') },
      { path: 'ranking',   name: 'GrowthRanking',    component: () => import('../views/admin/GrowthRanking.vue') },
      { path: 'report',    name: 'ReportExport',     component: () => import('../views/admin/ReportExport.vue') },
    ]
  },

  // 兼容旧路由
  {
    path: '/dashboard',
    redirect: '/student'
  },

  // 404
  { path: '/:pathMatch(.*)*', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳转登录页，角色不匹配跳转对应首页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  // 角色路由保护
  if (to.meta.role && userInfo && userInfo.roleKey !== to.meta.role) {
    return next(getRoleHome(userInfo.roleKey))
  }

  next()
})

/** 根据角色返回首页路径 */
function getRoleHome(roleKey) {
  const map = { student: '/student', tutor: '/tutor', hr: '/hr', admin: '/admin' }
  return map[roleKey] || '/login'
}

export { getRoleHome }
export default router
