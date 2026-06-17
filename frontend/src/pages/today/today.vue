<template>
  <app-shell :show-tab-bar="true" :current-tab="1">
    <template #menu>
      <view class="date-bar">
        <view class="date-nav" @click="changeDate(-1)">
          <text class="nav-arrow">◀</text>
        </view>
        <text class="date" @click="showDatePicker = true">{{ displayDate }}</text>
        <view class="date-nav" :class="{ disabled: isToday }" @click="changeDate(1)">
          <text class="nav-arrow">▶</text>
        </view>
      </view>

      <view class="date-picker-mask" v-if="showDatePicker" @click="showDatePicker = false">
        <view class="date-picker" @click.stop>
          <view class="picker-header">
            <text class="picker-cancel" @click="showDatePicker = false">取消</text>
            <text class="picker-title">选择日期</text>
            <text class="picker-confirm" @click="confirmDate">确定</text>
          </view>
          <picker-view class="picker-view" :value="pickerValue" @change="onPickerChange">
            <picker-view-column>
              <view class="picker-item" v-for="year in years" :key="year">{{ year }}年</view>
            </picker-view-column>
            <picker-view-column>
              <view class="picker-item" v-for="month in months" :key="month">{{ month }}月</view>
            </picker-view-column>
            <picker-view-column>
              <view class="picker-item" v-for="day in days" :key="day">{{ day }}日</view>
            </picker-view-column>
          </picker-view>
        </view>
      </view>

      <SegmentTabs
        v-model="currentMeal"
        :tabs="meals"
        variant="icon-large"
        @change="onMealChange"
      />
    </template>

    <!-- 今日主厨卡（仅当日） -->
    <view v-if="isToday" class="cook-card" @longpress="onCookLongPress">
      <MemberAvatar :text="cookInitial" size="xl" ring />
      <view class="cook-info">
        <view class="cook-row">
          <text class="cook-label">今日主厨</text>
          <RoleChip text="我来做" tone="cook" dot />
        </view>
        <text class="cook-name">{{ todayCookName }}</text>
        <text class="cook-sub">{{ isMeCook ? '今天轮到你下厨，加油！' : '今天 TA 来下厨' }}</text>
      </view>
      <view class="cook-action" @click="switchToMe">
        <text class="cook-action-text">{{ isMeCook ? '你已接任' : '换我来做' }}</text>
      </view>
    </view>

    <view class="section">
      <view class="section-title">
        <text>{{ currentMealName }}菜品</text>
        <text class="section-count">{{ mealDishes.length }} 道</text>
      </view>

      <view class="empty" v-if="mealDishes.length === 0">
        <text class="empty-icon">🍽</text>
        <text class="empty-text">暂无{{ currentMealName }}菜品</text>
        <text class="empty-link" @click="goToMenu">去菜单浏览全部 ›</text>
      </view>

      <view class="dish-list" v-else>
        <ListCard
          v-for="dish in mealDishes"
          :key="dish.id"
          layout="row-image"
          clickable
          @click="goToDetail(dish.id)"
          class="dish-card"
        >
          <image class="dish-image" :src="dish.image || '/static/default-dish.png'" mode="aspectFill" />
          <view class="dish-info">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc" v-if="dish.description">{{ dish.description }}</text>
            <view class="dish-meta">
              <text class="dish-cook">👤 {{ cookNameFor(dish) }}</text>
              <text v-if="dish.price" class="dish-price">¥{{ dish.price }}</text>
            </view>
          </view>
        </ListCard>
      </view>
    </view>

    <template #fab>
    <Fab
      v-if="isToday && isMeCook"
      icon="+"
      :text="'发布' + currentMealName + '菜品'"
      variant="solid"
      extended
      @click="goToPublish"
    />
    <Fab
      v-else-if="isToday && !isMeCook"
      text="想今天换我做"
      variant="ghost"
      extended
      @click="onWantCook"
    />
    </template>
  </app-shell>
</template>

<script>
import AppShell from '@/components/AppShell.vue'
import MemberAvatar from '@/components/MemberAvatar.vue'
import RoleChip from '@/components/RoleChip.vue'
import ListCard from '@/components/ListCard.vue'
import Fab from '@/components/Fab.vue'
import SegmentTabs from '@/components/SegmentTabs.vue'
import { getDishesByMealTime, getTodayCook, switchCook, getFamilyMembers } from '@/utils/api.js'
import { formatDate as _formatDate, formatYMD } from '@/utils/date.js'
import { warn, info, error } from '@/utils/toast.js'

export default {
  components: {
    AppShell,
    ListCard,
    Fab,
    SegmentTabs
  },
  data() {
    const now = new Date()
    const currentYear = now.getFullYear()
    const currentMonth = now.getMonth() + 1
    const currentDay = now.getDate()
    const hour = now.getHours()
    let defaultMeal = 'dinner'
    if (hour >= 5 && hour < 10) defaultMeal = 'breakfast'
    else if (hour >= 10 && hour < 15) defaultMeal = 'lunch'

    return {
      currentMeal: defaultMeal,
      selectedDate: now,
      showDatePicker: false,
      pickerValue: [2, currentMonth - 1, currentDay - 1],
      today: now,
      years: Array.from({ length: 3 }, (_, i) => currentYear - 2 + i),
      maxMonth: currentMonth,
      maxDay: currentDay,
      meals: [
        { value: 'breakfast', label: '早餐', icon: '🌅' },
        { value: 'lunch', label: '午餐', icon: '☀️' },
        { value: 'dinner', label: '晚餐', icon: '🌙' }
      ],
      mealDishes: [],
      familyId: null,
      currentUserId: '',
      todayCook: null,
      members: []
    }
  },
  computed: {
    months() {
      const [yearIdx] = this.pickerValue
      const year = this.years[yearIdx]
      if (year === this.today.getFullYear()) {
        return Array.from({ length: this.maxMonth }, (_, i) => i + 1)
      }
      return Array.from({ length: 12 }, (_, i) => i + 1)
    },
    days() {
      const [yearIdx, monthIdx] = this.pickerValue
      const year = this.years[yearIdx]
      const month = this.months[monthIdx]
      if (year === this.today.getFullYear() && month === this.maxMonth) {
        return Array.from({ length: this.maxDay }, (_, i) => i + 1)
      }
      return Array.from({ length: 31 }, (_, i) => i + 1)
    },
    displayDate() {
      const d = this.selectedDate
      const today = new Date()
      const dateStr = `${d.getMonth() + 1}月${d.getDate()}日`
      if (this.isSameDay(d, today)) {
        return `今天 ${dateStr}`
      } else if (this.isSameDay(d, new Date(today.getTime() - 86400000))) {
        return `昨天 ${dateStr}`
      }
      return dateStr
    },
    isToday() {
      return this.isSameDay(this.selectedDate, this.today)
    },
    currentMealName() {
      const item = this.meals.find(m => m.value === this.currentMeal)
      return item ? item.label : ''
    },
    isMeCook() {
      return this.todayCook && this.todayCook.cookUserId === this.currentUserId
    },
    todayCookName() {
      if (!this.todayCook) return '还没设置'
      const m = this.members.find(u => u.userId === this.todayCook.cookUserId)
      return m ? (m.nickname || m.phone) : this.todayCook.cookUserId
    },
    cookInitial() {
      const name = this.todayCookName || '家'
      return name.charAt(0).toUpperCase()
    }
  },
  onShow() {
    this.loadSession()
    this.loadDishes()
    if (this.isToday && this.familyId) {
      this.loadTodayCook()
      this.loadMembers()
    }
  },
  methods: {
    loadSession() {
      this.familyId = uni.getStorageSync('familyId') || null
      const user = uni.getStorageSync('currentUser')
      this.currentUserId = user ? user.userId : (uni.getStorageSync('userId') || '')
    },
    getMealByTime() {
      const hour = new Date().getHours()
      if (hour >= 5 && hour < 10) return 'breakfast'
      if (hour >= 10 && hour < 15) return 'lunch'
      return 'dinner'
    },
    isSameDay(d1, d2) {
      return d1.getFullYear() === d2.getFullYear() &&
             d1.getMonth() === d2.getMonth() &&
             d1.getDate() === d2.getDate()
    },
    changeDate(offset) {
      const newDate = new Date(this.selectedDate.getTime() + offset * 86400000)
      if (newDate > this.today) return
      this.selectedDate = newDate
      this.loadDishes()
    },
    onPickerChange(e) {
      this.pickerValue = e.detail.value
    },
    confirmDate() {
      const [yearIdx, monthIdx, dayIdx] = this.pickerValue
      const year = this.years[yearIdx]
      const month = this.months[monthIdx]
      const day = this.days[dayIdx]
      const selected = new Date(year, month - 1, day)
      if (selected > this.today) {
        warn('不能选择未来日期')
        return
      }
      this.selectedDate = selected
      this.showDatePicker = false
      this.loadDishes()
    },
    onMealChange(meal) {
      this.currentMeal = meal
      this.loadDishes()
    },
    formatDate(date) {
      return formatYMD(date)
    },
    async loadDishes() {
      try {
        if (!this.familyId) {
          this.mealDishes = []
          return
        }
        this.mealDishes = await getDishesByMealTime(this.currentMeal, this.familyId)
      } catch (e) {
        console.error('加载菜品失败', e)
        this.mealDishes = []
      }
    },
    async loadTodayCook() {
      try {
        this.todayCook = await getTodayCook(this.familyId, this.formatDate(this.today))
      } catch (e) {
        this.todayCook = null
      }
    },
    async loadMembers() {
      try {
        this.members = await getFamilyMembers(this.familyId)
      } catch (e) {
        this.members = []
      }
    },
    cookNameFor(dish) {
      const m = this.members.find(u => u.userId === dish.cookUserId)
      if (m) return m.nickname || m.phone
      return dish.cookUserId || '家人'
    },
    async switchToMe() {
      if (!this.familyId || !this.currentUserId) return
      if (this.isMeCook) {
        info('你已经是今日主厨')
        return
      }
      try {
        this.todayCook = await switchCook(this.familyId, this.currentUserId, this.formatDate(this.today))
        info('已切换为你来做')
      } catch (e) {
        error(e.message || '切换失败')
      }
    },
    onWantCook() {
      uni.showModal({
        title: '今天换你做？',
        content: '切换后今日菜品由你发布',
        success: (res) => {
          if (res.confirm) this.switchToMe()
        }
      })
    },
    onCookLongPress() {
      uni.showActionSheet({
        itemList: this.isMeCook ? ['换 TA 来做'] : ['换我来做'],
        success: (res) => {
          if (res.tapIndex === 0) this.switchToMe()
        }
      })
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/dish-detail?id=${id}` })
    },
    goToPublish() {
      if (!this.familyId) {
        warn('请先加入家庭')
        return
      }
      uni.navigateTo({ url: `/pages/publish/publish?mealTime=${this.currentMeal}` })
    },
    goToMenu() {
      uni.switchTab({ url: '/pages/menu/menu' })
    }
  }
}
</script>

<style scoped>
.date-bar {
  background: var(--gradient-primary);
  padding: 30rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.date-nav { padding: 10rpx 20rpx; }
.date-nav.disabled { opacity: 0.3; }
.nav-arrow { color: white; font-size: 28rpx; }
.date { color: white; font-size: 32rpx; font-weight: bold; min-width: 160rpx; text-align: center; }

.date-picker-mask {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.date-picker { width: 100%; background: white; border-radius: 24rpx 24rpx 0 0; }

.picker-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 30rpx; border-bottom: 1rpx solid #eee;
}

.picker-cancel { color: var(--color-text-tertiary); font-size: 30rpx; }
.picker-title { font-size: 32rpx; font-weight: bold; }
.picker-confirm { color: var(--color-primary); font-size: 30rpx; }

.picker-view { width: 100%; height: 400rpx; }
.picker-item {
  display: flex; align-items: center; justify-content: center; font-size: 28rpx;
}

/* 今日主厨卡 */
.cook-card {
  position: relative;
  margin: 24rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: var(--gradient-primary-fade);
  box-shadow: 0 8rpx 20rpx rgba(255, 107, 53, 0.12);
  display: flex;
  align-items: center;
  gap: 20rpx;
  overflow: hidden;
}

.cook-card::before {
  content: '';
  position: absolute;
  top: -40rpx;
  right: -40rpx;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  filter: blur(20rpx);
}

.cook-info {
  flex: 1;
  min-width: 0;
}

.cook-row {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.cook-label, .cook-name, .cook-sub, .section-title, .section-count, .empty-text, .empty-link, .cook-action-text {
  display: block;
}

.cook-label {
  color: var(--color-primary);
  font-size: 22rpx;
  font-weight: 800;
  letter-spacing: 1rpx;
}

.cook-name {
  margin-top: 8rpx;
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 800;
}

.cook-sub {
  margin-top: 4rpx;
  color: var(--color-text-secondary);
  font-size: 22rpx;
}

.cook-action {
  flex: none;
  padding: 14rpx 22rpx;
  border-radius: 999rpx;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-soft);
  transition: transform 0.15s ease;
}

.cook-action:active { transform: scale(0.96); }

.cook-action-text {
  color: var(--color-bg-card);
  font-size: 24rpx;
  font-weight: 700;
}

/* 菜品区 */
.section { padding: 0 24rpx 24rpx; }
.section-title {
  font-size: 32rpx; font-weight: bold; margin-bottom: 20rpx;
  color: var(--color-text-primary);
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}
.section-count { font-size: 24rpx; color: var(--color-text-tertiary); font-weight: 500; }

.empty {
  text-align: center; padding: 80rpx 30rpx;
  background: var(--color-bg-card); border-radius: 24rpx;
  color: var(--color-text-tertiary);
  display: flex; flex-direction: column; align-items: center;
}
.empty-icon { display: block; font-size: 80rpx; margin-bottom: 16rpx; }
.empty-text { display: block; font-size: 28rpx; color: var(--color-text-tertiary); margin-bottom: 12rpx; }
.empty-link { display: block; font-size: 26rpx; color: var(--color-primary); }

.dish-list {
  display: flex; flex-direction: column; gap: 20rpx;
}

/* ListCard row-image 已提供容器 + 阴影，这里只补图片与内容布局 */
.dish-card {
  /* ListCard 容器自带阴影/圆角，不再重复 */
}

.dish-image {
  width: 200rpx; height: 200rpx; flex: none;
}

.dish-info {
  flex: 1; min-width: 0; padding: 20rpx;
  display: flex; flex-direction: column; gap: 6rpx;
}

.dish-name { font-size: 30rpx; font-weight: bold; color: var(--color-text-primary); }
.dish-desc { font-size: 22rpx; color: var(--color-text-tertiary); line-height: 30rpx; }

.dish-meta {
  margin-top: auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dish-cook { font-size: 22rpx; color: var(--color-text-tertiary); }
.dish-price { font-size: 26rpx; font-weight: 800; color: var(--color-primary); }
</style>
