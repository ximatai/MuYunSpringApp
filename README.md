# MuYunSpringApp

`MuYunSpringApp` 是一个基于 [MuYunSpring](https://github.com/ximatai/MuYunSpring) 的独立业务应用样板。它以业务模块、应用配置和运行宿主组织交付，通过 BOM 与 Starter 接入已发布的平台能力。

它适合用作企业应用、内部管理系统或可配置业务系统的二开起点：开发者拥有自己的业务代码与发布节奏，同时直接使用平台已经沉淀的数据访问、租户、权限、审计、生命周期和标准 Web 交付能力。

## 为什么这种模式开发更快

快不等于把业务逻辑藏进框架。这里的提效来自把每个业务模块都会重复遇到的工程问题变成稳定的默认能力：

- **从业务模型开始**：模型、DAO 和 Service 位于同一个领域模块；标准 CRUD、软删、缓存、权限与租户语义由 Service 组合平台 Ability 获得。
- **Web 交付无需重复铺底**：`app-<domain>-web` 将 Service 投射为标准 HTTP 模块，避免为每个业务对象重复编写同类 Controller、查询和生命周期处理。
- **领域与交付可独立演进**：领域代码不含 Web 生产依赖；需要调整页面或 HTTP 契约时，不会把 URL、DTO 和请求上下文带入核心业务模型。
- **可复制而非空白起步**：Todo Demo 是一条完整且轻量的纵切，包含模型、DAO、Service、Web 投影和契约测试；新领域可按相同形态扩展。
- **版本与验证可控**：框架版本集中管理，默认使用 Maven Central；业务应用可在不改源码的情况下连接本地消费者仓库验证尚未发布的框架构件。

## 项目结构

Gradle 生产依赖方向（调用者 → 被依赖者）：

```text
app-boot → app-demo-web → app-demo → MuYunSpring BOM / Starter
```

| 模块           | 职责                                          |
| -------------- | --------------------------------------------- |
| `app-boot`     | 应用启动、运行配置和业务模块组合。            |
| `app-demo`     | 最小待办业务：模型、DAO、Service 与领域测试。 |
| `app-demo-web` | 待办业务的标准 HTTP 投影与 Web 契约测试。     |

真实业务以 `app-<domain>` / `app-<domain>-web` 成对扩展。详见[架构边界](docs/ARCHITECTURE.md)。

## 最小业务链路：Todo

Todo Demo 用三处业务代码发布一个“待办”模块。模型只声明业务字段；Service 选择需要的平台能力；Web 层声明模块身份后，标准 CRUD 与 OpenAPI 由平台投影出来。

```java
// app-demo/.../TodoItem.java
@Table(name = "app_demo_todo_item", comment = "二开示例待办")
public class TodoItem extends StandardTitledEntity {
    @Column(name = "completed", type = ColumnType.BOOLEAN, nullable = false,
            defaultVal = @Default(bool = TrueOrFalse.FALSE))
    private Boolean completed = Boolean.FALSE;
}
```

```java
// app-demo/.../TodoItemService.java
@Service
public class TodoItemService extends AbstractAbilityService<TodoItem>
        implements SoftDeleteAbility<TodoItem>, CacheAbility<TodoItem> {
    public static final String MODULE_ALIAS = "demo.todo_item";

    public TodoItemService(TodoItemDao dao) {
        super(MODULE_ALIAS, TodoItem.class, dao);
    }
}
```

```java
// app-demo-web/.../TodoItemWebController.java
@RestController
@PlatformStaticModule(application = TodoApplication.class,
        alias = TodoItemService.MODULE_ALIAS, title = "待办")
@StaticModuleOpenApi
@RequestMapping("/" + TodoItemService.MODULE_ALIAS)
public class TodoItemWebController extends WebSupport<TodoItemService>
        implements CrudWeb<TodoItem, TodoItemService> {
}
```

完整源码见 [TodoItem](app-demo/src/main/java/net/ximatai/muyun/app/demo/TodoItem.java)、[TodoItemService](app-demo/src/main/java/net/ximatai/muyun/app/demo/TodoItemService.java) 和 [TodoItemWebController](app-demo-web/src/main/java/net/ximatai/muyun/app/demo/web/TodoItemWebController.java)。

应用启动并以拥有 `demo.todo_item` 查看权限的用户登录后，在浏览器打开：

```text
http://127.0.0.1:8080/demo.todo_item/openapi
```

该 URL 返回模块的 OpenAPI 3.1.1 文档，可直接看到 Todo 的模型 schema 与可用标准动作。对应的列表 schema URL 是：

```text
http://127.0.0.1:8080/demo.todo_item/query/schema
```

两个 URL 都遵循模块查看权限；未登录时返回 `401`，这表示权限链路正常生效。

## 快速运行

要求 Java 21 与 Docker Compose v2；启动前端还需要 Node.js `>=22.23.0`。

```bash
./gradlew test
./scripts/dev-local.sh
```

同时启动 Todo 前端：

```bash
./scripts/dev-local.sh --web
```

联调尚未发布的前端平台包时，先按[开发指南](docs/DEVELOPMENT.md)建立 npm link，再使用不重装依赖的启动模式：

```bash
./scripts/dev-local.sh --web-linked
```

本地 profile 下，应用监听 `http://127.0.0.1:8081`，前端开发服务监听 `http://127.0.0.1:5174`，PostgreSQL 使用 `127.0.0.1:54322`。它们分别避开框架仓库的 `8080`、`5173` 与 `54321`，可同时运行。Compose 同时使用独立的 `muyunspring-app` 项目、`muyun_spring_app` 数据库和命名卷，不会复用框架开发数据。日志出现应用启动完成即表示装配成功；根路径返回 `404` 属于预期，因为样板没有把业务页面挂在 `/`。

首次开发态启动会初始化平台 schema，并创建用户名固定为 `admin` 的平台管理员。密码在项目根目录的 `application-local.yml` 中设置：

```yaml
muyun:
  initial-admin:
    initial-password: your-local-password
```

示例文件提供的 `admin123` 仅用于本地开发。该配置只在初始数据创建时读取，不会在后续启动时重置已有管理员密码；生产和共享环境必须以环境变量或受管密钥提供强密码，不能提交真实密码。

## 验证

日常快速验证：

```bash
./gradlew test
```

它验证 Todo Demo 的静态模型契约、Service 能力组合和 Web 投影边界，不需要数据库。提交前建议再运行构件验证：

```bash
./gradlew clean test :app-boot:bootJar
```

框架发布版本以根目录 `gradle.properties` 的 `muyunSpringVersion` 为准；前端构建会校验 npm 包声明与它一致。默认从 Maven Central/npm 解析；开发尚未发布的后端框架构件时，可临时传入本地消费者仓库和该仓库中的实际版本：

```bash
./gradlew test \
  -PmuyunRepository=/path/to/muyun-consumer-repo \
  -PmuyunSpringVersion=0.26.4-SNAPSHOT
```

前端临时联调本地 tarball 时使用：

```bash
npm run install:framework-local --prefix app-web -- /path/to/ximatai-muyun-web-app-0.26.4.tgz
```

完整的新增领域、运行配置、框架升级和验证步骤见[开发指南](docs/DEVELOPMENT.md)与[验证说明](docs/VERIFY.md)。需要判断平台已有能力和接入入口时，查看[平台能力索引](docs/PLATFORM_CAPABILITIES.md)。

## 开源许可

本项目使用 [Apache License 2.0](LICENSE)。
