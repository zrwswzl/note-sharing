<template>
  <div class="auth-container">
    <transition name="fade" mode="out-in">
      <component :is="currentComponent" @change-page="handlePageChange" />
    </transition>
  </div>
</template>

<script>
import LoginPage from '../components/LoginPage.vue'
import RegisterPage from '../components/RegisterPage.vue'
import ForgotPassword from '../components/ForgotPassword.vue'

export default {
  name: 'AuthView',
  components: {
    LoginPage,
    RegisterPage,
    ForgotPassword
  },
  computed: {
    currentComponent() {
      const routeName = this.$route.name
      if (routeName === 'Register') return 'RegisterPage'
      if (routeName === 'ForgotPassword') return 'ForgotPassword'
      return 'LoginPage'
    }
  },
  methods: {
    handlePageChange(page) {
      this.$router.push(`/${page}`)
    }
  }
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  width: 100%;
  /* max-width: 450px;
  margin: 0 auto; */
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}


</style>