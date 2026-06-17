<template>
  <app-shell content-style="padding: 0 24rpx 32rpx;" :show-tab-bar="true" :current-tab="2">
    <template #menu>
      <view class="my-header" :class="{ guest: !isLoggedIn }">
        <view class="my-header-bg">
          <view class="bg-blob bg-blob-1"></view>
          <view class="bg-blob bg-blob-2"></view>
          <view class="bg-blob bg-blob-3"></view>
        </view>

        <view v-if="isLoggedIn" class="my-toolbar">
          <view class="toolbar-title-group">
            <text class="toolbar-title">个人中心</text>
            <text class="toolbar-subtitle">账号、家庭和审核消息</text>
          </view>
          <view class="toolbar-actions">
            <view class="toolbar-action reserved"></view>
            <view class="toolbar-action reserved"></view>
            <view
              class="toolbar-action notify-bell"
              hover-class="toolbar-action--hover"
              :hover-stay-time="80"
              @click="goToNotifications"
            >
              <AppIcon name="bell" size="md" tone="inherit" />
              <text v-if="pendingCount > 0" class="notify-badge">{{ pendingCount }}</text>
            </view>
          </view>
        </view>

        <view
          v-if="isLoggedIn"
          class="my-profile"
          @click="handleProfileClick"
        >
          <view class="my-avatar">
            <image v-if="user.avatar" class="profile-avatar-image" :src="user.avatar" mode="aspectFill" />
            <MemberAvatar v-else :text="avatarInitial" size="xl" ring />
          </view>
          <view class="my-profile-info">
            <view class="my-name-row">
              <text class="my-name">{{ user.nickname || maskedPhone }}</text>
              <RoleChip
                :text="isMeCook ? '今日主厨' : '今日食客'"
                :tone="isMeCook ? 'cook' : 'eat'"
                dot
              />
            </view>
            <text class="my-subtitle">{{ maskedPhone }}</text>
          </view>
          <AppIcon name="chevron_right" size="lg" tone="muted" class="my-chevron" />
        </view>
      </view>
    </template>

    <!-- 未登录：合并为一张高级 hero 空状态卡 -->
    <view v-if="!isLoggedIn" class="guest-hero">
      <view class="guest-hero-bg">
        <view class="hero-blob hero-blob-1"></view>
        <view class="hero-blob hero-blob-2"></view>
      </view>

      <view class="guest-avatar-wrap">
        <view class="guest-avatar-ring"></view>
        <view class="guest-avatar">
          <view class="avatar-figure">
            <view class="avatar-head"></view>
            <view class="avatar-body"></view>
          </view>
        </view>
        <view class="avatar-badge">
          <text>?</text>
        </view>
      </view>

      <text class="guest-title">登录后开启更多功能</text>
      <text class="guest-desc">加入或创建你的家庭 · 一起好好吃饭</text>

      <view class="guest-feature-list">
        <view class="feature-item">
          <view class="feature-dot dot-1"></view>
          <text class="feature-text">家庭共享菜单</text>
        </view>
        <view class="feature-item">
          <view class="feature-dot dot-2"></view>
          <text class="feature-text">轮换主厨 · 今日我做</text>
        </view>
        <view class="feature-item">
          <view class="feature-dot dot-3"></view>
          <text class="feature-text">订单和修改申请同步</text>
        </view>
      </view>

      <view class="guest-cta" hover-class="guest-cta--hover" :hover-stay-time="80" @click="goToLogin">
        <text class="guest-cta-text">立即登录</text>
        <text class="guest-cta-arrow">›</text>
      </view>
    </view>

    <!-- 已登录 -->
    <view v-else>
      <view v-if="pageLoading" class="my-skeleton">
        <SkeletonBox width="100%" height="180rpx" radius="24rpx" />
        <SkeletonBox width="100%" height="160rpx" radius="24rpx" />
        <SkeletonBox width="100%" height="320rpx" radius="24rpx" />
      </view>

      <view v-else class="my-content">
      <!-- 家庭信息卡 -->
      <view class="family-card" @click="goToFamilyManage">
        <view class="family-head">
          <view class="family-icon">
            <AppIcon name="home" size="lg" tone="inherit" :bg="false" />
          </view>
          <view class="family-info">
            <text class="family-name">{{ familyName || '暂无家庭' }}</text>
            <text class="family-sub">{{ familyCode || '未绑定家庭' }} · {{ members.length }} 位家人</text>
          </view>
          <view v-if="receivedInviteCount > 0" class="family-invite-badge">
            <text>{{ receivedInviteCount }} 条邀请</text>
          </view>
          <view class="family-share" @click.stop="copyFamilyCode">
            <AppIcon name="copy" size="sm" tone="primary" style="width: 28rpx; height: 28rpx;" />
            <text>复制码</text>
          </view>
        </view>

        <view class="members-row">
          <view
            v-for="m in members"
            :key="m.userId"
            class="member-chip"
            :class="{ me: m.userId === currentUserId, cook: m.userId === todayCookUserId }"
          >
            <MemberAvatar
              :src="m.avatar"
              :text="(m.nickname || m.phone).charAt(0)"
              size="sm"
              :badge="m.userId === todayCookUserId ? '厨' : ''"
              :badge-tone="m.userId === todayCookUserId ? 'primary' : 'primary'"
            />
            <text class="member-name">{{ m.nickname || maskPhone(m.phone) }}</text>
          </view>
        </view>
      </view>

      <!-- 本周数据 -->
      <view class="stats-card">
        <view class="stat-item">
          <view class="stat-icon-wrap stat-cook">
            <AppIcon name="pot" size="lg" tone="inherit" />
          </view>
          <text class="stat-value">{{ weeklyStats.cookDays }}</text>
          <text class="stat-label">本周做饭</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon-wrap stat-dish">
            <AppIcon name="dish" size="lg" tone="inherit" />
          </view>
          <text class="stat-value">{{ weeklyStats.dishCount }}</text>
          <text class="stat-label">本周菜品</text>
        </view>
        <view class="stat-divider"></view>
        <view class="stat-item">
          <view class="stat-icon-wrap stat-order">
            <AppIcon name="order" size="lg" tone="inherit" />
          </view>
          <text class="stat-value">{{ weeklyStats.orderCount }}</text>
          <text class="stat-label">本周订单</text>
        </view>
      </view>

      <!-- 今日身份快捷切换 -->
      <view v-if="todayCook" class="today-cook-card">
        <view class="tcc-left">
          <view class="tcc-dot" :class="{ mine: isMeCook }"></view>
          <view class="tcc-text">
            <text class="tcc-title">{{ isMeCook ? '今天轮到你下厨' : '今天 TA 来下厨' }}</text>
            <text class="tcc-desc">{{ todayCookName }} · {{ todayDateStr }}</text>
          </view>
        </view>
        <view v-if="!isMeCook" class="tcc-action" @click="switchToMe">
          <text>换我</text>
        </view>
        <view v-else class="tcc-action done">
          <text>✓ 在岗</text>
        </view>
      </view>

      <view class="menu-card">
        <view class="menu-row" @click="goToOrder">
          <view class="menu-row-icon icon-order">
            <AppIcon name="order" size="lg" tone="inherit" />
          </view>
          <view class="menu-row-text">
            <text class="row-title">我的订单</text>
            <text class="row-desc">查看历史点餐记录</text>
          </view>
          <AppIcon name="chevron_right" size="md" tone="muted" class="row-chevron" />
        </view>
        <view class="menu-row" @click="goToApprove">
          <view class="menu-row-icon icon-approve">
            <AppIcon name="approve" size="lg" tone="inherit" />
          </view>
          <view class="menu-row-text">
            <text class="row-title">审核申请</text>
            <text class="row-desc">{{ pendingCount > 0 ? '有需要你处理的家庭消息' : '查看菜品修改和家庭加入申请' }}</text>
          </view>
          <view class="row-right">
            <text v-if="pendingCount > 0" class="badge">{{ pendingCount }}</text>
            <AppIcon name="chevron_right" size="md" tone="muted" class="row-chevron" />
          </view>
        </view>
        <view class="menu-row" @click="goToMyDishes">
          <view class="menu-row-icon icon-mine">
            <AppIcon name="dish" size="lg" tone="inherit" />
          </view>
          <view class="menu-row-text">
            <text class="row-title">我发布的菜品</text>
            <text class="row-desc">查看历史发布的所有菜品</text>
          </view>
          <AppIcon name="chevron_right" size="md" tone="muted" class="row-chevron" />
        </view>
        <view class="menu-row logout" @click="logout">
          <view class="menu-row-icon icon-logout">
            <AppIcon name="logout" size="lg" tone="inherit" />
          </view>
          <view class="menu-row-text">
            <text class="row-title">退出登录</text>
            <text class="row-desc">切换手机号或重新登录</text>
          </view>
          <AppIcon name="chevron_right" size="md" tone="muted" class="row-chevron" />
        </view>
      </view>

      <view class="footer-brand">
        <text>Eat · 一家人的餐桌</text>
      </view>
      </view>
    </view>
  </app-shell>
</template>

<script>
import AppShell from '@/components/AppShell.vue'
import AppIcon from '@/components/AppIcon.vue'
import MemberAvatar from '@/components/MemberAvatar.vue'
import RoleChip from '@/components/RoleChip.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import {
  getCurrentFamily,
  getFamilyMembers,
  getTodayCook,
  switchCook,
  getPendingRequests,
  getFamilyDishes,
  getOrdersByFamily,
  getReceivedFamilyInvitations,
  getUserFamilies,
  getFamilyInvitations,
  getNotifications,
  WS_NOTIFICATION_URL
} from '@/utils/api.js'
import { formatYMD } from '@/utils/date.js'
import { info, error, warn } from '@/utils/toast.js'

export default {
  components: {
    AppShell,
    AppIcon,
    MemberAvatar,
    RoleChip,
    SkeletonBox
  },
  data() {
    return {
      user: null,
      pageLoading: true,
      family: null,
      familyName: '',
      familyCode: '',
      members: [],
      todayCook: null,
      pendingCount: 0,
      dishPendingCount: 0,
      joinPendingCount: 0,
      receivedInviteCount: 0,
      weeklyStats: { cookDays: 0, dishCount: 0, orderCount: 0 },
      todayDateStr: '',
      notificationSocket: null,
      socketUserId: ''
    }
  },
  computed: {
    isLoggedIn() { return !!this.user },
    currentUserId() { return this.user ? this.user.userId : '' },
    todayCookUserId() { return this.todayCook ? this.todayCook.cookUserId : '' },
    isMeCook() { return this.todayCookUserId === this.currentUserId },
    todayCookName() {
      if (!this.todayCook) return ''
      const m = this.members.find(u => u.userId === this.todayCook.cookUserId)
      return m ? (m.nickname || this.maskPhone(m.phone)) : this.todayCook.cookUserId
    },
    avatarInitial() {
      const name = (this.user && (this.user.nickname || this.user.phone)) || '家'
      return name.charAt(0)
    },
    maskedPhone() {
      return this.user ? this.maskPhone(this.user.phone) : ''
    }
  },
  onShow() {
    this.loadSession()
  },
  onHide() {
    this.closeNotificationSocket()
  },
  onUnload() {
    this.closeNotificationSocket()
  },
  methods: {
    loadSession() {
      const user = uni.getStorageSync('currentUser')
      this.user = user || null
      this.familyName = uni.getStorageSync('familyName') || ''
      this.familyCode = uni.getStorageSync('familyCode') || ''
      const today = new Date()
      this.todayDateStr = `${today.getMonth() + 1}月${today.getDate()}日`
      if (!this.user) {
        this.closeNotificationSocket()
        this.resetFamilyState()
        this.pageLoading = false
        return
      }
      this.connectNotificationSocket()
      this.loadAll()
    },
    async loadAll() {
      this.pageLoading = true
      await this.loadFamily()
      if (!this.family) {
        this.resetFamilyState()
        await this.loadReceivedInvites()
        this.pageLoading = false
        return
      }
      await Promise.all([
        this.loadMembers(),
        this.loadTodayCook(),
        this.loadStats(),
        this.loadPending(),
        this.loadReceivedInvites()
      ])
      this.pageLoading = false
    },
    resetFamilyState() {
      this.family = null
      this.familyName = ''
      this.familyCode = ''
      this.members = []
      this.todayCook = null
      this.pendingCount = 0
      this.dishPendingCount = 0
      this.joinPendingCount = 0
      this.weeklyStats = { cookDays: 0, dishCount: 0, orderCount: 0 }
      uni.removeStorageSync('familyId')
      uni.removeStorageSync('familyCode')
      uni.removeStorageSync('familyName')
    },
    async loadFamily() {
      try {
        if (!this.currentUserId) {
          this.family = null
          return
        }
        this.family = await getCurrentFamily(this.currentUserId)
        if (this.family) {
          this.familyName = this.family.name
          this.familyCode = this.family.familyCode
          uni.setStorageSync('familyId', this.family.id)
          uni.setStorageSync('familyName', this.family.name)
          uni.setStorageSync('familyCode', this.family.familyCode)
        } else {
          this.familyName = ''
          this.familyCode = ''
        }
      } catch (e) {
        this.family = null
        this.familyName = ''
        this.familyCode = ''
      }
    },
    async loadMembers() {
      try {
        if (!this.family) {
          this.members = []
          return
        }
        this.members = await getFamilyMembers(this.family.id)
      } catch (e) {
        this.members = []
      }
    },
    async loadTodayCook() {
      try {
        if (!this.family) {
          this.todayCook = null
          return
        }
        this.todayCook = await getTodayCook(this.family.id, this.formatDate(new Date()))
      } catch (e) {
        this.todayCook = null
      }
    },
    async loadPending() {
      try {
        const list = await getNotifications(this.currentUserId)
        const notifications = Array.isArray(list) ? list : []
        this.dishPendingCount = notifications.filter(item => item.auditType === 'dish').length
        this.joinPendingCount = notifications.filter(item => item.auditType === 'familyJoin').length
        this.pendingCount = this.dishPendingCount + this.joinPendingCount
      } catch (e) {
        const [dishList, joinList] = await Promise.all([
          this.family ? getPendingRequests(this.family.id).catch(() => []) : Promise.resolve([]),
          this.loadJoinPendingList()
        ])
        this.dishPendingCount = Array.isArray(dishList) ? dishList.length : 0
        this.joinPendingCount = Array.isArray(joinList) ? joinList.length : 0
        this.pendingCount = this.dishPendingCount + this.joinPendingCount
      }
    },
    async loadJoinPendingList() {
      if (!this.currentUserId) return []
      const families = await getUserFamilies(this.currentUserId).catch(() => [])
      if (!Array.isArray(families) || !families.length) return []
      const groups = await Promise.all(
        families.map(family =>
          getFamilyInvitations(family.id, this.currentUserId)
            .then(list => (Array.isArray(list) ? list : []))
            .catch(() => [])
        )
      )
      return groups
        .flat()
        .filter(item => item.status === 'PENDING' && item.requestType === 'JOIN_REQUEST')
    },
    async loadReceivedInvites() {
      try {
        if (!this.currentUserId) return
        const list = await getReceivedFamilyInvitations(this.currentUserId)
        this.receivedInviteCount = list.length
      } catch (e) {
        this.receivedInviteCount = 0
      }
    },
    async loadStats() {
      try {
        if (!this.family) {
          this.weeklyStats = { cookDays: 0, dishCount: 0, orderCount: 0 }
          return
        }
        const [dishes, orders] = await Promise.all([
          getFamilyDishes(this.family.id),
          getOrdersByFamily(this.family.id)
        ])
        const weekAgo = new Date()
        weekAgo.setDate(weekAgo.getDate() - 7)
        const weekDishes = dishes.filter(d => d.createTime && new Date(d.createTime) >= weekAgo)
        const weekOrders = orders.filter(o => new Date(o.createTime) >= weekAgo)
        const cookUserIds = new Set(weekDishes.map(d => d.cookUserId).filter(Boolean))
        this.weeklyStats = {
          cookDays: cookUserIds.size,
          dishCount: weekDishes.length,
          orderCount: weekOrders.length
        }
      } catch (e) {
        this.weeklyStats = { cookDays: 0, dishCount: 0, orderCount: 0 }
      }
    },
    formatDate(date) {
      return formatYMD(date)
    },
    handleProfileClick() {
      if (!this.isLoggedIn) {
        this.goToLogin()
        return
      }
      uni.navigateTo({ url: '/pages/profile/profile' })
    },
    goToLogin() {
      uni.navigateTo({ url: '/pages/login/login' })
    },
    copyFamilyCode() {
      if (!this.familyCode) return
      uni.setClipboardData({ data: this.familyCode, success: () => {
        info('家庭码已复制')
      }})
    },
    goToFamilyManage() {
      uni.navigateTo({ url: '/pages/family/family' })
    },
    async switchToMe() {
      if (!this.family) return
      try {
        this.todayCook = await switchCook(this.family.id, this.currentUserId, this.formatDate(new Date()))
        info('已切换为你来做')
      } catch (e) {
        error(e.message || '切换失败')
      }
    },
    maskPhone(phone) {
      const value = String(phone || '')
      if (value.length !== 11) return value
      return `${value.slice(0, 3)}****${value.slice(7)}`
    },
    logout() {
      this.closeNotificationSocket()
      uni.removeStorageSync('currentUser')
      uni.removeStorageSync('userId')
      uni.removeStorageSync('phone')
      uni.removeStorageSync('nickname')
      uni.removeStorageSync('familyId')
      uni.removeStorageSync('familyCode')
      uni.removeStorageSync('familyName')
      this.user = null
      this.family = null
      this.members = []
      this.todayCook = null
      info('已退出')
    },
    goToOrder() { uni.navigateTo({ url: '/pages/order/order' }) },
    goToNotifications() {
      uni.navigateTo({ url: '/pages/notifications/notifications' })
    },
    goToApprove() {
      uni.navigateTo({ url: '/pages/approve/approve' })
    },
    goToMyDishes() { uni.navigateTo({ url: '/pages/order/order?tab=mine' }) },
    connectNotificationSocket() {
      if (!this.currentUserId || typeof WebSocket === 'undefined') return
      if (this.notificationSocket && this.socketUserId === this.currentUserId) return
      this.closeNotificationSocket()
      try {
        const socket = new WebSocket(`${WS_NOTIFICATION_URL}?userId=${encodeURIComponent(this.currentUserId)}`)
        socket.onmessage = () => {
          this.loadPending()
        }
        socket.onclose = () => {
          if (this.notificationSocket === socket) {
            this.notificationSocket = null
            this.socketUserId = ''
          }
        }
        this.notificationSocket = socket
        this.socketUserId = this.currentUserId
      } catch (e) {
        this.notificationSocket = null
        this.socketUserId = ''
      }
    },
    closeNotificationSocket() {
      if (this.notificationSocket) {
        try {
          this.notificationSocket.close()
        } catch (e) {}
      }
      this.notificationSocket = null
      this.socketUserId = ''
    }
  }
}
</script>

<style>
/* ============ 顶部 Header 背景 ============ */
.my-header {
  position: relative;
  padding: 24rpx 24rpx 0;
  background: transparent;
  overflow: hidden;
}

.my-header-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 320rpx;
  background: linear-gradient(135deg, var(--gradient-primary-soft) 50%, var(--color-primary-light) 100%);
  overflow: hidden;
  z-index: 0;
}

.my-header-bg::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: var(--gradient-blob-soft-1),
              var(--gradient-blob-soft-2);
}

.bg-blob {
  position: absolute;
  border-radius: 50%;
  background: var(--color-bg-overlay-soft);
  filter: blur(20rpx);
  animation: floatBlob 8s ease-in-out infinite;
}

.bg-blob-1 { width: 200rpx; height: 200rpx; top: -60rpx; right: -40rpx; }
.bg-blob-2 { width: 160rpx; height: 160rpx; top: 80rpx; left: -50rpx; animation-delay: 2s; }
.bg-blob-3 { width: 120rpx; height: 120rpx; top: 180rpx; right: 40rpx; animation-delay: 4s; }

@keyframes floatBlob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(10rpx, -15rpx) scale(1.08); }
}

.my-toolbar {
  position: relative;
  z-index: 3;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 88rpx;
  padding: 4rpx 4rpx 0;
}

.toolbar-title-group {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
}

.toolbar-title,
.toolbar-subtitle {
  display: block;
}

.toolbar-title {
  color: var(--color-text-primary);
  font-size: 36rpx;
  font-weight: 900;
}

.toolbar-subtitle {
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  font-weight: 600;
}

.toolbar-actions {
  flex: none;
  display: grid;
  grid-template-columns: repeat(3, 72rpx);
  gap: 12rpx;
  justify-content: end;
}

.toolbar-action {
  position: relative;
  width: 72rpx;
  height: 72rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.toolbar-action.reserved {
  visibility: hidden;
}

.notify-bell {
  color: var(--color-primary);
  background: var(--color-bg-card);
  box-shadow: 0 14rpx 32rpx rgba(255, 91, 46, 0.22);
  border: 1rpx solid rgba(255, 107, 53, 0.12);
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.toolbar-action--hover,
.notify-bell:active {
  transform: scale(0.94);
  box-shadow: 0 8rpx 22rpx rgba(255, 45, 85, 0.22);
}

.notify-badge {
  position: absolute;
  top: -10rpx;
  right: -10rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border-radius: 999rpx;
  color: #fff;
  background: #ff2d55;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 34rpx;
  text-align: center;
  border: 4rpx solid #fff;
  box-shadow: 0 8rpx 18rpx rgba(255, 45, 85, 0.34);
  animation: badgePulse 1.8s ease-in-out infinite;
}

@keyframes badgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.12); }
}

/* ============ 已登录 Profile 卡片 ============ */
.my-avatar {
  flex: none;
  width: 168rpx;
  height: 168rpx;
  margin-right: 28rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.my-profile {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  min-height: 224rpx;
  padding: 28rpx;
  margin-top: 24rpx;
  border-radius: 32rpx;
  background: var(--color-bg-overlay-strong);
  box-shadow: var(--shadow-primary-strong),
              0 4rpx 12rpx var(--shadow-color-soft);
  backdrop-filter: blur(20rpx);
  transition: transform 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.my-profile:active { transform: scale(0.985); }

.my-profile-info { flex: 1; min-width: 0; }

.my-name-row { display: flex; align-items: center; gap: 12rpx; }

.my-name, .my-subtitle, .guest-title, .guest-desc, .feature-text,
.stat-value, .stat-label, .family-name, .family-sub, .member-name,
.tcc-title, .tcc-desc, .tcc-action, .row-title, .row-desc,
.guest-cta-text, .empty-bound, .footer-brand {
  display: block;
}

.my-name { color: var(--color-text-primary); font-size: 40rpx; font-weight: 800; letter-spacing: -0.5rpx; }

.my-subtitle { margin-top: 10rpx; color: var(--color-text-tertiary); font-size: 24rpx; letter-spacing: 0.3rpx; }

.my-chevron { transition: transform 0.2s ease, color 0.2s ease; }
.my-profile:active .my-chevron { color: var(--color-primary) !important; transform: translateX(4rpx); }

.profile-avatar-image {
  width: 168rpx;
  height: 168rpx;
  border-radius: 50%;
  border: 6rpx solid var(--color-bg-card);
  box-shadow: var(--shadow-primary-soft);
  background: var(--color-bg-page-soft);
}

/* ============ 未登录 Hero 空状态卡 ============ */
.guest-hero {
  position: relative;
  margin-top: 24rpx;
  padding: 56rpx 32rpx 40rpx;
  border-radius: 32rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-primary-soft),
              0 4rpx 12rpx var(--shadow-color-soft);
  overflow: hidden;
  display: flex; flex-direction: column; align-items: center; text-align: center;
}

.guest-hero-bg {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 220rpx;
  background: var(--gradient-primary-fade-vertical-soft);
  pointer-events: none;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(30rpx);
  opacity: 0.6;
}

.hero-blob-1 { width: 180rpx; height: 180rpx; top: -40rpx; left: -40rpx; background: var(--gradient-blob-1); }
.hero-blob-2 { width: 160rpx; height: 160rpx; top: -30rpx; right: -30rpx; background: var(--gradient-blob-2); }

.guest-avatar-wrap {
  position: relative;
  width: 168rpx; height: 168rpx;
  margin-bottom: 32rpx;
  display: flex; align-items: center; justify-content: center;
}

.guest-avatar-ring {
  position: absolute;
  top: -12rpx; left: -12rpx; right: -12rpx; bottom: -12rpx;
  border-radius: 50%;
  background: var(--gradient-primary-rainbow);
  opacity: 0.18;
  animation: spin 6s linear infinite;
}

@keyframes spin { to { transform: rotate(360deg); } }

.guest-avatar {
  position: relative;
  width: 144rpx; height: 144rpx;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: var(--gradient-primary-fade);
  box-shadow: var(--shadow-primary-strong),
              inset 0 -4rpx 8rpx var(--shadow-primary-soft),
              inset 0 2rpx 4rpx var(--color-bg-overlay-strong);
  z-index: 1;
}

.avatar-figure { width: 88rpx; display: flex; flex-direction: column; align-items: center; }

.avatar-head {
  width: 36rpx; height: 36rpx;
  margin-bottom: 8rpx;
  border-radius: 50%;
  background: var(--gradient-avatar);
  box-shadow: inset 0 -2rpx 4rpx var(--shadow-color-soft);
}

.avatar-body {
  width: 72rpx; height: 36rpx;
  border-radius: 36rpx 36rpx 20rpx 20rpx;
  background: var(--gradient-avatar);
  box-shadow: inset 0 -4rpx 6rpx var(--shadow-color-soft);
}

.avatar-badge {
  position: absolute;
  right: 4rpx; top: 8rpx;
  width: 44rpx; height: 44rpx;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--color-bg-card);
  font-size: 26rpx; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  box-shadow: var(--shadow-primary-strong);
  border: 4rpx solid var(--color-bg-card);
  z-index: 2;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(1.08); } }

.guest-title {
  color: var(--color-text-primary); font-size: 36rpx; font-weight: 800;
  letter-spacing: -0.5rpx; margin-bottom: 14rpx;
  position: relative; z-index: 1;
}

.guest-desc {
  color: var(--color-text-tertiary); font-size: 25rpx; line-height: 36rpx; letter-spacing: 0.5rpx;
  position: relative; z-index: 1;
}

.guest-feature-list {
  width: 100%;
  margin-top: 36rpx;
  padding: 24rpx 28rpx;
  border-radius: 20rpx;
  background: var(--gradient-bg-soft);
  display: flex; flex-direction: column; gap: 18rpx;
  position: relative; z-index: 1;
}

.feature-item { display: flex; align-items: center; gap: 16rpx; }

.feature-dot {
  width: 16rpx; height: 16rpx; border-radius: 50%;
  flex: none; position: relative;
}
.feature-dot::after {
  content: '';
  position: absolute;
  top: -4rpx; left: -4rpx; right: -4rpx; bottom: -4rpx;
  border-radius: 50%; opacity: 0.25;
}

.dot-1 { background: var(--color-primary); box-shadow: var(--shadow-primary-soft); }
.dot-1::after { background: var(--color-primary); }
.dot-2 { background: var(--color-primary-light); box-shadow: var(--shadow-primary-soft); }
.dot-2::after { background: var(--color-primary-light); }
.dot-3 { background: var(--color-primary-lighter); box-shadow: var(--shadow-primary-soft); }
.dot-3::after { background: var(--color-primary-lighter); }

.feature-text {
  color: var(--color-text-primary); font-size: 26rpx; font-weight: 600;
  text-align: left; flex: 1;
}

.guest-cta {
  margin-top: 40rpx;
  width: 100%; height: 96rpx;
  border-radius: 999rpx;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-strong),
              inset 0 2rpx 4rpx var(--color-bg-overlay-medium),
              inset 0 -2rpx 4rpx var(--shadow-color-soft);
  display: flex; align-items: center; justify-content: center; gap: 8rpx;
  position: relative; z-index: 1;
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1), box-shadow 0.2s ease;
  overflow: hidden;
}

.guest-cta:active {
  transform: scale(0.97);
  box-shadow: var(--shadow-primary-soft),
              inset 0 2rpx 4rpx var(--color-bg-overlay-medium);
}

.guest-cta-text { color: var(--color-bg-card); font-size: 32rpx; font-weight: 800; letter-spacing: 1rpx; }
.guest-cta-arrow { color: var(--color-bg-card); font-size: 40rpx; font-weight: 300; line-height: 40rpx; margin-top: -2rpx; }

/* ============ 已登录：家庭信息卡 ============ */
.family-card {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: var(--gradient-primary-fade-soft);
  box-shadow: var(--shadow-primary-soft);
  border: 1rpx solid var(--shadow-primary-soft);
}

.family-head {
  display: flex; align-items: center; gap: 16rpx;
}

.family-icon {
  width: 72rpx; height: 72rpx;
  border-radius: 20rpx;
  background: var(--gradient-primary-strong);
  display: flex; align-items: center; justify-content: center;
  box-shadow: var(--shadow-primary-soft);
  flex: none;
  color: var(--color-bg-card);
}

.family-icon-text {
  color: var(--color-bg-card);
  font-size: 28rpx;
  font-weight: 800;
  line-height: 1;
}

.family-info { flex: 1; min-width: 0; }
.family-name { color: var(--color-text-primary); font-size: 30rpx; font-weight: 800; }
.family-sub { margin-top: 6rpx; color: var(--color-text-muted); font-size: 22rpx; letter-spacing: 1rpx; font-family: monospace; }

.family-share {
  flex: none;
  padding: 10rpx 20rpx;
  border-radius: 999rpx;
  background: var(--color-bg-card);
  color: var(--color-primary);
  font-size: 22rpx; font-weight: 700;
  box-shadow: var(--shadow-primary-soft);
  transition: transform 0.15s ease;
  display: flex; align-items: center; gap: 6rpx;
}
.family-share:active { transform: scale(0.96); }

.family-invite-badge {
  flex: none;
  padding: 10rpx 16rpx;
  border-radius: 999rpx;
  color: var(--color-bg-card);
  background: var(--gradient-danger);
  font-size: 22rpx;
  font-weight: 800;
  box-shadow: var(--shadow-color-soft);
}

.members-row {
  margin-top: 24rpx;
  display: flex; gap: 20rpx; flex-wrap: wrap;
}

.member-chip {
  display: flex; flex-direction: column; align-items: center; gap: 8rpx;
  padding: 16rpx 22rpx;
  border-radius: 20rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-primary-soft);
  min-width: 120rpx;
}

.member-chip.cook {
  background: var(--gradient-primary-fade);
  border: 1rpx solid var(--color-primary-soft);
}


.member-name { color: var(--color-text-primary); font-size: 22rpx; font-weight: 600; max-width: 110rpx; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ============ 已登录：统计卡 ============ */
.stats-card {
  display: flex; align-items: center;
  margin-top: 24rpx;
  padding: 32rpx 24rpx;
  border-radius: 24rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
}

.stat-item {
  flex: 1;
  display: flex; flex-direction: column; align-items: center; gap: 8rpx;
}

.stat-icon-wrap {
  width: 72rpx; height: 72rpx;
  border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center;
  color: var(--color-bg-card);
  box-shadow: var(--shadow-color-soft);
  flex: none;
}

.stat-cook { background: var(--gradient-primary-strong); }
.stat-dish { background: var(--gradient-info); }
.stat-order { background: var(--gradient-success); }

.stat-value { color: var(--color-text-primary); font-size: 30rpx; font-weight: 800; }
.stat-label { color: var(--color-text-tertiary); font-size: 22rpx; }

.stat-divider {
  width: 1rpx; height: 64rpx;
  background: var(--gradient-divider-fade);
  flex: none;
}

/* ============ 已登录：今日主厨快捷卡 ============ */
.today-cook-card {
  margin-top: 24rpx;
  padding: 22rpx 28rpx;
  border-radius: 20rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card-soft);
  display: flex; align-items: center; justify-content: space-between;
}

.tcc-left { display: flex; align-items: center; gap: 16rpx; flex: 1; min-width: 0; }

.tcc-dot {
  width: 16rpx; height: 16rpx; border-radius: 50%;
  background: var(--color-info);
  box-shadow: var(--shadow-info-soft);
}
.tcc-dot.mine {
  background: var(--color-primary);
  box-shadow: var(--shadow-primary-soft);
}

.tcc-text { flex: 1; min-width: 0; }
.tcc-title { color: var(--color-text-primary); font-size: 28rpx; font-weight: 700; }
.tcc-desc { margin-top: 4rpx; color: var(--color-text-tertiary); font-size: 22rpx; }

.tcc-action {
  flex: none;
  padding: 12rpx 22rpx;
  border-radius: 999rpx;
  background: var(--gradient-primary);
  color: var(--color-bg-card);
  font-size: 24rpx; font-weight: 700;
  box-shadow: var(--shadow-primary-soft);
  transition: transform 0.15s ease;
}
.tcc-action:active { transform: scale(0.96); }
.tcc-action.done {
  background: var(--gradient-success);
  box-shadow: var(--shadow-success-soft);
}

/* ============ 已登录：菜单卡 ============ */
.menu-card {
  margin-top: 24rpx;
  padding: 0;
  border-radius: 24rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
  overflow: hidden;
}

.menu-row {
  position: relative;
  min-height: 112rpx;
  padding: 24rpx 28rpx;
  border-bottom: 1rpx solid var(--color-border-light);
  display: flex; align-items: center;
  transition: background 0.2s ease;
}

.menu-row:last-child { border-bottom: 0; }
.menu-row:active { background: var(--color-bg-page-soft); }

.menu-row-icon {
  width: 72rpx; height: 72rpx;
  margin-right: 20rpx;
  border-radius: 20rpx;
  display: flex; align-items: center; justify-content: center;
  color: var(--color-bg-card);
  flex: none;
  box-shadow: var(--shadow-color-soft);
}

.icon-order { background: var(--gradient-info); }
.icon-approve { background: var(--gradient-primary-strong); }
.icon-mine { background: var(--gradient-success); }
.icon-logout { background: var(--gradient-danger); }

.menu-row-text { flex: 1; min-width: 0; }
.row-title { color: var(--color-text-primary); font-size: 28rpx; font-weight: 700; }
.row-desc { margin-top: 6rpx; color: var(--color-text-tertiary); font-size: 22rpx; }

.logout .row-title { color: var(--color-danger); }

.row-right { display: flex; align-items: center; }

.row-chevron { transition: transform 0.2s ease; }
.menu-row:active .row-chevron { transform: translateX(4rpx); color: var(--color-primary) !important; }
.logout.menu-row:active .row-chevron { color: var(--color-danger) !important; }

.badge {
  margin-right: 12rpx;
  padding: 5rpx 14rpx;
  border-radius: 999rpx;
  color: #fff;
  background: #ff2d55;
  font-size: 20rpx; font-weight: 900;
  box-shadow: 0 8rpx 18rpx rgba(255, 45, 85, 0.28);
  border: 3rpx solid #fff;
  min-width: 34rpx; text-align: center;
}

.footer-brand {
  margin-top: 40rpx;
  text-align: center;
  color: var(--color-text-quaternary);
  font-size: 22rpx;
  letter-spacing: 1rpx;
  padding-bottom: 12rpx;
}

.my-skeleton {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
  padding-top: 4rpx;
}
</style>
