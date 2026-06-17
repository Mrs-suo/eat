<template>
  <PageLayout title="审核申请">
    <view class="approve-page">
      <view v-if="loading" class="loading">
        <text>加载中...</text>
      </view>

      <EmptyState
        v-else-if="totalPendingCount === 0"
        emoji="🎉"
        title="没有待审核申请"
        hint="家庭暂时没有需要你处理的事项"
      />

      <view v-else class="request-list">
      <view v-if="joinRequests.length" class="audit-section">
        <text class="audit-section-title">家庭加入申请</text>
        <ListCard
          v-for="item in joinRequests"
          :key="'family-' + item.id"
          class="request-card join-card"
        >
          <template #header>
            <view class="request-header">
              <view class="request-header-left">
                <text class="user-emoji">🏠</text>
                <view class="user-meta">
                  <text class="user-name">{{ item.inviteeName || maskPhone(item.inviteePhone) }}</text>
                  <text class="user-sub">{{ item.familyName || '家庭' }} · {{ formatDate(item.createTime) }}</text>
                </view>
              </view>
              <Tag text="入家申请" tone="blue" />
            </view>
          </template>
          <view class="request-content join-content">
            <view class="join-info">
              <view class="info-row">
                <text class="label">申请人</text>
                <text class="value">{{ item.inviteeName || maskPhone(item.inviteePhone) }}</text>
              </view>
              <view class="info-row">
                <text class="label">手机号</text>
                <text class="value">{{ maskPhone(item.inviteePhone) }}</text>
              </view>
              <view class="info-row">
                <text class="label">家庭码</text>
                <text class="value">{{ item.familyCode || '—' }}</text>
              </view>
            </view>
          </view>
          <template #footer>
            <view class="action-btns">
              <view class="btn reject" @click="rejectJoin(item.id)">拒绝</view>
              <view class="btn approve" @click="approveJoin(item.id)">通过</view>
            </view>
          </template>
        </ListCard>
      </view>

      <view v-if="pendingList.length" class="audit-section">
        <text class="audit-section-title">菜品修改申请</text>
      <ListCard
        v-for="item in pendingList"
        :key="'dish-' + item.id"
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
    </view>
  </PageLayout>
</template>

<script>
import PageLayout from '@/components/PageLayout.vue'
import EmptyState from '@/components/EmptyState.vue'
import ListCard from '@/components/ListCard.vue'
import Tag from '@/components/Tag.vue'
import {
  getPendingRequests,
  approveRequest,
  rejectRequest,
  getFamilyMembers,
  getUserFamilies,
  getFamilyInvitations,
  approveJoinRequest,
  rejectJoinRequest
} from '@/utils/api.js'
import { formatShortDateTime } from '@/utils/date.js'
import { success, error, info } from '@/utils/toast.js'

const AVATAR_EMOJIS = ['🐱', '🐶', '🐰', '🦊', '🐼', '🐨', '🐯', '🐸', '🦁', '🐵', '🐔', '🦄']

export default {
  components: { PageLayout, EmptyState, ListCard, Tag },
  data() {
    return {
      pendingList: [],
      joinRequests: [],
      members: [],
      familyId: '',
      familyName: '',
      userId: '',
      loading: false
    }
  },
  computed: {
    totalPendingCount() {
      return this.pendingList.length + this.joinRequests.length
    }
  },
  onShow() {
    const user = uni.getStorageSync('currentUser') || {}
    this.userId = user.userId || uni.getStorageSync('userId') || ''
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    this.loadAll()
  },
  methods: {
    async loadAll() {
      this.loading = true
      try {
        const [pending, members, joinRequests] = await Promise.all([
          this.familyId ? getPendingRequests(this.familyId).catch(() => []) : Promise.resolve([]),
          this.familyId ? getFamilyMembers(this.familyId).catch(() => []) : Promise.resolve([]),
          this.loadJoinRequests()
        ])
        this.pendingList = Array.isArray(pending) ? pending : []
        this.members = Array.isArray(members) ? members : []
        this.joinRequests = Array.isArray(joinRequests) ? joinRequests : []
      } catch (e) {
        console.error('加载申请失败', e)
        this.pendingList = []
        this.joinRequests = []
      } finally {
        this.loading = false
      }
    },
    async loadJoinRequests() {
      if (!this.userId) return []
      const families = await getUserFamilies(this.userId).catch(() => [])
      if (!Array.isArray(families) || !families.length) return []
      const groups = await Promise.all(
        families.map(family =>
          getFamilyInvitations(family.id, this.userId)
            .then(list => (Array.isArray(list) ? list : []))
            .catch(() => [])
        )
      )
      return groups
        .flat()
        .filter(item => item.status === 'PENDING' && item.requestType === 'JOIN_REQUEST')
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
    maskPhone(phone) {
      const value = String(phone || '')
      if (value.length !== 11) return value
      return `${value.slice(0, 3)}****${value.slice(7)}`
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
    },
    async approveJoin(id) {
      uni.showModal({
        title: '确认通过',
        content: '通过后对方会加入该家庭。',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await approveJoinRequest(id, this.userId)
            success('已通过')
            this.loadAll()
          } catch (e) {
            error(e.message || '操作失败')
          }
        }
      })
    },
    rejectJoin(id) {
      uni.showModal({
        title: '拒绝加入',
        content: '确定拒绝这个家庭加入申请吗？',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await rejectJoinRequest(id, this.userId)
            info('已拒绝')
            this.loadAll()
          } catch (e) {
            error(e.message || '操作失败')
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.approve-page {
  min-height: 100%;
  padding: 8rpx 24rpx 40rpx;
}

.request-list {
  display: flex;
  flex-direction: column;
  gap: 22rpx;
  padding: 24rpx;
}

.audit-section {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.audit-section-title {
  display: block;
  color: var(--color-text-primary);
  font-size: var(--font-xl);
  font-weight: 900;
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

.request-content.join-content {
  padding-top: 12rpx;
}

.join-info {
  flex: 1;
  min-width: 0;
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
