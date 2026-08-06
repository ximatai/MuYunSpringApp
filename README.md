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

| 模块 | 职责 |
| --- | --- |
| `app-boot` | 应用启动、运行配置和业务模块组合。 |
| `app-demo` | 最小待办业务：模型、DAO、Service 与领域测试。 |
| `app-demo-web` | 待办业务的标准 HTTP 投影与 Web 契约测试。 |

真实业务以 `app-<domain>` / `app-<domain>-web` 成对扩展。详见[架构边界](docs/ARCHITECTURE.md)。

## 快速运行

要求 Java 21 和 Docker Compose v2。

```bash
./gradlew test
cp app-boot/src/main/resources/application-local.yml.example application-local.yml
docker compose up -d
./gradlew :app-boot:bootRun --args='--spring.profiles.active=local'
```

应用默认监听 `http://127.0.0.1:8080`，本地 PostgreSQL 使用 `127.0.0.1:54322`。日志出现应用启动完成即表示装配成功；根路径返回 `404` 属于预期，因为样板没有把业务页面挂在 `/`。

首次开发态启动会初始化平台 schema，并创建本地管理员 `admin` / `admin123`。该默认密码仅允许用于本地环境。

## 验证

日常快速验证：

```bash
./gradlew test
```

它验证 Todo Demo 的静态模型契约、Service 能力组合和 Web 投影边界，不需要数据库。提交前建议再运行构件验证：

```bash
./gradlew clean test :app-boot:bootJar
```

框架版本由根目录 `gradle.properties` 的 `muyunSpringVersion` 唯一管理。默认从 Maven Central 解析；开发尚未发布的框架构件时，可临时传入本地消费者仓库：

```bash
./gradlew test -PmuyunRepository=/path/to/muyun-consumer-repo
```

完整的新增领域、运行配置、框架升级和验证步骤见[开发指南](docs/DEVELOPMENT.md)与[验证说明](docs/VERIFY.md)。

## 开源许可

本项目使用 [Apache License 2.0](LICENSE)。
