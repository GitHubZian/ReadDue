# 部署（Docker Compose）

面向有 Docker 的服务器或局域网机器，一条命令拉起 MySQL、Redis、RabbitMQ、后端与管理端。

## 启动

```bash
cp .env.example .env
# 编辑 .env：至少设置 APP_JWT_SECRET（≥32 字节随机串）
docker compose --env-file .env up -d --build
```

- 管理端：http://<host>:8080
- 后端：http://<host>:8125/api
- 首次启动由 Flyway 自动建表并执行 V1–V14；`app.demo.enabled=false`，不会写入演示数据，需要账号时请自行插入或临时用 local profile 单独导入。

## 说明

- 管理端 Nginx 把 `/api/` 原样转发到 `backend:8125/api/`，前端无需改构建参数。
- 上传文件落在 `uploads` 卷；重建容器不丢。
- 邮件通道需要 `MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD`，QQ 邮箱用授权码而非登录密码。
- 升级：`git pull` 后 `docker compose --env-file .env up -d --build`；Flyway 只前进，不回滚。
- 仅本机调试可 `docker compose up mysql redis rabbitmq`，后端用 IDEA 或 `mvn spring-boot:run` 连 127.0.0.1。
