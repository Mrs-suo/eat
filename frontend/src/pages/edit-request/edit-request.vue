<template>
  <view class="container">
    <HeroPanel
      title="申请菜品变更"
      :subtitle="'家庭：' + (familyName || '未加入家庭') + ' · 当前餐次：' + currentMealName"
      tone="orange"
      fullwidth
    />

    <SectionCard title="变更信息" subtitle="选择修改或新增菜品，提交后由主厨审核">
      <view class="form-item">
        <text class="label">选择变更方式</text>
        <view class="type-tabs">
          <view class="type-tab" :class="{ active: editType === 'modify' }" @click="editType = 'modify'">
            修改现有菜品
          </view>
          <view class="type-tab" :class="{ active: editType === 'new' }" @click="editType = 'new'">
            申请新菜品
          </view>
        </view>
      </view>

      <view class="form-item" v-if="editType === 'modify'">
        <text class="label">选择要修改的菜品</text>
        <picker :range="existingDishes" range-key="name" @change="onDishSelect">
          <view class="picker">
            {{ selectedDish ? selectedDish.name : '请选择菜品' }}
          </view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">菜品名称</text>
        <input class="input" v-model="form.name" placeholder="请输入菜品名称" />
      </view>

      <view class="form-item">
        <text class="label">描述</text>
        <input class="input" v-model="form.description" placeholder="请输入菜品描述" />
      </view>

      <view class="form-item">
        <text class="label">分类</text>
        <picker :range="categories" @change="onCategoryChange">
          <view class="picker">{{ form.category || '请选择分类' }}</view>
        </picker>
      </view>

      <view class="form-item">
        <text class="label">菜品图片</text>
        <view class="upload" @click="chooseImage">
          <image v-if="form.image" :src="form.image" class="preview"></image>
          <view v-else class="upload-btn">
            <text class="upload-icon">📷</text>
            <text>点击上传图片</text>
          </view>
        </view>
      </view>

      <view class="submit-btn" @click="submitRequest">提交申请</view>
    </SectionCard>

    <view class="records-section">
      <view class="section-title">我的申请记录</view>
      <EmptyState
        v-if="myRequests.length === 0"
        emoji="📝"
        title="暂无申请记录"
      />
      <view class="record-list" v-else>
        <ListCard
          v-for="item in myRequests"
          :key="item.id"
          layout="compact"
          class="record-card"
        >
          <text class="record-name">{{ item.name }}</text>
          <Tag :text="statusText(item.status)" :tone="statusTone(item.status)" />
          <view class="record-extra">
            <text class="record-time">{{ formatDate(item.createTime) }}</text>
            <text v-if="item.status === 2 && item.rejectReason" class="record-reason">
              拒绝原因：{{ item.rejectReason }}
            </text>
          </view>
        </ListCard>
      </view>
    </view>
  </view>
</template>

<script>
import HeroPanel from '@/components/HeroPanel.vue'
import EmptyState from '@/components/EmptyState.vue'
import ListCard from '@/components/ListCard.vue'
import Tag from '@/components/Tag.vue'
import {
  getFamilyDishes,
  createEditRequest,
  getRequestsByUserId,
  uploadFile
} from '@/utils/api.js'
import { getCategoryDict } from '@/utils/categoryDict.js'
import { formatShortDateTime } from '@/utils/date.js'
import { warn, error } from '@/utils/toast.js'

export default {
  components: { HeroPanel, EmptyState, ListCard, Tag },
  data() {
    return {
      mealTime: 'breakfast',
      editType: 'modify',
      categories: [],
      existingDishes: [],
      selectedDish: null,
      familyId: '',
      familyName: '',
      userId: '',
      form: {
        name: '',
        description: '',
        category: '',
        image: ''
      },
      myRequests: []
    }
  },
  computed: {
    currentMealName() {
      const map = { breakfast: '早餐', lunch: '午餐', dinner: '晚餐' }
      return map[this.mealTime] || '菜品'
    }
  },
  onLoad(options) {
    this.mealTime = options.mealTime || 'breakfast'
    const user = uni.getStorageSync('currentUser') || {}
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.familyName = user.familyName || uni.getStorageSync('familyName') || ''
    this.userId = user.userId || uni.getStorageSync('userId') || ''
    this.loadCategories()
    this.loadExistingDishes()
    this.loadMyRequests()
  },
  methods: {
    async loadCategories() {
      try {
        this.categories = await getCategoryDict()
      } catch (e) {
        this.categories = ['主食', '小吃', '饮品', '甜点']
      }
    },
    async loadExistingDishes() {
      if (!this.familyId) {
        this.existingDishes = []
        return
      }
      try {
        this.existingDishes = await getFamilyDishes(this.familyId)
      } catch (e) {
        console.error('加载菜品失败', e)
        this.existingDishes = []
      }
    },
    async loadMyRequests() {
      if (!this.userId) {
        this.myRequests = []
        return
      }
      try {
        this.myRequests = await getRequestsByUserId(this.userId)
      } catch (e) {
        console.error('加载申请记录失败', e)
        this.myRequests = []
      }
    },
    onDishSelect(e) {
      this.selectedDish = this.existingDishes[e.detail.value]
      if (this.selectedDish) {
        this.form.name = this.selectedDish.name
        this.form.description = this.selectedDish.description
        this.form.category = this.selectedDish.category
        this.form.image = this.selectedDish.image
      }
    },
    onCategoryChange(e) {
      this.form.category = this.categories[e.detail.value]
    },
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          this.form.image = res.tempFilePaths[0]
        }
      })
    },
    async submitRequest() {
      if (!this.familyId) {
        warn('请先加入家庭')
        return
      }
      if (!this.userId) {
        warn('请先登录')
        return
      }
      if (!this.form.name) {
        warn('请填写菜品名称')
        return
      }
      try {
        let imageUrl = this.form.image
        if (imageUrl && (imageUrl.startsWith('http://tmp') || imageUrl.startsWith('_doc'))) {
          imageUrl = await uploadFile(imageUrl)
        }
        await createEditRequest({
          familyId: this.familyId,
          userId: this.userId,
          originalDish: this.editType === 'modify' ? this.selectedDish : null,
          name: this.form.name,
          description: this.form.description,
          image: imageUrl || '',
          category: this.form.category,
          mealTime: this.mealTime
        })
        success('申请已提交')
        setTimeout(() => {
          this.loadMyRequests()
          this.resetForm()
        }, 1500)
      } catch (e) {
        console.error('提交失败', e)
        error(e.message || '提交失败')
      }
    },
    resetForm() {
      this.form = { name: '', description: '', category: '', image: '' }
      this.selectedDish = null
    },
    statusText(status) {
      const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
      return map[status] || '未知'
    },
    statusTone(status) {
      const map = { 0: 'orange', 1: 'green', 2: 'red' }
      return map[status] || 'gray'
    },
    formatDate(input) {
      return formatShortDateTime(input)
    },
  }
}
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: var(--color-bg-page);
  padding-bottom: 40rpx;
}

.form {
  padding: 30rpx;
}

.form-item {
  background: white;
  padding: 24rpx;
  border-radius: 12rpx;
  margin-bottom: 20rpx;
}

.label {
  display: block;
  font-size: 26rpx;
  color: #666;
  margin-bottom: 12rpx;
}

.type-tabs {
  display: flex;
  gap: 20rpx;
}

.type-tab {
  flex: 1;
  padding: var(--space-4);
  text-align: center;
  background: var(--color-bg-hover);
  border-radius: var(--radius-sm);
  font-size: var(--font-base);
}

.type-tab.active {
  background: var(--color-primary);
  color: white;
}

.input {
  width: 100%;
  font-size: 28rpx;
}

.picker {
  font-size: 28rpx;
  color: #333;
}

.upload {
  width: 100%;
  height: 300rpx;
  background: var(--color-bg-hover);
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
  font-size: 26rpx;
}

.upload-icon {
  font-size: 48rpx;
  margin-bottom: 12rpx;
}

.preview {
  width: 100%;
  height: 100%;
  border-radius: 8rpx;
}

.submit-btn {
  margin-top: 20rpx;
  padding: var(--space-6);
  background: var(--color-primary);
  color: white;
  text-align: center;
  border-radius: var(--radius-sm);
  font-size: 30rpx;
}

.records-section {
  padding: 0 30rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
}

.empty {
  text-align: center;
  padding: 40rpx;
  background: white;
  border-radius: 12rpx;
  color: #999;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

/* record-card: ListCard compact 已提供容器和 padding，这里只补子元素 */
.record-card {
  display: flex;
  flex-direction: row;
  align-items: center;
  flex-wrap: wrap;
  gap: 16rpx;
}

.record-name {
  font-size: var(--font-lg);
  font-weight: 700;
  color: var(--color-text-primary);
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.record-extra {
  flex-basis: 100%;
  display: flex;
  flex-direction: column;
  gap: 6rpx;
  margin-top: 2rpx;
}

.record-time {
  font-size: var(--font-sm);
  color: var(--color-text-tertiary);
}

.record-reason {
  font-size: var(--font-sm);
  color: var(--color-danger);
  background: var(--color-danger-soft);
  padding: 8rpx 12rpx;
  border-radius: var(--radius-sm);
  line-height: 32rpx;
}
</style>
