# readdue-backend

阅有期 · 图书馆催还与借阅管理平台后端。模块化单体，按限界上下文组织，每个上下文内部遵循 DDD 四层。

## 技术栈

| 组件 | 版本 / 说明 |
| --- | --- |
| Java | 21（本机 `C:\JDK\jdk21`） |
| Spring Boot | 3.5.6（Spring 6.2.11） |
| 安全 | Spring Security 6 + JWT（jjwt 0.12.6）+ Redis 吊销 |
| 持久层 | MyBatis-Plus 3.5.14 + MySQL 8（库 `readdue`） |
| 数据库迁移 | Flyway（`classpath:db/migration`，已基线到 v2） |
| 消息 | RabbitMQ 3.13（topic/direct + TTL + DLX），spring-boot-starter-amqp |
| 缓存 / 会话 | Redis 7、Spring Session、Redisson 3.50.0 |
| 接口文档 | springdoc-openapi 2.8.13 |
| 观测 | Spring Boot Actuator（health/info/metrics） |
| 构建 | Maven 3.9.8（本地仓库 `E:\maven\apache-maven-3.9.8\mvn_repo`） |

## 启动

前置：MySQL `127.0.0.1:3306/readdue`、Redis `127.0.0.1:6379/0`、RabbitMQ `127.0.0.1:5672`（guest/guest）已启动。

```powershell
$env:JAVA_HOME = 'C:\JDK\jdk21'
cd G:\workspace\ReadDue\project\backend
mvn -B spring-boot:run
```

邮件（QQ SMTP）需要三个环境变量，**授权码不写进任何文件**；未设置时启动日志会提示「邮件通道未配置」，
EMAIL 投递记为 `SKIPPED / MAIL_DISABLED`，站内信不受影响：

```powershell
$env:MAIL_USERNAME = '你的完整QQ邮箱@qq.com'
$env:MAIL_PASSWORD = 'QQ邮箱授权码（不是登录密码）'
```

发件人 `app.mail.from` 默认取 `MAIL_USERNAME`（QQ 要求发件人与认证账号一致，否则拒信）。

- 服务地址：`http://127.0.0.1:8125/api`
- 健康检查：`GET http://127.0.0.1:8125/api/v1/system/health`（含 mysql / redis / 表数量）
- OpenAPI：`http://127.0.0.1:8125/api/v3/api-docs`
- Swagger UI：`http://127.0.0.1:8125/api/swagger-ui.html`

首次启动会自动执行 Flyway：已有库按 `baseline-on-migrate` 打基线到 v1，再应用 `V2__runtime_reference_data.sql` 与 `V3__borrow_and_reservation_uniqueness.sql`；空库则完整执行 v1 + v2 + v3。

V3 为并发兜底加了三组唯一索引（生成列在结束态为 `NULL`，历史数据不受影响）：同一副本同时只能有一条在借单、同一读者同一书目同时只能有一条在借单、同一读者同一书目同时只能有一条有效预约。

### 演示数据

`DemoDataInitializer` 在 `local` profile 且 `app.demo.enabled=true` 时运行，`sys_user` 非空即跳过。
演示数据默认**只在 local 开启**：`application.yml` 里 `app.demo.enabled=false`，`application-local.yml` 用
`${APP_DEMO_ENABLED:true}` 覆盖；生产环境不要打开。

演示账号（密码统一 `123456`）：

| 账号 | 密码 | 角色 |
| --- | --- | --- |
| `ADMIN001` | `123456` | ADMIN |
| `LIB001` | `123456` | LIBRARIAN |
| `2021001` | `123456` | READER（有逾期记录） |
| `2021002` / `2021003` / `2021004` | `123456` | READER |
| `2021005` | `123456` | READER（黑名单） |

重建演示数据（会清空业务表）：

```powershell
$env:APP_DEMO_RESET = 'true'; mvn -B spring-boot:run
# 启动成功后移除该环境变量，避免每次重启都清库
```

造数覆盖：6 本书 / 8 个副本、5 条借阅（含 2 条逾期、1 条今日到期）、3 条预约、3 条宽限（PENDING/APPROVED/REJECTED）、2 条申诉（OPEN/ACKED）、6 条提醒任务、审计日志若干，保证管理端各页面非空。

## 鉴权

- 登录：`POST /api/v1/auth/login`，请求体 `{"username":"ADMIN001","password":"123456"}`（`userNo` 为同义字段），返回 `token` / `expiresIn` / `user`。
- 请求头：`Authorization: Bearer <token>`；写接口带 `Idempotency-Key` 时按「用户 + 键」在 Redis 缓存 24h，重复请求回放首次响应（`IdempotencyFilter`）。
- 令牌无状态校验 + Redis 白名单 `readdue:auth:token:{jti}`（TTL 7200s，值必须是该用户的 id），`POST /auth/logout` 删除键即刻失效。
- 身份每请求回源：`JwtAuthenticationFilter` 只用 token 定位用户，角色与权限由 `AuthService.principalOf(id)` 从库里重算，**不信任 token 里的 `role/perms`**；拉黑、改角色、回收权限立即生效。
- 生产必设 `APP_JWT_SECRET`（≥ 32 字节）：非 `local` profile 下仍用内置默认密钥时应用**拒绝启动**（`IllegalStateException`）。
- 数据边界：读者只能为自己借书、只能归还自己的在借册（否则 `403 / 40300`）；`/v1/admin/**` 仅馆员及以上。
- 权限：方法级 `@PreAuthorize("hasAuthority('grace:approve')")`，`/me` 返回权限码并附带管理端别名（`workbench:read`、`overdue:read`、`notify:manage`、`user:manage`）。
- 统一响应：`{code, message, data, traceId}`，`code=0` 表示成功；错误码按 HTTP 语义映射（401/403/404/409/422/429/500）。响应头回写 `X-Trace-Id`。

## 接口一览

| 分类 | 方法 & 路径 | 权限 |
| --- | --- | --- |
| 登录 | `POST /v1/auth/login`、`GET /v1/me`、`POST /v1/auth/logout` | 公开 / 登录态 |
| 借还 | `POST /v1/borrows`、`POST /v1/returns` | `borrow:create` / `borrow:return` |
| 读者自助 | `GET /v1/borrows/self`、`POST /v1/borrows/{id}/renew` | `borrow:read:self` / `borrow:renew` |
| 读者预约 | `POST /v1/reservations`、`GET /v1/reservations/self`、`DELETE /v1/reservations/{id}` | `reservation:create` |
| 读者宽限 | `POST /v1/graces`、`GET /v1/graces/self` | `grace:apply` |
| 读者申诉 | `POST /v1/appeals`、`GET /v1/appeals/self` | `appeal:create` |
| 工作台 | `GET /v1/admin/dashboard` | `workbench:read` |
| 宽限 | `GET /v1/admin/graces`、`POST /v1/admin/graces/{id}/approve\|reject` | `grace:approve` |
| 申诉 | `GET /v1/admin/appeals`、`POST /v1/admin/appeals/{id}/ack\|resolve\|reject\|compensate` | `appeal:handle` |
| 预约 | `GET /v1/admin/reservations`、`POST /v1/admin/reservations/{id}/promote\|expire` | `reservation:manage` |
| 逾期 | `GET /v1/admin/overdue`、`POST /v1/admin/remind/batch` | `overdue:read` / `overdue:board` |
| 通知 | `GET /v1/admin/notify-tasks`、`POST /v1/admin/notify-tasks/{id}/retry`、`GET/PUT /v1/admin/templates[/{id}]` | `notify:manage` |
| 用户 | `GET /v1/admin/users`、`POST /v1/admin/users/{id}/blacklist\|restore` | `user:blacklist` |
| 规则 | `GET/PUT /v1/admin/rules`、`GET /v1/admin/tiers`、`GET /v1/admin/calendar` | `rule:update` |
| 审计 | `GET /v1/admin/audit-logs[/{id}]`、`GET /v1/admin/audit-logs/export`（CSV） | `audit:read` |
| 流通 | `GET /v1/admin/circulation/today` | `borrow:read:all` |

列表接口统一 `page` / `size` / `sort`（`size` 上限 200），返回 `{items,total,page,size}`；`total/page/size` 是 JSON 数字，其余 `Long` 字段（含主键）序列化为字符串。

管理端读接口的时间统一为带 `+08:00` 偏移的 ISO-8601 字符串（`AdminViews`/`NotifyDtos` 的时间字段是 `String`，由 `AdminQueryService.iso()` 生成），不使用 `OffsetDateTime` 直接序列化，避免被全局 `spring.jackson.time-zone=UTC` 转成 `Z`。

## 消息与事件

业务事务内只写 `event_outbox`，提交后由 `OutboxPublisher`（2s 轮询）投递到 RabbitMQ，发布确认后置 `PUBLISHED`，失败重试至 5 次后置 `DEAD`。

| Exchange | Routing Key | 队列 | 消费方 |
| --- | --- | --- | --- |
| `yyq.event`（topic） | `borrow.created` / `borrow.returned` / `borrow.renewed` / `overdue.marked` / `reservation.ready` | `q.notify.plan` | 提醒编排 |
| | `grace.approved` | `q.notify.replan` | 宽限后重排提醒 |
| | `borrow.returned` | `q.reservation.promote` | 预约兑现 |
| `yyq.delivery`（direct） | `channel.inbox` / `channel.email` | `q.delivery.inbox` / `q.delivery.email` | 站内信 / 邮件 |
| | `delivery.dead`（DLX 路由） | `q.delivery.dlq` | 投递死信 |

消费端手动 ack、按 `dedup_key` 幂等；邮件失败先投 `q.delivery.email.retry`（TTL 30s 回原队列），累计 2 次后进 `q.delivery.dlq`，不静默丢弃。邮件发送走 `MailGateway` 端口：配置了 `spring.mail.host` 才真发，否则记为 `SKIPPED/MAIL_DISABLED`。

## 目录结构

```text
com.zian.backend
├── BackendApplication.java          # @SpringBootApplication + @MapperScan(annotationClass = Mapper.class)
├── bootstrap                        # 启动与全局基础设施
│   ├── config                       # Cors / Json / MybatisPlus / RabbitTopology / Scheduling / MetaObjectHandler
│   ├── demo                         # DemoDataInitializer（local profile）
│   ├── security                     # SecurityConfig / JwtAuthenticationFilter / EntryPoint / AccessDeniedHandler
│   └── web                          # GlobalExceptionHandler / SystemController / TraceIdFilter
├── shared                           # 跨上下文共享能力
│   ├── common                       # BaseResponse / ErrorCode / PageQuery / PageResult / TraceContext / Times / ResultUtils
│   ├── constant                     # MqConstant
│   ├── dto                          # PersonView / BookRefView
│   ├── exception                    # BusinessException
│   ├── idempotency                  # IdempotencyFilter（写请求 24h 幂等回放）
│   ├── outbox                       # 本地消息表：OutboxEvent / Repository / OutboxAppender / OutboxPublisher
│   └── security                     # JwtService / LoginUser / UserContext
├── iam / catalog / borrow / reservation / grace / notify / appeal / rule / audit
└── reporting                        # CQRS 只读侧：管理端聚合查询（AdminReadRepository + @Select 联表）
```

`src/main` 内没有 `JdbcTemplate`：单表走 `BaseMapper` + `LambdaQueryWrapper`，跨表只读走 `@Mapper` + `@Select`（定义在 `reporting/domain/repository` 的端口上），演示种子数据也走 `DemoSeedMapper`。规范见 `AGENT.md` §6.2 与项目技能 `mybatis-plus`。

每个限界上下文固定四层：

```text
<context>
├── interfaces                       # Controller、请求/响应 DTO、参数校验、权限注解
├── application                      # 用例编排、@Transactional 边界、跨上下文调用
├── domain
│   ├── model                        # 聚合根、实体、值对象（禁止 Spring/MyBatis/Jackson）
│   └── repository                   # 仓储接口
└── infrastructure
    └── persistence
        ├── po                       # XxxPO，MyBatis-Plus 表映射
        └── mapper                   # XxxMapper extends BaseMapper<XxxPO>
```

依赖方向：`interfaces → application → domain ← infrastructure`。跨上下文只调用对方 application 接口，或通过 `event_outbox` 发领域事件；禁止跨上下文直连 Mapper。管理端只读聚合集中在 `reporting`，联表查询不会污染各上下文的写模型。

## 关键实现约定

- **数据访问**：禁止 `JdbcTemplate`。单表用 `BaseMapper` / `LambdaQueryWrapper` / `selectPage`；跨表只读用 `@Mapper` + `@Select`（端口在 `reporting/domain/repository`，实现放 `infrastructure`），动态条件用 `<script>` 且 `<` 转义为 `&lt;`，分页统一 `LIMIT #{limit} OFFSET #{offset}`；`${}` 只用于代码内白名单常量。
- **主键 / 逻辑删除**：雪花 `ASSIGN_ID`；`deleted=0` 未删除，删除写本行主键；`reader_profile` / `reader_credit` / `reader_quiet_hours` 用 `user_id` 作主键。
- **乐观锁**：`@Version` 冲突映射为 409；规则参数用手写 `lambdaUpdate`（旧 version 作 WHERE，新 version 作 SET），避免 MP 把同一字段同时用于 SET/WHERE。
- **JSON 列**（`event_outbox.payload` / `notify_task.payload` / `audit_log.detail`）只写字符串/数字/布尔，`java.time` 一律先 `toString()`，否则 `JacksonTypeHandler` 会抛 `InvalidDefinitionException`。
- **审计**：`AuditRecorder` 记录操作者、角色、用途、IP；`object_id` 无默认值，空值写 0。
- **自动填充**：`AuditMetaObjectHandler` 填 `created_at` / `updated_at` / `deleted=0`。
- **时间输出**：管理端读模型由 `AdminQueryService.iso()`、读者端由 `Times.iso()` 统一格式化成 `+08:00` 字符串，不直接序列化 `OffsetDateTime`/`LocalDateTime`。
- **幂等**：`IdempotencyFilter` 在安全链内、`JwtAuthenticationFilter` 之后生效，键为 `readdue:idem:{userId}:{key}`，只缓存 2xx。
- **邮件**：应用层依赖 `MailGateway` 端口（不直接依赖 `JavaMailSender`），`SmtpMailGateway` 在 infrastructure 实现；未配置 SMTP 时投递记 `SKIPPED/MAIL_DISABLED`，消费者据此 ack 而非重试。
- **业务规则**：宽限 `D = min(14, round0.5(Bs × Kt × Kq))` 由 `GraceCalculator` 纯函数实现；罚金按 `FINE.PER_DAY`（默认 0.50 元/天/册）计算并 `HALF_UP` 保留 2 位；拉黑只拦借出/续借/预约/申请宽限，归还与申诉不受影响；申诉 SLA 2 小时超时自动补偿 3 天。
- **静默窗口**：`QUIET.START` / `QUIET.END`（默认 22:00–08:00）内的提醒平移到窗口结束；预约到书（P0）直投不受影响。

## 测试

```powershell
$env:JAVA_HOME = 'C:\JDK\jdk21'
mvn -B test
```

当前 24 个测试全绿：

- `BackendApplicationTests`：上下文启动；
- `JsonConfigTest`：Long → 字符串；
- `MapperSmokeTest`：MapperScan 与 PO 表映射；
- `LogicDeleteTest`：雪花主键 + `deleted=id` 逻辑删除；
- `GraceCalculatorTest`：公式乘积、0.5 天步长四舍五入、Dmax 封顶、空值与非法步长；
- `LayeringRulesTest`：ArchUnit 四条守卫（domain 纯净、application 不依赖 infrastructure、interfaces 不依赖 infrastructure、application 不反向依赖 Controller）；
- `security/AuthenticationBoundaryTest`：匿名 401、跨角色 403、分页越界 400；
- `security/BlacklistBorrowGateTest`：黑名单读者借阅 422，归还仍放行；
- `security/RuleVersionConflictTest`：过期 `expectedVersion` → 409；
- `security/SecurityTestSupport`：真实登录 + MockMvc 基类。

## 前端联调

管理端在 `project/frontend`，开发代理指向 `127.0.0.1:8125`。切换真实后端：

```powershell
cd G:\workspace\ReadDue\project\frontend
# .env.development
# VITE_USE_MOCK=false
pnpm dev
```

前端写入接口自带 `Idempotency-Key` 与 `X-Trace-Id`；401 会自动清会话跳转登录页。

## 尚未实现

- 邮件通道：`spring.mail.host` 或 `spring.mail.username` 任一为空时，`SmtpMailGateway.available()` 为 false，投递记为 `SKIPPED/MAIL_DISABLED`；配置 `MAIL_USERNAME`、`MAIL_PASSWORD`（QQ 授权码）后即走真实 SMTP（`smtp.qq.com:465` + SSL，失败进 30s 重试队列，2 次后死信）；日志里的账号与收件人均脱敏；
- 课表 / 学术日历解析（`academic_calendar` 目前只提供查询与种子数据）；
- 校历写入、通知模板变量渲染（邮件正文当前为固定中文文案，不套 `notify_template`）；
- Testcontainers 集成测试（当前测试连本地 MySQL/Redis）。

安全审计报告与整改记录见仓库外层 `G:\workspace\ReadDue\temp\security-audit-report.md`。
