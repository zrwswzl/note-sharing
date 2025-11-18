import { createRouter, createWebHistory } from 'vue-router'
import AuthView from '../views/AuthView.vue'
import MainView from '../views/MainView.vue'

const routes = [
  {
    path: '/',
    redirect: '/login',
  },
  {
    path: '/login',
    name: 'Login',
    component: AuthView,
  },
  {
    path: '/register',
    name: 'Register',
    component: AuthView,
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: AuthView,
  },
  {
    path: '/main',
    name: 'Main',
    component: MainView,
    meta: { requiresAuth: true }  // 需要登录才能访问
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    // 需要登录但未登录,跳转到登录页
    next('/login')
  } else if ((to.path === '/login' || to.path === '/register') && token) {
    // 已登录用户访问登录/注册页,跳转到主页
    next('/main')
  } else {
    next()
  }
})

export default router