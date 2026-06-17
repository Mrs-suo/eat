# Eat 项目 · 交接文档（v2 · 供 AI 使用）

> **文档定位**：这是一份给"接手这个项目"的 AI 工程师/设计师/产品看的机器可读文档。所有信息均来自对 `C:\Users\suozhijie\Desktop\eat\` 的源代码扫描（2026-06-17 真实状态），不是 PRD 推测。
>
> **文档结构**（4 份，可独立索引）：
> - [00 · 概览与约定](./HANDOFF-00-overview.md) — 你正在读
> - [01 · 模块与功能需求](./HANDOFF-01-modules-and-features.md) — 9 个页面 + 7 个 Controller 详尽说明
> - [02 · UI 设计规范](./HANDOFF-02-ui-design-system.md) — token 原文 + 19 个组件规约 + 配色用法
> - [03 · 增改指南](./HANDOFF-03-how-to-extend.md) — 新增页面/组件/接口/改主题色的步骤
>
> **与之共存的文件**（不替代）：
> - `EAT-PROJECT-OVERVIEW.md`（项目根目录）— 人类可读 v1 总览（中文），保留为项目历史快照
> - `docs/PRD-family-model.md` — 家庭模型改造的早期 PRD（已实施）
>
> **使用建议**：
> - AI 进入项目先读 `00`，再按任务读 `01` / `02` / `03`
> - 修复 bug / 加功能 → `01` 找现有逻辑 → `03` 按规范加
> - 改样式 / 加组件 → `02` 看 token 和组件 API
> - 任何不确定字段名 / 路径，先看 `01` 的实体表 + `02` 的 token 表

---

## 0 · 项目一句话

**Eat（吃什么）** 是一个家庭共享点餐小应用。**一个家庭、每天一个主厨、多个家人**。
- 今日主厨：发布菜品、审批修改申请、创建分类
- 其他家人：点菜、对菜品提修改/新增申请、查看订单
- 核心循环：早/午/晚餐 3 餐 + 每日主厨轮换 + 申请审批

**目标用户**：小两口（最多 2 人/家，硬编码 `MAX_FAMILY_MEMBERS = 2`），轮换做饭场景。

---

## 1 · 技术栈与版本

| 层 | 技术 | 版本 |
|---|---|---|
| 前端框架 | Vue 3 (Options API) | `^3.4.21` |
| 跨端 | uni-app alpha | `3.0.0-alpha-4040120241211001` |
| 状态 | Pinia **已装未用**，改用 `uni.setStorageSync` 持久化 | `^2.1.7` |
| UI 库 | Vant 4（仅用 van-search / van-tabs / van-picker / van-dialog） | `^4.9.24` |
| 构建 | Vite + `@dcloudio/vite-plugin-uni` | `^5.2.8` |
| 后端 | Spring Boot 3 + Spring Data JPA | `3.2.0` |
| Java | 17 | `pom.xml` 锁定 |
| ORM | Hibernate（无 MyBatis） | 由 SB BOM 管理 |
| DB（生产） | MySQL 8.4 | `mysql-connector-j` |
| DB（本地） | H2 in-memory（`local` profile） | `h2database` |
| 工具 | Lombok | `1.18.42` |
| 校验 | `spring-boot-starter-validation`（**已引入但未使用**——实体上无 `@NotNull` 等） | |
| 鉴权 | **无 Spring Security / 无 JWT**（userId 明文传递） | — |
| 测试 | `spring-boot-starter-test` 已引入，`src/test/` 为空 | — |

> **⚠️ 关键风险**（项目当前事实状态）：
> 1. 生产 `spring.jpa.hibernate.ddl-auto=create-drop` —— 每次重启清表
> 2. 任意调用方传入 `userId` 即可冒充 —— **无身份验证**
> 3. `Order.items` 与 `OrderItem.order` 双向引用无 `@JsonIgnore` —— 可能 JSON 循环
> 4. `application.yml` 密码明文 `root`
> 5. **H5 dev 端口实际是 `30000`**，不是 `30001`；prod 预览默认 `4173`

---

## 2 · 目录结构（真实扫描）

```
C:\Users\suozhijie\Desktop\eat\
├── EAT-PROJECT-OVERVIEW.md            # v1 中文总览（人类可读）
├── backend/                           # Spring Boot
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/main/
│       ├── java/com/eat/
│       │   ├── EatApplication.java
│       │   ├── config/WebConfig.java          # CORS + 静态文件
│       │   ├── controller/                    # 7 个 Controller
│       │   ├── entity/                        # 10 个 Entity
│       │   ├── repository/                    # 9 个 JpaRepository
│       │   └── service/                       # 7 个 Service
│       └── resources/
│           ├── application.yml                # MySQL / 生产
│           ├── application-local.yml          # H2 / 本地
│           ├── schema.sql                     # 10 张表 DDL
│           └── data.sql                       # 仅 `SELECT 1;` 无种子
├── frontend/                          # uni-app
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── App.vue                    # 入口，导入 tokens.css + 隐藏原生 tabbar
│       ├── main.js                    # Vue + Pinia + Vant
│       ├── pages.json                 # 路由 + tabBar
│       ├── manifest.json              # H5 hash 路由配置
│       ├── components/                # 19 个 .vue
│       ├── pages/                     # 9 个页面
│       ├── styles/tokens.css          # 设计 token 唯一源
│       └── utils/
│           ├── api.js                 # 25+ 端点封装
│           ├── categoryDict.js
│           ├── date.js
│           └── toast.js
└── docs/                              # 本目录
    ├── PRD-family-model.md            # 家庭模型改造 PRD（已实施）
    ├── HANDOFF-00-overview.md         # ← 本文件
    ├── HANDOFF-01-modules-and-features.md
    ├── HANDOFF-02-ui-design-system.md
    └── HANDOFF-03-how-to-extend.md
```

---

## 3 · 启动与运行

### 后端（Spring Boot）

```bash
# 默认配置（连本地 MySQL eat_db / root:root）
mvn -f backend/pom.xml spring-boot:run "--server.port=8080"

# 本地 H2 内存模式
mvn -f backend/pom.xml spring-boot:run -Dspring-boot.run.profiles=local
```

| 配置项 | 值 | 位置 |
|---|---|---|
| 端口 | `8080` | `application.yml` |
| MySQL URL | `jdbc:mysql://localhost:3306/eat_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8` | 同上 |
| 用户/密码 | `root` / `root` | 同上（**明文**） |
| `ddl-auto` | `create-drop`（生产） / `create`（local） | 同上 |
| `sql.init.mode` | `never`（生产） / `always`（local） | 同上 |
| 上传目录 | `./file-storage` | `app.upload.dir` |
| 上传 URL 前缀 | `/files` | `app.upload.url-prefix` |
| 文件大小上限 | 10MB | `spring.servlet.multipart.*` |

### 前端（uni-app H5）

```bash
cd frontend

# 开发（端口 30000，代理 /api -> localhost:8080）
npm run dev:h5

# H5 生产构建（输出到 dist/build/h5/）
npm run build:h5

# 微信小程序构建
npm run build:mp-weixin
```

| 配置项 | 值 |
|---|---|
| dev 端口 | `30000`（注意不是 30001） |
| H5 路由 | `hash` |
| 代理 | `/api` → `http://localhost:8080` |
| 真实后端基址（生产） | `http://localhost:8080`（`utils/api.js` 写死 `FILE_SERVICE_ORIGIN`） |

### 完整启动顺序

1. 启 MySQL（`net start MySQL84`）
2. 创库：`CREATE DATABASE IF NOT EXISTS eat_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 启后端：`mvn -f backend/pom.xml spring-boot:run "--server.port=8080"`
4. 启前端：`cd frontend && npm run dev:h5`
5. 浏览器开 `http://localhost:30000`

> **当前 Vant 在 mp-weixin 端报错** `isVNode is not exported`，所以 H5 是唯一能跑通的端。生产构建前先跑 H5 验证。

---

## 4 · 跨切面约定（AI 必须遵守）

### 4.1 前端"登录态"判定

应用**无 token、无 cookie**。登录态 = `uni.getStorageSync('currentUser')` 是否返回对象。

登录成功会写 7 个 key：
```js
uni.setStorageSync('currentUser', { userId, phone, nickname, familyId, familyCode, familyName })
uni.setStorageSync('userId', ...)
uni.setStorageSync('phone', ...)
uni.setStorageSync('nickname', ...)
uni.setStorageSync('familyId', ...)
uni.setStorageSync('familyCode', ...)
uni.setStorageSync('familyName', ...)
```

登出 = 清这 7 个 key。

`userId` 格式：`"u_" + phone`，例如 `"u_15102605106"`（**不是纯数字**）。

### 4.2 API 客户端（`utils/api.js`）

- 基址：`/api`（Vite 代理）
- 调用方：`uni.request`（**不走 fetch 也不走 XHR**，mock fetch/XHR 没用）
- 拦截器：仅设 `Content-Type: application/json`；**无 Authorization 头**
- 错误：非 2xx 包装为 `Error(message)`，消息取 `res.data?.message ?? 状态码`
- 文件上传用 `uni.uploadFile`（小程序路径）/ `XMLHttpRequest + FormData`（H5 blob）

### 4.3 路由（uni-app API，非 vue-router）

| 模式 | API | 用途 |
|---|---|---|
| Tab 切换 | `uni.switchTab` | menu / today / my |
| 子页跳转 | `uni.navigateTo` | login / publish / order / edit-request / approve / dish-detail |
| 返回 | `uni.navigateBack` | 常规 |
| 兜底 | `uni.reLaunch` | 栈底且无 fallback |

**子页参数**（约定）：
- `dish-detail?id={dishId}`
- `publish?mealTime=breakfast|lunch|dinner`
- `order?tab=mine`（来自 my "我发布的菜品"）
- `edit-request?mealTime=...`

### 4.4 组件注册

- 显式 `components: { MemberAvatar, ... }` 列出（**无 easycom 自动注册**）
- Vue 单文件组件 SFC，Options API（**不**用 Composition API）

### 4.5 后端安全边界

- 所有需要用户的端点都接受 `userId` 作参数或路径，**后端不校验**
- 任何第三方可以"冒充"任意用户读写数据（**已知风险**）
- CORS `*`，全开
- 全局无 `@ControllerAdvice`、无拦截器、过滤器、AOP、定时任务
- `@Transactional` 仅在 `AppUserService.register` / `FamilyService.createFamily/joinFamily/switchCook` / `DishCategoryService` 中

---

## 5 · 关键业务数字（不要猜，看这里）

| 数字 | 值 | 来源 |
|---|---|---|
| 家庭最大成员数 | **2**（硬编码 `MAX_FAMILY_MEMBERS`） | `FamilyService.java` |
| 家庭码长度 | **6** 位，字符集 `ABCDEFGHJKLMNPQRSTUVWXYZ23456789` | `FamilyService.java` |
| 每日主厨唯一性 | `(family_id, cook_date)` 唯一 | `daily_cooks` 表 DDL |
| 订单状态枚举 | `0=待支付, 1=已支付, 2=制作中, 3=已完成` | 后端 + 前端共用 |
| 申请状态枚举 | `0=待审批, 1=已批准, 2=已拒绝` | 后端 + 前端共用 |
| 默认餐时自动判定 | 5-10 早 / 10-15 午 / 其余晚 | `today.vue` `currentMeal` |
| 默认分类 | `["主食", "小吃", "饮品", "甜点"]`（`sortOrder` 1-4） | `DishCategoryService` |
| 文件大小上限 | 10MB | `application.yml` |
| 默认时间窗 | 早餐 5-10, 午餐 10-15, 晚餐 15-5 | `today.vue` |

---

## 6 · 详细文档索引

| 任务 | 读 |
|---|---|
| 改某个页面的逻辑 | `HANDOFF-01` 对应页面章节 |
| 加新页面 / 改导航 | `HANDOFF-01` 路由段 + `HANDOFF-03` |
| 调样式 / 加新组件 | `HANDOFF-02` |
| 加新 API | `HANDOFF-01` 后端段 + `HANDOFF-03` |
| 改品牌色 / 主题 | `HANDOFF-02` token 表 + `HANDOFF-03` |
| 排查 bug | `HANDOFF-01` 实体段 + 关键业务规则段 |
| 性能优化 | `HANDOFF-02` 性能段 + `HANDOFF-01` 加载策略段 |

---

**文档版本**：v2.0（基于真实代码扫描）  
**生成日期**：2026-06-17  
**覆盖范围**：9 页面 + 19 组件 + 25+ API + 10 数据表  
**作者**：像素君（前端开发工程师）
