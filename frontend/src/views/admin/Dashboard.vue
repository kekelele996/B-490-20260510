<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside-menu">
      <div class="logo-box">
        <span class="logo-text">管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="el-menu-vertical"
        @select="handleMenuSelect"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#fff"
      >
        <el-menu-item index="overview">
          <el-icon><DataLine /></el-icon>
          <span>系统概览</span>
        </el-menu-item>
        <el-menu-item index="users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="appointments">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="withdrawals">
          <el-icon><Wallet /></el-icon>
          <span>提现管理</span>
        </el-menu-item>
        <el-menu-item index="settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>

      </el-menu>
    </el-aside>
    
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuTitle[activeMenu] }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :src="getAvatar(userStore.user?.avatar)" :icon="UserFilled" />
              <span class="username">{{ userStore.user?.nickname || '管理员' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <!-- Overview Section -->
        <div v-if="activeMenu === 'overview'" class="overview-section">
          <el-row :gutter="20" class="data-cards">
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card blue">
                <div class="stat-content">
                  <div class="stat-info">
                    <div class="stat-label">总用户数</div>
                    <div class="stat-value">{{ users.length }}</div>
                  </div>
                  <el-icon class="stat-icon"><User /></el-icon>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card green">
                <div class="stat-content">
                  <div class="stat-info">
                    <div class="stat-label">咨询师人数</div>
                    <div class="stat-value">{{ counselors.length }}</div>
                  </div>
                  <el-icon class="stat-icon"><User /></el-icon>
                </div>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card shadow="hover" class="stat-card orange">
                <div class="stat-content">
                  <div class="stat-info">
                    <div class="stat-label">总预约数</div>
                    <div class="stat-value">{{ appointments.length }}</div>
                  </div>
                  <el-icon class="stat-icon"><User /></el-icon>
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <el-card shadow="hover" class="chart-card">
                <template #header>
                  <div class="chart-header">
                    <span><el-icon><PieChart /></el-icon> 用户角色分布</span>
                  </div>
                </template>
                <div ref="roleChartRef" style="height: 350px;"></div>
              </el-card>
            </el-col>
            <el-col :span="12">
              <el-card shadow="hover" class="chart-card">
                <template #header>
                  <div class="chart-header">
                    <span><el-icon><Histogram /></el-icon> 预约状态分布</span>
                  </div>
                </template>
                <div ref="statusChartRef" style="height: 350px;"></div>
              </el-card>
            </el-col>
          </el-row>
        </div>

        <!-- Users Management -->
        <div v-if="activeMenu === 'users'">
          <el-card shadow="hover" class="section-card">
            <div class="section-header">
              <h3><el-icon><User /></el-icon> 用户管理</h3>
              <el-button type="primary" :icon="Plus" @click="openCreateUser">新增用户</el-button>
            </div>
            <el-table :data="users" stripe style="width: 100%">
              <el-table-column align="center" prop="id" label="ID" width="80" />
              <el-table-column align="center" label="头像" width="80">
                <template #default="scope">
                  <el-avatar :size="36" :src="getAvatar(scope.row.avatar)" />
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="username" label="用户名" />
              <el-table-column align="center" prop="nickname" label="昵称" />
              <el-table-column align="center" prop="role" label="角色">
                <template #default="scope">
                  <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : (scope.row.role === 'COUNSELOR' ? 'warning' : 'info')">{{ formatRole(scope.row.role) }}</el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="phone" label="手机" />
              <el-table-column align="center" label="操作" width="180" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <el-button size="small" :icon="Edit" @click="openEditUser(scope.row)" link type="primary">编辑</el-button>
                    <el-button size="small" type="danger" :icon="Delete" @click="deleteUser(scope.row.id)" link>删除</el-button>
                  </div>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- Appointments Management -->
        <div v-if="activeMenu === 'appointments'">
          <el-card shadow="hover" class="section-card">
            <div class="section-header">
              <h3><el-icon><Calendar /></el-icon> 预约管理</h3>
              <el-button type="primary" :icon="Plus" @click="openCreateAppointment">新增预约</el-button>
            </div>
            <el-table :data="appointments" stripe style="width: 100%">
              <el-table-column align="center" prop="id" label="ID" width="80" />
              <el-table-column align="center" label="用户">
                <template #default="scope">
                  {{ scope.row.user?.nickname || '未知用户' }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="咨询师">
                <template #default="scope">
                  {{ scope.row.counselor?.user?.nickname || '未知咨询师' }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="appointmentTime" label="预约时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.appointmentTime) }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="rating" label="评分" width="160">
                <template #default="scope">
                  <el-rate v-model="scope.row.rating" disabled v-if="scope.row.rating" />
                  <span v-else>-</span>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="操作" width="180" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <el-button size="small" :icon="Edit" @click="openEditAppointment(scope.row)" link type="primary">编辑</el-button>
                    <el-button size="small" type="danger" @click="deleteAppointment(scope.row.id)" link :icon="Delete">删除</el-button>
                  </div>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- Withdrawals Management -->
        <div v-if="activeMenu === 'withdrawals'">
          <el-card shadow="hover" class="section-card">
            <div class="section-header">
              <h3><el-icon><User /></el-icon> 提现管理</h3>
            </div>
            <el-table :data="withdrawals" stripe style="width: 100%">
              <el-table-column align="center" prop="id" label="ID" width="80" />
              <el-table-column align="center" prop="counselor.user.nickname" label="咨询师" />
              <el-table-column align="center" prop="amount" label="金额">
                <template #default="scope">
                  <span style="color: #f56c6c; font-weight: bold;">¥{{ scope.row.amount }}</span>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="requestTime" label="申请时间">
                <template #default="scope">
                  {{ formatDateTime(scope.row.requestTime) }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="status" label="状态">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="操作" width="180" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <template v-if="scope.row.status === 'PENDING'">
                      <el-button size="small" type="success" @click="approveWithdrawal(scope.row.id)" link :icon="Check">通过</el-button>
                      <el-button size="small" type="danger" @click="rejectWithdrawal(scope.row.id)" link :icon="Close">拒绝</el-button>
                    </template>
                    <span v-else>-</span>
                  </div>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- System Settings -->
        <div v-if="activeMenu === 'settings'">
          <el-card shadow="hover" class="section-card">
            <div class="section-header">
              <h3><el-icon><Setting /></el-icon> 系统设置</h3>
            </div>
            <el-alert title="配置项 invite_rebate_amount 用于控制老带新奖励金额,单位:元" type="info" :closable="false" style="margin-bottom: 20px;" />
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card shadow="hover">
                  <template #header>
                    <div class="setting-card-header">
                      <span>邀请返现金额</span>
                      <el-tag type="warning" size="small">核心参数</el-tag>
                    </div>
                  </template>
                  <el-form label-width="100px">
                    <el-form-item label="返现金额">
                      <el-input-number v-model="inviteRebateAmount" :min="0" :step="10" :precision="2" style="width: 100%" />
                    </el-form-item>
                    <el-form-item>
                      <el-button type="primary" @click="saveInviteRebate" :icon="Check" :loading="savingSetting">保存设置</el-button>
                    </el-form-item>
                  </el-form>
                </el-card>
              </el-col>
            </el-row>
          </el-card>
        </div>

      </el-main>
    </el-container>
    
    <!-- Dialogs -->


    <el-dialog v-model="userDialogVisible" :title="isEditUser ? '编辑用户' : '新增用户'" width="560px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名" v-if="!isEditUser">
          <el-input v-model="userForm.username" autocomplete="new-username" />
        </el-form-item>
        <el-form-item label="头像">
             <el-upload
                 class="avatar-uploader"
                 action="/api/files/upload"
                 :show-file-list="false"
                 :on-success="handleAvatarSuccess"
                 :before-upload="beforeAvatarUpload"
             >
                 <img v-if="userForm.avatar" :src="userForm.avatar" class="avatar" />
                 <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
             </el-upload>
        </el-form-item>
        <el-form-item label="密码" v-if="!isEditUser">
             <el-input v-model="userForm.password" type="password" placeholder="请输入密码" autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="密码" v-else>
             <el-input v-model="userForm.password" type="password" placeholder="留空不修改" autocomplete="new-password" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="userForm.role" style="width: 100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="咨询师" value="COUNSELOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机">
            <el-input v-model="userForm.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
            <el-input v-model="userForm.email" />
        </el-form-item>
         <el-form-item label="性别">
            <el-select v-model="userForm.gender" style="width: 100%">
                <el-option label="男" value="Male" />
                <el-option label="女" value="Female" />
            </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveUser">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="appointmentDialogVisible" :title="appointmentForm.id ? '编辑预约' : '新增预约'" width="500px">
        <el-form :model="appointmentForm" label-position="top">
            <el-form-item label="用户" v-if="!appointmentForm.id">
                 <el-select v-model="appointmentForm.userId" filterable placeholder="选择用户" style="width: 100%">
                     <el-option v-for="u in users" :key="u.id" :label="u.nickname || u.username" :value="u.id" />
                 </el-select>
            </el-form-item>
            <el-form-item label="咨询师" v-if="!appointmentForm.id">
                 <el-select v-model="appointmentForm.counselorId" filterable placeholder="选择咨询师" style="width: 100%">
                     <el-option v-for="c in counselors" :key="c.id" :label="c.user.nickname" :value="c.id" />
                 </el-select>
            </el-form-item>
            <el-form-item label="1. 选择预约日期">
                 <el-date-picker 
                    v-model="bookingDate" 
                    type="date" 
                    value-format="YYYY-MM-DD" 
                    placeholder="选择日期" 
                    :disabled-date="disabledDate"
                    style="width: 100%" 
                    @change="handleDateChange"
                 />
            </el-form-item>
            <el-form-item label="2. 选择时间段">
                 <el-select v-model="bookingTimeSlot" placeholder="请先选择日期" :disabled="!bookingDate" style="width: 100%">
                     <el-option v-for="slot in filteredTimeSlots" :key="slot.value" :label="slot.label" :value="slot.value" />
                 </el-select>
                 <div v-if="filteredTimeSlots.length === 0 && bookingDate" class="no-slot-tip">
                    <span style="color: #f56c6c; font-size: 12px;">该日期暂无可用时段 (或已过时)</span>
                 </div>
            </el-form-item>
            <el-form-item label="状态">
                <el-select v-model="appointmentForm.status" style="width: 100%">
                     <el-option v-for="(label, value) in statusMap" :key="value" :label="label" :value="value" />
                </el-select>
            </el-form-item>
        </el-form>
        <template #footer>
            <span class="dialog-footer">
                <el-button @click="appointmentDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="saveAppointment">保存</el-button>
            </span>
        </template>
    </el-dialog>

    <el-dialog v-model="counselorDialogVisible" title="编辑咨询师信息" width="500px">
      <el-form :model="counselorForm" label-position="top">
        <el-form-item label="咨询师">
          <span style="font-weight: bold">{{ counselorForm.user?.nickname }}</span>
        </el-form-item>
        <el-form-item label="资质证明">
          <el-upload
            ref="adminQualificationUploadRef"
            class="hidden-upload"
            action="/api/files/upload"
            :show-file-list="false"
            :on-success="handleQualificationSuccess"
            :before-upload="beforeQualificationUpload"
          />
          <div class="qualification-uploader-admin" @click="triggerAdminQualificationUpload">
            <img v-if="counselorForm.qualification" :src="resolveAssetUrl(counselorForm.qualification)" class="qualification-preview" />
            <div v-else class="qualification-placeholder">点击上传资质证明</div>
          </div>
          <div class="form-tip">点击图片区域上传或更换资质证明（JPG/PNG，大小不超过 5MB）</div>
        </el-form-item>
        <el-form-item label="擅长领域">
          <el-input v-model="counselorForm.expertise" placeholder="请输入擅长领域" />
        </el-form-item>
        <el-form-item label="咨询费用">
          <el-input-number v-model="counselorForm.fee" :min="0" :precision="2" :step="10" style="width: 100%" />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input v-model="counselorForm.introduction" type="textarea" :rows="3" placeholder="请输入咨询师简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="counselorDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveCounselor">保存修改</el-button>
        </span>
      </template>
    </el-dialog>

  </el-container>
</template>

<script setup>
import { ref, onMounted, nextTick, watch, computed } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { formatDateTime } from '../../utils/format'
import { 
    DataLine, User, Avatar, Calendar, Wallet,
    UserFilled, ArrowDown, Plus, Edit, Delete,
    Check, Close, PieChart, Histogram, Setting
} from '@element-plus/icons-vue'

import { getAvatar } from '../../utils/avatar'
import { resolveAssetUrl } from '../../utils/asset'
import * as echarts from 'echarts'

const userStore = useUserStore()
const router = useRouter()
const activeMenu = ref('overview')

const menuTitle = {
    'overview': '系统概览',
    'users': '用户管理',
    'appointments': '预约管理',
    'withdrawals': '提现管理',
    'settings': '系统设置'
}

const users = ref([])
const counselors = ref([])
const appointments = ref([])
const withdrawals = ref([])

const inviteRebateAmount = ref(0)
const savingSetting = ref(false)


const userDialogVisible = ref(false)
const userForm = ref({ id: null, username: '', nickname: '', role: 'USER', password: '', phone: '', email: '', gender: '' })
const isEditUser = ref(false)

const handleAvatarSuccess = (response, uploadFile) => {
    if (response.code === 200) {
        userForm.value.avatar = response.data
        // If editing self, sync with header immediately for real-time preview
        if (userForm.value.id === userStore.user?.id) {
            userStore.setUser({ ...userStore.user, avatar: response.data })
        }
    } else {
        ElNotification.error(response.message)
    }
}

const beforeAvatarUpload = (rawFile) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElNotification.error('头像必须是 JPG 或 PNG 格式!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 2) {
        ElNotification.error('头像大小不能超过 2MB!')
        return false
    }
    return true
}

const appointmentDialogVisible = ref(false)
const appointmentForm = ref({ id: null, appointmentTime: '', status: 'PENDING' })
const bookingDate = ref('')
const bookingTimeSlot = ref('')
const availableTimeSlots = ref([])
const availableDates = ref(new Set())

const loadAvailableSlots = async (counselorId) => {
    if (!counselorId) return
    availableTimeSlots.value = []
    availableDates.value = new Set()
    try {
        const res = await axios.get(`/api/timeslots/counselor/${counselorId}/available`)
        availableTimeSlots.value = res.data.data || []
        
        const dates = new Set()
        availableTimeSlots.value.forEach(slot => {
             const date = slot.startTime.replace('T', ' ').split(' ')[0]
             dates.add(date)
        })
        availableDates.value = dates
        
        // If editing, force add current appointment date to available set so it's not disabled
        if (appointmentForm.value.id && appointmentForm.value.appointmentTime) {
            const currentDate = appointmentForm.value.appointmentTime.split(' ')[0]
            availableDates.value.add(currentDate)
        }
    } catch (e) {
        ElNotification.error('获取可用时段失败')
    }
}

const disabledDate = (time) => {
    if (time.getTime() < Date.now() - 8.64e7) return true
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`
    return !availableDates.value.has(dateStr)
}

const filteredTimeSlots = computed(() => {
    if (!bookingDate.value) return []
    
    let slots = availableTimeSlots.value.filter(slot => {
        const timeStr = slot.startTime.replace('T', ' ')
        return timeStr.startsWith(bookingDate.value)
    }).map(slot => {
        const timeStrStart = slot.startTime.replace('T', ' ')
        const timeStrEnd = slot.endTime.replace('T', ' ')
        const start = timeStrStart.split(' ')[1].substring(0, 5)
        const end = timeStrEnd.split(' ')[1].substring(0, 5)
        return {
            label: `${start} - ${end}`,
            value: slot.startTime
        }
    })
    
    // If Editing and the current slot is not in the list (e.g. because it's booked by this appt), add it manually
    if (appointmentForm.value.id && appointmentForm.value.appointmentTime && appointmentForm.value.appointmentTime.startsWith(bookingDate.value)) {
        const currentVal = appointmentForm.value.appointmentTime.replace('T', ' ')
        const exists = slots.some(s => s.value.replace('T', ' ') === currentVal)
        if (!exists) {
            const start = currentVal.split(' ')[1].substring(0, 5)
            // Fake end time +1 hour
            const endH = parseInt(start.split(':')[0]) + 1
            const end = `${String(endH).padStart(2, '0')}:${start.split(':')[1]}`
            slots.push({
                label: `${start} - ${end} (当前)`,
                value: appointmentForm.value.appointmentTime
            })
        }
    }
    
    return slots
})
    
const handleDateChange = () => {
    bookingTimeSlot.value = ''
}

// Watch counselor selection to reload slots
watch(() => appointmentForm.value.counselorId, (newVal) => {
    if (newVal) loadAvailableSlots(newVal)
})

// Sync bookingTimeSlot to appointmentForm
watch(bookingTimeSlot, (newVal) => {
    appointmentForm.value.appointmentTime = newVal
})

const counselorDialogVisible = ref(false)
const counselorForm = ref({ id: null, qualification: '', expertise: '', fee: 0, user: {} })
const adminQualificationUploadRef = ref(null)

const handleQualificationSuccess = (response) => {
    if (response.code === 200) {
        counselorForm.value.qualification = response.data
    } else {
        ElNotification.error(response.message)
    }
}

const beforeQualificationUpload = (rawFile) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElNotification.error('图片必须是 JPG 或 PNG 格式!')
        return false
    } else if (rawFile.size / 1024 / 1024 > 5) {
        ElNotification.error('图片大小不能超过 5MB!')
        return false
    }
    return true
}

const triggerAdminQualificationUpload = () => {
    adminQualificationUploadRef.value?.$el?.querySelector('input')?.click()
}

const openEditCounselor = (row) => {
    counselorForm.value = { ...row }
    counselorDialogVisible.value = true
}

const saveCounselor = async () => {
    try {
        await axios.put(`/api/admin/counselors/${counselorForm.value.id}`, counselorForm.value)
        ElMessage.success('咨询师信息更新成功')
        counselorDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}



const roleMap = { 'USER': '普通用户', 'COUNSELOR': '咨询师', 'ADMIN': '管理员' }
const statusMap = { 'PENDING': '待审核', 'APPROVED': '已通过', 'REJECTED': '已拒绝', 'CONFIRMED': '已确认', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }

const roleChartRef = ref(null)
const statusChartRef = ref(null)

const formatRole = (role) => roleMap[role] || role
const formatStatus = (status) => statusMap[status] || status

const getStatusType = (status) => {
    if (['APPROVED', 'COMPLETED', 'CONFIRMED'].includes(status)) return 'success'
    if (['REJECTED', 'CANCELLED'].includes(status)) return 'danger'
    if (['PENDING'].includes(status)) return 'warning'
    return 'info'
}

const handleMenuSelect = (index) => {
    activeMenu.value = index
    if (index === 'overview') {
        nextTick(() => {
            initCharts()
        })
    }
}

const handleCommand = (command) => {
    if (command === 'logout') {
        logout()
    }
}

const logout = () => {
  userStore.logout()
  router.push('/login')
}

const loadData = async () => {
  try {
    const userRes = await axios.get('/api/admin/users')
    users.value = userRes.data.data
    const counselorRes = await axios.get('/api/admin/counselors')
    counselors.value = counselorRes.data.data
    
    const apptRes = await axios.get('/api/admin/appointments')
    appointments.value = apptRes.data.data

    const wdRes = await axios.get('/api/withdrawals/admin/all')
    withdrawals.value = wdRes.data.data
    
    loadSettings()

    if (activeMenu.value === 'overview') {
        nextTick(() => initCharts())
    }
  } catch (e) {
    ElNotification.error((e.response?.data?.message || '数据加载失败'))
  }
}

const loadSettings = async () => {
  try {
    const res = await axios.get('/api/admin/settings')
    const settings = res.data.data || []
    const rebate = settings.find(s => s.settingKey === 'invite_rebate_amount')
    if (rebate) {
      inviteRebateAmount.value = parseFloat(rebate.settingValue) || 0
    }
  } catch (e) {}
}

const saveInviteRebate = async () => {
  savingSetting.value = true
  try {
    await axios.post('/api/admin/settings', {
      settingKey: 'invite_rebate_amount',
      settingValue: String(inviteRebateAmount.value),
      description: '老带新邀请返现金额(元)'
    })
    ElMessage.success('设置已保存')
  } catch (e) {
    ElNotification.error((e.response?.data?.message || '保存失败'))
  } finally {
    savingSetting.value = false
  }
}

const initCharts = () => {
    if (!roleChartRef.value || !statusChartRef.value) return

    // Role Chart
    const roleChart = echarts.init(roleChartRef.value)
    const roleCounts = {}
    users.value.forEach(u => {
        roleCounts[u.role] = (roleCounts[u.role] || 0) + 1
    })
    roleChart.setOption({
        title: { text: '用户角色分布', left: 'center' },
        tooltip: { trigger: 'item' },
        legend: { bottom: '5%', left: 'center' },
        series: [
            {
                name: '角色',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                    borderRadius: 10,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: { show: false, position: 'center' },
                emphasis: {
                    label: { show: true, fontSize: 20, fontWeight: 'bold' }
                },
                data: Object.keys(roleCounts).map(role => ({ value: roleCounts[role], name: formatRole(role) }))
            }
        ]
    })

    // Appointment Status Chart
    const statusChart = echarts.init(statusChartRef.value)
    const statusCounts = {}
    appointments.value.forEach(a => {
        statusCounts[a.status] = (statusCounts[a.status] || 0) + 1
    })
    statusChart.setOption({
        title: { text: '预约状态分布', left: 'center' },
        tooltip: { trigger: 'item' },
        xAxis: { type: 'category', data: Object.keys(statusCounts).map(s => formatStatus(s)) },
        yAxis: { type: 'value' },
        series: [{ data: Object.values(statusCounts), type: 'bar', backgroundStyle: { color: 'rgba(180, 180, 180, 0.2)' } }]
    })
}

// User Actions
const openCreateUser = () => {
    userForm.value = { id: null, username: '', nickname: '', role: 'USER', password: '', phone: '', email: '', gender: '' }
    isEditUser.value = false
    userDialogVisible.value = true
}

const openEditUser = (row) => {
    userForm.value = { ...row, password: '' } // Clear password field
    isEditUser.value = true
    userDialogVisible.value = true
}

const saveUser = async () => {
    try {
        // Regex Validation
        const phoneRegex = /^1[3-9]\d{9}$/
        if (userForm.value.phone && !phoneRegex.test(userForm.value.phone)) {
            ElNotification.error('手机号格式不正确')
            return
        }
        
        const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
        if (userForm.value.email && !emailRegex.test(userForm.value.email)) {
            ElNotification.error('邮箱格式不正确')
            return
        }

        if (isEditUser.value) {
            await axios.put(`/api/admin/users/${userForm.value.id}`, userForm.value)
            ElMessage.success('更新成功')
            // If the edited user is the current logged-in admin, update the store to sync the header
            if (userForm.value.id === userStore.user?.id) {
                userStore.setUser({ ...userStore.user, ...userForm.value })
            }
        } else {
            await axios.post('/api/admin/users', userForm.value)
            ElMessage.success('创建成功')
        }
        userDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const deleteUser = async (id) => {
    try {
        await ElMessageBox.confirm(
            '该操作将永久删除该用户账号及其所有关联数据，且无法恢复。您确定要执行此操作吗？',
            '敏感操作确认',
            {
                confirmButtonText: '确定删除',
                cancelButtonText: '暂不删除',
                type: 'warning',
                center: true,
                draggable: true
            }
        )
        await axios.delete(`/api/admin/users/${id}`)
        ElMessage.success('删除成功')
        loadData()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

// Appointment Actions
const openCreateAppointment = () => {
    appointmentForm.value = { id: null, userId: '', counselorId: '', appointmentTime: '', status: 'PENDING' }
    bookingDate.value = ''
    bookingTimeSlot.value = ''
    availableTimeSlots.value = []
    appointmentDialogVisible.value = true
}

const openEditAppointment = (row) => {
    appointmentForm.value = { ...row }
    if (row.appointmentTime) {
        bookingDate.value = row.appointmentTime.split(' ')[0]
        bookingTimeSlot.value = row.appointmentTime
        // Trigger load slots immediately
        if (row.counselorId || (row.counselor && row.counselor.id)) {
            loadAvailableSlots(row.counselorId || row.counselor.id)
        }
    }
    appointmentDialogVisible.value = true
}

const saveAppointment = async () => {
    try {
        if (appointmentForm.value.id) {
            await axios.put(`/api/admin/appointments/${appointmentForm.value.id}`, appointmentForm.value)
            ElMessage.success('预约更新成功')
        } else {
            await axios.post('/api/appointments', {
                userId: appointmentForm.value.userId,
                counselorId: appointmentForm.value.counselorId,
                appointmentTime: appointmentForm.value.appointmentTime
            })
            ElMessage.success('预约创建成功')
        }
        appointmentDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const deleteAppointment = async (id) => {
    try {
        await ElMessageBox.confirm(
            '确定要移除这条预约记录吗？建议已完成的预约应保留作为历史查阅。',
            '操作确认',
            {
                confirmButtonText: '确认移除',
                cancelButtonText: '取消',
                type: 'warning',
                center: true,
                draggable: true
            }
        )
        await axios.delete(`/api/admin/appointments/${id}`)
        ElMessage.success('删除成功')
        loadData()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

// Withdrawal Actions
const approveWithdrawal = async (id) => {
    try {
        await axios.put(`/api/withdrawals/admin/${id}/approve`)
        ElMessage.success('打款处理成功')
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const rejectWithdrawal = async (id) => {
    try {
        const { value: reason } = await ElMessageBox.prompt('请输入拒绝原因', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            inputErrorMessage: '原因不能为空',
            inputValidator: (val) => {
                if (!val) return '原因不能为空'
                return true
            }
        })
        
        await axios.put(`/api/withdrawals/admin/${id}/reject`, { reason })
        ElMessage.success('已拒绝申请')
        loadData()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

const deleteCounselor = async (id) => {
    try {
        await ElMessageBox.confirm(
            '确定要取消该咨询师的认证状态吗？取消后该账号将恢复为普通用户。',
            '操作确认',
            {
                confirmButtonText: '确认取消认证',
                cancelButtonText: '返回',
                type: 'warning',
                center: true,
                draggable: true
            }
        )
        await axios.delete(`/api/admin/counselors/${id}`)
        ElMessage.success('删除成功')
        loadData()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

onMounted(loadData)
</script>

<style scoped>
:root {
  --primary-color: #4361ee;
  --success-color: #4cc9f0;
  --warning-color: #f72585;
  --danger-color: #ef233c;
  --bg-color: #f8f9fa;
  --card-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.layout-container {
  height: 100vh;
  background-color: #f8f9fa;
}

.aside-menu {
  background-color: #1a1c23;
  box-shadow: 4px 0 15px rgba(0,0,0,0.05);
  z-index: 10;
  transition: all 0.3s;
}

.logo-box {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #1a1c23;
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.logo-text {
  color: white;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: 2px;
  background: linear-gradient(120deg, #fff 0%, #aaa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.el-menu-vertical {
  border-right: none;
  padding: 10px;
}

.el-menu-vertical :deep(.el-menu-item) {
  height: 50px;
  line-height: 50px;
  margin-bottom: 8px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #4361ee 0%, #4cc9f0 100%) !important;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  color: white !important;
}

.el-menu-vertical :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05) !important;
}

.header {
  background-color: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  height: 70px;
  border-bottom: 1px solid #edf2f7;
}

.header-right {
  display: flex;
  align-items: center;
}

.username {
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
  margin-left: 8px;
  line-height: 1;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 5px 8px;
  border-radius: 20px;
  transition: all 0.2s;
}

.el-dropdown-link:hover {
  background: rgba(0,0,0,0.04);
}

.main-content {
  padding: 30px;
  background-color: #f8f9fa;
}

/* Stat Cards Revamp */
.stat-card {
  border: none;
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: var(--card-shadow);
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  pointer-events: none;
}

.stat-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
}

.stat-content {
  padding: 15px 10px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
  opacity: 0.8;
}

.stat-value {
  font-size: 32px;
  margin-top: 10px;
}

.stat-icon {
  font-size: 56px;
  position: absolute;
  right: -10px;
  bottom: -10px;
  opacity: 0.2;
}

.stat-card.blue { background: linear-gradient(135deg, #4361ee 0%, #4895ef 100%); color: white; }
.stat-card.green { background: linear-gradient(135deg, #2ec4b6 0%, #cbf3f0 100%); color: #006d77; }
.stat-card.orange { background: linear-gradient(135deg, #ff9f1c 0%, #ffbf69 100%); color: #854d0e; }
.stat-card.purple { background: linear-gradient(135deg, #7209b7 0%, #b5179e 100%); color: white; }

/* Table & Section Cards */
.section-card {
  border-radius: 24px;
  border: none;
  box-shadow: var(--card-shadow);
  padding: 10px;
}

.section-header {
  padding-bottom: 20px;
  border-bottom: 1px solid #f1f3f5;
  margin-bottom: 20px;
}

.section-header h3 {
  font-size: 20px;
  color: #1a202c;
  font-weight: 700;
}

:deep(.el-table) {
  --el-table-border-color: #f1f3f5;
  --el-table-header-bg-color: #f8f9fa;
  border-radius: 16px;
}

:deep(.el-rate) {
  --el-rate-fill-color: #f59e0b;
  --el-rate-void-color: #e2e8f0;
  display: inline-flex;
  align-items: center;
}

:deep(.el-rate__item) {
  transition: transform 0.2s ease;
}

:deep(.el-rate__item:hover) {
  transform: scale(1.2);
}

:deep(.el-table th) {
  text-transform: uppercase;
  font-size: 12px;
  letter-spacing: 1px;
  color: #718096;
}

:deep(.el-button--primary.is-link) {
  font-weight: 600;
}

/* Dialog Styling */
:deep(.el-dialog) {
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

:deep(.el-dialog__header) {
  margin-right: 0;
  padding: 25px 25px 10px;
  font-weight: 700;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px var(--el-border-color) inset, 0 2px 4px rgba(0,0,0,0.02) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset, 0 2px 4px rgba(0,0,0,0.02) inset;
}

/* Remove browser autofill blue background */
:deep(.el-input__inner:-webkit-autofill),
:deep(.el-input__inner:-webkit-autofill:hover),
:deep(.el-input__inner:-webkit-autofill:focus) {
  -webkit-box-shadow: 0 0 0 1000px white inset !important;
  -webkit-text-fill-color: #303133 !important;
  transition: background-color 5000s ease-in-out 0s;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #edf2f7;
  border-radius: 20px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 100px;
  height: 100px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f9fa;
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: #4361ee;
  background-color: rgba(67, 97, 238, 0.02);
}

.avatar-uploader-icon {
  font-size: 24px;
  color: #a0aec0;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.hidden-upload {
  display: none;
}

.qualification-uploader-admin {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  position: relative;
  overflow: hidden;
  width: 200px;
  height: 120px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fbfdff;
  cursor: pointer;
}

.qualification-uploader-admin:hover {
  border-color: #409eff;
  background-color: #f8f9fa;
}

.qualification-preview {
  width: 100%;
  height: 120px;
  display: block;
  object-fit: cover;
}

.qualification-placeholder {
  color: #8c939d;
  font-size: 13px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

.table-actions :deep(.el-button) {
  margin-left: 0 !important;
}

.setting-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #2d3748;
}
</style>
