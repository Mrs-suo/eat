<template>
  <text class="badge" :class="[`badge--${tone}`]">{{ text }}</text>
</template>

<script>
export default {
  name: 'StatusBadge',
  props: {
    text: { type: String, default: '' },
    tone: {
      type: String,
      default: 'warning',
      validator: v => ['warning','success','danger','info','neutral'].includes(v)
    }
  }
}
</script>

<style scoped>
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6rpx 16rpx;
  font-size: 22rpx;
  font-weight: 700;
  border-radius: 999rpx;
  line-height: 1.4;
}

.badge--warning {
  background: var(--color-primary-soft);
  color: var(--color-primary-deep);
}

.badge--success {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.badge--danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.badge--info {
  background: var(--color-info-soft);
  color: var(--color-info);
}

.badge--neutral {
  background: var(--color-bg-hover);
  color: var(--color-text-tertiary);
}
</style>
