<template>
  <view class="app-shell">
    <view class="app-shell__menu">
      <slot name="menu" />
    </view>

    <view class="app-shell__content" :style="contentStyle">
      <slot />
    </view>

    <view class="tab-bar" v-if="showTabBar">
      <view
        class="tab-item side-item"
        :class="{ active: currentTab === 0 }"
        @click="switchTab(0)"
      >
        <image class="tab-icon" :src="tabIcon(0)" />
        <text class="tab-text">{{ tabs[0].text }}</text>
      </view>

      <view
        class="tab-item center-item"
        :class="{ active: currentTab === 1 }"
        @click="switchTab(1)"
      >
        <view class="center-circle">
          <image class="center-icon" :src="tabs[1].selectedIconPath" />
        </view>
        <text class="tab-text">{{ tabs[1].text }}</text>
      </view>

      <view
        class="tab-item side-item"
        :class="{ active: currentTab === 2 }"
        @click="switchTab(2)"
      >
        <image class="tab-icon" :src="tabIcon(2)" />
        <text class="tab-text">{{ tabs[2].text }}</text>
      </view>
    </view>

    <slot name='fab' />
  </view>
</template>

<script>
export default {
  name: 'AppShell',
  props: {
    contentStyle: {
      type: String,
      default: ''
    },
    showTabBar: {
      type: Boolean,
      default: false
    },
    currentTab: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      tabs: [
        {
          pagePath: '/pages/menu/menu',
          text: '\u83dc\u5355',
          iconPath: '/static/menu.png',
          selectedIconPath: '/static/menu-active.png'
        },
        {
          pagePath: '/pages/today/today',
          text: '\u4eca\u65e5',
          iconPath: '/static/today.png',
          selectedIconPath: '/static/today-active.png'
        },
        {
          pagePath: '/pages/my/my',
          text: '\u6211\u7684',
          iconPath: '/static/my.png',
          selectedIconPath: '/static/my-active.png'
        }
      ]
    }
  },
  methods: {
    tabIcon(index) {
      return this.currentTab === index ? this.tabs[index].selectedIconPath : this.tabs[index].iconPath
    },
    switchTab(index) {
      if (index === this.currentTab) return
      uni.switchTab({ url: this.tabs[index].pagePath })
    }
  }
}
</script>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  height: 100vh;
  min-height: 100vh;
  overflow: hidden;
  position: relative;
  background: #f8f8f8;
}

.app-shell__menu {
  flex: none;
}

.app-shell__content {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.tab-bar {
  position: relative;
  flex: none;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: space-around;
  width: 100%;
  min-height: 112rpx;
  box-sizing: border-box;
  padding: 18rpx 0 max(18rpx, env(safe-area-inset-bottom));
  background: #ffffff;
  border-top: 1rpx solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 -6rpx 18rpx rgba(25, 32, 45, 0.06);
  transform: translateZ(0);
  backface-visibility: hidden;
}

.tab-item {
  flex: 1;
  min-width: 0;
  height: 88rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  color: #a1aab3;
  text-align: center;
}

.side-item {
  gap: 10rpx;
}

.tab-icon {
  width: 42rpx;
  height: 42rpx;
}

.tab-text {
  color: #a1aab3;
  font-size: 24rpx;
  font-weight: 400;
  line-height: 32rpx;
  white-space: nowrap;
}

.tab-item.active .tab-text {
  color: #ff5f36;
  font-weight: 600;
}

.center-item {
  position: relative;
  justify-content: flex-end;
}

.center-circle {
  position: absolute;
  top: -48rpx;
  left: 50%;
  width: 92rpx;
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: linear-gradient(180deg, #fff7ee 0%, #ffe4d4 100%);
  box-shadow: 0 10rpx 24rpx rgba(255, 107, 53, 0.2);
  transform: translateX(-50%);
}

.center-item.active .center-circle {
  background: linear-gradient(180deg, #fff3e8 0%, #ffd9c4 100%);
  box-shadow: 0 12rpx 28rpx rgba(255, 95, 54, 0.26);
}

.center-icon {
  width: 56rpx;
  height: 56rpx;
}
</style>
