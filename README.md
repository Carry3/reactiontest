# Cognitive Reaction Test - Project Skeleton

该项目为认知反应测试（Cognitive Reaction Test）Spring Boot 初始骨架，包含实体、仓库、服务、控制器、DTO、配置与异常处理的示例文件。

如何运行（开发环境）

1. 修改 `src/main/resources/application.properties` 中的数据库连接配置（当前为 PostgreSQL 占位示例）。
2. 构建并运行：

```bash
mvn clean package
mvn spring-boot:run
```

说明
- 使用 Lombok 简化实体/DTO 的样板代码。IDE 中请启用 Lombok 支持。
- `SecurityConfig` 为开发时允许所有请求，部署前请根据需求调整。
