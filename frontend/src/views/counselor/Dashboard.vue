<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside-menu">
      <div class="logo-box">
        <span class="logo-text">咨询师工作台</span>
      </div>
      <el-menu
        :default-active="activeTab"
        class="el-menu-vertical"
        @select="(index) => activeTab = index"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#fff"
      >
        <el-menu-item index="appointments">
          <el-icon><Calendar /></el-icon>
          <span>预约管理</span>
        </el-menu-item>
        <el-menu-item index="info">
          <el-icon><User /></el-icon>
          <span>我的信息</span>
        </el-menu-item>
        <el-menu-item index="schedule">
          <el-icon><Timer /></el-icon>
          <span>排班管理</span>
        </el-menu-item>
        <el-menu-item index="revenue">
          <el-icon><Wallet /></el-icon>
          <span>收益管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ menuTitle[activeTab] }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleNotificationCommand">
              <div class="notification-badge">
                 <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
                    <el-icon :size="20"><Bell /></el-icon>
                 </el-badge>
              </div>
              <template #dropdown>
                <el-dropdown-menu class="notification-dropdown">
                  <div class="notification-header">系统通知</div>
                  <div v-if="notifications.length === 0" class="no-notification">暂无通知</div>
                  <el-dropdown-item v-for="n in notifications.slice(0, 5)" :key="n.id" :command="n" :class="{ 'unread': !n.isRead }">
                    <div class="notification-item">
                      <div class="n-content">{{ n.content }}</div>
                      <div class="n-time">{{ formatDateTime(n.createTime) }}</div>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item v-if="notifications.length > 0" command="__VIEW_ALL__" class="notification-footer">查看全部</el-dropdown-item>
                </el-dropdown-menu>
              </template>
           </el-dropdown>

           <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-avatar :size="32" :icon="UserFilled" :src="getAvatar(userStore.user?.avatar)" />
              <span class="username">{{ userStore.user?.nickname || userStore.user?.username }}</span>
              <el-icon class="el-icon--right"><arrow-down /></el-icon>
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
        <!-- Appointments Management -->
        <div v-if="activeTab === 'appointments'">
          <el-card shadow="hover" class="section-card">
            <template #header>
              <div class="section-header">
                <h3><el-icon><Calendar /></el-icon> 预约管理</h3>
              </div>
            </template>
            <el-table :data="appointments" stripe style="width: 100%">
              <el-table-column align="center" label="用户信息" width="200">
                <template #default="scope">
                  <div style="display: flex; align-items: center; gap: 10px;" v-if="scope.row.user">
                    <el-avatar :size="32" :src="getAvatar(scope.row.user?.avatar)" />
                    <span>{{ scope.row.user.nickname }}</span>
                  </div>
                  <span v-else>未知用户</span>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="appointmentTime" label="预约时间" width="180">
                <template #default="scope">
                  {{ formatDateTime(scope.row.appointmentTime) }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="用户评价" min-width="250">
                <template #default="scope">
                  <div v-if="scope.row.rating">
                    <el-rate v-model="scope.row.rating" disabled show-score text-color="#ff9900" size="small" />
                    <div class="feedback-text">{{ scope.row.feedback || '无文字评价' }}</div>
                    <div v-if="scope.row.counselorReply" class="reply-box">
                      <span class="reply-label">我的回复:</span> {{ scope.row.counselorReply }}
                    </div>
                    <el-button v-else type="primary" link size="small" @click="openReply(scope.row)" :icon="ChatLineRound">回复评价</el-button>
                  </div>
                  <span v-else class="empty-text">暂无评价</span>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="操作" width="250" fixed="right">
                <template #default="scope">
                  <div class="table-actions">
                    <template v-if="scope.row.status === 'PENDING'">
                      <el-button size="small" type="success" @click="updateStatus(scope.row.id, 'CONFIRMED')" link :icon="Check">确认</el-button>
                      <el-button size="small" type="danger" @click="updateStatus(scope.row.id, 'CANCELLED')" link :icon="Close">拒绝</el-button>
                    </template>
                    <el-button v-if="scope.row.status === 'CONFIRMED'" size="small" type="primary" @click="updateStatus(scope.row.id, 'COMPLETED')" link :icon="CircleCheck">完成咨询</el-button>
                    <el-button v-if="scope.row.user" size="small" type="info" @click="openChat(scope.row.user)" link :icon="ChatDotRound">联系用户</el-button>
                  </div>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- Personal Info Management -->
        <div v-if="activeTab === 'info'">
          <el-card shadow="hover" class="section-card">
            <template #header>
              <div class="section-header">
                <h3><el-icon><User /></el-icon> 个人资料管理</h3>
              </div>
            </template>
            <el-form :model="counselorInfo" label-width="120px" class="info-form">
              <el-row :gutter="40">
                <el-col :span="14">
                  <el-form-item label="擅长领域">
                    <el-input v-model="counselorInfo.expertise" placeholder="如：青少年心理、职场减压" />
                  </el-form-item>
                  <el-form-item label="专业简介">
                    <el-input v-model="counselorInfo.introduction" type="textarea" :rows="6" placeholder="请详细介绍您的专业背景和经验..." />
                  </el-form-item>
                  <el-form-item label="咨询费用">
                    <el-input-number v-model="counselorInfo.fee" :min="0" :step="10" style="width: 100%" />
                    <div class="form-tip">每小时咨询费用（元）</div>
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="updateCounselorInfo" :icon="Checked" size="large">保存资料更新</el-button>
                  </el-form-item>
                </el-col>
                <el-col :span="10">
                  <el-form-item label="资质证明">
                    <div class="qualification-uploader">
                      <img v-if="qualificationImageUrl" :src="qualificationImageUrl" class="qualification-img" />
                      <div v-else class="uploader-placeholder">
                        <el-icon class="uploader-icon"><Upload /></el-icon>
                        <div class="uploader-text">暂无资质证明</div>
                      </div>
                    </div>
                    <div class="form-tip">资质证明需联系管理员人工更换</div>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>
        </div>

        <!-- Schedule Management -->
        <div v-if="activeTab === 'schedule'">
          <el-card shadow="hover" class="section-card">
            <template #header>
              <div class="section-header">
                <h3><el-icon><Timer /></el-icon> 排班管理</h3>
                <el-button type="primary" :icon="Plus" @click="openBatchAdd">批量添加排班</el-button>
              </div>
            </template>
            <div class="filter-bar">
              <el-form :inline="true">
                <el-form-item label="日期过滤">
                  <el-date-picker
                    v-model="scheduleDate"
                    type="date"
                    placeholder="选择日期查看排班"
                    value-format="YYYY-MM-DD"
                    @change="filterTimeSlots"
                  />
                </el-form-item>
              </el-form>
            </div>
            <el-table :data="filteredTimeSlots" stripe style="width: 100%">
              <el-table-column align="center" label="日期" width="150">
                <template #default="scope">
                  {{ scope.row.startTime.split('T')[0] }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="时间段">
                <template #default="scope">
                  <span class="time-range">{{ formatTimeRange(scope.row.startTime, scope.row.endTime) }}</span>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="status" label="状态" width="120">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'info'" effect="plain">
                    {{ scope.row.status === 'AVAILABLE' ? '可预约' : '已预约' }}
                  </el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button 
                    size="small" 
                    type="danger" 
                    :icon="Delete" 
                    @click="deleteTimeSlot(scope.row.id)" 
                    link
                    :disabled="scope.row.status !== 'AVAILABLE'"
                  >删除</el-button>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- Revenue Management -->
        <div v-if="activeTab === 'revenue'">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-card shadow="hover" class="revenue-card premium-gradient">
                <div class="revenue-header">当前可提现余额</div>
                <div class="revenue-value">¥ {{ counselorInfo.totalRevenue || 0 }}</div>
                <el-button 
                  type="primary" 
                  class="withdraw-btn glass-btn" 
                  @click="withdrawDialogVisible = true" 
                  :disabled="(counselorInfo.totalRevenue || 0) <= 0"
                  :icon="Wallet"
                >申请提现</el-button>
              </el-card>

              <el-card shadow="hover" class="feedback-summary-card" style="margin-top: 20px;">
                <template #header>
                  <div class="card-header">
                    <span><el-icon><Star /></el-icon> 服务评价概览</span>
                  </div>
                </template>
                <div class="feedback-stats">
                  <div class="stat-item">
                    <div class="stat-label">平均评分</div>
                    <div class="stat-value">{{ averageRating }}</div>
                    <el-rate v-model="averageRating" disabled size="small" />
                  </div>
                  <div class="stat-item">
                    <div class="stat-label">高分好评率</div>
                    <div class="stat-value">{{ positiveRate }}%</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            <el-col :span="16">
              <el-card shadow="hover" class="section-card">
                <template #header>
                  <div class="section-header">
                    <h3><el-icon><List /></el-icon> 提现记录</h3>
                  </div>
                </template>
                <el-table :data="myWithdrawals" stripe style="width: 100%">
                  <el-table-column align="center" prop="requestTime" label="申请时间" width="180">
                    <template #default="scope">
                      {{ formatDateTime(scope.row.requestTime) }}
                    </template>
                  </el-table-column align="center">
                  <el-table-column align="center" prop="amount" label="金额">
                    <template #default="scope">
                      <span class="amount-text">¥{{ scope.row.amount }}</span>
                    </template>
                  </el-table-column align="center">
                  <el-table-column align="center" prop="status" label="状态">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)">{{ formatStatus(scope.row.status) }}</el-tag>
                    </template>
                  </el-table-column align="center">
                  <el-table-column align="center" prop="note" label="备注" show-overflow-tooltip />
                </el-table>
              </el-card>
            </el-col>
          </el-row>
        </div>



      </el-main>
    </el-container>
  </el-container>

  <el-dialog v-model="chatDialogVisible" title="在线咨询" width="600px" class="chat-dialog" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><ChatDotRound /></el-icon>
        <span>在线咨询会话 - {{ currentChatTargetName }}</span>
      </div>
    </template>
    <div class="chat-box" ref="chatBoxRef">
        <div v-for="(msg, index) in chatMessages" :key="index" :class="['message', msg.senderId === userStore.user?.id ? 'my-message' : 'other-message']">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatDateTime(msg.timestamp).split(' ')[1] }}</div>
        </div>
    </div>
    <div class="chat-input">
        <el-input 
            v-model="chatInput" 
            placeholder="输入消息，按回车发送..." 
            @keyup.enter="sendMessage"
            :prefix-icon="ChatLineRound"
        >
             <template #append>
                 <el-button type="primary" @click="sendMessage">发送</el-button>
             </template>
        </el-input>
    </div>
  </el-dialog>

  <el-dialog v-model="withdrawDialogVisible" title="申请提现" width="440px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><Wallet /></el-icon>
        <span>余额提现申请</span>
      </div>
    </template>
    <el-form label-position="top">
      <el-form-item label="提现金额">
        <el-input-number 
            v-model="withdrawAmount" 
            :min="1" 
            :max="counselorInfo.totalRevenue" 
            :precision="2" 
            :step="100"
            style="width: 100%"
            placeholder="请输入提现金额"
        >
             <template #suffix>元</template>
        </el-input-number>
        <div class="form-tip">当前可提现最高金额: <span class="amount-highlight">¥{{ counselorInfo.totalRevenue || 0 }}</span></div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWithdraw" :icon="Check">确认提交</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- Reply Dialog -->
  <el-dialog v-model="replyDialogVisible" title="回复用户评价" width="500px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><ChatLineRound /></el-icon>
        <span>回复评价内容</span>
      </div>
    </template>
    <el-form label-position="top">
      <el-form-item label="您的回复">
        <el-input 
            v-model="replyContent" 
            type="textarea" 
            :rows="5" 
            placeholder="请输入您的专业回复，这有助于建立您的良好声誉..." 
            maxlength="500"
            show-word-limit
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :icon="Check">提交回复</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- Batch Schedule Dialog -->
  <el-dialog v-model="batchAddVisible" title="批量排班设置" width="600px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><Calendar /></el-icon>
        <span>批量生成排班时段</span>
      </div>
    </template>
    <el-form :model="batchForm" label-position="top">
        <el-form-item label="选择生效日期">
            <el-date-picker 
                v-model="batchForm.dates" 
                type="dates" 
                placeholder="点击选择一个或多个日期" 
                value-format="YYYY-MM-DD"
                style="width: 100%"
            />
            <div class="form-tip">您可以一次性选择多个日期进行批量排班</div>
        </el-form-item>
        <el-form-item label="设置时间段">
            <div v-for="(range, index) in batchForm.ranges" :key="range.id" class="range-row">
                <el-time-select 
                    v-model="range.start"
                    start="08:00"
                    step="00:30"
                    end="22:00"
                    placeholder="开始时间"
                    style="width: 140px"
                />
                <span class="range-separator">至</span>
                <el-time-select 
                    v-model="range.end"
                    start="08:00"
                    step="00:30"
                    end="22:00"
                    placeholder="结束时间"
                    :min-time="range.start"
                    style="width: 140px"
                />
                <el-button type="danger" circle @click="removeRange(index)" v-if="batchForm.ranges.length > 1" size="small">
                    <el-icon><Delete /></el-icon>
                </el-button>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="addRange" style="width: 100%; margin-top: 10px">
                添加更多时间段
            </el-button>
        </el-form-item>
    </el-form>
    <template #footer>
        <el-button @click="batchAddVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBatchSchedule" :icon="Checked">生成排班</el-button>
    </template>
  </el-dialog>

  <!-- Notification Dialog -->
  <el-dialog v-model="notificationDialogVisible" title="所有通知" width="600px">
      <div v-if="notifications.length === 0" style="text-align: center; color: #909399; padding: 20px;">暂无通知</div>
      <div v-else class="notification-list">
          <div v-for="n in notifications" :key="n.id" class="notification-full-item" :class="{ 'unread': !n.isRead }" @click="handleNotificationCommand(n)">
              <div class="n-icon">
                  <el-icon v-if="n.type === 'WALLET'" color="#67C23A"><Wallet /></el-icon>
                  <el-icon v-else-if="n.type === 'NEW_APPOINTMENT'" color="#E6A23C"><Calendar /></el-icon>
                  <el-icon v-else color="#409EFF"><Bell /></el-icon>
              </div>
              <div class="n-body">
                  <div class="n-content">{{ n.content }}</div>
                  <div class="n-time">{{ formatDateTime(n.createTime) }}</div>
              </div>
              <div class="n-status" v-if="!n.isRead">
                  <el-badge is-dot />
              </div>
          </div>
      </div>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { formatDateTime } from '../../utils/format'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'
import { 
  Calendar, User, Wallet, ArrowDown, Timer, 
  Delete, UserFilled, Plus, Check, Close, 
  CircleCheck, ChatLineRound, List, ChatDotRound,
  Upload, Checked, Bell, Star
} from '@element-plus/icons-vue'

import { getAvatar } from '../../utils/avatar'
import { resolveAssetUrl } from '../../utils/asset'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('appointments')
const appointments = ref([])
const counselorInfo = ref({})
const counselorId = ref(null)

const timeSlots = ref([])
const filteredTimeSlots = ref([])
const scheduleDate = ref('')
const batchAddVisible = ref(false)
const batchForm = ref({
    dates: [],
    ranges: [{ start: '', end: '' }]
})

const chatDialogVisible = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const currentChatTargetId = ref(null)
const currentChatTargetName = ref('')
const chatBoxRef = ref(null)
let stompClient = null

// Notifications
const notifications = ref([])
const unreadCount = ref(0)
const notificationDialogVisible = ref(false)

const loadNotifications = async () => {
    try {
        if (!userStore.user?.id) return
        const res = await axios.get('/api/notifications/my')
        notifications.value = res.data.data
        const countRes = await axios.get('/api/notifications/unread-count')
        unreadCount.value = countRes.data.data
    } catch (e) {
        ElNotification.error('通知加载失败')
    }
}

const handleNotificationCommand = async (n) => {
    if (n === '__VIEW_ALL__') {
        viewAllNotifications()
        return
    }
    if (!n.isRead) {
        try {
            await axios.put(`/api/notifications/${n.id}/read`)
            n.isRead = true
            unreadCount.value = Math.max(0, unreadCount.value - 1)
        } catch (e) {}
    }
}

const viewAllNotifications = () => {
    notificationDialogVisible.value = true
}

const myWithdrawals = ref([])
const withdrawDialogVisible = ref(false)
const withdrawAmount = ref(0)
const replyDialogVisible = ref(false)
const replyContent = ref('')
const currentReplyId = ref(null)

const openReply = (appointment) => {
    currentReplyId.value = appointment.id
    replyContent.value = ''
    replyDialogVisible.value = true
}

const submitReply = async () => {
    if (!replyContent.value.trim()) {
        ElMessage.warning('请输入回复内容')
        return
    }
    try {
        await axios.put(`/api/appointments/${currentReplyId.value}/reply`, { reply: replyContent.value })
        ElMessage.success('回复成功')
        replyDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error('回复失败')
    }
}

const menuTitle = {
    'appointments': '预约管理',
    'info': '我的信息',
    'schedule': '排班管理',
    'revenue': '收益管理'
}

const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CONFIRMED': '已确认',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
}

const formatStatus = (status) => statusMap[status] || status

const getStatusType = (status) => {
    if (['APPROVED', 'COMPLETED', 'CONFIRMED'].includes(status)) return 'success'
    if (['REJECTED', 'CANCELLED'].includes(status)) return 'danger'
    if (['PENDING'].includes(status)) return 'warning'
    return 'info'
}

const handleCommand = (command) => {
    if (command === 'logout') {
        logout()
    }
}

const logout = () => {
  if (stompClient) stompClient.deactivate()
  userStore.logout()
  router.push('/login')
}

const averageRating = computed(() => {
    const rated = appointments.value.filter(a => a.rating)
    if (!rated.length) return 0
    const sum = rated.reduce((acc, a) => acc + a.rating, 0)
    return Number((sum / rated.length).toFixed(1))
})

const qualificationImageUrl = computed(() => {
    return resolveAssetUrl(counselorInfo.value?.qualification || counselorInfo.value?.qualificationUrl || '')
})

const positiveRate = computed(() => {
    const rated = appointments.value.filter(a => a.rating)
    if (!rated.length) return 0
    const high = rated.filter(a => a.rating >= 4).length
    return Math.round((high / rated.length) * 100)
})

const loadData = async () => {
    try {
        if (!userStore.user?.id) return
        const allCounselors = await axios.get('/api/counselors')
        const myCounselor = allCounselors.data.data.find(c => c.user?.id === userStore.user.id)
        
        if (myCounselor) {
            counselorId.value = myCounselor.id
            counselorInfo.value = myCounselor
            const res = await axios.get('/api/appointments/counselor?counselorId=' + counselorId.value)
            appointments.value = res.data.data

            loadTimeSlots()
            loadWithdrawals()
            initWebSocket()
            loadNotifications()
        } else {
            ElMessage.warning('您还不是认证咨询师')
        }
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '数据加载失败'))
    }
}

const loadWithdrawals = async () => {
    try {
        const res = await axios.get('/api/withdrawals/my')
        myWithdrawals.value = res.data.data
    } catch (e) {
        ElNotification.error('提现记录加载失败')
    }
}

const filterTimeSlots = () => {
    if (!scheduleDate.value) {
        filteredTimeSlots.value = timeSlots.value
    } else {
        filteredTimeSlots.value = timeSlots.value.filter(slot => {
            if (!slot.startTime) return false
            // Handle ISO string format "YYYY-MM-DDTHH:mm:ss" or "YYYY-MM-DD HH:mm:ss"
            const slotDate = slot.startTime.replace('T', ' ').substring(0, 10)
            return slotDate === scheduleDate.value
        })
    }
}

const getWithdrawalStatusType = (status) => {
    const map = {
        'PENDING': 'warning',
        'APPROVED': 'success',
        'REJECTED': 'danger'
    }
    return map[status] || 'info'
}

const formatTimeRange = (start, end) => {
    if (!start || !end) return ''
    // Handle both "YYYY-MM-DDTHH:mm:ss" and "YYYY-MM-DD HH:mm:ss"
    const s = start.replace('T', ' ').split(' ')[1].substring(0, 5)
    const e = end.replace('T', ' ').split(' ')[1].substring(0, 5)
    return `${s} - ${e}`
}

const loadTimeSlots = async () => {
    if (!counselorId.value) return
    try {
        const res = await axios.get(`/api/timeslots/counselor/${counselorId.value}`)
        timeSlots.value = (res.data.data || []).sort((a, b) => new Date(a.startTime) - new Date(b.startTime))
        filterTimeSlots()
    } catch (e) {
        ElNotification.error('排班加载失败')
    }
}

const openBatchAdd = () => {
    batchForm.value = {
        dates: [],
        ranges: [{ id: Date.now(), start: '', end: '' }]
    }
    batchAddVisible.value = true
}

const addRange = () => {
    batchForm.value.ranges.push({ id: Date.now(), start: '', end: '' })
}

const removeRange = (index) => {
    batchForm.value.ranges.splice(index, 1)
}

const submitBatchSchedule = async () => {
    if (!batchForm.value.dates.length) {
        ElMessage.warning('请选择日期')
        return
    }
    // Validate ranges
    for (const range of batchForm.value.ranges) {
        if (!range.start || !range.end) {
            ElMessage.warning('请完善时间段信息')
            return
        }
        if (range.start >= range.end) {
             ElMessage.warning('开始时间必须小于结束时间')
             return
        }
    }

    const slots = []
    for (const date of batchForm.value.dates) {
        for (const range of batchForm.value.ranges) {
            slots.push({
                startTime: `${date}T${range.start}:00`,
                endTime: `${date}T${range.end}:00`
            })
        }
    }

    try {
        await axios.post(`/api/timeslots/counselor/${counselorId.value}/batch`, slots)
        ElMessage.success(`成功添加 ${slots.length} 个排班`)
        batchAddVisible.value = false
        // Clear date filter to show all slots including newly added ones
        scheduleDate.value = ''
        loadTimeSlots()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || e.message))
    }
}

const deleteTimeSlot = async (id) => {
    try {
        await ElMessageBox.confirm(
            '确定要移除这个排班时段吗？移除后用户将无法再预约此时间。',
            '排班调整确认',
            {
                confirmButtonText: '确认移除',
                cancelButtonText: '取消',
                type: 'warning',
                center: true,
                draggable: true
            }
        )
        await axios.delete(`/api/timeslots/${id}`)
        ElMessage.success('删除成功')
        loadTimeSlots()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

const submitWithdraw = async () => {
    if (withdrawAmount.value <= 0) {
        ElNotification.error('金额必须大于0')
        return
    }
    if (withdrawAmount.value > counselorInfo.value.totalRevenue) {
        ElNotification.error('提现金额不能超过可提现余额')
        return
    }
    try {
        await axios.post('/api/withdrawals/request', {
            counselorId: counselorId.value,
            amount: withdrawAmount.value
        })
        ElMessage.success('申请提交成功，请等待管理员审核')
        withdrawDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || e.message))
    }
}

const initWebSocket = () => {
    if (stompClient) {
        if (stompClient.active) return
        stompClient.deactivate()
    }
    const token = userStore.token || localStorage.getItem('token')
    if (!token) {
        return
    }
    const socket = new SockJS('/ws-chat')
    stompClient = new Client({
        webSocketFactory: () => socket,
        connectHeaders: {
            Authorization: `Bearer ${token}`
        },
        onConnect: () => {
            if (!userStore.user?.id) return
            stompClient.subscribe(`/queue/messages/${userStore.user.id}`, (message) => {
                const msg = JSON.parse(message.body)
                if ((msg.senderId === currentChatTargetId.value || msg.senderId === userStore.user.id) && chatDialogVisible.value) {
                    // Check if message already exists
                    if (!chatMessages.value.some(m => m.timestamp === msg.timestamp && m.content === msg.content)) {
                        chatMessages.value.push(msg)
                        nextTick(scrollToBottom)
                    }
                } else {
                    ElMessage.info(`收到新消息`)
                }
            })

            // Notification Subscription
            stompClient.subscribe(`/queue/notifications/${userStore.user.id}`, (message) => {
                const notification = JSON.parse(message.body)
                notifications.value.unshift(notification)
                unreadCount.value++
                ElMessage.info('新通知: ' + notification.content)
            })
        },
        onStompError: (frame) => {
            ElNotification.error(frame.headers['message'] || '聊天服务异常')
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000
    })
    stompClient.activate()
}

const openChat = async (target) => {
    if (!userStore.user?.id) return
    const targetId = typeof target === 'object' ? target.id : target
    currentChatTargetId.value = targetId
    currentChatTargetName.value = typeof target === 'object' ? (target.nickname || target.username) : '用户'
    chatDialogVisible.value = true
    try {
        const res = await axios.get(`/api/messages/${userStore.user.id}/${targetId}`)
        chatMessages.value = res.data.data
        nextTick(scrollToBottom)
    } catch (e) {
        ElNotification.error('聊天记录加载失败')
    }
}

const sendMessage = () => {
    if (!chatInput.value.trim() || !userStore.user?.id) return
    
    if (!stompClient || !stompClient.active) {
        ElNotification.error('聊天服务未连接，正在尝试重新连接...')
        initWebSocket()
        return
    }

    const message = {
        senderId: userStore.user.id,
        receiverId: currentChatTargetId.value,
        content: chatInput.value
    }
    
    try {
        stompClient.publish({
            destination: "/app/chat",
            body: JSON.stringify(message)
        })
        chatInput.value = ''
    } catch (e) {
        ElNotification.error('发送失败')
    }
}

const scrollToBottom = () => {
    if (chatBoxRef.value) {
        chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
    }
}

const updateStatus = async (id, status) => {
    try {
        await axios.put(`/api/appointments/${id}/status?status=${status}`)
        ElMessage.success('操作成功')
        loadData()
    } catch (e) {
        ElNotification.error('操作失败')
    }
}

const updateCounselorInfo = async () => {
    if (!counselorId.value) {
        ElNotification.error('咨询师信息未加载，请刷新页面')
        return
    }
    try {
        await axios.put(`/api/counselors/${counselorId.value}`, counselorInfo.value)
        ElMessage.success('信息更新成功')
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || e.message))
    }
}

onMounted(loadData)
</script>

<style scoped>
:root {
  --primary-gradient: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
  --card-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
}

.layout-container {
  height: 100vh;
  background-color: #f8f9fa;
}

.aside-menu {
  background-color: #1a1c23;
  box-shadow: 4px 0 15px rgba(0,0,0,0.05);
}

.logo-box {
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(90deg, #001529 0%, #00284d 100%);
  border-bottom: 1px solid rgba(255,255,255,0.05);
}

.logo-text {
  color: white;
  font-size: 20px;
  font-weight: 800;
  letter-spacing: 1px;
  background: linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 2px 10px rgba(142, 197, 252, 0.3);
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
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background: var(--primary-gradient) !important;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  color: white !important;
}

.header {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 70px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 30px rgba(0, 0, 0, 0.03);
  z-index: 100;
}

.header-right {
  display: flex;
  align-items: center;
}

.el-dropdown-link {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 6px 14px;
  border-radius: 14px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid transparent;
}

.el-dropdown-link:hover {
  background: rgba(255, 255, 255, 0.8);
  border-color: rgba(67, 97, 238, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.username {
  font-weight: 600;
  color: #334155;
  line-height: 1;
  margin-left: 12px;
  font-size: 14px;
  letter-spacing: 0.3px;
}

.main-content {
  padding: 30px;
}

.stat-card {
  border-radius: 20px;
  border: none;
  box-shadow: 0 10px 20px rgba(0,0,0,0.05);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  position: relative;
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(67, 97, 238, 0.2);
}

.stat-card.blue { background: linear-gradient(135deg, #4361ee 0%, #4895ef 100%); color: white; }
.stat-card.purple { background: linear-gradient(135deg, #7209b7 0%, #b5179e 100%); color: white; }

.section-card {
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 10px 30px rgba(0,0,0,0.04);
  overflow: hidden;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: #1a202c;
}

:deep(.el-table) {
  border-radius: 16px;
}

:deep(.el-button--primary) {
  border-radius: 12px;
}

.qualification-uploader {
  border: 2px dashed #edf2f7;
  border-radius: 20px;
  width: 100%;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f8f9fa;
  overflow: hidden;
}

.qualification-img {
  width: 100%;
  height: 100%;
  object-fit: contain; /* Keep the aspect ratio for certificates */
}

.uploader-placeholder {
  text-align: center;
  color: #a0aec0;
}

.uploader-icon {
  font-size: 40px;
  margin-bottom: 10px;
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

/* Notification Styles */
.notification-badge {
    margin-right: 30px;
    cursor: pointer;
    color: #64748b;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
    padding: 8px;
    border-radius: 12px;
}

.notification-badge:hover {
    color: var(--primary-color);
    background: rgba(67, 97, 238, 0.08);
    transform: scale(1.1);
}

.notification-dropdown {
    width: 300px;
    padding: 0;
}

.notification-header {
    padding: 10px 15px;
    font-weight: 600;
    border-bottom: 1px solid #EBEEF5;
    color: #303133;
}

.no-notification {
    padding: 20px;
    text-align: center;
    color: #909399;
}

.notification-item {
    padding: 5px 0;
}

.n-content {
    font-size: 14px;
    color: #606266;
    margin-bottom: 4px;
    white-space: normal;
    line-height: 1.4;
}

.n-time {
    font-size: 12px;
    color: #909399;
}

.notification-footer {
    padding: 10px;
    text-align: center;
    border-top: 1px solid #EBEEF5;
    cursor: pointer;
    color: #4361ee;
    background: #fcfcfc;
}

.notification-footer:hover {
    background: #f0f2f5;
}

.notification-full-item {
    display: flex;
    align-items: flex-start;
    gap: 15px;
    padding: 15px;
    border-bottom: 1px solid #EBEEF5;
    cursor: pointer;
    transition: background 0.3s;
}

.notification-full-item:hover {
    background: #f5f7fa;
}

.notification-full-item.unread {
    background: #ecf5ff;
}

.n-icon {
    font-size: 24px;
    margin-top: 2px;
}

.n-body {
    flex: 1;
}
/* Modern Table Styling */
:deep(.el-table) {
  border-radius: 16px;
  overflow: hidden;
  --el-table-header-bg-color: #f8faff;
  --el-table-row-hover-bg-color: #f0f7ff;
}

:deep(.el-table th.el-table__cell) {
  background-color: #f8faff;
  font-weight: 700;
  color: #4a5568;
  height: 50px;
}

:deep(.el-table__row) {
  transition: background-color 0.2s;
}
.range-row {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 12px;
  background: #f8faff;
  padding: 15px;
  border-radius: 12px;
  border: 1px solid #edf2f7;
}
.range-separator {
  color: #909399;
  font-size: 13px;
}
.username {
  font-weight: 600;
  color: #334155;
  margin-left: 12px;
  font-size: 14px;
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
}

.premium-gradient {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%) !important;
  color: white !important;
  border: none !important;
}

.revenue-header {
  opacity: 0.8;
  font-size: 14px;
  margin-bottom: 15px;
}

.revenue-value {
  font-size: 36px;
  font-weight: 800;
  margin-bottom: 25px;
}

.glass-btn {
  background: rgba(255, 255, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: white !important;
  width: 100%;
  height: 44px;
}

.glass-btn:hover {
  background: rgba(255, 255, 255, 0.2) !important;
  transform: translateY(-2px);
}

.feedback-summary-card {
  border-radius: 20px;
  border: 1px solid #f1f5f9;
}

.feedback-stats {
  display: flex;
  justify-content: space-around;
  padding: 10px 0;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 800;
  color: #1e293b;
  margin-bottom: 5px;
}

/* Chat Styles */
.chat-box {
  height: 400px;
  overflow-y: auto;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message {
  max-width: 80%;
  display: flex;
  flex-direction: column;
}

.my-message {
  align-self: flex-end;
}

.other-message {
  align-self: flex-start;
}

.message-content {
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.my-message .message-content {
  background: var(--primary-gradient);
  color: white;
  border-bottom-right-radius: 4px;
}

.other-message .message-content {
  background: white;
  color: #1e293b;
  border-bottom-left-radius: 4px;
  border: 1px solid #e2e8f0;
}

.message-time {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 5px;
  padding: 0 4px;
}

.my-message .message-time {
  text-align: right;
}

.chat-input {
  padding: 0 4px;
}

.chat-input :deep(.el-input-group) {
  display: flex;
  width: 100%;
}

.chat-input :deep(.el-input-group__append) {
  padding: 0;
  border: none;
  background: transparent;
  flex-shrink: 0;
}

.chat-input :deep(.el-input-group__append .el-button) {
  border-radius: 0 8px 8px 0;
  height: 100%;
  padding: 8px 20px;
  margin: 0;
}
</style>
