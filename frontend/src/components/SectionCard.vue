<template>
  <view class="section-card" :class="{ 'is-tight': tight }">
    <view v-if="title || note" class="section-heading">
      <view class="section-title-wrap">
        <text v-if="title" class="section-title">{{ title }}</text>
        <text v-if="subtitle" class="section-subtitle">{{ subtitle }}</text>
      </view>
      <view v-if="note" class="section-note" :class="{ optional: !required }">
        <text class="section-note-text">{{ note }}</text>
      </view>
    </view>
    <view class="section-body">
      <slot />
    </view>
  </view>
</template>

<script>
export default {
  name: 'SectionCard',
  props: {
    title: { type: String, default: '' },
    subtitle: { type: String, default: '' },
    note: { type: String, default: '' },
    required: { type: Boolean, default: true },
    tight: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.section-card {
  padding: 30rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  background: #ffffff;
  box-shadow: 0 8rpx 22rpx rgba(22, 28, 36, 0.05);
}

.section-card.is-tight {
  padding: 20rpx 24rpx;
}

.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12rpx;
}

.section-title-wrap {
  flex: 1;
  min-width: 0;
}

.section-title {
  color: #202633;
  font-size: 30rpx;
  font-weight: 700;
  display: block;
}

.section-subtitle {
  display: block;
  margin-top: 6rpx;
  color: #a1aab3;
  font-size: 22rpx;
}

.section-note {
  flex: none;
  padding: 4rpx 12rpx;
  border-radius: 999rpx;
  background: #fff1ea;
}

.section-note.optional {
  background: #f3f5f7;
}

.section-note-text {
  color: #ff5f36;
  font-size: 22rpx;
}

.section-note.optional .section-note-text {
  color: #9aa3ad;
}
</style>
