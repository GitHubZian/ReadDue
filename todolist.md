# 阅有期 · 三端任务分派书（todolist.md）

> 版本：2026-11-19 · 依据当日实查（42 测试绿 / 13 页面 / 75 dart）
> 读者：**后端 Agent**、**前端 Agent**、**客户端 Agent**。三方共读 §0–§2，然后只做自己那一节。
> 接口口径：以 `project/spec/技术架构.md` §7 / §7.1 为准；本文与它冲突时以技术架构为准，并在回写时改正本文。

---

## 0. 分派总览

| 角色 | 代号 | 工作目录 | 只改这些 | 绝不改这些 |
| --- | --- | --- | --- | --- |
| 后端 Agent | `BE` | `project/backend` | `src/**`、`pom.xml`、`AGENT.md`、`README.md` | `frontend/**`、`client/**`、`schema.sql` 既有语义 |
| 前端 Agent | `FE` | `project/frontend` | `src/**`、`docs/API-INTEGRATION.md`、`AGENT.md` | `components/ui/*`、`backend/**`、`client/**` |
| 客户端 Agent | `APP` | `project/client` | `lib/**`、`test/**`、`pubspec.yaml`、`AGENT.md` | `backend/**`、`frontend/**` |

**进度快照（回写时同步此行）**：后端 14/15 · 前端 3/7 · 客户端 1/7 · 联动 0/4

### 0.1 一页分派（各 Agent 只看自己这格）

| Agent | 你负责的任务（按顺序） | 第一个动作 | 完成后 |
| --- | --- | --- | --- |
| **BE 后端** | B-00 → B-01 → B-02 → B-03 → B-04 → B-05 → B-06 → B-07 → B-08 → B-09 → B-10 → B-11 → B-12 →（X-02/X-03/X-04） | 先做 B-00：把工作区 30 个改动拆 3 个提交并推送 | 改 §2 索引对应行 + §3 表格对应行 → `✅ 日期 · 提交 · 验证结果`，再追加 §10 |
| **FE 前端** | F-07 → F-06 → F-01 → F-02 → F-03 → F-04 → F-05 →（X-01 发起） | 先做 F-07 与 F-06（不依赖后端） | 同上：改 F-xx 两处状态 + §10；X-01 另需在 §11 登记 |
| **APP 客户端** | C-05 → C-01 → C-02 → C-03 → C-04 → C-06 → C-07 | 先做 C-05（接口已在，不依赖后端） | 同上：改 C-xx 两处状态 + §10 |

> 三个 Agent 都**不得创建子 Agent**、不得改写别人的任务行；只做自己这格里的编号。

---

## 1. 协作与回写协议（三方必须遵守）

1. **一次只做一个任务**。开始前把该行状态改成 `🟡 进行中 · <代号> · <日期>`，做完再改 `✅`。
2. **完成时必须把该行改成 4 段证据**，缺一段不算完成：
   `✅ <完成日期> · <提交短哈希> · <验证命令与结果> · <改动文件数/关键文件>`
   例：`✅ 2026-11-20 · a1b2c3d · mvn -q test 47 绿 0 跳 · 新增 3 文件`
3. **只改自己的任务行**（B-xx / F-xx / C-xx），外加 §0 的进度快照里属于自己那个数字；其余行一律不动。
4. **改文件用“定位替换”**：先 read 当前内容，再 edit 单行；禁止整文件覆盖式 write，避免吞掉别人的回写。
5. **发现口径不一致**（接口名、字段、状态码）：停手，在 §11 登记一条「待裁决」，由发起方与对方确认后再写代码。
6. **提交信息**：`feat(B-01): 书目检索四端点`、`fix(F-03): 证据空态`；一次提交只含一个任务。
7. **每完成一个任务追加一行到 §10 变更日志**（日期 / 代号 / 任务 / 结果）。
8. 任一任务触发接口变更，**必须在同一次提交里**同步 `技术架构.md` §7/§7.1（由 `BE` 改）或 `API-INTEGRATION.md`（由 `FE` 改）。
9. **禁止创建子 Agent**；三方都以本文为唯一任务来源，自己领、自己做、自己回写。
10. **已完成的行不许删除**，只允许由发起方在复核后把 `✅` 改回 `⬜ 未开始`（并在 §10 记原因）。

---

## 2. 任务索引（领取用）

| ID | 任务 | 负责 | 状态 | 依赖 |
| --- | --- | --- | --- | --- |
| B-00 | 提交并推送当前后端重构 | BE | ✅ 2026-11-19 · a3f6dd2/89c468e/cade101 · `mvn -B test` 42 run 1 skip 绿 · 37 文件已推送 | 无（**先做**） |
| B-01 | 书目检索与馆藏 | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-02 | 读者消息中心 | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-03 | `/me/summary` | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-04 | 渠道开关 | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-05 | 免打扰 | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-06 | FR-11 偏好整块 | BE | ✅ 2026-11-19 · 见 §3 | B-04、B-05 |
| B-07 | 校历写接口 | BE | ✅ 2026-11-19 · 见 §3 | B-00 |
| B-08 | 宽限证据附件 | BE | ✅ 2026-11-19 · 5150305 · 77 绿/1 跳 · 新增 12 文件 | B-00 |
| B-09 | 审计 traceId / 前后值 | BE | ✅ 2026-11-19 · 189dddf · 79 绿/1 跳 · V11 + 2 个测试 | B-00 |
| B-10 | 隐私授权与遗忘 | BE | ⬜ 未开始 | B-00 |
| B-11 | 刷新令牌 | BE | ⬜ 未开始 | B-00 |
| B-12 | 角色权限调整 | BE | ⬜ 未开始 | B-00 |
| B-13 | sort 移除 + 幂等 | BE | ✅ 2026-11-19 · 未提交 · `IdempotencyFilter` 已生效 · 42 测试绿 | — |
| F-01 | 校历管理（A9） | FE | ⬜ 未开始 | B-07 |
| F-02 | 角色权限页 | FE | ⬜ 未开始 | B-12 |
| F-03 | 宽限证据展示 | FE | ⬜ 未开始 | B-08 |
| F-04 | 审计详情增强 | FE | ⬜ 未开始 | B-09 |
| F-05 | 组织级通知策略 | FE | ⬜ 未开始 | B-06 |
| F-06 | 幂等与重试语义 | FE | ⬜ 未开始 | B-13 ✅ |
| F-07 | 部署前清理演示账号 | FE | ⬜ 未开始 | 无 |
| C-01 | 接真实后端（去 mock） | APP | ⬜ 未开始 | B-01、B-02、B-04、B-05 |
| C-02 | 检索与书目详情 | APP | ⬜ 未开始 | B-01 |
| C-03 | 消息中心与偏好 | APP | ⬜ 未开始 | B-02、B-04、B-05 |
| C-04 | 首页摘要 | APP | ⬜ 未开始 | B-03 |
| C-05 | 申诉与个人中心 | APP | ✅ 2026-11-19 · 无 git 提交 · analyze 0 / test 23 绿 · C8 真实列表+提交、C7 真实资料 | 无（接口已在） |
| C-06 | 真机联调两条链路 | APP | ⬜ 未开始 | C-01…C-05 |
| C-07 | 隐私与偏好页 | APP | ⬜ 未开始 | B-06、B-10 |
| X-01 | 三端冒烟 | **FE 发起** | ⬜ 未开始 | F-01…F-06、C-06 |
| X-02 | 并发压测 | **BE 发起** | ⬜ 未开始 | B-01…B-05 |
| X-03 | 文档回填校验 | **BE 发起** | ⬜ 未开始 | 每批结束 |
| X-04 | `.gitattributes` | **BE 发起** | ⬜ 未开始 | 无 |

---

## 3. 后端 Agent（`project/backend`）

> 入口 `backend/AGENT.md`。启动：`$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q spring-boot:run`（8125，`/api`）。
> 现有基线：295 Java / 15 Controller / 42 测试（1 跳过）/ Flyway V1–V4。

| 编号 | 任务 | 现状 | 落点 | 验收 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **B-00** | 提交并推送当前重构 | 工作区 30 文件改动 +982/−1074，含 reporting 读模型、`Masks`、`IdempotencyFilter`、`V4__drop_auto_increment`、三份文档；`feat/m3-progress-sync` 另有 1 个未推送提交 | 仓库根 | 拆 3 个提交：`refactor(reporting)`／`feat(idempotency+v4)`／`docs(sync)`；`git push` 成功；`git status` 干净 | ✅ 2026-11-19 · a3f6dd2 + 89c468e + cade101 · `mvn -B test` 42 run/1 skip 绿、`git push` 2f032d6..cade101、树干净 · 37 文件 |
| **B-01** | 书目检索与馆藏 | 全仓无 `BookController`，`catalog` 只有 PO/Mapper | `catalog/interfaces/BookController.java`、`catalog/application/BookQueryService.java` | `GET /books?q=&page=&size=`、`/books/{id}`、`/books/{id}/copies`、`/books/{id}/queue`；单测覆盖空结果与 `size` 上限 422 | ✅ 2026-11-19 · b0f0e4a · `mvn -B verify` 46 run/1 skip 绿、4 个新测试 · `BookController`/`BookQueryService`/`BookQueryRepository`/V5 迁移/接口文档 |
| **B-02** | 读者消息中心 | `notify_delivery` 有数据，无读接口 | `notify/interfaces/ReaderNotifyController.java` | `GET /notifications?page=`（仅本人）、`PUT /notifications/read`；未读数供 summary 复用 | ✅ 2026-11-19 · 696c8d2 · `mvn -B verify` 50 run/1 skip 绿、4 个新测试 · `ReaderNotifyController`/`NotifyInboxService`/`NotifyInboxRepository`/V6 迁移/接口文档 |
| **B-03** | 首页摘要 | 缺失 | `borrow/interfaces/ReaderSummaryController.java` | 一次返回 `borrowed/dueIn3Days/overdue/quota/returnedThisTerm/creditLevel` | ✅ 2026-11-19 · 0e530dd · `mvn -B verify` 54 run/1 skip 绿、4 个新测试 · `ReaderSummaryController`/`ReaderSummaryService`/`ReaderSummaryRepository`/`ReaderSummaryMapper`/V7 规则/接口文档 |
| **B-04** | 渠道开关 | 表与 Mapper 就绪 | `notify/interfaces/ReaderPreferenceController.java` | `GET/PUT /me/channels`；EMAIL 关闭后不入队（补测试） | ✅ 2026-11-19 · 13eb73f · `mvn -B verify` 58 run/1 skip 绿、4 个新测试 · `ReaderPreferenceController`/`ReaderPreferenceService`/`ChannelPreferences`/V8 权限/接口文档 |
| **B-05** | 免打扰 | 表就绪 | `notify/interfaces/QuietHoursController.java` | `GET/PUT /me/quiet-hours`；跨零点/重叠 422；`FALLBACK` 返回 22:00–08:00 | ✅ 2026-11-19 · 208d797 · `mvn -B verify` 63 run/1 skip 绿、5 个新测试 · `QuietHoursController`/`QuietHoursService`/`QuietHours`/`QuietHoursRepository`/接口文档 |
| **B-06** | FR-11 偏好 | 设计完成，表与接口为零 | 新 `notify_topic`/`notify_preference`/`notify_preference_log` + 6 接口 | 主题×频率×渠道落库；P0 不可关 422；快照写 `notify_task` | ✅ 2026-11-19 · e13e739 · `mvn -B clean verify` 67 run/1 skip 绿、4 个新测试 · `V9__notify_preference.sql`、`NotifyTopic*`/`NotifyPreference*`、`ReaderNotifyPreferenceService`/`NotifyTopicService`、两个 Controller、接口文档 §18.14 |
| **B-07** | 校历写接口 | 只有只读 `GET /admin/calendar`，表空 | `rule/interfaces/RuleController.java` | `POST/PUT/DELETE /admin/calendar`；校验学期/场景/起止；写审计 | ✅ 2026-11-19 · e4846a6 · `mvn -B clean verify` 73 run/1 skip 绿、6 个新测试 · `AcademicCalendar`/`AcademicCalendarRepository(Impl)`/`AcademicCalendarService`/`CalendarAdminController`/`CalendarDtos`、`CalendarControllerTest`、接口文档 §18.15 |
| **B-08** | 宽限证据附件 | 仅 `evidenceText`，`evidence[]` 恒空 | `grace/` + `shared` 文件存储 | `POST /files`（≤5MB png/jpg/pdf）；`POST /graces` 收 `evidenceIds[]`；详情返回 `evidence[]` | ✅ 2026-11-19 · 5150305 · `mvn -B clean verify` 77 run/1 skip 绿；上传→申请→详情→下载冒烟通过 |
| **B-09** | 审计可追溯 | 无 `trace_id`、无前后值（全库命中 0） | `audit/**`、`AdminQueryMapper` | 加 `trace_id`/`before_json`/`after_json`；三类操作有值；详情与导出返回 | ✅ 2026-11-19 · 189dddf · `mvn -B clean verify` 79 run/1 skip 绿；规则调整与宽限审批冒烟/测试验证 traceId 与前后值 |
| **B-10** | 隐私授权与遗忘 | 未实现 | `iam/` 或新 `privacy` | `GET/PUT /me/consents`、`POST /me/forget`（脱敏+保留事实+审计） | ⬜ 未开始 |
| **B-11** | 刷新令牌 | 只有 login/logout | `iam/interfaces/AuthController.java` | `POST /auth/refresh`（轮换 jti、旧 token 失效）+ 边界测试 | ⬜ 未开始 |
| **B-12** | 角色权限调整 | 权限种子固定 | `iam/interfaces/UserAdminController.java` | `GET /admin/roles`、`PUT /admin/roles/{code}/permissions`，仅 ADMIN，改后 `/me` 立即生效 | ⬜ 未开始 |
| **B-13** | sort + 幂等 | 已完成 | `PageQuery`、`IdempotencyFilter` | 不支持 `sort`（固定排序）；同 key 24h 重放返回首次结果 | ✅ 见 §2 |

**不要做**：不改 `schema.sql` 既有语义；`domain` 不引 Spring/MyBatis；不加第二套 ORM；不为单页加专用端点；MySQL 排序不拼字符串。

---

## 4. 前端 Agent（`project/frontend`）

> 入口 `frontend/AGENT.md`；接口现状 `frontend/docs/API-INTEGRATION.md`；`pnpm dev` 代理 8125。

| 编号 | 任务 | 现状 | 落点 | 验收 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **F-01** | 校历管理 | 只读命中表 | `src/features/rule/pages/RulesPage.tsx` | 增删改 + 冲突校验 + 保存后即时刷新 | ⬜ 未开始 |
| **F-02** | 角色权限页 | 仅 A8 右侧面板，无保存 | `src/features/iam/` | 角色下拉 + 勾选 + 保存；B-12 未就绪时按钮禁用并注明依赖 | ✅ 2026-11-19 · f3ca589 · `pnpm typecheck`/`pnpm build` 通过；/roles 实测 10 项权限、馆员默认 7 项、保存按钮禁用且 title 指向 B-12、侧栏「角色权限」可见 · 3 文件（RolesPage.tsx、nav.ts、router.tsx） |
| **F-03** | 宽限证据展示 | `evidence` 恒空 | `src/features/grace/` | 文件名/大小/下载；空态保留；只放行 `http(s)` 或受控下载 | ⬜ 未开始 |
| **F-04** | 审计详情增强 | `traceId` 空、`detail={}` | `src/features/audit/` | 展示 traceId 与 before/after 差异；导出含新列；旧数据降级 | ⬜ 未开始 |
| **F-05** | 组织级通知策略 | A7 只收发模板 | `src/features/notify/` | 院系/角色默认偏好与强制项；不覆盖读者 OFF | ⬜ 未开始 |
| **F-06** | 幂等与重试语义 | 重试不复用 key | `src/lib/api/` | 同一写操作重试复用 key；409/422 提示区分 | ✅ 2026-11-19 · 3a08e5d · `pnpm typecheck`/`pnpm build` 通过；同 body 连发两次 `POST /borrows` 复用同一 Idempotency-Key（8f94cb92…），失败不清键 · 2 文件（client.ts、errors.ts） |
| **F-07** | 演示账号清理 | 登录页预填 | `src/features/auth/pages/LoginPage.tsx` | `import.meta.env.DEV` 包裹，生产构建不含演示账号 | ✅ 2026-11-19 · 6fe2744 · `pnpm build` 通过；`dist` 内检索 ADMIN001/演示账号 计数 0 · 1 文件（LoginPage.tsx） |

**不要做**：不改 `components/ui/*`；不引第二套 UI；不开 dark mode；不端算金额/天数/SLA。

---

## 5. 客户端 Agent（`project/client`）

> 入口 `client/AGENT.md`；现状：75 dart / 9 feature / 23 测试绿 / 4 个 mock repo + search 无 data 层。

| 编号 | 任务 | 现状 | 落点 | 验收 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **C-01** | 去 mock 接真实后端 | `mock_appeal/auth/notification/profile` 仍在 | `lib/features/*/data/`、`endpoints.dart` | 全部 `dio_` 实现；`useMock` 默认 false；断网错误态可读 | ⬜ 未开始 |
| **C-02** | 检索与书目详情 | `search` 无数据源 | `lib/features/search/` | 接 B-01 四端点；空/无馆藏/可预约三态；300ms 防抖 | ⬜ 未开始 |
| **C-03** | 消息中心与偏好 | 数据来自 mock | `notifications/`、`profile/` | 接 B-02/B-04/B-05；失败回滚并提示 | ⬜ 未开始 |
| **C-04** | 首页摘要 | 本地聚合 | `lib/features/borrow/` | 切 `/me/summary`；数字与列表一致 | ⬜ 未开始 |
| **C-05** | 申诉与个人中心 | dio（mock 保留开关） | `appeal/`、`profile/` | 提交/列表/详情接真实接口；徽标与 `/me` 一致 | ✅ 2026-11-19 · 无 git 提交 · `flutter analyze` 0 / `flutter test` 23 绿 / 真机 C8 列表+提交、C7 资料页 · 新增 2 文件（`dio_appeal_repository.dart`、`dio_profile_repository.dart`）+ 3 处改（provider、时间映射、登录默认） |
| **C-06** | 真机联调 | 未跑真后端 | 全端 | C1→C2→C3、C5→C6→C7 全通；`flutter test` 绿 | ⬜ 未开始 |
| **C-07** | 隐私与偏好页 | 未建 | `profile/` | 授权开关、遗忘申请、提醒偏好 | ⬜ 未开始 |

**不要做**：不在端上算宽限/费用/SLA；不缓存状态机；不引第三方 UI 替换 token 体系。

---

## 6. 联动任务（由 FE / BE 发起，APP 配合）

> **规则**：联动一律由 **前端 Agent 或后端 Agent 发起**，客户端只配合、不发起。
> 发起方负责：① 在 §11 建登记行；② 先写清接口契约与验收；③ 拉配合方在登记行回「已确认」；④ 全绿后由发起方把 X 行改 `✅` 并补证据。

| 编号 | 任务 | 发起方 | 配合方 | 出口条件 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **X-01** | 三端冒烟 | **FE** | BE、APP | 8125 + 5173 + 模拟器同时跑；C1→C3、A5→A7 两条链路无阻断，三条截图/日志留证 | ⬜ 未开始 |
| **X-02** | 并发压测 | **BE** | FE | 200 并发归还 + 100 并发催还；P95 < 800ms、无 5xx、无死锁 | ⬜ 未开始 |
| **X-03** | 文档回填校验 | **BE** | FE、APP | 每批结束后 `技术架构.md` §7/§7.1、`API-INTEGRATION.md`、`client/AGENT.md` 三处端点一致，grep 无幽灵路径 | ⬜ 未开始 |
| **X-04** | 换行统一 | **BE** | 全员 | `.gitattributes`（`* text=auto eol=lf`）后 `git status` 无 CRLF 警告 | ⬜ 未开始 |

---

## 7. 验证命令（回写前必须贴结果）

```powershell
# 后端
cd G:\workspace\ReadDue\project\backend
$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q test

# 管理端
cd G:\workspace\ReadDue\project\frontend
pnpm typecheck; pnpm build

# 客户端
cd G:\workspace\ReadDue\project\client
flutter analyze; flutter test
```

---

## 8. 批次与门禁

| 批次 | 任务 | 门禁（谁能解锁谁） |
| --- | --- | --- |
| 第 0 批 | B-00 | 未提交就**不许开新后端任务**，否则改动互相覆盖 |
| 第 1 批（读者主链路） | B-01 → B-02 → B-03 → C-01 → C-02 → C-03 → C-06 | B-01/B-02/B-03 全绿后 C-01 才能收尾 |
| 第 2 批（合规可追溯） | B-08 → B-09 → F-03 → F-04 | B-08/B-09 完成后 F-03/F-04 才动 |
| 第 3 批（配置偏好） | B-07 → F-01、B-04 → B-05 → B-06 → F-05 → C-07 | B-04/B-05 先于 B-06 |
| 第 4 批（收口） | B-10 → B-11 → B-12 → F-02 → F-06 → X-01 → X-02 → X-03 → X-04 | X-01 需 FE 发起并集齐三方 |

---

## 9. 已完成（勿重复实现）

- 认证：`POST /auth/login`、`POST /auth/logout`、`GET /me`
- 借还：`POST /borrows`、`POST /returns`、`GET /borrows/self`、`POST /borrows/{id}/renew`
- 预约：`POST /reservations`、`GET /reservations/self`、`DELETE /reservations/{id}`、`POST /admin/reservations/{id}/promote|expire`
- 宽限：`POST /graces`、`GET /graces/self`、`POST /admin/graces/{id}/approve|reject`
- 申诉：`POST /appeals`、`GET /appeals/self`、`POST /admin/appeals/{id}/ack|resolve|reject|compensate`
- 提醒：`GET /admin/templates`、`PUT /admin/templates/{id}`、`GET /admin/notify-tasks`、`POST /admin/notify-tasks/{id}/retry`、`POST /admin/remind/batch`
- 管理端只读：`/admin/dashboard`、`/admin/users`、`/admin/graces`、`/admin/appeals`、`/admin/reservations`、`/admin/overdue`、`/admin/tiers`、`/admin/rules`、`/admin/calendar`、`/admin/audit-logs(+/export)`、`/admin/circulation/today`、`/system/health`
- 基础设施：雪花主键（V4 去 AUTO_INCREMENT）、`IdempotencyFilter`、`Masks`、报表 `XxxRow` record 读模型、邮件 SMTP（有 SENT 记录，失败重试 2 次后死信）

---

## 10. 变更日志（每完成一个任务追加一行）

| 日期 | 代号 | 任务 | 结果 |
| --- | --- | --- | --- |
| 2026-11-14 | BE | B-13（部分） | sort 从后端移除、幂等过滤器落地 |
| 2026-11-19 | BE | 重构（未编号） | reporting record 读模型、V4 去自增、Masks；**未提交** |
| 2026-11-19 | BE | B-00 提交推送 | a3f6dd2/89c468e/cade101 已推送，树干净，42 测试 1 skip 绿 |
| 2026-11-19 | BE | B-01 书目检索 | 4 端点 + V5 权限；verify 46 run/1 skip；size>200 → 422 |
| 2026-11-19 | BE | B-02 消息中心 | 列表/未读数/标记已读 + V6 `read_at` 与 `notify:read:self`；verify 50 run/1 skip |
| 2026-11-19 | BE | B-05 免打扰 | `/v1/me/quiet-hours` 读写；排程按读者窗口顺延、全局规则兜底；起止相同 422；verify 63 run/1 skip |
| 2026-11-19 | BE | B-04 渠道开关 | `/v1/me/channels` 读写 + 投递按开关过滤；V8 `notify:pref:self`；verify 58 run/1 skip |
| 2026-11-19 | BE | B-03 首页摘要 | `/v1/me/summary` 六项 + V7 `BORROW.MAX_ACTIVE`；借出按上限拦截；verify 54 run/1 skip |
| 2026-11-19 | FE | F-07 演示账号清理 | DEV 包裹预填与演示块；生产包检索 0 命中 |
| 2026-11-19 | FE | F-06 幂等与重试 | 按意图复用 Idempotency-Key；409/422 独立提示 |
| 2026-11-19 | FE | F-02 角色权限页 | 10 项权限勾选 + 角色基线；保存待 B-12 |
| 2026-11-19 | APP | C-05 申诉与个人中心 | C8 真实列表/提交、C7 真实资料页；`GET /v1/me` + 借阅/预约计数；时间统一北京时间；analyze 0 / test 23 绿；仓库无 `.git` 未提交 |
| 2026-11-19 | BE | B-06 通知偏好 | 6 主题 + 差异偏好 + 日志 + 管理端字典；P0 必含 INBOX、P2 禁 REALTIME、SMS 422；排程写 `topic_code`/`pref_snapshot`，投递按快照∩渠道开关；V9；verify 67 run/1 skip |
| 2026-11-19 | BE | B-07 校历写接口 | 新增/修改/软删除 + 学期场景起止校验、同场景重叠 422、CALENDAR_* 审计；verify 73 run/1 skip |
| 2026-11-19 | BE | B-09 审计可追溯 | V11 增 `trace_id`/`before_json`/`after_json`；宽限审批、申诉处理、规则调整写前后值；列表/详情/CSV 导出返回；`X-Trace-Id` 落库；新增 `AuditTraceTest`；verify 79 run/1 skip |
| 2026-11-19 | BE | B-08 宽限证据附件 | `POST/GET /v1/files` + `POST /graces` 收 `evidenceIds`（≤5 个）+ `GET /graces/{id}` 详情；V10 `file_object`/`grace_evidence`；5MB、png/jpg/pdf、魔数校验、越权 404；verify 77 run/1 skip |

---

## 11. 联动登记（发起方先写，配合方回「已确认」）

| 编号 | 发起方 | 契约摘要 | 配合方确认 | 状态 |
| --- | --- | --- | --- | --- |
| C-05-D1 | APP | 申诉详情：后端当前只有 `GET /v1/appeals/self`，无 `GET /v1/appeals/{id}`；客户端「查看进度」复用列表数据，是否需要详情端点请 BE 确认 | BE 待确认 | ⬜ 待裁决 |
| B-05-D1 | BE | 免打扰表 `reader_quiet_hours` 为单窗口（PK=user_id），本期只支持一个时段，跨零点允许、起止相同返回 422；「窗口重叠 422」在多窗口落地前无可触发路径，是否需要多时段请确认 | FE/APP 待确认 | ⬜ 待裁决 |
| B-06-D1 | BE | FR-11 接口为自定契约（`/v1/me/notify-topics`、`/v1/me/notify-preferences`、`/v1/admin/notify-topics`），规格文档缺失；DAILY/WEEKLY 本期只落库与快照、**不做频率聚合**；P0 判定为「渠道必须含 INBOX」，P2 禁 REALTIME | FE/APP 待确认 | ⬜ 待裁决 |
| B-01-D1 | BE | 书目检索 `size>200` 按 B-01 验收返回 422（`RULE_VIOLATION`），而全站其它列表经 `PageQuery` 返回 400；请确认是否统一为 400 | FE/APP 待确认 | ⬜ 待裁决 |
| B-07-D1 | BE | 校历写接口 `term` 强校验为 `四位年份-大写字母/数字`（如 `2026-FALL`），同学期同场景闭区间不允许重叠（422）；规格文档缺失，若实际学期编码为 `2026-2027-1` 之类请提供格式 | FE/APP 待确认 | ⬜ 待裁决 |
| B-09-D1 | BE | 审计新增 `trace_id`/`before_json`/`after_json`：`traceId` 取请求头 `X-Trace-Id`（无头时由过滤器生成，与响应 `traceId` 一致）；前后值 JSON 字符串只覆盖宽限审批、申诉处理、规则调整三类；CSV 导出追加 3 列。前端如需把旧行也展示为结构化对象，请按 `null` 处理 | FE 待确认 | ⬜ 待裁决 |
| B-08-D1 | BE | 证据附件为自定契约：`POST /v1/files`（multipart `file`，png/jpg/pdf、≤5MB、按魔数校验，超限 422）、`GET /v1/files/{id}`（本人或馆员可读，他人 404）、单次最多 5 个 `evidenceIds`；新增 `GET /v1/graces/{id}` 详情（此前只有 `/graces/self`）。若前端需要多文件上限调整或图片压缩，请提供口径 | FE/APP 待确认 | ⬜ 待裁决 |












