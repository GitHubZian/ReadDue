# 部署（Docker Compose）

面向有 Docker 的服务器或局域网机器，一条命令拉起 MySQL、Redis、RabbitMQ、后端与管理端。

## 启动

```bash
cp .env.example .env
# 编辑 .env：
#   1) APP_JWT_SECRET 必须填：openssl rand -base64 48
#   2) DB_ROOT_PASSWORD / DB_PASSWORD / RABBITMQ_PASSWORD 换成强口令
docker compose --env-file .env up -d --build
```

- 管理端：http://<host>:8080
- 后端只在容器网络内监听 8125，由管理端 Nginx 的 `/api/` 反向代理；**不要**再把 8125 发布到宿主。
- 首次启动由 Flyway 自动建表并执行 V1–V20；`app.demo.enabled=false`，不会写入演示数据，需要账号时请自行插入或临时用 local profile 单独导入。

## 说明

- 管理端 Nginx 把 `/api/` 原样转发到 `backend:8125/api/`，前端无需改构建参数。
- 上传文件落在 `uploads` 卷；重建容器不丢。
- 邮件通道需要 `MAIL_HOST/MAIL_USERNAME/MAIL_PASSWORD`，QQ 邮箱用授权码而非登录密码。
- 升级：`git pull` 后 `docker compose --env-file .env up -d --build`；Flyway 只前进，不回滚。
- 仅本机调试可 `docker compose up mysql redis rabbitmq`，后端用 IDEA 或 `mvn spring-boot:run` 连 127.0.0.1。

## 上线安全清单

- **密钥**：`APP_JWT_SECRET` 留空时 compose 直接拒绝启动；非 local 环境用示例值或弱密钥会被后端启动校验拒绝。
- **口令**：DB 与 RabbitMQ 默认口令只用于本地；生产替换后同步更新 `.env`，不要提交 `.env`。
- **网络**：只发布 8080；3306/6379/5672/8125 都不对宿主发布。若需 TLS，在宿主机或前置反代（Caddy/Traefik/Nginx）终止，不要用本 compose 直接暴露 80。
- **响应头**：管理端 Nginx 已加 CSP、`X-Content-Type-Options`、`X-Frame-Options: DENY`、`Referrer-Policy`、`Permissions-Policy`，并关闭 `server_tokens`。
- **真实 IP**：后端 prod 已开 `server.forward-headers-strategy=framework`，审计日志依赖 Nginx 传入的 `X-Forwarded-For`；**不要**让客户端直连 8125。
- **权限**：后端容器以非 root 用户运行；数据库账号仅授予 `readdue` 库所需权限，不用 root 连接应用。
- **演示数据**：生产禁止 `app.demo.enabled=true`（启动会失败）；演示账号只存在于 local 种子。

## 排查：QQ 邮箱授权码失效

现象：`notify_delivery` 中 EMAIL 记录为 `FAILED / SEND_FAILED`，`error_msg` 含 `Authentication failed` / `535`，或邮件通道启动日志显示「邮件通道未配置」。

按「端口 → 发件人 → 授权码」顺序判断，不要先怀疑网络：

1. **端口**：QQ 邮箱用 `smtp.qq.com:465` + `mail.smtp.ssl.enable=true`（隐式 SSL）。587 是 STARTTLS，配置不同，不要混用。
2. **发件人**：`app.mail.from` 必须等于认证账号 `MAIL_USERNAME`，否则 QQ 以「发件人与认证用户不一致」拒信。
3. **授权码**：QQ 邮箱「设置 → 账户 → POP3/IMAP/SMTP 服务」中**重新生成授权码会立即使旧码失效**；改密、关闭再开启服务同样会使旧码失效。授权码不是登录密码。

处理与验证：

```bash
# 只在部署环境/进程环境变量中更新，禁止写入 .env 提交、application*.yml 或日志
# MAIL_USERNAME=完整QQ邮箱  MAIL_PASSWORD=新授权码
docker compose --env-file .env up -d backend   # 或重启本机后端进程
```

```sql
SELECT id, task_id, status, attempt, error_code, provider_msg_id, sent_at
FROM notify_delivery WHERE channel = 'EMAIL' ORDER BY id DESC LIMIT 3;
```

期望：新记录 `status=SENT`、`error_code` 为空、`provider_msg_id` 为 JavaMail 生成的 Message-ID。若仍失败，用同一账号在 QQ 网页端确认 SMTP 服务已开启，并重新生成授权码后再试；不要通过关闭 TLS 或改用 25 端口绕过。

