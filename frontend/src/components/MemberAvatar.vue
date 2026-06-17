<template>
  <view class="member-avatar-wrap" :class="['size-' + size, { 'has-ring': ring }]">
    <view v-if="ring" class="member-avatar-ring"></view>
    <view class="member-avatar-inner" :style="innerStyle">
      <text v-if="text" class="member-avatar-text">{{ text }}</text>
      <text v-else-if="emoji" class="member-avatar-emoji">{{ emoji }}</text>
    </view>
    <view v-if="badge" class="member-avatar-badge" :class="'badge-' + badgeTone">
      <text class="member-avatar-badge-text">{{ badge }}</text>
    </view>
  </view>
</template>

<script>
const COLOR_PALETTE = [
  '#ff6b35', '#5c7cfa', '#22c55e', '#f59e0b', '#a855f7',
  '#ec4899', '#14b8a6', '#f97316', '#3b82f6', '#84cc16'
]

export default {
  name: 'MemberAvatar',
  props: {
    text: { type: String, default: '' },
    emoji: { type: String, default: '' },
    size: { type: String, default: 'md' }, // sm | md | lg | xl
    ring: { type: Boolean, default: false },
    badge: { type: String, default: '' },
    badgeTone: { type: String, default: 'primary' }, // primary | success | warning
    color: { type: String, default: '' }
  },
  computed: {
    innerStyle() {
      if (this.color) return { background: this.color }
      if (this.text) {
        const idx = this.text.charCodeAt(0) % COLOR_PALETTE.length
        return { background: COLOR_PALETTE[idx] }
      }
      return {}
    }
  }
}
</script>

<style scoped>
.member-avatar-wrap {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
}

.member-avatar-inner {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  color: #ffffff;
  font-weight: 700;
  overflow: hidden;
}

.member-avatar-text {
  color: #ffffff;
  font-weight: 700;
}

.member-avatar-emoji {
  font-size: 1em;
  line-height: 1;
}

/* sizes — wrap + inner must both be sized to avoid collapse in flex */
.size-sm { width: 56rpx; height: 56rpx; font-size: 26rpx; }
.size-md { width: 80rpx; height: 80rpx; font-size: 32rpx; }
.size-lg { width: 120rpx; height: 120rpx; font-size: 44rpx; }
.size-xl { width: 168rpx; height: 168rpx; font-size: 60rpx; }

.size-sm .member-avatar-inner { width: 56rpx; height: 56rpx; font-size: 26rpx; }
.size-md .member-avatar-inner { width: 80rpx; height: 80rpx; font-size: 32rpx; }
.size-lg .member-avatar-inner { width: 120rpx; height: 120rpx; font-size: 44rpx; }
.size-xl .member-avatar-inner { width: 168rpx; height: 168rpx; font-size: 60rpx; }

.size-sm .member-avatar-emoji { font-size: 32rpx; }
.size-md .member-avatar-emoji { font-size: 44rpx; }
.size-lg .member-avatar-emoji { font-size: 64rpx; }
.size-xl .member-avatar-emoji { font-size: 96rpx; }

/* ring (conic gradient) */
.member-avatar-ring {
  position: absolute;
  inset: -8rpx;
  border-radius: 50%;
  background: conic-gradient(
    from 0deg,
    #ff6b35, #ff8c5a, #5c7cfa, #22c55e, #ff6b35
  );
  z-index: 0;
  animation: avatar-ring-spin 6s linear infinite;
}

.size-sm .member-avatar-ring { inset: -4rpx; }
.size-lg .member-avatar-ring { inset: -10rpx; }
.size-xl .member-avatar-ring { inset: -12rpx; }

.member-avatar-wrap.has-ring .member-avatar-inner {
  position: relative;
  z-index: 1;
  box-shadow: 0 0 0 6rpx #ffffff;
}

.size-sm.member-avatar-wrap.has-ring .member-avatar-inner { box-shadow: 0 0 0 4rpx #ffffff; }
.size-lg.member-avatar-wrap.has-ring .member-avatar-inner { box-shadow: 0 0 0 8rpx #ffffff; }
.size-xl.member-avatar-wrap.has-ring .member-avatar-inner { box-shadow: 0 0 0 10rpx #ffffff; }

@keyframes avatar-ring-spin {
  to { transform: rotate(360deg); }
}

/* badge (corner dot/label) */
.member-avatar-badge {
  position: absolute;
  right: -6rpx;
  bottom: -6rpx;
  min-width: 36rpx;
  height: 36rpx;
  padding: 0 10rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  border: 4rpx solid #ffffff;
  background: #ff6b35;
  z-index: 2;
}

.size-md .member-avatar-badge { right: -4rpx; bottom: -4rpx; min-width: 32rpx; height: 32rpx; }
.size-lg .member-avatar-badge { min-width: 40rpx; height: 40rpx; right: 0; bottom: 0; }
.size-xl .member-avatar-badge { min-width: 44rpx; height: 44rpx; right: 4rpx; bottom: 4rpx; }

/* size-sm 用小色点，避开与头像重叠；md 及以上才显示文字标 */
.size-sm .member-avatar-badge {
  min-width: 16rpx; height: 16rpx;
  right: 2rpx; bottom: 2rpx;
  padding: 0;
}
.size-sm .member-avatar-badge-text { display: none; }

.badge-success { background: #22c55e; }
.badge-warning { background: #f59e0b; }

.member-avatar-badge-text {
  color: #ffffff;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1;
}
</style>
