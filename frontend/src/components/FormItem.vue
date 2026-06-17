<template>
  <view class="form-item" :class="{ 'is-clickable': clickable }" @tap="onTap">
    <view v-if="label || desc" class="form-label-wrap">
      <text v-if="label" class="form-label">{{ label }}</text>
      <text v-if="desc" class="form-desc">{{ desc }}</text>
    </view>
    <view class="form-value-wrap">
      <slot>
        <text v-if="value" class="form-value">{{ value }}</text>
        <text v-else-if="placeholder" class="form-value placeholder">{{ placeholder }}</text>
      </slot>
      <text v-if="arrow" class="form-arrow">›</text>
    </view>
  </view>
</template>

<script>
export default {
  name: 'FormItem',
  props: {
    label: { type: String, default: '' },
    desc: { type: String, default: '' },
    value: { type: [String, Number], default: '' },
    placeholder: { type: String, default: '' },
    arrow: { type: Boolean, default: false },
    clickable: { type: Boolean, default: false }
  },
  methods: {
    onTap() {
      if (this.clickable) this.$emit('click')
    }
  }
}
</script>

<style scoped>
.form-item {
  display: flex;
  align-items: center;
  gap: 20rpx;
  min-height: 92rpx;
  padding: 0;
}

.form-item.is-clickable:active {
  opacity: 0.6;
}

.form-label-wrap {
  flex: none;
  max-width: 200rpx;
}

.form-label {
  display: block;
  color: #202633;
  font-size: 28rpx;
  font-weight: 600;
}

.form-desc {
  display: block;
  margin-top: 6rpx;
  color: #a1aab3;
  font-size: 22rpx;
}

.form-value-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8rpx;
}

.form-value {
  max-width: 360rpx;
  color: #202633;
  font-size: 28rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-value.placeholder {
  color: #a1aab3;
}

.form-arrow {
  flex: none;
  color: #c4cad1;
  font-size: 40rpx;
  line-height: 40rpx;
}
</style>
