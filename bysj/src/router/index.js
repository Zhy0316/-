import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Dashboard from '../views/Dashboard.vue'

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    children: [
      {
        path: '',
        name: 'Welcome',
        component: () => import('../views/Welcome.vue')
      },
      {
        path: 'student/profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue')
      },
      {
        path: 'student/academic',
        name: 'StudentAcademic',
        component: () => import('../views/student/Academic.vue')
      },
      {
        path: 'student/record',
        name: 'StudentRecord',
        component: () => import('../views/student/Record.vue')
      }
    ]
  },
  {
    path: '/tutor',
    name: 'TutorDashboard',
    component: () => import('../views/tutor/TutorDashboard.vue'),
    children: [
      {
        path: '',
        redirect: '/tutor/home'
      },
      {
        path: 'home',
        name: 'TutorHome',
        component: () => import('../views/tutor/Home.vue')
      },
      {
        path: 'students',
        name: 'TutorStudents',
        component: () => import('../views/tutor/Students.vue')
      },
      {
        path: 'pending',
        name: 'TutorPending',
        component: () => import('../views/tutor/Pending.vue')
      },
      {
        path: 'profile',
        name: 'TutorProfile',
        component: () => import('../views/tutor/Profile.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router