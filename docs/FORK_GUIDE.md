# Fork 指南

本仓库是管理型业务应用模板。新项目应 fork 它，而不是复制 MuYunSpring 的 `muyun-boot` 或平台内部模块；平台只通过 Maven BOM / Starter 和 npm 包消费。以下步骤形成新应用的第一个可提交状态。

## 1. 建立应用身份

先完成一轮只改变应用身份、不改变业务行为的改名。一次提交中同步调整以下位置：

| 范围 | 当前位置 | 新项目应修改为 |
| --- | --- | --- |
| Gradle 项目名 | `settings.gradle.kts` 的 `rootProject.name` | 自己的应用仓库名 |
| Maven group 与 Java 根包 | `build.gradle.kts`，`app-*/src/main/java` | 自己稳定的组织与应用包名 |
| 启动类 | `app-boot/.../MuYunSpringAppApplication.java` | 自己的应用名；仍只负责装配 |
| Compose 与开发数据 | `compose.yaml` | 独立项目名、数据库名、数据卷和端口 |
| 本地配置 | `application-local.yml.example` | 自己的应用配置；真实密码仍只放 ignored 的 `application-local.yml` 或环境变量 |
| 前端标识 | `app-web/package.json`、页面标题与应用壳 | 自己的应用名称与品牌 |

应用别名、模块别名和 Java 包名是不同概念。运行时别名按平台约束使用小写点分形式，例如 `orders.sales_order`；不要把 Gradle 名或 Java 包名当作模块别名。

## 2. 先跑通模板

在删除或新增业务之前，使用独立的本地环境确认模板可运行：

```bash
./gradlew clean test :app-boot:bootJar
./scripts/dev-local.sh --web
```

登录 `http://127.0.0.1:5174/` 后，确认平台菜单和 Todo 示例均可进入。初始化管理员为 `admin`，密码由根目录 ignored 的 `application-local.yml` 中 `muyun.initial-admin.initial-password` 决定。更多运行与验证说明见 [开发指南](DEVELOPMENT.md) 和 [验证说明](VERIFY.md)。

## 3. 用新领域替代 Demo

`app-demo` / `app-demo-web` 只提供最小纵切参考，不是未来业务的默认包。新业务始终以一对模块开始：

```text
app-<domain>       模型、DAO、Service、领域规则与测试
app-<domain>-web   标准 HTTP 投影、独立接口与 Web 测试
```

1. 在 `settings.gradle.kts` 注册两个模块。
2. 参考 Todo 创建模型、`BaseDao` 与继承 `AbstractAbilityService` 的 Service；只组合实际需要的 Ability。
3. 在 `app-<domain>-web` 使用 `WebSupport`、`CrudWeb`、`@PlatformStaticModule` 和 `StaticModuleUiContributor` 声明标准管理页。
4. 在 `app-boot` 仅添加该 Web 模块依赖；不把 Controller、Service 或 Repository 放进宿主。
5. 需要自定义应用体验时，在 `app-web/src/modules/<domain>/` 添加页面、请求 client 和模块注册；标准管理页继续由平台工作台交付，不在 App 重写列表和抽屉。

新领域验证通过后，Demo 可暂时保留为参考和回归样本。决定移除时，连同后端模块、Boot 依赖、菜单配置、前端 Todo 模块及其测试完整移除，避免留下失效菜单或半截依赖。

## 4. 跟随平台升级

后端平台版本以根目录 `gradle.properties` 的 `muyunSpringVersion` 为唯一入口；`app-web/package.json` 中的 `@ximatai/muyun-web-app` 必须使用相同正式版本，构建脚本会校验二者一致。

每次升级使用一笔独立提交：更新两个版本坐标和 npm lockfile，然后执行：

```bash
./gradlew clean test :app-boot:bootJar
npm ci --prefix app-web
npm run build --prefix app-web
```

平台尚未发布时，不修改正式版本坐标。后端传入本地 consumer Maven 仓库与实际 Snapshot 版本；前端使用 `npm link` 和 `./scripts/dev-local.sh --web-linked`。这两条路径只用于联调，不进入应用的正式依赖声明。
