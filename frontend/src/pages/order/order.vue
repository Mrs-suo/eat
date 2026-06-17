<template>
  <app-shell content-style="padding: 24rpx 24rpx 32rpx;" :show-tab-bar="true" :current-tab="2">
    <template #menu>
      <HeroPanel
        title="家庭订单"
        :subtitle="(familyName || '我的小家') + ' · ' + orders.length + ' 单 · ' + totalAmount + ' 元'"
        tone="orange"
        fullwidth
      >
        <template #badge>
          <view class="order-emoji-circle">
            <text class="order-emoji">🧾</text>
          </view>
        </template>
      </HeroPanel>
    </template>

    <view v-if="orderLoading" class="order-skeleton">
      <view v-for="i in 3" :key="i" class="skeleton-card">
        <SkeletonBox width="60%" height="28rpx" />
        <SkeletonBox width="40%" height="22rpx" />
        <SkeletonBox width="100%" height="80rpx" radius="16rpx" />
      </view>
    </view>

    <EmptyState
      v-else-if="orders.length === 0"
      emoji="📭"
      title="家庭还没有订单"
      hint="去菜单页挑选想吃的菜吧"
    />

    <view v-else class="order-list">
      <ListCard
        v-for="order in orders"
        :key="order.id"
        layout="column"
        class="order-card"
      >
        <view class="order-header">
          <view class="order-header-left">
            <text class="order-no">订单 {{ order.orderNo }}</text>
            <text class="order-time">{{ formatDate(order.createTime) }}</text>
          </view>
          <Tag :text="statusText(order.status)" :tone="statusTone(order.status)" />
        </view>

        <view class="order-dishes">
          <view class="dish" v-for="item in order.items" :key="item.id">
            <view class="dish-line">
              <text class="dish-name">{{ item.dishName }}</text>
              <text class="dish-qty">x{{ item.quantity }}</text>
            </view>
            <text class="dish-price">¥{{ formatMoney(item.price * item.quantity) }}</text>
          </view>
        </view>

        <view class="order-footer">
          <text class="order-cook">🏠 {{ familyName || '家庭订单' }}</text>
          <text class="order-total">合计：¥{{ formatMoney(order.totalPrice) }}</text>
        </view>
      </ListCard>
    </view>
  </app-shell>
</template>

<script>
import AppShell from '@/components/AppShell.vue'
import HeroPanel from '@/components/HeroPanel.vue'
import EmptyState from '@/components/EmptyState.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import ListCard from '@/components/ListCard.vue'
import Tag from '@/components/Tag.vue'
import { getOrdersByFamily } from '@/utils/api.js'
import { formatShortDateTime } from '@/utils/date.js'

export default {
  components: { AppShell, HeroPanel, EmptyState, SkeletonBox, ListCard, Tag },
  data() {
    return {
      orders: [],
      orderLoading: true,
      familyId: '',
      familyName: ''
    }
  },
  computed: {
    totalAmount() {
      return this.formatMoney(
        this.orders.reduce((sum, o) => sum + Number(o.totalPrice || 0), 0)
      )
    }
  },
  onShow() {
    const user = uni.getStorageSync('currentUser') || {}
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    this.loadOrders()
  },
  methods: {
    async loadOrders() {
      this.orderLoading = true
      if (!this.familyId) {
        this.orders = []
        this.orderLoading = false
        return
      }
      try {
        const data = await getOrdersByFamily(this.familyId)
        this.orders = Array.isArray(data) ? data : []
      } catch (e) {
        console.error('加载订单失败', e)
        this.orders = []
      } finally {
        this.orderLoading = false
      }
    },
    statusText(status) {
      const map = { 0: '待支付', 1: '已支付', 2: '制作中', 3: '已完成' }
      return map[status] || '未知'
    },
    statusTone(status) {
      // 与 Tag tone 颜色语义一致
      const map = { 0: 'orange', 1: 'blue', 2: 'orange', 3: 'green' }
      return map[status] || 'gray'
    },
    formatDate(input) {
      return formatShortDateTime(input)
    },
    formatMoney(v) {
      const num = Number(v) || 0
      return num.toFixed(2)
    }
  }
}
</script>

<style scoped>
/* HeroPanel 已经提供背景与标题，这里只补全 emoji 圆圈 */
.order-emoji-circle {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.22);
}

.order-emoji {
  font-size: 52rpx;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding-bottom: 60rpx;
}

.order-card {
  padding: 24rpx 24rpx 20rpx;
}

.order-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: var(--space-4);
  margin-bottom: 14rpx;
  border-bottom: 1rpx solid var(--color-bg-hover);
}

.order-header-left {
  display: flex;
  flex-direction: column;
}

.order-no {
  color: var(--color-text-primary);
  font-size: var(--font-lg);
  font-weight: 800;
}

.order-time {
  margin-top: 4rpx;
  color: var(--color-text-tertiary);
  font-size: var(--font-sm);
  font-weight: 500;
}

.order-dishes {
  margin-bottom: 14rpx;
}

.dish {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8rpx 0;
}

.dish-line {
  display: flex;
  align-items: center;
}

.dish-name {
  color: var(--color-text-primary);
  font-size: var(--font-base);
  font-weight: 600;
}

.dish-qty {
  margin-left: 10rpx;
  color: var(--color-text-tertiary);
  font-size: var(--font-sm);
  font-weight: 600;
}

.dish-price {
  color: var(--color-primary);
  font-size: var(--font-base);
  font-weight: 700;
}

.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 14rpx;
  border-top: 1rpx dashed var(--color-border-light);
}

.order-cook {
  color: var(--color-text-secondary);
  font-size: var(--font-sm);
  font-weight: 600;
}

.order-total {
  color: var(--color-primary-deep);
  font-size: var(--font-lg);
  font-weight: 800;
}

.order-skeleton {
  display: flex;
  flex-direction: column;
  gap: 24rpx;
}

.skeleton-card {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
  padding: 28rpx;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}
</style>
