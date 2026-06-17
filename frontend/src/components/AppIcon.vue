<template>
  <!--
    AppIcon · 统一图标系统
    - 双端兼容：31 个图标用独立 v-if 分支渲染，不用 v-html（小程序 rich-text 对 inline SVG 不友好）
    - lucide 风格：24x24 viewBox，圆头线
    - 显式给每个 <svg> 加 fill/stroke/stroke-width 等属性，确保编译到 mp-weixin 后不丢样式
      （uni-app 编译后 SVG 的 CSS currentColor 在属性层不生效，必须用属性值）
    - size: sm(36) / md(44) / lg(52) / xl(64)
    - tone: primary / info / success / danger / muted / inherit
    - stroke: 线宽（默认 2.4，比 lucide 默认粗一档，肉眼更清晰）
    - bg: true = 加圆角彩色背景（色块+白图标）
  -->
  <view
    class="app-icon"
    :class="['app-icon--' + size, 'app-icon--' + tone, { 'app-icon--bg': bg }]"
    :style="wrapStyle"
  >
    <!-- 31 个独立 SVG 节点：v-if 控制显隐，所有属性显式写出来避免默认值在编译时丢失 -->
    <svg v-if="name==='home'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6h-6v6H4a1 1 0 0 1-1-1z"/></svg>
    <svg v-else-if="name==='users'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.3 2.7-6 6-6s6 2.7 6 6"/><circle cx="17" cy="9" r="2.6"/><path d="M15 20c0-2.5 1.5-4.6 4-5.3"/></svg>
    <svg v-else-if="name==='user'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="12" cy="8" r="4"/><path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/></svg>
    <svg v-else-if="name==='chef'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M6 13c-1.7-1-3-2.6-3-5 0-2.8 2.2-5 5-5 1.5 0 2.8.6 3.7 1.6.5-2.4 2.6-4.1 5.1-4.1 2.9 0 5.2 2.3 5.2 5.2 0 .8-.2 1.6-.5 2.3 1.5.6 2.5 2 2.5 3.6 0 2.2-1.8 4-4 4H6z"/><path d="M8 18h8v3H8z"/></svg>
    <svg v-else-if="name==='dish'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M3 11h18a9 9 0 0 1-18 0z"/><path d="M3 11c0-1.1.9-2 2-2h14a2 2 0 0 1 2 2"/><path d="M12 4v3"/></svg>
    <svg v-else-if="name==='order'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6M9 12h6M9 16h4"/></svg>
    <svg v-else-if="name==='approve'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M9 12.5 11 14.5 15.5 10"/><path d="M21 12a9 9 0 1 1-9-9c1.4 0 2.7.3 3.9.9"/></svg>
    <svg v-else-if="name==='logout'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M15 4h4a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-4"/><path d="M10 17 5 12l5-5"/><path d="M5 12h11"/></svg>
    <svg v-else-if="name==='login'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M9 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h4"/><path d="m14 17 5-5-5-5"/><path d="M19 12H8"/></svg>
    <svg v-else-if="name==='copy'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><rect x="8" y="8" width="13" height="13" rx="2"/><path d="M16 8V5a1 1 0 0 0-1-1H5a1 1 0 0 0-1 1v10a1 1 0 0 0 1 1h3"/></svg>
    <svg v-else-if="name==='share'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="18" cy="5" r="2.6"/><circle cx="6" cy="12" r="2.6"/><circle cx="18" cy="19" r="2.6"/><path d="M8.3 10.7 15.7 6.3M8.3 13.3l7.4 4.4"/></svg>
    <svg v-else-if="name==='edit'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M4 20h4l11-11-4-4L4 16z"/><path d="m14 6 4 4"/></svg>
    <svg v-else-if="name==='flame'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M12 2c1 4 4 5 4 9a4 4 0 0 1-8 0c0-1.5.5-2.6 1.5-3.7C10 9 9.5 10.5 11 11c0-2.5.5-5 1-9z"/><path d="M12 22a5 5 0 0 1-5-5c0-1 .3-2 .9-2.8C8.5 15 9.5 16 10.5 16c0-2.5 1-4 1.5-6 0 2 1.5 3 1.5 5a3 3 0 0 0 2-1.5c.3.7.5 1.5.5 2.5a5 5 0 0 1-5 5z"/></svg>
    <svg v-else-if="name==='star'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="m12 3 2.7 5.5 6 .9-4.4 4.2 1 6L12 16.8 6.7 19.6l1-6L3.3 9.4l6-.9z"/></svg>
    <svg v-else-if="name==='bell'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M6 8a6 6 0 1 1 12 0c0 6 2 7 2 7H4s2-1 2-7z"/><path d="M10 20a2 2 0 0 0 4 0"/></svg>
    <svg v-else-if="name==='calendar'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18M8 3v4M16 3v4"/></svg>
    <svg v-else-if="name==='clock'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/></svg>
    <svg v-else-if="name==='location'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M12 22s7-6.5 7-12a7 7 0 1 0-14 0c0 5.5 7 12 7 12z"/><circle cx="12" cy="10" r="2.6"/></svg>
    <svg v-else-if="name==='heart'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M12 20s-7-4.5-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.5-7 10-7 10z"/></svg>
    <svg v-else-if="name==='search'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="11" cy="11" r="7"/><path d="m20 20-3.5-3.5"/></svg>
    <svg v-else-if="name==='settings'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1A2 2 0 1 1 4.4 17l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.8L4.3 7a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3h.1a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1A2 2 0 1 1 19.7 7l-.1.1a1.7 1.7 0 0 0-.3 1.8v.1a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1z"/></svg>
    <svg v-else-if="name==='arrow_right'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M5 12h14M13 6l6 6-6 6"/></svg>
    <svg v-else-if="name==='chevron_right'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="m9 5 7 7-7 7"/></svg>
    <svg v-else-if="name==='chevron_down'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="m5 9 7 7 7-7"/></svg>
    <svg v-else-if="name==='plus'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M12 5v14M5 12h14"/></svg>
    <svg v-else-if="name==='close'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M6 6l12 12M6 18 18 6"/></svg>
    <svg v-else-if="name==='check'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="m5 12 5 5 9-11"/></svg>
    <svg v-else-if="name==='warn'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M12 3 2 21h20z"/><path d="M12 10v5M12 18v.5"/></svg>
    <svg v-else-if="name==='info'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="12" cy="12" r="9"/><path d="M12 11v5M12 8v.5"/></svg>
    <svg v-else-if="name==='qr'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><path d="M14 14h3v3M21 14v0M14 21h7v-4"/></svg>
    <svg v-else-if="name==='crown'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="m3 7 4 8 4-5 1 5 4-5 4 5 1-8z"/><path d="M4 19h16"/></svg>
    <svg v-else-if="name==='pot'" viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><path d="M4 9h16l-1 9a3 3 0 0 1-3 3H8a3 3 0 0 1-3-3z"/><path d="M2 9h2M20 9h2"/><path d="M9 4h6v2a2 2 0 0 1-2 2h-2a2 2 0 0 1-2-2z"/></svg>
    <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" :stroke-width="stroke" stroke-linecap="round" stroke-linejoin="round" class="svg"><circle cx="12" cy="12" r="9"/><path d="M12 8v4M12 16v.5"/></svg>
  </view>
</template>

<script>
const SIZE_PX = { sm: 36, md: 44, lg: 52, xl: 64 }

export default {
  name: 'AppIcon',
  props: {
    name: { type: String, default: 'info' },
    size: { type: String, default: 'md' },
    tone: { type: String, default: 'inherit' },
    stroke: { type: Number, default: 2.4 },
    bg: { type: Boolean, default: false }
  },
  computed: {
    wrapStyle() {
      const px = SIZE_PX[this.size] || SIZE_PX.md
      return `width: ${px}rpx; height: ${px}rpx;`
    }
  }
}
</script>

<style>
.app-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: none;
  color: currentColor;
  transition: transform 0.2s ease;
}
.app-icon .svg {
  width: 100%;
  height: 100%;
  display: block;
}

/* 色调 */
.app-icon--primary { color: var(--color-primary); }
.app-icon--info    { color: var(--color-info); }
.app-icon--success { color: var(--color-success); }
.app-icon--danger  { color: var(--color-danger); }
.app-icon--muted   { color: var(--color-text-tertiary); }
.app-icon--inherit { color: inherit; }

/* 可选：圆角彩色背景（色块+图标）。图标颜色固定白色以保证对比度。 */
.app-icon--bg {
  border-radius: 20rpx;
  color: #ffffff;
  box-shadow: var(--shadow-color-soft);
}
.app-icon--bg.app-icon--primary { background: var(--gradient-primary-strong); }
.app-icon--bg.app-icon--info    { background: var(--gradient-info); }
.app-icon--bg.app-icon--success { background: var(--gradient-success); }
.app-icon--bg.app-icon--danger  { background: var(--gradient-danger); }
.app-icon--bg.app-icon--muted   { background: var(--color-bg-page-soft); color: var(--color-text-secondary); }
</style>
