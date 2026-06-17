/**
 * 日期格式化工具
 * 收口 7 个页面重复的 formatDate 逻辑
 */

/**
 * 格式化日期为 MM-DD
 * @param {string|Date|number} input
 * @returns {string} MM-DD
 */
export function formatDate(input) {
  if (!input) return ''
  const d = new Date(input)
  if (isNaN(d.getTime())) return ''
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${m}-${day}`
}

/**
 * 格式化为 YYYY-MM-DD（用于接口入参）
 * @param {Date} date
 * @returns {string}
 */
export function formatYMD(date) {
  const d = date || new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

/**
 * 格式化为 M-D HH:MM（订单/审核/记录列表的时间戳）
 * @param {string|Date|number} input
 * @returns {string}
 */
export function formatShortDateTime(input) {
  if (!input) return ''
  const d = new Date(input)
  if (isNaN(d.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getMonth() + 1}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

/**
 * 友好的相对时间（"刚刚" / "5 分钟前" / "今天 12:30" / "昨天" / "MM-DD"）
 * @param {string|Date|number} input
 * @returns {string}
 */
export function formatRelative(input) {
  if (!input) return ''
  const d = new Date(input)
  if (isNaN(d.getTime())) return ''

  const now = new Date()
  const diff = Math.floor((now - d) / 1000) // 秒

  if (diff < 60) return '刚刚'
  if (diff < 3600) return `${Math.floor(diff / 60)} 分钟前`

  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) {
    const hh = String(d.getHours()).padStart(2, '0')
    const mm = String(d.getMinutes()).padStart(2, '0')
    return `今天 ${hh}:${mm}`
  }

  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  const sameYday = d.toDateString() === yesterday.toDateString()
  if (sameYday) return '昨天'

  return formatDate(input)
}
