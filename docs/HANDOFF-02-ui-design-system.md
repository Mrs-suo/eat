# 02 · UI 设计规范

> **本文是给"接手 UI 工作"的 AI 看的真值表**。所有 token 数值来自 `frontend/src/styles/tokens.css` 的实际内容（不是猜测），所有组件 API 来自 `frontend/src/components/*.vue` 的实际 props。
>
> **核心原则**：
> 1. 颜色 / 圆角 / 阴影 / 间距 / 字号 **必须用 `var(--xxx)`**，禁止硬编码
> 2. 复杂组件用 `components: { xxx }` 显式注册
> 3. 图标用 `<AppIcon name="xxx" />`，不要外联 SVG

---

## 1 · 设计令牌（Design Tokens）

**文件**：`frontend/src/styles/tokens.css`（共 134 行）  
**入口**：`App.vue` 第 16 行 `@import './styles/tokens.css';`

> ⚠️ 当前**只有 `:root { ... }`**，**无** `[data-theme="dark"]` / `@media (prefers-color-scheme: dark)`。**项目目前不支持暗色模式**。

### 1.1 品牌主色（橘色 5 档）

```css
--color-primary:        #ff6b35   /* 主品牌色 */
--color-primary-deep:   #ff5f36   /* 深一档 */
--color-primary-light:  #ff8a5b   /* 浅一档 */
--color-primary-lighter:#ffb591   /* 更浅 */
--color-primary-soft:   #fff1ea   /* 浅底背景 */
--color-primary-bg:     #fff7f1   /* 区域底 */
--color-primary-faint:  #fff0e6   /* 极浅描边/hover */
```

### 1.2 渐变（19 个）

```css
/* 主色 5 种浓度 */
--gradient-primary:               linear-gradient(135deg, #ff6b35 0%, #ff8a5b 100%);
--gradient-primary-soft:          linear-gradient(135deg, #ff8a5b 0%, #ff6b35 100%);
--gradient-primary-strong:        linear-gradient(135deg, #ff5f36 0%, #ff6b35 100%);
--gradient-primary-fade:          linear-gradient(135deg, #fff1ea 0%, #ffd9c4 100%);
--gradient-primary-fade-soft:     linear-gradient(135deg, #fff7f1 0%, #ffe4d1 100%);
--gradient-primary-fade-vertical-soft: linear-gradient(180deg, #fff1ea 0%, #fff1ea00 100%);

/* 彩虹环（MemberAvatar ring） */
--gradient-primary-rainbow: conic-gradient(#ff6b35, #ff5f36, #ff8a5b, #ffb591, #ff6b35);

/* 语义色 */
--gradient-info:    linear-gradient(135deg, #3b82f6 0%, #5b9bff 100%);
--gradient-success: linear-gradient(135deg, #36a854 0%, #5cc97f 100%);
--gradient-danger:  linear-gradient(135deg, #f44336 0%, #ff6b5a 100%);
--gradient-danger-soft: linear-gradient(135deg, #ffebee 0%, #ffd2d4 100%);

/* 装饰光斑（my.vue guest hero） */
--gradient-blob-1:        linear-gradient(135deg, #ffd9c4 0%, #ffb591 100%);
--gradient-blob-2:        linear-gradient(135deg, #fff1ea 0%, #ffd9c4 100%);
--gradient-blob-soft-1:   radial-gradient(circle at 30% 30%, #ff8a5b66 0%, #ff8a5b00 70%);
--gradient-blob-soft-2:   radial-gradient(circle at 70% 70%, #ff6b354d 0%, #ff6b3500 70%);
--gradient-bg-soft:       linear-gradient(135deg, #fff7f1 0%, #fff1ea 100%);
--gradient-divider-fade:  linear-gradient(180deg, #eef2f5 0%, #eef2f500 100%);
--gradient-avatar:        linear-gradient(135deg, #d4a373 0%, #b08968 100%);
--gradient-page:          linear-gradient(180deg, #fff7f1 0%, #ffffff 50%);
```

### 1.3 文字色（5 档）

```css
--color-text-primary:     #2a3344   /* 标题/正文主色 */
--color-text-secondary:   #5d6673   /* 次要文字 */
--color-text-muted:       #5d6878   /* 弱化文字 */
--color-text-tertiary:    #8a95a3   /* 描述/标签 */
--color-text-quaternary:  #b0b8c2   /* 占位/最弱 */
--color-text-placeholder: #c2c8d0   /* 输入占位 */
```

> **v2 注**：v1 文档里写的是 `#172033` 系列，实际文件是 `#2a3344` 系列。**以本文档为准**（这是真实扫描结果）。

### 1.4 背景 / 边框 / 蒙层

```css
/* 背景 */
--color-bg-page:        #f8f8f8   /* 全局页面底 */
--color-bg-page-soft:   #f5f6f8   /* 卡片底 */
--color-bg-card:        #ffffff
--color-bg-hover:       #f3f5f8
--color-bg-dark:        #1e2633   /* 仅作深色 chip 背景 */

/* 蒙层（frosted glass / modal） */
--color-bg-overlay-light:   rgba(255, 255, 255, 0.4);
--color-bg-overlay-soft:    rgba(255, 255, 255, 0.6);
--color-bg-overlay-medium:  rgba(255, 255, 255, 0.75);
--color-bg-overlay-strong:  rgba( 255, 255, 255, 0.92);

/* 黑色蒙层 */
--color-mask-light:    rgba(0, 0, 0, 0.2);
--color-mask-medium:   rgba(0, 0, 0, 0.4);
--color-mask-strong:   rgba(0, 0, 0, 0.6);

/* 边框 */
--color-border-light:    #f0f2f5
--color-border-default:  #eef2f5
--color-border-strong:   rgba(18, 26, 38, 0.04)
```

### 1.5 语义色

```css
--color-success:      #36a854
--color-success-soft: #e7f8ec
--color-warning:      #ff9500
--color-warning-soft: #fff3e0
--color-danger:       #f44336
--color-danger-soft:  #ffebee
--color-info:         #3b82f6
--color-info-soft:    #e3f2fd
```

### 1.6 圆角（5 档）

```css
--radius-sm:   12rpx
--radius-md:   16rpx
--radius-lg:   20rpx
--radius-xl:   22rpx
--radius-pill: 999rpx
```

### 1.7 阴影（12 档）

```css
/* 中性 */
--shadow-sm:   0 4rpx 14rpx rgba(31, 42, 55, 0.06);
--shadow-md:   0 10rpx 26rpx rgba(20, 30, 40, 0.07);
--shadow-lg:   0 12rpx 28rpx rgba(31, 42, 55, 0.1);

/* 卡片专用 */
--shadow-card:         0 8rpx 22rpx rgba(20, 30, 40, 0.06);
--shadow-card-soft:    0 4rpx 14rpx rgba(20, 30, 40, 0.05);
--shadow-card-strong:  0 12rpx 30rpx rgba(22, 30, 42, 0.08);

/* 主色专用（橘色发光） */
--shadow-primary:        0 12rpx 28rpx rgba(255, 107, 53, 0.4);
--shadow-primary-soft:   0 8rpx 18rpx rgba(255, 107, 53, 0.22);
--shadow-primary-strong: 0 14rpx 32rpx rgba(255, 107, 53, 0.32);

/* 杂项 */
--shadow-color-soft:   0 4rpx 12rpx rgba(20, 30, 40, 0.08);
--shadow-info-soft:    0 6rpx 16rpx rgba(59, 130, 246, 0.2);
--shadow-success-soft: 0 6rpx 16rpx rgba(54, 168, 84, 0.2);
```

### 1.8 间距（4 步阶）

```css
--space-1:  4rpx
--space-2:  8rpx
--space-3:  12rpx
--space-4:  16rpx
--space-5:  20rpx
--space-6:  24rpx
--space-8:  32rpx
--space-10: 40rpx
--space-12: 48rpx
--space-16: 64rpx
```

> ⚠️ **`--space-*` 实际很少被消费**，组件多用硬编码 rpx 值。后续工作建议逐步迁移。

### 1.9 字号（9 档）

```css
--font-xs:   20rpx
--font-sm:   22rpx
--font-base: 24rpx
--font-md:   26rpx
--font-lg:   28rpx
--font-xl:   32rpx
--font-2xl:  36rpx
--font-3xl:  40rpx
--font-4xl:  48rpx
```

**字重（实际观察值，非 token）**：

| 重量 | 典型用途 |
|---|---|
| 300 | Fab icon、CTA 箭头 |
| 400 | Tab 文字、placeholder |
| 500 | 副标题、计数 |
| 600 | 导航、表单 label、成员名 |
| 700 | 区段标题、卡片标题、chip、按钮 |
| 800 | Hero 标题、菜品名、页标题、CTA、统计数字、profile 名 |
| 900 | 登录页标题（52rpx / 900） |

---

## 2 · 19 个组件 API 速查

> 完整 prop 表见 `frontend/src/components/*.vue` 文件本身。

### 2.1 布局容器

#### `AppShell` — 页面外壳

| Prop | 类型 | 默认 | 说明 |
|---|---|---|---|
| `showTabBar` | Boolean | `true` | 显示自定义 3-tab |
| `currentTab` | Number | `0` | 高亮 tab（0=menu, 1=today, 2=my） |
| `contentStyle` | String | `''` | 内联 padding 等 |

**插槽**：`#menu`（顶部区）、`#default`（内容）、`#fab`（悬浮按钮）

**关键尺寸**：
- 外层 `height: 100vh; display: flex; flex-direction: column;`
- tab bar `min-height: 112rpx; padding: 18rpx 0 max(18rpx, env(safe-area-inset-bottom))`
- 中间 tab 圆形凸起 `92×92rpx`，背景 `linear-gradient(180deg, #fff7ee, #ffe4d4)`

#### `PageLayout` — 子页 layout

| Prop | 类型 | 默认 | 说明 |
|---|---|---|---|
| `title` | String | `''` | 标题 |
| `showBack` | Boolean | `true` | 显示返回 |

**插槽**：`#header-right`、`#default`

**特点**：渐变 header（橘色），返回按钮用 `/static/back.png` 资源

### 2.2 内容展示

#### `HeroPanel` — 大色块 hero

| Prop | 类型 | 默认 | 说明 |
|---|---|---|---|
| `title` | String | — | 主标题 |
| `subtitle` | String | — | 副标题 |
| `kicker` | String | — | 顶部小标签 |
| `badgeEmoji` | String | — | 右上角大 emoji |
| `tone` | String | `'orange'` | `orange` / `blue` / `dark` |
| `fullwidth` | Boolean | `false` | 取消圆角阴影，贴顶 |

#### `EmptyState` — 空状态

| Prop | 类型 | 默认 | 说明 |
|---|---|---|---|
| `emoji` | String | `'📭'` | 大图标 |
| `title` | String | `'暂无数据'` | 标题 |
| `hint` | String | `''` | 提示 |

**设计**：padding `120rpx 32rpx`；emoji 96rpx；title 30rpx/700；hint 24rpx。

#### `SkeletonBox` — 加载骨架

| Prop | 类型 | 默认 |
|---|---|---|
| `width` | String | `'100%'` |
| `height` | String | `'32rpx'` |
| `radius` | String | `'8rpx'` |
| `block` | Boolean | `false` |
| `rounded` | Boolean | `false` |

**动画**：`@keyframes skeleton-shimmer 1.4s ease-in-out infinite`

#### `DateBadge` — 日期徽章

| Prop | 类型 | 默认 | 可选值 |
|---|---|---|---|
| `date` | Date/String | — | |
| `format` | String | `'M-D'` | `M-D` / `YYYY-MM-DD` / `datetime` / `weekday` |
| `tone` | String | `'default'` | `default` / `orange` / `gray` |
| `size` | String | `'md'` | `md` / `lg` |

### 2.3 卡片

#### `ListCard` — 列表卡

| Prop | 类型 | 默认 | 可选值 |
|---|---|---|---|
| `layout` | String | `'row-image'` | `row-image` / `column` / `compact` |
| `clickable` | Boolean | `true` | |

**插槽**：`#header`（顶部渐变区）、`#default`（内容）、`#footer`（操作栏）

#### `InfoCard` — 信息卡

| Prop | 类型 | 默认 |
|---|---|---|
| `title` | String | `''` |

**插槽**：`#action`（标题右侧操作）、`#default`（内容）

#### `SectionCard` — 表单/区段卡

| Prop | 类型 | 默认 |
|---|---|---|
| `title` | String | `''` |
| `subtitle` | String | `''` |
| `note` | String | `''` | 必填/选填标记 |
| `required` | Boolean | `false` |
| `tight` | Boolean | `false` |

### 2.4 表单

#### `FormItem` — 表单行

| Prop | 类型 | 默认 |
|---|---|---|
| `label` | String | `''` |
| `desc` | String | `''` |
| `value` | String | `''` |
| `placeholder` | String | `''` |
| `arrow` | Boolean | `true` |
| `clickable` | Boolean | `false` |

**事件**：`@click`

### 2.5 导航 / 反馈

#### `TopBar` — 自定义顶栏

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `title` | String | `''` | |
| `showBack` | Boolean | `true` | |
| `variant` | String | `'transparent'` | `transparent` / `solid` / `gradient` |
| `fixed` | Boolean | `true` | |
| `backIcon` | String | `'<'` | |
| `fallbackUrl` | String | `'/pages/menu/menu'` | 栈底 fallback |

**变体背景**：
- `transparent`：透明（用于图片上）
- `solid`：`#ffffff` + 底边
- `gradient`：`--gradient-primary`

**回退逻辑**：栈 > 1 → `navigateBack`；fallbackUrl 是 tab → `switchTab`；否则 `reLaunch`

#### `Divider` — 分割线

| Prop | 类型 | 默认 |
|---|---|---|
| `inset` | String/Number | `0` |
| `vertical` | Boolean | `false` |
| `color` | String | `'--color-border-light'` |
| `thickness` | String/Number | `1` |

#### `Tag` — 标签

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `text` | String | — | |
| `tone` | String | `'gray'` | `orange` / `blue` / `green` / `red` / `gray` |
| `variant` | String | `'soft'` | `soft` / `solid` |
| `size` | String | `'md'` | `sm` / `md` |
| `icon` | String | `''` | AppIcon name |

#### `StatusBadge` — 状态徽章

> ⚠️ **目前没有被任何页面直接引用**（可能为备选组件）。

| Prop | 类型 | 默认 |
|---|---|---|
| `text` | String | — |
| `tone` | String | `'gray'` |

#### `Fab` — 悬浮按钮

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `icon` | String | — | AppIcon name |
| `text` | String | `''` | 文字（extended 模式） |
| `variant` | String | `'solid'` | `solid` / `ghost` |
| `extended` | Boolean | `false` | 含文字胶囊形态 |
| `position` | String | `'bottom-right'` | `bottom-right` / `bottom-center` |
| `bottom` | String | `'180rpx'` | 距底距离 |

### 2.6 身份 / 角色

#### `MemberAvatar` — 成员头像

| Prop | 类型 | 默认 | 说明 |
|---|---|---|---|
| `text` | String | `''` | 取首字符 |
| `emoji` | String | `''` | emoji 替代文字 |
| `size` | String | `'md'` | `sm` / `md` / `lg` / `xl` |
| `ring` | Boolean | `false` | 启用彩虹环动画 |
| `badge` | String | `''` | 角标文字（`'厨'`） |
| `badgeTone` | String | `'primary'` | `primary` / `success` / `warning` |
| `color` | String | `''` | 覆盖色（默认按 text 哈希选 10 色调色板） |

**尺寸（rpx）**：

| Size | 宽×高 | 字号 | emoji |
|---|---|---|---|
| `sm` | 56 | 26 | 32 |
| `md` | 80 | 32 | 44 |
| `lg` | 120 | 44 | 64 |
| `xl` | 168 | 60 | 96 |

**角标行为**：
- `sm`（56rpx）：**仅 16×16rpx 小色点**，文字隐藏（`display: none`） — **避免覆盖头像**
- `md+`：36rpx 圆 + 4rpx 白边 + 文字 20rpx/700

**色板（JS 哈希）**：
```js
['#ff6b35', '#5c7cfa', '#22c55e', '#f59e0b', '#a855f7',
 '#ec4899', '#14b8a6', '#f97316', '#3b82f6', '#84cc16']
```

#### `RoleChip` — 角色 chip

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `text` | String | **必填** | |
| `tone` | String | `'cook'` | `cook` / `eat` / `admin` / `owner` / `neutral` |
| `dot` | Boolean | `false` | 发光点 |
| `solid` | Boolean | `false` | 实心渐变 |
| `emoji` | String | `''` | 前缀 |

**5 tone 配色**：

| Tone | 默认颜色 | 软背景 | 实心渐变 |
|---|---|---|---|
| `cook` | `#ff6b35` | `rgba(255,107,53,0.12)` | `--gradient-primary` |
| `eat` | `#5c7cfa` | `rgba(92,124,250,0.12)` | `--gradient-info` |
| `admin` | `#a855f7` | `rgba(168,85,247,0.12)` | 紫渐变 |
| `owner` | `#f59e0b` | `rgba(245,158,11,0.14)` | 黄渐变 |
| `neutral` | `#5d6673` | `#f2f4f7` | `#94a0b0` |

#### `SegmentTabs` — 分段 Tab

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `modelValue` | String/Number | — | v-model |
| `tabs` | Array | `[]` | `[{ value, label, count? }]` |
| `variant` | String | `'pill'` | `pill` / `icon-large` / `underline` |

**事件**：`@update:modelValue`、`@change`

### 2.7 图标

#### `AppIcon` — 31 个内联 SVG 图标

| Prop | 类型 | 默认 | 可选 |
|---|---|---|---|
| `name` | String | `'info'` | 见图标表 |
| `size` | String | `'md'` | `sm`(36) / `md`(44) / `lg`(52) / `xl`(64) |
| `tone` | String | `'inherit'` | `primary` / `info` / `success` / `danger` / `muted` / `inherit` |
| `stroke` | Number | `2.4` | 描边宽（比 lucide 默认 2.0 粗，加粗为屏幕可读） |
| `bg` | Boolean | `false` | 圆角色块背景（20rpx 圆角 + 白图标 + 阴影） |

**31 个图标名**：

| 分类 | 名称 |
|---|---|
| 导航 | `home`, `arrow_right`, `chevron_right`, `chevron_down`, `plus`, `close` |
| 用户 | `users`, `user`, `chef`, `crown` |
| 业务 | `dish`, `order`, `approve`, `copy`, `edit`, `share`, `pot` |
| 状态 | `star`, `flame`, `bell`, `check`, `warn`, `info` |
| 工具 | `calendar`, `clock`, `location`, `heart`, `search`, `settings`, `qr`, `login`, `logout` |

> **实现细节**：SVG 属性**显式内联**（不用 `currentColor` CSS），原因是 uni-app 编译到 mp-weixin 时会剥掉 SVG 的 CSS 样式。

---

## 3 · 关键 UI 模式

### 3.1 页面骨架（3 个 Tab 页通用）

```vue
<template>
  <AppShell :show-tab-bar="true" :current-tab="0" content-style="padding: 24rpx 24rpx 32rpx;">
    <template #menu>
      <!-- 顶部 header 区（hero / search / tabs） -->
    </template>

    <!-- 主内容（可滚动） -->
    <EmptyState v-if="!dishes.length" />
    <ListCard v-for="d in dishes" :key="d.id" :layout="'row-image'" @click="goDetail(d.id)">
      ...
    </ListCard>

    <template #fab>
      <Fab icon="plus" @click="goPublish" />
    </template>
  </AppShell>
</template>
```

### 3.2 安全区处理

```css
/* tab bar 底部 */
padding: 18rpx 0 max(18rpx, env(safe-area-inset-bottom));

/* publish 提交按钮 */
padding-bottom: max(30rpx, env(safe-area-inset-bottom));
```

### 3.3 渐变 hero 装饰

```css
background: var(--gradient-primary-fade-vertical-soft);  /* 顶部淡入 */
position: relative;
overflow: hidden;
```

**光斑动画**：
```css
@keyframes floatBlob {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%      { transform: translate(10rpx, -15rpx) scale(1.08); }
}
animation: floatBlob 8s ease-in-out infinite;
```

### 3.4 标准缓动

| 场景 | 缓动 | 时长 |
|---|---|---|
| 通用 hover / press | `ease` | `0.15s` / `0.18s` / `0.2s` |
| 弹簧感（CTA / Fab） | `cubic-bezier(0.16, 1, 0.3, 1)` | `0.2s` / `0.3s` |
| avatar 环 | `linear infinite` | `6s` |
| skeleton 闪光 | `ease-in-out infinite` | `1.4s` |
| pulse 徽章 | `ease-in-out infinite` | `2s` |

### 3.5 按下反馈

```css
:active {
  transform: scale(0.92);
  opacity: 0.78;
}
```

---

## 4 · 配色用法地图

> 想知道"某个颜色该用哪个 token"，查这里。

| 用途 | Token |
|---|---|
| 主按钮 / 主 icon | `--color-primary` / `--gradient-primary` |
| 警告 / 强提示 | `--color-primary-deep` + `--color-primary-soft` 背景 |
| 弱化主色（次要 CTA） | `--color-primary-light` |
| 区段浅底 | `--color-primary-bg` / `--color-primary-soft` |
| 标题文字 | `--color-text-primary` |
| 描述 / 副标题 | `--color-text-secondary` / `--color-text-muted` |
| 占位 / 弱化 | `--color-text-tertiary` / `--color-text-quaternary` |
| 成功态 | `--color-success` + `--color-success-soft` |
| 错误态 | `--color-danger` + `--color-danger-soft` |
| 警告态 | `--color-warning` + `--color-warning-soft` |
| 信息态 | `--color-info` + `--color-info-soft` |
| 页面底 | `--color-bg-page` |
| 卡片底 | `--color-bg-card` |
| hover 底 | `--color-bg-hover` |
| 卡片阴影 | `--shadow-card`（软）/ `--shadow-card-strong`（强） |
| 主 CTA 阴影 | `--shadow-primary`（强）/ `--shadow-primary-soft`（弱） |
| 主 CTA 渐变 | `--gradient-primary-strong`（实心按钮） / `--gradient-primary`（图标块） |

---

## 5 · 当前限制 & 待改进

| 项 | 现状 | 建议 |
|---|---|---|
| 暗色模式 | 无 token | 加 `[data-theme="dark"]` 段 |
| `--space-*` | 多处硬编码 | 迁移到 token |
| `--font-*` | 多处硬编码 | 迁移到 token |
| `StatusBadge` | 无引用 | 删或文档化 |
| A11y | **完全无**（无 `aria-` / `role` / `alt` / `tabindex`） | 加基础 aria 标签 |
| 国际化 | 全中文硬编码 | i18n 抽离（暂无需求） |
| `aria-` 屏幕阅读器 | 0 处 | 至少 image 加 `alt`，按钮加 `aria-label` |

---

**END OF HANDOFF-02**
