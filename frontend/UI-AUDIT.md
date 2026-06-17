# 项目 UI 优化建议（吃什么 / Eat）

> 范围：`frontend/src/pages/**` 全部 9 个页面 + `components/AppShell.vue`、`components/PageLayout.vue`、`utils/categoryDict.js`
> 撰写时间：2026-06-17
> 阅读方式：P0 是先做的，P1 重要，P2 看时间和心情。每一项都给了对应的文件和具体位置。

---

## 一句话诊断

**项目没有 Design System，9 个页面像 9 个设计师各自做的，UI 模式重复发明 5+ 遍。** 主要矛盾是：好看的"风格基因"（橙色渐变、24rpx 圆角、玻璃拟态 hero）已经在 my/login/menu 三个页面试出来了，但没沉淀成可复用组件，其他 6 个页面只能用 1.0 版本的老旧样式（`#fff` 卡 + 8rpx 圆角 + 12rpx padding）硬跟，于是出现强烈的"代际割裂感"。

---

## 🔴 P0（本周必做，可显著提升统一感）

### P0-1 抽出设计令牌（Design Tokens）
**问题**：橙色 `#ff6b35` / `#ff8c5a` / `#ff5f36` / `#ff7246` / `#ff9a6a` / `#ff9364` 这 6 个变体在 9 个页面里到处用，文字灰色 `#5d6673` `#8a95a3` `#98a2ad` `#a1aab3` `#c4cad1` 也是 5 个变体，根本没规律。

**做法**：新建 `frontend/src/styles/tokens.scss` 或在 `App.vue` 里 import：
```scss
// 颜色
$brand-50:  #fff7f2;
$brand-100: #fff1ea;
$brand-200: #ffd2c2;
$brand-300: #ffc8b5;
$brand-400: #ff9a6a;
$brand-500: #ff8a5b;   // 主色
$brand-600: #ff7246;
$brand-700: #ff6b35;   // 标准
$brand-800: #ff5f36;
$brand-900: #c44a25;

$accent-blue: #5c7cfa;
$accent-green: #22c55e;
$accent-red:   #ef4444;
$accent-amber: #f59e0b;

$ink-900: #172033;   // 主标题
$ink-800: #1a1f2e;
$ink-700: #2a3344;   // 强文本
$ink-600: #4b5563;
$ink-500: #5d6673;   // 次要文本
$ink-400: #8a95a3;   // 辅助
$ink-300: #98a2ad;   // 占位
$ink-200: #a1aab3;   // 弱化
$ink-100: #c1c8d0;   // 边框
$ink-50:  #f0f2f5;
$surface: #f5f6f8;   // 页面背景
$surface-2: #f7f8fa;
$surface-card: #ffffff;

// 阴影（统一收敛到 3 档）
$shadow-xs: 0 2rpx 6rpx rgba(20,30,40,0.04);
$shadow-sm: 0 8rpx 22rpx rgba(22,28,36,0.05);
$shadow-md: 0 10rpx 26rpx rgba(20,30,40,0.07);
$shadow-lg: 0 12rpx 30rpx rgba(255,107,53,0.18);
$shadow-fab: 0 12rpx 26rpx rgba(255,107,53,0.24);

// 圆角
$r-xs: 8rpx;
$r-sm: 12rpx;
$r-md: 18rpx;
$r-lg: 24rpx;
$r-pill: 999rpx;

// 间距（4 倍数）
$sp-1: 4rpx;  $sp-2: 8rpx;   $sp-3: 12rpx;  $sp-4: 16rpx;
$sp-5: 20rpx; $sp-6: 24rpx;  $sp-8: 32rpx;  $sp-10: 40rpx;
$sp-12: 48rpx; $sp-16: 64rpx;
```

> **收益**：9 个页面立刻看起来"出自同一只手"。后续要换主色只改 1 个文件。

---

### P0-2 抽出 4 个核心组件
目前 9 个页面里下列模式都被重复实现 3 次以上：

| 模式 | 出现位置 | 应抽为 |
|------|---------|--------|
| `.hero-panel`（橙色渐变 + kicker + title + 圆形徽章） | login、my、today（部分）、publish、approve、edit-request | `<HeroPanel>` |
| `.section-card`（白卡 + 圆角 + 阴影 + 标题） | menu、my、order、publish、edit-request、approve、dish-detail | `<SectionCard>` |
| `.empty-state`（emoji + 主文案 + 副文案） | menu、order、approve、edit-request | `<EmptyState>` |
| `.form-item`（左侧 label 块 + 右侧 value/箭头） | publish、edit-request、dish-detail | `<FormItem>` |
| 状态徽章（待审核/已通过/已拒绝/新增/修改） | approve、edit-request | `<StatusBadge :status="...">` |
| 用户 emoji 头像（基于名字 hash 选 emoji） | my、approve | `<MemberAvatar :name="...">` |
| 主厨 chip / 角色 chip | my、today、publish | `<RoleChip>` |

**做法的具体模板**（这里只列 hero 那个最关键的）：
```vue
<!-- components/HeroPanel.vue -->
<template>
  <view class="hero" :class="['hero--' + tone, { 'hero--compact': compact }]">
    <view class="hero__text">
      <text v-if="kicker" class="hero__kicker">{{ kicker }}</text>
      <text class="hero__title">{{ title }}</text>
      <text v-if="subtitle" class="hero__subtitle">{{ subtitle }}</text>
      <slot name="extra" />
    </view>
    <view v-if="$slots.badge || badgeEmoji" class="hero__badge">
      <slot name="badge">
        <text class="hero__badge-emoji">{{ badgeEmoji }}</text>
      </slot>
    </view>
  </view>
</template>

<style scoped>
.hero {
  display: flex; align-items: center; justify-content: space-between;
  padding: 34rpx 32rpx; margin-bottom: 24rpx;
  border-radius: 24rpx;
  color: #fff;
}
.hero--orange { background: linear-gradient(135deg, #ff7246, #ff9a6a); box-shadow: 0 12rpx 30rpx rgba(255,107,53,0.18); }
.hero--blue   { background: linear-gradient(135deg, #5c7cfa, #7a96ff); }
.hero--dark   { background: linear-gradient(135deg, #1a1f2e, #2a3344); }
.hero--compact { padding: 22rpx 28rpx; }
.hero__kicker { font-size: 24rpx; opacity: 0.88; display: block; margin-bottom: 10rpx; }
.hero__title { font-size: 40rpx; font-weight: 700; line-height: 52rpx; display: block; }
.hero__subtitle { display: block; margin-top: 12rpx; font-size: 24rpx; opacity: 0.82; }
.hero__badge { flex: none; width: 104rpx; height: 104rpx; display: flex; align-items: center; justify-content: center; border-radius: 50%; background: rgba(255,255,255,0.22); }
.hero__badge-emoji { font-size: 54rpx; }
</style>
```

> **收益**：9 个页面的 hero 全部统一，approve/edit-request 立刻有"现代感"。

---

### P0-3 统一 empty-state 组件
**问题**：现在 4 处 empty 长得都不一样——

| 文件 | 现状 |
|------|------|
| `menu.vue` | 大 emoji 96rpx + "你还没发布过菜品" + "去发布→" 按钮 |
| `order.vue` | 中 emoji 80rpx + "家庭还没有订单" + 副文案 |
| `approve.vue` | 中 emoji 96rpx + "没有待审核申请" + "家庭的菜谱暂时稳定啦" |
| `edit-request.vue` | 小 文字"暂无申请记录" |

**统一为**：
```vue
<EmptyState emoji="🎉" title="没有待审核申请" hint="家庭的菜谱暂时稳定啦">
  <template #action>
    <button class="empty-action" @click="...">去看看菜单</button>
  </template>
</EmptyState>
```

---

### P0-4 统一 StatusBadge
现在 4 种状态用了 4 套颜色：
- approve 页面：修改/新增 → `#ff5f36` 橙 + `#36a854` 绿
- edit-request 页面：0/1/2 → `#ff9500` 黄 + `#4caf50` 绿 + `#f44336` 红
- order 页面：状态 pill → 浅灰

```vue
<StatusBadge status="pending" />    <!-- 橙黄 0=待审核 -->
<StatusBadge status="approved" />   <!-- 绿 1=已通过 -->
<StatusBadge status="rejected" />   <!-- 红 2=已拒绝 -->
<StatusBadge tone="orange" text="修改" />
<StatusBadge tone="green"  text="新增" />
```

---

## 🟡 P1（重要但不紧急，建议本月完成）

### P1-1 给每个页面加 skeleton loading
**问题**：所有页面都用空数组占位，导致首屏闪一下 empty 态再变 list。
**做法**：抽 `<SkeletonRow :rows="3" />`，my/menu/order/approve 4 个列表页加。

### P1-2 统一橙色 header
**问题**：现在 header 有 4 种实现：
- `PageLayout.vue`（publish/dish-detail 用）：橙色渐变 + 左返回 + 居中标题
- `today.vue` 自己写：橙色渐变 + 日期选择器
- `approve.vue`/`edit-request.vue` 直接硬编码：橙色渐变 + title/desc
- `order.vue` 用 emoji 圆形装饰

**建议**：`PageLayout` 加 prop（`variant="orange"` 默认 / `"subtle"` 浅色 / `"hero"` 带 kicker），让所有子页面用同一组件。

### P1-3 Vant 依赖瘦身
- `van-search`：login 页用，建议改成原生 `<view class="search-bar">`（参考 menu.vue 的 search 样式）
- `van-tabs` + `van-tab`：menu 用 `van-tabs sticky`，可以改 `<scroll-view scroll-x class="scroll-tabs">`（包大小能少 60~100KB）

### P1-4 暗色模式骨架
颜色全硬编码到 `var(--xxx)`，加一套 `[data-theme="dark"]`：
```scss
:root {
  --brand-700: #ff6b35;
  --ink-900: #172033;
  --surface: #f5f6f8;
  --surface-card: #ffffff;
}
[data-theme="dark"] {
  --brand-700: #ff8a5b;
  --ink-900: #f3f5f7;
  --surface: #0f1219;
  --surface-card: #1a1f2e;
}
```
**注意**：因为页面里都是直接写 `#ff6b35` 不是 `var(--brand-700)`，这个改造工作量大，建议先做 P0-1 token 化，token 化之后再批量替换为 var()，暗色模式自然就出来了。

### P1-5 `<text>` 滥用修正
`my.vue` 等页面有大量 `<text>` 包一个图标或装饰，uni-app 编译后占节点；改为 `<view>` + `line-height` 0。

### P1-6 日期选择器交互升级
`today.vue` 只有左右箭头，可以加：
- 长按箭头 = 跳到今天/明天
- 滑动切换日期（`@touchstart` + `@touchend`）
- 周末加个橙点提醒"周末菜单"

### P1-7 order 页加时间/状态筛选
现在 `order.vue` 一次拉全量订单；加 `<SegmentedControl :options="['全部','待处理','已完成']" />` 顶部筛选 + 按月分组。

### P1-8 publish 重复的 `<style>` 块
`publish.vue` 1178-1480 行是一份**几乎完全一样**的全局 `<style>`，是写漏删的。删掉。

---

## 🟢 P2（看心情）

### P2-1 动画
- 页面跳转用 `uni.navigateTo({ animationType: 'pop-in' })`
- 长列表用 `<view class="stagger-item" :style="animationDelay(i)">` 做渐入
- Hero badge 加 8s 慢转的 conic-gradient（my 页已有可复用）

### P2-2 微交互
- 所有按钮加 `:active` 缩放 0.98
- 菜品卡片长按弹出 quick action（编辑/删除/设为今日主菜）
- TabBar 切换时图标 bounce 动画

### P2-3 无障碍
- 所有 icon-only 按钮加 `aria-label`（目前 logout ⏻ 按钮没标签）
- `<view @click>` 改用 `<button>` 标签或加 `role="button"`
- 加 `prefers-reduced-motion` 媒体查询，禁用动画

### P2-4 错误状态
- `approve.vue` 加载失败时只 `console.error`，没 UI 提示
- 建议统一一个 `loadWithRetry(fn, { retries: 2, fallback: '家庭服务暂不可用' })`

### P2-5 toast 抽象
`uni.showToast` 出现 20+ 次，抽个：
```js
toast.success('发布成功')
toast.error(e.message || '发布失败')
toast.warn('图片上传中')
```

### P2-6 把 my 页的"今日主厨"dot 抽到 RoleChip
现在 3 处自己实现：
- `my.vue` 黄色 dot
- `today.vue` 圆形主厨头像
- `publish.vue` hero-chip 里的 `👨‍🍳` 文字

### P2-7 字体系统
现在字体大小 24-42rpx 全靠经验值；建议定一个 scale：
```scss
$fs-xs: 22rpx;   // 辅助说明
$fs-sm: 24rpx;   // 次要文字
$fs-base: 28rpx; // 正文
$fs-md: 30rpx;   // 卡片标题
$fs-lg: 32rpx;   // 按钮
$fs-xl: 36rpx;   // 区块标题
$fs-2xl: 40rpx;  // Hero
$fs-3xl: 48rpx;  // 大数字
```

---

## 文件级快速建议（按修改成本从低到高）

| 难度 | 建议 |
|------|------|
| 🟢 5min | 删 `publish.vue` 1166-1480 行重复的全局 `<style>` 块 |
| 🟢 15min | 抽 `<HeroPanel>` 组件 + 让 6 个页面替换 hero-panel |
| 🟢 30min | 抽 `<EmptyState>` 组件替换 4 处 |
| 🟢 30min | 抽 `<StatusBadge>` 组件统一状态色 |
| 🟡 1h | 抽 `tokens.scss`，用 find-replace 把硬编码颜色映射到 token |
| 🟡 1h | 抽 `<SectionCard>` + `<FormItem>` 让 publish/edit-request 用上 |
| 🟡 1h | 把 `approve.vue` `edit-request.vue` 的 hero 改为 PageLayout 的新 variant |
| 🟡 2h | 删 Vant 依赖（van-search + van-tabs），自己实现 |
| 🔴 4h | 抽 `<MemberAvatar>` `<RoleChip>` 让 my/today/approve 统一 |
| 🔴 4h | 暗色模式（前置：先做 P0-1 token 化） |

---

## 我的推荐执行顺序

1. **今天**：删 publish.vue 重复 style + 抽 `<HeroPanel>` + `<EmptyState>`（2 小时，立刻能让 approve/edit-request 看起来"现代"）
2. **明天**：抽 `tokens.scss`（P0-1）+ 用 token 替换 my/menu/login 3 个核心页（4 小时）
3. **本周**：抽 `<SectionCard> <FormItem> <StatusBadge> <MemberAvatar>`（1 天）
4. **下周**：去掉 Vant + 暗色模式骨架（2~3 天）
5. **下下周**：动画/微交互/无障碍

---

## 三个最大的"原本可以避免"的失误

1. **没有 `tokens.scss`**——9 个页面硬编码颜色值，后续换主色要改 100+ 处
2. **没有组件库意识**——my 页面做出来的 hero 在 publish/edit-request 没被复用
3. **`publish.vue` 写完忘了删**——1166-1480 行是 scoped 之前调试残留的全局 style，会污染全局类名（虽然现在没冲突但风险大）
