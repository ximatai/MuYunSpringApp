# 验证

无需数据库的快速验证：

```bash
./gradlew test
```

该命令会验证 Demo 静态模型的表/字段契约、Service 能力组合和 Web 模块投影边界；它不需要启动 PostgreSQL。

运行态验证需要 PostgreSQL：

```bash
docker compose up -d
cp app-boot/src/main/resources/application-local.yml.example application-local.yml
./gradlew :app-boot:bootRun --args='--spring.profiles.active=local'
```

日志出现应用启动完成后即可确认运行态装配成功；访问 `http://127.0.0.1:8080` 返回 `404` 属于预期行为，因为样板未声明根路径业务接口。平台会按 development 模式初始化 schema。

首次启动会用 `muyun.initial-admin.initial-password` 创建用户名为 `admin` 的初始化管理员。示例中的 `admin123` 只用于本地验证；共享或生产环境应通过 `MUYUN_INITIAL_ADMIN_INITIAL_PASSWORD` 注入强密码。该配置不会重置已存在管理员的密码。

验证完成后停止应用，数据库容器可按需要保留。仅在需要重置本地开发数据时执行 `docker compose down -v`：该命令会删除 PostgreSQL 数据卷，数据不可恢复。
