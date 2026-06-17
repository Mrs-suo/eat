<template>
  <view class="empty">
    <text class="empty__icon">{{ emoji }}</text>
    <text class="empty__title">{{ title }}</text>
    <text v-if="hint" class="empty__hint">{{ hint }}</text>
    <slot />
  </view>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    emoji: { type: String, default: '📭' },
    title: { type: String, default: '暂无数据' },
    hint:  { type: String, default: '' }
  }
}
</script>

<style scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 32rpx;
  color: #8a95a3;
}

.empty__icon {
  font-size: 96rpx;
  margin-bottom: 18rpx;
}

.empty__title {
  font-size: 30rpx;
  font-weight: 700;
  color: #2a3344;
  margin-bottom: 8rpx;
}

.empty__hint {
  font-size: 24rpx;
  color: #98a2ad;
}
</style>
