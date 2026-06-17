<template>
  <view class="detail-page">
    <TopBar
      variant="transparent"
      :fixed="true"
      :fallback-url="'/pages/menu/menu'"
    />

    <view class="dish-image-wrap" v-if="dish">
      <image
        class="dish-image"
        :src="dish.image || '/static/default-dish.png'"
        mode="aspectFill"
      />
      <view class="image-overlay" />
    </view>

    <view class="dish-content" v-if="dish">
      <view class="dish-header">
        <view class="dish-header-text">
          <text class="dish-name">{{ dish.name }}</text>
          <text class="dish-sub" v-if="familyName">🏠 {{ familyName }}</text>
        </view>
        <view class="dish-tag" v-if="dish.category">{{ dish.category }}</view>
      </view>

      <InfoCard title="基本信息">
        <view class="info-row" v-if="dish.mealTime">
          <text class="info-label">餐次</text>
          <text class="info-value">{{ mealTimeText }}</text>
        </view>
        <Divider v-if="dish.mealTime && dish.cookNickname" />
        <view class="info-row" v-if="dish.cookNickname">
          <text class="info-label">主厨</text>
          <text class="info-value">{{ dish.cookNickname }}</text>
        </view>
        <Divider v-if="dish.cookNickname && dish.cookDate" />
        <view class="info-row" v-if="dish.cookDate">
          <text class="info-label">日期</text>
          <text class="info-value">{{ formatDate(dish.cookDate) }}</text>
        </view>
        <Divider v-if="dish.cookDate && dish.createdByNickname && dish.createdByNickname !== dish.cookNickname" />
        <view class="info-row" v-if="dish.createdByNickname && dish.createdByNickname !== dish.cookNickname">
          <text class="info-label">发布人</text>
          <text class="info-value">{{ dish.createdByNickname }}</text>
        </view>
      </InfoCard>

      <InfoCard v-if="dish.description" title="简介">
        <text class="desc-text">{{ dish.description }}</text>
      </InfoCard>
    </view>

    <view class="loading" v-else>
      <view class="loading-stack">
        <SkeletonBox width="100%" height="44rpx" radius="12rpx" />
        <SkeletonBox width="70%" height="44rpx" radius="12rpx" />
        <SkeletonBox width="100%" height="160rpx" radius="24rpx" />
        <SkeletonBox width="100%" height="120rpx" radius="24rpx" />
      </view>
    </view>
  </view>
</template>

<script>
import { getDishById, getCurrentFamily } from '@/utils/api.js'
import { formatYMD } from '@/utils/date.js'
import TopBar from '@/components/TopBar.vue'
import InfoCard from '@/components/InfoCard.vue'
import Divider from '@/components/Divider.vue'
import SkeletonBox from '@/components/SkeletonBox.vue'
import { info } from '@/utils/toast.js'

export default {
  components: { TopBar, InfoCard, Divider, SkeletonBox },
  data() {
    return {
      dish: null,
      familyName: ''
    }
  },
  computed: {
    mealTimeText() {
      const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐' }
      return map[this.dish?.mealTime] || ''
    }
  },
  onLoad(options) {
    const user = uni.getStorageSync('currentUser') || {}
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    if (options.id) {
      this.loadDish(options.id)
    }
    if (!this.familyName && user.userId) {
      // 容错：万一 storage 没存好，再拉一次
      getCurrentFamily(user.userId).then(f => {
        if (f && f.name) this.familyName = f.name
      }).catch(() => {})
    }
  },
  methods: {
    async loadDish(id) {
      try {
        this.dish = await getDishById(id)
      } catch (e) {
        console.error('加载菜品详情失败', e)
        error('加载失败')
      }
    },
    formatDate(input) {
      return formatYMD(input ? new Date(input) : null)
    }
  }
}
</script>

<style scoped>
.detail-page {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.dish-image-wrap {
  position: relative;
  width: 100%;
  height: 500rpx;
}

.dish-image {
  width: 100%;
  height: 100%;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120rpx;
  background: linear-gradient(transparent, var(--color-bg-page));
}

.dish-content {
  position: relative;
  margin-top: -40rpx;
  padding: 0 30rpx 40rpx;
}

.dish-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16rpx;
  margin-bottom: 24rpx;
}

.dish-header-text {
  flex: 1;
  min-width: 0;
}

.dish-name {
  display: block;
  font-size: 40rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.dish-sub {
  display: block;
  margin-top: 8rpx;
  color: var(--color-primary);
  font-size: var(--font-md);
  font-weight: 600;
}

.dish-tag {
  padding: 6rpx 16rpx;
  background: var(--color-primary-soft);
  color: var(--color-primary);
  font-size: var(--font-sm);
  border-radius: var(--radius-sm);
  white-space: nowrap;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14rpx 0;
}

.info-label {
  color: var(--color-text-tertiary);
  font-size: var(--font-lg);
}

.info-value {
  color: var(--color-text-primary);
  font-size: var(--font-lg);
  font-weight: 600;
}

.desc-text {
  font-size: var(--font-lg);
  color: var(--color-text-secondary);
  line-height: 1.8;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 0 30rpx;
}

.loading-stack {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  width: 100%;
}
</style>
