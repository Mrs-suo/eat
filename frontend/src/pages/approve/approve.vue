<template>
  <view class="container">
    <HeroPanel
      title="家庭审核"
      :subtitle="(familyName || '我的小家') + ' · 待审核 ' + pendingList.length + ' 条'"
      tone="orange"
      fullwidth
    />

    <view v-if="loading" class="loading">
      <text>加载中...</text>
    </view>

    <EmptyState
      v-else-if="pendingList.length === 0"
      emoji="🎉"
      title="没有待审核申请"
      hint="家庭的菜谱暂时稳定啦"
    />

    <view v-else class="request-list">
      <ListCard
        v-for="item in pendingList"
        :key="item.id"
        layout="row-image"
        class="request-card"
      >
        <template #header>
          <view class="request-header">
            <view class="request-header-left">
              <text class="user-emoji">{{ avatarOf(item.userId) }}</text>
              <view class="user-meta">
                <text class="user-name">{{ memberNameOf(item.userId) }}</text>
                <text class="user-sub">{{ item.mealTime ? mealTimeText(item.mealTime) : '菜品' }} · {{ formatDate(item.createTime) }}</text>
              </view>
            </view>
            <Tag
              v-if="item.originalDish"
              text="修改"
              tone="orange"
            />
            <Tag
              v-else
              text="新增"
              tone="green"
            />
          </view>
        </template>
        <view class="request-content">
          <view class="dish-preview" v-if="item.image">
            <image :src="item.image" class="preview-image" mode="aspectFill"></image>
          </view>
          <view class="dish-info">
            <view class="info-row">
              <text class="label">名称</text>
              <text class="value">{{ item.name }}</text>
            </view>
            <view class="info-row" v-if="item.originalDish">
              <text class="label">原菜品</text>
              <text class="value">{{ item.originalDish.name }}</text>
            </view>
            <view class="info-row">
              <text class="label">分类</text>
              <text class="value">{{ item.category || '—' }}</text>
            </view>
            <view class="info-row" v-if="item.description">
              <text class="label">描述</text>
              <text class="value desc">{{ item.description }}</text>
            </view>
          </view>
        </view>
        <template #footer>
          <view class="action-btns">
            <view class="btn reject" @click="reject(item.id)">拒绝</view>
            <view class="btn approve" @click="approve(item.id)">通过</view>
          </view>
        </template>
      </ListCard>
    </view>
  </view>
</template>

<script>
import HeroPanel from '@/components/HeroPanel.vue'
import EmptyState from '@/components/EmptyState.vue'
import ListCard from '@/components/ListCard.vue'
import Tag from '@/components/Tag.vue'
import {
  getPendingRequests,
  approveRequest,
  rejectRequest,
  getFamilyMembers
} from '@/utils/api.js'
import { formatShortDateTime } from '@/utils/date.js'
import { success, error, info } from '@/utils/toast.js'

const AVATAR_EMOJIS = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐵', '🐔', '🦄']

export default {
  components: { HeroPanel, EmptyState, ListCard, Tag },
  data() {
    return {
      pendingList: [],
      members: [],
      familyId: '',
      familyName: '',
      loading: false
    }
  },
  onShow() {
    const user = uni.getStorageSync('currentUser') || {}
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    this.loadAll()
  },
  methods: {
    async loadAll() {
      if (!this.familyId) {
        this.pendingList = []
        return
      }
      this.loading = true
      try {
        const [pending, members] = await Promise.all([
          getPendingRequests(this.familyId).catch(() => []),
          getFamilyMembers(this.familyId).catch(() => [])
        ])
        this.pendingList = Array.isArray(pending) ? pending : []
        this.members = Array.isArray(members) ? members : []
      } catch (e) {
        console.error('加载申请失败', e)
        this.pendingList = []
      } finally {
        this.loading = false
      }
    },
    memberNameOf(userId) {
      if (!userId) return '家人'
      const m = this.members.find(x => String(x.userId) === String(userId))
      return (m && m.nickname) || '家人'
    },
    avatarOf(userId) {
      const name = this.memberNameOf(userId)
      let hash = 0
      for (let i = 0; i < name.length; i++) {
        hash = (hash * 31 + name.charCodeAt(i)) >>> 0
      }
      return AVATAR_EMOJIS[hash % AVATAR_EMOJIS.length]
    },
    mealTimeText(time) {
      const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐' }
      return map[time] || time
    },
    formatDate(input) {
      return formatShortDateTime(input)
    },
    async approve(id) {
      uni.showModal({
        title: '确认通过',
        content: '通过后菜品会更新到家庭菜单。',
        success: async (res) => {
          if (res.confirm) {
            try {
              await approveRequest(id)
              success('已通过')
              this.loadAll()
            } catch (e) {
              error(e.message || '操作失败')
            }
          }
        }
      })
    },
    reject(id) {
      uni.showModal({
        title: '拒绝原因',
        content: '请输入拒绝原因',
        editable: true,
        placeholderText: '例如：口味不符合，可以下次再提',
        success: async (res) => {
          if (res.confirm && res.content) {
            try {
              await rejectRequest(id, res.content)
              info('已拒绝')
              this.loadAll()
            } catch (e) {
              error(e.message || '操作失败')
            }
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: var(--color-bg-page);
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding: 24rpx;
}

.request-card {
  /* ListCard 已提供容器样式，padding 0 让内部 row 完全占满 */
  padding: 0;
}

.request-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.request-header-left {
  display: flex;
  align-items: center;
  min-width: 0;
}

.user-emoji {
  font-size: 36rpx;
  margin-right: 12rpx;
}

.user-meta {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.user-name {
  color: var(--color-text-primary);
  font-size: var(--font-lg);
  font-weight: 800;
}

.user-sub {
  margin-top: 2rpx;
  color: var(--color-text-tertiary);
  font-size: var(--font-sm);
  font-weight: 500;
}

.request-content {
  display: flex;
  padding: 20rpx 24rpx;
  gap: 20rpx;
}

.dish-preview {
  flex: none;
}

.preview-image {
  width: 168rpx;
  height: 168rpx;
  border-radius: var(--radius-md);
  background: var(--color-border-default);
}

.dish-info {
  flex: 1;
  min-width: 0;
}

.info-row {
  display: flex;
  margin-bottom: 8rpx;
  font-size: 26rpx;
  line-height: 36rpx;
}

.label {
  flex: none;
  width: 96rpx;
  color: var(--color-text-tertiary);
  font-weight: 600;
}

.value {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-weight: 600;
}

.value.desc {
  font-weight: 500;
  color: var(--color-text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.action-btns {
  display: flex;
  gap: 18rpx;
}

.btn {
  flex: 1;
  height: 76rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-pill);
  font-size: var(--font-lg);
  font-weight: 700;
}

.btn.reject {
  background: var(--color-bg-hover);
  color: var(--color-text-secondary);
}

.btn.approve {
  color: var(--color-bg-card);
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-soft);
}
</style>
