<template>
  <div class="login-container">
    <div class="login-left">
      <div class="login-content">
        <div class="logo-area">
          <h1 class="title">在线心理咨询</h1>
        </div>
        <p class="slogan">专业 · 贴心 · 隐私 · 守护您的心灵家园</p>
        <ul class="features">
          <li><el-icon><Check /></el-icon> 资深咨询师团队</li>
          <li><el-icon><Check /></el-icon> 严格的隐私保护</li>
          <li><el-icon><Check /></el-icon> 便捷的预约流程</li>
          <li><el-icon><Check /></el-icon> 实时在线沟通</li>
        </ul>
      </div>
      <div class="bg-decoration"></div>
    </div>
    <div class="login-right">
      <el-card class="login-card" shadow="hover">
        <div class="form-header">
          <h2>欢迎登录</h2>
          <p>请输入您的账号和密码</p>
        </div>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" autocomplete="off" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" @keyup.enter="handleLogin" autocomplete="new-password" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">立即登录</el-button>
          </el-form-item>
          <div class="form-footer">
            <span>还没有账号？</span>
            <el-link type="primary" @click="$router.push('/register')">去注册</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Lock, Check } from '@element-plus/icons-vue'

const form = ref({
  username: '',
  password: ''
})
const loading = ref(false)

const router = useRouter()
const userStore = useUserStore()

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const res = await axios.post('/api/auth/login', form.value)
    if (res.data.code === 200) {
      const { user, token } = res.data.data
      userStore.setUser(user, token)
      ElMessage.success('登录成功')
      const role = user.role
      if (role === 'ADMIN') router.push('/admin')
      else if (role === 'COUNSELOR') router.push('/counselor')
      else router.push('/user')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('登录失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.login-left {
  flex: 1.2;
  background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.login-left::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url('https://images.unsplash.com/photo-1506126613408-eca07ce68773?ixlib=rb-1.2.1&auto=format&fit=crop&w=1350&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.1;
  z-index: 0;
}

.login-content {
  position: relative;
  z-index: 1;
  max-width: 500px;
  text-align: center;
}

.title {
  font-size: 48px;
  font-weight: 800;
  margin-bottom: 20px;
  letter-spacing: 2px;
  text-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.slogan {
  font-size: 20px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.features {
  list-style: none;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 15px;
  align-items: center; /* Centered capsules */
}

.features li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  background: rgba(255,255,255,0.1);
  padding: 10px 24px;
  border-radius: 30px;
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255,255,255,0.2);
  white-space: nowrap !important;
  width: fit-content;
}




.login-right {
  flex: 0.8;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.login-card {
  width: 420px;
  border-radius: 24px;
  padding: 20px;
  border: none;
  box-shadow: 0 20px 40px rgba(0,0,0,0.05) !important;
}

.form-header {
  text-align: center;
  margin-bottom: 30px;
}

.form-header h2 {
  font-size: 28px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 8px;
}

.form-header p {
  color: #718096;
}

.submit-btn {
  width: 100%;
  height: 54px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 12px;
  margin-top: 10px;
  background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(67, 97, 238, 0.4);
  filter: brightness(1.1);
}

.form-footer {
  text-align: center;
  margin-top: 30px;
  color: #64748b;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.form-footer :deep(.el-link) {
  font-weight: 600;
  font-size: 14px;
  transition: all 0.3s;
}

.form-footer :deep(.el-link:hover) {
  transform: translateX(2px);
}

/* Responsive */
@media (max-width: 768px) {
  .login-left {
    display: none;
  }
}
</style>