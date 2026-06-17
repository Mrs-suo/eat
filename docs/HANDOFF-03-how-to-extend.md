# 03 · 增改指南（给后续 AI）

> 每一节都是可直接 follow 的 SOP。先看"准备"再"步骤"再"检查清单"。

---

## 1 · 新增一个页面

### 准备

1. 确认页面归属：Tab 页 or 子页？
2. 确定路由参数（如 `?id=`）
3. 确定数据源（用哪些 API、读哪些 storage key）
4. 列出需要的组件（在 `02` 中查 API）

### 步骤

```bash
# 1. 创建文件
frontend/src/pages/<group>/<name>.vue
```

**最小骨架**：

```vue
<template>
  <AppShell :show-tab-bar="false" :current-tab="-1" content-style="padding: 24rpx;">
    <template #menu>
      <PageLayout title="新页面" :show-back="true" />
    </template>

    <EmptyState v-if="!list.length" emoji="🍽" title="还没有数据" hint="去发布一个吧" />
    <ListCard v-for="item in list" :key="item.id" :layout="'row-image'">
      {{ item.name }}
    </ListCard>
  </AppShell>
</template>

<script>
import { getXxx } from '@/utils/api.js'

export default {
  components: { AppShell, PageLayout, EmptyState, ListCard },
  data() {
    return { list: [] }
  },
  async onShow() {
    const user = uni.getStorageSync('currentUser')
    if (!user) { /* 跳登录或显示未登录态 */ return }
    this.list = await getXxx(user.familyId)
  },
  methods: {}
}
</script>

<style scoped>
/* 颜色用 var(--xxx)，不要硬编码 */
</style>
```

### 注册到路由

编辑 `frontend/src/pages.json`，在 `pages` 数组加：

```json
{
  "path": "pages/<group>/<name>",
  "style": { "navigationStyle": "custom" }
}
```

### 检查清单

- [ ] 在 `components: {}` 显式注册
- [ ] `onShow` 入口先读 `currentUser` 判断登录态
- [ ] 颜色用 `var(--xxx)`，跑 `grep "#[0-9a-fA-F]\{6\}" pages/yourpage/` 应为 0（除图片地址外）
- [ ] API 调用包在 `try/catch` + `toast.error(e.message)`
- [ ] 跑 `npm run dev:h5` 验证

---

## 2 · 新增一个组件

### 准备

1. 看 `02` 文档：是否已有类似组件可复用？
2. 决定 props 命名（驼峰，与现有风格一致）
3. 决定样式 token（颜色 / 圆角 / 阴影 / 间距）— 全部用 `var(--xxx)`

### 步骤

```bash
# 1. 创建文件
frontend/src/components/<Name>.vue
```

**最小骨架**：

```vue
<template>
  <view class="my-component" :class="[`my-component--${tone}`]">
    <slot />
  </view>
</template>

<script>
export default {
  name: '<Name>',
  props: {
    tone: { type: String, default: 'default' }
  }
}
</script>

<style scoped>
.my-component {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4);
  box-shadow: var(--shadow-card-soft);
}
.my-component--primary { border: 2rpx solid var(--color-primary); }
</style>
```

### 引用

```vue
import <Name> from '@/components/<Name>.vue'

export default {
  components: { <Name> }   // 显式注册
}
```

### 检查清单

- [ ] `name: '<Name>'` 设了 component name
- [ ] 显式 `components: { <Name> }` 注册（**无 easycom**）
- [ ] 0 硬编码颜色（`grep -E '#[0-9a-fA-F]{6}' components/<Name>.vue` 应为 0）
- [ ] 文档化在 `HANDOFF-02 §2` 加一节

---

## 3 · 新增一个图标

编辑 `frontend/src/components/AppIcon.vue`，在 `<svg>` 链末尾加：

```vue
<svg v-else-if="name==='your-icon'" :width="iconSize" :height="iconSize"
     viewBox="0 0 24 24" fill="none" :stroke="iconStroke" stroke-width="2.4"
     stroke-linecap="round" stroke-linejoin="round">
  <!-- 路径从 lucide 复制，viewBox 24x24 -->
  <path d="..." />
</svg>
```

**注意事项**：
- 必须**显式内联 `stroke` 和 `stroke-width`**（不用 CSS），否则 mp-weixin 编译剥样式
- `viewBox="0 0 24 24"`
- 路径从 https://lucide.dev 复制，确保 fill="none"

更新 `HANDOFF-02 §2.7` 图标表。

---

## 4 · 新增一个 API（前端）

编辑 `frontend/src/utils/api.js`：

```js
export const getMyEntity = (params) => request({
  url: '/my-entity',
  method: 'GET',
  data: params
})

export const createMyEntity = (body) => request({
  url: '/my-entity',
  method: 'POST',
  data: body
})
```

**注意**：
- 走 `uni.request`，**不**走 `fetch` / `XHR`
- 不传 `Authorization`（项目无 token）
- 错误自动包装为 `Error(message)`，调用方 `try/catch` 即可

---

## 5 · 新增一个 API（后端）

### 步骤

```bash
# 1. 如需新表，先在 src/main/resources/schema.sql 加 DDL，然后
#    用 mysql.exe 执行（项目当前 ddl-auto=create-drop，启动后自动建）
```

```sql
CREATE TABLE my_entities (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  ...
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

```bash
# 2. 创建 Entity
src/main/java/com/eat/entity/MyEntity.java

# 3. 创建 Repository
src/main/java/com/eat/repository/MyEntityRepository.java

# 4. 创建 Service
src/main/java/com/eat/service/MyEntityService.java

# 5. 创建 Controller
src/main/java/com/eat/controller/MyEntityController.java
```

### Entity 模板

```java
package com.eat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "my_entities")
@Data
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", length = 64)
    private String userId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "create_time", insertable = false, updatable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", insertable = false, updatable = false)
    private LocalDateTime updateTime;
}
```

### Repository 模板

```java
package com.eat.repository;

import com.eat.entity.MyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyEntityRepository extends JpaRepository<MyEntity, Long> {
    List<MyEntity> findByUserId(String userId);
}
```

### Controller 模板

```java
package com.eat.controller;

import com.eat.entity.MyEntity;
import com.eat.service.MyEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/my-entities")
@CrossOrigin
public class MyEntityController {

    @Autowired
    private MyEntityService service;

    @GetMapping
    public List<MyEntity> list(@RequestParam(required = false) String userId) {
        return service.list(userId);
    }

    @PostMapping
    public MyEntity create(@RequestBody MyEntity body) {
        return service.create(body);
    }
}
```

### 重启后端

⚠️ **生产 ddl-auto=create-drop**：每次重启**清空数据**。Schema 变更后必须：

1. 备份（如需保留数据）：`mysqldump eat_db > backup.sql`
2. 重启：`mvn -f backend/pom.xml spring-boot:run "--server.port=8080"`
3. 验证启动日志 `Started EatApplication in X.XXX seconds`

> 长期建议把 `ddl-auto` 改成 `validate` + 用 Flyway/Liquibase 管理 schema 变更。

### 检查清单

- [ ] 实体类无 `@NotNull` 等校验注解（如需校验，自己处理）
- [ ] Controller 方法上有 `userId` 参数，**后端不验证身份**（按项目当前惯例）
- [ ] 更新 `HANDOFF-01 §B` 端点表

---

## 6 · 改主题色（全局换皮）

**单文件改，全站自动跟随**（前提：代码全部用 `var(--xxx)`）。

```bash
# 1. 编辑
frontend/src/styles/tokens.css
```

只改这 4 行：
```css
--color-primary:        #ff6b35   → 你新主色
--color-primary-deep:   #ff5f36   → 主色深一档
--color-primary-light:  #ff8a5b   → 主色浅一档
--gradient-primary:     linear-gradient(135deg, #ff6b35 0%, #ff8a5b 100%);
                                              → 主色渐变
```

保存后 `npm run dev:h5`，全站自动重渲染。

### 验证清单

```bash
# 检查没有遗漏的硬编码
grep -rE '#[0-9a-fA-F]{6}' frontend/src/pages/ frontend/src/components/
# 输出应为空（除图片 URL 外）
```

如果输出非空，按文件改用 `var(--xxx)`。

---

## 7 · 改 API 请求基址（如部署到不同域名）

```js
// frontend/src/utils/api.js
const BASE_URL = '/api'   // 改成 https://your-domain.com/api
```

```js
// frontend/src/utils/api.js 末尾
const FILE_SERVICE_ORIGIN = 'http://localhost:8080'
// 改成你的后端域名
```

---

## 8 · 排查常见问题

### 8.1 H5 dev 报 `isVNode is not exported`

**Vant 4 + uni-app alpha 不兼容**。当前**只有 H5 端能跑**。临时解决：
```js
// vite.config.js 加 alias 把 @vant 转到兼容版本
// 长期建议：等 uni-app 升级到支持 vue 3.4 的稳定版
```

### 8.2 后端启动后接口 404

检查 `application.yml`：
```yaml
server:
  port: 8080   # 端口对吗
```

### 8.3 前端调接口 500/网络错误

- 浏览器开 `http://localhost:30000`
- DevTools → Network 查 `/api/xxx` 实际请求
- 看后端启动日志

### 8.4 上传图片后 `image` 字段是 base64 而非 URL

- `publish.vue` 用了 `uploadBlob`（H5），`shouldUploadFile(url)` 检查 URL 是否是 file 服务地址
- 非 H5 端用 `uni.uploadFile`
- 检查 `isFileServiceUrl` 是否把 `localhost:8080` 写死

### 8.5 菜品发布失败：权限不足

- `todayCook.cookUserId` 必须等于当前 `userId`
- 确认 `daily_cooks` 表当天记录
- 确认家庭成员关系

### 8.6 Vant Picker 警告

不影响功能，可忽略。如果非要去掉，换自实现 `picker-view`（参考 `today.vue`）。

---

## 9 · 上线前 Checklist（生产）

- [ ] **后端**：`ddl-auto` 改 `validate` 或 `none`
- [ ] **后端**：加 Spring Security + JWT
- [ ] **后端**：加 `@ControllerAdvice` 统一异常（避免 500 冒泡）
- [ ] **后端**：实体加 `@NotNull` / `@Size` 校验
- [ ] **后端**：CORS 限白名单（当前是 `*`）
- [ ] **后端**：MySQL 密码从 env var 读（不要硬编码）
- [ ] **后端**：`@Transactional` 加到写操作上
- [ ] **前端**：mp-weixin 端验证（当前 H5 only）
- [ ] **前端**：加暗色模式 token
- [ ] **前端**：加 `aria-` 标签 / `alt` 文本
- [ ] **文档**：把 v1 旧文档的过时数字同步（`docs/PRD-family-model.md`）
- [ ] **CI/CD**：跑 `npm run build:h5` 出 `dist/build/h5/`
- [ ] **CI/CD**：跑 `mvn clean package` 出 `target/*.jar`

---

**END OF HANDOFF-03**
