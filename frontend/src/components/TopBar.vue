<template>
  <view
    class="topbar"
    :class="[
      `topbar--${variant}`,
      { 'topbar--fixed': fixed }
    ]"
    :style="topbarStyle"
  >
    <view v-if="showBack" class="topbar__back" :class="{ 'topbar__back--on-light': variant === 'solid' }" @click="handleBack" hover-class="topbar__back--hover" hover-stay-time="80">
      <text class="topbar__back-icon">{{ backIcon }}</text>
    </view>

    <view v-if="title || $slots.title" class="topbar__title">
      <slot name="title">
        <text class="topbar__title-text" :class="{ 'topbar__title-text--light': variant !== 'solid' }">{{ title }}</text>
      </slot>
    </view>

    <view v-if="$slots.right" class="topbar__right">
      <slot name="right" />
    </view>
  </view>
</template>

<script>
export default {
  name: 'TopBar',
  props: {
    title: { type: String, default: '' },
    showBack: { type: Boolean, default: true },
    // transparent 透明（盖在图片上） | solid 白底 | gradient 橙渐变
    variant: { type: String, default: 'transparent' },
    fixed: { type: Boolean, default: true },
    backIcon: { type: String, default: '‹' },
    // 自定义返回路径，null = 默认 navigateBack
    fallbackUrl: { type: String, default: '/pages/menu/menu' }
  },
  computed: {
    topbarStyle() {
      return {}
    }
  },
  methods: {
    handleBack() {
      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      if (pages.length > 1) {
        uni.navigateBack({ delta: 1 })
        return
      }
      if (this.fallbackUrl) {
        const isTab = ['/pages/menu/menu', '/pages/today/today', '/pages/my/my'].includes(this.fallbackUrl)
        uni[isTab ? 'switchTab' : 'reLaunch']({
          url: this.fallbackUrl,
          fail: () => uni.reLaunch({ url: this.fallbackUrl })
        })
      } else {
        uni.navigateBack({ delta: 1 })
      }
    }
  }
}
</script>

<style scoped>
.topbar {
  position: relative;
  display: flex;
  align-items: center;
  height: 88rpx;
  padding: 0 24rpx;
  z-index: 100;
}

.topbar--fixed {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
}

.topbar--transparent {
  background: transparent;
}

.topbar--solid {
  background: #ffffff;
  border-bottom: 1rpx solid #f0f2f5;
}

.topbar--gradient {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c5a 100%);
}

.topbar__back {
  flex: none;
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.32);
  transition: all 0.18s ease;
}

.topbar__back--on-light {
  background: #f5f6f8;
}

.topbar__back--hover {
  transform: scale(0.92);
  opacity: 0.78;
}

.topbar__back-icon {
  font-size: 44rpx;
  line-height: 44rpx;
  color: #ffffff;
  font-weight: 600;
  margin-top: -4rpx;
}

.topbar__back--on-light .topbar__back-icon {
  color: #1a1f2e;
}

.topbar__title {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16rpx;
  min-width: 0;
}

.topbar__title-text {
  font-size: 32rpx;
  font-weight: 700;
  color: #1a1f2e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topbar__title-text--light {
  color: #ffffff;
}

.topbar__right {
  flex: none;
  display: flex;
  align-items: center;
  gap: 12rpx;
}
</style>
