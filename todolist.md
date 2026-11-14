# 阅有期 · 待办清单（三端 Agent 任务书）

> 版本：2026-11-14 · 依据当日代码实测与 `frontend/docs/API-INTEGRATION.md`
> 用法：三个 Agent 各自只读自己那一节 + §0 公共约束；任务编号用于提交信息（`feat(B-01): ...`）。
> 接口口径：**以 `project/spec/技术架构.md` §7 / §7.1 为准**，本文与它冲突时以技术架构为准，并回填本文。

---

## 0. 公共约束（三端都要遵守）

| 项 | 约束 |
| --- | --- |
| 权威文档 | `backend/AGENT.md`、`frontend/AGENT.md`、`client/AGENT.md`；冲突优先级：AGENT.md > 本文 > 其他文档 |
| 包名 | `com.zian.backend`（后端）；前端 `@/` 别名；客户端 `lib/core` 与 `lib/features` |
| 响应格式 | `{ code, message, data, traceId }`；分页 `{ list, total, page, size }`，`size` 上限 100 |
| ID | 雪花 19 位，**一律字符串**传输；禁止 `parseInt` |
| 写接口 | 必须带 `Idempotency-Key`；错误码 400/401/403/404/409/422/500 不得自造 |
| 删除 | 逻辑删除：`deleted` 写本行主键；业务事实（借阅/预约/宽限/工单）不删 |
| 权限 | 三道闸：认证 → `@PreAuthorize` → 对象级校验；他人资源返回 **404** 而非 403 |
| 拉黑 | 只拦借出/续借/预约/申请宽限；归还与申诉必须可用 |
| 配色 | 只走 token（`#FBFCF8/#334155/#64C2A6/#2A7159`）；薄荷绿禁作正文色 |
| 密钥 | 一律走环境变量或 `application-local.yml`（已 gitignore）；**任何密钥不入库、不进日志** |
| 提交 | 一次提交一件事；提交前跑 §5 对应验证命令；`main` 只接受 PR |

---

## 1. 后端 Agent（`project/backend`）

> 入口：`backend/AGENT.md`。启动：`$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q spring-boot:run`（8125，context-path `/api`）。

| 编号 | 任务 | 现状与证据 | 主要落点 | 验收 |
| --- | --- | --- | --- | --- |
| **B-01** | 书目检索与馆藏接口 | **完全没有 `BookController`**；`catalog` 上下文只有 PO/Mapper，客户端 C4 无接口可用 | `catalog/interfaces/BookController.java`、`catalog/application/BookQueryService.java` | `GET /books?q=&page=&size=`、`GET /books/{id}`、`GET /books/{id}/copies`、`GET /books/{id}/queue` 四端点可用；分页/排序走 `PageQuery`；单测覆盖空结果与分页上限 |
| **B-02** | 读者消息中心 | 投递已落 `notify_delivery`，但无读取/已读接口 | `notify/interfaces/ReaderNotifyController.java` | `GET /notifications?page=`（只返回本人）、`PUT /notifications/read`（支持单条/全部）；未读数进 `GET /me` 或 summary |
| **B-03** | 首页摘要 | `/me/summary` 缺失（客户端暂用 `/me`+`/borrows/self` 拼） | `borrow/interfaces/ReaderBorrowController.java` 或 `iam` | `{ borrowed, dueIn3Days, overdue, quota:{used,total}, returnedThisTerm, creditLevel }` 一次返回 |
| **B-04** | 渠道开关 | 表 `reader_channel_pref` 与 Mapper 已就绪，无接口 | `notify/interfaces/ReaderPreferenceController.java` | `GET/PUT /me/channels`；EMAIL 关闭后 `NotifyService.dispatch` 不再入队（补测试） |
| **B-05** | 免打扰 | 表 `reader_quiet_hours` 已就绪，无接口 | 同上 | `GET/PUT /me/quiet-hours`；窗口重叠/跨零点校验返回 422；`mode=FALLBACK` 时返回 `22:00–08:00` |
| **B-06** | FR-11 提醒偏好（整块） | 设计完成、表未建、接口为零 | 新 `notify_topic` / `notify_preference` / `notify_preference_log` + 6 接口 | 主题×频率×渠道落库；P0 不可关返回 422；`preference_snapshot` 写入 `notify_task` |
| **B-07** | 校历写接口 | `GET /admin/calendar` 只读且表为空 | `rule/interfaces/RuleController.java`、`AcademicCalendarMapper` | `POST/PUT/DELETE /admin/calendar`；学期/场景/起止校验；操作写 `audit_log`；前端「校历命中」不再是空表 |
| **B-08** | 宽限证据附件 | `ApplyRequest` 只有 `evidenceText`；`ApplicationView` 无证据字段；`/admin/graces` 的 `evidence` 恒为空数组 | `grace/`（表 `grace_evidence` + 上传接口）、`shared` 文件存储 | `POST /files`（≤5MB、png/jpg/pdf）；`POST /graces` 带 `evidenceIds[]`；管理端详情返回 `evidence[]` 含文件名/URL/大小 |
| **B-09** | 审计 traceId / before-after | `audit_log` 只有 `detail`、`ip`、`purpose`，无 `trace_id`、无前后值 | `audit/infrastructure/persistence/po/AuditLogPO.java`、写审计的切面/服务、`AdminQueryMapper` | 表加 `trace_id`、`before_json`、`after_json`；`TraceIdFilter` 的值落库；改规则/拉黑/借还三类操作有前后值；`/audit-logs/{id}` 返回；导出含新列 |
| **B-10** | 隐私授权与遗忘 | 未实现 | `iam/` 或新 `privacy` 上下文 | `GET/PUT /me/consents`、`POST /me/forget`（脱敏 + 保留借阅事实 + 写审计） |
| **B-11** | 刷新令牌 | 只有 `/auth/login`、`/auth/logout` | `iam/interfaces/AuthController.java`、`JwtService` | `POST /auth/refresh`（轮换 jti、旧 token 失效）；过期边界测试 |
| **B-12** | 角色权限调整 | 权限按种子固定，无接口 | `iam/interfaces/UserAdminController.java` | `GET /admin/roles`、`PUT /admin/roles/{code}/permissions`；仅 ADMIN；改完 `GET /me` 的 permissions 立即变化 |
| **B-13** | `sort` 与幂等落地 | `sort` 接收不生效；`Idempotency-Key` 只收不判 | `shared/common/PageQuery`、幂等拦截器 | 支持的 `sort` 字段白名单生效；同 key 24h 内重放返回首次结果，不再二次写库 |

**不要做**：不改 `schema.sql` 的既有表语义；不在 `domain` 引 Spring/MyBatis；不新增第二套 ORM；不为单页加专用端点。

---

## 2. 管理端 Agent（`project/frontend`）

> 入口：`frontend/AGENT.md`；接口现状见 `frontend/docs/API-INTEGRATION.md`。`pnpm dev` 代理到 8125。

| 编号 | 任务 | 现状与证据 | 主要落点 | 验收 |
| --- | --- | --- | --- | --- |
| **F-01** | 校历管理（A9 内） | 只有只读命中表；后端 B-07 未开 | `src/features/rule/pages/RulesPage.tsx` | 学期/场景/起止的增删改；冲突与跨学期校验提示；保存后列表即时刷新 |
| **F-02** | 角色权限页 | 现仅 A8 右侧面板，无保存到后端的接口（B-12 未开） | `src/features/iam/` | 角色下拉 + 权限勾选 + 保存；未接后端前按钮禁用并注明「依赖 B-12」 |
| **F-03** | 宽限证据展示 | `evidence` 恒空，前端按空态处理 | `src/features/grace/` | 有附件时列表显示文件名/大小/下载；无附件保持空态；仅放行 `http(s)` 或受控下载地址 |
| **F-04** | 审计详情增强 | 详情 `detail="{}"`、`traceId` 空，前端显示「—」 | `src/features/audit/` | 展示 `traceId`、`before`/`after` 差异；导出 CSV 含新列；旧数据缺字段仍优雅降级 |
| **F-05** | 组织级通知策略 | A7 仅模板与任务，无「默认值下发」 | `src/features/notify/` | 院系/角色维度设默认偏好与强制项；只改默认值，不覆盖读者 OFF；依赖 B-06 |
| **F-06** | 幂等与重试语义 | 现「照发但后端不去重」 | `src/lib/api/` | B-13 上线后：重试复用同一 key；失败提示区分 409/422 |
| **F-07** | 部署前清理 | 登录页预填 ADMIN001/LIB001 | `src/features/auth/pages/LoginPage.tsx` | 用 `import.meta.env.DEV` 包住预填；生产构建不含演示账号 |

**不要做**：不改 `components/ui/*`；不引第二套 UI 库；不开 dark mode；不端算业务金额/天数/SLA。

---

## 3. 读者端 Agent（`project/client`）

> 入口：`client/AGENT.md`。当前 5 个 feature 仍走 mock：`appeal`、`notifications`、`profile`、`auth`（含 mock 兜底）、`search`（无 data 层）。

| 编号 | 任务 | 现状与证据 | 主要落点 | 验收 |
| --- | --- | --- | --- | --- |
| **C-01** | 接真实后端 | `dio_*` 仅 borrow/grace/order/reservation；appeal、notifications、profile 只有 mock | `lib/features/*/data/`、`lib/core/network/endpoints.dart` | 五个 feature 全部 `dio_` 实现；`AppConfig.useMock` 默认 false；断网时错误态可读 |
| **C-02** | 检索与书目详情（C4） | `search` 只有 domain + presentation，无数据源 | `lib/features/search/` | 接 B-01 四端点；空结果/无馆藏/可预约三态齐备；300ms 防抖 |
| **C-03** | 消息中心与偏好（C6） | 页面在、数据来自 mock | `lib/features/notifications/`、`profile` | 接 B-02/B-04/B-05；已读、渠道开关、免打扰失败均回滚并提示 |
| **C-04** | 首页摘要（C1） | 现在用 `/me`+`/borrows/self` 聚合 | `lib/features/borrow/` | B-03 上线后切 `/me/summary`，聚合逻辑删除；数字与列表口径一致 |
| **C-05** | 申诉与个人中心 | appeal/profile 仍 mock | `lib/features/appeal/`、`profile/` | 申诉提交/列表/详情接真实接口；个人页状态徽标与 `GET /me` 一致 |
| **C-06** | 真机联调 | 未跑过真后端 | 全端 | C1→C2→C3、C5→C6→C7 两条链路在 Android 模拟器跑通；`flutter test` 全绿 |
| **C-07** | 隐私与偏好页 | 页面未建（FR-07/FR-11） | `lib/features/profile/` | 授权开关、遗忘申请、主题偏好；依赖 B-06/B-10 |

**不要做**：不在 UI 端算宽限/费用/SLA；不缓存状态机结果；不引第三方 UI 套件替换现有 token 体系。

---

## 4. 跨端联调（由提出方牵头）

| 编号 | 任务 | 出口条件 |
| --- | --- | --- |
| X-01 | 三端冒烟 | 后端 8125 + 管理端 5173 + 读者端模拟器；C1→C3、A5→A7 两条主链路无阻断 |
| X-02 | 压测 | 200 并发归还 + 100 并发催还；P95 < 800ms，无 5xx |
| X-03 | 文档回填 | 完成任一 `B-xx` 后同步 `技术架构.md` §7/§7.1 与 `API-INTEGRATION.md`，避免再次漂移 |
| X-04 | 换行统一（可选） | 加 `.gitattributes`（`* text=auto eol=lf`），消除 CRLF 警告 |

---

## 5. 验证命令（提交前必跑）

```powershell
# 后端
cd G:\workspace\ReadDue\project\backend
$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q test
$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q spring-boot:run   # 另开窗口
curl http://127.0.0.1:8125/api/v1/system/health

# 管理端
cd G:\workspace\ReadDue\project\frontend
pnpm typecheck; pnpm build

# 读者端
cd G:\workspace\ReadDue\project\client
flutter pub get; dart format --set-exit-if-changed lib test; flutter analyze; flutter test
```

---

## 6. 建议执行顺序

1. **第一批（打通读者主链路）**：B-01 → B-02 → B-03 → C-01 → C-02 → C-03 → C-06
2. **第二批（合规与可追溯）**：B-08 → B-09 → F-03 → F-04
3. **第三批（配置与偏好）**：B-07 → F-01 → B-04 → B-05 → B-06 → F-05 → C-07
4. **第四批（收口）**：B-10 → B-11 → B-12 → F-02 → B-13 → F-06 → X-01 → X-02

---

## 7. 已完成（勿重复实现）

- 认证：`POST /auth/login`、`POST /auth/logout`、`GET /me`
- 借还：`POST /borrows`、`POST /returns`、`GET /borrows/self`、`POST /borrows/{id}/renew`
- 预约：`POST /reservations`、`GET /reservations/self`、`DELETE /reservations/{id}`、管理端 promote/expire
- 宽限：`POST /graces`、`GET /graces/self`、`POST /admin/graces/{id}/approve|reject`
- 申诉：`POST /appeals`、`GET /appeals/self`、`POST /admin/appeals/{id}/ack|resolve|reject|compensate`
- 提醒：`GET /admin/templates`、`PUT /admin/templates/{id}`、`GET /admin/notify-tasks`、`POST /admin/notify-tasks/{id}/retry`、`POST /admin/remind/batch`
- 逾期/看板/规则/用户/审计/健康：`/admin/overdue(+tiers)`、`/admin/dashboard`、`/admin/circulation/today`、`/admin/rules`、`/admin/calendar`（只读）、`/admin/users`、`/admin/audit-logs`、`/system/health`
- 邮件通道：站内信 + SMTP（`notify_delivery` 有 `SENT` 记录），失败进重试队列、2 次后死信
