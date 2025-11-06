import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '../views/AuthView.vue'
import UserDashboard from '../views/UserDashboard.vue'
const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: AuthView
  },
  {
    path: '/register',
    name: 'Register',
    component: AuthView
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: AuthView
  },
  {
    path: '/user',
    name: 'UserDashboard',
    component: UserDashboard,
    // 实际开发中，这里通常会加一个路由守卫(beforeEnter)判断是否已登录
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router