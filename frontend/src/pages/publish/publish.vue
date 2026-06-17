<template>
  <PageLayout title="添加菜品" @back="handleBack">
    <view class="publish-page">
        <HeroPanel
          :kicker="mealTimeLabel"
          title="新增菜单菜品"
          subtitle="完善名称、分类和图片，让食客更容易做选择。"
          badge-emoji="🍽️"
        >
          <template v-slot:extra>
            <view v-if="nickname" class="hero-chip">
              <text class="hero-chip-emoji">👨‍🍳</text>
              <text class="hero-chip-text">
                今日主厨：{{ (todayCook && todayCook.cookNickname) || nickname }}
              </text>
            </view>
          </template>
        </HeroPanel>

      <view class="upload-card" @click="handleImageCardClick">
        <image v-if="form.image" :src="form.image" class="preview" mode="aspectFill" />
        <view v-else class="upload-placeholder">
          <view class="upload-mark">
            <text class="upload-plus">+</text>
          </view>
          <text class="upload-title">上传菜品图片</text>
          <text class="upload-hint">建议使用横向实拍图</text>
        </view>
      </view>

      <SectionCard title="基础信息" note="必填">
        <FormItem
          label="菜品分类"
          desc="主食、小吃、饮品或甜点"
          :value="form.category"
          placeholder="请选择"
          arrow
          clickable
          @click="showCategoryPicker = true"
        />

        <Divider />

        <FormItem label="菜品名称" desc="让人一眼看懂">
          <input
            class="form-input"
            v-model="form.name"
            placeholder="例如：番茄牛腩饭"
            placeholder-class="input-placeholder"
          />
        </FormItem>
      </SectionCard>

      <SectionCard title="菜品描述" note="选填" :required="false">
        <textarea
          class="description-input"
          v-model="form.description"
          maxlength="120"
          placeholder="写一点口味、配料或推荐理由..."
          placeholder-class="input-placeholder"
        />
        <view class="counter">{{ form.description.length }}/120</view>
      </SectionCard>

      <view class="submit-button" @click="submit">
        <text>发布菜品</text>
      </view>
    </view>

    <view v-if="showCategoryPicker" class="picker-mask" @click="showCategoryPicker = false">
      <view class="picker-popup" @click.stop>
        <view class="picker-handle"></view>
        <view class="picker-header">
          <text class="picker-title">选择分类</text>
          <text class="picker-close" @click="showCategoryPicker = false">×</text>
        </view>
        <view class="category-search">
          <input
            class="category-search-input"
            v-model="categoryKeyword"
            placeholder="搜索或输入新分类"
            placeholder-class="input-placeholder"
            confirm-type="done"
            @confirm="addCustomCategory"
          />
        </view>
        <view class="picker-list">
          <view
            v-for="item in filteredCategories"
            :key="item"
            :class="['picker-item', form.category === item ? 'active' : '']"
            @click="selectCategory(item)"
          >
            <text>{{ item }}</text>
            <text v-if="form.category === item" class="picker-check">✓</text>
          </view>
          <view v-if="canCreateCategory" class="picker-item create-item" @click="addCustomCategory">
            <text>添加“{{ newCategoryName }}”</text>
            <text class="picker-add">+</text>
          </view>
          <view v-else-if="filteredCategories.length === 0" class="picker-empty">
            <text>没有匹配的分类</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="imageViewerVisible" class="image-viewer-mask" @click="closeImageViewer">
      <view class="image-viewer-popup" @click.stop>
        <view class="image-viewer-header">
          <text class="image-viewer-title">菜品图片</text>
          <text class="image-viewer-close" @click="closeImageViewer">×</text>
        </view>
        <image class="image-viewer-image" :src="form.image" mode="aspectFit" />
        <view class="image-viewer-actions">
          <view class="image-viewer-btn secondary" @click="closeImageViewer">关闭</view>
          <view class="image-viewer-btn primary" @click="replaceImage">更换图片</view>
        </view>
      </view>
    </view>

    <view v-if="cropper.visible" class="crop-mask">
      <view class="crop-popup">
        <view class="crop-header">
          <text class="crop-title">裁剪菜品图片</text>
          <text class="crop-close" @click="cancelCrop">×</text>
        </view>
        <view
          ref="cropFrame"
          class="crop-frame"
          @touchstart="startCropDrag"
          @touchmove.stop.prevent="moveCropDrag"
          @touchend="endCropDrag"
          @mousedown="startCropDrag"
          @mousemove.stop.prevent="moveCropDrag"
          @mouseup="endCropDrag"
          @mouseleave="endCropDrag"
        >
          <image
            class="crop-image"
            :src="cropper.source"
            mode="aspectFill"
            :style="cropImageStyle"
          />
        </view>
        <view class="crop-control">
          <text class="crop-label">缩放</text>
          <slider
            class="crop-slider"
            :value="Math.round(cropper.scale * 100)"
            min="100"
            max="220"
            :activeColor="sliderActiveColor"
            :backgroundColor="sliderBgColor"
            :block-color="sliderActiveColor"
            @changing="onCropScaleChange"
            @change="onCropScaleChange"
          />
        </view>
        <view class="crop-actions">
          <view class="crop-btn secondary" @click="chooseImage">重新选择</view>
          <view class="crop-btn primary" @click="confirmCropAndUpload">确认上传</view>
        </view>
      </view>
    </view>
  </PageLayout>
</template>

<script>
import PageLayout from '@/components/PageLayout.vue'
import HeroPanel from '@/components/HeroPanel.vue'
import SectionCard from '@/components/SectionCard.vue'
import FormItem from '@/components/FormItem.vue'
import Divider from '@/components/Divider.vue'
import {
  createDish,
  shouldUploadFile,
  uploadBlob,
  uploadFile,
  getTodayCook
} from '@/utils/api.js'
import { getCategoryDict, saveCategoryToDict } from '@/utils/categoryDict.js'
import { success, error, info, loading, hideLoading } from '@/utils/toast.js'

export default {
  components: { PageLayout, HeroPanel, SectionCard, FormItem, Divider },
  data() {
    return {
      mealTime: '',
      familyId: '',
      userId: '',
      nickname: '',
      todayCook: null,
      categories: [],
      categoryKeyword: '',
      showCategoryPicker: false,
      imageUploading: false,
      imageViewerVisible: false,
      cropper: {
        visible: false,
        source: '',
        scale: 1.1,
        offsetX: 0,
        offsetY: 0,
        dragging: false,
        dragStartX: 0,
        dragStartY: 0,
        dragOriginX: 0,
        dragOriginY: 0,
        frameWidth: 320,
        frameHeight: 240,
        pinchStartDistance: 0,
        pinchStartScale: 1.1
      },
      form: {
        name: '',
        description: '',
        category: '',
        image: ''
      },
      sliderActiveColor: '#ff6b35',
      sliderBgColor: '#edf0f3'
    }
  },
  computed: {
    newCategoryName() {
      return this.categoryKeyword.trim()
    },
    filteredCategories() {
      const keyword = this.newCategoryName.toLowerCase()
      if (!keyword) return this.categories
      return this.categories.filter(item => item.toLowerCase().includes(keyword))
    },
    canCreateCategory() {
      const name = this.newCategoryName
      if (!name) return false
      return !this.categories.some(item => item.toLowerCase() === name.toLowerCase())
    },
    mealTimeLabel() {
      const labels = {
        breakfast: '早餐上新',
        lunch: '午餐上新',
        dinner: '晚餐上新'
      }
      return labels[this.mealTime] || '菜单上新'
    },
    cropImageStyle() {
      return {
        transform: `translate(${this.cropper.offsetX}px, ${this.cropper.offsetY}px) scale(${this.cropper.scale})`
      }
    }
  },
  onLoad(options) {
    this.mealTime = options.mealTime || ''
    const user = uni.getStorageSync('currentUser') || {}
    this.familyId = user.familyId || uni.getStorageSync('familyId') || ''
    this.userId = user.userId || uni.getStorageSync('userId') || ''
    this.nickname = user.nickname || uni.getStorageSync('nickname') || ''
    this.loadCategories()
    this.loadTodayCook()
  },
  beforeUnmount() {
    this.unbindCropWheel()
  },
  onUnload() {
    this.unbindCropWheel()
  },
  methods: {
    handleBack() {
      if (this.imageViewerVisible) {
        this.closeImageViewer()
        return
      }
      if (this.cropper.visible) {
        this.cancelCrop()
        return
      }
      if (this.showCategoryPicker) {
        this.showCategoryPicker = false
        return
      }

      const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : []
      if (pages.length > 1) {
        uni.navigateBack({ delta: 1 })
        return
      }

      uni.switchTab({
        url: '/pages/menu/menu',
        fail: () => {
          uni.reLaunch({ url: '/pages/menu/menu' })
        }
      })
    },
    async loadCategories() {
      this.categories = await getCategoryDict()
    },
    async loadTodayCook() {
      if (!this.familyId) {
        this.todayCook = null
        return
      }
      try {
        this.todayCook = await getTodayCook(this.familyId)
      } catch (e) {
        console.warn('加载今日主厨失败', e)
        this.todayCook = null
      }
    },
    selectCategory(item) {
      this.form.category = item
      this.categoryKeyword = ''
      this.showCategoryPicker = false
    },
    async addCustomCategory() {
      const name = this.newCategoryName
      if (!name) return
      this.categories = await saveCategoryToDict(name)
      this.selectCategory(name)
    },
    handleImageCardClick() {
      if (this.form.image) {
        this.imageViewerVisible = true
        return
      }
      this.chooseImage()
    },
    closeImageViewer() {
      this.imageViewerVisible = false
    },
    replaceImage() {
      this.imageViewerVisible = false
      this.chooseImage()
    },
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: (res) => {
          const filePath = res.tempFilePaths[0]
          this.cropper.source = filePath
          this.cropper.scale = 1.1
          this.cropper.offsetX = 0
          this.cropper.offsetY = 0
          this.cropper.visible = true
          this.$nextTick(() => {
            this.bindCropWheel()
          })
        }
      })
    },
    onCropScaleChange(e) {
      this.setCropScale(Number(e.detail.value) / 100)
    },
    setCropScale(scale) {
      this.cropper.scale = Math.max(1, Math.min(2.2, scale))
      this.limitCropOffset()
    },
    onCropWheel(e) {
      if (e && e.preventDefault) {
        e.preventDefault()
      }
      if (!this.cropper.visible) return
      const delta = typeof e.wheelDelta === 'number'
        ? e.wheelDelta
        : (typeof e.deltaY === 'number' ? -e.deltaY : -e.detail)
      const nextScale = this.cropper.scale + (delta > 0 ? 0.1 : -0.1)
      this.setCropScale(nextScale)
    },
    bindCropWheel() {
      if (this._cropWheelHandler || typeof document === 'undefined') return

      this._cropWheelHandler = (event) => {
        const frame = event.target && event.target.closest
          ? event.target.closest('.crop-frame')
          : null
        if (!frame) return
        this.onCropWheel(event)
      }
      document.addEventListener('wheel', this._cropWheelHandler, { passive: false })
      document.addEventListener('mousewheel', this._cropWheelHandler, { passive: false })
    },
    unbindCropWheel() {
      if (!this._cropWheelHandler || typeof document === 'undefined') return

      document.removeEventListener('wheel', this._cropWheelHandler)
      document.removeEventListener('mousewheel', this._cropWheelHandler)
      this._cropWheelHandler = null
    },
    getTouchDistance(e) {
      if (!e.touches || e.touches.length < 2) return 0
      const first = e.touches[0]
      const second = e.touches[1]
      return Math.hypot(second.clientX - first.clientX, second.clientY - first.clientY)
    },
    getPointerPosition(e) {
      const point = e.touches && e.touches.length ? e.touches[0] : e
      return {
        x: point.clientX || 0,
        y: point.clientY || 0
      }
    },
    startCropDrag(e) {
      if (e.touches && e.touches.length >= 2) {
        this.cropper.dragging = false
        this.cropper.pinchStartDistance = this.getTouchDistance(e)
        this.cropper.pinchStartScale = this.cropper.scale
        return
      }
      const point = this.getPointerPosition(e)
      const rect = e.currentTarget && e.currentTarget.getBoundingClientRect
        ? e.currentTarget.getBoundingClientRect()
        : null
      if (rect) {
        this.cropper.frameWidth = rect.width
        this.cropper.frameHeight = rect.height
      }
      this.cropper.dragging = true
      this.cropper.dragStartX = point.x
      this.cropper.dragStartY = point.y
      this.cropper.dragOriginX = this.cropper.offsetX
      this.cropper.dragOriginY = this.cropper.offsetY
    },
    moveCropDrag(e) {
      if (e.touches && e.touches.length >= 2) {
        const distance = this.getTouchDistance(e)
        if (this.cropper.pinchStartDistance > 0) {
          this.setCropScale(this.cropper.pinchStartScale * (distance / this.cropper.pinchStartDistance))
        }
        return
      }
      if (!this.cropper.dragging) return
      const point = this.getPointerPosition(e)
      this.cropper.offsetX = this.cropper.dragOriginX + point.x - this.cropper.dragStartX
      this.cropper.offsetY = this.cropper.dragOriginY + point.y - this.cropper.dragStartY
      this.limitCropOffset()
    },
    endCropDrag() {
      this.cropper.dragging = false
      this.cropper.pinchStartDistance = 0
    },
    limitCropOffset() {
      const maxOffsetX = (this.cropper.frameWidth / 2) * Math.max(this.cropper.scale - 1, 0)
      const maxOffsetY = (this.cropper.frameHeight / 2) * Math.max(this.cropper.scale - 1, 0)
      this.cropper.offsetX = Math.max(-maxOffsetX, Math.min(maxOffsetX, this.cropper.offsetX))
      this.cropper.offsetY = Math.max(-maxOffsetY, Math.min(maxOffsetY, this.cropper.offsetY))
    },
    cancelCrop() {
      this.unbindCropWheel()
      this.cropper.visible = false
      this.cropper.source = ''
    },
    async confirmCropAndUpload() {
      if (!this.cropper.source || this.imageUploading) return
      this.imageUploading = true
      uni.showLoading({ title: '上传中' })
      try {
        const blob = await this.createCroppedBlob(
          this.cropper.source,
          this.cropper.scale,
          this.cropper.offsetX,
          this.cropper.offsetY,
          this.cropper.frameWidth,
          this.cropper.frameHeight
        )
        this.form.image = await uploadBlob(blob, 'dish-image.jpg')
        this.unbindCropWheel()
        this.cropper.visible = false
        this.cropper.source = ''
        success('上传成功')
      } catch (e) {
        console.error('图片上传失败', e)
        error('图片上传失败')
      } finally {
        this.imageUploading = false
        hideLoading()
      }
    },
    createCroppedBlob(source, zoom, offsetX = 0, offsetY = 0, frameWidth = 320, frameHeight = 240) {
      return new Promise((resolve, reject) => {
        if (typeof document === 'undefined') {
          reject(new Error('当前环境暂不支持裁剪'))
          return
        }

        const image = new Image()
        image.onload = () => {
          const width = 1000
          const height = 750
          const canvas = document.createElement('canvas')
          canvas.width = width
          canvas.height = height

          const context = canvas.getContext('2d')
          const baseScale = Math.max(width / image.naturalWidth, height / image.naturalHeight)
          const scale = baseScale * zoom
          const drawWidth = image.naturalWidth * scale
          const drawHeight = image.naturalHeight * scale
          const dx = (width - drawWidth) / 2 + (offsetX / frameWidth) * width
          const dy = (height - drawHeight) / 2 + (offsetY / frameHeight) * height

          context.fillStyle = '#ffffff'
          context.fillRect(0, 0, width, height)
          context.drawImage(image, dx, dy, drawWidth, drawHeight)
          canvas.toBlob((blob) => {
            if (blob) {
              resolve(blob)
            } else {
              reject(new Error('裁剪失败'))
            }
          }, 'image/jpeg', 0.9)
        }
        image.onerror = reject
        image.src = source
      })
    },
    async submit() {
      const user = uni.getStorageSync('currentUser') || {}
      if (!this.familyId || !this.userId) {
        warn('请先登录并加入家庭')
        return
      }
      if (!this.form.category) {
        warn('请选择菜品分类')
        return
      }
      if (!this.form.name.trim()) {
        warn('请填写菜品名称')
        return
      }
      if (this.imageUploading) {
        info('图片上传中，请稍候')
        return
      }
      try {
        this.categories = await saveCategoryToDict(this.form.category)
        let imageUrl = this.form.image
        if (shouldUploadFile(imageUrl)) {
          imageUrl = await uploadFile(imageUrl)
        }
        // cookUserId 优先用今日主厨（家庭共享菜单归属），createdByUserId 记录实际发布人
        const cookUserId = (this.todayCook && this.todayCook.cookUserId) || this.userId
        await createDish({
          ...this.form,
          name: this.form.name.trim(),
          description: this.form.description.trim(),
          image: imageUrl || '',
          familyId: this.familyId,
          cookUserId,
          createdByUserId: this.userId,
          mealTime: this.mealTime || 'all',
          available: true
        })
        success('发布成功')
        setTimeout(() => {
          uni.navigateBack()
        }, 1200)
      } catch (e) {
        console.error('发布失败', e)
        error('发布失败')
      }
    }
  }
}
</script>

<style scoped>
.publish-page {
  min-height: 100%;
  padding: 28rpx 28rpx 48rpx;
  background: var(--color-bg-page);
}

.hero-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 34rpx 32rpx;
  margin-bottom: 24rpx;
  border-radius: 24rpx;
  background: var(--gradient-primary-strong);
  box-shadow: var(--shadow-primary-strong);
}

.hero-text {
  flex: 1;
  min-width: 0;
}

.hero-chip {
  display: inline-flex;
  align-items: center;
  margin-top: 16rpx;
  padding: 6rpx 16rpx 6rpx 10rpx;
  border-radius: 999rpx;
  background: var(--color-bg-overlay-medium);
}

.hero-chip-emoji {
  font-size: 24rpx;
  margin-right: 6rpx;
}

.hero-chip-text {
  color: var(--color-bg-card);
  font-size: 22rpx;
  font-weight: 600;
}

.hero-kicker,
.hero-title,
.hero-subtitle {
  display: block;
  color: var(--color-bg-card);
}

.hero-kicker {
  font-size: 24rpx;
  opacity: 0.88;
  margin-bottom: 10rpx;
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
  line-height: 52rpx;
}

.hero-subtitle {
  margin-top: 12rpx;
  font-size: 24rpx;
  opacity: 0.82;
}

.hero-badge {
  flex: none;
  width: 104rpx;
  height: 104rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-bg-overlay-medium);
}

.hero-badge-icon {
  font-size: 54rpx;
}

.upload-card {
  height: 320rpx;
  margin-bottom: 24rpx;
  overflow: hidden;
  border-radius: 24rpx;
  background: var(--color-bg-card);
  border: 2rpx dashed var(--color-primary-light);
  box-shadow: var(--shadow-card-soft);
}

.preview {
  width: 100%;
  height: 100%;
}

.upload-placeholder {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.upload-mark {
  width: 86rpx;
  height: 86rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: var(--color-primary-soft);
  color: var(--color-primary-deep);
  margin-bottom: 18rpx;
}

.upload-plus {
  font-size: 52rpx;
  line-height: 52rpx;
}

.upload-title {
  color: var(--color-text-primary);
  font-size: 30rpx;
  font-weight: 600;
}

.upload-hint {
  margin-top: 8rpx;
  color: var(--color-text-tertiary);
  font-size: 24rpx;
}

.form-input {
  flex: 1;
  min-width: 0;
  color: var(--color-text-primary);
  font-size: 28rpx;
  text-align: right;
}

.input-placeholder {
  color: var(--color-text-quaternary);
}

.description-input {
  width: 100%;
  min-height: 170rpx;
  box-sizing: border-box;
  padding: 20rpx;
  color: var(--color-text-primary);
  font-size: 28rpx;
  line-height: 40rpx;
  border-radius: 18rpx;
  background: var(--color-bg-page-soft);
}

.counter {
  margin-top: 12rpx;
  color: var(--color-text-muted);
  font-size: 22rpx;
  text-align: right;
}

.submit-button {
  height: 92rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  color: var(--color-bg-card);
  font-size: 32rpx;
  font-weight: 700;
  background: var(--gradient-primary);
  box-shadow: var(--shadow-primary-strong);
}

.submit-button:active {
  transform: scale(0.98);
}

.picker-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 999;
  display: flex;
  align-items: flex-end;
  background: var(--color-mask-light);
}

.picker-popup {
  width: 100%;
  padding-bottom: max(30rpx, env(safe-area-inset-bottom));
  overflow: hidden;
  border-radius: 28rpx 28rpx 0 0;
  background: var(--color-bg-card);
}

.picker-handle {
  width: 72rpx;
  height: 8rpx;
  margin: 18rpx auto 10rpx;
  border-radius: 999rpx;
  background: var(--color-border-default);
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18rpx 30rpx 20rpx;
}

.category-search {
  padding: 0 24rpx 16rpx;
}

.category-search-input {
  height: 76rpx;
  box-sizing: border-box;
  padding: 0 26rpx;
  color: var(--color-text-primary);
  font-size: 28rpx;
  border-radius: 999rpx;
  background: var(--color-bg-page);
}

.picker-title {
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 700;
}

.picker-close {
  width: 52rpx;
  height: 52rpx;
  color: var(--color-text-tertiary);
  font-size: 40rpx;
  line-height: 48rpx;
  text-align: center;
}

.picker-list {
  padding: 4rpx 24rpx 18rpx;
}

.picker-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 24rpx;
  margin-bottom: 12rpx;
  color: var(--color-text-primary);
  font-size: 30rpx;
  border-radius: 18rpx;
  background: var(--color-bg-page-soft);
}

.picker-item.active {
  color: var(--color-primary-deep);
  font-weight: 700;
  background: var(--color-primary-soft);
}

.picker-item.create-item {
  color: var(--color-primary-deep);
  background: var(--color-primary-faint);
  border: 1rpx dashed var(--color-primary-light);
}

.picker-check {
  color: var(--color-primary-deep);
  font-size: 34rpx;
}

.picker-add {
  width: 42rpx;
  height: 42rpx;
  color: var(--color-bg-card);
  font-size: 32rpx;
  line-height: 40rpx;
  text-align: center;
  border-radius: 50%;
  background: var(--color-primary-deep);
}

.picker-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120rpx;
  color: var(--color-text-muted);
  font-size: 26rpx;
}

.crop-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: var(--color-mask-medium);
}

.image-viewer-mask {
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32rpx;
  background: var(--color-mask-strong);
}

.image-viewer-popup {
  width: 100%;
  max-width: 700rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: var(--color-bg-card);
}

.image-viewer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.image-viewer-title {
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 700;
}

.image-viewer-close {
  width: 54rpx;
  height: 54rpx;
  color: var(--color-text-tertiary);
  font-size: 42rpx;
  line-height: 50rpx;
  text-align: center;
}

.image-viewer-image {
  width: 100%;
  height: 520rpx;
  border-radius: 18rpx;
  background: var(--color-bg-dark);
}

.image-viewer-actions {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.image-viewer-btn {
  flex: 1;
  height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 700;
}

.image-viewer-btn.secondary {
  color: var(--color-text-secondary);
  background: var(--color-bg-hover);
}

.image-viewer-btn.primary {
  color: var(--color-bg-card);
  background: var(--gradient-primary);
}

.crop-popup {
  width: 100%;
  max-width: 680rpx;
  padding: 28rpx;
  border-radius: 24rpx;
  background: var(--color-bg-card);
}

.crop-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 22rpx;
}

.crop-title {
  color: var(--color-text-primary);
  font-size: 32rpx;
  font-weight: 700;
}

.crop-close {
  width: 54rpx;
  height: 54rpx;
  color: var(--color-text-tertiary);
  font-size: 42rpx;
  line-height: 50rpx;
  text-align: center;
}

.crop-frame {
  position: relative;
  width: 100%;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  border-radius: 18rpx;
  background: var(--color-bg-dark);
  cursor: grab;
  touch-action: none;
  user-select: none;
}

.crop-frame::after {
  content: '';
  position: absolute;
  inset: 16rpx;
  border: 2rpx solid var(--color-bg-overlay-strong);
  border-radius: 12rpx;
  pointer-events: none;
}

.crop-image {
  width: 100%;
  height: 100%;
  transform-origin: center center;
  pointer-events: none;
}

.crop-control {
  margin-top: 22rpx;
}

.crop-label {
  display: block;
  margin-bottom: 8rpx;
  color: var(--color-text-secondary);
  font-size: 26rpx;
}

.crop-slider {
  width: 100%;
}

.crop-actions {
  display: flex;
  gap: 18rpx;
  margin-top: 24rpx;
}

.crop-btn {
  flex: 1;
  height: 82rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 999rpx;
  font-size: 28rpx;
  font-weight: 700;
}

.crop-btn.secondary {
  color: var(--color-text-secondary);
  background: var(--color-bg-hover);
}

.crop-btn.primary {
  color: var(--color-bg-card);
  background: var(--gradient-primary);
}
</style>
