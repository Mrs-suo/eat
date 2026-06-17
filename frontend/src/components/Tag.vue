<template>
  <text
    class="tag"
    :class="[`tag--${tone}`, `tag--${variant}`, `tag--${size}`]"
  >
    <text v-if="icon" class="tag__icon">{{ icon }}</text>
    <text class="tag__text">{{ text }}</text>
  </text>
</template>

<script>
/**
 * 标签 / Chip — 统一小型色块
 *
 * 用途：餐次、分类、状态、价格、计数等
 *
 * tone: 语义色
 *   orange — 品牌主色
 *   blue   — 信息
 *   green  — 成功
 *   red    — 错误 / 拒绝
 *   gray   — 中性 / 次要
 *
 * variant: 实心 / 柔和
 *   solid — 实心背景
 *   soft  — 柔和（默认）— 浅色背景 + 深色字
 *
 * size: sm / md
 */
export default {
  name: 'Tag',
  props: {
    text: { type: [String, Number], required: true },
    tone: {
      type: String,
      default: 'gray',
      validator: v => ['orange', 'blue', 'green', 'red', 'gray'].includes(v)
    },
    variant: {
      type: String,
      default: 'soft',
      validator: v => ['soft', 'solid'].includes(v)
    },
    size: {
      type: String,
      default: 'sm',
      validator: v => ['sm', 'md'].includes(v)
    },
    icon: { type: String, default: '' }
  }
}
</script>

<style scoped>
.tag {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  flex-shrink: 0;
  border-radius: 999rpx;
  font-weight: 700;
  line-height: 1.2;
  white-space: nowrap;
  vertical-align: middle;
}

.tag__icon {
  margin-right: 4rpx;
}

.tag--sm {
  padding: 6rpx 14rpx;
  font-size: 22rpx;
}

.tag--md {
  padding: 8rpx 18rpx;
  font-size: 24rpx;
}

/* orange */
.tag--orange.tag--soft {
  background: var(--color-primary-soft);
  color: var(--color-primary-deep);
}
.tag--orange.tag--solid {
  background: var(--gradient-primary);
  color: #ffffff;
  box-shadow: var(--shadow-primary-soft);
}

/* blue */
.tag--blue.tag--soft {
  background: var(--color-info-soft);
  color: var(--color-info);
}
.tag--blue.tag--solid {
  background: var(--color-info);
  color: #ffffff;
}

/* green */
.tag--green.tag--soft {
  background: var(--color-success-soft);
  color: var(--color-success);
}
.tag--green.tag--solid {
  background: var(--color-success);
  color: #ffffff;
}

/* red */
.tag--red.tag--soft {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}
.tag--red.tag--solid {
  background: var(--color-danger);
  color: #ffffff;
}

/* gray */
.tag--gray.tag--soft {
  background: var(--color-bg-hover);
  color: var(--color-text-secondary);
}
.tag--gray.tag--solid {
  background: var(--color-border-default);
  color: var(--color-text-primary);
}
</style>
