/**
 * Toast 工具 — 统一收口 uni.showToast 的 34 处散落调用
 *
 * 使用方式：
 *   import { success, error, warn, info, loading, hideLoading } from '@/utils/toast.js'
 *
 *   success('发布成功')              // icon: success, 1.5s
 *   error('发布失败')                // icon: none, 2s
 *   warn('图片上传中')              // icon: none, 2s
 *   info('请先登录')                // icon: none, 1.5s
 *   loading('保存中…')              // icon: loading（持续显示）
 *   hideLoading()                   // 手动关闭 loading
 */

const DURATION = {
  short: 1500,
  long: 2000,
}

/**
 * 成功提示（绿色对勾）
 */
export function success(msg, duration = DURATION.short) {
  uni.showToast({ title: String(msg), icon: 'success', duration })
}

/**
 * 错误提示（无图标，红色文字由系统决定）
 */
export function error(msg, duration = DURATION.long) {
  uni.showToast({ title: String(msg), icon: 'none', duration })
}

/**
 * 警告提示（无图标，较长停留）
 */
export function warn(msg, duration = DURATION.long) {
  uni.showToast({ title: String(msg), icon: 'none', duration })
}

/**
 * 普通信息提示（无图标）
 */
export function info(msg, duration = DURATION.short) {
  uni.showToast({ title: String(msg), icon: 'none', duration })
}

/**
 * 显示 loading 提示（持续显示，需手动 hideLoading）
 */
export function loading(title = '加载中…') {
  uni.showLoading({ title, mask: false })
}

/**
 * 关闭 loading 提示
 */
export function hideLoading() {
  uni.hideLoading()
}

/**
 * 通用方法 — 直接透传 uni.showToast 选项
 * 用于需要自定义 icon/duration 的边缘场景
 */
export function toast(options) {
  uni.showToast(options)
}
