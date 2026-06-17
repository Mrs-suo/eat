<template>
  <view
    class="divider"
    :class="{ 'divider--inset': inset, 'divider--vertical': vertical }"
    :style="dividerStyle"
  />
</template>

<script>
export default {
  name: 'Divider',
  props: {
    // 缩进
    inset: { type: Boolean, default: false },
    // 垂直分隔线
    vertical: { type: Boolean, default: false },
    // 自定义颜色
    color: { type: String, default: '#f0f2f5' },
    // 厚度
    thickness: { type: String, default: '1rpx' }
  },
  computed: {
    dividerStyle() {
      return {
        backgroundColor: this.color,
        [this.vertical ? 'width' : 'height']: this.thickness
      }
    }
  }
}
</script>

<style scoped>
.divider {
  display: block;
  width: 100%;
  height: 1rpx;
  background-color: #f0f2f5;
}

.divider--inset {
  margin-left: 30rpx;
  margin-right: 30rpx;
  width: auto;
}

.divider--vertical {
  display: inline-block;
  width: 1rpx;
  height: 100%;
  vertical-align: middle;
}
</style>
