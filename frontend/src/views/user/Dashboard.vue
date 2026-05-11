<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside-menu">
      <div class="logo-box">
        <span class="logo-text">用户中心</span>
      </div>
      <el-menu
        :default-active="activeTab"
        class="el-menu-vertical"
        @select="(index) => activeTab = index"
        background-color="#001529"
        text-color="rgba(255, 255, 255, 0.65)"
        active-text-color="#fff"
      >
        <el-menu-item index="counselors">
          <el-icon><Search /></el-icon>
          <span>咨询师列表</span>
        </el-menu-item>
        <el-menu-item index="appointments">
          <el-icon><Calendar /></el-icon>
          <span>我的预约</span>
        </el-menu-item>
        <el-menu-item index="profile">
          <el-icon><User /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
        <el-menu-item index="wallet">
          <el-icon><Wallet /></el-icon>
          <span>我的钱包</span>
        </el-menu-item>
        <el-menu-item index="invite">
          <el-icon><Present /></el-icon>
          <span>我的邀请</span>
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
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <!-- Counselors List -->
        <div v-if="activeTab === 'counselors'">
          <div class="filter-bar">
            <el-input 
              v-model="searchKeyword" 
              placeholder="搜索咨询师姓名、擅长领域..." 
              :prefix-icon="Search" 
              style="width: 400px;" 
              clearable 
              class="search-input"
            />
          </div>
          <el-row :gutter="25">
            <el-col :span="6" v-for="c in filteredCounselors" :key="c.id" style="margin-bottom: 25px;">
              <el-card :body-style="{ padding: '0px' }" shadow="hover" class="counselor-card">
                <div class="counselor-banner"></div>
                <div class="counselor-info-box">
                  <el-avatar :size="70" :src="getAvatar(c.user?.avatar)" class="counselor-avatar" />
                  <div class="counselor-name">{{ c.user?.nickname }}</div>
                  <div class="counselor-expertise">
                    <el-tag size="small" effect="dark" round>{{ c.expertise }}</el-tag>
                  </div>
                  <p class="counselor-intro">{{ c.introduction }}</p>
                  <div class="counselor-footer">
                    <div class="counselor-price">
                      <span class="price-symbol">¥</span>
                      <span class="price-value">{{ c.fee }}</span>
                      <span class="price-unit">/小时</span>
                    </div>
                    <el-button type="primary" round @click="bookAppointment(c)" :icon="Calendar">预约</el-button>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          <el-empty v-if="filteredCounselors.length === 0" description="未找到匹配的咨询师" />
        </div>

        <!-- Appointments -->
        <div v-if="activeTab === 'appointments'">
          <el-card shadow="hover" class="section-card">
            <template #header>
              <div class="section-header">
                <h3><el-icon><List /></el-icon> 我的预约记录</h3>
              </div>
            </template>
            <el-table :data="appointments" stripe style="width: 100%" class="appointment-table">
              <el-table-column align="center" label="咨询师" min-width="240">
                <template #default="scope">
                  <div style="display: flex; align-items: center; gap: 10px;">
                    <el-avatar :size="32" :src="getAvatar(scope.row.counselor?.user?.avatar)" />
                    <span>{{ scope.row.counselor?.user?.nickname }}</span>
                  </div>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="appointmentTime" label="预约时间" min-width="240">
                <template #default="scope">
                  {{ formatDateTime(scope.row.appointmentTime) }}
                </template>
              </el-table-column align="center">
              <el-table-column align="center" prop="status" label="状态" min-width="140">
                <template #default="scope">
                  <el-tag :type="getStatusType(scope.row.status)" effect="plain">{{ formatStatus(scope.row.status) }}</el-tag>
                </template>
              </el-table-column align="center">
              <el-table-column align="center" label="操作" min-width="260">
                <template #default="scope">
                  <div class="table-actions">
                    <el-button v-if="scope.row.counselor?.user" size="small" type="primary" @click="openChat(scope.row.counselor.user)" link :icon="ChatDotRound">联系</el-button>
                    <el-button 
                      v-if="scope.row.status === 'COMPLETED' && !scope.row.rating" 
                      size="small" 
                      type="warning" 
                      @click="openRate(scope.row)" 
                      link 
                      :icon="Star"
                    >评价</el-button>
                    <el-button 
                      v-if="scope.row.rating" 
                      size="small" 
                      type="success" 
                      @click="viewRate(scope.row)" 
                      link 
                      :icon="View"
                    >详情</el-button>
                    <el-button 
                      v-if="scope.row.status === 'PENDING' || scope.row.status === 'CONFIRMED'" 
                      size="small" 
                      type="danger" 
                      @click="cancelAppointment(scope.row.id)" 
                      link 
                      :icon="CircleClose"
                    >取消</el-button>
                  </div>
                </template>
              </el-table-column align="center">
            </el-table>
          </el-card>
        </div>

        <!-- Profile -->
        <div v-if="activeTab === 'profile'" class="profile-container">
          <el-row :gutter="30">
            <el-col :span="10">
              <el-card shadow="hover" class="profile-main-card">
                <div class="profile-header-bg"></div>
                <div class="profile-content">
                  <div class="avatar-section">
                    <div class="avatar-wrapper">
                      <el-avatar :size="100" :src="getAvatar(user.avatar)" class="main-avatar" />
                      <div class="avatar-edit-mask" v-if="isEditing" @click="triggerUpload">
                        <el-icon><Camera /></el-icon>
                      </div>
                    </div>
                    <div class="name-section">
                      <h2 class="display-name">{{ user.nickname || user.username }}</h2>
                      <el-tag size="small" effect="light" round class="role-tag">普通用户</el-tag>
                    </div>
                  </div>

                  <div v-if="!isEditing" class="profile-info-list">
                    <div class="info-item">
                      <div class="info-label"><el-icon><Postcard /></el-icon> 账号ID</div>
                      <div class="info-value">{{ user.id }}</div>
                    </div>
                    <div class="info-item">
                      <div class="info-label"><el-icon><Phone /></el-icon> 手机号码</div>
                      <div class="info-value">{{ user.phone || '未填写' }}</div>
                    </div>
                    <div class="info-item">
                      <div class="info-label"><el-icon><Message /></el-icon> 电子邮箱</div>
                      <div class="info-value">{{ user.email || '未填写' }}</div>
                    </div>
                    <div class="info-item">
                      <div class="info-label"><el-icon><Male /></el-icon> 性别</div>
                      <div class="info-value">
                        <el-tag size="small" :type="user.gender === 'Male' ? '' : 'danger'" plain>
                          {{ user.gender === 'Male' ? '男' : (user.gender === 'Female' ? '女' : '未设置') }}
                        </el-tag>
                      </div>
                    </div>
                    <div class="info-item">
                      <div class="info-label"><el-icon><Calendar /></el-icon> 出生日期</div>
                      <div class="info-value">{{ user.birthday || '未设置' }}</div>
                    </div>
                    <div class="profile-actions">
                      <el-button type="primary" plain round @click="isEditing = true" :icon="Edit">编辑个人资料</el-button>
                    </div>
                  </div>

                  <el-form v-else :model="user" label-position="top" class="profile-edit-form">
                    <el-upload
                      ref="avatarUploadRef"
                      class="hidden-upload"
                      action="/api/files/upload"
                      :show-file-list="false"
                      :on-success="handleAvatarSuccess"
                      :before-upload="beforeAvatarUpload"
                    />
                    <el-form-item label="昵称">
                      <el-input v-model="user.nickname" placeholder="请输入您的昵称" :prefix-icon="User" />
                    </el-form-item>
                    <el-form-item label="手机">
                      <el-input v-model="user.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
                    </el-form-item>
                    <el-form-item label="邮箱">
                      <el-input v-model="user.email" placeholder="请输入邮箱" :prefix-icon="Message" />
                    </el-form-item>
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="性别">
                          <el-select v-model="user.gender" placeholder="选择性别" style="width: 100%">
                            <el-option label="男" value="Male" />
                            <el-option label="女" value="Female" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="生日">
                          <el-date-picker v-model="user.birthday" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <div class="edit-footer">
                      <el-button type="primary" round @click="updateProfile" :icon="Check">保存修改</el-button>
                      <el-button round @click="cancelEdit">取消</el-button>
                    </div>
                  </el-form>
                </div>
              </el-card>
            </el-col>

            <el-col :span="14">
              <el-card shadow="hover" class="apply-counselor-card">
                <template #header>
                  <div class="card-header">
                    <h3><el-icon><Promotion /></el-icon> 成为心理咨询师</h3>
                    <p class="header-subtitle">加入我们的专业团队，开启您的线上咨询之旅</p>
                  </div>
                </template>
                
                <div v-if="user.role === 'COUNSELOR'" class="apply-status-result">
                  <el-result icon="success" title="身份认证通过" sub-title="您已具备咨询师权限，可以开始接单了">
                    <template #extra>
                      <el-button type="primary" round @click="$router.push('/counselor')" :icon="Right">进入工作台</el-button>
                    </template>
                  </el-result>
                </div>

                <div v-else-if="myCounselorApplication" class="apply-status-result">
                  <el-result icon="success" title="已开通咨询师权限" sub-title="您可直接进入咨询师工作台开始接单">
                    <template #extra>
                      <el-button type="primary" round @click="$router.push('/counselor')" :icon="Right">进入工作台</el-button>
                    </template>
                  </el-result>
                </div>

                <div v-else class="apply-form-wrapper">
                  <div class="apply-intro">
                    <div class="intro-item">
                      <el-icon class="intro-icon blue"><Monitor /></el-icon>
                      <div class="intro-content">
                        <h4>线上执业</h4>
                        <p>随时随地开展咨询工作，灵活安排排班</p>
                      </div>
                    </div>
                    <div class="intro-item">
                      <el-icon class="intro-icon green"><Wallet /></el-icon>
                      <div class="intro-content">
                        <h4>收益保障</h4>
                        <p>合理的定价体系，及时的报酬结算</p>
                      </div>
                    </div>
                  </div>

                  <el-form :model="applyForm" label-position="top" class="custom-apply-form">
                    <el-form-item label="擅长领域">
                      <el-input v-model="applyForm.expertise" placeholder="例如：婚姻情感、亲子教育、焦虑抑郁" />
                    </el-form-item>
                    <el-form-item label="专业简介">
                      <el-input v-model="applyForm.introduction" type="textarea" :rows="4" placeholder="请介绍您的受训背景、临床经验和咨询取向..." />
                    </el-form-item>
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <el-form-item label="咨询费用 (元/小时)">
                          <el-input-number v-model="applyForm.fee" :min="0" :step="50" style="width: 100%" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                    <el-form-item label="资质证明">
                      <el-upload
                        ref="qualificationUploadRef"
                        class="hidden-upload"
                        action="/api/files/upload"
                        :show-file-list="false"
                        :on-success="handleQualificationSuccess"
                        :before-upload="beforeQualificationUpload"
                      />
                      <div class="qualification-upload-box" @click="triggerQualificationUpload">
                        <img v-if="applyForm.qualification" :src="resolveAssetUrl(applyForm.qualification)" class="qualification-image" />
                        <div v-else class="qualification-placeholder">请上传资质证明图片</div>
                      </div>
                      <div class="form-tip">点击上方区域上传或更换图片（JPG/PNG，大小不超过 5MB）</div>
                    </el-form-item>
                    <el-button type="success" @click="submitApplication" :icon="Promotion" class="submit-apply-btn">提交入驻申请</el-button>
                  </el-form>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>


        <!-- Wallet Management -->
        <div v-if="activeTab === 'wallet'">
          <el-card shadow="hover" class="section-card">
             <template #header>
               <div class="section-header">
                 <h3><el-icon><Wallet /></el-icon> 我的钱包</h3>
               </div>
             </template>
             <div class="wallet-box">
               <div class="balance-card">
                 <div class="balance-title">当前余额</div>
                 <div class="balance-amount">¥ {{ user.balance || 0 }}</div>
                 <el-button type="primary" size="large" @click="topUpDialogVisible = true" :icon="Wallet">立即充值</el-button>
               </div>
               <div class="wallet-tips">
                 <h4><el-icon><InfoFilled /></el-icon> 充值说明</h4>
                 <p>1. 充值金额将用于支付咨询费用。</p>
                 <p>2. 预约咨询时将自动扣除相应费用。</p>
                 <p>3. 若预约被取消或拒绝，费用将自动退回您的账户。</p>
               </div>
             </div>

             <div class="transaction-history" style="margin-top: 30px;">
               <div class="section-header">
                   <h4><el-icon><List /></el-icon> 交易记录</h4>
               </div>
               <el-table :data="transactions" stripe style="width: 100%">
                   <el-table-column align="center" prop="createTime" label="时间" width="180">
                       <template #default="scope">
                           {{ formatDateTime(scope.row.createTime) }}
                       </template>
                   </el-table-column align="center">
                   <el-table-column align="center" prop="type" label="类型" width="120">
                       <template #default="scope">
                           <el-tag :type="scope.row.type === 'TOPUP' ? 'success' : (scope.row.type === 'REFUND' ? 'warning' : 'danger')">
                               {{ scope.row.type === 'TOPUP' ? '充值' : (scope.row.type === 'REFUND' ? '退款' : '消费') }}
                           </el-tag>
                       </template>
                   </el-table-column align="center">
                   <el-table-column align="center" prop="amount" label="金额">
                       <template #default="scope">
                           <span :style="{ color: scope.row.type === 'PAYMENT' ? 'red' : 'green', fontWeight: 'bold' }">
                               {{ scope.row.type === 'PAYMENT' ? '-' : '+' }}¥{{ scope.row.amount }}
                           </span>
                       </template>
                   </el-table-column align="center">
                   <el-table-column align="center" prop="description" label="说明" />
                   <el-table-column align="center" prop="status" label="状态" width="100">
                       <template #default="scope">
                           <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'" effect="plain">
                               {{ scope.row.status === 'SUCCESS' ? '成功' : '失败' }}
                           </el-tag>
                       </template>
                   </el-table-column align="center">
               </el-table>
             </div>
          </el-card>
        </div>

        <!-- Invite Management -->
        <div v-if="activeTab === 'invite'">
          <el-card shadow="hover" class="section-card">
            <template #header>
              <div class="section-header">
                <h3><el-icon><Present /></el-icon> 我的邀请</h3>
              </div>
            </template>

            <el-row :gutter="20">
              <el-col :span="10">
                <div class="invite-card">
                  <div class="invite-card-header">
                    <el-icon class="invite-icon"><Present /></el-icon>
                    <div class="invite-card-title">我的专属邀请码</div>
                  </div>
                  <div class="invite-code-box">
                    <span class="invite-code-text">{{ myInviteCode || '生成中...' }}</span>
                    <div class="invite-code-actions">
                      <el-button type="primary" @click="copyInviteCode" :icon="DocumentCopy" size="small">复制</el-button>
                      <el-button v-if="!myInviteCode" type="success" @click="generateInviteCode" :icon="Plus" size="small">生成</el-button>
                    </div>
                  </div>
                  <el-divider />
                  <div class="invite-tips">
                    <h4><el-icon><InfoFilled /></el-icon> 邀请规则</h4>
                    <p>1. 将您的专属邀请码分享给好友</p>
                    <p>2. 好友注册时填写邀请码即可绑定关系</p>
                    <p>3. 好友完成首次付费预约后,您将获得邀请奖励</p>
                    <p>4. 奖励金额以系统设置为准</p>
                  </div>
                </div>
              </el-col>

              <el-col :span="14">
                <div class="invite-stats">
                  <el-row :gutter="15">
                    <el-col :span="8">
                      <div class="stat-item blue">
                        <div class="stat-num">{{ myInvites.length }}</div>
                        <div class="stat-label">总邀请人数</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-item green">
                        <div class="stat-num">{{ completedInvites }}</div>
                        <div class="stat-label">已完成首单</div>
                      </div>
                    </el-col>
                    <el-col :span="8">
                      <div class="stat-item orange">
                        <div class="stat-num">¥{{ totalRebateAmount }}</div>
                        <div class="stat-label">累计奖励</div>
                      </div>
                    </el-col>
                  </el-row>
                </div>

                <div class="invite-list-box">
                  <div class="section-header" style="margin-bottom: 15px;">
                    <h4><el-icon><List /></el-icon> 邀请记录</h4>
                  </div>
                  <el-table :data="myInvites" stripe style="width: 100%">
                    <el-table-column align="center" label="被邀请人" min-width="180">
                      <template #default="scope">
                        <div style="display: flex; align-items: center; gap: 10px;">
                          <el-avatar :size="32" :src="getAvatar(scope.row.invitee?.avatar)" />
                          <span>{{ scope.row.invitee?.nickname || scope.row.invitee?.username || '-' }}</span>
                        </div>
                      </template>
                    </el-table-column>
                    <el-table-column align="center" prop="inviteCode" label="邀请码" width="140" />
                    <el-table-column align="center" prop="createTime" label="绑定时间" width="180">
                      <template #default="scope">
                        {{ formatDateTime(scope.row.createTime) }}
                      </template>
                    </el-table-column>
                    <el-table-column align="center" prop="status" label="状态" width="120">
                      <template #default="scope">
                        <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'warning'" effect="plain">
                          {{ scope.row.status === 'COMPLETED' ? '已完成' : '未完成' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column align="center" prop="rebateAmount" label="奖励金额" width="120">
                      <template #default="scope">
                        <span v-if="scope.row.rebateAmount" style="color: #67c23a; font-weight: bold;">
                          +¥{{ scope.row.rebateAmount }}
                        </span>
                        <span v-else>-</span>
                      </template>
                    </el-table-column>
                    <el-table-column align="center" prop="completeTime" label="完成时间" width="180">
                      <template #default="scope">
                        {{ scope.row.completeTime ? formatDateTime(scope.row.completeTime) : '-' }}
                      </template>
                    </el-table-column>
                  </el-table>
                  <el-empty v-if="myInvites.length === 0" description="暂无邀请记录" style="margin-top: 30px;" />
                </div>
              </el-col>
            </el-row>
          </el-card>
        </div>



      </el-main>
    </el-container>
  </el-container>

  <!-- Dialogs -->
  <el-dialog v-model="rateDialogVisible" title="评价咨询" width="440px" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <el-icon><Star /></el-icon>
          <span>评价您的咨询体验</span>
        </div>
      </template>
      <el-form :model="rateForm" label-position="top">
          <el-form-item label="总体评价">
              <el-rate 
                v-model="rateForm.rating" 
                show-score 
                text-color="#ff9900"
                score-template="{value} 分"
              />
          </el-form-item>
          <el-form-item label="详细反馈">
              <el-input 
                v-model="rateForm.feedback" 
                type="textarea" 
                :rows="4" 
                placeholder="分享您的咨询感受，帮助咨询师不断改进..." 
                maxlength="200"
                show-word-limit
              />
          </el-form-item>
      </el-form>
      <template #footer>
          <span class="dialog-footer">
              <el-button @click="rateDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="submitRate" :icon="Check">提交评价</el-button>
          </span>
      </template>
  </el-dialog>

  <el-dialog v-model="viewRateDialogVisible" title="评价详情" width="440px" destroy-on-close>
      <template #header>
        <div class="dialog-header">
          <el-icon><View /></el-icon>
          <span>我的评价详情</span>
        </div>
      </template>
      <div class="rate-detail-box">
          <div class="rate-row">
              <span class="rate-label">评分:</span>
              <el-rate v-model="currentRate.rating" disabled show-score />
          </div>
          <div class="rate-row">
              <span class="rate-label">反馈内容:</span>
              <div class="rate-text">{{ currentRate.feedback || '未填写评价内容' }}</div>
          </div>
          <div v-if="currentRate.counselorReply" class="counselor-reply-section">
              <div class="reply-header">
                  <el-icon><ChatLineRound /></el-icon>
                  <span>咨询师回复:</span>
              </div>
              <div class="reply-content">{{ currentRate.counselorReply }}</div>
          </div>
      </div>
  </el-dialog>

  <el-dialog v-model="chatDialogVisible" title="在线咨询" width="600px" class="chat-dialog" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><ChatDotRound /></el-icon>
        <span>在线咨询会话 - {{ currentChatTargetName }}</span>
      </div>
    </template>
    <div class="chat-box" ref="chatBoxRef">
        <div v-for="(msg, index) in chatMessages" :key="index" :class="['message', msg.senderId === user.id ? 'my-message' : 'other-message']">
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

  <el-dialog v-model="withdrawDialogVisible" title="提现申请" width="400px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><Wallet /></el-icon>
        <span>申请余额提现</span>
      </div>
    </template>
    <el-form label-position="top">
      <el-form-item label="提现金额">
        <el-input-number 
            v-model="withdrawAmount" 
            :min="1" 
            :max="myCounselorApplication?.totalRevenue" 
            style="width: 100%"
        >
             <template #suffix>元</template>
        </el-input-number>
        <div class="form-tip">当前可提现最高金额: <span class="amount-highlight">¥{{ myCounselorApplication?.totalRevenue || 0 }}</span></div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="withdrawDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitWithdraw" :icon="Check">提交申请</el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog v-model="bookingDialogVisible" title="预约咨询" width="460px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><Calendar /></el-icon>
        <span>预约咨询服务</span>
      </div>
    </template>
    <el-form label-position="top" class="booking-form">
      <div v-if="selectedCounselorInfo" class="selected-counselor-info">
          <el-avatar :size="50" :src="getAvatar(selectedCounselorInfo.user?.avatar)" />
          <div class="counselor-text">
              <div class="c-name">{{ selectedCounselorInfo.user?.nickname }}</div>
              <div class="c-fee">咨询费用: <span class="fee-val">¥{{ selectedCounselorInfo.fee }}</span> / 小时</div>
          </div>
      </div>
      
      <el-form-item label="1. 选择预约日期">
        <el-date-picker 
            v-model="bookingDate" 
            type="date" 
            placeholder="点击选择日期" 
            value-format="YYYY-MM-DD" 
            :disabled-date="disabledDate" 
            style="width: 100%" 
        />
      </el-form-item>
      <el-form-item label="2. 选择可用时间段">
        <el-select v-model="bookingTimeSlot" placeholder="请先选择日期" :disabled="!bookingDate" style="width: 100%">
            <el-option v-for="slot in filteredTimeSlots" :key="slot.value" :label="slot.label" :value="slot.value" />
        </el-select>
        <div v-if="filteredTimeSlots.length === 0 && bookingDate" class="no-slot-tip">
            <el-icon><Warning /></el-icon> 该日期暂无可用时段，请尝试其他日期
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="bookingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmBooking" :icon="Check" :disabled="!bookingTimeSlot">确认预约</el-button>
      </span>
    </template>
  </el-dialog>

  <el-dialog v-model="topUpDialogVisible" title="账户充值" width="400px" destroy-on-close>
    <template #header>
      <div class="dialog-header">
        <el-icon><Wallet /></el-icon>
        <span>账户余额充值</span>
      </div>
    </template>
    <el-form label-position="top">
      <el-form-item label="选择充值金额">
         <div class="amount-presets">
             <el-button v-for="amount in [50, 100, 200, 500, 1000]" :key="amount" 
                :type="topUpAmount === amount ? 'primary' : 'default'" 
                @click="topUpAmount = amount"
                size="small"
                plain
             >¥{{ amount }}</el-button>
         </div>
      </el-form-item>
      <el-form-item label="自定义金额">
        <el-input-number 
            v-model="topUpAmount" 
            :min="1" 
            :step="10" 
            style="width: 100%"
        >
             <template #prefix>¥</template>
        </el-input-number>
      </el-form-item>
    </el-form>
    <div class="payment-tip">
        <el-icon><Monitor /></el-icon> 安全支付环境 · 模拟支付
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="topUpDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitTopUp" :loading="topUpLoading" :icon="Check">立即支付</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue'
import { useUserStore } from '../../stores/user'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { formatDateTime, formatDate } from '../../utils/format'
import { 
  UserFilled, ArrowDown, Search, Calendar, User, 
  List, Edit, Promotion, Star, View, CircleClose, 
  ChatDotRound, Wallet, Check, Warning, ChatLineRound, Position,
  Camera, Postcard, Phone, Message, Male, Right, Monitor,
  Bell, InfoFilled, Present, DocumentCopy, Plus
} from '@element-plus/icons-vue'

import { getAvatar } from '../../utils/avatar'
import { resolveAssetUrl } from '../../utils/asset'

import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client/dist/sockjs'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('counselors')
const user = ref({ ...userStore.user })
const counselors = ref([])
const appointments = ref([])
const myCounselorApplication = ref(null)
const applyForm = ref({ expertise: '', introduction: '', fee: 0, qualification: '' })
const qualificationUploadRef = ref(null)
const myWithdrawals = ref([])
const withdrawDialogVisible = ref(false)
const withdrawAmount = ref(0)
const searchKeyword = ref('')

const bookingDialogVisible = ref(false)
const bookingDate = ref('')
const bookingTimeSlot = ref('')
const timeSlots = ['09:00', '10:00', '11:00', '12:00', '14:00', '15:00', '16:00', '17:00', '18:00']
const availableTimeSlots = ref([])
const availableDates = ref(new Set())

const selectedCounselorId = ref(null)
const selectedCounselorInfo = ref(null)

const rateDialogVisible = ref(false)
const rateForm = ref({ id: null, rating: 0, feedback: '' })
const viewRateDialogVisible = ref(false)
const currentRate = ref({ rating: 0, feedback: '' })

const chatDialogVisible = ref(false)
const chatMessages = ref([])
const chatInput = ref('')
const currentChatTargetId = ref(null)
const currentChatTargetName = ref('')
const chatBoxRef = ref(null)
let stompClient = null

// Wallet
const topUpDialogVisible = ref(false)
const topUpAmount = ref(100)
const topUpLoading = ref(false)
const transactions = ref([])

// Invite
const myInviteCode = ref('')
const myInvites = ref([])
const completedInvites = computed(() => myInvites.value.filter(i => i.status === 'COMPLETED').length)
const totalRebateAmount = computed(() => myInvites.value.reduce((sum, i) => sum + (i.rebateAmount || 0), 0))

const loadMyInviteInfo = async () => {
  try {
    const codeRes = await axios.get('/api/invite/my-code')
    if (codeRes.data.code === 200) {
      myInviteCode.value = codeRes.data.data
    }
    const listRes = await axios.get('/api/invite/my-invites')
    if (listRes.data.code === 200) {
      myInvites.value = listRes.data.data || []
    }
  } catch (e) {
    // ignore
  }
}

const copyInviteCode = async () => {
  if (!myInviteCode.value) {
    ElMessage.warning('请先生成邀请码')
    return
  }
  try {
    await navigator.clipboard.writeText(myInviteCode.value)
    ElMessage.success('邀请码已复制')
  } catch (e) {
    ElMessage.error('复制失败,请手动复制')
  }
}

const generateInviteCode = async () => {
  try {
    const res = await axios.get('/api/invite/my-code')
    if (res.data.code === 200) {
      myInviteCode.value = res.data.data
      ElMessage.success('邀请码生成成功')
    }
  } catch (e) {
    ElNotification.error('生成邀请码失败')
  }
}

const loadTransactions = async () => {
    try {
        const res = await axios.get('/api/wallet/transactions')
        transactions.value = res.data.data
    } catch (e) {
        ElNotification.error('交易记录加载失败')
    }
}

const submitTopUp = async () => {
    if (topUpAmount.value <= 0) {
        ElMessage.warning('充值金额必须大于0')
        return
    }
    topUpLoading.value = true
    try {
        // Simulate payment delay
        await new Promise(resolve => setTimeout(resolve, 1500))
        
        await axios.post(`/api/wallet/topup?amount=${topUpAmount.value}`)
        ElMessage.success('充值成功')
        topUpDialogVisible.value = false
        // Refresh user data to get new balance
        const userRes = await axios.get(`/api/users/${userStore.user.id}`)
        user.value = userRes.data.data
        userStore.setUser(user.value)
        loadTransactions()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    } finally {
        topUpLoading.value = false
    }
}

// Notifications

const notifications = ref([])
const unreadCount = ref(0)
const notificationDialogVisible = ref(false)

const loadNotifications = async () => {
    try {
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

const handleAvatarSuccess = (response, uploadFile) => {
    if (response.code === 200) {
        user.value.avatar = response.data
        // Sync with Pinia store immediately for real-time header update
        userStore.setUser({ ...userStore.user, avatar: response.data })
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

const handleQualificationSuccess = (response, uploadFile) => {
    if (response.code === 200) {
        applyForm.value.qualification = response.data
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

const triggerQualificationUpload = () => {
    qualificationUploadRef.value?.$el?.querySelector('input')?.click()
}

const menuTitle = {
    'counselors': '咨询师列表',
    'appointments': '我的预约',
    'wallet': '我的钱包',
    'profile': '个人信息',
    'invite': '我的邀请'
}

const filteredCounselors = computed(() => {
    if (!searchKeyword.value) return counselors.value
    return counselors.value.filter(c => c.user.nickname.includes(searchKeyword.value) || c.expertise.includes(searchKeyword.value))
})

const handleCommand = (command) => {
    if (command === 'logout') logout()
    if (command === 'profile') activeTab.value = 'profile'
}

const logout = () => {
  if (stompClient) stompClient.deactivate()
  userStore.logout()
  router.push('/login')
}

const loadData = async () => {
  if (!userStore.user?.id) {
    return
  }
  
  // Sync local user ref if not editing
  if (!isEditing.value) {
    user.value = { ...userStore.user }
  }

  try {
    // Refresh user info
    try {
        const userRes = await axios.get(`/api/users/${userStore.user.id}`)
        if (userRes.data.code === 200) {
            const freshUser = userRes.data.data
            userStore.setUser(freshUser)
            if (!isEditing.value) {
                 user.value = { ...freshUser }
            }
        }
    } catch (err) {
        ElNotification.error('用户信息刷新失败')
    }

    const counselorRes = await axios.get('/api/counselors')
    const allCounselors = counselorRes.data.data || []
    counselors.value = allCounselors.filter(c => c.status === 'APPROVED')
    
    myCounselorApplication.value = allCounselors.find(c => c.user?.id === userStore.user.id)

    if (myCounselorApplication.value) {
        loadWithdrawals()
    }

    // Load transactions for wallet
    loadTransactions()

    // Load invite info
    loadMyInviteInfo()

    const apptRes = await axios.get('/api/appointments/my')
    appointments.value = apptRes.data.data || []
    
    initWebSocket()
    loadNotifications()
  } catch (e) {
    ElNotification.error((e.response?.data?.message || '页面数据加载失败'))
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

const isEditing = ref(false)

const avatarUploadRef = ref(null)

const triggerUpload = () => {
  avatarUploadRef.value?.$el.querySelector('input').click()
}

const updateProfile = async () => {
  try {
    // Create a copy and remove sensitive/read-only fields if necessary
    const userData = { ...user.value }
    // Ensure we don't send password if it's empty (though backend ignores it usually)
    delete userData.password 
    
    // Handle empty birthday string to avoid 400 Bad Request
    if (!userData.birthday) {
        userData.birthday = null
    }
    
    // Regex Validation
    const phoneRegex = /^1[3-9]\d{9}$/
    if (userData.phone && !phoneRegex.test(userData.phone)) {
        ElNotification.error('手机号格式不正确')
        return
    }
    
    const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/
    if (userData.email && !emailRegex.test(userData.email)) {
        ElNotification.error('邮箱格式不正确')
        return
    }

    const res = await axios.put(`/api/users/${user.value.id}`, userData)
    if (res.data.code === 200) {
        const updatedUser = res.data.data
        userStore.setUser(updatedUser)
        user.value = { ...updatedUser }
        ElMessage.success('个人信息更新成功')
        isEditing.value = false
    } else {
        ElNotification.error(res.data.message || '更新失败')
    }
  } catch (e) {
    ElNotification.error((e.response?.data?.message || '服务器错误'))
  }
}

const cancelEdit = () => {
    user.value = { ...userStore.user }
    isEditing.value = false
}

const submitApplication = async () => {
    if (!applyForm.value.introduction) {
        ElNotification.error('请填写个人简介')
        return
    }
    if (!applyForm.value.expertise) {
        ElNotification.error('请填写擅长领域')
        return
    }
    if (!applyForm.value.fee || applyForm.value.fee <= 0) {
        ElNotification.error('请填写有效的期望费用')
        return
    }
    if (!applyForm.value.qualification) {
        ElNotification.error('请上传资质证明')
        return
    }
    try {
        const res = await axios.post('/api/counselors/apply', applyForm.value)
        if(res.data.code === 200) {
            const userRes = await axios.get(`/api/users/${user.value.id}`)
            if (userRes.data.code === 200) {
                user.value = userRes.data.data
                userStore.setUser(user.value)
            }
            ElMessage.success('已开通咨询师权限')
            router.push('/counselor')
        } else {
            ElNotification.error(res.data.message || '申请失败')
        }
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const loadAvailableSlots = async (counselorId) => {
    // Reset before loading
    availableTimeSlots.value = []
    availableDates.value = new Set()
    
    try {
        const res = await axios.get(`/api/timeslots/counselor/${counselorId}/available`)
        availableTimeSlots.value = res.data.data || []
        
        // Extract unique dates
        const dates = new Set()
        availableTimeSlots.value.forEach(slot => {
            const dateTime = toApiLocalDateTime(slot.startTime)
            if (dateTime) {
                dates.add(dateTime.split('T')[0])
            }
        })
        availableDates.value = dates
    } catch (e) {
        ElNotification.error('获取可用时段失败')
    }
}

const bookAppointment = async (counselor) => {
    selectedCounselorId.value = counselor.id
    selectedCounselorInfo.value = counselor
    bookingDate.value = ''
    bookingTimeSlot.value = ''
    
    await loadAvailableSlots(counselor.id)
    
    bookingDialogVisible.value = true
}

const disabledDate = (time) => {
    // Disable past dates
    if (time.getTime() < Date.now() - 8.64e7) return true
    
    // Disable dates without available slots
    const date = new Date(time)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const dateStr = `${year}-${month}-${day}`
    
    return !availableDates.value.has(dateStr)
}

const filteredTimeSlots = computed(() => {
    if (!bookingDate.value) return []
    const now = Date.now()
    return availableTimeSlots.value.filter(slot => {
        const startTime = toApiLocalDateTime(slot.startTime)
        const startAt = parseApiLocalDateTime(slot.startTime)
        return startTime.startsWith(bookingDate.value) && startAt && startAt.getTime() > now
    }).map(slot => {
        const timeStrStart = toApiLocalDateTime(slot.startTime)
        const timeStrEnd = toApiLocalDateTime(slot.endTime)
        const start = timeStrStart.split('T')[1].substring(0, 5)
        const end = timeStrEnd.split('T')[1].substring(0, 5)
        return {
            label: `${start} - ${end}`,
            value: timeStrStart
        }
    })
})

const confirmBooking = async () => {
    if (!bookingDate.value || !bookingTimeSlot.value) {
        ElMessage.warning('请选择日期和时间')
        return
    }
    
    const appointmentTime = toApiLocalDateTime(bookingTimeSlot.value)
    const appointmentDate = parseApiLocalDateTime(appointmentTime)
    if (!appointmentDate || appointmentDate.getTime() <= Date.now()) {
        ElMessage.warning('请选择将来的可用时间段')
        return
    }

    try {
        await axios.post('/api/appointments', {
            userId: user.value.id,
            counselorId: selectedCounselorId.value,
            appointmentTime: appointmentTime
        })
        ElMessage.success('预约咨询成功')
        bookingDialogVisible.value = false
        // Refresh to update balance
        const userRes = await axios.get(`/api/users/${user.value.id}`)
        user.value = userRes.data.data
        userStore.setUser(user.value)
        
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const toApiLocalDateTime = (value) => {
    if (!value) return ''
    const raw = String(value).trim()
    const hasTimeZone = raw.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(raw)

    if (!hasTimeZone) {
        return raw.replace(' ', 'T').replace(/\.\d+$/, '')
    }

    const date = new Date(raw)
    if (Number.isNaN(date.getTime())) {
        return raw.replace(' ', 'T').replace(/\.\d+$/, '')
    }

    const pad = (n) => String(n).padStart(2, '0')
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const parseApiLocalDateTime = (value) => {
    const normalized = toApiLocalDateTime(value)
    if (!normalized) return null
    const date = new Date(normalized)
    return Number.isNaN(date.getTime()) ? null : date
}

const openRate = (appointment) => {
    rateForm.value = { id: appointment.id, rating: 5, feedback: '' }
    rateDialogVisible.value = true
}

const submitRate = async () => {
    try {
        await axios.put(`/api/appointments/${rateForm.value.id}/rate`, {
            rating: rateForm.value.rating,
            feedback: rateForm.value.feedback
        })
        ElMessage.success('评价成功')
        rateDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error('评价失败')
    }
}

const viewRate = (appointment) => {
    currentRate.value = { rating: appointment.rating, feedback: appointment.feedback }
    viewRateDialogVisible.value = true
}

const cancelAppointment = async (id) => {
    try {
        await ElMessageBox.confirm(
            '确定要取消该预约吗？取消后预约费用将全额退回您的账户余额。',
            '操作确认',
            {
                confirmButtonText: '确认取消',
                cancelButtonText: '暂不取消',
                type: 'warning',
                center: true,
                draggable: true
            }
        )
        await axios.put(`/api/appointments/${id}/status?status=CANCELLED`)
        ElMessage.success('预约已取消')
        loadData()
    } catch (e) {
        if (e !== 'cancel') {
            ElNotification.error((e.response?.data?.message || '服务器错误'))
        }
    }
}

const initWebSocket = () => {
    // Prevent duplicate connections
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
            stompClient.subscribe(`/queue/messages/${user.value.id}`, (message) => {
                const msg = JSON.parse(message.body)
                if ((msg.senderId === currentChatTargetId.value || msg.senderId === user.value.id) && chatDialogVisible.value) {
                    if (!chatMessages.value.some(m => m.timestamp === msg.timestamp && m.content === msg.content)) {
                        chatMessages.value.push(msg)
                        nextTick(scrollToBottom)
                    }
                } else {
                    ElMessage.info(`收到新消息`)
                }
            })

            // Notification Subscription
            stompClient.subscribe(`/queue/notifications/${user.value.id}`, (message) => {
                const notification = JSON.parse(message.body)
                notifications.value.unshift(notification)
                unreadCount.value++
                ElMessage.info('新通知: ' + notification.content)
            })
        },
        onStompError: (frame) => {
            ElNotification.error(frame.headers['message'] || '聊天服务异常')
        }
    })
    stompClient.activate()
}

const openChat = async (target) => {
    const targetId = typeof target === 'object' ? target.id : target
    currentChatTargetId.value = targetId
    currentChatTargetName.value = typeof target === 'object' ? (target.nickname || target.username) : '咨询师'
    chatDialogVisible.value = true
    try {
        const res = await axios.get(`/api/messages/${user.value.id}/${targetId}`)
        chatMessages.value = res.data.data
        nextTick(() => {
            if (chatBoxRef.value) {
                chatBoxRef.value.scrollTop = chatBoxRef.value.scrollHeight
            }
        })
    } catch (e) {
        ElNotification.error('聊天记录加载失败')
    }
}

const sendMessage = () => {
    if (!chatInput.value.trim()) return
    
    if (!stompClient || !stompClient.active) {
        ElNotification.error('聊天服务未连接，正在尝试重新连接...')
        initWebSocket()
        return
    }

    const message = {
        senderId: user.value.id,
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

const submitWithdraw = async () => {
    if (withdrawAmount.value <= 0) {
        ElNotification.error('请输入有效的提现金额')
        return
    }
    try {
        await axios.post('/api/withdrawals/request', {
            counselorId: myCounselorApplication.value?.id,
            amount: withdrawAmount.value
        })
        ElMessage.success('提现申请已提交，请耐心等待管理员处理')
        withdrawDialogVisible.value = false
        loadData()
    } catch (e) {
        ElNotification.error((e.response?.data?.message || '服务器错误'))
    }
}

const statusMap = { 'PENDING': '待审核', 'APPROVED': '已通过', 'REJECTED': '已拒绝', 'CONFIRMED': '已确认', 'COMPLETED': '已完成', 'CANCELLED': '已取消' }
const formatStatus = (status) => statusMap[status] || status
const getStatusType = (status) => {
    if (['APPROVED', 'COMPLETED', 'CONFIRMED'].includes(status)) return 'success'
    if (['REJECTED', 'CANCELLED'].includes(status)) return 'danger'
    if (['PENDING'].includes(status)) return 'warning'
    return 'info'
}

onMounted(loadData)
</script>

<style scoped>
:root {
  --primary-color: #4361ee;
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
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-menu-vertical :deep(.el-menu-item.is-active) {
  background: linear-gradient(90deg, #4361ee 0%, #4cc9f0 100%) !important;
  box-shadow: 0 4px 15px rgba(67, 97, 238, 0.3);
  color: white !important;
}

.header {
  position: sticky;
  top: 0;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 40px;
  height: 70px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
  z-index: 100;
}

.header-right {
  display: flex;
  align-items: center;
}

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
  margin-left: 12px;
  font-size: 14px;
  letter-spacing: 0.3px;
  display: flex;
  align-items: center;
}

.main-content {
  padding: 30px;
  background-color: #f8f9fa;
}

.counselor-card {
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.6);
  overflow: hidden;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  box-shadow: 0 10px 25px rgba(0,0,0,0.05);
  background: white;
  position: relative;
}

.counselor-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 40px rgba(67, 97, 238, 0.15) !important;
  border-color: rgba(67, 97, 238, 0.2);
}

.counselor-banner {
  height: 100px;
  background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
}

.counselor-info-box {
  padding: 0 25px 25px;
  text-align: center;
  margin-top: -45px;
}

.counselor-avatar {
  border: 5px solid white;
  box-shadow: 0 5px 15px rgba(0,0,0,0.1);
  margin-bottom: 15px;
}

.counselor-name {
  font-size: 20px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 10px;
}

.section-card {
  border-radius: 24px;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 10px 30px rgba(0,0,0,0.04);
  overflow: hidden;
}

.appointment-table {
  width: 100%;
}

.section-header h3 {
  font-size: 20px;
  font-weight: 800;
  color: #2d3748;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* Dialog & Form Revamp */
:deep(.el-dialog) {
  border-radius: 28px;
  overflow: hidden;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  background-color: #f8f9fa;
  border: 1px solid #edf2f7;
  box-shadow: none !important;
}

:deep(.el-button--primary) {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  height: auto;
}

.avatar-uploader :deep(.el-upload) {
  border: 2px dashed #edf2f7;
  border-radius: 20px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
  width: 120px;
  height: 120px;
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
  font-size: 28px;
  color: #a0aec0;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

/* Profile Section Enhancements */
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
}

.profile-main-card {
  border-radius: 24px;
  overflow: hidden;
  position: relative;
}

.profile-header-bg {
  height: 120px;
  background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
}

.profile-content {
  padding: 0 30px 30px;
  margin-top: -50px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 15px;
}

.main-avatar {
  border: 4px solid white;
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
}

.avatar-edit-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.4);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  font-size: 24px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-edit-mask {
  opacity: 1;
}

.name-section {
  text-align: center;
}

.display-name {
  font-size: 24px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 5px;
}

.role-tag {
  font-weight: 600;
}

.profile-info-list {
  background: #f8f9fa;
  border-radius: 20px;
  padding: 20px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #edf2f7;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #718096;
  font-size: 14px;
}

.info-value {
  color: #2d3748;
  font-weight: 600;
}

.profile-actions {
  margin-top: 25px;
  text-align: center;
}

.profile-edit-form {
  padding: 20px 0;
}

.hidden-upload {
  display: none;
}

.edit-footer {
  display: flex;
  gap: 15px;
  justify-content: center;
  margin-top: 30px;
}

/* Apply Counselor Card */
.apply-counselor-card {
  border-radius: 24px;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 800;
  margin-bottom: 5px;
}

.header-subtitle {
  color: #718096;
  font-size: 14px;
}

.apply-intro {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.intro-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 16px;
}

.intro-icon {
  font-size: 24px;
  padding: 12px;
  border-radius: 12px;
}

.intro-icon.blue { background: rgba(67, 97, 238, 0.1); color: #4361ee; }
.intro-icon.green { background: rgba(72, 187, 120, 0.1); color: #48bb78; }

.intro-content h4 {
  margin-bottom: 4px;
  color: #2d3748;
}

.intro-content p {
  font-size: 12px;
  color: #718096;
}

.qualification-upload-box {
  position: relative;
  width: 100%;
  max-width: 360px;
  height: 180px;
  border: 1px dashed #d9d9d9;
  border-radius: 12px;
  background: #fbfdff;
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, background-color 0.2s ease;
}

.qualification-upload-box:hover {
  border-color: #409eff;
  background-color: #f5f7fa;
}

.qualification-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.qualification-placeholder {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 14px;
}

.form-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.submit-apply-btn {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  margin-top: 20px;
}

.apply-status-progress {
  padding: 20px 0;
}

.status-banner {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px;
  border-radius: 20px;
  margin-bottom: 40px;
}

.status-banner.pending { background: rgba(236, 158, 28, 0.1); color: #ec9e1c; }
.status-banner.approved { background: rgba(72, 187, 120, 0.1); color: #48bb78; }
.status-banner.rejected { background: rgba(245, 108, 108, 0.1); color: #f56c6c; }

.status-icon { font-size: 32px; }
.status-title { font-size: 18px; font-weight: 800; margin-bottom: 4px; }
.status-desc { font-size: 14px; opacity: 0.8; }

.custom-steps {
  margin: 0 20px;
}


/* Wallet Styles */
.wallet-box {
    display: flex;
    gap: 30px;
}

.balance-card {
    flex: 1;
    background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
    border-radius: 20px;
    padding: 30px;
    color: white;
    text-align: center;
    box-shadow: 0 10px 20px rgba(67, 97, 238, 0.3);
}

.balance-title {
    font-size: 16px;
    opacity: 0.9;
    margin-bottom: 10px;
}

.balance-amount {
    font-size: 48px;
    font-weight: 800;
    margin-bottom: 20px;
}

.wallet-tips {
    flex: 1.5;
    background: #f8f9fa;
    border-radius: 20px;
    padding: 25px;
}

.wallet-tips h4 {
    margin-bottom: 15px;
    display: flex;
    align-items: center;
    gap: 8px;
    color: #4361ee;
}

.wallet-tips p {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 8px;
}

/* Notification Styles */
.notification-badge {
    margin-right: 25px;
    cursor: pointer;
    color: #606266;
    display: flex;
    align-items: center;
}

.notification-badge:hover {
    color: #4361ee;
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

/* Table Actions - Keep buttons in one row */
.table-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: nowrap;
}

.table-actions :deep(.el-button) {
  margin-left: 0 !important;
  padding: 4px 8px;
}

.amount-presets {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.payment-tip {
  margin-top: 20px;
  background: #f0f9eb;
  color: #67c23a;
  padding: 10px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}

.booking-form {
  padding: 10px 0;
}

.booking-form .el-form-item__label {
  font-weight: 600;
  color: #2d3748;
}

.booking-form :deep(.el-date-editor.el-input),
.booking-form :deep(.el-select) {
  width: 100% !important;
}

.booking-form :deep(.el-input__wrapper) {
  height: 45px;
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

/* Rating Component Polish */
:deep(.el-rate) {
  --el-rate-fill-color: #f59e0b;
  --el-rate-void-color: #e2e8f0;
  overflow: visible;
  display: inline-flex;
  align-items: center;
}

:deep(.el-rate__item) {
  transition: transform 0.2s ease;
}

:deep(.el-rate__item:hover) {
  transform: scale(1.2);
}

/* Edit links/buttons */
.table-actions .el-button--warning.is-link,
.table-actions .el-button--success.is-link {
  font-weight: 600 !important;
  font-size: 13px !important;
  padding: 4px 8px !important;
  border-radius: 8px !important;
  transition: all 0.2s !important;
}

.table-actions .el-button--warning.is-link:hover {
  background: rgba(245, 158, 11, 0.1) !important;
}

.table-actions .el-button--success.is-link:hover {
  background: rgba(34, 197, 94, 0.1) !important;
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

/* Invite Section */
.invite-card {
  background: white;
  border-radius: 20px;
  padding: 25px;
  height: 100%;
}

.invite-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.invite-icon {
  font-size: 28px;
  color: #4361ee;
}

.invite-card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a202c;
}

.invite-code-box {
  background: linear-gradient(135deg, #4361ee 0%, #4cc9f0 100%);
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
}

.invite-code-text {
  font-size: 32px;
  font-weight: 800;
  color: white;
  letter-spacing: 6px;
  display: block;
  margin-bottom: 20px;
  text-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.invite-code-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.invite-code-actions :deep(.el-button) {
  background: rgba(255,255,255,0.2);
  border: 1px solid rgba(255,255,255,0.3);
  color: white;
}

.invite-code-actions :deep(.el-button:hover) {
  background: rgba(255,255,255,0.3);
  color: white;
}

.invite-tips {
  margin-top: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  padding: 20px;
}

.invite-tips h4 {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #4361ee;
  font-size: 15px;
  margin-bottom: 12px;
}

.invite-tips p {
  font-size: 13px;
  color: #606266;
  line-height: 2;
  margin-bottom: 4px;
}

.invite-stats {
  margin-bottom: 25px;
}

.invite-stats .stat-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}

.invite-stats .stat-item.blue { border-top: 3px solid #4361ee; }
.invite-stats .stat-item.green { border-top: 3px solid #48bb78; }
.invite-stats .stat-item.orange { border-top: 3px solid #ed8936; }

.invite-stats .stat-num {
  font-size: 28px;
  font-weight: 800;
  color: #1a202c;
  margin-bottom: 5px;
}

.invite-stats .stat-label {
  font-size: 13px;
  color: #718096;
}

.invite-list-box {
  background: white;
  border-radius: 16px;
  padding: 20px;
}
</style>
