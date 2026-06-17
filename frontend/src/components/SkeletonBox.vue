<template>
  <view
    class="skeleton"
    :class="{ 'skeleton--block': block, 'skeleton--rounded': rounded }"
    :style="boxStyle"
  />
</template>

<script>
export default {
  name: 'SkeletonBox',
  props: {
    width: { type: String, default: '100%' },
    height: { type: String, default: '32rpx' },
    radius: { type: String, default: '8rpx' },
    block: { type: Boolean, default: false },
    rounded: { type: Boolean, default: true }
  },
  computed: {
    boxStyle() {
      return {
        width: this.width,
        height: this.height,
        borderRadius: this.rounded ? this.radius : '0'
      }
    }
  }
}
</script>

<style scoped>
.skeleton {
  position: relative;
  overflow: hidden;
  background: linear-gradient(90deg, #eef0f3 0%, #f7f8fa 50%, #eef0f3 100%);
  background-size: 200% 100%;
  animation: skeleton-shimmer 1.4s ease-in-out infinite;
}

.skeleton--block { display: block; }

@keyframes skeleton-shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>
