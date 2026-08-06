# 验证

无需数据库的快速验证：

```bash
./gradlew test
```

该命令会验证 Demo 静态模型的表/字段契约、Service 能力组合和 Web 模块投影边界。

运行态验证需要 PostgreSQL：

```bash
docker compose up -d
cp app-boot/src/main/resources/application-local.yml.example application-local.yml
./gradlew :app-boot:bootRun --args='--spring.profiles.active=local'
```

启动成功后访问 `http://127.0.0.1:8080`；平台会按 development 模式初始化 schema。验证完成后停止应用，数据库容器可按需要保留，或使用 `docker compose down -v` 删除。
