# 开发提示词：把 QQ 邮箱 SMTP 接入后端通知链路

> 用法：把本文件整段发给后端开发 agent（或人）。它自带背景、约束、改法、验收，不需要你再解释。

---

## 0. 你的任务

在 `G:\workspace\ReadDue\project\backend` 里，把「邮件投递」从**已编码但未配置**的状态打通到**真能发出信**：

- 用 **QQ 邮箱 SMTP**（`smtp.qq.com:465`，SSL）；
- 账号与授权码**只走环境变量**，任何情况下不得写入仓库、日志、测试资源、配置文件；
- 不改造现有分层与接口契约，只补配置 + 必要的启动自检 + 测试。

## 1. 先读，别急着改

1. `G:\workspace\ReadDue\project\backend\AGENT.md`（九上下文四层、事务边界、逻辑删除、MP 铁律、三道闸）。
2. `G:\workspace\ReadDue\project\spec\技术架构.md` §7（接口）、§13（通知）。
3. 现有实现，逐个读一遍再动手：
   - `src/main/java/com/zian/backend/notify/domain/port/MailGateway.java`
   - `src/main/java/com/zian/backend/notify/infrastructure/mail/SmtpMailGateway.java`
   - `src/main/java/com/zian/backend/notify/application/EmailDeliveryService.java`
   - `src/main/java/com/zian/backend/notify/infrastructure/messaging/EmailDeliveryListener.java`
   - `src/main/java/com/zian/backend/notify/infrastructure/messaging/RabbitEmailNotifier.java`
   - `src/main/java/com/zian/backend/notify/application/NotifyService.java`（`emailEnabled` 处）
   - `src/main/resources/application.yml`、`application-local.yml`

已确认的事实，不要重复调研：

- `pom.xml` 已有 `spring-boot-starter-mail`；
- `SmtpMailGateway` 用 `ObjectProvider<JavaMailSender>`，取不到 bean 时 `available()` 为 false；
- `EmailDeliveryService` 的落库分支：`SENT` / `SKIPPED+NO_EMAIL` / `SKIPPED+MAIL_DISABLED` / `FAILED+SEND_FAILED`；
- MQ：`yyq.delivery / channel.email` → `q.delivery.email`，失败进 `q.delivery.email.retry`，重试 2 次进死信；
- 库里 2 条 EMAIL 投递记录都是 `SKIPPED / MAIL_DISABLED / 未配置 SMTP`；
- `.gitignore` 已覆盖 `.env*`、`*-local.yml`；`application-local.yml` 当前未被 git 跟踪；
- 本机 `smtp.qq.com:465` 可连通；`JAVA_HOME` 必须是 `C:\JDK\jdk21`（默认的 jdk11 会报 `UnsupportedClassVersionError ... class file version 61.0`）。

## 2. 硬约束（违反即返工）

1. **密钥零入库**：`MAIL_USERNAME`、`MAIL_PASSWORD` 只以 `${MAIL_USERNAME}` / `${MAIL_PASSWORD}` 形式出现在 yml；不得出现明文授权码。
2. **不改 .gitignore 的语义**：`.env*`、`*-local.yml` 保持忽略；`application-local.yml` 继续不跟踪。
3. **只改 local 生效面**：SMTP 配置写在 `application-local.yml`；`application.yml` 最多留空占位或注释，不要塞任何凭据。
4. **不写日志**：不得打印 `password`、完整 `username`；失败日志只到 `to` 的域或掩码。
5. **不发信到第三方**：测试只发到 `MAIL_USERNAME` 自己的地址。
6. **不引入新依赖**（不需要 dotenv、不需要 mailgun SDK）。
7. 编译/测试必须用 JDK 21；提交前 `mvn -q test` 全绿。

## 3. 要改的东西

### 3.1 `application-local.yml`（新增，或在既有 app 段旁补）

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}
    default-encoding: UTF-8
    properties:
      mail.smtp.auth: true
      mail.smtp.ssl.enable: true
      mail.smtp.connectiontimeout: 5000
      mail.smtp.timeout: 5000
      mail.smtp.writetimeout: 5000

app:
  mail:
    from: ${MAIL_USERNAME:no-reply@readdue.local}
```

说明：`from` 必须等于发件账号，否则 QQ 会以「发件人与认证用户不一致」拒信；`465 + ssl.enable` 是 QQ 唯一稳定组合，不要用 25。

### 3.2 `application.yml`（可读性，不含凭据）

在 `app:` 段补一行注释说明「本地 SMTP 见 application-local.yml，凭据走环境变量」；不要再加别的。

### 3.3 启动自检（小改，可选但建议）

在 `notify` 启动路径上加一条一次性 INFO/WARN：`spring.mail.host` 为空时打印「邮件通道未配置，EMAIL 投递将记为 SKIPPED(MAIL_DISABLED)」。不要抛异常、不要阻断启动。

### 3.4 测试

- `MailGateway` 用 mock：覆盖 `available()==false` 时不发信且落 `SKIPPED/MAIL_DISABLED`；`send` 抛异常时落 `FAILED/SEND_FAILED` 并重抛。
- 真实 SMTP 的集成测试用条件启用，缺失 `MAIL_PASSWORD` 时跳过：

```java
@EnabledIfEnvironmentVariable(named = "MAIL_PASSWORD", matches = ".+")
```

- 断言点写清楚：`notify_delivery.status`、`error_code`、`attempt`。

### 3.5 文档（一行）

在 `backend/README.md` 的启动说明里加三条环境变量与启动命令（见 §4），不要写授权码。

## 4. 本机配置与启动（PowerShell）

现状：`MAIL_USERNAME=reader@example.com` 与 `MAIL_PASSWORD`（QQ 授权码，16 位）都已写入**当前 Windows 用户环境变量**，无需再设、也不要再问、更不要把它们抄进任何文件。新开的终端才继承；当前终端可用 `$env:MAIL_USERNAME` 验证（只打印用户名，不要打印密码）。

一次性写入用户环境变量（关掉终端重开才生效）：

```powershell
setx MAIL_USERNAME "你的完整QQ邮箱@qq.com"
setx MAIL_PASSWORD "你的QQ邮箱授权码"
```

当前会话临时生效（推荐调试时用）：

```powershell
$env:MAIL_USERNAME = '你的完整QQ邮箱@qq.com'
$env:MAIL_PASSWORD = '你的QQ邮箱授权码'
$env:JAVA_HOME     = 'C:\JDK\jdk21'
mvn -q spring-boot:run -Dspring-boot.run.profiles=local
```

QQ 邮箱侧前提：已在「设置 → 账户 → POP3/IMAP/SMTP 服务」开启服务并生成**授权码**（授权码不是登录密码，重新生成会让旧码立即失效）。

## 5. 验收（必须贴证据）

1. 启动成功，`GET http://127.0.0.1:8125/api/v1/system/health` 返回 `status=UP`。
2. 触发一次真实通知（管理端催还，或插入一条到期提醒），确认：

```sql
SELECT id, task_id, channel, status, attempt, error_code, error_msg, created_at
FROM notify_delivery WHERE channel='EMAIL' ORDER BY id DESC LIMIT 3;
```

期望：新记录 `status=SENT`、`error_code` 为空；自己的 QQ 邮箱收到信。

3. 失败路径：临时把 `MAIL_PASSWORD` 改错重启，确认落 `FAILED/SEND_FAILED` 且重试 2 次后进死信，再改回。
4. 密钥卫生：

```powershell
git status --porcelain
git log -p -1 | Select-String '授权码的任意前 6 位'
git check-ignore -v backend/src/main/resources/application-local.yml
```

期望：status 里没有任何 env/密钥文件；git log 无命中；local yml 被忽略。
5. `mvn -q test` 通过（现有 24 个测试不许退化）。

## 6. 汇报格式

四段：**改了什么**（文件+行）/ **怎么验证的**（命令与输出）/ **结果**（SENT 截图或 SQL 行）/ **未做与风险**（如 HTML 模板、SPF/DKIM、生产改用 DirectMail）。

## 7. 不要做

- 不要把授权码写进 `application.yml`、`application-local.yml`、测试类、README、注释、提交信息。
- 不要在 `SmtpMailGateway` 里硬编码 `smtp.qq.com`（保持 `spring.mail.*` 驱动，将来换服务商只改配置）。
- 不要为了「发出信」改 `EmailDeliveryService` 的落库语义或 MQ 重试策略。
- 不要给 Controller 加「发测试邮件」的公网端点；要自测就写测试类或本地临时脚本。
- 不要顺手改前端、读者端、数据库 schema。
