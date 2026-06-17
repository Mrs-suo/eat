<template>
  <view
    class="fab"
    :class="['fab--' + variant, { 'fab--extended': extended }]"
    :style="fabStyle"
    @click="onClick"
  >
    <text v-if="icon" class="fab-icon">{{ icon }}</text>
    <slot>
      <text v-if="text" class="fab-text">{{ text }}</text>
    </slot>
  </view>
</template>

<script>
/**
 * Floating Action Button
 * 浮动操作按钮 — 替代 menu/today 等页面手写的 .publish-fab
 *
 * Props:
 *   - icon: emoji/字符（如 "+" / "📝"）
 *   - text: 扩展文字（"发布菜品"、"想今天换我做"）
 *   - variant: solid（默认实心橙） | ghost（白底橙边）
 *   - extended: 扩展型（带文字），圆形（仅 icon）默认
 *   - position: bottom-right（默认）| bottom-center
 *   - bottom: 距底部距离 rpx（默认 160，避开 tab-bar）
 *   - customStyle: 透传 style
 */
export default {
  name: 'Fab',
  props: {
    icon: { type: String, default: '+' },
    text: { type: String, default: '' },
    variant: { type: String, default: 'solid' },
    extended: { type: Boolean, default: false },
    position: { type: String, default: 'bottom-right' },
    bottom: { type: [Number, String], default: 160 },
    customStyle: { type: String, default: '' }
  },
  computed: {
    fabStyle() {
      const bottom = typeof this.bottom === 'number' ? `${this.bottom}rpx` : this.bottom
      const pos = this.position === 'bottom-center'
        ? 'left: 50%; transform: translateX(-50%);'
        : 'right: 32rpx;'
      return `${pos} bottom: ${bottom}; ${this.customStyle || ''}`.trim()
    }
  },
  methods: {
    onClick(e) {
      this.$emit('click', e)
    }
  }
}
</script>

<style scoped>
.fab {
  position: fixed;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  transition: transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  user-select: none;
}

.fab--bottom-right { right: 32rpx; }
.fab--bottom-center { left: 50%; transform: translateX(-50%); }

.fab--extended {
  padding: 22rpx 36rpx;
  gap: 8rpx;
  border-radius: 999rpx;
  font-size: 28rpx;
}

.fab:not(.fab--extended) {
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  font-size: 44rpx;
}

.fab--solid {
  background: var(--gradient-primary);
  color: #ffffff;
  box-shadow: var(--shadow-primary);
}

.fab--ghost {
  background: #ffffff;
  color: var(--color-primary);
  box-shadow: var(--shadow-lg);
  border: 2rpx solid rgba(255, 107, 53, 0.2);
}

.fab:active {
  transform: scale(0.95);
}

.fab--bottom-center:active {
  transform: translateX(-50%) scale(0.95);
}

.fab-icon { line-height: 1; }
.fab-text { font-size: 26rpx; }
</style>
