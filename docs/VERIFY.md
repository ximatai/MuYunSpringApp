# 验证

无需数据库的快速验证：

```bash
./gradlew test
npm ci --prefix app-web
npm run build --prefix app-web
```

Gradle 命令会验证 Demo 静态模型的表/字段契约、Service 能力组合和 Web 模块投影边界；前端命令会验证已发布 npm 包的类型与打包消费。它们都不需要启动 PostgreSQL，但前端要求 Node.js `>=22.23.0`。

运行态验证需要 PostgreSQL：

```bash
./scripts/dev-local.sh --web
```

验证未发布的前端平台包时，先建立 npm link，然后使用 `./scripts/dev-local.sh --web-linked`；它不会重装 `app-web` 依赖。

日志出现应用启动完成后即可确认运行态装配成功；后端为 `http://127.0.0.1:8081`，Todo 前端为 `http://127.0.0.1:5174`。后端根路径返回 `404` 属于预期行为，因为样板未声明根路径业务接口。平台会按 development 模式初始化 schema。

首次启动会用 `muyun.initial-admin.initial-password` 创建用户名为 `admin` 的初始化管理员。示例中的 `admin123` 只用于本地验证；共享或生产环境应通过 `MUYUN_INITIAL_ADMIN_INITIAL_PASSWORD` 注入强密码。该配置不会重置已存在管理员的密码。

验证完成后停止应用，数据库容器可按需要保留。仅在需要重置本地开发数据时执行 `docker compose down -v`：该命令会删除 PostgreSQL 数据卷，数据不可恢复。
