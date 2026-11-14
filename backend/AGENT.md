# AGENT.md · 后端工程初始化指南

> 给接手本目录的 AI 代理：你负责把「阅有期」后端从零初始化成可运行、可测试、可继续开发的骨架。
> 本文件是**约束**，不是建议。与文档冲突时，以 `project/spec/` 下的设计文档为准，并回来改本文件。

## 1. 先读什么（按顺序，别跳）

| 顺序 | 文件 | 只读这些节 |
| --- | --- | --- |
| 1 | `../requires/require_total.md` | §3 功能需求、§11 补充工程需求（尤其 §11.7 FR-11） |
| 2 | `../spec/技术架构.md` | §2 分层与九个限界上下文、§10 权限分级、§11 用户状态、§12 逻辑删除、§13 通知偏好、§14 课表免打扰 |
| 3 | `../spec/借阅与归还说明书.md` | §3 借出校验、§9 通知与免打扰、§11 数据留存、§12 状态机、§14 表与规则映射 |
| 4 | `../spec/schema.sql` | 全量 DDL 与种子数据（26 表 / 2 视图），**以它为准，不要自己改表名** |
| 5 | `../PRD.md` | §3 进度、§6 决策记录（D-1…D-7，含账号状态强制范围、课表来源） |
| 6 | `../../design/页面与接口分析.md` | §7 偏好接口，其余接口用于对齐前端 |

## 2. 不可协商的 12 条

1. **包根 `com.zian.backend`**（`com.zian` 是组织根，`backend` 是项目名），模块化单体，一个 Spring Boot 进程，不拆微服务。
2. **九个限界上下文**：`iam`、`catalog`、`borrow`、`reservation`、`grace`、`notify`、`appeal`、`rule`、`audit`。每个上下文内部四层：`interfaces` → `application` → `domain` ← `infrastructure`。
3. **禁止跨上下文直连 Mapper / 直接改别人的表**。跨上下文只走「应用服务接口」或「领域事件（outbox）」。
4. **默认一个事务一个聚合**。唯一例外：归还时 `BorrowOrder` + `BookCopy` 强一致（同一事务），其余用事件最终一致。
5. **领域层不依赖 Spring / MyBatis / Jackson**：`domain` 包里不出现 `@Service`、`@Mapper`、`@TableName`、`@Transactional`。
6. **逻辑删除**：`deleted BIGINT UNSIGNED DEFAULT 0`，删除时写本行主键；唯一索引一律带上 `deleted`。禁止 0/1 布尔。
7. **业务事实永不删除**：借阅单、预约、宽限、工单只走状态机，不物理删。`audit_log` 90 天后物理删除。
8. **权限三道闸**：认证（JWT）→ `@PreAuthorize`（权限码）→ 对象级校验（本人 / 本馆）。越权 403，他人资源 404。
9. **账号状态**：`NORMAL` / `BLACKLISTED`。拉黑只拦「借出、续借、预约、申请宽限」，归还与申诉必须可用。
10. **通知优先级**：P0（申诉结果 / 预约到书 / 到书首发）直投；账号状态类只强制站内信、仍走 P1 免打扰与合并；P1 可顺延；P2 只能 DAILY/WEEKLY。
11. **事务内不调用外部 IO**（邮件、HTTP）；用 outbox + 消费者重试。
12. **`@Transactional` 只出现在 `application` 层**，`domain` 与 `interfaces` 不写事务注解。

## 3. 技术栈（版本下限）

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Java | 21 (LTS) | 用 record / sealed / pattern matching，不用 preview |
| Spring Boot | 3.3.x | Web、Validation、Security、Actuator |
| MyBatis-Plus | 3.5.7+ | `mybatis-plus-spring-boot3-starter` |
| MySQL | 8.0 | 库名 `readdue`，字符集 `utf8mb4_0900_ai_ci` |
| Redis | 7.x | Lettuce，键前缀 `readdue:` |
| RabbitMQ | 3.13 | 事件消费、延迟队列用 TTL+DLX，不用插件 |
| 迁移 | Flyway | `V1__schema.sql` 直接用 `schema.sql`，后续增量 `V2__...` |
| 测试 | JUnit 5 + AssertJ + Testcontainers | MySQL/Redis/RabbitMQ 容器化 |
| 构建 | Maven 3.9 | 单模块 jar，`spring-boot-maven-plugin` |

## 4. 工程结构

```
backend/
├── pom.xml
├── AGENT.md
├── README.md
├── docker-compose.yml            # mysql/redis/rabbitmq，本地一键起
├── src/main/java/com/zian/backend/
│   ├── BackendApplication.java        # 已有脚手架
│   ├── bootstrap/                # 装配、全局异常、拦截器、SecurityConfig、MybatisConfig
│   ├── shared/                   # 只放跨上下文的技术原语：Result、PageQuery、TraceId、IdGen、Clock
│   ├── iam/{interfaces,application,domain,infrastructure}
│   ├── catalog/…
│   ├── borrow/…
│   ├── reservation/…
│   ├── grace/…
│   ├── notify/…
│   ├── appeal/…
│   ├── rule/…
│   └── audit/…
├── src/main/resources/
│   ├── application.yml           # 默认 local
│   ├── application-local.yml
│   ├── db/migration/V1__schema.sql
│   └── mapper/**/*.xml            # 复杂 SQL 才写 XML，简单查询用 Wrapper
└── src/test/java/com/zian/backend/…
```

> 现有脚手架已在 `com.zian.backend` 下建好 `common`（Result/PageQuery/错误码）、`config`、`constant`、`exception` 四个平台包，可直接复用；`bootstrap`、`shared` 是后续收敛目标，不要求一步到位。

**依赖方向**（用 ArchUnit 测试固化，不允许破例）：

```
interfaces ──> application ──> domain <── infrastructure
      └──────────────┘（只允许 interfaces 依赖 application 的 DTO/命令对象）
```

## 5. 分层职责（写代码前先对齐）

| 层 | 放什么 | 不放什么 |
| --- | --- | --- |
| `interfaces` | Controller、请求/响应 DTO、参数校验、权限注解 | 业务判断、事务、Mapper |
| `application` | 用例编排、事务边界、发事件、DTO 转换 | SQL、领域规则 |
| `domain` | 聚合根、实体、值对象、领域服务、仓储接口、领域异常 | 框架注解、数据库对象 |
| `infrastructure` | Mapper、PO、仓储实现、外部网关、消息收发 | 业务规则 |

**充血 / 贫血**（照 §2.4 执行）：`BorrowOrder`、`GraceApplication`、`AppealTicket`、`BookCopy`、`Reservation` 充血；`Book`、`RuleParam`、`OverdueTier`、`NotifyTemplate`、`AcademicCalendar`、`ReaderProfile`、`ReaderCredit` 贫血；DTO 一律贫血。

示例（归还的领域方法，不要写成 service 里的一堆 set）：

```java
public void returnCopy(Instant at, Money fine, ReturnPolicy policy) {
    if (status != BorrowStatus.ON_LOAN) throw new BorrowRejected("NOT_ON_LOAN");
    if (policy.requiresInspection() && !policy.inspected()) throw new BorrowRejected("INSPECTION_REQUIRED");
    this.returnedAt = at;
    this.fine = fine;
    this.status = fine.isZero() ? BorrowStatus.RETURNED : BorrowStatus.RETURNED_WITH_FINE;
    registerEvent(new BookReturned(id, copyId, at));
}
```

## 6. MyBatis-Plus 铁律

- `BaseMapper` 只在 `infrastructure` 出现，`domain` 只认仓储接口。
- 分页必须 `PaginationInnerInterceptor`：默认 20，上限 200；`size>200` 直接 400。
- 乐观锁：`OptimisticLockerInnerInterceptor`，冲突统一 `409 CONCURRENT_MODIFIED`。
- 逻辑删除：全局 `logic-delete-field: deleted`、`logic-delete-value: "id"`、`logic-not-delete-value: 0`。
- 自动填充：`MetaObjectHandler` 填 `created_at/updated_at`，有操作人的表填 `updated_by`，删除时填 `deleted_by`。
- 枚举：`@EnumValue` 存 code；JSON 字段用 `JacksonTypeHandler` + `autoResultMap = true`。
- 禁止 `select *` 进接口层；禁止 N+1（列表页要么 join，要么批量 `in`）。
- 批量写入每批 ≤ 500，用 `saveBatch` 或手写 `foreach`。
- **禁止 `IService` / `ServiceImpl` 出现在 Controller**；也禁止为了省事让 Controller 直接调 Mapper。

```yaml
mybatis-plus:
  global-config:
    db-config:
      id-type: assign_id
      logic-delete-field: deleted
      logic-delete-value: "id"
      logic-not-delete-value: 0
  configuration:
    map-underscore-to-camel-case: true
```

### 6.1 主键：雪花 ID（ASSIGN_ID）

- 全库主键 `BIGINT UNSIGNED`，**不用 `AUTO_INCREMENT`、不用 UUID**；MyBatis-Plus 全局 `id-type: assign_id`，实体不必逐个写 `@TableId(type = IdType.ASSIGN_ID)`。
- 雪花 id 是 19 位 `Long`，超过 JS `Number.MAX_SAFE_INTEGER`（2^53）。**所有对外 JSON 必须把 Long 序列化成字符串**：项目已有 `config/JsonConfig`，统一给 `Long.class` / `long.class` 注册 `ToStringSerializer`；前端 TS 里 id 一律声明 `string`。
- `workerId` / `datacenterId` 默认 0；多实例部署必须逐台注入且互不重复（`MYBATIS_PLUS_WORKER_ID=0..31`、`DATACENTER_ID=0..31`），或自定义 `IdentifierGenerator`（如 Redis 分配）。**禁止多台机器共用同一 workerId**，否则会产出重复 id。
- 时钟回拨：内置序列检测到回拨会抛异常；容器开启 NTP，运行期禁止改系统时间。测试要覆盖「回拨即报错、不产生重复 id」。
- 与逻辑删除配合：`deleted` 存本行主键（雪花 id），唯一索引写 `uk(业务键, deleted)`，同一业务键可被删除多次且互不冲突；`deleted = 0` 表示未删。
- 本期不做分库分表；雪花 id 单调递增、可按时间范围查，后续确实要拆表时也不用改主键策略。
- 验收：并发 1 万次插入无重复；接口返回的 `id` 是字符串；`ORDER BY id DESC` 与按创建时间倒序一致（同毫秒内允许少量乱序）。

若用 MyBatis-Plus 内置的 `GlobalConfig`（3.5.x 可用，官方更推荐自定义 `IdentifierGenerator`）：

```yaml
mybatis-plus:
  global-config:
    worker-id: ${MYBATIS_PLUS_WORKER_ID:0}
    datacenter-id: ${DATACENTER_ID:0}
```

### 6.2 数据访问：一律 MyBatis-Plus / MyBatis，禁止 JdbcTemplate

**主代码（`src/main`）里不得出现 `JdbcTemplate`、`NamedParameterJdbcTemplate` 或拼接 SQL 字符串。** 项目已有 `mybatis-plus-spring-boot3-starter`，所有读写都走它；grep 到即视为不合规。

按场景选型：

| 场景 | 写法 | 位置 |
| --- | --- | --- |
| 单表增删改查 | `BaseMapper` + `LambdaQueryWrapper` / `LambdaUpdateWrapper` | 业务上下文 `infrastructure/persistence` |
| 单表分页 | `selectPage(new Page<>(pageNo, size, false), wrapper)` | 同上 |
| 多表只读联查（管理端读模型） | 独立 `@Mapper` 接口 + `@Select`（动态条件用 `<script>`） | `reporting/infrastructure/persistence/mapper` |
| 角色权限等基础只读查询 | 独立 `@Mapper` 接口 + `@Select` | 对应上下文 `infrastructure/persistence/mapper` |
| 健康检查等元数据探针 | 独立 `@Mapper` 接口 + `@Select` | `bootstrap/web` |

硬规则：

- Mapper 由 MyBatis-Spring-Boot 自动配置注册：**启动类不加 `@MapperScan`**，新增接口只加 `@Mapper`。`@MapperScan(basePackages = "com.zian.backend")` 会把 `domain/repository` 里没有 SQL 的端口接口也注册成 Bean，和 `XxxRepositoryImpl` 撞成「同类型多个 Bean」。**不要**再建 `DataSource`/`JdbcTemplate` Bean。
- 读模型的端口与实现分开：`domain/repository/XxxReadRepository` 定义方法，`infrastructure/persistence/XxxReadRepositoryImpl`（`@Repository`）委托 `@Mapper`；不要让 `@Mapper` 接口直接 `extends` 端口，否则同一端口会出现两个 Bean。
- SQL 只允许出现在 `infrastructure` 或只读 `mapper` 接口里；`application`/`interfaces` 不得出现 SQL 片段。
- 动态 SQL 写在 `<script>` 中，`<`、`>` 必须转义为 `&lt;`、`&gt;`；字符串比较写 `status == 'PENDING'`（OGNL），不要写 `status == "PENDING"`。
- 统一用 `LIMIT #{limit} OFFSET #{offset}` 分页，禁止 `last("limit " + n)` 这类字符串拼接；`${}` 只允许接白名单常量（如本地重建脚本的固定表名），且必须来自代码内 `List.of(...)` 常量而非外部入参。
- 返回 `Map<String, Object>` 的读模型必须在服务层显式转换：MySQL 驱动可能把 `DATETIME` 给成 `LocalDateTime`，`TINYINT(1)` 给成 `Boolean`，`DECIMAL` 给成 `BigDecimal`；统一走 `reporting/application/AdminQueryService` 里的 `str/integer/truthy/decimal/date/dateTime/iso` 工具，不要假设单一类型。
- 跨上下文只读聚合集中在 `reporting`，禁止为了写 SQL 去注入别的上下文的 `XxxMapper`（分层与上下文边界仍然有效）。
- 事务：写操作在 `application` 上加 `@Transactional`；只读查询加 `@Transactional(readOnly = true)`。

```java
// 单表：LambdaQueryWrapper
BookCopyPO po = copyMapper.selectOne(Wrappers.<BookCopyPO>lambdaQuery()
        .eq(BookCopyPO::getBarcode, barcode)
        .eq(BookCopyPO::getDeleted, 0));

// 只读联表：@Mapper + <script>
@Select("""
        <script>
        SELECT o.id, o.effective_due_at, u.name AS reader_name
          FROM borrow_order o
          JOIN sys_user u ON u.id = o.reader_id
         WHERE o.status IN ('BORROWED','OVERDUE')
         <if test="keyword != null and keyword != ''">
           AND (u.name LIKE CONCAT('%', #{keyword}, '%'))
         </if>
         ORDER BY o.effective_due_at
         LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
List<Map<String, Object>> overdue(@Param("keyword") String keyword,
                                  @Param("limit") int limit,
                                  @Param("offset") long offset);
```

## 7. 接口规范

- 前缀 `/api/v1`，资源用复数，动作用子资源：`POST /borrow-orders/{id}/return`。
- 分页：`page`（从 1 起）、`size`（≤200）、`sort`；响应 `{items,total,page,size}`。
- 幂等：写接口支持 `Idempotency-Key`，24 小时内同键返回首次结果。
- 错误体：`{code,message,traceId,details?}`；HTTP 语义：400 参数、401 未认证、403 越权、404 不存在或他人资源、409 冲突/乐观锁、422 业务规则拒绝。
- 时间统一 `Instant` + ISO-8601（UTC），展示层再转本地时区。
- 金额用 `BigDecimal`，保留 2 位，`HALF_UP`。
- 所有响应带 `X-Trace-Id`，日志用 MDC 透传。

## 8. 认证与权限

- JWT 无状态；`sub=userId`，`role` 单值（READER / LIBRARIAN / ADMIN），`jti` 进 Redis 黑名单支持登出。
- 权限码形如 `borrow:return`、`user:blacklist`、`rule:update`，与 §10 权限矩阵一致；`@PreAuthorize("hasAuthority('borrow:return')")`。
- 对象级校验写在 `application`：读者只能读自己的单据（他人返回 404，不返回 403，避免探测）。
- 管理员约束：不能拉黑自己、不能删除最后一个管理员、不能改自己的角色。

## 9. 领域不变量（必须写成测试）

| 聚合 | 不变量 |
| --- | --- |
| `BorrowOrder` | 在借数 ≤ 额度；续借次数 ≤ `PolicyParam.maxRenew`；有预约队列时禁续借；`effective_due_at = due_at + grace_days` |
| `BookCopy` | 一个副本同时只能有一条在借单；`status` 只允许 `AVAILABLE/ON_LOAN/RESERVED/LOST/REPAIR` |
| `Reservation` | 同读者同书目最多 1 条有效预约；到书后 48 小时未取自动顺延 |
| `GraceApplication` | `D = min(Dmax, round0.5(Bs×Kt×Kq))`；同借阅 30 天内 ≤2 次；逾期后不再延长 |
| `AppealTicket` | SLA 2 小时；超时自动补偿 3 天并写 `appeal_event` |
| `NotifyTask` | `dedup_key` 唯一；P0 不可被偏好关闭；账号状态类强制 INBOX |

## 10. FR-11 通知与课表（新增，容易漏）

- 主题 × 频率 × 渠道 × 优先级四元组，落 `notify_topic` + `notify_preference`（只存差异）。
- 解析顺序：读者偏好 → 组织策略（只填默认）→ 硬约束（P0 不可关、P2 不能 REALTIME）。
- 免打扰双模式：`TIMETABLE`（学生自行导入课表）与 `FALLBACK`（工作日 22:00–08:00、周末 23:00–09:00）；课表未确认不生效。
- 课表解析置信度：≥0.90 默认勾选、0.75–0.90 需确认、**<0.75 直接丢弃**（记 `dropped_count`，不进确认页、不参与免打扰）；阈值读 `rule_param`。
- 来源只允许 `ICS/PDF/IMAGE/MANUAL`，没有教务直连。
- 到书追加提醒：最多 1 次（1/3/6/12h），必须早于保留到期前 6 小时，否则 `FOLLOW_UP_TOO_LATE`。
- 自动降噪：同主题按自然周累计 3 条未读降为 DAILY，可一键恢复。
- `SMS` 枚举保留但本期返回 `422 CHANNEL_NOT_AVAILABLE`。

## 11. 配置与环境变量

```
DB_URL / DB_USER / DB_PASSWORD
REDIS_HOST / REDIS_PORT / REDIS_PASSWORD
MQ_HOST / MQ_PORT / MQ_USER / MQ_PASSWORD
JWT_SECRET (≥32 字节) / JWT_TTL / JWT_REFRESH_TTL
MAIL_HOST / MAIL_PORT / MAIL_USER / MAIL_PASSWORD   # 可空，空则只发站内信
STORAGE_ENDPOINT / STORAGE_BUCKET / STORAGE_KEY / STORAGE_SECRET  # 课表原图，7 天过期
```

- 本地默认值写在 `application-local.yml`，生产强制走环境变量；启动时校验缺失即失败（`@ConfigurationProperties` + `@Validated`）。
- 日志脱敏：手机号、邮箱、身份证中间打码；不打印 JWT 与密码。

## 12. 初始化顺序（每步都要能跑通再下一步）

1. `mvn -q archetype` 或手写 `pom.xml`，跑通 `ReadDueApplication` 与 `/actuator/health`。
2. `docker-compose.yml` 起 MySQL/Redis/RabbitMQ；Flyway 导入 `schema.sql`，验证 26 表 + 2 视图。
3. `shared` 原语：`Result`、`ApiException`、`GlobalExceptionHandler`、`TraceIdFilter`、`PageQuery`。
4. `iam`：登录、JWT、`@PreAuthorize`、`GET /me`；用种子账号跑通 401/403/200。
5. `catalog` + `borrow`：借出、归还（双聚合同事务）、续借；写 3 个集成测试。
6. `reservation` + `grace` + `rule`：预约队列、宽限计算（`GraceCalculator` 纯函数单测）、参数快照。
7. `notify`：outbox 表、事件发布、站内信落库、邮件网关（可空实现）、免打扰路由、课表解析骨架。
8. `appeal` + `audit`：工单状态机、SLA 定时补偿、审计切面。
9. 全局：`@ControllerAdvice` 错误码对齐、OpenAPI（springdoc）输出到 `/v3/api-docs` 供前端生成类型。
10. ArchUnit：分层依赖、上下文隔离、`domain` 无框架注解，三条测试必须绿。
11. 可观测：Actuator + Micrometer，`/actuator/prometheus` 可抓。
12. 写 `README.md`：起服务、跑测试、导入数据、常见错误。

## 13. 测试要求

- 领域服务纯函数单测（`GraceCalculator`、`OverdueTierPolicy`、`RenewPolicy`），不启动 Spring。
- 聚合行为测试：不变量、状态迁移、领域事件是否登记。
- 集成测试：Testcontainers 起 MySQL，跑 Mapper + 事务 + 乐观锁冲突。
- 接口测试：`@SpringBootTest` + MockMvc，覆盖 200/400/401/403/404/409/422。
- 覆盖率门槛：`domain` ≥ 80%，整体 ≥ 60%；不达标不算完成。

## 14. 代码风格与提交

- Google Java Format（或 Spotless 默认配置），行宽 120；`final` 用在不可变字段与参数。
- 命名：`XxxRepository`（接口）、`XxxRepositoryImpl`、`XxxMapper`、`XxxPO`、`XxxService`（application）、`XxxController`。
- 异常：业务异常继承 `DomainException`，带 `code`；不要用 `RuntimeException` 兜底。
- **Javadoc 是交付物的一部分**（同事要靠它对接），规则如下：
  - 类、接口、record、枚举：必须有类级 Javadoc，一句话说明「它是什么、负责哪一段业务」；聚合再补一段不变量的说明。
  - `public` / `protected` 方法、仓储端口方法、Controller 端点：必须有 Javadoc；除了说明做什么，参数用 `@param`、返回值用 `@return`、抛出的业务异常用 `@throws`。
  - 领域方法要写清「前置状态 → 结果状态」和校验规则，例如「仅 BORROWED 可续借，成功则顺延 dueAt 并 renewCount+1」。
  - DTO / record 的字段用 `@param` 描述业务含义与单位（金额两位小数、时间为 +08:00 ISO 字符串）。
  - 注释位置：**Javadoc 在注解之上**（`/** … */` 紧接 `@Transactional` / `@PreAuthorize`），与声明之间不留空行。
  - 实现类的 `@Override`、Lombok 生成的 getter/setter、`main` 方法可以省略 Javadoc。
  - 注释只写「为什么 / 业务约束」，不写「给变量赋值」这类复述代码的句子。
- 分节注释统一用 `// region 名称` … `// endregion`，不要用 `// ---------- 名称 ----------` 划线风格。
- **Git 不属于后端 Agent 的职责**：不要执行 `git add/commit/push/branch/checkout/stash/reset` 等任何写操作，也不要改 `.gitignore`。版本管理由产品经理 Agent 负责；后端只改 `project/backend` 下的源码、配置与文档，需要提交时在回复里说明改了什么即可。

## 15. 完成定义（DoD）

- [ ] `mvn verify` 绿，ArchUnit 三条规则绿
- [ ] `docker compose up` 后本地能登录、能借、能还
- [ ] OpenAPI 可访问，前端能据此生成 client
- [ ] 至少 1 条 outbox 事件端到端跑通（借出 → 通知任务）
- [ ] README 里的命令，新人照抄可跑通
- [ ] 没有 TODO 遗留关键路径；有则写进 `docs/backlog.md` 并标注影响

## 16. 不要做

- 不要拆微服务、不要引注册中心/网关组件（Nginx 在部署层）。
- 不要用 `@Autowired` 字段注入，构造器注入。
- 不要在 `domain` 里用 Lombok 的 `@Data`（会破坏不变量），用 `@Getter` + 显式构造。
- 不要把 `deleted` 改成 `is_deleted`，不要用 0/1。
- 不要为「以后可能要用」提前抽象（多租户、多语言、插件化）。
- 不要在没有测试的情况下改 `schema.sql`；表结构改动一律新增 Flyway 迁移。




---

## 完成后回复

任务完成后，用一句简短的话回复人类：**「大鲸鲸做完了哦」**。
