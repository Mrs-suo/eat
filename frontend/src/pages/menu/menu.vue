<template>
  <app-shell content-style="padding: 24rpx 24rpx 32rpx;" :show-tab-bar="true" :current-tab="0">
    <template #menu>
      <view class="menu-panel">
        <view class="menu-hero">
          <view class="menu-hero-text">
            <text class="menu-kicker">家庭菜单</text>
            <text class="menu-title">{{ familyName || '我的小家' }}</text>
            <text class="menu-sub">{{ familyCodeLabel }} · 共 {{ memberCount }} 位成员</text>
          </view>
          <view class="menu-count">
            <text class="menu-count-number">{{ filteredDishes.length }}</text>
            <text class="menu-count-label">道菜</text>
          </view>
        </view>

        <!-- 本周小统计 -->
        <view class="weekly-strip" v-if="weekStats.length">
          <view class="weekly-strip-title">本周主厨 · 菜品贡献</view>
          <view class="weekly-strip-list">
            <view
              v-for="(item, idx) in weekStats"
              :key="item.userId"
              class="weekly-chip"
              :class="{ 'weekly-chip--lead': idx === 0 && item.count > 0 }"
            >
              <text class="weekly-chip-emoji">{{ avatarEmoji(item.nickname) }}</text>
              <view class="weekly-chip-info">
                <text class="weekly-chip-name">{{ item.nickname || '成员' }}</text>
                <text class="weekly-chip-count">{{ item.count }} 道</text>
              </view>
            </view>
          </view>
        </view>

        <van-search
          v-model="keyword"
          shape="round"
          background="transparent"
          placeholder="搜索菜品"
          class="dish-search"
        />

        <!-- 家庭菜单 / 我的私房菜 Tab -->
        <SegmentTabs
          v-model="activeMode"
          :tabs="modeTabs"
          variant="pill"
        />

        <van-tabs
          v-model:active="activeCategory"
          line-width="0"
          :color="tabActiveColor"
          :title-active-color="tabActiveTextColor"
          :title-inactive-color="tabInactiveTextColor"
          background="transparent"
          class="category-tabs"
        >
          <van-tab v-for="item in categories" :key="item" :title="item" />
        </van-tabs>
      </view>
    </template>

    <EmptyState
      v-if="filteredDishes.length === 0"
      :emoji="activeMode === 'mine' ? '🍳' : '🍽️'"
      :title="activeMode === 'mine' ? '你还没发布过菜品' : '家庭菜单还是空空的'"
      :hint="activeMode === 'mine' ? '点击右下角发布一道私房菜' : '右下角发布菜品，或邀请家人加入'"
    />

    <view v-else class="dish-list">
      <ListCard
        v-for="dish in filteredDishes"
        :key="dish.id"
        layout="row-image"
        clickable
        @click="goToDetail(dish.id)"
        class="dish-item"
      >
        <view class="dish-image-wrap">
          <image
            class="dish-image"
            :src="dish.image || '/static/default-dish.png'"
            mode="aspectFill"
          />
          <text v-if="dish.category" class="dish-category">{{ dish.category }}</text>
        </view>
        <view class="dish-info">
          <view class="dish-info-top">
            <text class="dish-name">{{ dish.name }}</text>
            <text class="dish-desc">{{ dish.description || '暂无描述' }}</text>
          </view>
          <view class="dish-publisher">
            <text class="dish-publisher-emoji">{{ avatarEmoji(dish.cookNickname) }}</text>
            <text class="dish-publisher-name">{{ dish.cookNickname || '家人' }}</text>
            <text class="dish-publisher-dot">·</text>
            <text class="dish-publisher-date">{{ formatDate(dish.createTime) }}</text>
          </view>
          <view class="dish-meta">
            <Tag v-if="dish.cookDate" :text="mealTimeText(dish.mealTime)" tone="orange" size="sm" />
            <DateBadge v-if="dish.cookDate" :date="dish.cookDate" format="M-D" tone="gray" />
            <text class="dish-arrow">›</text>
          </view>
        </view>
      </ListCard>
    </view>

    <template #fab>
      <view class="fab" @click="goToPublish">
        <text class="fab-icon">+</text>
      </view>
    </template>
  </app-shell>
</template>

<script>
import AppShell from '@/components/AppShell.vue'
import {
  getFamilyDishes,
  getCurrentFamily,
  getFamilyMembers,
  getCookHistory
} from '@/utils/api.js'
import { mergeCategories } from '@/utils/categoryDict.js'
import EmptyState from '@/components/EmptyState.vue'
import ListCard from '@/components/ListCard.vue'
import Tag from '@/components/Tag.vue'
import SegmentTabs from '@/components/SegmentTabs.vue'
import DateBadge from '@/components/DateBadge.vue'
import { formatDate } from '@/utils/date.js'
import { info } from '@/utils/toast.js'

const AVATAR_EMOJIS = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐵', '🐔', '🦄']

export default {
  components: {
    AppShell,
    EmptyState,
    ListCard,
    Tag,
    SegmentTabs,
    DateBadge
  },
  data() {
    return {
      keyword: '',
      activeCategory: 0,
      categories: ['全部'],
      dishes: [],
      members: [],
      familyId: '',
      familyCode: '',
      familyName: '',
      userId: '',
      activeMode: 'family', // family | mine
      weekStats: [],
      tabActiveColor: '#d8705d',
      tabActiveTextColor: '#ffffff',
      tabInactiveTextColor: '#172033'
    }
  },
  computed: {
    modeTabs() {
      return [
        { value: 'family', label: '家庭菜单' },
        { value: 'mine', label: '我的私房菜' }
      ]
    },
    memberCount() {
      return this.members.length
    },
    familyCodeLabel() {
      return this.familyCode ? `邀请码 ${this.familyCode}` : '未加入家庭'
    },
    filteredDishes() {
      let list = this.dishes
      if (this.activeMode === 'mine') {
        list = list.filter(d => String(d.createdByUserId) === String(this.userId))
      }
      const cat = this.categories[this.activeCategory] || '全部'
      if (cat !== '全部') {
        list = list.filter(d => d.category === cat)
      }
      if (this.keyword.trim()) {
        const kw = this.keyword.trim().toLowerCase()
        list = list.filter(d => (d.name || '').toLowerCase().includes(kw))
      }
      return list
    }
  },
  onShow() {
    const user = uni.getStorageSync('currentUser') || {}
    this.userId = user.userId || uni.getStorageSync('userId') || ''
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.familyCode = user.familyCode || uni.getStorageSync('familyCode') || ''
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    this.loadAll()
  },
  methods: {
    async loadAll() {
      if (!this.familyId) {
        this.dishes = []
        this.members = []
        this.weekStats = []
        this.categories = ['全部']
        this.activeCategory = 0
        return
      }
      try {
        const [dishes, family, members, history] = await Promise.all([
          getFamilyDishes(this.familyId),
          getCurrentFamily(this.userId).catch(() => null),
          getFamilyMembers(this.familyId).catch(() => []),
          getCookHistory(this.familyId).catch(() => [])
        ])
        this.dishes = Array.isArray(dishes) ? dishes : []
        if (family) {
          this.familyCode = family.familyCode || this.familyCode
          this.familyName = family.name || this.familyName
        }
        this.members = Array.isArray(members) ? members : []
        this.weekStats = this.buildWeekStats(history)
        const currentCategory = this.categories[this.activeCategory]
        const categories = await mergeCategories(this.dishes.map(d => d.category))
        this.categories = ['全部', ...categories]
        const nextIndex = this.categories.indexOf(currentCategory)
        this.activeCategory = nextIndex >= 0 ? nextIndex : 0
      } catch (e) {
        console.error('加载菜单失败', e)
        this.dishes = []
      }
    },
    buildWeekStats(history) {
      const now = new Date()
      const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
      const counts = new Map()
      ;(history || []).forEach(h => {
        if (!h.cookDate) return
        const t = new Date(h.cookDate)
        if (isNaN(t.getTime()) || t < weekAgo || t > now) return
        const uid = h.cookUserId
        const name = h.cookNickname || '家人'
        const prev = counts.get(uid) || { userId: uid, nickname: name, count: 0 }
        prev.count += 1
        prev.nickname = name
        counts.set(uid, prev)
      })
      // 合并家庭成员中本周没做过菜的
      this.members.forEach(m => {
        if (!counts.has(m.userId)) {
          counts.set(m.userId, { userId: m.userId, nickname: m.nickname || '家人', count: 0 })
        }
      })
      return Array.from(counts.values()).sort((a, b) => b.count - a.count)
    },
    countOf(mode) {
      if (mode === 'mine') {
        return this.dishes.filter(d => String(d.createdByUserId) === String(this.userId)).length
      }
      return this.dishes.length
    },
    mealTimeText(mealTime) {
      const map = {
        breakfast: '早餐',
        lunch: '午餐',
        dinner: '晚餐',
        all: '全天'
      }
      return map[mealTime] || '菜品'
    },
    avatarEmoji(name) {
      if (!name) return '🍳'
      let hash = 0
      for (let i = 0; i < name.length; i++) {
        hash = (hash * 31 + name.charCodeAt(i)) >>> 0
      }
      return AVATAR_EMOJIS[hash % AVATAR_EMOJIS.length]
    },
    goToDetail(id) {
      uni.navigateTo({ url: `/pages/dish-detail/dish-detail?id=${id}` })
    },
    goToPublish() {
      if (!this.familyId) {
        info('请先加入一个家庭')
        uni.switchTab({ url: '/pages/my/my' })
        return
      }
      uni.navigateTo({ url: '/pages/publish/publish' })
    }
  }
}
</script>

<style scoped>
.menu-panel {
  flex: none;
  padding: 28rpx 24rpx 22rpx;
  background: var(--gradient-primary-fade-vertical-soft);
  border-bottom: 1rpx solid var(--color-primary-faint);
}

.menu-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18rpx;
}

.menu-hero-text {
  flex: 1;
  min-width: 0;
}

.menu-kicker,
.menu-title,
.menu-sub,
.menu-count-number,
.menu-count-label {
  display: block;
}

.menu-kicker {
  color: var(--color-primary);
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 6rpx;
}

.menu-title {
  color: var(--color-text-primary);
  font-size: 42rpx;
  font-weight: 800;
  line-height: 52rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.menu-sub {
  margin-top: 6rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  font-weight: 500;
}

.menu-count {
  min-width: 112rpx;
  padding: 12rpx 18rpx;
  border-radius: 18rpx;
  text-align: center;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-primary-soft);
}

.menu-count-number {
  color: var(--color-primary-deep);
  font-size: 34rpx;
  font-weight: 800;
  line-height: 40rpx;
}

.menu-count-label {
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  margin-top: 2rpx;
}

.weekly-strip {
  margin: 6rpx 0 18rpx;
  padding: 18rpx 18rpx 16rpx;
  background: var(--color-bg-card);
  border-radius: 22rpx;
  box-shadow: var(--shadow-card-soft);
  border: 1rpx solid var(--color-border-light);
}

.weekly-strip-title {
  display: block;
  color: var(--color-text-primary);
  font-size: 24rpx;
  font-weight: 700;
  margin-bottom: 12rpx;
}

.weekly-strip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10rpx;
}

.weekly-chip {
  display: flex;
  align-items: center;
  padding: 8rpx 14rpx 8rpx 8rpx;
  background: var(--color-bg-hover);
  border-radius: 999rpx;
  border: 1rpx solid var(--color-border-light);
}

.weekly-chip--lead {
  background: var(--color-primary-soft);
  border-color: rgba(255, 107, 53, 0.18);
  box-shadow: 0 6rpx 14rpx rgba(255, 107, 53, 0.15);
}

.weekly-chip-emoji {
  font-size: 28rpx;
  margin-right: 8rpx;
}

.weekly-chip-info {
  display: flex;
  flex-direction: column;
  line-height: 1.1;
}

.weekly-chip-name {
  color: var(--color-text-primary);
  font-size: 22rpx;
  font-weight: 700;
}

.weekly-chip-count {
  color: var(--color-text-tertiary);
  font-size: 20rpx;
  font-weight: 600;
  margin-top: 2rpx;
}

.dish-search {
  margin: 0 -8rpx;
}

.dish-search :deep(.van-search__content) {
  background: var(--color-bg-hover);
  box-shadow: inset 0 0 0 1rpx rgba(21, 28, 36, 0.03);
}

.category-tabs {
  margin-top: 12rpx;
  padding: 0 4rpx;
}

.category-tabs :deep(.van-tabs__wrap),
.category-tabs :deep(.van-tabs__content),
.category-tabs :deep(.van-tabs__track) {
  height: auto;
  overflow: visible;
}

.category-tabs :deep(.van-tabs__nav) {
  display: grid;
  grid-auto-flow: column;
  grid-auto-columns: minmax(108rpx, max-content);
  gap: 12rpx;
  padding: 0;
  height: 76rpx;
  overflow-x: auto;
  overflow-y: visible;
  align-items: center;
}

.category-tabs :deep(.van-tab) {
  min-width: 0;
  height: 62rpx;
  padding: 0 26rpx;
  background: var(--color-bg-hover);
  border-radius: 999rpx;
  font-size: 26rpx;
  font-weight: 600;
  box-shadow: inset 0 0 0 1rpx rgba(23, 32, 51, 0.04);
}

.category-tabs :deep(.van-tab__text) {
  max-width: 180rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.category-tabs :deep(.van-tab--active) {
  background: var(--gradient-primary);
  color: var(--color-bg-card);
  box-shadow: 0 10rpx 20rpx rgba(255, 107, 53, 0.18);
}

.dish-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding-bottom: 120rpx;
}

/* dish-item: ListCard row-image 已提供容器 + 阴影 */
.dish-item {
  /* container 由 ListCard 提供，padding 0 让 image-wrap 完全占满左侧 */
  padding: 0;
}

.dish-image-wrap {
  position: relative;
  flex: none;
  width: 208rpx;
  min-height: 230rpx;
  overflow: hidden;
  background: var(--color-border-default);
}

.dish-image {
  width: 100%;
  height: 100%;
}

.dish-category {
  position: absolute;
  left: 14rpx;
  bottom: 14rpx;
  max-width: 160rpx;
  padding: 6rpx 14rpx;
  color: var(--color-bg-card);
  font-size: 22rpx;
  font-weight: 700;
  border-radius: 999rpx;
  background: rgba(18, 24, 34, 0.58);
  backdrop-filter: blur(8rpx);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 20rpx 22rpx 18rpx;
}

.dish-info-top {
  display: flex;
  flex-direction: column;
}

.dish-name {
  display: block;
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 800;
  line-height: 42rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dish-desc {
  display: block;
  margin-top: 8rpx;
  color: var(--color-text-tertiary);
  font-size: 24rpx;
  line-height: 34rpx;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.dish-publisher {
  display: flex;
  align-items: center;
  margin-top: 10rpx;
  font-size: 22rpx;
  color: var(--color-text-secondary);
}

.dish-publisher-emoji {
  font-size: 22rpx;
  margin-right: 6rpx;
}

.dish-publisher-name {
  font-weight: 700;
  color: var(--color-text-primary);
}

.dish-publisher-dot {
  margin: 0 6rpx;
  color: var(--color-text-placeholder);
}

.dish-publisher-date {
  color: var(--color-text-tertiary);
  font-weight: 500;
}

.dish-meta {
  display: flex;
  align-items: center;
  margin-top: 12rpx;
  gap: 10rpx;
}

.dish-arrow {
  margin-left: auto;
  color: var(--color-text-placeholder);
  font-size: 42rpx;
  line-height: 42rpx;
}

.fab {
  position: absolute;
  right: 34rpx;
  bottom: 192rpx;
  width: 104rpx;
  height: 104rpx;
  border-radius: 50%;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 99;
}

.fab:active {
  transform: scale(0.92);
}

.fab-icon {
  color: var(--color-bg-card);
  font-size: 56rpx;
  font-weight: 300;
  line-height: 56rpx;
  text-align: center;
}
</style>
