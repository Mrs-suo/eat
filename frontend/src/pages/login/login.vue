<template>
  <view class="login-page">
    <view class="login-top">
      <view class="back-btn" @click="goBack">‹</view>
      <view class="brand-mark">
        <view class="brand-dot"></view>
        <text class="brand-text">吃什么</text>
      </view>
      <text class="title">{{ mode === 'login' ? '手机号登录' : '注册账号' }}</text>
      <text class="subtitle">
        {{ mode === 'login' ? '输入已注册手机号进入账号' : '加入或创建你的家庭' }}
      </text>
    </view>

    <view class="login-card">
      <view class="mode-tabs">
        <view class="mode-tab" :class="{ active: mode === 'login' }" @click="switchMode('login')">登录</view>
        <view class="mode-tab" :class="{ active: mode === 'register' }" @click="switchMode('register')">注册</view>
      </view>

      <!-- 注册：创建/加入家庭 -->
      <view v-if="mode === 'register'" class="family-mode">
        <text class="family-mode-label">你打算怎么开始？</text>
        <view class="family-mode-row">
          <view
            class="family-mode-card"
            :class="{ active: familyMode === 'create' }"
            @click="familyMode = 'create'"
          >
            <view class="fmc-icon fmc-create">+</view>
            <text class="fmc-title">创建家庭</text>
            <text class="fmc-desc">新家，把家人邀请进来</text>
          </view>
          <view
            class="family-mode-card"
            :class="{ active: familyMode === 'join' }"
            @click="familyMode = 'join'"
          >
            <view class="fmc-icon fmc-join">⌂</view>
            <text class="fmc-title">加入家庭</text>
            <text class="fmc-desc">输入家人给的 6 位家庭码</text>
          </view>
        </view>
      </view>

      <view class="field">
        <text class="field-label">手机号</text>
        <input
          class="phone-input"
          v-model="phone"
          type="number"
          maxlength="11"
          placeholder="请输入 11 位手机号"
          placeholder-class="input-placeholder"
        />
      </view>

      <view v-if="mode === 'register'" class="field">
        <text class="field-label">我的昵称</text>
        <input
          class="phone-input"
          v-model="nickname"
          maxlength="12"
          placeholder="在家里的称呼（可不填）"
          placeholder-class="input-placeholder"
        />
      </view>

      <view v-if="mode === 'register' && familyMode === 'create'" class="field">
        <text class="field-label">家庭名</text>
        <input
          class="phone-input"
          v-model="familyName"
          maxlength="20"
          placeholder="比如：我和小红的小家"
          placeholder-class="input-placeholder"
        />
      </view>

      <view v-if="mode === 'register' && familyMode === 'join'" class="field">
        <text class="field-label">家庭码</text>
        <view class="family-code-row">
          <input
            class="phone-input family-code-input"
            v-model="familyCode"
            maxlength="6"
            placeholder="6 位家庭码（不区分大小写）"
            placeholder-class="input-placeholder"
          />
          <view class="preview-btn" :class="{ disabled: !canPreview }" @click="previewFamily">
            预览
          </view>
        </view>
        <view v-if="familyPreview" class="family-preview">
          <text class="fp-title">{{ familyPreview.family.name }}</text>
          <text class="fp-desc">当前 {{ familyPreview.memberCount }} 位成员</text>
        </view>
      </view>

      <button class="submit-btn" :loading="loading" @click="submit">
        {{ mode === 'login' ? '登录' : '注册并加入家庭' }}
      </button>

      <view v-if="mode === 'login'" class="hint-row">
        <text>还没有账号？</text>
        <text class="hint-link" @click="switchMode('register')">去注册</text>
      </view>
      <view v-else class="hint-row">
        <text>已经注册？</text>
        <text class="hint-link" @click="switchMode('login')">去登录</text>
      </view>
    </view>
  </view>
</template>

<script>
import { loginByPhone, registerByPhone, previewFamilyByCode } from '@/utils/api.js'
import { warn, error, info } from '@/utils/toast.js'

export default {
  data() {
    return {
      mode: 'login',
      familyMode: 'create',
      phone: '',
      nickname: '',
      familyName: '',
      familyCode: '',
      familyPreview: null,
      loading: false
    }
  },
  computed: {
    canPreview() {
      return /^[\dA-Za-z]{6}$/.test((this.familyCode || '').trim())
    }
  },
  methods: {
    switchMode(m) {
      this.mode = m
      this.familyPreview = null
    },
    validatePhone(phone) {
      return /^1\d{10}$/.test(String(phone || '').trim())
    },
    async previewFamily() {
      if (!this.canPreview) {
        warn('请输入 6 位家庭码')
        return
      }
      try {
        const res = await previewFamilyByCode(this.familyCode.trim())
        this.familyPreview = res
      } catch (e) {
        this.familyPreview = null
        error(e.message || '家庭码无效')
      }
    },
    async submit() {
      const phone = String(this.phone || '').trim()
      if (!this.validatePhone(phone)) {
        warn('请输入正确的手机号')
        return
      }
      this.loading = true
      try {
        let user, family
        if (this.mode === 'login') {
          const res = await loginByPhone(phone)
          user = res.user
          family = res.family
        } else {
          const payload = {
            phone,
            nickname: this.nickname
          }
          if (this.familyMode === 'create') {
            if (!this.familyName || !this.familyName.trim()) {
              throw new Error('请输入家庭名')
            }
            payload.familyName = this.familyName.trim()
          } else {
            if (!this.canPreview) {
              throw new Error('请输入正确的 6 位家庭码')
            }
            payload.familyCode = this.familyCode.trim()
          }
          const res = await registerByPhone(payload)
          user = res.user
          family = res.family
        }
        this.saveSession(user, family)
        info(this.mode === 'login' ? '登录成功' : '注册成功')
        setTimeout(() => {
          uni.navigateBack()
        }, 500)
      } catch (e) {
        error(e.message || (this.mode === 'login' ? '登录失败' : '注册失败'))
      } finally {
        this.loading = false
      }
    },
    saveSession(user, family) {
      uni.setStorageSync('currentUser', user)
      uni.setStorageSync('userId', user.userId)
      uni.setStorageSync('phone', user.phone)
      uni.setStorageSync('nickname', user.nickname || '')
      if (family) {
        uni.setStorageSync('familyId', family.id)
        uni.setStorageSync('familyCode', family.familyCode)
        uni.setStorageSync('familyName', family.name)
      }
    },
    goBack() {
      const pages = getCurrentPages()
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        uni.switchTab({ url: '/pages/my/my' })
      }
    }
  }
}
</script>

<style>
.login-page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 32rpx 30rpx;
  background: var(--gradient-page);
}

.back-btn {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  color: var(--color-text-primary);
  font-size: 54rpx;
  line-height: 64rpx;
  text-align: center;
  background: var(--color-bg-overlay-light);
}

.brand-mark {
  display: flex;
  align-items: center;
  margin-top: 46rpx;
}

.brand-dot {
  width: 18rpx;
  height: 18rpx;
  margin-right: 12rpx;
  border-radius: 50%;
  background: var(--color-primary);
}

.brand-text {
  color: var(--color-primary);
  font-size: 24rpx;
  font-weight: 800;
}

.title,
.subtitle,
.field-label,
.role-title,
.role-desc,
.family-mode-label,
.fmc-title,
.fmc-desc,
.fp-title,
.fp-desc {
  display: block;
}

.title {
  margin-top: 20rpx;
  color: var(--color-text-primary);
  font-size: 52rpx;
  font-weight: 900;
  line-height: 64rpx;
}

.subtitle {
  margin-top: 12rpx;
  color: var(--color-text-tertiary);
  font-size: 26rpx;
}

.login-card {
  margin-top: 46rpx;
  padding: 28rpx;
  border-radius: 28rpx;
  background: var(--color-bg-card);
  box-shadow: var(--shadow-card-strong);
}

.mode-tabs {
  display: grid;
  grid-template-columns: 1fr 1fr;
  padding: 6rpx;
  border-radius: 999rpx;
  background: var(--color-bg-hover);
}

.mode-tab {
  height: 70rpx;
  border-radius: 999rpx;
  color: var(--color-text-tertiary);
  font-size: 28rpx;
  font-weight: 800;
  line-height: 70rpx;
  text-align: center;
}

.mode-tab.active {
  color: var(--color-bg-card);
  background: var(--color-bg-dark);
}

.family-mode {
  margin-top: 28rpx;
}

.family-mode-label {
  color: var(--color-text-tertiary);
  font-size: 24rpx;
  font-weight: 600;
  margin-bottom: 14rpx;
}

.family-mode-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16rpx;
}

.family-mode-card {
  padding: 22rpx 20rpx;
  border-radius: 20rpx;
  background: var(--color-bg-page-soft);
  border: 2rpx solid transparent;
  transition: all 0.2s ease;
}

.family-mode-card.active {
  background: var(--color-primary-faint);
  border-color: var(--color-primary);
  box-shadow: var(--shadow-primary-soft);
}

.fmc-icon {
  width: 56rpx;
  height: 56rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12rpx;
  border-radius: 16rpx;
  color: var(--color-bg-card);
  font-size: 30rpx;
  font-weight: 800;
}

.fmc-create { background: var(--gradient-primary); }
.fmc-join { background: var(--gradient-info); }

.fmc-title {
  color: var(--color-text-primary);
  font-size: 28rpx;
  font-weight: 800;
}

.fmc-desc {
  margin-top: 6rpx;
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  line-height: 30rpx;
}

.field {
  margin-top: 24rpx;
  padding: 18rpx 24rpx;
  border-radius: 20rpx;
  background: var(--color-bg-page-soft);
}

.field-label {
  color: var(--color-text-tertiary);
  font-size: 22rpx;
  margin-bottom: 6rpx;
  font-weight: 600;
}

.phone-input {
  height: 64rpx;
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 600;
}

.family-code-row {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.family-code-input {
  flex: 1;
  letter-spacing: 4rpx;
  font-family: monospace;
  text-transform: uppercase;
}

.preview-btn {
  flex: none;
  height: 56rpx;
  padding: 0 22rpx;
  border-radius: 999rpx;
  color: var(--color-bg-card);
  font-size: 24rpx;
  font-weight: 700;
  line-height: 56rpx;
  background: var(--gradient-info);
  box-shadow: var(--shadow-info-soft);
  transition: opacity 0.2s ease;
}

.preview-btn.disabled {
  opacity: 0.4;
}

.family-preview {
  margin-top: 14rpx;
  padding: 16rpx 20rpx;
  border-radius: 16rpx;
  background: var(--color-primary-faint);
  border: 1rpx solid var(--color-primary-soft);
}

.fp-title {
  color: var(--color-primary);
  font-size: 26rpx;
  font-weight: 800;
}

.fp-desc {
  margin-top: 4rpx;
  color: var(--color-text-muted);
  font-size: 22rpx;
}

.input-placeholder {
  color: var(--color-text-quaternary);
  font-weight: 400;
}

.submit-btn {
  height: 92rpx;
  margin-top: 36rpx;
  border: 0;
  border-radius: 999rpx;
  color: var(--color-bg-card);
  font-size: 30rpx;
  font-weight: 800;
  line-height: 92rpx;
  background: var(--gradient-primary-strong);
  box-shadow: var(--shadow-primary-strong);
}

.hint-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 24rpx;
  color: var(--color-text-tertiary);
  font-size: 24rpx;
}

.hint-link {
  margin-left: 8rpx;
  color: var(--color-primary);
  font-weight: 800;
}
</style>
