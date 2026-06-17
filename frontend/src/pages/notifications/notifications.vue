<template>
  <PageLayout title="消息提醒">
    <view v-if="loading" class="loading-card">
      <text>加载消息中...</text>
    </view>

    <EmptyState
      v-else-if="!notifications.length"
      emoji="✓"
      title="消息都处理完了"
      hint="家庭加入申请和菜品审核会在这里集中显示"
    />

    <view v-else class="notify-list">
      <view
        v-for="item in notifications"
        :key="item.id"
        class="notify-card"
        hover-class="notify-card--hover"
        :hover-stay-time="80"
        @click="openAudit(item)"
      >
        <view class="notify-icon" :class="item.auditType === 'familyJoin' ? 'family' : 'dish'">
          <AppIcon :name="item.auditType === 'familyJoin' ? 'home' : 'dish'" size="lg" tone="inherit" />
        </view>
        <view class="notify-main">
          <view class="notify-title-row">
            <text class="notify-title">{{ item.title }}</text>
            <text class="notify-dot">待处理</text>
          </view>
          <text class="notify-content">{{ item.content || '有一条新的审核消息' }}</text>
          <text class="notify-meta">{{ item.familyName || '家庭' }} · {{ formatDate(item.createTime) }}</text>
        </view>
        <AppIcon name="chevron_right" size="md" tone="muted" />
      </view>
    </view>
  </PageLayout>
</template>

<script>
import AppIcon from '@/components/AppIcon.vue'
import EmptyState from '@/components/EmptyState.vue'
import PageLayout from '@/components/PageLayout.vue'
import { getNotifications } from '@/utils/api.js'
import { formatShortDateTime } from '@/utils/date.js'
import { error } from '@/utils/toast.js'

export default {
  components: { AppIcon, EmptyState, PageLayout },
  data() {
    return {
      loading: false,
      notifications: [],
      userId: ''
    }
  },
  onShow() {
    const user = uni.getStorageSync('currentUser') || {}
    this.userId = user.userId || uni.getStorageSync('userId') || ''
    this.loadNotifications()
  },
  methods: {
    async loadNotifications() {
      if (!this.userId) {
        this.notifications = []
        return
      }
      this.loading = true
      try {
        const list = await getNotifications(this.userId)
        this.notifications = Array.isArray(list) ? list : []
      } catch (e) {
        error(e.message || '消息加载失败')
        this.notifications = []
      } finally {
        this.loading = false
      }
    },
    openAudit(item) {
      uni.navigateTo({
        url: `/pages/approve/approve?focusType=${encodeURIComponent(item.auditType)}&focusId=${encodeURIComponent(item.auditId)}`
      })
    },
    formatDate(input) {
      return formatShortDateTime(input)
    }
  }
}
</script>

<style scoped>
.loading-card {
  margin: 24rpx;
  padding: 36rpx;
  border-radius: 24rpx;
  color: var(--color-text-tertiary);
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
  text-align: center;
}

.notify-list {
  padding: 24rpx;
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.notify-card {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 24rpx;
  border-radius: 24rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
  border: 1rpx solid rgba(255, 107, 53, 0.08);
  transition: transform 0.16s ease, box-shadow 0.16s ease;
}

.notify-card--hover,
.notify-card:active {
  transform: scale(0.985);
  box-shadow: var(--shadow-primary-soft);
}

.notify-icon {
  flex: none;
  width: 80rpx;
  height: 80rpx;
  border-radius: 24rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: var(--shadow-color-soft);
}

.notify-icon.family { background: var(--gradient-info); }
.notify-icon.dish { background: var(--gradient-primary-strong); }

.notify-main {
  flex: 1;
  min-width: 0;
}

.notify-title-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.notify-title {
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 850;
}

.notify-dot {
  flex: none;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  color: #fff;
  background: #ff2d55;
  font-size: 20rpx;
  font-weight: 900;
  box-shadow: 0 8rpx 18rpx rgba(255, 45, 85, 0.24);
}

.notify-content,
.notify-meta {
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notify-content {
  margin-top: 8rpx;
  color: var(--color-text-secondary);
  font-size: 25rpx;
  font-weight: 600;
}

.notify-meta {
  margin-top: 6rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
}
</style>
