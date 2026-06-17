<template>
  <view class="page-layout">
    <view class="page-header">
      <view class="page-header-left">
        <view class="page-header-back" @click.stop="goBack" v-if="showBack">
          <image class="page-header-back-icon" src="/static/back.png" mode="aspectFit" />
        </view>
      </view>
      <text class="page-header-title">{{ title }}</text>
      <view class="page-header-right">
        <slot name="header-right" />
      </view>
    </view>
    <view class="page-content">
      <slot />
    </view>
  </view>
</template>

<script>
export default {
  name: 'PageLayout',
  props: {
    title: {
      type: String,
      default: ''
    },
    showBack: {
      type: Boolean,
      default: true
    }
  },
  methods: {
    goBack() {
      const hasBackHandler = this.$.vnode.props && this.$.vnode.props.onBack
      if (hasBackHandler) {
        this.$emit('back')
        return
      }
      this.$nextTick(() => {
        const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
        if (pages.length > 1) {
          uni.navigateBack({ delta: 1 })
          return
        }

        uni.switchTab({
          url: '/pages/menu/menu',
          fail: () => {
            uni.reLaunch({ url: '/pages/menu/menu' })
          }
        })
      })
    }
  }
}
</script>

<style scoped>
.page-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
  background: #f2f4f7;
}

.page-header {
  display: flex;
  align-items: center;
  flex: none;
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  padding: 12rpx 30rpx 20rpx;
}

.page-header-left {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.page-header-back {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.page-header-back-icon {
  width: 64rpx;
  height: 64rpx;
}

.page-header-title {
  flex: 1;
  text-align: center;
  color: #ffffff;
  font-size: 36rpx;
  font-weight: 600;
}

.page-header-right {
  width: 64rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.page-content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}
</style>
