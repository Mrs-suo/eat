<template>
  <view
    class="list-card"
    :class="[
      `list-card--${layout}`,
      { 'list-card--clickable': clickable },
      { 'list-card--has-header': hasHeaderSlot }
    ]"
    @click="onClick"
  >
    <view v-if="hasHeaderSlot" class="list-card__header">
      <slot name="header" />
    </view>
    <view class="list-card__body">
      <slot />
    </view>
    <view v-if="$slots.footer" class="list-card__footer">
      <slot name="footer" />
    </view>
  </view>
</template>

<script>
/**
 * 列表卡片 — 统一白色卡片容器
 *
 * 三种布局：
 * - row-image: 横向（图 + 内容），用于菜品卡
 * - column: 纯容器，内容竖排堆叠
 * - compact: 紧凑行（一行高），用于申请记录、订单条目
 *
 * 用法：
 *   <ListCard layout="row-image" @click="goDetail">
 *     <template #header><text>...</text></template>
 *     <image class="lc-thumb" :src="..." />
 *     <view class="lc-content">...</view>
 *   </ListCard>
 *
 *   <ListCard layout="compact">
 *     <text class="lc-name">xxx</text>
 *     <Tag text="已通过" tone="success" />
 *   </ListCard>
 */
export default {
  name: 'ListCard',
  props: {
    layout: {
      type: String,
      default: 'column',
      validator: v => ['row-image', 'column', 'compact'].includes(v)
    },
    clickable: { type: Boolean, default: false }
  },
  computed: {
    hasHeaderSlot() {
      return !!this.$slots.header
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
.list-card {
  position: relative;
  background: #ffffff;
  border-radius: var(--radius-xl);
  border: 1rpx solid var(--color-border-strong);
  box-shadow: var(--shadow-md);
  overflow: hidden;
  transition: transform 0.18s ease;
}

.list-card--clickable:active {
  transform: scale(0.99);
}

/* row-image: 左侧 thumb 容器，右侧 body */
.list-card--row-image {
  display: flex;
  flex-direction: row;
  min-height: 220rpx;
}

.list-card--row-image .list-card__body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* column: 整卡竖排 */
.list-card--column .list-card__body {
  display: block;
}

/* compact: 单行紧凑 */
.list-card--compact {
  border-radius: 18rpx;
  padding: 22rpx 24rpx;
  box-shadow: 0 6rpx 18rpx rgba(20, 30, 40, 0.05);
}

.list-card--compact .list-card__body {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16rpx;
}

/* 顶部高亮 header */
.list-card__header {
  padding: 18rpx 24rpx;
  background: linear-gradient(135deg, var(--color-primary-bg) 0%, #ffeede 100%);
  border-bottom: 1rpx solid rgba(255, 107, 53, 0.08);
}

.list-card--has-header.list-card--row-image {
  display: block;
}

.list-card--has-header.list-card--row-image .list-card__body {
  display: flex;
  flex-direction: row;
}

/* footer 操作栏 */
.list-card__footer {
  border-top: 1rpx solid #f1f3f5;
  padding: 18rpx 24rpx 20rpx;
  background: #fafbfc;
}
</style>
