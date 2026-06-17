# 01 · 模块与功能需求

> 本文按"页面"组织（前端视角为主），每个页面包含：用途、数据流、API 调用、状态、组件用法、关键交互、边界场景。
>
> 后端部分按"业务域"组织：7 个 Controller、10 张表、关键业务规则。

---

## A · 前端页面

---

### A1 · `pages/menu/menu` — 菜单（家庭 / 我的）

**路径**：`pages/menu/menu`（Tab 1）  
**文件**：`frontend/src/pages/menu/menu.vue`

**作用**：浏览家庭共享菜单 / 个人私房菜，支持分类筛选 + 关键词搜索。

**视觉层级**：
1. 顶部模式切换 `SegmentTabs`：家庭菜单 / 我的私房菜
2. 今日主厨横幅（MemberAvatar + RoleChip + "换我"）
3. 家庭信息条（家庭名 + 邀请码 + 成员数，复制码）
4. 分类横滑栏（全部 + 全部菜品分类）
5. 菜品列表 `ListCard`（row-image）
6. 空状态 `EmptyState`
7. 右下角 `Fab` 发布按钮（已登录才显示）

**核心数据**：
```js
data() { return {
  activeMode: 'family' | 'mine',
  activeCategory: 0,  // 索引
  keyword: '',
  dishes: [],
  categories: [],
  weekStats: [],
  familyCode: '',
  members: [],
  family: null,
  familyDishes: [],
  // ...
}}
```

**API 调用**（`onShow → loadAll` 并行）：
- `getFamilyDishes(familyId)`
- `getCurrentFamily(userId)`（兜底刷新 familyCode）
- `getFamilyMembers(familyId)`
- `getCookHistory(familyId)`

**关键计算**：
- `filteredDishes`：mode × category × keyword × familyId 四重过滤

**关键交互**：
- 搜索框 `confirm-type="search"`（H5 软键盘搜索键）
- 长按菜品（H5 only）→ `deleteDish` + 重新拉
- 切换主厨 → 重拉菜品 + 重拉统计
- 复制码 → `uni.setClipboardData` + toast

---

### A2 · `pages/today/today` — 今日

**路径**：`pages/today/today`（Tab 2）  
**文件**：`frontend/src/pages/today/today.vue`

**作用**：按"早/午/晚"3 时段 + 任意历史日期，查当天菜品 + 今日主厨。

**视觉层级**：
1. 顶部日期卡片 + 时段 `SegmentTabs`（icon-large 变体，🌅 ☀️ 🌙）
2. 日期选择器（自实现 picker-view，年/月/日）
3. 今日主厨卡（仅当日显示，MemberAvatar + RoleChip + "换我来做"）
4. 菜品列表 `ListCard`
5. 空状态
6. 底部 `Fab`：主厨看到"发布 X 餐菜品"，非主厨看到"想今天换我做"

**核心数据**：
```js
data() { return {
  currentMeal: 'breakfast' | 'lunch' | 'dinner',
  selectedDate: Date,           // 默认今天
  meals: [{key, label, icon}, ...],  // 3 项
  pickerValue: [year-idx, month-1, day-1],
  pickerYearRange: [2020, 当前年],
  mealDishes: [],
  todayCook: null,
  members: [],
}}
```

**API 调用**（`onShow`）：
- `getDishesByDate(familyId, dateStr)` 或 `getDishesByMealTime(mealTime, familyId)`
- `getTodayCook(familyId, dateStr)`
- `getFamilyMembers(familyId)`

**关键计算 / 判定**：
- `currentMeal` 初始 = 当前小时智能判定：5-10 早 / 10-15 午 / 其余晚
- `pickerValue` 默认 = 当前年月日对应索引

**关键交互**：
- `changeDate(-1/+1)`：受 `MIN_DATE` 限制，不允许未来日期
- 切换日期 / 切换时段 → 重拉数据
- 点菜品 → 跳 `dish-detail?id=`
- "换我来做" → `switchCook` 后重拉

**视觉特色**：
- 三时段 emoji 配对（🌅/☀️/🌙）
- 滚轮式日期选择器（不用 van-picker，避免 mp-weixin 报错）

---

### A3 · `pages/my/my` — 我的

**路径**：`pages/my/my`（Tab 3）  
**文件**：`frontend/src/pages/my/my.vue`

**作用**：家庭信息中心 + 个人本周统计 + 快捷菜单 + 登出 + 未登录 Hero。

**视觉层级（已登录态）**：
1. 渐变 header + 3 个浮动光斑（`@keyframes floatBlob`）
2. Profile 卡：168rpx 大头像（`MemberAvatar size="xl" ring`）+ 昵称 + RoleChip（主厨/食客）+ 手机号
3. 家庭信息卡（`.family-card`）：家庭图标 + 名 + 邀请码 + 复制码 + 成员芯片（带"厨"角标）
4. 统计卡：3 列（做饭次数 / 菜品数 / 订单数）+ 2 段分隔
5. 今日主厨快捷卡
6. 操作菜单卡：4 行（我的订单 / 审核申请 / 我发布的菜品 / 退出登录）
7. 页脚："Eat · 一家人的餐桌"

**视觉层级（未登录态）**：
1. 渐变 Hero + 装饰光斑
2. 抽象人形插画头像（`gradient-avatar` + 角标）
3. "登录后开启更多功能" 标题
4. 3 条 feature（家庭共享 / 主厨轮换 / 订单同步）
5. 大号 CTA "立即登录"

**核心数据**：
```js
data() { return {
  user: null,
  family: null,
  members: [],
  todayCook: null,
  pendingCount: 0,
  weeklyStats: { cookDays, dishCount, orderCount },
  isGuest: true,
  pageLoading: true,
}}
```

**API 调用**（`onShow → loadAll`）：
- `getCurrentFamily(userId)`
- `getFamilyMembers(family.id)`
- `getTodayCook(family.id, date)`
- `getPendingRequests(family.id)`
- `getFamilyDishes(family.id)` + `getOrdersByFamily(family.id)`（算本周）

**关键计算**：
- `weeklyStats.cookDays`：过去 7 天内 `createTime` 唯一 `cookUserId` 数
- `weeklyStats.dishCount`：过去 7 天内菜品数
- `weeklyStats.orderCount`：过去 7 天内订单数

**关键交互**：
- Profile 点击 → 未登录跳 login
- "换我" → `switchCook` 后重拉
- 复制码 → `uni.setClipboardData` + toast
- 审核申请：非主厨时弹 warn toast，不跳
- 退出登录 → 清 7 个 storage key + 切未登录态

**视觉特色**：
- 4 张主卡片
- 主厨角标：`MemberAvatar badge="厨"`（**注意**：sm 尺寸下显示 16rpx 圆点，文字隐藏；md+ 显"厨"字）
- 4 个彩色 icon 块用 `AppIcon` 渐变背景 + 白图标

---

### A4 · `pages/login/login` — 登录 / 注册

**路径**：`pages/login/login`（子页，`navigationStyle: custom`）  
**文件**：`frontend/src/pages/login/login.vue`

**作用**：手机号免验证登录 / 昵称 + 创建/加入家庭 注册。

**视觉层级**：
1. 顶部返回按钮（深色 + 圆角白底）
2. 品牌标识 "Eat" + 副标题 "一家人的餐桌"
3. `SegmentTabs` 模式切换：登录 / 注册
4. **登录表单**：手机号 + "获取验证码" 按钮（**无真实验证码**）
5. **注册表单**（额外）：
   - 昵称
   - 家庭模式 `SegmentTabs`：创建 / 加入
   - 创建时：家庭名
   - 加入时：6 位家庭码 + 预览按钮
6. 家庭预览卡（输入合法码后显示）
7. 主操作按钮（带 loading 态）

**核心数据**：
```js
data() { return {
  mode: 'login' | 'register',
  familyMode: 'create' | 'join',
  phone: '',
  nickname: '',
  familyName: '',
  familyCode: '',
  familyPreview: null,
  loading: false,
}}
```

**API 调用**：
- `loginByPhone(phone)`
- `registerByPhone({ phone, nickname, familyCode, familyName })`
- `previewFamilyByCode(code)`

**校验**：
- 手机号：`/^1\d{10}$/`
- 家庭码：`/^[\dA-Za-z]{6}$/`

**关键交互**：
- 切模式 → 重置 `familyPreview`
- 注册选"加入"必须先 preview 才能提交
- 成功 → 写 7 个 storage key → `uni.navigateBack()`（或 fallback `switchTab /pages/my/my`）

---

### A5 · `pages/publish/publish` — 发布菜品

**路径**：`pages/publish/publish`（子页）  
**文件**：`frontend/src/pages/publish/publish.vue`

**作用**：主厨发布家庭共享菜品（**主厨权限才可发**），含图片裁剪 + 分类选择。

**视觉层级**：
1. `PageLayout` 顶部（返回 + 标题 + 餐时 `SegmentTabs`）
2. `HeroPanel`（餐时标签 + "新增菜单菜品"）
3. 图片上传大卡：
   - 未上传：占位 + "点击上传"
   - 已上传：显示图 + 重新裁剪 / 预览按钮
4. 裁剪弹层（自实现 canvas：单指拖动 + 双指缩放 + 滚轮缩放）
5. 图片预览弹层（全屏查看）
6. `SectionCard` 基础信息：分类选择（chip + 搜索 + 自定义）+ 菜名输入
7. `SectionCard` 菜品描述：textarea（限 120 字 + 计数器）
8. 底部发布按钮

**核心数据**：
```js
data() { return {
  mealTime: 'lunch',  // from query
  form: { name: '', description: '', category: '', image: '', price: 0 },
  cropper: { visible, imgUrl, scale, offsetX, offsetY, dragging, baseScale },
  imageUploading: false,
  imageViewerVisible: false,
  categories: [],
  showCategoryPicker: false,
  newCategoryName: '',
  todayCook: null,
}}
```

**API 调用**（`onLoad`）：
- `getTodayCook(familyId, date)`（权限校验）
- `getCategories()`
- `createCategory({ name })`（新建分类）
- `uploadFile(filePath)` / `uploadBlob(blob)`（图片）
- `createDish(dish)`

**校验规则**：
- 菜名：必填，≤ 20 字
- 分类：必选
- 图片：选填（但建议传）
- 价格：未在前端使用 slider（v1 文档提及，v2 已移除 — publish 表单不再含价格 slider）
- **权限**：`todayCook.cookUserId === currentUserId` 才能发

**关键交互**：
- 上传 → 进裁剪弹层 → 确认 → 触发上传
- 选分类：chip 单选，未列出的可搜索或自定义
- 提交 → 校验 → createDish → toast → 延迟返回上一页

**图片裁剪实现**（核心难点）：
- 自实现 canvas，**不依赖第三方插件**
- 裁剪框固定 320×240rpx
- 输出：base64 → blob → 上传（H5） / `uni.uploadFile`（mp）

---

### A6 · `pages/order/order` — 订单

**路径**：`pages/order/order`（子页）  
**文件**：`frontend/src/pages/order/order.vue`

**作用**：查看家庭所有订单（按时间倒序）+ 总金额 + 状态显示。

**视觉层级**：
1. `HeroPanel`（"家庭订单" + 总金额 + emoji 徽章）
2. 加载态 `SkeletonBox` × 3
3. 空状态 `EmptyState`
4. 订单列表 `ListCard layout="column"`：每条含
   - 订单号（短）
   - 状态 `Tag`
   - 时间
   - 菜品明细（名 × 数量 + 小计）
   - 页脚合计金额
   - 状态操作按钮（v1 文档提及，v2 仅显示，不操作）

**核心数据**：
```js
data() { return {
  orders: [],
  orderLoading: true,
  familyId: null,
  familyName: '',
}}
```

**API 调用**：
- `getOrdersByFamily(familyId)`

**计算**：
- `totalAmount`：所有订单 `totalPrice` 累加

**状态映射**（`status` → 显示）：
| 状态 | 文案 | 色调 |
|---|---|---|
| 0 | 待支付 | orange |
| 1 | 已支付 | blue |
| 2 | 制作中 | orange |
| 3 | 已完成 | green |

---

### A7 · `pages/edit-request/edit-request` — 申请修改菜品

**路径**：`pages/edit-request/edit-request`（子页）  
**文件**：`frontend/src/pages/edit-request/edit-request.vue`

**作用**：非主厨对"现有菜品"提修改申请，或新增菜品申请，待主厨审批。

**视觉层级**：
1. `HeroPanel`（家庭 + 餐时信息）
2. `SectionCard` 变更信息：
   - 变更方式 `SegmentTabs`（修改现有 / 新增菜品）
   - 修改模式：菜品选择器 chip（横滑 + 当前选中态）
   - 文本输入：菜名、描述
   - 分类选择器
   - 图片上传
3. 提交按钮
4. "我的申请记录" 列表 `ListCard layout="compact"`：名称 + 状态 Tag + 时间 + 拒绝原因

**核心数据**：
```js
data() { return {
  mealTime: 'lunch',  // from query
  editType: 'modify' | 'add',
  existingDishes: [],
  selectedDish: null,
  form: { originalDish: null, name: '', description: '', image: '', category: '' },
  myRequests: [],
  categories: [],
  showCategoryPicker: false,
}}
```

**API 调用**：
- `getDishesByMealTime(mealTime, familyId)`（拉候选）
- `getCategories()`
- `getRequestsByUserId(userId)`（我的申请）
- `createEditRequest(data)`（提交）

**关键交互**：
- modify + 选菜品 → 表单预填该菜品
- add → 表单空白
- 提交 → createEditRequest → toast → 重拉"我的申请"
- 拒绝原因（status=2）展示在申请卡

**状态映射**：
| 状态 | 文案 | 色调 |
|---|---|---|
| 0 | 待审核 | orange |
| 1 | 已通过 | green |
| 2 | 已拒绝 | red |

---

### A8 · `pages/approve/approve` — 审核申请

**路径**：`pages/approve/approve`（子页）  
**文件**：`frontend/src/pages/approve/approve.vue`

**作用**：今日主厨审核家庭成员的菜品修改/新增申请。

**视觉层级**：
1. `HeroPanel`（家庭名 + 待审核计数）
2. 加载中
3. 空状态 `EmptyState`（"没有待审核申请"）
4. 审批项 `ListCard layout="row-image"`：每条
   - header：申请人 emoji 头像 + 姓名 + 餐时/时间 + "修改"/"新增"标签
   - content：图片预览 + 菜品信息（名 / 原菜品 / 分类 / 描述）
   - footer：拒绝 + 通过按钮
5. 拒绝弹层：输入拒绝原因（`uni.showModal` prompt）

**核心数据**：
```js
data() { return {
  pendingList: [],
  members: [],
  loading: true,
}}
```

**API 调用**（`onShow`）：
- `getPendingRequests(familyId)`（并行）
- `getFamilyMembers(familyId)`
- `approveRequest(id)`（批准）
- `rejectRequest(id, reason)`（拒绝）

**关键交互**：
- 权限由 my.vue 入口处校验（非主厨不让进）
- 拒绝需输入原因（无原因禁用按钮）
- 批准/拒绝成功后从列表移除该项

---

### A9 · `pages/dish-detail/dish-detail` — 菜品详情

**路径**：`pages/dish-detail/dish-detail`（子页，`navigationStyle: custom`）  
**文件**：`frontend/src/pages/dish-detail/dish-detail.vue`

**作用**：展示菜品完整信息（只读）。

**视觉层级**：
1. `TopBar variant="transparent"`（固定在图片上方）
2. 菜品图（500rpx 高 + 底部渐变遮罩）
3. 内容区：
   - 菜名（大字）
   - 家庭标签 + 分类标签
   - `InfoCard` 基本信息（餐时 / 主厨 / 日期 / 发布人）由 `Divider` 分隔
   - `InfoCard` 简介
4. 加载中：`SkeletonBox` × 4

**核心数据**：
```js
data() { return {
  dish: null,
  familyName: '',
  dishId: null,
}}
```

**API 调用**：
- `onLoad(options)` → `this.dishId = options.id`
- `getDishById(id)`
- `getCurrentFamily(userId)`（兜底拿 familyName）

**关键交互**：
- 纯只读
- 顶部返回（`TopBar` fallback 到 `/pages/menu/menu`）

---

## B · 后端 7 个 Controller

| Controller | 路径前缀 | 端点数 | 关键端点 |
|---|---|---|---|
| `AppUserController` | `/api/users` | 4 | login / register / getById / getFamily |
| `FamilyController` | `/api/families` | 6 | code / byId / members / today-cook / switch-cook / cook-history |
| `DishController` | `/api/dishes` | 8 | family / byId / meal-time / by-date / by-cook / create / update / delete |
| `DishCategoryController` | `/api/categories` | 2 | list / create |
| `DishEditRequestController` | `/api/edit-requests` | 7 | pending / family / user / create / approve / reject / delete |
| `OrderController` | `/api/orders` | 6 | user / family / byId / create / status / delete |
| `FileController` | `/api/files` | 1 | upload（jpg/png/webp/gif，10MB 上限） |

### B1 · `AppUserController` — `/api/users`

| Method | Path | Body | Response | 说明 |
|---|---|---|---|---|
| POST | `/login` | `Map { phone }` | `{ user, family }` | **无验证码**；用户必须已注册且有家庭 |
| POST | `/register` | `Map { phone, nickname, familyCode?, familyName? }` | `{ user, family }` | 必传 `familyCode`（加入）或 `familyName`（创建） |
| GET | `/{userId}` | — | `AppUser` | 查用户 |
| GET | `/{userId}/family` | — | `Family` | 查用户所属家庭 |

**AppUserService.RegisterResult**：record `{ AppUser user, Family family }`

### B2 · `FamilyController` — `/api/families`

| Method | Path | Query / Body | Response | 说明 |
|---|---|---|---|---|
| GET | `/code/{code}` | — | `{ family, memberCount }` | 按家庭码预览 |
| GET | `/{familyId}` | — | `Family` | 家庭详情 |
| GET | `/{familyId}/members` | — | `List<AppUser>` | 成员列表 |
| GET | `/{familyId}/today-cook` | `?date=yyyy-MM-dd` | `DailyCook` | 当日主厨（无则自动建默认） |
| POST | `/{familyId}/switch-cook` | `{ userId, date }` | `DailyCook` | 切换主厨（必须为该家庭成员） |
| GET | `/{familyId}/cook-history` | — | `List<DailyCook>` | 主厨历史 |

**关键硬编码**：`MAX_FAMILY_MEMBERS = 2`（在 `FamilyService`）。`familyCode` 生成：6 位随机，字符集 `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`（去 I,O,0,1）。

### B3 · `DishController` — `/api/dishes`

| Method | Path | Query | Response | 说明 |
|---|---|---|---|---|
| GET | `` | `?familyId=` | `List<Dish>` | 家庭菜单（**仅 available=true**） |
| GET | `/{id}` | — | `Dish` | 按 ID |
| GET | `/meal-time/{mealTime}` | `?familyId=` | `List<Dish>` | 按餐时（breakfast/lunch/dinner） |
| GET | `/by-date` | `?familyId=&date=` | `List<Dish>` | 按日期 |
| GET | `/by-cook/{cookUserId}` | — | `List<Dish>` | 按主厨 |
| POST | `` | `Dish` JSON | `Dish` | 创建 |
| PUT | `/{id}` | `Dish` JSON | `Dish` | 更新 |
| DELETE | `/{id}` | — | `void` | 删除 |

### B4 · `DishCategoryController` — `/api/categories`

| Method | Path | Body | Response | 说明 |
|---|---|---|---|---|
| GET | `` | — | `List<DishCategory>` | 全部分类（**自动初始化默认**） |
| POST | `` | `DishCategory` JSON | `DishCategory` (201) | 创建 |

**默认分类**：`["主食", "小吃", "饮品", "甜点"]`，`sortOrder` 1-4。

### B5 · `DishEditRequestController` — `/api/edit-requests`

| Method | Path | Query | Response | 说明 |
|---|---|---|---|---|
| GET | `/pending` | `?familyId=` | `List<DishEditRequest>` | 待审批（status=0） |
| GET | `/family/{familyId}` | — | `List<DishEditRequest>` | 家庭全部 |
| GET | `/user/{userId}` | — | `List<DishEditRequest>` | 按用户 |
| POST | `` | `DishEditRequest` JSON | `DishEditRequest` | 创建 |
| PUT | `/{id}/approve` | — | `DishEditRequest` | 批准 → 改原菜品或建新菜品 |
| PUT | `/{id}/reject` | `?reason=` | `DishEditRequest` | 拒绝（带原因） |
| DELETE | `/{id}` | — | `void` | 删除 |

**status 枚举**：`0=pending, 1=approved, 2=rejected`  
**`originalDish` 字段**：可空（新增菜品时为 null）。`@ManyToOne @JoinColumn` 关联到 `dishes.id`。

### B6 · `OrderController` — `/api/orders`

| Method | Path | Query | Response | 说明 |
|---|---|---|---|---|
| GET | `/user/{userId}` | — | `List<Order>` | 按用户 |
| GET | `/family/{familyId}` | — | `List<Order>` | 按家庭 |
| GET | `/{id}` | — | `Order` | 按 ID |
| POST | `` | `Order` JSON（含 items） | `Order` | 创建（自动算总价） |
| PUT | `/{id}/status` | `?status=` | `Order` | 更新状态 |
| DELETE | `/{id}` | — | `void` | 删除 |

**status 枚举**：`0=待支付, 1=已支付, 2=制作中, 3=已完成`  
**orderNo 格式**：`ORD + yyyyMMddHHmmss + 6位UUID`

### B7 · `FileController` — `/api/files`

| Method | Path | Body | Response | 说明 |
|---|---|---|---|---|
| POST | `/upload` | multipart `file=@xxx` | `{id, url, relativePath, originalName, contentType, size}` | 仅 jpg/png/webp/gif |

**存储路径**：`./file-storage/yyyy/MM/dd/{uuid-no-dash}.{ext}`  
**访问 URL**：`http://localhost:8080/files/yyyy/MM/dd/{uuid}.{ext}`

---

## C · 10 张数据表

> 详细字段见 `00` 文档 + 实际 `entity/*.java` + `resources/schema.sql`。这里是 AI 必备的速查表。

### C1 · `app_users` — 用户

| 字段 | 类型 | 约束 |
|---|---|---|
| `id` | BIGINT | PK auto |
| `user_id` | VARCHAR(64) | **UNIQUE**，格式 `u_{phone}` |
| `phone` | VARCHAR(20) | **UNIQUE** |
| `nickname` | VARCHAR(32) | 可空，默认 `"家人" + phone.substring(7)` |
| `family_id` | BIGINT | 索引（非 FK） |
| `create_time` / `update_time` | TIMESTAMP | |

### C2 · `families` — 家庭

| `id` PK | `family_code` VARCHAR(8) **UNIQUE** | `name` VARCHAR(64) NOT NULL | `create_time` |

> 注意：实体长度 8 字符，但实际生成是 6 位（`FamilyService` 中随机生成），存在历史/兼容差异。

### C3 · `family_members` — 家庭成员

| `id` PK | `family_id` BIGINT NOT NULL | `user_id` VARCHAR(64) NOT NULL | `joined_at` |
|---|---|---|---|
| **UNIQUE** | `(family_id, user_id)` | | |

### C4 · `dishes` — 菜品

| 字段 | 备注 |
|---|---|
| `id` PK | |
| `family_id` BIGINT | 索引 |
| `name` VARCHAR(255) NOT NULL | |
| `description` VARCHAR(255) | 可空 |
| `price` DECIMAL(19,2) NOT NULL | 默认 0 |
| `image` VARCHAR(255) | URL 路径 |
| `category` VARCHAR(255) | |
| `cook_user_id` VARCHAR(64) | 主厨 userId |
| `cook_date` DATE | 默认今天 |
| `meal_time` VARCHAR(255) | breakfast/lunch/dinner |
| `created_by_user_id` VARCHAR(64) | |
| `available` BOOLEAN | 默认 true（**查询只返回 true**） |
| `create_time` / `update_time` | |

**索引**：`idx_dish_cook_date(family_id, cook_date)`

### C5 · `dish_categories` — 分类

| `id` PK | `name` VARCHAR(255) **UNIQUE** | `sort_order` INT DEFAULT 0 | `enabled` BOOLEAN DEFAULT true |

### C6 · `dish_edit_requests` — 修改申请

| 字段 | 备注 |
|---|---|
| `id` PK | |
| `family_id` / `user_id` | |
| `original_dish_id` BIGINT | **FK → dishes.id**，**可空**（新增菜品时 null） |
| `name` / `description` / `price` / `image` / `category` / `meal_time` | 同 Dish |
| `status` INT DEFAULT 0 | 0=pending, 1=approved, 2=rejected |
| `reject_reason` VARCHAR(500) | |
| `create_time` / `update_time` | |

**索引**：`idx_request_family(family_id)` / `idx_request_status(status)`

### C7 · `orders` — 订单

| 字段 | 备注 |
|---|---|
| `id` PK | |
| `order_no` VARCHAR(50) NOT NULL | `ORD + yyyyMMddHHmmss + 6位UUID` |
| `family_id` / `user_id` | |
| `items` `List<OrderItem>` | `@OneToMany(cascade=ALL)` |
| `total_price` DECIMAL(19,2) | 自动算 |
| `status` INT DEFAULT 0 | 0/1/2/3 |
| `address` VARCHAR(500) | |
| `phone` VARCHAR(20) | |
| `create_time` / `update_time` | |

### C8 · `order_items` — 订单明细

| `id` PK | `order_id` BIGINT **FK → orders.id** | `dish_id` BIGINT **FK → dishes.id** | `quantity` INT DEFAULT 1 | `price` DECIMAL(19,2) |

### C9 · `daily_cooks` — 每日主厨

| `id` PK | `family_id` BIGINT NOT NULL | `cook_user_id` VARCHAR(64) NOT NULL | `cook_date` DATE NOT NULL | `set_at` |
|---|---|---|---|---|
| **UNIQUE** | `(family_id, cook_date)` | | | |

### C10 · `file_resources` — 文件

| `id` PK | `original_name` / `stored_name`（UUID 无横杠） | `content_type` / `size` | `relative_path`（`yyyy/MM/dd/uuid.ext`） | `url`（完整 URL） | `create_time` |

---

## D · 关键业务规则

### D1 · 主厨轮换

- `daily_cooks` 唯一约束 `(family_id, cook_date)` → 一天一家一个主厨
- `getTodayCook` 无记录时**自动建默认主厨**（取家庭第一个成员）
- 切换主厨：`POST /switch-cook` body `{ userId, date }`，必须为家庭成员
- 主厨权限：发布菜品 + 审批修改 + 创建分类
- 非主厨可点菜 + 提修改申请

### D2 · 菜品状态机

```
创建（available=true）
  ↓
有人点 → 关联到 order_items
  ↓
修改申请 → 主厨批准 → 字段更新
                  主厨拒绝 → 保留原值 + rejectReason
```

### D3 · 订单状态机

```
0 待支付 → 1 已支付 → 2 制作中 → 3 已完成
   ↓
DELETE（仅 h5 端 long-press 触发）
```

### D4 · 申请状态机

```
0 待审批 ─┬→ 1 已批准（菜品更新或新建）
          └→ 2 已拒绝（保留原值 + rejectReason）
```

### D5 · 权限矩阵

| 角色 | 发布菜品 | 申请修改 | 审批修改 | 切换主厨 | 创分类 |
|---|---|---|---|---|---|
| 今日主厨 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 家庭成员 | ❌ | ✅ | ❌ | ❌ | ❌ |
| 家庭外 | ❌ | ❌ | ❌ | ❌ | ❌ |

> ⚠️ **以上"权限"全部是前端校验**。后端无任何身份验证，理论上任何人都可以调用任意接口操作任意家庭的数据。

### D6 · 关键约束与边界

| 场景 | 行为 |
|---|---|
| 家庭码重复 | 后端生成时循环检测，重试 |
| 家庭成员超过 2 人 | `FamilyService.joinFamily` 抛 `MAX_FAMILY_MEMBERS` 错误 |
| 主厨非家庭成员 | `switchCook` 抛错 |
| 菜品删除 | 软删？硬删？**目前是硬删**（`DELETE /api/dishes/{id}` 直接删行） |
| 申请已审批后再次审批 | `approveRequest` 内部检查 `status == 0`，否则抛错 |
| 文件上传超 10MB | Spring Boot Multipart 抛 `MaxUploadSizeExceededException`（**无统一异常处理**，会冒泡到 500） |
| Order 删除时 items | **级联删除**（`@OneToMany(cascade=ALL)`） |

---

## E · 路由全景

```
                           ┌─→ menu (Tab 1) ─→ dish-detail
                           │              ├→ publish
                           │              └→ edit-request
                           │
   (unauth? my → guest) ──┼─→ today (Tab 2) ─→ dish-detail
                           │                └→ edit-request
                           │
                           └─→ my (Tab 3) ─→ order
                                          ├→ approve (限主厨)
                                          └→ login (未登录时)
```

---

**END OF HANDOFF-01**
