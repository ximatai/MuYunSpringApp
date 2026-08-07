# 开发指南

本指南说明如何在 `MuYunSpringApp` 中从零新增一个业务领域，并以可运行、可测试的方式交付。

## 1. 准备本地环境

需要 Java 21 和 Docker Compose v2；启动前端还需要 Node.js `>=22.23.0`。PostgreSQL 由 Compose 提供。第一次运行：

```bash
./scripts/dev-local.sh
```

需要同时启动 Todo 前端时使用 `./scripts/dev-local.sh --web`。脚本会在首次运行时创建 ignored 的 `application-local.yml`，启动 App 自己的 PostgreSQL，并在退出时仅停止本次后端和前端进程；数据库容器与数据卷保留。

`application-local.yml` 位于项目根目录且已被 Git 忽略，用于保存当前开发机的数据库连接和本地初始化密码。不要把真实密码写入示例文件或提交到仓库。

App 本地环境与框架仓库完全隔离：Compose 项目为 `muyunspring-app`，数据库为 `muyun_spring_app`，宿主 PostgreSQL 端口为 `54322`，独立命名卷为 `muyun-spring-app-postgres`；本地后端端口为 `8081`，前端端口为 `5174`。因此可以与框架的 `54321`、`8080`、`5173` 同时运行。

### 初始化管理员

裸库首次启动会创建平台管理员，用户名固定为 `admin`。在 `application-local.yml` 配置其初始密码：

```yaml
muyun:
  initial-admin:
    initial-password: replace-with-a-local-secret
```

部署到生产或共享环境时，不提交配置文件中的密码；通过运行环境提供 `MUYUN_INITIAL_ADMIN_INITIAL_PASSWORD`，或由部署平台的受管密钥注入。例如：

```bash
export MUYUN_INITIAL_ADMIN_INITIAL_PASSWORD='replace-with-a-secret'
```

该值只供初始数据链路创建管理员时读取，不是密码重置开关。管理员已经存在时，修改该配置不会改变其密码；应使用平台账户管理或既定的改密流程。

## 2. 先阅读可运行的 Demo

以 Todo Demo 为起点阅读，顺序如下：

1. `app-demo/.../TodoItem.java`：业务模型和数据库字段。
2. `app-demo/.../TodoItemDao.java`：领域 DAO。
3. `app-demo/.../TodoItemService.java`：领域模块别名和 Ability 组合。
4. `app-demo-web/.../TodoItemWebController.java`：标准 Web 投影。
5. 两个 `*Test`：模型与 Web 交付的最小契约。

它说明一个业务对象如何从领域模型进入平台能力与标准 HTTP 交付，而不是要求复制所有 Demo 名称或字段。

开始设计新领域前，先查阅[平台能力索引](PLATFORM_CAPABILITIES.md)，判断应组合哪类 Ability、何时使用动态对象，以及对应的平台专题入口。

## 3. 新增领域模块

以下以订单领域为例。

### 3.1 注册模块

在 `settings.gradle.kts` 注册两个模块：

```kotlin
include("app-orders", "app-orders-web")
```

### 3.2 创建领域模块

创建 `app-orders`，其 `build.gradle.kts` 可参考 `app-demo`。在其中放置：

- `Order`：继承或组合合适的平台模型契约，声明字段和约束。
- `OrderDao`：继承 `BaseDao<Order, String>`。
- `OrderService`：继承 `AbstractAbilityService<Order>`，声明稳定模块别名，并组合该领域需要的 Ability。
- 领域配置与测试：Repository 扫描、模型契约和业务规则测试。

业务规则由 `OrderService` 表达。例如状态转换、下单校验和领域动作都应从 Service 进入，使 HTTP、任务或后续消息入口复用同一规则。

### 3.3 创建 Web 模块

创建 `app-orders-web`，依赖领域模块：

```kotlin
dependencies {
    api(project(":app-orders"))
}
```

标准业务对象使用 `WebSupport`、`CrudWeb` 和 `@PlatformStaticModule` 声明投影；真正独立的 HTTP 接口也位于此模块。Controller 负责请求和响应语义，调用 `OrderService` 完成业务操作。

如果模块要进入平台标准管理页，Controller 还必须实现 `StaticModuleUiContributor` 并返回 `ModuleUiDefinition`，至少声明列表与表单字段。该声明是字段暴露策略：它决定业务字段的顺序、标签、必填和控件类型；不要期待平台根据 Java 实体自动把所有字段投射到 UI，也不要在 App 前端重复实现列表或详情抽屉。

### 3.4 由应用宿主组合

在 `app-boot/build.gradle.kts` 增加：

```kotlin
dependencies {
    implementation(project(":app-orders-web"))
}
```

至此订单领域会随应用启动被装配。`app-boot` 继续只负责组合与配置，不沉淀订单领域实现。

### 3.5 增加 App 前端模块（需要自定义体验时）

如果订单需要 App 自定义页面，在 `app-web/src/modules/orders/` 放置订单页面、请求 client 和 `index.ts`。`index.ts` 只声明菜单 route 与页面组件，再把它加入 `app-web/src/modules/index.ts` 的注册表。应用壳会为已注册模块提供当前用户、页签与平台工作台上下文；不要在 `App.vue` 中为订单增加路由判断或业务请求。

## 4. 选择静态或动态对象

- **静态对象**：业务规则稳定、需要 Java 类型与 Service 扩展时，使用领域模块中的 Java 模型、DAO 和 Service。
- **动态对象**：对象结构主要由配置决定时，使用平台元数据和动态运行态。

两种对象使用相同的平台能力与治理语义。选择动态对象不是绕过领域规则；需要稳定业务规则时，仍应从应用侧定义清晰的 Service 或扩展点。

## 5. 版本与本地框架联调

`gradle.properties` 是后端平台版本唯一入口；`app-web` 的构建会校验其与 npm 包版本一致：

- `muyunSpringVersion`：要消费的 MuYunSpring 发布版本。
- `springBootVersion` 与 `springDependencyManagementVersion`：应用构建工具版本。
- `appVersion`：本应用自身版本。

默认从 Maven Central 和 npm 获取框架依赖。联调尚未发布的后端框架构件时，不修改构建脚本，只在命令中传入本地消费者仓库和该仓库中实际存在的版本：

```bash
./gradlew test \
  -PmuyunRepository=/path/to/muyun-consumer-repo \
  -PmuyunSpringVersion=0.26.4-SNAPSHOT
```

本机持续联调未发布的前端平台包时，优先链接框架生成包：它只消费公开 `exports`、声明文件和样式，不走 npm 正式发布。框架重新打包会替换生成包目录，因此启动或重启 App 时使用 `dev:linked` 强制刷新 Vite 的依赖缓存。

```bash
npm run pack:consumer --prefix /path/to/MuYunSpring/muyun-web
cd app-web
npm link /path/to/MuYunSpring/build/consumer-npm/staging/web-app
npm run dev:linked
```

`app-web` 已配置 Vite 保留该链接的包路径，使平台包的 `vue`、Ant Design Vue 和图标 peer dependencies 仍从 App 自己的 `node_modules` 解析；不要向生成包目录安装依赖或为联调建立源码 alias。

交付前或需要确认干净安装时，再由框架构建 tarball 并在 App 中覆盖安装；该命令不修改 `package.json` 或锁文件：

```bash
npm run install:framework-local --prefix app-web -- /path/to/ximatai-muyun-web-app-0.26.4.tgz
```

## 6. 提交前验证

```bash
./gradlew clean test :app-boot:bootJar
npm ci --prefix app-web
npm run build --prefix app-web
```

涉及真实数据访问、建表、字段解析或 HTTP 装配时，再启动本地 PostgreSQL 和 `app-boot` 做运行态验证。更多验证标准见[验证说明](VERIFY.md)。
