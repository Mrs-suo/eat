<template>
  <!-- pill 变体：水平胶囊（menu 的"家庭菜单/我的私房菜"） -->
  <view v-if="variant === 'pill'" class="seg-tabs seg-tabs--pill">
    <view
      v-for="t in tabs"
      :key="String(t.value)"
      class="seg-pill"
      :class="{ 'seg-pill--active': modelValue === t.value }"
      @click="select(t.value)"
    >
      <text class="seg-pill-text">{{ t.label }}</text>
      <Tag
        v-if="t.count !== undefined"
        :text="t.count"
        :tone="modelValue === t.value ? 'orange' : 'gray'"
        variant="soft"
        size="sm"
      />
    </view>
  </view>

  <!-- icon-large 变体：大图标垂直（today 的"早/午/晚"餐时切换） -->
  <view v-else-if="variant === 'icon-large'" class="seg-tabs seg-tabs--icon-large">
    <view
      v-for="t in tabs"
      :key="String(t.value)"
      class="seg-icon"
      :class="{ 'seg-icon--active': modelValue === t.value }"
      @click="select(t.value)"
    >
      <view class="seg-icon-circle">
        <text class="seg-icon-emoji">{{ t.icon }}</text>
      </view>
      <text class="seg-icon-label">{{ t.label }}</text>
    </view>
  </view>

  <!-- underline 变体：底部下划线（备选） -->
  <view v-else class="seg-tabs seg-tabs--underline">
    <view
      v-for="t in tabs"
      :key="String(t.value)"
      class="seg-underline"
      :class="{ 'seg-underline--active': modelValue === t.value }"
      @click="select(t.value)"
    >
      <text class="seg-underline-text">{{ t.label }}</text>
    </view>
  </view>
</template>

<script>
import Tag from './Tag.vue'

/**
 * SegmentTabs — 分段标签 / Tab 切换
 * 收口 3+ 处手写 tab 系统（menu.mode-tabs / today.meal-tabs / publish.type-tabs）
 *
 * Props:
 *   - modelValue: 当前选中值（v-model）
 *   - tabs: [{ value, label, count?, icon? }]
 *   - variant: 'pill' | 'icon-large' | 'underline'
 */
export default {
  name: 'SegmentTabs',
  components: { Tag },
  props: {
    modelValue: { type: [String, Number], default: '' },
    tabs: { type: Array, required: true },
    variant: { type: String, default: 'pill' }
  },
  emits: ['update:modelValue', 'change'],
  methods: {
    select(v) {
      this.$emit('update:modelValue', v)
      this.$emit('change', v)
    }
  }
}
</script>

<style scoped>
.seg-tabs { display: flex; }

/* ========== pill ========== */
.seg-tabs--pill { gap: 12rpx; }

.seg-pill {
  display: flex;
  align-items: center;
  padding: 12rpx 22rpx;
  background: var(--color-bg-hover);
  border-radius: var(--radius-pill);
  border: 1rpx solid var(--color-border-strong);
  transition: all 0.2s ease;
}

.seg-pill--active {
  background: var(--gradient-primary);
  border-color: transparent;
  box-shadow: 0 10rpx 20rpx rgba(255, 107, 53, 0.18);
}

.seg-pill-text {
  color: var(--color-text-primary);
  font-size: 26rpx;
  font-weight: 700;
}

.seg-pill--active .seg-pill-text { color: #ffffff; }

/* ========== icon-large ========== */
.seg-tabs--icon-large {
  justify-content: center;
  align-items: flex-end;
  background: var(--color-bg-card);
  padding: 30rpx 40rpx 24rpx;
  gap: 48rpx;
}

.seg-icon {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10rpx;
  font-size: 24rpx;
  color: var(--color-text-tertiary);
  transition: all 0.2s;
}

.seg-icon--active { color: var(--color-primary); }

.seg-icon-circle {
  width: 96rpx;
  height: 96rpx;
  border-radius: 50%;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.seg-icon--active .seg-icon-circle {
  background: var(--gradient-primary);
  box-shadow: 0 6rpx 20rpx rgba(255, 107, 53, 0.35);
  transform: scale(1.1);
}

.seg-icon-emoji {
  font-size: 40rpx;
  line-height: 96rpx;
  text-align: center;
}

.seg-icon-label { font-weight: 600; }

/* ========== underline ========== */
.seg-tabs--underline {
  gap: 24rpx;
  border-bottom: 1rpx solid var(--color-border-light);
}

.seg-underline {
  position: relative;
  padding: 16rpx 8rpx;
  font-weight: 600;
}

.seg-underline-text {
  font-size: 28rpx;
  color: var(--color-text-secondary);
}

.seg-underline--active .seg-underline-text {
  color: var(--color-primary);
  font-weight: 800;
}

.seg-underline--active::after {
  content: '';
  position: absolute;
  left: 50%;
  bottom: -1rpx;
  transform: translateX(-50%);
  width: 48rpx;
  height: 4rpx;
  background: var(--color-primary);
  border-radius: 2rpx;
}
</style>
