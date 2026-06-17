<template>
  <view class="info-card">
    <view v-if="title || $slots.title" class="info-card__head">
      <slot name="title">
        <text class="info-card__title">{{ title }}</text>
      </slot>
      <view v-if="$slots.action" class="info-card__action">
        <slot name="action" />
      </view>
    </view>

    <view class="info-card__body">
      <slot />
    </view>
  </view>
</template>

<script>
export default {
  name: 'InfoCard',
  props: {
    title: { type: String, default: '' }
  }
}
</script>

<style scoped>
.info-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx 30rpx;
  margin-bottom: 24rpx;
  box-shadow: var(--shadow-sm);
}

.info-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20rpx;
}

.info-card__title {
  font-size: 30rpx;
  font-weight: 700;
  color: var(--color-text-primary);
}

.info-card__action {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.info-card__body {
  display: block;
}
</style>
