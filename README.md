# 阅有期 · 代码工程

高校图书馆借阅到期与归还小管家：读者端 Flutter、管理端 React、后端 Spring Boot 模块化单体。
需求与规则见 Obsidian `Projects/阅有期/`，接口契约以 [`后端的交互接口文档.md`](后端的交互接口文档.md) 为准。

## 目录

```
backend/    Spring Boot 3.5 + MyBatis-Plus + Flyway；包根 com.zian.backend
frontend/   管理端 React 18 + Vite + TanStack Query + Radix（pnpm）
client/     读者端 Flutter 3.24（Riverpod / go_router / dio / freezed）
```

后端 9 个业务上下文（iam/catalog/borrow/reservation/grace/notify/appeal/rule/audit）+ `reporting`、`shared`、`bootstrap`；
依赖方向 `interfaces → application → domain ← infrastructure`，归还时 `BorrowOrder` + `BookCopy` 单事务强一致，其余走 outbox。

## 本地启动

```powershell
# 后端（8125，context-path /api；需 JDK 21）
cd backend; $env:JAVA_HOME='C:\JDK\jdk21'; mvn spring-boot:run

# 管理端（5173，代理 /api → 8125）
cd frontend; pnpm install; pnpm dev

# 读者端（模拟器访问宿主机用 10.0.2.2）
cd client; flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8125/api
```

演示账号：管理员 `ADMIN001/123456`、馆员 `LIB001/123456`、读者 `2021001/123456`。
依赖：MySQL 8+（库 `readdue`）、Redis 7、RabbitMQ 3.13；连接串见 `backend/src/main/resources/application-local.yml`。

## 验证

```powershell
cd backend;  $env:JAVA_HOME='C:\JDK\jdk21'; mvn test        # 98 tests
cd frontend; pnpm typecheck; pnpm build                     # 13 业务页 / 14 路由
cd client;   flutter analyze; flutter test                  # 30 tests
```

## CI/CD 与部署

- `.github/workflows/ci.yml`：push/PR 跑三端——后端 `mvn verify`（自带 MySQL/Redis/RabbitMQ service）、管理端 `pnpm build`、客户端 `analyze + test`。
- `.github/workflows/release.yml`：打 `v*` tag 时产出 backend jar、frontend dist、release APK，并推送 `ghcr.io/<repo>/backend|frontend` 镜像、创建 GitHub Release。APK 的 API 地址由仓库变量 `APK_API_BASE_URL` 注入（默认 `http://10.0.2.2:8125/api`）。
- `deploy/docker-compose.yml`：MySQL + Redis + RabbitMQ + 后端 + 管理端一键起（`cp deploy/.env.example deploy/.env` 后 `docker compose --env-file deploy/.env up -d --build`），生产 profile 见 `backend/src/main/resources/application-prod.yml`；细节见 `deploy/README.md`。

## 进度与约定

- 任务与证据：`todolist.md`（唯一任务来源，完成一项回写四段证据）。
- 当前分支 `feat/m3-progress-sync`，尚未合入 `main`；X-01 三端冒烟待办（管理端/后端已验，客户端 APK 受离线 Gradle 阻塞）。
- 数据库迁移 V1–V14 由 Flyway 管理；新增表/列必须配迁移，不改历史脚本。
