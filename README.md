# Eat · 一家人的餐桌

> 家庭共享菜单 + 今日轮换主厨 + 审核申请 + 订单管理 的 uni-app 全栈小项目

## 技术栈

- **前端**：uni-app（Vue 3 SFC） + 组件库（AppShell / PageLayout / MemberAvatar / RoleChip 等 11+ 个）
- **后端**：Spring Boot 3 + Spring Data JPA + WebSocket
- **数据库**：MySQL 8.x（默认）/ H2 内存库（local profile）
- **构建**：Maven 3.8+ / HBuilderX 或 cli 跑前端

## 目录结构

```
eat/
├── backend/                Spring Boot 后端
│   ├── src/main/java/com/eat/
│   │   ├── controller/     REST 控制器
│   │   ├── service/        业务服务
│   │   ├── repository/     JPA Repository
│   │   ├── entity/         数据库实体
│   │   └── config/         Web/WebSocket 配置
│   └── src/main/resources/
│       ├── application.yml           默认配置（MySQL）
│       ├── application-local.yml     -Plocal 切换 H2 内存库
│       ├── schema.sql                建表脚本（自动执行）
│       └── data.sql                  初始化数据脚本
├── frontend/               uni-app 前端
│   ├── src/pages/          业务页面（my / today / menu / family / ...）
│   ├── src/components/     基础组件
│   └── src/utils/          API / 日期 / Toast 工具
└── docs/                   交接文档（HANDOFF-00~03）
```

## 快速启动（MySQL 模式，推荐）

### 1. 准备 MySQL

```sql
-- 用 root 登录后执行（密码默认 root，按需改）
CREATE DATABASE IF NOT EXISTS eat_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

确保 MySQL 8.0+ 已启动，端口 3306，账号 `root` / 密码 `root`。
**如需修改密码**，编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    username: 你的账号
    password: 你的密码
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"
```

看到 `Started EatApplication in X.XXX seconds` 即成功。
访问 `http://localhost:8080` 可测试 API（无前端时返回 404 是正常的）。

> **首次启动会自动建表**（`schema.sql`）+ **不插入任何 demo 数据**（`data.sql` 只为占位）。
> 后续重启不会再执行（`sql.init.mode: never`），避免重复。

### 3. 启动前端

用 HBuilderX 打开 `frontend/` 目录直接运行到浏览器；或用 CLI：

```bash
cd frontend
npm install
# H5 模式
npm run dev:h5
```

默认端口 30000，访问 `http://localhost:30000`。

## 快速启动（H2 内存库 / 零依赖模式）

不想装 MySQL？用 H2 内存库跑：

```bash
cd backend
mvn spring-boot:run \
  "-Dspring-boot.run.arguments=--spring.profiles.active=local --server.port=8080"
```

- 内存库每次重启数据清空
- H2 控制台：`http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:eat_db`
  - User: `sa` / Password: 空

## 后端重启标准流程

修改 schema 后**必须**走这套流程，否则旧 jar 仍在内存、新代码不生效：

```bash
# 1. 杀旧进程
tasklist /FI "IMAGENAME eq java.exe"
taskkill /F /PID <pid>

# 2. 清库（外键约束会让 ddl-auto: create-drop 失败，必须手动清）
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -proot -e "
USE eat_db;
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS app_users, daily_cooks, dish_categories, dish_edit_requests,
                   dishes, families, family_invitations, family_members,
                   file_resources, notifications, order_items, orders;
SET FOREIGN_KEY_CHECKS=1;"

# 3. 重新拉起
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8080"

# 4. 验证
netstat -ano | findstr LISTENING | findstr :8080
```

## 默认账号

无。首个用户登录时在 `app_users` 表自动创建（手机号即 userId）。
家庭创建人即"主理人"（占位管理员角色），成员顺序：
1. 家庭创建人（首位）
2. 其他成员按 `joinedAt` 升序

## 关键接口

| 路径 | 说明 |
|------|------|
| `POST /api/users/login` | 手机号登录/注册 |
| `GET  /api/families/current?userId=xxx` | 当前家庭 |
| `GET  /api/families/{id}/members` | 家庭成员（创建人排首位） |
| `GET  /api/dishes?familyId=xxx` | 家庭菜品 |
| `POST /api/orders` | 创建订单 |
| `GET  /api/notifications?userId=xxx` | 审核通知 |
| `POST /api/notifications/{id}/approve` | 通过审核 |
| `WS  /ws/notifications?userId=xxx` | 实时通知推送 |

## 文档索引

详细模块、UI 设计规范、组件 API 见 `docs/HANDOFF-*.md`：
- `HANDOFF-00-overview.md` 项目总览
- `HANDOFF-01-modules-and-features.md` 模块与功能矩阵
- `HANDOFF-02-ui-design-system.md` UI 设计系统 + 组件库
- `HANDOFF-03-data-model-and-api.md` 数据模型 + API 契约

## License

Private / Internal
