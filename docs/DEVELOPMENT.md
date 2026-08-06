# 开发指南

## 前置条件

- Java 21
- Docker Compose v2
- PostgreSQL（本地可通过 `compose.yaml` 启动）

## 新增业务模块

1. 在 `settings.gradle.kts` 增加 `app-<domain>` 与 `app-<domain>-web`：

   ```kotlin
   include("app-orders", "app-orders-web")
   ```

2. 在 `app-<domain>` 放置模型、DAO、Service、领域配置和测试；在 `app-<domain>-web` 添加 `api(project(":app-<domain>"))`，放置标准 Web 投影或独立 HTTP 契约。
3. 在 `app-boot/build.gradle.kts` 增加 `implementation(project(":app-<domain>-web"))`，让宿主组合该业务模块。
4. 静态 Service 优先组合 MuYunSpring Ability；动态对象使用平台元数据，并让两类对象复用同一套平台能力。

`muyunSpringVersion` 位于根目录 `gradle.properties`，是框架依赖版本的唯一来源。默认从 Maven Central 解析；开发尚未正式发布的框架版本时，可显式传入本地消费者仓库：

```bash
./gradlew test -PmuyunRepository=/path/to/muyun-consumer-repo
```

本地运行配置从示例文件复制，且不会提交：

```bash
cp app-boot/src/main/resources/application-local.yml.example application-local.yml
docker compose up -d
./gradlew :app-boot:bootRun --args='--spring.profiles.active=local'
```
