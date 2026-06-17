<template>
  <view class="hero" :class="[`hero--${tone}`, { 'hero--fullwidth': fullwidth }]">
    <view class="hero__text">
      <text v-if="kicker" class="hero__kicker">{{ kicker }}</text>
      <text class="hero__title">{{ title }}</text>
      <text v-if="subtitle" class="hero__subtitle">{{ subtitle }}</text>
      <slot name="extra" />
    </view>
    <view v-if="badgeEmoji || $slots.badge" class="hero__badge">
      <slot name="badge">
        <text class="hero__badge-emoji">{{ badgeEmoji }}</text>
      </slot>
    </view>
  </view>
</template>

<script>
export default {
  name: 'HeroPanel',
  props: {
    title:    { type: String, default: '' },
    subtitle: { type: String, default: '' },
    kicker:   { type: String, default: '' },
    badgeEmoji: { type: String, default: '' },
    tone:     { type: String, default: 'orange', validator: v => ['orange','blue','dark'].includes(v) },
    fullwidth: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 34rpx 32rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  color: #fff;
  background: linear-gradient(135deg, #ff7246, #ff9a6a);
  box-shadow: 0 12rpx 30rpx rgba(255,107,53,0.18);
}

/* 全宽模式：去掉圆角/阴影/外边距，变成页面顶部 header */
.hero--fullwidth {
  border-radius: 0;
  margin-bottom: 0;
  box-shadow: none;
  padding: 40rpx 30rpx 32rpx;
}

/* 配色方案 */
.hero--orange { background: var(--gradient-primary-soft); box-shadow: 0 12rpx 30rpx rgba(255,107,53,0.18); }
.hero--fullwidth.hero--orange { background: var(--gradient-primary); box-shadow: none; }

.hero--blue   { background: linear-gradient(135deg, #5c7cfa, #7a96ff); box-shadow: 0 12rpx 30rpx rgba(92,124,250,0.18); }
.hero--fullwidth.hero--blue   { box-shadow: none; }

.hero--dark   { background: linear-gradient(135deg, #1a1f2e, #2a3344); box-shadow: 0 12rpx 30rpx rgba(20,30,40,0.18); }
.hero--fullwidth.hero--dark   { box-shadow: none; }

.hero__text {
  flex: 1;
  min-width: 0;
}

.hero__kicker,
.hero__title,
.hero__subtitle {
  display: block;
  color: #ffffff;
}

.hero__kicker {
  font-size: 24rpx;
  opacity: 0.88;
  margin-bottom: 10rpx;
}

.hero__title {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 52rpx;
}

.hero--fullwidth .hero__title {
  font-weight: 800;
}

.hero__subtitle {
  margin-top: 12rpx;
  font-size: 24rpx;
  opacity: 0.82;
  font-weight: 500;
}

.hero--fullwidth .hero__subtitle {
  opacity: 0.86;
}

.hero__badge {
  flex: none;
  width: 104rpx;
  height: 104rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255,255,255,0.22);
}

.hero__badge-emoji {
  font-size: 54rpx;
}
</style>
