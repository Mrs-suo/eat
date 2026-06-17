<template>
  <view class="profile-page">
    <view class="profile-top">
      <view class="top-action" @click="goBack">
        <AppIcon name="chevron_right" size="md" tone="inherit" class="back-icon" />
      </view>
      <text class="top-title">个人资料</text>
      <view class="top-action ghost"></view>
    </view>

    <view class="profile-hero">
      <view class="avatar-picker" @click="chooseAvatar">
        <image v-if="form.avatar" class="avatar-image" :src="form.avatar" mode="aspectFill" />
        <MemberAvatar v-else :text="avatarInitial" size="xl" ring />
        <view class="avatar-edit">
          <AppIcon name="edit" size="sm" tone="inherit" />
        </view>
      </view>
      <text class="hero-title">{{ form.nickname || '设置你的名字' }}</text>
      <text class="hero-desc">头像和名字会展示给同一个家庭里的成员</text>
    </view>

    <view class="profile-card">
      <view class="field">
        <text class="field-label">昵称</text>
        <input
          class="field-input"
          v-model="form.nickname"
          maxlength="32"
          placeholder="请输入昵称"
          placeholder-class="input-placeholder"
        />
      </view>
      <view class="field readonly">
        <text class="field-label">手机号</text>
        <text class="field-value">{{ maskedPhone }}</text>
      </view>
      <view class="field readonly">
        <text class="field-label">家庭身份</text>
        <text class="field-value">{{ familyName || '已加入家庭' }}</text>
      </view>
    </view>

    <view class="profile-actions">
      <view class="save-btn" :class="{ disabled: saving }" @click="saveProfile">
        <text>{{ saving ? '保存中...' : '保存资料' }}</text>
      </view>
    </view>
  </view>
</template>

<script>
import AppIcon from '@/components/AppIcon.vue'
import MemberAvatar from '@/components/MemberAvatar.vue'
import { updateUserProfile, uploadFile } from '@/utils/api.js'
import { info, error } from '@/utils/toast.js'

export default {
  components: { AppIcon, MemberAvatar },
  data() {
    return {
      user: null,
      form: {
        nickname: '',
        avatar: ''
      },
      familyName: '',
      saving: false,
      uploading: false
    }
  },
  computed: {
    avatarInitial() {
      const value = this.form.nickname || (this.user && this.user.phone) || '家'
      return value.charAt(0)
    },
    maskedPhone() {
      const phone = String((this.user && this.user.phone) || '')
      if (phone.length !== 11) return phone
      return `${phone.slice(0, 3)}****${phone.slice(7)}`
    }
  },
  onShow() {
    this.loadProfile()
  },
  methods: {
    loadProfile() {
      const user = uni.getStorageSync('currentUser')
      if (!user || !user.userId) {
        uni.redirectTo({ url: '/pages/login/login' })
        return
      }
      this.user = user
      this.form.nickname = user.nickname || ''
      this.form.avatar = user.avatar || ''
      this.familyName = uni.getStorageSync('familyName') || ''
    },
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
        return
      }
      uni.switchTab({ url: '/pages/my/my' })
    },
    chooseAvatar() {
      if (this.uploading) return
      uni.chooseImage({
        count: 1,
        sizeType: ['compressed'],
        sourceType: ['album', 'camera'],
        success: async (res) => {
          const filePath = res.tempFilePaths && res.tempFilePaths[0]
          if (!filePath) return
          this.uploading = true
          uni.showLoading({ title: '上传头像', mask: true })
          try {
            this.form.avatar = await uploadFile(filePath)
          } catch (e) {
            error(e.message || '头像上传失败')
          } finally {
            this.uploading = false
            uni.hideLoading()
          }
        }
      })
    },
    async saveProfile() {
      if (this.saving) return
      const nickname = this.form.nickname.trim()
      if (!nickname) {
        error('请输入昵称')
        return
      }
      this.saving = true
      try {
        const updated = await updateUserProfile(this.user.userId, {
          nickname,
          avatar: this.form.avatar || ''
        })
        const nextUser = { ...this.user, ...updated }
        uni.setStorageSync('currentUser', nextUser)
        uni.setStorageSync('nickname', nextUser.nickname || '')
        this.user = nextUser
        info('资料已保存')
        setTimeout(() => this.goBack(), 350)
      } catch (e) {
        error(e.message || '保存失败')
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style>
.profile-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx 24rpx 56rpx;
  background: var(--gradient-primary-fade-vertical-soft), var(--color-bg-page);
}

.profile-top {
  height: 88rpx;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.top-action {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  background: var(--color-bg-overlay-strong);
  color: var(--color-text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-card-soft);
}

.top-action.ghost {
  opacity: 0;
  pointer-events: none;
}

.back-icon {
  transform: rotate(180deg);
}

.top-title {
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 800;
}

.profile-hero {
  margin-top: 18rpx;
  padding: 44rpx 28rpx 36rpx;
  border-radius: 32rpx;
  background: var(--color-bg-overlay-strong);
  box-shadow: var(--shadow-primary-soft);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.avatar-picker {
  position: relative;
  width: 176rpx;
  height: 176rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image {
  width: 168rpx;
  height: 168rpx;
  border-radius: 50%;
  border: 6rpx solid var(--color-bg-card);
  box-shadow: var(--shadow-primary-soft);
  background: var(--color-bg-page-soft);
}

.avatar-edit {
  position: absolute;
  right: 0;
  bottom: 4rpx;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  background: var(--gradient-primary);
  color: var(--color-bg-card);
  border: 5rpx solid var(--color-bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary-soft);
}

.hero-title {
  margin-top: 28rpx;
  color: var(--color-text-primary);
  font-size: 38rpx;
  font-weight: 800;
}

.hero-desc {
  margin-top: 12rpx;
  color: var(--color-text-tertiary);
  font-size: 24rpx;
  line-height: 36rpx;
}

.profile-card {
  margin-top: 24rpx;
  padding: 4rpx 28rpx;
  border-radius: 28rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card);
}

.field {
  min-height: 112rpx;
  border-bottom: 1rpx solid var(--color-border-light);
  display: flex;
  align-items: center;
  gap: 28rpx;
}

.field:last-child {
  border-bottom: 0;
}

.field-label {
  width: 136rpx;
  color: var(--color-text-secondary);
  font-size: 28rpx;
  font-weight: 700;
}

.field-input {
  flex: 1;
  min-width: 0;
  height: 88rpx;
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 700;
}

.input-placeholder {
  color: var(--color-text-quaternary);
  font-weight: 400;
}

.field-value {
  flex: 1;
  color: var(--color-text-tertiary);
  font-size: 28rpx;
  text-align: right;
}

.profile-actions {
  margin-top: 36rpx;
}

.save-btn {
  height: 96rpx;
  border-radius: 999rpx;
  background: var(--gradient-primary);
  color: var(--color-bg-card);
  font-size: 32rpx;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: var(--shadow-primary-strong);
}

.save-btn:active {
  transform: scale(0.98);
}

.save-btn.disabled {
  opacity: 0.72;
}
</style>
