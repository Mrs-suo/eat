<template>
  <PageLayout title="家庭管理">
    <view class="family-page">
      <view class="section family-switch">
      <view class="section-head">
        <view>
          <text class="section-title">我的家庭</text>
          <text class="section-desc">切换、创建、编辑当前家庭，并邀请家人加入</text>
        </view>
      </view>
      <scroll-view scroll-x class="family-tabs">
        <view class="family-tabs-inner">
          <view
            v-for="item in families"
            :key="item.id"
            class="family-tab"
            :class="{ active: item.id === currentFamilyId }"
            @click="switchFamily(item)"
          >
            <text class="family-tab-name">{{ item.name }}</text>
            <text class="family-tab-code">{{ item.familyCode }}</text>
          </view>
        </view>
      </scroll-view>
      <view class="create-row">
        <input class="create-input" v-model="newFamilyName" placeholder="新家庭名称" placeholder-class="placeholder" />
        <view class="small-btn" @click="createNewFamily">新建</view>
      </view>

      <view v-if="currentFamily" class="sub-panel">
        <view class="section-head">
          <view>
            <text class="section-title small">当前家庭</text>
            <text class="section-desc">{{ currentFamily.familyCode }} · {{ members.length }} / 6 位成员</text>
          </view>
          <view class="outline-btn" @click="copyCode">复制码</view>
        </view>
        <view class="edit-row">
          <input class="edit-input" v-model="familyName" maxlength="64" placeholder="家庭名称" placeholder-class="placeholder" />
          <view class="small-btn" @click="saveFamilyName">保存</view>
        </view>
      </view>

      <view class="sub-panel">
        <view class="section-head">
          <view>
            <text class="section-title small">邀请家人</text>
            <text class="section-desc">输入对方已注册手机号，对方同意后自动加入</text>
          </view>
        </view>
        <view class="invite-row">
          <input
            class="invite-input"
            v-model="invitePhone"
            type="number"
            maxlength="11"
            placeholder="输入 11 位手机号"
            placeholder-class="placeholder"
          />
          <view class="small-btn" @click="inviteMember">邀请</view>
        </view>
        <view v-if="pendingInvitations.length" class="pending-list">
          <view v-for="item in pendingInvitations" :key="item.id" class="pending-item">
            <text class="pending-main">{{ item.inviteeName || maskPhone(item.inviteePhone) }}</text>
            <text class="pending-sub">邀请中 · {{ maskPhone(item.inviteePhone) }}</text>
          </view>
        </view>
        <view v-if="joinRequests.length" class="join-request-list">
          <text class="mini-title">待审核申请</text>
          <view v-for="item in joinRequests" :key="item.id" class="join-request-item">
            <view>
              <text class="pending-main">{{ item.inviteeName || maskPhone(item.inviteePhone) }}</text>
              <text class="pending-sub">申请加入 · {{ maskPhone(item.inviteePhone) }}</text>
            </view>
            <view class="received-actions compact">
              <view class="plain-btn" @click="rejectJoin(item)">拒绝</view>
              <view class="small-btn" @click="approveJoin(item)">通过</view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <view>
          <text class="section-title">申请加入家庭</text>
          <text class="section-desc">输入对方手机号，或输入 6 位家庭码，提交后等待家庭创建人审核</text>
        </view>
      </view>
      <view class="apply-row">
        <input
          class="apply-input"
          v-model="joinTarget"
          maxlength="11"
          placeholder="手机号 / 家庭码"
          placeholder-class="placeholder"
        />
        <view class="small-btn" @click="applyJoinFamily">申请</view>
      </view>
    </view>

    <view class="section">
      <view class="section-head">
        <view>
          <text class="section-title">成员</text>
          <text class="section-desc">可移除不再属于这个家庭的成员</text>
        </view>
      </view>
      <view class="member-list">
        <view v-for="member in members" :key="member.userId" class="member-row">
          <view class="member-left">
            <image v-if="member.avatar" class="member-avatar-img" :src="member.avatar" mode="aspectFill" />
            <MemberAvatar v-else :text="(member.nickname || member.phone || '家').charAt(0)" size="md" />
            <view class="member-info">
              <text class="member-name">{{ member.nickname || maskPhone(member.phone) }}</text>
              <text class="member-phone">{{ maskPhone(member.phone) }}</text>
            </view>
          </view>
          <view v-if="member.userId !== userId" class="danger-btn" @click="removeMember(member)">移除</view>
          <text v-else class="me-tag">我</text>
        </view>
      </view>
    </view>
    </view>
  </PageLayout>
</template>

<script>
import AppIcon from '@/components/AppIcon.vue'
import MemberAvatar from '@/components/MemberAvatar.vue'
import {
  createFamily,
  getCurrentFamily,
  getUserFamilies,
  getFamilyMembers,
  switchCurrentFamily,
  updateFamily,
  inviteFamilyMember,
  requestJoinFamily,
  getFamilyInvitations,
  approveJoinRequest,
  rejectJoinRequest,
  removeFamilyMember
} from '@/utils/api.js'
import { info, error, warn } from '@/utils/toast.js'

import PageLayout from '@/components/PageLayout.vue'

export default {
  components: { AppIcon, MemberAvatar, PageLayout },
  data() {
    return {
      user: null,
      userId: '',
      currentFamily: null,
      families: [],
      members: [],
      invitations: [],
      familyName: '',
      newFamilyName: '',
      joinTarget: '',
      invitePhone: '',
      loading: false
    }
  },
  computed: {
    currentFamilyId() {
      return this.currentFamily ? this.currentFamily.id : ''
    },
    pendingInvitations() {
      return this.invitations.filter(item => item.status === 'PENDING' && item.requestType !== 'JOIN_REQUEST')
    },
    joinRequests() {
      return this.invitations.filter(item => item.status === 'PENDING' && item.requestType === 'JOIN_REQUEST')
    }
  },
  onShow() {
    this.loadPage()
  },
  methods: {
    async loadPage() {
      const user = uni.getStorageSync('currentUser')
      if (!user || !user.userId) {
        uni.redirectTo({ url: '/pages/login/login' })
        return
      }
      this.user = user
      this.userId = user.userId
      await this.loadFamilies()
      await this.loadCurrentFamily()
      await this.loadFamilyDetail()
    },
    async loadFamilies() {
      this.families = await getUserFamilies(this.userId).catch(() => [])
    },
    async loadCurrentFamily() {
      this.currentFamily = await getCurrentFamily(this.userId).catch(() => null)
      if (!this.currentFamily && this.families.length) {
        this.currentFamily = this.families[0]
      }
      this.familyName = this.currentFamily ? this.currentFamily.name : ''
      this.syncFamilyStorage(this.currentFamily)
    },
    async loadFamilyDetail() {
      if (!this.currentFamilyId) {
        this.members = []
        this.invitations = []
        return
      }
      const [members, invitations] = await Promise.all([
        getFamilyMembers(this.currentFamilyId).catch(() => []),
        getFamilyInvitations(this.currentFamilyId, this.userId).catch(() => [])
      ])
      this.members = members
      this.invitations = invitations
    },
    async switchFamily(item) {
      if (!item || item.id === this.currentFamilyId) return
      try {
        const family = await switchCurrentFamily(item.id, this.userId)
        this.currentFamily = family
        this.familyName = family.name
        this.syncFamilyStorage(family)
        await this.loadFamilyDetail()
        info('已切换家庭')
      } catch (e) {
        error(e.message || '切换失败')
      }
    },
    async createNewFamily() {
      const name = this.newFamilyName.trim()
      if (!name) {
        warn('请输入家庭名称')
        return
      }
      try {
        const family = await createFamily(this.userId, name)
        this.newFamilyName = ''
        await this.loadFamilies()
        await this.switchFamily(family)
        info('家庭已创建')
      } catch (e) {
        error(e.message || '创建失败')
      }
    },
    async applyJoinFamily() {
      const target = this.joinTarget.trim()
      if (!target) {
        warn('请输入手机号或家庭码')
        return
      }
      if (!/^1\d{10}$/.test(target) && !/^[A-Za-z0-9]{6}$/.test(target)) {
        warn('请输入正确的手机号或 6 位家庭码')
        return
      }
      try {
        await requestJoinFamily(this.userId, target)
        this.joinTarget = ''
        info('申请已提交，等待审核')
      } catch (e) {
        error(e.message || '申请失败')
      }
    },
    async saveFamilyName() {
      const name = this.familyName.trim()
      if (!this.currentFamilyId || !name) {
        warn('请输入家庭名称')
        return
      }
      try {
        const family = await updateFamily(this.currentFamilyId, this.userId, name)
        this.currentFamily = family
        this.familyName = family.name
        await this.loadFamilies()
        this.syncFamilyStorage(family)
        info('家庭名称已保存')
      } catch (e) {
        error(e.message || '保存失败')
      }
    },
    inviteMember() {
      const phone = this.invitePhone.trim()
      if (!/^1\d{10}$/.test(phone)) {
        warn('请输入正确的手机号')
        return
      }
      if (!this.currentFamilyId) return
      inviteFamilyMember(this.currentFamilyId, this.userId, phone)
        .then(async () => {
          this.invitePhone = ''
          await this.loadFamilyDetail()
          info('邀请已发送')
        })
        .catch(e => error(e.message || '邀请失败'))
    },
    removeMember(member) {
      uni.showModal({
        title: '移除成员',
        content: `确定移除 ${member.nickname || this.maskPhone(member.phone)} 吗？`,
        confirmText: '移除',
        confirmColor: '#f44336',
        success: async (res) => {
          if (!res.confirm) return
          try {
            await removeFamilyMember(this.currentFamilyId, this.userId, member.userId)
            await this.loadFamilyDetail()
            info('已移除')
          } catch (e) {
            error(e.message || '移除失败')
          }
        }
      })
    },
    async approveJoin(item) {
      try {
        await approveJoinRequest(item.id, this.userId)
        await this.loadFamilyDetail()
        info('已通过申请')
      } catch (e) {
        error(e.message || '审核失败')
      }
    },
    async rejectJoin(item) {
      try {
        await rejectJoinRequest(item.id, this.userId)
        await this.loadFamilyDetail()
        info('已拒绝申请')
      } catch (e) {
        error(e.message || '操作失败')
      }
    },
    copyCode() {
      if (!this.currentFamily) return
      uni.setClipboardData({
        data: this.currentFamily.familyCode,
        success: () => info('家庭码已复制')
      })
    },
    syncFamilyStorage(family) {
      if (!family) return
      const nextUser = { ...this.user, familyId: family.id, familyName: family.name, familyCode: family.familyCode }
      this.user = nextUser
      uni.setStorageSync('currentUser', nextUser)
      uni.setStorageSync('familyId', family.id)
      uni.setStorageSync('familyName', family.name)
      uni.setStorageSync('familyCode', family.familyCode)
    },
    maskPhone(phone) {
      const value = String(phone || '')
      if (value.length !== 11) return value
      return `${value.slice(0, 3)}****${value.slice(7)}`
    }
  }
}
</script>

<style>
.family-page {
  box-sizing: border-box;
  padding: 24rpx 24rpx 56rpx;
  background: var(--gradient-page);
}

.section {
  margin-top: 24rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.section-title,
.section-desc,
.family-tab-name,
.family-tab-code,
.member-name,
.member-phone,
.pending-main,
.pending-sub,
.received-title,
.received-desc,
.empty-line {
  display: block;
}

.section-title {
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 800;
}

.section-title.small {
  font-size: 26rpx;
}

.section-desc {
  margin-top: 6rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  line-height: 32rpx;
}

.family-tabs {
  width: 100%;
  margin-top: 22rpx;
  white-space: nowrap;
}

.sub-panel {
  margin-top: 26rpx;
  padding-top: 24rpx;
  border-top: 1rpx solid var(--color-border-light);
}

.family-tabs-inner {
  display: flex;
  gap: 16rpx;
}

.family-tab {
  min-width: 196rpx;
  padding: 20rpx 22rpx;
  border-radius: 20rpx;
  background: var(--color-bg-page-soft);
  border: 2rpx solid transparent;
}

.family-tab.active {
  background: var(--gradient-primary-fade-soft);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-primary-soft);
}

.family-tab-name {
  color: var(--color-text-primary);
  font-size: 26rpx;
  font-weight: 800;
  max-width: 200rpx;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.family-tab-code {
  margin-top: 8rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  letter-spacing: 1rpx;
}

.create-row,
.edit-row,
.invite-row,
.apply-row {
  margin-top: 22rpx;
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.create-input,
.edit-input,
.invite-input,
.apply-input {
  flex: 1;
  min-width: 0;
  height: 78rpx;
  box-sizing: border-box;
  padding: 0 22rpx;
  border-radius: 18rpx;
  background: var(--color-bg-page-soft);
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 700;
}

.placeholder {
  color: var(--color-text-quaternary);
  font-weight: 400;
}

.small-btn,
.outline-btn,
.plain-btn,
.danger-btn {
  flex: none;
  border-radius: 999rpx;
  font-size: 24rpx;
  font-weight: 800;
  text-align: center;
}

.small-btn {
  padding: 0 26rpx;
  height: 70rpx;
  line-height: 70rpx;
  color: var(--color-bg-card);
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-soft);
}

.outline-btn {
  padding: 0 22rpx;
  height: 58rpx;
  line-height: 58rpx;
  color: var(--color-primary);
  background: var(--color-primary-soft);
}

.plain-btn {
  padding: 0 22rpx;
  height: 64rpx;
  line-height: 64rpx;
  color: var(--color-text-tertiary);
  background: var(--color-bg-page-soft);
}

.danger-btn {
  padding: 0 22rpx;
  height: 58rpx;
  line-height: 58rpx;
  color: var(--color-danger);
  background: var(--color-danger-soft);
}

.pending-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.join-request-list {
  margin-top: 22rpx;
}

.mini-title {
  display: block;
  margin-bottom: 12rpx;
  color: var(--color-text-primary);
  font-size: 24rpx;
  font-weight: 800;
}

.pending-item {
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  background: var(--color-warning-soft);
}

.join-request-item {
  padding: 18rpx 20rpx;
  border-radius: 18rpx;
  background: var(--color-info-soft);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16rpx;
}

.pending-main {
  color: var(--color-text-primary);
  font-size: 26rpx;
  font-weight: 800;
}

.pending-sub {
  margin-top: 4rpx;
  color: var(--color-warning);
  font-size: 22rpx;
}

.member-list,
.invite-card-list {
  margin-top: 18rpx;
  display: flex;
  flex-direction: column;
}

.member-row {
  min-height: 92rpx;
  padding: 18rpx 0;
  border-bottom: 1rpx solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18rpx;
}

.member-row:last-child {
  border-bottom: 0;
}

.member-left {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.member-avatar-img {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  background: var(--color-bg-page-soft);
}

.member-info {
  flex: 1;
  min-width: 0;
}

.member-name {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 800;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.member-phone {
  margin-top: 4rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
}

.me-tag {
  color: var(--color-success);
  font-size: 24rpx;
  font-weight: 800;
}

.received-card {
  padding: 22rpx 0;
  border-bottom: 1rpx solid var(--color-border-light);
}

.received-card:last-child {
  border-bottom: 0;
}

.received-title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.received-desc {
  margin-top: 6rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  line-height: 32rpx;
}

.received-actions.compact {
  margin-top: 0;
  flex: none;
}

.empty-line {
  margin-top: 22rpx;
  color: var(--color-text-tertiary);
  font-size: 24rpx;
  text-align: center;
}
</style>
