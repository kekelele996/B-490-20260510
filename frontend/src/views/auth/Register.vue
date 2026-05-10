<template>
  <div class="register-container">
    <div class="register-left">
      <div class="register-content">
        <div class="logo-area">
          <h1 class="title">加入我们</h1>
        </div>
        <p class="slogan">开启您的心理健康之旅</p>
        <ul class="features">
          <li><el-icon><Check /></el-icon> 免费注册体验</li>
          <li><el-icon><Check /></el-icon> 随时随地预约</li>
          <li><el-icon><Check /></el-icon> 专属健康档案</li>
        </ul>
      </div>
      <div class="bg-decoration"></div>
    </div>
    <div class="register-right">
      <el-card class="register-card" shadow="hover">
        <div class="form-header">
          <h2>创建账号</h2>
          <p>填写以下信息完成注册</p>
        </div>
        <el-form :model="form" label-position="top" size="large">
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" autocomplete="off" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password :prefix-icon="Lock" autocomplete="new-password" />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" placeholder="请输入昵称" :prefix-icon="Avatar" />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Iphone" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="form.email" placeholder="请输入邮箱" :prefix-icon="Message" />
          </el-form-item>
          <el-form-item label="邀请码">
            <el-input v-model="form.inviteCode" placeholder="选填:若有邀请码请填写" :prefix-icon="Present" maxlength="20" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">立即注册</el-button>
          </el-form-item>
          <div class="form-footer">
            <span>已有账号？</span>
            <el-link type="primary" @click="$router.push('/login')">去登录</el-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { User, Lock, Avatar, Iphone, Check, Message, Present } from '@element-plus/icons-vue'

const form = ref({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  inviteCode: ''
})
const loading = ref(false)

const router = useRouter()

const handleRegister = async () => {
    if (!form.value.username || !form.value.password || !form.value.nickname) {
    ElMessage.warning('请填写必要信息')
    return
  }
  

  // Regex Validation
  const phoneRegex = /^1[3-9]\d{9}$/
  if (form.value.phone && !phoneRegex.test(form.value.phone)) {
      ElMessage.error('手机号格式不正确')
      return
  }
  
  const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
  if (form.value.email && !emailRegex.test(form.value.email)) {
      ElMessage.error('邮箱格式不正确')
      return
  }

  loading.value = true
  try {
    const res = await axios.post('/api/auth/register', form.value)
    if (res.data.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (error) {
    ElMessage.error('注册失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
}

.register-left {
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

.register-left::after {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url('https://images.unsplash.com/photo-1522202176988-66273c2fd55f?ixlib=rb-1.2.1&auto=format&fit=crop&w=1351&q=80');
  background-size: cover;
  background-position: center;
  opacity: 0.1;
  z-index: 0;
}

.register-content {
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
  white-space: nowrap;
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
  white-space: nowrap;
  width: max-content;
}




.register-right {
  flex: 0.8;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  overflow-y: auto;
}

.register-card {
  width: 440px;
  border-radius: 24px;
  padding: 20px;
  margin: 20px;
  border: none;
  box-shadow: 0 20px 40px rgba(0,0,0,0.05) !important;
}

.form-header {
  text-align: center;
  margin-bottom: 24px;
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
</style>