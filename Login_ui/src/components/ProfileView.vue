<template>
  <div class="profile-container">
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar">👤</div>
        <h2>个人信息</h2>
      </div>

      <div class="profile-content">
        <!-- 用户信息显示 -->
        <div class="info-section">
          <div class="info-item">
            <label>用户名</label>
            <div class="info-value">{{ userInfo.username }}</div>
          </div>

          <div class="info-item">
            <label>学号</label>
            <div class="info-value">{{ userInfo.studentId }}</div>
          </div>

          <div class="info-item">
            <label>邮箱</label>
            <div class="info-value">{{ userInfo.email }}</div>
          </div>

          <p class="info-note">* 邮箱不可修改</p>
        </div>

        <!-- 操作按钮 -->
        <div class="actions-section">
          <button class="action-btn change-password" @click="showChangePasswordDialog = true">
            🔒 修改密码
          </button>
          <button class="action-btn logout" @click="handleLogout">
            🚪 退出登录
          </button>
        </div>
      </div>
    </div>

    <!-- 修改密码对话框 -->
    <div v-if="showChangePasswordDialog" class="modal" @click.self="showChangePasswordDialog = false">
      <div class="modal-content">
        <h3>修改密码</h3>
        
        <div class="form-group">
          <label>当前密码</label>
          <input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入当前密码"
          />
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
          />
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入新密码"
          />
        </div>

        <div v-if="passwordError" class="error-message">
          {{ passwordError }}
        </div>

        <div class="modal-actions">
          <button @click="closePasswordDialog">取消</button>
          <button class="primary" @click="handleChangePassword">确认修改</button>
        </div>
      </div>
    </div>

    <!-- 退出登录确认对话框 -->
    <div v-if="showLogoutDialog" class="modal" @click.self="showLogoutDialog = false">
      <div class="modal-content">
        <h3>确认退出</h3>
        <p>确定要退出登录吗?</p>
        <div class="modal-actions">
          <button @click="showLogoutDialog = false">取消</button>
          <button class="primary danger" @click="confirmLogout">确认退出</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const userInfo = ref({
  username: '',
  studentId: '',
  email: ''
})

// 对话框显示状态
const showChangePasswordDialog = ref(false)
const showLogoutDialog = ref(false)

// 修改密码表单
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordError = ref('')

/**
 * API: 获取用户信息
 * GET /api/user/profile
 * 输出: {
 *   code: number,
 *   data: {
 *     username: string,
 *     studentId: string,
 *     email: string,
 *     createdAt: string
 *   }
 * }
 */
const loadUserInfo = async () => {
  try {
    // const response = await fetch('/api/user/profile')
    // const result = await response.json()
    // userInfo.value = result.data
    
    // 模拟数据
    userInfo.value = {
      username: '张三',
      studentId: '2021001234',
      email: 'zhangsan@example.com'
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

/**
 * API: 修改密码
 * POST /api/user/change-password
 * 输入: {
 *   oldPassword: string,
 *   newPassword: string
 * }
 * 输出: {
 *   code: number,
 *   message: string
 * }
 */
const handleChangePassword = async () => {
  passwordError.value = ''

  // 表单验证
  if (!passwordForm.value.oldPassword) {
    passwordError.value = '请输入当前密码'
    return
  }

  if (!passwordForm.value.newPassword) {
    passwordError.value = '请输入新密码'
    return
  }

  if (passwordForm.value.newPassword.length < 6) {
    passwordError.value = '新密码长度不能少于6位'
    return
  }

  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }

  try {
    // const response = await fetch('/api/user/change-password', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({
    //     oldPassword: passwordForm.value.oldPassword,
    //     newPassword: passwordForm.value.newPassword
    //   })
    // })
    // const result = await response.json()
    
    // if (result.code === 200) {
    //   alert('密码修改成功,请重新登录')
    //   closePasswordDialog()
    //   handleLogout()
    // } else {
    //   passwordError.value = result.message
    // }

    // 模拟成功
    alert('密码修改成功')
    closePasswordDialog()
  } catch (error) {
    console.error('修改密码失败:', error)
    passwordError.value = '修改密码失败,请稍后重试'
  }
}

const closePasswordDialog = () => {
  showChangePasswordDialog.value = false
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordError.value = ''
}

const handleLogout = () => {
  showLogoutDialog.value = true
}

/**
 * API: 退出登录
 * POST /api/auth/logout
 * 输出: {
 *   code: number,
 *   message: string
 * }
 */
const confirmLogout = async () => {
  try {
    // const response = await fetch('/api/auth/logout', { method: 'POST' })
    // const result = await response.json()
    
    // 清除本地token/session
    localStorage.removeItem('token')
    
    // 跳转到登录页
    router.push('/login')
  } catch (error) {
    console.error('退出登录失败:', error)
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.profile-header {
  background: #00bcd4;
  color: white;
  padding: 30px;
  text-align: center;
}

.avatar {
  font-size: 80px;
  margin-bottom: 15px;
}

.profile-header h2 {
  margin: 0;
  font-size: 24px;
}

.profile-content {
  padding: 30px;
}

.info-section {
  margin-bottom: 30px;
}

.info-item {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e0e0e0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.info-value {
  font-size: 18px;
  color: #333;
  font-weight: 500;
}

.info-note {
  color: #999;
  font-size: 12px;
  margin-top: 10px;
}

.actions-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.action-btn {
  padding: 15px;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
}

.change-password {
  background: #00bcd4;
  color: white;
}

.change-password:hover {
  background: #00acc1;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 188, 212, 0.3);
}

.logout {
  background: #f5f5f5;
  color: #666;
}

.logout:hover {
  background: #e0e0e0;
  color: #333;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  min-width: 400px;
  max-width: 90%;
}

.modal-content h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.modal-content p {
  margin: 20px 0;
  color: #666;
  font-size: 16px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: #666;
  font-size: 14px;
  margin-bottom: 8px;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #00bcd4;
}

.error-message {
  padding: 10px;
  background: #ffebee;
  color: #c62828;
  border-radius: 5px;
  font-size: 14px;
  margin-bottom: 15px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.modal-actions button {
  padding: 10px 25px;
  border: 1px solid #e0e0e0;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
}

.modal-actions button:hover {
  background: #f5f5f5;
}

.modal-actions button.primary {
  background: #00bcd4;
  color: white;
  border-color: #00bcd4;
}

.modal-actions button.primary:hover {
  background: #00acc1;
}

.modal-actions button.danger {
  background: #f44336;
  border-color: #f44336;
}

.modal-actions button.danger:hover {
  background: #d32f2f;
}
</style>