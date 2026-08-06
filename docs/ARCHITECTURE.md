# 架构边界

`MuYunSpringApp` 是独立业务应用：业务模块、应用配置和运行宿主都在本仓库内演进；平台能力通过稳定的 BOM 与 Starter 依赖接入。

Gradle 生产依赖方向（调用者 → 被依赖者）：

```text
app-boot → app-demo-web → app-demo → MuYunSpring BOM / Starter
```

- `app-boot`：唯一 Spring Boot 宿主，负责启动、配置和模块组合。
- `app-demo`：轻量业务领域示例；模型、DAO、Service 与平台能力组合留在这里，并保持领域层不含 Web 生产依赖。
- `app-demo-web`：将业务 Service 投射为标准 HTTP 接口，复用平台提供的 CRUD、权限、租户、审计与生命周期链路。

新增真实领域时，以 `app-<domain>` 与 `app-<domain>-web` 成对扩展。领域模块独立承载模型、DAO 和 Service；Web 模块依赖领域模块完成 HTTP 交付；`app-boot` 聚合可运行模块并保持为启动与配置入口。

Demo 的 `TodoItem` 展示最小静态链路：标准标题实体、`completed` 业务字段、`BaseDao`、组合软删与缓存能力的 Service，以及 `CrudWeb` 标准 Web 投影。
