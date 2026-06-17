# Eat 项目 · 页面与需求总览

> 一份"打开就能跑通全站"的文档 — 9 个页面的功能、状态、组件、API、视觉规范、交互细节、关键边界场景。

---

## 0 · 项目速览

| 项目 | Eat（吃什么）|
|---|---|
| 业务 | 家庭共享点餐小程序 — 每天 1 个主厨 / 多人点菜 / 修改申请审批 |
| 双端 | uni-app Vue 3 — H5 (web) + mp-weixin (微信小程序) |
| 技术栈 | 前端：uni-app 3 alpha / Vue 3.4 / Vite 5 / @vant 4<br>后端：Spring Boot 3 / JPA / MySQL 8.4 / Lombok<br>构建：Maven 3.9 + npm |
| 项目位置 | `C:\Users\suozhijie\Desktop\eat\` |
| 启动 | 后端 `mvn spring-boot:run "--server.port=8080"`（已在 Windows 服务中注册为 MySQL84）<br>前端 `npm run dev:h5`（H5 端口 30000）<br>前端 `npm run build:mp-weixin`（出 `dist/dev/mp-weixin`）|

### 代码规模

- 前端页面：9 个（4823 行）
- 前端组件：18 个（位于 `src/components/`）
- 前端工具：4 个（`api.js` 32 端点 / `date.js` / `toast.js` / `categoryDict.js`）
- 设计系统：`styles/tokens.css` 包含 60+ 设计 token
- 后端 Entity：10 个表 / Controller：7 个 / 端点：30+

---

## 1 · 架构与数据流

### 路由结构（`pages.json`）

```
Tab 1: pages/menu/menu        → 菜单（家庭 / 我的私房菜）
Tab 2: pages/today/today      → 今日（按时段查当日菜品）
Tab 3: pages/my/my            → 我的（家庭信息、统计、菜单、登出）

子页面（navigateTo 跳转）:
  pages/login/login           → 登录 / 注册（双模式）
  pages/publish/publish       → 发布菜品（含图片裁剪）
  pages/order/order           → 我的订单 / 家庭订单
  pages/edit-request/edit-request → 申请修改菜品（点菜人用）
  pages/approve/approve       → 审核申请（主厨用）
  pages/dish-detail/dish-detail → 菜品详情
```

### 全局 UI 容器

所有 Tab 页面统一使用 `AppShell` 组件：
- 顶部 menu slot（自定义 header 区）
- 中部内容区（可滚动）
- 底部 tab-bar（3 个 Tab）
- `#fab` slot（绝对定位的悬浮按钮，绕过滚动容器）

### 数据持久化

| 存储 | 用途 |
|---|---|
| `uni.storage` | `currentUser`（userId + phone + familyId + familyName）<br>`familyId` / `familyCode` / `familyName` / `userId` / `phone` / `nickname` |
| MySQL | 全部业务数据（10 张表） |
| 文件服务 | `localhost:8080/files/{uuid}.jpg` — 上传后图片 URL 存入 `dish.image` |

### 登录态判定

所有页面入口第一件事是 `uni.getStorageSync('currentUser')` 拿缓存用户，userId/phone 命中 → 已登录；未命中 → my.vue 自动切到「未登录 Hero」状态（不是真的拦截式跳登录）。

---

## 2 · 9 个页面详解

---

### 2.1 `pages/menu/menu` — 菜单（家庭 / 我的）

**核心功能**：浏览家庭共享菜单 + 个人私房菜，支持分类 + 关键词搜索 + 滑动切换 Tab。

**视觉层级**：
1. **顶部模式切换**：`SegmentTabs`（家庭菜单 / 我的私房菜，2 个 Tab）
2. **主厨横幅**：今日主厨头像 + 姓名 + 本周做饭次数 + 切换/在岗按钮
3. **家庭信息条**：家庭名 + 邀请码 + 成员数（点击复制码）
4. **分类横滑栏**：横向滚动的分类 Chip（全部 / 全部菜品分类）
5. **菜品列表**：`ListCard` 卡片网格
6. **空状态**：`EmptyState`（无菜品时显示）
7. **右下角 FAB**：发布菜品（仅已登录显示）

**核心数据**：
- `dishes[]`：当前家庭的全部菜品 / 当前用户的全部菜品（按 activeMode 过滤）
- `categories[]`：菜品分类（含 '全部'）
- `activeMode`：`'family' | 'mine'`
- `activeCategory`：当前分类下标
- `keyword`：搜索关键词
- `weekStats[]`：每位成员本周做饭次数

**API 调用**：
- `getFamilyDishes(familyId)` — 拉家庭菜单
- `getDishesByCook(userId)` — 拉我的私房菜
- `getCategories()` — 拉分类
- `getFamilyMembers(familyId)` — 拉成员（用于主厨/统计）
- `getCurrentFamily(userId)` — 拉家庭（兜底刷新 familyCode）

**computed 关键**：
- `filteredDishes`：四重过滤（mode / category / keyword / family 范围）

**交互细节**：
- 输入框支持 `confirm-type="search"` 软键盘搜索
- 长按菜品（h5 上）可调用删除 API
- 切换主厨 → 重新拉菜品 + 拉统计
- 复制码：调用 `uni.setClipboardData` + toast 提示

**关键样式 token**：
- Tab 主题色 `var(--color-primary)`
- 分类 chip：未选中浅底，选中主色
- 菜品卡片：白底 + 主色阴影 + 菜品图（无图显示占位图）

---

### 2.2 `pages/today/today` — 今日

**核心功能**：按"早/午/晚"3 个时段 + 任意日期，查询当天所有菜品。

**视觉层级**：
1. **顶部大日期卡片**：当前选中的日期 + 时段（早餐/午餐/晚餐）
2. **时段切换**：`SegmentTabs`（3 个 Tab：早餐/午餐/晚餐）
3. **日期选择器**：`van-picker`（年/月/日，可向前选历史日期）
4. **今日主厨条**：显示今日主厨头像 + 姓名
5. **菜品列表**：按时段过滤的菜品
6. **空状态**：无菜品时显示
7. **底部固定 CTA**：`立即点这道 → 申请修改菜品`（跳 edit-request）

**核心数据**：
- `currentMeal`：`'breakfast' | 'lunch' | 'dinner'`（根据当前时间自动判定：5-10 早 / 10-15 午 / 其他晚）
- `selectedDate`：`Date` 对象
- `meals`：3 个时段配置（含 icon 表情）
- `pickerValue`：`[年索引, 月-1, 日-1]`
- `mealDishes[]`：当前日期 + 时段的菜品
- `todayCook`：今日主厨对象
- `members[]`：家庭成员（用于显示主厨头像）

**API 调用**：
- `getDishesByDate(familyId, dateStr)` — 按日期拉菜品
- `getTodayCook(familyId, dateStr)` — 拉当日主厨
- `getFamilyMembers(familyId)` — 拉成员（缓存主厨显示）

**关键交互**：
- 默认时段 = 根据当前小时智能判定
- 切换日期 → 重新拉数据
- 切换时段 → 重新拉数据
- 点菜品 → 跳 dish-detail
- "申请修改菜品" → 跳 edit-request（带 `?mealTime=xxx`）

**视觉特色**：
- 顶部 3 个时段 Tab 配不同 emoji（🌅 ☀️ 🌙）
- 日期选择器：滚轮式
- 列表支持"空状态" + "有菜品"两种视图

---

### 2.3 `pages/my/my` — 我的

**核心功能**：家庭信息中心 + 个人统计 + 快捷菜单 + 登出。

**视觉层级**（已登录态）：
1. **顶部 Header**（`#menu` slot）：
   - 渐变背景 + 3 个浮动光斑
   - Profile 卡片：168rpx 大头像 + 昵称 + RoleChip（今日主厨 / 今日食客） + 手机号
2. **家庭信息卡**（`.family-card`）：渐变底 + 家庭图标 + 家庭名 + 邀请码 + 复制码按钮 + 成员芯片（带"厨"角标）
3. **本周数据统计**（`.stats-card`）：3 列（做饭次数 / 菜品数 / 订单数） + 2 段分隔
4. **今日主厨快捷卡**（`.today-cook-card`）：左 dot + 文案，右"换我 / 在岗"按钮
5. **菜单卡**（`.menu-card`）：4 行（我的订单 / 审核申请 / 我发布的菜品 / 退出登录），每行彩色图标块 + 标题 + 描述 + chevron
6. **页脚品牌**："Eat · 一家人的餐桌"

**视觉层级**（未登录态）：
1. **Hero 卡片**（`.guest-hero`）：渐变 + 装饰光斑
2. **大头像 + "?号徽章"**：抽象人形插画
3. **"登录后开启更多功能"** 标题
4. **特性列表**：3 条 feature（家庭共享 / 主厨轮换 / 订单同步）
5. **大号 CTA**："立即登录" 按钮

**核心数据**：
- `user`：从 `currentUser` 缓存读取
- `family`：家庭对象
- `members[]`：家庭成员
- `todayCook`：今日主厨
- `weeklyStats`：`{ cookDays, dishCount, orderCount }`
- `pendingCount`：待审核申请数
- `todayDateStr`：今日日期文案

**API 调用**：
- `getCurrentFamily(userId)`
- `getFamilyMembers(familyId)`
- `getTodayCook(familyId, date)`
- `getPendingRequests(familyId)` — 算未处理数
- `getFamilyDishes(familyId)` + `getOrdersByFamily(familyId)` — 算本周数据
- `switchCook(familyId, userId, date)` — 切换主厨

**关键交互**：
- Profile 点击 → 未登录跳 login；已登录无操作
- "换我" 按钮 → 调用 switchCook → 重新拉数据
- 复制码 → `uni.setClipboardData` + toast
- 审核申请：未当主厨时弹 warn toast，不跳转
- 退出登录 → 清 storage（7 项）→ 切未登录态

**视觉特色**：
- 全卡片化设计，4 张主卡片
- 主厨角标：`MemberAvatar` 的 `badge="厨"`
- 4 个彩色 icon 块用 `AppIcon` 渐变背景 + 白图标

---

### 2.4 `pages/login/login` — 登录 / 注册

**核心功能**：手机号 + 验证码免验证登录 / 昵称 + 家庭创建或加入注册。

**视觉层级**：
1. **顶部 Logo 区域**（大标题 "Eat" + 副标题 "一家人的餐桌"）
2. **模式切换**：`SegmentTabs`（登录 / 注册）
3. **登录表单**：
   - 手机号输入
   - "获取验证码" 按钮（无验证码，本项目免验证直接登录）
4. **注册表单**（额外字段）：
   - 昵称
   - 家庭模式：`SegmentTabs`（创建新家庭 / 加入家庭）
   - 创建时：家庭名称输入
   - 加入时：6 位家庭码 + 预览按钮
5. **家庭预览卡片**：输入合法家庭码后预览家庭信息
6. **主操作按钮**："登录" / "注册"

**核心数据**：
- `mode`：`'login' | 'register'`
- `familyMode`：`'create' | 'join'`
- `phone`：手机号
- `nickname`：昵称
- `familyName`：家庭名
- `familyCode`：家庭码
- `familyPreview`：家庭预览对象
- `loading`：防重复提交

**API 调用**：
- `loginByPhone(phone)` — 登录（如手机号已注册直接返回 userId）
- `registerByPhone({ phone, nickname, familyCode, familyName })` — 注册
- `previewFamilyByCode(code)` — 6 位码预览家庭

**校验**：
- 手机号：`/^1\d{10}$/`
- 家庭码：`/^[\dA-Za-z]{6}$/`

**关键交互**：
- 切换模式 → 重置 familyPreview
- 验证码按钮无真实发送逻辑（项目简化）
- 注册时如选"加入"必须先 preview 才能提交
- 成功后：`uni.setStorageSync` 7 项用户信息 → 跳回上一页

---

### 2.5 `pages/publish/publish` — 发布菜品

**核心功能**：主厨或其他成员发布家庭共享菜品，支持图片裁剪 + 分类选择 + 时段选择。

**视觉层级**：
1. **顶部时段切换**：`SegmentTabs`（早餐/午餐/晚餐）
2. **图片上传区**（大卡片）：
   - 未上传：占位 + "点击上传" 提示
   - 已上传：显示图片 + 删除/重新裁剪按钮
3. **裁剪弹层**：自定义 canvas 裁剪器（缩放/拖动/双指缩放）
4. **图片预览弹层**：全屏查看上传后的图
5. **菜名输入**（必填）
6. **价格 slider**（0-200 元）
7. **分类选择**（chip 列表 + 搜索 + "自定义" 入口）
8. **自定义分类弹层**
9. **菜品描述**（多行 textarea）
10. **底部发布按钮**

**核心数据**：
- `mealTime`：当前选中的时段
- `form`：`{ name, description, category, image, price }`
- `cropper`：裁剪器状态（scale, offsetX/Y, dragging, ...）
- `imageUploading`：上传中标志
- `imageViewerVisible`：预览标志
- `categories[]`：分类列表
- `showCategoryPicker`：自定义分类弹层
- `todayCook`：今日主厨（用于判断发布权限）

**API 调用**：
- `getTodayCook(familyId, date)` — 校验发布权限
- `getCategories()` — 拉分类
- `createCategory({ name })` — 新建分类
- `uploadFile(filePath)` / `uploadBlob(blob)` — 上传图片
- `createDish(dish)` — 提交菜品

**校验规则**：
- 菜名：必填，长度 ≤ 20
- 价格：slider 0-200，步长 1
- 图片：选填，但建议传
- 分类：必选
- 发布权限：必须是今日主厨才能发布（取 `todayCook.cookUserId === currentUserId`）

**图片裁剪（核心难点）**：
- 用 canvas + touch 事件实现：单指拖动 + 双指缩放
- 裁剪框固定 320×240rpx
- 输出：base64 → 转 blob → 上传

**关键交互**：
- 上传图片 → 进入裁剪弹层 → 确认 → 触发上传
- 选分类：chip 单选，未列出的可搜索/自定义
- 提交时校验所有必填项 → 调 createDish → 成功 toast → 返回上一页

---

### 2.6 `pages/order/order` — 订单

**核心功能**：查看家庭所有订单（按时间倒序）+ 统计总金额 + 切换状态。

**视觉层级**：
1. **顶部统计卡**（`.order-stat`）：总订单数 + 总金额
2. **订单列表**：每条订单是一张卡片
   - 订单号（短）
   - 状态 tag（待支付/已支付/制作中/已完成）
   - 时间
   - 菜品明细（图片 + 名称 + 单价 × 数量）
   - 合计金额
   - 操作按钮（按状态显示：去支付 / 制作中 / 完成 / 取消）

**核心数据**：
- `orders[]`：当前家庭所有订单
- `orderLoading`：加载状态
- `familyId` / `familyName`：上下文

**API 调用**：
- `getOrdersByFamily(familyId)` — 拉家庭订单

**computed**：
- `totalAmount`：所有订单金额累加

**订单状态机**（`status`）：
```
0-待支付 → 1-已支付 → 2-制作中 → 3-已完成
   ↓
 取消
```

**关键交互**：
- 下拉刷新 → 重新拉订单
- 状态 tag 颜色：待支付（warning）/ 已支付（info）/ 制作中（primary）/ 已完成（success）
- 长按订单可取消（h5 only）

---

### 2.7 `pages/edit-request/edit-request` — 申请修改菜品

**核心功能**：点菜人对"现有菜品"提交修改申请（修改菜名/价格/描述/图片）或新增菜品申请，待主厨审批。

**视觉层级**：
1. **顶部时段**：`SegmentTabs`（早/午/晚）
2. **类型选择**：`SegmentTabs`（修改现有菜品 / 新增菜品）
3. **类型 = 修改时**：菜品选择器（横滑 chip 列表 + 当前选中态）
4. **菜品表单**（修改/新增共用）：
   - 菜名输入
   - 描述 textarea
   - 价格 slider
   - 分类选择
   - 图片上传
5. **我的申请列表**：当前用户提交过的所有申请（状态 + 拒绝原因）

**核心数据**：
- `mealTime`：当前时段
- `editType`：`'modify' | 'add'`
- `existingDishes[]`：当前家庭当前时段的所有菜品
- `selectedDish`：选中的现有菜品
- `form`：表单数据
- `myRequests[]`：当前用户的申请记录
- `categories[]`：分类

**API 调用**：
- `getDishesByMealTime(mealTime, familyId)` — 拉现有菜品
- `getCategories()` — 拉分类
- `getRequestsByUserId(userId)` — 拉我的申请
- `createEditRequest(requestData)` — 提交申请

**关键交互**：
- 选 modify + 选菜品 → 表单预填该菜品的数据
- 选 add → 表单空白
- 提交申请 → 调 createEditRequest → 成功 toast → 重新拉"我的申请"
- 拒绝原因展示（status=2 时显示 rejectReason）

**状态机**：
- 0-待审批 / 1-已通过 / 2-已拒绝

---

### 2.8 `pages/approve/approve` — 审核申请

**核心功能**：今日主厨审核家庭成员提交的菜品修改/新增申请。

**视觉层级**：
1. **顶部权限校验**：
   - 是主厨：显示待审批列表
   - 不是主厨：显示"今天不是你做饭"空状态
2. **待审批列表**：每条申请是一张卡
   - 申请人头像 + 昵称
   - 申请类型（修改/新增）
   - 目标菜品（如 modify）
   - 改动对比（旧 → 新）
   - 拒绝原因输入（展开）
   - 操作按钮：批准 / 拒绝
3. **空状态**：无待审批时

**核心数据**：
- `pendingList[]`：当前家庭待审批申请
- `members[]`：家庭成员（用于显示申请人信息）
- `loading`：加载中

**API 调用**：
- `getPendingRequests(familyId)` — 拉待审批
- `getFamilyMembers(familyId)` — 拉成员
- `approveRequest(id)` — 批准
- `rejectRequest(id, reason)` — 拒绝

**关键交互**：
- 入口权限校验：my.vue 中点击 "审核申请" 时就已校验过
- 拒绝需要输入原因（不输入禁用按钮）
- 批准/拒绝成功后从列表移除该项
- 改动对比：旧值 → 新值（差异用主色高亮）

---

### 2.9 `pages/dish-detail/dish-detail` — 菜品详情

**核心功能**：展示菜品完整信息 + 提供"点这道"入口（跳 edit-request）。

**视觉层级**：
1. **顶部大图轮播**（菜品图 + 截图占位）
2. **菜品基本信息**：
   - 菜名（大字）
   - 价格
   - 分类 tag
   - 时段（早/午/晚）
3. **菜品描述**（多行）
4. **主厨信息**：头像 + 昵称 + 日期
5. **底部固定 CTA**：
   - "点这道 → 申请修改" 按钮
   - "我也要做这道" 按钮

**核心数据**：
- `dish`：菜品对象（含主厨、分类、时段、描述、图片）
- `familyName`：家庭名（用于 header）

**API 调用**：
- `getDishById(id)` — 拉菜品
- `getCurrentFamily(userId)` — 兜底拿家庭名

**路由入参**：
- `id`：菜品 ID（onLoad 接收）

**关键交互**：
- 点"申请修改" → 跳 edit-request 带 `?mealTime=xxx`
- 点"我也要做这道" → 跳 publish 带 `?mealTime=xxx`
- 图片可点击放大预览

---

## 3 · 18 个组件清单

| 组件 | 用途 | 关键 props |
|---|---|---|
| `AppShell` | 页面外壳（顶部 slot / 内容 / 底部 tab-bar / #fab）| `showTabBar`, `currentTab`, `contentStyle` |
| `AppIcon` | 31 个 lucide 风 SVG 图标（linecap round）| `name`, `size: sm/md/lg/xl`, `tone`, `stroke`, `bg` |
| `TopBar` | 自定义导航栏 | `title`, `variant: simple/gradient`, `fallbackRoute` |
| `PageLayout` | 子页 layout（带返回按钮 + 标题）| `title` |
| `HeroPanel` | 大色块 hero（3 tone + fullwidth）| `tone`, `fullwidth` |
| `SectionCard` | 通用白卡 | `title`, `padding` |
| `ListCard` | 列表卡（row-image/column/compact）| `layout`, `title`, `desc`, `image`, `meta` |
| `InfoCard` | 信息卡（带 action slot）| `title`, `desc` |
| `FormItem` | 表单行（label + value + arrow）| `label`, `value`, `arrow` |
| `EmptyState` | 空状态（emoji + title + hint + CTA）| `emoji`, `title`, `hint`, `ctaText` |
| `StatusBadge` | 状态 tag（5 tone）| `status`, `tone` |
| `Tag` | 通用 tag（5 tone × 2 variant × 2 size）| `tone`, `variant: solid/soft`, `size` |
| `Divider` | 分割线（水平/垂直 + inset）| `direction`, `inset` |
| `SkeletonBox` | 骨架屏（shimmer 动画）| `width`, `height`, `radius` |
| `MemberAvatar` | 成员头像（4 尺寸 + 渐变环 + 角标）| `text`, `size`, `ring`, `badge`, `badgeTone` |
| `RoleChip` | 角色 chip（5 tone + 发光点）| `text`, `tone`, `dot`, `variant` |
| `DateBadge` | 日期徽章 | `date`, `variant` |
| `SegmentTabs` | 分段控制器 | `tabs: [{ value, label }]`, `activeColor` |
| `Fab` | 悬浮按钮（绝对定位）| `icon`, `position`, `bottom`, `customStyle` |

### AppIcon 31 个图标（按用途分组）

```
导航: home, arrow_right, chevron_right, chevron_down, plus, close
用户: users, user, chef, crown
业务: dish, order, approve, copy, edit, share, pot
状态: star, flame, bell, check, warn, info
工具: calendar, clock, location, heart, search, settings, qr, login, logout
```

---

## 4 · 4 个工具模块

### 4.1 `utils/api.js` — 32 个端点

按业务域分组：

| 域 | 端点 | 方法 |
|---|---|---|
| **认证** | `loginByPhone(phone)` | POST |
| | `registerByPhone({ phone, nickname, familyCode, familyName })` | POST |
| | `getCurrentFamily(userId)` | GET |
| | `previewFamilyByCode(code)` | GET |
| **家庭** | `getFamilyMembers(familyId)` | GET |
| | `getTodayCook(familyId, date)` | GET |
| | `switchCook(familyId, userId, date)` | POST |
| | `getCookHistory(familyId)` | GET |
| **菜品** | `getFamilyDishes(familyId)` | GET |
| | `getDishById(id)` | GET |
| | `getDishesByMealTime(mealTime, familyId)` | GET |
| | `getDishesByDate(familyId, date)` | GET |
| | `getDishesByCook(cookUserId)` | GET |
| | `createDish(dish)` | POST |
| | `deleteDish(id)` | DELETE |
| **分类** | `getCategories()` | GET |
| | `createCategory(category)` | POST |
| **修改申请** | `getPendingRequests(familyId)` | GET |
| | `getFamilyRequests(familyId)` | GET |
| | `getRequestsByUserId(userId)` | GET |
| | `createEditRequest(requestData)` | POST |
| | `approveRequest(id)` | PUT |
| | `rejectRequest(id, reason)` | PUT |
| **订单** | `getOrdersByUserId(userId)` | GET |
| | `getOrdersByFamily(familyId)` | GET |
| | `createOrder(order)` | POST |
| | `updateOrderStatus(id, status)` | PUT |
| **文件** | `uploadFile(filePath)` | uni.uploadFile |
| | `uploadBlob(blob, filename)` | xhr |

### 4.2 `utils/date.js` — 日期格式化

- `formatYMD(date)` → "2026-06-17"
- `formatYMDHM(date)` → "2026-06-17 14:30"
- `formatRelativeTime(date)` → "刚刚 / 5 分钟前 / 2 小时前 / 昨天 / MM-DD"

### 4.3 `utils/toast.js` — 反馈

- `info(msg)` / `success(msg)` / `warn(msg)` / `error(msg)` → 统一封装 `uni.showToast`
- 颜色规则：info=蓝 / success=绿 / warn=橙 / error=红

### 4.4 `utils/categoryDict.js` — 分类字典

- 预置 12 个常用分类（主食 / 炒菜 / 汤品 / 凉菜 / ...）
- 提供 `findCategoryByName(name)` / `listAll()` / `addCustomCategory(name)`

---

## 5 · 设计系统（`styles/tokens.css`）

### 主色（品牌橘）

```
--color-primary: #ff6b35          (主品牌色)
--color-primary-deep: #ff5f36     (深一档)
--color-primary-light: #ff8a5b    (浅一档)
--color-primary-soft: #fff1ea     (浅底)
--color-primary-bg: #fff7f1       (区域背景)
```

### 语义色

```
--color-success: #36a854  (绿)
--color-warning: #ff9500  (橙黄)
--color-danger: #f44336   (红)
--color-info: #3b82f6     (蓝)
```

### 渐变（5 个）

```
--gradient-primary: 135deg, #ff6b35 → #ff8a5b
--gradient-primary-soft: 135deg, #ff8a5b → #ff6b35
--gradient-primary-strong: 主色强渐变（更亮）
--gradient-info / success / danger: 语义色渐变
--gradient-blob-1 / blob-2: 装饰光斑
```

### 字号（rpx）

```
xs=20 / sm=22 / base=24 / md=26 / lg=28 / xl=32 / 2xl=36 / 3xl=40
```

### 圆角

```
sm=12 / md=16 / lg=20 / xl=22 / pill=999
```

### 阴影

```
sm / md / lg / primary / primary-soft
```

### 间距（4 步阶）

```
1=4 / 2=8 / 3=12 / 4=16 / 5=20 / 6=24 / 8=32 / 10=40 / 12=48 / 16=64
```

---

## 6 · 后端数据模型（10 张表）

| 表 | 说明 | 关键字段 |
|---|---|---|
| `app_users` | 用户 | userId (unique), phone (unique), nickname, familyId |
| `families` | 家庭 | familyCode (8位 unique), name |
| `family_members` | 家庭成员 | familyId + userId (unique) |
| `dishes` | 菜品 | familyId, name, price, image, category, cookUserId, cookDate, mealTime, createdByUserId, available |
| `dish_categories` | 分类 | name (unique), sortOrder, enabled |
| `dish_edit_requests` | 修改申请 | familyId, userId, originalDishId, name, price, status (0/1/2), rejectReason |
| `orders` | 订单 | orderNo, familyId, userId, totalPrice, status (0/1/2/3), address, phone |
| `order_items` | 订单明细 | orderId, dishId, quantity, unitPrice |
| `daily_cooks` | 每日主厨 | familyId, cookDate (unique with familyId), cookUserId |
| `file_resources` | 文件 | uuid, originalName, contentType, size, url |

### 关键约束

- `families.familyCode` 唯一 8 位（创建时随机生成）
- `dish_edit_requests.originalDishId` 可空（新增菜品时为空）
- `daily_cooks` 唯一约束 `(familyId, cookDate)` — 一天一家一个主厨
- `family_members` 唯一约束 `(familyId, userId)` — 防止重复加入

---

## 7 · 关键业务规则

### 7.1 主厨轮换

- 每天一家一个主厨，由 `daily_cooks` 表确定
- 主厨在 `today` 页能切换给任何家庭成员（点"换我"或 my 页"换我"）
- 主厨权限：发布菜品 + 审批修改申请 + 创建新分类
- 非主厨可点菜 + 提修改申请

### 7.2 菜品状态机

```
创建（available=true）→ 有人点 → 关联到订单 → 订单完结
                  ↓
              修改申请 → 主厨批准 → 字段更新 / 主厨拒绝 → 保留原值
```

### 7.3 订单状态机

```
0-待支付 → 1-已支付 → 2-制作中 → 3-已完成
   ↓
 取消（DELETE）
```

### 7.4 修改申请状态机

```
0-待审批 → 1-已批准（菜品更新） / 2-已拒绝（保留原值 + rejectReason）
```

### 7.5 权限矩阵

| 角色 | 发布菜品 | 申请修改 | 审批修改 | 切换主厨 | 创分类 |
|---|---|---|---|---|---|
| 主厨 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 家庭成员 | ❌ | ✅ | ❌ | ❌ | ❌ |

---

## 8 · 关键边界场景

| 场景 | 当前处理 |
|---|---|
| 未登录访问 | my.vue 切未登录 Hero；其他页面需先登录 |
| 家庭码预览失败 | toast 提示 + 按钮保持可点 |
| 选图失败 | toast 提示，未上传则表单提交时不带图 |
| 发布权限不足 | publish 顶部权限提示 + 禁用提交按钮 |
| 主厨非自己 | 申请提交弹 warn toast（"今天不是你做饭"） |
| 主厨切换冲突 | 后端按 `(familyId, cookDate)` upsert，无冲突 |
| 图片上传失败 | toast 提示，恢复按钮可点 |
| 裁剪取消 | cropper 关闭，图片未上传 |
| 申请被拒绝 | edit-request "我的申请" 列表显示 rejectReason |
| 订单取消 | 仅 h5 端支持，状态直接 DELETE |
| storage 清空 | my.vue onShow 重新拉缓存 |

---

## 9 · 性能与体验

### 9.1 已落地优化

- **Token 化**：392 处硬编码颜色 → 全部走 `var(--xxx)`，0 处遗漏
- **AppIcon 系统**：31 个 lucide 风 SVG 统一图标，stroke 2.4px，对比度优化
- **图片懒加载**：菜品大图用 `lazy-load`
- **Toast 统一**：4 种 tone 复用
- **路由懒加载**：Tab 页 + 子页 navigateTo

### 9.2 已知技术债

- **mp-weixin 编译问题**：`@vant/use 4.x` + `uni-app alpha 404` 报 "isVNode is not exported"，当前只跑 H5 端验证
- **Vant Picker 警告**：picker 组件在小程序端有兼容警告
- **图片裁剪**：自实现 canvas 裁剪器，复杂但可控
- **没有真实验证码**：登录免验证（开发期简化）
- **订单无支付集成**：纯状态流转

### 9.3 待做（按 ROI 排序）

- [ ] AppIcon 推广到 menu / today / approve / order（统一全站图标）
- [ ] 暗色模式 token 扩展（已有骨架，缺 [data-theme="dark"] 段）
- [ ] my.vue 拆 SectionCard（家庭信息 / 统计 / 菜单分组）
- [ ] publish.vue 图片裁剪换 uni-app 原生插件
- [ ] 接入真实支付（微信支付 V2）

---

## 10 · 速查：常见任务

### 新增一个页面

1. `pages/<name>/<name>.vue`
2. `pages.json` 注册
3. 如需登录态校验，开头 `const user = uni.getStorageSync('currentUser')`
4. 路由跳转：`uni.navigateTo({ url: '/pages/<name>/<name>' })`

### 新增一个组件

1. `components/<Name>.vue` — 显式 `components: {}` 注册
2. 颜色用 `var(--xxx)`，不要硬编码
3. 复杂 props 用 `computed` 包一层

### 新增一个图标

1. `AppIcon.vue` 末尾加 `<svg v-else-if="name==='your-icon'">...</svg>`
2. `viewBox="0 0 24 24"` + `fill="none"` + `stroke="currentColor"`
3. 路径从 lucide 复制即可

### 新增一个 API

1. `utils/api.js` 加 `export const xxx = (params) => request({ url, method, data })`
2. 后端 `controller` 加 `@GetMapping` / `@PostMapping`
3. 后端 `entity` + `repository` + `service` 三件套

### 改主题色

1. `styles/tokens.css` 改 `--color-primary` 等 4 个橘色变量
2. 全站自动跟随（前提是没用硬编码）
3. 验证：跑一遍所有页面，搜 `grep "#[a-f0-9]{6}"` 应为 0

---

## 11 · 路由全景

```
                    ┌─→ menu (Tab 1) ─→ dish-detail
                    │              └→ publish
                    │              └→ edit-request
                    │
    start (login?) ─┼─→ today (Tab 2) ─→ dish-detail
                    │               └→ edit-request
                    │
                    └─→ my (Tab 3) ─→ order
                                  └→ approve (限主厨)
                                  └→ login (未登录时)
```

---

**文档版本**：v1.0
**生成日期**：2026-06-17
**覆盖代码**：9 页面 + 18 组件 + 32 API + 10 数据表
**作者**：像素君（前端开发工程师）
