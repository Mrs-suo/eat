<template>
  <view class="role-chip" :class="['tone-' + tone, { solid: solid }]">
    <view v-if="dot" class="role-chip-dot"></view>
    <text v-if="emoji" class="role-chip-emoji">{{ emoji }}</text>
    <text class="role-chip-text">{{ text }}</text>
  </view>
</template>

<script>
export default {
  name: 'RoleChip',
  props: {
    text: { type: String, required: true },
    tone: { type: String, default: 'cook' }, // cook | eat | admin | owner | neutral
    dot: { type: Boolean, default: false },
    solid: { type: Boolean, default: false },
    emoji: { type: String, default: '' }
  }
}
</script>

<style scoped>
.role-chip {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  height: 36rpx;
  padding: 0 14rpx;
  border-radius: 999rpx;
  background: rgba(255, 107, 53, 0.12);
  color: #ff6b35;
  font-size: 20rpx;
  font-weight: 700;
  line-height: 1;
  flex: none;
}

.role-chip-dot {
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 8rpx currentColor;
}

.role-chip-emoji {
  font-size: 20rpx;
  line-height: 1;
}

.role-chip-text {
  color: currentColor;
  font-size: 20rpx;
  font-weight: 700;
}

/* tones */
.tone-cook { color: #ff6b35; background: rgba(255, 107, 53, 0.12); }
.tone-eat { color: #5c7cfa; background: rgba(92, 124, 250, 0.12); }
.tone-admin { color: #a855f7; background: rgba(168, 85, 247, 0.12); }
.tone-owner { color: #f59e0b; background: rgba(245, 158, 11, 0.14); }
.tone-neutral { color: #5d6673; background: #f2f4f7; }

/* solid variant */
.role-chip.solid {
  color: #ffffff;
  background: currentColor;
}
.role-chip.solid.cook { background: linear-gradient(135deg, #ff6b35, #ff8c5a); }
.role-chip.solid.eat { background: linear-gradient(135deg, #5c7cfa, #7e96fc); }
.role-chip.solid.admin { background: linear-gradient(135deg, #a855f7, #c084fc); }
.role-chip.solid.owner { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
.role-chip.solid.neutral { background: #94a0b0; }
.role-chip.solid .role-chip-text,
.role-chip.solid .role-chip-dot { color: #ffffff; background: #ffffff; box-shadow: none; }
</style>
