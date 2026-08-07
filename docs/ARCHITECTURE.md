# 架构边界

`MuYunSpringApp` 是独立业务应用模板：新项目 fork 本仓库后，业务模块、应用配置和运行宿主都在自己的仓库内演进；MuYunSpring 通过稳定的 BOM 与 Starter 提供底座能力。模板的首日改造步骤见 [Fork 指南](FORK_GUIDE.md)。

## 模块关系

Gradle 生产依赖方向（调用者 → 被依赖者）：

```text
app-boot → app-<domain>-web → app-<domain> → MuYunSpring BOM / Starter
```

| 层 | 模块 | 负责什么 |
| --- | --- | --- |
| 应用组合 | `app-boot` | 启动、运行配置和已交付业务模块的组合。 |
| HTTP 交付 | `app-<domain>-web` | 标准模块投影、独立 HTTP 契约和 Web 测试。 |
| 业务领域 | `app-<domain>` | 模型、DAO、Service、领域规则与领域测试。 |
| 平台依赖 | MuYunSpring BOM / Starter | 数据访问、Ability、平台配置、IAM 和通用 Web 交付能力。 |

当前 `app-demo` / `app-demo-web` 是该结构的最小实例。

## 职责边界

### 领域模块

领域模块表达业务事实和业务规则：模型字段、聚合关系、Service、DAO、领域配置与测试都归属这里。它通过 Ability 组合获得标准行为，例如软删、缓存、树、排序、引用和启停；业务规则仍保持在所属 Service 中。

领域模块保持不含 Web 生产依赖。这样业务规则可以同时被标准 HTTP 投影、后台任务、消息消费或后续其他入口复用。

### Web 模块

Web 模块负责把领域 Service 交付为 HTTP 契约。标准业务对象优先使用 `WebSupport`、`CrudWeb` 和 `@PlatformStaticModule` 接入平台提供的 CRUD、权限、租户、审计与生命周期链路。

真正独立的业务接口也可以在这里声明；其业务读写仍应回到领域 Service，而不是在 Controller 中重复实现数据访问和业务规则。

### 应用宿主

`app-boot` 是唯一启动入口。它组合 `*-web` 模块、提供环境配置并启动 Spring Boot；新增领域实现、Controller、Repository 和可复用业务测试不放在宿主中。

## 前端认证与登录页

登录页是 App 级体验，可以复用平台默认页面，也可以按业务品牌自行实现。无论采用哪种页面，认证接口、token/session 存储、认证失效、强制改密和错误归一属于平台认证内核，业务页面不得各自定义协议。当前 `app-web` 通过已发布的 `@ximatai/muyun-web-app` 消费工作台、菜单与平台管理运行时；App 自己只保留 `authSession` 和 Todo 页面等应用体验编排。

## 前端源码边界

`app-web/src` 按应用壳与业务模块组织：

```text
App.vue                  应用壳、登录与平台工作台编排
app/auth/                App 级认证会话适配
modules/<domain>/        一个业务域的页面、请求 client 与模块声明
modules/index.ts         App 业务模块注册表；按菜单 route 解析页面
```

业务开发从 `modules/<domain>` 开始。模块目录只包含该领域自己的页面、状态和接口调用；在 `index.ts` 导出它的菜单 route 与组件。随后把模块加入 `modules/index.ts`，由应用壳统一完成页签、鉴权上下文和平台页面兜底。`App.vue` 不直接导入或判断具体 Todo、订单等业务字段，避免业务增加后把应用壳演变成巨型页面。

## 扩展模式

新增订单领域时，推荐形成一对模块：

```text
app-orders       Order、OrderDao、OrderService、订单领域测试
app-orders-web   OrderWebController、订单 HTTP 契约测试
```

再由 `app-boot` 聚合 `app-orders-web`。该结构让领域演进、HTTP 交付和应用组合各自保持清晰的变化范围。

## Demo 的作用

`TodoItem` 是可运行、可复制的最小静态业务纵切：标准标题实体、`completed` 业务字段、`BaseDao`、组合软删和缓存能力的 Service，以及 `CrudWeb` 标准 Web 投影。它不是平台功能的完整展示，而是新领域接入时最小且可验证的参考实现。

fork 后不要把 `app-demo` 演变成正式业务域：先以它阅读和验证接入形态，再创建 `app-<domain>` / `app-<domain>-web` 承载真实业务。Demo 可在新领域跑通后作为独立的、可回归验证的参考保留；若产品不再需要它，应在一次完整改动中移除其 Gradle 注册、Boot 组合、菜单、前端模块和测试，不能只删除部分源码。
