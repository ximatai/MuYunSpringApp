# MuYunSpringApp

`MuYunSpringApp` 是基于 [MuYunSpring](https://github.com/ximatai/MuYunSpring) 的独立业务应用样板，用于二次开发。
它不复制框架的 `muyun-boot`，只通过 BOM 与 Starter 使用已发布的平台能力。

## 模块

```text
app-boot → app-demo-web → app-demo → MuYunSpring BOM / Starter
```

| 模块 | 职责 |
| --- | --- |
| `app-boot` | Spring Boot 宿主、运行配置与应用启动。 |
| `app-demo` | 最小待办业务模型、DAO、Service 与领域测试。 |
| `app-demo-web` | 待办业务的标准 HTTP 投影与 Web 契约测试。 |

Demo 仅用于说明接入边界；真实业务按 `app-<domain>` / `app-<domain>-web` 扩展。

## 快速开始

要求 Java 21 和 Docker Compose v2。

```bash
cp app-boot/src/main/resources/application-local.yml.example application-local.yml
docker compose up -d
./gradlew :app-boot:bootRun --args='--spring.profiles.active=local'
```

应用默认监听 `http://127.0.0.1:8080`，本地 PostgreSQL 使用 `127.0.0.1:54322`。首次开发态启动会初始化平台 schema；默认初始管理员密码只适用于本地环境。

## 验证

```bash
./gradlew test
```

框架依赖版本由根目录 `gradle.properties` 的 `muyunSpringVersion` 唯一管理。正式版本默认从 Maven Central 解析；需要接入尚未发布的框架构件时可增加：

```bash
./gradlew test -PmuyunRepository=/path/to/muyun-consumer-repo
```

更多说明见：[架构边界](docs/ARCHITECTURE.md)、[开发指南](docs/DEVELOPMENT.md)、[验证](docs/VERIFY.md)。

## 开源许可

本项目使用 [Apache License 2.0](LICENSE)。
