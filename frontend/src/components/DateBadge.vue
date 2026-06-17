<template>
  <view
    class="date-badge"
    :class="['date-badge--' + tone, { 'date-badge--lg': size === 'lg' }]"
  >
    <text v-if="weekday" class="date-badge-weekday">{{ weekday }}</text>
    <text class="date-badge-main">{{ main }}</text>
    <text v-if="sub" class="date-badge-sub">{{ sub }}</text>
  </view>
</template>

<script>
import { formatDate, formatYMD } from '@/utils/date.js'

/**
 * DateBadge — 日期徽标
 * 收口 6+ 处手写日期块（menu 卡片底部"📅 06-17" / today 头部"06-17 周二"等）
 *
 * Props:
 *   - date: Date | string | 'today' | null
 *   - format: 'M-D' | 'YYYY-MM-DD' | 'datetime' | 'weekday' | 'auto'
 *            auto = 今天显示"今天 HH:MM" / 昨天"昨天" / 同年"M-D" / 跨年"YYYY-MM-DD"
 *   - tone: 'default' | 'orange' | 'gray'  (默认 default = 中性灰)
 *   - size: 'md' | 'lg'  (lg 显示主号字大 + weekday 行)
 */
export default {
  name: 'DateBadge',
  props: {
    date: { type: [String, Date], default: null },
    format: { type: String, default: 'auto' },
    tone: { type: String, default: 'default' },
    size: { type: String, default: 'md' }
  },
  computed: {
    parsedDate() {
      if (this.date === 'today' || !this.date) return new Date()
      return new Date(this.date)
    },
    isValid() {
      return !isNaN(this.parsedDate.getTime())
    },
    main() {
      if (!this.isValid) return '—'
      const d = this.parsedDate
      if (this.format === 'YYYY-MM-DD') return formatYMD(d)
      if (this.format === 'datetime') {
        const pad = (n) => String(n).padStart(2, '0')
        return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
      }
      return formatDate(d)
    },
    weekday() {
      if (!this.isValid || this.size !== 'lg') return ''
      const map = ['日', '一', '二', '三', '四', '五', '六']
      return `周${map[this.parsedDate.getDay()]}`
    },
    sub() {
      if (!this.isValid) return ''
      const d = this.parsedDate
      const now = new Date()
      const sameDay = d.toDateString() === now.toDateString()
      if (sameDay) return '今天'
      const y = new Date(now)
      y.setDate(y.getDate() - 1)
      if (d.toDateString() === y.toDateString()) return '昨天'
      return ''
    }
  }
}
</script>

<style scoped>
.date-badge {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  padding: 4rpx 12rpx;
  font-size: 22rpx;
  font-weight: 600;
  border-radius: 999rpx;
  background: rgba(31, 42, 55, 0.04);
  color: var(--color-text-secondary);
  line-height: 1.2;
}

.date-badge--lg {
  flex-direction: column;
  align-items: center;
  gap: 2rpx;
  padding: 12rpx 18rpx;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
}

.date-badge--orange {
  background: var(--color-primary-soft);
  color: var(--color-primary);
}

.date-badge--gray {
  background: var(--color-bg-hover);
  color: var(--color-text-tertiary);
}

.date-badge-weekday {
  font-size: 20rpx;
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.date-badge-main {
  font-weight: 800;
}

.date-badge--lg .date-badge-main {
  font-size: 32rpx;
  color: var(--color-text-primary);
  font-weight: 800;
}

.date-badge-sub {
  font-size: 20rpx;
  color: var(--color-primary);
  font-weight: 700;
}
</style>
