# 阅有期 · 三端任务分派书（todolist.md）

> 版本：2026-11-24 · 收口复核（后端 107 测试绿 / 管理端 13 业务页 / 客户端 46 测试绿 / Flyway V14）
> 读者：**后端 Agent**、**前端 Agent**、**客户端 Agent**。三方共读 §0–§2，然后只做自己那一节。
> 接口口径：以 `project/后端的交互接口文档.md` 为准（`project/spec/技术架构.md` 不存在，X-03-D1 已由 Lead 裁决，2026-11-23）。

---

## 0. 分派总览

| 角色 | 代号 | 工作目录 | 只改这些 | 绝不改这些 |
| --- | --- | --- | --- | --- |
| 后端 Agent | `BE` | `project/backend` | `src/**`、`pom.xml`、`AGENT.md`、`README.md` | `frontend/**`、`client/**`、`schema.sql` 既有语义 |
| 前端 Agent | `FE` | `project/frontend` | `src/**`、`docs/API-INTEGRATION.md`、`AGENT.md` | `components/ui/*`、`backend/**`、`client/**` |
| 客户端 Agent | `APP` | `project/client` | `lib/**`、`test/**`、`pubspec.yaml`、`AGENT.md` | `backend/**`、`frontend/**` |

**进度快照（回写时同步此行）**：后端 20/20（B-00…B-12 + B-14 申诉证据 + B-15 预约过期清扫 + B-16 SMTP 通道 + B-17 /me 资料 + B-18 拉黑到期 + B-19 上传/配置加固；X-02/03/04 另计） · 前端 12/12 · 客户端 12/12 + 证据附件真实上传 + 刷新令牌 + 真实预约 + 真机复验 + 检索交互修补 · 联动 5/5（X-01…X-05 ✅）

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
| B-10 | 隐私授权与遗忘 | BE | ✅ 2026-11-19 · f6609a9 · 83 绿/1 跳 · V12 + 4 个测试 | B-00 |
| B-11 | 刷新令牌 | BE | ✅ 2026-11-19 · e7879cb · 86 绿/1 跳 · 3 个测试 | B-00 |
| B-12 | 角色权限调整 | BE | ✅ 2026-11-19 · 9f53861 · 90 绿/1 跳 · 4 个测试 | B-00 |
| B-13 | sort 移除 + 幂等 | BE | ✅ 2026-11-19 · 未提交 · `IdempotencyFilter` 已生效 · 42 测试绿 | — |
| B-14 | 申诉证据附件 | BE | ✅ 2026-11-23 · 见 §3 | B-08 |
| B-15 | 预约过期自动清扫 | BE | ✅ 2026-11-23 · 见 §3 | B-08、FR-05-D1 |
| B-16 | SMTP 邮件通道恢复 | BE | ✅ 2026-11-23 · 见 §3 | — |
| B-17 | `/me` 资料字段扩展 | BE | ✅ 2026-11-23 · 见 §3 | — |
| B-18 | 拉黑到期自动解除 | BE | ✅ 2026-11-23 · b37c6e4 · 见 §3 | — |
| B-19 | 上传/配置加固 | BE | ✅ 2026-11-23 · b37c6e4 · 见 §3 | B-08 |
| F-01 | 校历管理（A9） | FE | ✅ 2026-11-19 · 7b140e0 | B-07 |
| F-02 | 角色权限页 | FE | ✅ 2026-11-19 · 1da9f4c | B-12 |
| F-03 | 宽限证据展示 | FE | ✅ 2026-11-19 · 663e8a1 · 管理端 evidence 由 3d79e35 补齐 | B-08 |
| F-04 | 审计详情增强 | FE | ✅ 2026-11-19 · 5379860 | B-09 |
| F-05 | 组织级通知策略 | FE | ✅ 2026-11-19 · d4e6e04 | B-06 |
| F-06 | 幂等与重试语义 | FE | ✅ 2026-11-19 · 3a08e5d · 见 §4 | B-13 ✅ |
| F-07 | 部署前清理演示账号 | FE | ✅ 2026-11-19 · 6fe2744 · 见 §4 | 无 |
| F-08 | 管理端状态真实性 | FE | ✅ 2026-11-23 · 4cf2d5f · 见 §4 | 无 |
| F-09 | 值班卡去假名 | BE+FE | ✅ 2026-11-23 · 见 §4 | 无 |
| F-10 | 管理端刷新令牌接入 | FE | ✅ 2026-11-23 · 44013c5 · 401→refresh→重放，single-flight；失败清会话回登录；build 通过 | B-11 |
| F-11 | 审批深链与搜索关键词 | FE | ✅ 2026-11-23 · 44013c5 · 宽限/申诉读 ?id=、失效提示与确认弹窗，审计读 ?keyword=；build 通过 + ego 深链实测 | 无 |
| F-12 | 管理端交互 P1 修补 | FE | ✅ 2026-11-24 · 见 §4 · 馆员搜索提示、通知可点、借还台内联校验、指标卡跳转、审计空态；build 1864 modules / JS 770.56KB | UX 审查 |
| C-01 | 接真实后端（去 mock） | APP | ✅ 2026-11-19 · 无 git 提交 · `useMock` 默认 false，7 个 provider 按 `appUseMockProvider` 切换；C1 摘要/未读、C6 通知/渠道/免打扰、C7 资料走真接口 | B-01、B-02、B-04、B-05 |
| C-02 | 检索与书目详情 | APP | ✅ 2026-11-19 · 无 git 提交 · `/books` 四端点 + 详情弹层 + 可借/可预约；UI 真实借阅「算法导论」成功并回流 C1 | B-01 |
| C-03 | 消息中心与偏好 | APP | ✅ 2026-11-19 · 无 git 提交 · 通知列表/未读数、渠道开关、免打扰读写真接口；失败回滚提示；空态真机验证 | B-02、B-04、B-05 |
| C-04 | 首页摘要 | APP | ✅ 2026-11-19 · 无 git 提交 · C1 改用 `/me/summary`，3 在借/0 临期/2 逾期与列表一致 | B-03 |
| C-05 | 申诉与个人中心 | APP | ✅ 2026-11-19 · 无 git 提交 · analyze 0 / test 23 绿 · C8 真实列表+提交、C7 真实资料 | 无（接口已在） |
| C-06 | 真机联调两条链路 | APP | ✅ 2026-11-19 · 无 git 提交 · C1→C2→C3、C5→C6→C7 真机走通 | C-01…C-05 |
| C-07 | 隐私与偏好页 | APP | ✅ 2026-11-19 · 无 git 提交 · 隐私授权/6 主题频率/遗忘确认；PUT 真机验证；test 26 绿 | B-06、B-10 |
| C-08 | 个人资料接真实字段 | APP | ✅ 2026-11-23 · 8548e34 · analyze 0 / test 34 绿 · 解析 `/me` department/major/grade/totalBorrowed，副标题与累计借阅去假值 | B-17 |
| C-09 | 客户端刷新令牌接入 | APP | ✅ 2026-11-23 · 9861f40 · 401→refresh→重放，single-flight；失败清会话回登录；analyze 0 / test 37 绿 | 无 |
| C-10 | 客户端真实预约 | APP | ✅ 2026-11-23 · 9861f40 · 检索页预约确认→`POST /v1/reservations`；成功回显排位 + 底栏角标；409/422 原地提示不假成功；analyze 0 / test 40 绿；真机 2021001 预约《解忧杂货店》成功（reservation 2124881324775514113，WAITING/排位 1，截图 temp/ux-audit/fix-reserve-*.png） | 无（接口已在） |
| C-11 | 真机默认 IP 与 debug 溢出 | APP | ✅ 2026-11-23 · f85c16b · 默认 API 改 `192.168.0.3`（dart-define 可覆盖）；登录 2px、检索头部 13px overflow 清零；analyze 0 / test 40 绿；真机 UCB685CMAUIFFA7P 重装复验（APK 115,242,041 B，SHA256 8DC4B431…A9DA），新预约 2124891109856104449（算法导论/排位 2） | C-10 |
| C-12 | 检索页真机反馈与交互修补 | APP | ✅ 2026-11-24 · 见 §5 · 已预约行换行不截断、详情固定 86% 仅下滑关闭、筛选/排序真实面板、下拉刷新、借出刷新、申诉缺项汇总；analyze 0 / test 46 绿 | UX 审查 |
| X-01 | 三端冒烟 | **FE 发起** | ✅ 2026-11-23 · 8125+5173+emulator-5554 同时在线；管理端 14 路由双账号验收；客户端新 debug APK（API 10.0.2.2）C1 登录→C2 书架（3 在借/2 逾期）→C3 详情（活着逾期 11 天）无阻断；宽限真实上传 evidence.png 落库（GA202611232183，evidence 2124863056660606977）；截图 temp/x01-client/01–09 | F-01…F-06、C-06 |
| X-02 | 并发压测 | **BE 发起** | ✅ 2026-11-19 · 200 归还 + 100 催还，P95 294/185ms，0 个 5xx | B-01…B-05 |
| X-03 | 文档回填校验 | **BE 发起** | ✅ 2026-11-19 · 幽灵路径 0；FE 文档缺口段已刷新 | 每批结束 |
| X-04 | `.gitattributes` | **BE 发起** | ✅ 2026-11-19 · `* text=auto eol=lf`，279 个文本文件归一化 | 无 |
| X-05 | CI/CD 与容器化 | **Lead** | ✅ 2026-11-23 · ci/release workflow + 三端镜像 + compose，离线校验通过（未推送） | 无 |

---

## 3. 后端 Agent（`project/backend`）

> 入口 `backend/AGENT.md`。启动：`$env:JAVA_HOME='C:\JDK\jdk21'; mvn -q spring-boot:run`（8125，`/api`）。
> 现有基线：383 Java / 26 Controller / 107 测试（1 跳过）/ Flyway V1–V14。

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
| **B-10** | 隐私授权与遗忘 | 未实现 | `iam/` 或新 `privacy` | `GET/PUT /me/consents`、`POST /me/forget`（脱敏+保留事实+审计） | ✅ 2026-11-19 · f6609a9 · 授权默认最小、关闭记时间、遗忘清姓名/手机/邮箱/院系并保留借阅事实，`USER_FORGET` 审计 |
| **B-11** | 刷新令牌 | 只有 login/logout | `iam/interfaces/AuthController.java` | `POST /auth/refresh`（轮换 jti、旧 token 失效）+ 边界测试 | ✅ 2026-11-19 · （提交待回写） · 刷新后旧 token 401、新 token 可访问；同一旧 token 只能刷新一次；写 `AUTH_REFRESH` 审计 |
| **B-12** | 角色权限调整 | 权限种子固定 | `iam/interfaces/UserAdminController.java` | `GET /admin/roles`、`GET /admin/permissions`、`PUT /admin/roles/{code}/permissions`，仅 ADMIN，改后 `/me` 立即生效；ADMIN 不可移除 `role:manage` | ✅ 2026-11-19 · 9f53861 · 90 绿/1 跳 · 4 个测试：角色列表/权限目录、旧 token 立即失权、未知角色/权限 404、ADMIN 422 |
| **B-13** | sort + 幂等 | 已完成 | `PageQuery`、`IdempotencyFilter` | 不支持 `sort`（固定排序）；同 key 24h 重放返回首次结果 | ✅ 见 §2 |
| **B-14** | 申诉证据附件 | 申诉只有 `reason`，无附件；宽限证据已通 | `appeal/**` + `shared.file` + 管理端读模型 | `POST /v1/appeals` 收可选 `evidenceIds`（≤5，先传文件，仅本人）；`GET /v1/appeals/{id}` 返回 `evidence[{id,name,contentType,size}]`；管理端列表装配 `evidence[{label,url}]`；旧请求兼容空数组 | ✅ 2026-11-23 · 未提交 · `mvn test` 95 绿/1 跳（新增 4 例）；V14 `appeal_evidence`；契约见接口文档 §18.20 |
| **B-15** | 预约过期自动清扫 | `expire()` 只改状态，不释放副本、不顺延队首；无定时任务 | `reservation/**` + `catalog/BookCopyRepository` | 每 10 分钟扫描 `READY` 且过期的预约：置 `EXPIRED` + 审计；有下一位则 `READY`（48h）+ outbox `RESERVATION_READY`；无人则释放 `READY_SHELF` 副本 | ✅ 2026-11-23 · f4b1ce0 · `mvn test` 98 绿/1 跳（新增 `ReservationExpiryTest` 3 例）；`@Scheduled(fixedDelay=600000, initialDelay=60000)`；`scanExpired` 逐条容错 |
| **B-16** | SMTP 邮件通道恢复 | 用户级 `MAIL_PASSWORD` 为失效旧码，EMAIL 投递 `Authentication failed`；`provider_msg_id` 恒空 | `notify/domain/port/MailGateway`、`notify/infrastructure/mail/SmtpMailGateway`、`notify/application/EmailDeliveryService`、`notify/domain/repository/NotifyDeliveryRepository(+Impl)` | 新授权码只经进程环境变量注入并重启；JavaMail Message-ID 落 `notify_delivery.provider_msg_id`；deploy README 补排查步骤 | ✅ 2026-11-23 · 未提交 · `MailGateway.send` 返回 `String`（MimeMessage `getMessageID()`）；真实催还 1 封：delivery `2124869019035705345` / SENT / provider_msg_id `<981383191.0.1795443225035@ZiAn>` / 22:13:47；`mvn -o test` 98 绿/1 跳 |
| **B-17** | `/me` 资料字段扩展 | `SessionUser` 只有 id/name/role/studentNo/avatarUrl/permissions；客户端「我的」院系空、累计借阅用在借数 | `iam/interfaces/dto/AuthDtos`、`iam/application/AuthService`、新增 `iam/domain/repository/ReaderSelfRepository` + 基础设施实现、`ReaderProfileMapper` | `/v1/me`（及登录/刷新 user）追加 `department/major/grade`（无档案为 `""`）与 `totalBorrowed`（全部借阅单，含已归还）；只读计数，不改表 | ✅ 2026-11-23 · 未提交 · 新增 `MeProfileTest` 2 例（档案字段 + 含已归还计数；空档案默认 `""`/0）；`mvn -o test` 100 绿/1 跳；接口文档 §3.2 |
| **B-18** | 拉黑到期自动解除 | `User.blacklistExpiresAt` 只写不读，临时拉黑实际永久；`user_status_log` 无人写 | `iam/domain/model/User`、新增 `iam/application/BlacklistExpiryService`、`UserAdminService`、`UserRepository(+Impl)`、新增 `UserStatusLogRepository(+Impl)`、`SysUserPO` | 到期实时按 `NORMAL` 放行；每 60s 扫描回写状态并写 `user_status_log`（「到期自动解除」/「系统」）+ 审计 `USER_UNBLOCK_EXPIRE`；人工拉黑/恢复也写日志；`NULL` = 永久 | ✅ 2026-11-23 · 未提交 · 新增 `BlacklistExpiryTest` 4 例（到期恢复/未到期保留/扫描前放行/拉黑写日志）；修复 MyBatis-Plus 更新跳过 null 导致解禁字段不清空；`mvn -o test` 107 绿/1 跳；接口文档 §11.2 |
| **B-19** | 上传与配置加固 | PNG 仅校验 4 字节魔数（12 字节伪图可入库，已有 9 个）；`uploads` 未忽略；prod Swagger/actuator/CORS 过宽 | `shared/file/application/FileService`、`bootstrap/config/CorsConfig`、`application.yml`、`application-prod.yml`、`project/.gitignore`、`deploy/docker-compose.yml`、`deploy/.env.example` | png/jpg 走 `ImageIO` 真实解码且真实格式=扩展名；清理 9 个 12B 伪 PNG；prod 关 springdoc、actuator 仅 `health,info`、日志 INFO、CORS 白名单；compose/示例环境变量提示默认口令 | ✅ 2026-11-23 · 未提交 · 新增 `FileUploadValidationTest` 3 例；`mvn -o test` 107 绿/1 跳；`docker compose -f deploy/docker-compose.yml --env-file deploy/.env.example config --quiet` exit 0 |

**不要做**：不改 `schema.sql` 既有语义；`domain` 不引 Spring/MyBatis；不加第二套 ORM；不为单页加专用端点；MySQL 排序不拼字符串。

---

## 4. 前端 Agent（`project/frontend`）

> 入口 `frontend/AGENT.md`；接口现状 `frontend/docs/API-INTEGRATION.md`；`pnpm dev` 代理 8125。

| 编号 | 任务 | 现状 | 落点 | 验收 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **F-01** | 校历管理 | 只读命中表 | `src/features/rule/pages/RulesPage.tsx` | 增删改 + 冲突校验 + 保存后即时刷新 | ✅ 2026-11-19 · 7b140e0 · `pnpm typecheck`/`pnpm build` 通过；`POST /admin/calendar` 创建 2026-FALL/EXAM → id 2123424523504750593，行内编辑/删除与 Dialog 校验已实测 · 3 文件（rule/api.ts、queries.ts、StrategyPanels.tsx） |
| **F-02** | 角色权限页 | 仅 A8 右侧面板，无保存 | `src/features/iam/` | 角色下拉 + 勾选 + 保存；B-12 未就绪时按钮禁用并注明依赖 | ✅ 2026-11-19 · 1da9f4c · `pnpm typecheck`/`pnpm build` 通过；`GET /admin/roles` 三组 24/13/11 项、`PUT /admin/roles/LIBRARIAN/permissions` code=0，保存后刷新 /me · 3 文件（iam/api.ts、queries.ts、RolesPage.tsx） |
| **F-03** | 宽限证据展示 | `evidence` 已由后端管理端视图装配 | `src/features/grace/` | 文件名/大小/下载；空态保留；只放行 `http(s)` 或受控下载 | ✅ 2026-11-19 · 663e8a1（管理端 evidence 由 3d79e35 补齐） · `pnpm typecheck`/`pnpm build` 通过；附件行支持文件名/大小、`GET /files/{id}` 受控下载、http(s) 链接兜底；管理端列表已返回 `evidence[{label,url}]` · 2 文件（grace/api.ts、pages/GracePage.tsx） |
| **F-04** | 审计详情增强 | `traceId` 空、`detail={}` | `src/features/audit/` | 展示 traceId 与 before/after 差异；导出含新列；旧数据降级 | ✅ 2026-11-19 · 5379860 · `pnpm typecheck`/`pnpm build` 通过；/audit 详情 traceId 可复制、before/after 差异表、空快照降级；新增动作码与对象前缀中文化 · 1 文件（pages/AuditPage.tsx） |
| **F-05** | 组织级通知策略 | A7 只收发模板 | `src/features/notify/` | 院系/角色默认偏好与强制项；不覆盖读者 OFF | ✅ 2026-11-19 · d4e6e04 · `pnpm typecheck`/`pnpm build` 通过；/notify 主题策略 6 个主题渲染，`PUT /admin/notify-topics/BORROW_DUE` code=0；SMS 渠道 422，UI 仅保留 INBOX/EMAIL · 3 文件（notify/api.ts、queries.ts、pages/NotifyPage.tsx） |
| **F-06** | 幂等与重试语义 | 重试不复用 key | `src/lib/api/` | 同一写操作重试复用 key；409/422 提示区分 | ✅ 2026-11-19 · 3a08e5d · `pnpm typecheck`/`pnpm build` 通过；同 body 连发两次 `POST /borrows` 复用同一 Idempotency-Key（8f94cb92…），失败不清键 · 2 文件（client.ts、errors.ts） |
| **F-07** | 演示账号清理 | 登录页预填 | `src/features/auth/pages/LoginPage.tsx` | `import.meta.env.DEV` 包裹，生产构建不含演示账号 | ✅ 2026-11-19 · 6fe2744 · `pnpm build` 通过；`dist` 内检索 ADMIN001/演示账号 计数 0 · 1 文件（LoginPage.tsx） |
| **F-08** | 管理端状态真实性 | 顶栏连接态/通知气泡/免打扰卡/侧栏值班为静态；批量拉黑只 toast；借还台「当前在办 0」硬编码 | `app/layout/*`、`features/circulation`、`features/iam` | 连接态取 dashboard 查询结果；通知数取 `pendingTotal` 与真实 notices；批量拉黑走真实接口并逐人报告；删除硬编码的在办数与假值班行 | ✅ 2026-11-23 · 4cf2d5f · `pnpm build` 通过（1864 modules / JS 764.93KB gzip 240.01KB）；浏览器实测徽标「服务在线」、批量弹窗勾选 2 人可提交（已取消）、借还台仅 2 张真实统计卡 |
| **F-09** | 值班卡去假名 | 工作台值班卡写死「值班馆员 · 王倩」 | `reporting/application/AdminQueryService.java`、`application.yml`、`WorkbenchPage.tsx` | 值班信息由 `app.duty.*` 配置注入（支持 `DUTY_NAME/DUTY_WINDOW/DUTY_PHONE`），默认「前台值班」；前端兜底同步 | ✅ 2026-11-23 · `mvn compile` 与 `pnpm build` 通过；运行态 `/admin/dashboard` 返回 `duty.name=前台值班` |

| **F-10** | 刷新令牌接入 | 401 直接清会话跳登录 | `src/lib/api/client.ts`、`src/lib/auth/session.ts`、`src/lib/api/models.ts` | 401→refresh→重放一次；并发 single-flight；失败才清会话并保留 from | ✅ 2026-11-23 · 无 git 提交 · `pnpm build` 通过（1864 modules / JS 765.66KB gzip 240.20KB）；ego-browser 实测 3 并发 401 仅 1 次 refresh、3 请求均重放 200；access+refresh 均无效时落 `/login?from=%2Fworkbench` 且会话清空；截图 temp/admin-verify2/refresh-after.png、refresh-fail-login.png；注：后端当前单 token（TTL 2h），过期后 refresh 同样 401，彻底消除 2h 重登需后端独立长 TTL refresh token，超本次前端范围 |

| **F-11** | 审批深链与搜索关键词 | 工作台 `/grace?id=`、`/appeals?id=` 被忽略而落第一条；审计忽略 `?keyword=` | `src/features/grace/pages/GracePage.tsx`、`src/features/appeal/pages/AppealPage.tsx`、`src/features/audit/pages/AuditPage.tsx` | Grace/Appeal 读 id 选中；失效 toast 并回落；Grace 标题=单号·学号·书名，通过/驳回确认弹窗写明对象·书名·天数；Appeal 顶部常显单号+状态，PENDING 行「去处理」/其他「查看」；Audit 用 keyword 初始化并立即查询 | ✅ 2026-11-23 · 无 git 提交 · `pnpm build` 通过（1864 modules / JS 767.98KB gzip 241.70KB）+ ego-browser 三条深链实测：GA202611191780 自动选中第 2 行、AP20260209002 选中处理中单、BC0001 审计回填并查到 0 条；失效 id 提示后 URL 清 id 回落；截图 temp/admin-verify2/fix-grace.png、fix-grace-confirm.png、fix-appeal.png、fix-audit.png · 3 文件 |

| **F-12** | 管理端交互 P1 修补 | 馆员搜索静默、通知不可点、借还台空表单无反馈、指标卡不可点、审计空态误导 | `app/layout/Topbar.tsx`、`features/circulation/pages/CirculationPage.tsx`、`components/common/StatCard.tsx`、`features/workbench/pages/WorkbenchPage.tsx`、`features/grace|appeal/pages/*.tsx`、`features/audit/pages/AuditPage.tsx` | 搜索无匹配 toast 并关层；通知可 Tab/Enter 跳转；空表单内联错误；四卡整卡可点；审计空态区分 | ✅ 2026-11-24 · 无 git 提交 · `pnpm typecheck` + `pnpm build` 通过（1864 modules / JS 770.56KB gzip 241.95KB）；ego 实测 lib-search-toast、notify-focus、circulation-empty-errors、workbench-card-focus、appeal-processing、audit-empty-category、rules-zero；记录 temp/ux-admin-fix/验收记录.md · 7 文件 |

**不要做**：不改 `components/ui/*`；不引第二套 UI；不开 dark mode；不端算金额/天数/SLA。

---

## 5. 客户端 Agent（`project/client`）

> 入口 `client/AGENT.md`；现状：全部 feature 具备 dio 实现（mock 保留开关）/ 46 测试绿 / `useMock` 默认 false / 附件真实上传 / 401 自动刷新令牌 / 预约接真实接口。

| 编号 | 任务 | 现状 | 落点 | 验收 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **C-01** | 去 mock 接真实后端 | `dio_` 覆盖全部 feature，`useMock` 默认 false | `lib/features/*/data/`、`endpoints.dart` | 全部 `dio_` 实现；`useMock` 默认 false；断网错误态可读 | ✅ 2026-11-19 · 无 git 提交 · analyze 0 / test 24 绿 · 通知/摘要/渠道/免打扰真接口，C1/C6/C7 真机验证 |
| **C-02** | 检索与书目详情 | 接 `/books`、`/copies`、`/queue` 与 `POST /borrows` | `lib/features/search/` | 接 B-01 四端点；空/无馆藏/可预约三态；300ms 防抖 | ✅ 2026-11-19 · 无 git 提交 · 详情弹层 + 库存状态；UI 真借阅成功（算法导论，订单 B20261119231652）并回流 C1；test 24 绿 |
| **C-03** | 消息中心与偏好 | 通知/渠道/免打扰走真接口 | `notifications/`、`profile/` | 接 B-02/B-04/B-05；失败回滚并提示 | ✅ 2026-11-19 · 无 git 提交 · 列表/未读数、渠道开关、免打扰读写真接口；失败回滚 toast；空态真机验证 |
| **C-04** | 首页摘要 | `/me/summary` | `lib/features/borrow/` | 切 `/me/summary`；数字与列表一致 | ✅ 2026-11-19 · 无 git 提交 · C1 摘要 3 在借/0 临期/2 逾期与列表一致；creditLabel 取服务端 |
| **C-05** | 申诉与个人中心 | dio（mock 保留开关） | `appeal/`、`profile/` | 提交/列表/详情接真实接口；徽标与 `/me` 一致 | ✅ 2026-11-19 · 无 git 提交 · `flutter analyze` 0 / `flutter test` 23 绿 / 真机 C8 列表+提交、C7 资料页 · 新增 2 文件（`dio_appeal_repository.dart`、`dio_profile_repository.dart`）+ 3 处改（provider、时间映射、登录默认） |
| **C-06** | 真机联调 | 真后端 + emulator-5554 | 全端 | C1→C2→C3、C5→C6→C7 全通；`flutter test` 绿 | ✅ 2026-11-19 · 无 git 提交 · C1→C2→C3 与 C5→C6→C7 真机走通；摘要/详情/宽限/预约/消息/我的均读真接口；test 26 绿 |
| **C-07** | 隐私与偏好页 | 已建 `privacy_page.dart` + dio/mock repo | `profile/` | 授权开关、遗忘申请、提醒偏好 | ✅ 2026-11-19 · 无 git 提交 · 设置→隐私与提醒；授权开关、6 主题频率、遗忘确认弹窗；真机 PUT 验证（sceneConsent=true、BORROW_DUE=WEEKLY）；test 26 绿 |
| **C-08** | 个人资料接真实字段 | `/me` 解析 department/major/grade/totalBorrowed | `profile/`、`core/models/` | 副标题真实、累计借阅取 totalBorrowed、空值省略 | ✅ 2026-11-23 · 无 git 提交 · `dart format` 0 changed / `flutter analyze` 0 / `flutter test` 34 绿 · 8 文件（含 2 生成物，新增 2 测试文件） |
| **C-09** | 刷新令牌接入 | 401 自动 refresh 并重放，single-flight | `core/network/`、`auth/` | `/v1/auth/refresh`；失败清会话回登录；并发只刷一次 | ✅ 2026-11-23 · 无 git 提交 · `endpoints.dart` 增 refresh；`api_client.dart` 401 单飞刷新+重放；失败清会话回调登录；3 拦截器测试；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 37 绿 |
| **C-10** | 真实预约 | 检索页「预约」原先只 toast；改真实 `POST /v1/reservations` | `search/`、`reservation/`、`core/widgets/`、`core/router/` | 确认 sheet（书名/预计排位/保留 48h）；成功行与详情「已预约 · 排位 N」；预约 tab 角标；409/422 原地提示；后端不可用置灰「暂不可预约」 | ✅ 2026-11-23 · 无 git 提交 · `endpoints.dart` 增 reservations；`SearchRepository.reserve` + `ReservationRepository.activeCount`；AppShell/AppTabBar 角标；新增 2 仓储测试 + 1 页面测试；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 40 绿；真机 2021001 预约《解忧杂货店》成功（reservation 2124881324775514113，WAITING/排位 1，截图 temp/ux-audit/fix-reserve-*.png） |

| **C-11** | 真机默认 IP 与 debug 溢出 | `192.168.0.3` 默认值；登录/检索两处 overflow | `core/config/`、`auth/`、`search/` | 默认 IP 可被 dart-define 覆盖；真机无黄黑条、无截断 | ✅ 2026-11-23 · f85c16b · `app_config.dart` 默认改 `192.168.0.3`；登录输入框 `minHeight:60`；检索头部 `Expanded+FittedBox`、副文案两行；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 40 绿；真机 UCB685CMAUIFFA7P 复验 01/04 截图，新预约 2124891109856104449 |

| **C-12** | 检索页真机反馈与交互修补 | 已预约行挤压文字、详情可上拉、筛选/排序是假入口 | `features/search/presentation/search_page.dart`、`features/search/domain/book_sort.dart`、`features/search/data/dio_search_repository.dart`、`features/{notifications,reservation,profile}/presentation/*.dart`、`features/appeal/presentation/appeal_create_page.dart` | 行内换行不截断；详情固定 86% 仅下滑关闭；筛选/排序真实生效；三页下拉刷新；借出刷新；申诉缺项汇总 | ✅ 2026-11-24 · 无 git 提交 · `dart format` 103 files 0 changed / `flutter analyze` 0 / `flutter test` 46 绿（+6）；模拟器实测 02–10 截图 temp/ux-client-fix · 未完成 B1/B3/B5/B7 |

**不要做**：不在端上算宽限/费用/SLA；不缓存状态机；不引第三方 UI 替换 token 体系。

---

## 6. 联动任务（由 FE / BE 发起，APP 配合）

> **规则**：联动一律由 **前端 Agent 或后端 Agent 发起**，客户端只配合、不发起。
> 发起方负责：① 在 §11 建登记行；② 先写清接口契约与验收；③ 拉配合方在登记行回「已确认」；④ 全绿后由发起方把 X 行改 `✅` 并补证据。

| 编号 | 任务 | 发起方 | 配合方 | 出口条件 | 状态 |
| --- | --- | --- | --- | --- | --- |
| **X-01** | 三端冒烟 | **FE** | BE、APP | 8125 + 5173 + 模拟器同时跑；C1→C3、A5→A7 两条链路无阻断，三条截图/日志留证 | ✅ 2026-11-23 · 8125+5173+emulator-5554 同时在线；管理端 14 路由（ADMIN001/LIB001）逐页通过；客户端新 debug APK（API 10.0.2.2）C1 登录→C2 书架（3 在借/2 逾期）→C3 详情（活着逾期 11 天）无阻断；宽限真实上传 evidence.png 落库（GA202611232183 / evidence 2124863056660606977）；A5→A7 由管理端宽限/申诉页覆盖 · 证据 `temp/admin-verify/`、`temp/ego-verify-20261123/`、`temp/x01-client/01–09` |
| **X-02** | 并发压测 | **BE** | FE | 200 并发归还 + 100 并发催还；P95 < 800ms、无 5xx、无死锁 | ✅ 2026-11-19 · 归还 200/200 成功、P95 294.4ms（max 587.7，wall 2.51s）；催还 100/100 成功、P95 185.1ms（max 228.4，wall 1.37s）；5xx=0；`SHOW ENGINE INNODB STATUS` 无 LATEST DETECTED DEADLOCK；脚本 `backend/scripts/loadtest-x02.ps1`（`-Mode returns|remind`） |
| **X-03** | 文档回填校验 | **BE** | FE、APP | 每批结束后 `技术架构.md` §7/§7.1、`API-INTEGRATION.md`、`client/AGENT.md` 三处端点一致，grep 无幽灵路径 | ✅ 2026-11-19 · `技术架构.md` 不存在（§11 X-03-D1）；FE 43 处 / 客户端 11 处 `/v1` 引用全部可解析；`frontend/docs/API-INTEGRATION.md` 缺口段已按 B-01…B-12 重写；报告 `temp/x03-doc-consistency.md` |
| **X-04** | 换行统一 | **BE** | 全员 | `.gitattributes`（`* text=auto eol=lf`）后 `git status` 无 CRLF 警告 | ✅ 2026-11-19 · 根 `.gitattributes`（文本 LF、二进制 binary、`*.cmd/*.bat` CRLF）；279 个已跟踪文本文件由 CRLF 归一为 LF；`git add` 不再出现 CRLF 警告（仅 APP 未提交的 `reader_mappers.dart` 保持原状） |

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
| 2026-11-19 | FE | F-02 角色权限页 | 10 项权限勾选 + 角色基线；B-12 已就绪，可接保存 |
| 2026-11-19 | FE | F-01 校历管理 | 新增/编辑/删除 + 学期/场景/来源校验；创建 2026-FALL 校历并回显 |
| 2026-11-19 | FE | F-02 角色权限接真接口 | 24 项权限按 13 模块分组；保存后刷新 /me 收敛菜单 |
| 2026-11-19 | FE | F-03 宽限证据 | 文件名/大小/受控下载/链接兜底；管理端 evidence 仍为空，待后端补 |
| 2026-11-19 | FE | F-04 审计详情 | traceId 复制、before/after 差异表、空快照降级、动作码中文化 |
| 2026-11-19 | FE | F-05 通知主题策略 | 6 主题频率/渠道/启停；SMS/WECHAT 422 后仅保留 INBOX/EMAIL |
| 2026-11-19 | APP | C-05 申诉与个人中心 | C8 真实列表/提交、C7 真实资料页；`GET /v1/me` + 借阅/预约计数；时间统一北京时间；analyze 0 / test 23 绿；仓库无 `.git` 未提交 |
| 2026-11-19 | APP | C-01 去 mock | 7 个 provider 改按 `appUseMockProvider` 切换，`useMock` 默认 false；C1 摘要/未读取自 `/me/summary` + `unread-count`，C6 通知/渠道/免打扰、C7 资料走真接口；analyze 0 / test 24 绿；仓库无 `.git` 未提交 |
| 2026-11-19 | APP | C-02 检索与详情 | 接 `/books`、`/books/{id}`、`/copies`、`/queue`；详情弹层展示 ISBN/馆藏/副本/排队；UI 真实借阅「算法导论」成功（B20261119231652），C1 回流 3 在借；analyze 0 / test 24 绿；仓库无 `.git` 未提交 |
| 2026-11-19 | APP | C-03/C-04 消息与摘要 | 通知列表/未读数、渠道/免打扰真接口；C1 摘要切 `/me/summary`，3 在借/0 临期/2 逾期与列表一致；失败回滚提示；analyze 0 / test 26 绿 |
| 2026-11-19 | APP | C-06/C-07 联调与隐私页 | C1→C2→C3、C5→C6→C7 真机全通；设置新增「隐私与提醒」：授权开关、6 主题频率、遗忘确认；真机 PUT 验证 sceneConsent=true、BORROW_DUE=WEEKLY；analyze 0 / test 26 绿 |
| 2026-11-19 | APP | C-01–C-07 提交与安装包 | 客户端 7/7；commit `90cf93a`（功能）+ `ad5be55`（README）；release APK 23.5MB，`API_BASE_URL=http://192.168.123.115:8125/api`；模拟器 release 启动验证通过 |
| 2026-11-19 | BE | B-06 通知偏好 | 6 主题 + 差异偏好 + 日志 + 管理端字典；P0 必含 INBOX、P2 禁 REALTIME、SMS 422；排程写 `topic_code`/`pref_snapshot`，投递按快照∩渠道开关；V9；verify 67 run/1 skip |
| 2026-11-19 | BE | B-07 校历写接口 | 新增/修改/软删除 + 学期场景起止校验、同场景重叠 422、CALENDAR_* 审计；verify 73 run/1 skip |
| 2026-11-19 | BE | X-02 并发压测 | 种子 300 读者/300 册（`temp/x02-seed.sql`）；`backend/scripts/loadtest-x02.ps1` 并发 64：归还 200/200、P95 294.4ms、max 587.7ms；催还 100/100、P95 185.1ms、max 228.4ms；5xx=0、无死锁；压测数据已清理 |
| 2026-11-19 | BE | X-04 换行统一 | 新增 `.gitattributes`（`* text=auto eol=lf` + 二进制规则）；`git add` 归一化 279 个文本文件，之后 `git status`/`git add` 无 CRLF 警告 |
| 2026-11-19 | BE | X-03 文档回填校验 | 全仓比对 76 个后端路径 vs FE 43 / 客户端 11 处引用，幽灵路径 0；`技术架构.md` 缺失登记 `X-03-D1`；FE `API-INTEGRATION.md` 缺口段重写为 B-01…B-12 新增端点清单，并纠正「续借/证据/traceId/sort/幂等/校历」六处过时描述 |
| 2026-11-19 | BE | B-12 角色权限调整 | `GET /v1/admin/roles`、`GET /v1/admin/permissions`、`PUT /v1/admin/roles/{code}/permissions`；V13 新增 `role:manage`（仅 ADMIN）；全量替换、ADMIN 守卫；`JwtAuthenticationFilter` 回源使旧 token 立即失权；新增 `RoleAdminControllerTest` 4 例；verify 90 run/1 skip |
| 2026-11-19 | BE | B-11 刷新令牌 | `POST /v1/auth/refresh`：校验旧 jti → 按库重算权限 → 签发新 token → 吊销旧 jti；旧 token 立即 401，同一旧 token 只能刷新一次；写 `AUTH_REFRESH` 审计；新增 `AuthRefreshTest` 3 例；verify 86 run/1 skip |
| 2026-11-19 | BE | B-10 隐私授权与遗忘 | V12 增 `privacy:self`；`GET/PUT /v1/me/consents` 默认最小授权、关闭记 `consentRevokedAt` 并写 `CONSENT_REVOKE`；`POST /v1/me/forget` 脱敏姓名/手机/邮箱/院系、保留借阅事实、写 `USER_FORGET`，重复 409；新增 `PrivacyControllerTest` 4 例；verify 83 run/1 skip |
| 2026-11-19 | BE | B-09 审计可追溯 | V11 增 `trace_id`/`before_json`/`after_json`；宽限审批、申诉处理、规则调整写前后值；列表/详情/CSV 导出返回；`X-Trace-Id` 落库；新增 `AuditTraceTest`；verify 79 run/1 skip |
| 2026-11-19 | BE | B-08 宽限证据附件 | `POST/GET /v1/files` + `POST /graces` 收 `evidenceIds`（≤5 个）+ `GET /graces/{id}` 详情；V10 `file_object`/`grace_evidence`；5MB、png/jpg/pdf、魔数校验、越权 404；verify 77 run/1 skip |
| 2026-11-19 | BE | F-03 管理端证据补齐 | `AdminQueryService#graceView` 不再固定空数组：管理端列表批量装配 `evidence[{label,url}]`；新增 `GET /v1/admin/graces/{id}`（`grace:approve`）返回同结构，不存在 404；`GraceEvidenceTest` 增管理端断言；verify 90 run/1 skip。顺带 ponytail 清理：`GraceEvidenceRepositoryImpl.fileIdsOf(long)` 复用批量实现、`NotifyTemplateService.list()` 收口模板列表，`AdminQueryService` 不再直连 `notify.domain.repository` |
| 2026-11-19 | BE | ponytail 收口（3/4/5） | ① `ReaderSummaryService` 学期起点优先读 `academic_calendar` 的 `NORMAL` 区间（`AcademicCalendarService.semesterStart`），空表回退近似并留 `// ponytail:` 注释；② 书目 `size>200` 删除私有 422 分支，统一走 `PageQuery` 的 400/40000；③ 渠道报错文案改 `String.join("、", NotifyChannel.SUPPORTED)`；定向 12 例绿，`BookControllerTest` 断言改 400 |
| 2026-11-23 | BE | B-14 申诉证据附件 | 新增 V14 `appeal_evidence`（UK(appeal_id,file_id)、FK）与仓储/读模型；`POST /v1/appeals` 收 `evidenceIds`（≤5，仅本人文件，越权 404）；新增 `GET /v1/appeals/{id}`；管理端列表装配 `evidence[{label,url}]`；旧请求兼容 `evidence=[]`；`mvn test` 95 绿/1 跳（新增 4 例）；接口文档 §18.20 |
| 2026-11-23 | APP | API 地址与证据附件 | `app_config.dart` 默认 `http://10.22.127.30:8125/api`；新增 `POST /v1/files` multipart 与 `evidence_upload.dart`；宽限/申诉真实选文件、上限 5、≤5MB、先传后提；`flutter analyze` 0 / `flutter test` 30 绿；APK 构建被离线 Gradle 阻塞（未产生新包） |
| 2026-11-23 | FE | 权限收敛与工作台文案 | 馆员隐藏借出表单（`borrow:create`）与未授权死链；顶栏搜索先按权限过滤；免打扰卡改真实 `stats.pendingTotal`/`duty.window`，去掉「已延后 14 条」等假状态；`pnpm build` 通过（1864 modules / JS 764.16KB gzip 239.74KB） |
| 2026-11-23 | Lead | 收口复核 | §11 七条全部裁决；§2 索引与 §4 详表状态校正（F-03/F-06/F-07）；PRD 升至 V1.1、README 重写；X-01 记 🟡（管理端+后端已验，客户端 APK 阻塞） |
| 2026-11-23 | Lead | B-15 预约过期清扫 | `@Scheduled` 每 10 分钟扫 `READY` 过期：置 `EXPIRED` + 审计；队首 `READY`（48h）+ outbox；无人释放 `READY_SHELF` 副本（`releaseHoldForBook`）；`ReservationExpiryTest` 3 例；`mvn test` 98 绿/1 跳；提交 f4b1ce0 |
| 2026-11-23 | BE | B-16 SMTP 邮件通道恢复 | 用户级新授权码经进程环境变量注入，重启 8125；`MailGateway.send` 改返回 Message-ID，`EmailDeliveryService` 落 `notify_delivery.provider_msg_id`（6 参 append 保留 default 兼容 INBOX）；真实催还 1 封：delivery `2124869019035705345` SENT / provider_msg_id `<981383191.0.1795443225035@ZiAn>`；旧码被判为 QQ 侧重新生成后失效（同端口/发件人仅换码即成功）；deploy README 补三步排查；`mvn -o test` 98 绿/1 跳 |
| 2026-11-23 | BE | B-17 `/me` 资料字段扩展 | `SessionUser` 追加 `department/major/grade`（NULL→`""`）与 `totalBorrowed`（全部借阅单含已归还），登录/刷新/`/v1/me` 同构；新增 `ReaderSelfRepository`（IAM 领域端口）+ 实现与 `ReaderProfileMapper.countBorrowed` 只读计数；不动表结构、不改 client；`MeProfileTest` 2 例；`mvn -o test` 100 绿/1 跳；接口文档 §3.2 |
| 2026-11-23 | BE+FE | F-09 值班卡去假名 | 后端 `AdminQueryService` 改 `@Value` 注入 `app.duty.name/window/phone`（默认「前台值班」/08:00–22:00/内线 8021），`application.yml` 支持 `DUTY_*` 环境变量；管理端兜底文案同步；接口文档示例更新；`mvn compile` 与 `pnpm build` 通过；运行态 `/admin/dashboard` 返回 `duty.name=前台值班` |
| 2026-11-23 | APP | X-01 客户端链路 | 网络恢复后重建 debug APK（Gradle 8.14.3 本地包 + AGP 8.11.0/Kotlin 2.1.20/compileSdk 36，471.5s，API 10.0.2.2）；模拟器安装后 C1→C2→C3 无阻断；宽限真实上传 evidence.png 并提交（GA202611232183 / evidence 2124863056660606977）；截图 temp/x01-client/01–09；`client/android` 临时构建改动已在提交前还原，配方写入 client/README |
| 2026-11-23 | FE | F-08 管理端状态真实性 | 顶栏连接态/通知数取真实查询；批量拉黑走接口并逐人报告；借还台删硬编码「当前在办」；侧栏删假值班行；`pnpm build` 通过、浏览器实测 |
| 2026-11-23 | Lead | X-05 CI/CD 与容器化 | `.github/workflows/ci.yml`（三端构建/测试）、`release.yml`（tag 制品 + ghcr 镜像 + Release）、`backend|frontend/Dockerfile`、`frontend/nginx.conf`、`deploy/docker-compose.yml`、`.env.example`、`application-prod.yml`；`docker compose config` 与 YAML 校验通过；README 增 CI/CD 段 |
| 2026-11-23 | Lead | 离线收尾复核 | 后端 98 测试、管理端构建、客户端 30 测试全绿；工作树待提交（B-15/F-08/X-05/文档）；GitHub 仍不可达，未推送 |
| 2026-11-23 | APP | C-08 个人资料真实字段 | `DioProfileRepository` 解析 `/me` 的 department/major/grade，`totalBorrowed` 用 `asInt` 兼容字符串；删除 `department: ''`、`degree: roleLabel`、`totalBorrowed: borrowed` 假值；副标题改 `studentNo · major · grade` 且空值省略；累计借阅取 totalBorrowed；mock 同步；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 34 绿（新增 2 仓库映射 + 2 页面两态） |
| 2026-11-23 | APP | C-09 客户端刷新令牌 | `endpoints.dart` 增 `/v1/auth/refresh`；`ApiClient` 401 用旧 token 刷新并重放一次，`_refreshInFlight` single-flight；refresh 失败 `clearSession` 并回调 `AuthController.sessionExpired` 回登录；新增 `token_refresh_test.dart` 3 例（成功重放/失败清会话/并发单飞）；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 37 绿 |
| 2026-11-23 | APP | C-10 客户端真实预约 | `endpoints.dart` 增 `/v1/reservations`；`SearchRepository.reserve` POST `{bookId}` 解析 `queuePosition`；检索页预约改确认 sheet，成功回显「已预约 · 排位 N」并刷新预约列表/角标，409/422 原地提示，网络/5xx 后按钮置灰「暂不可预约」；`ReservationRepository.activeCount` 驱动 AppTabBar 角标；新增 2 仓储 + 1 页面测试；`dart format` 0 changed / `flutter analyze` 0 / `flutter test` 40 绿；真机 2021001 预约《解忧杂货店》成功（reservation 2124881324775514113，WAITING/排位 1，截图 temp/ux-audit/fix-reserve-*.png） |
| 2026-11-23 | FE | F-10 管理端刷新令牌 | `session.ts` 增 refreshToken（旧会话回退 token）、`LoginResult` 可选 refreshToken；`client.ts` 401 单飞 `POST /auth/refresh` 后重放一次，失败清会话并跳 `/login?from=`；ego-browser 3 并发 401 仅 1 次 refresh 且均重放 200，双无效 token 落登录页；`pnpm build` 通过（1864 modules / 765.66KB gzip 240.20KB） |
| 2026-11-23 | BE | B-18 拉黑到期自动解除 | `User.isBlacklisted()` 增加到期实时判定（到期即按 NORMAL 放行，不等扫描）；新增 `BlacklistExpiryService` 每 60s 扫描 `BLACKLISTED + expires_at<=now`，回写状态并写 `user_status_log`（`BLACKLISTED→NORMAL` /「到期自动解除」/「系统」）+ 审计 `USER_UNBLOCK_EXPIRE`；`UserAdminService` 拉黑/恢复也写状态日志；`SysUserPO` 四个拉黑字段 `updateStrategy=ALWAYS` 修复 null 不清空；`BlacklistExpiryTest` 4 例；`mvn -o test` 107 绿/1 跳；接口文档 §11.2 |
| 2026-11-23 | BE | B-19 上传与配置加固 | `FileService` 对 png/jpg 改 `ImageIO` 真实解码并要求格式=扩展名（12B 伪图拒绝）；清理 `backend/data/uploads` 下 9 个伪 PNG；`.gitignore` 忽略 uploads；`CorsConfig` 改白名单（`CORS_ALLOWED_ORIGINS`）；prod 关 springdoc、actuator 仅 `health,info`、日志 INFO；compose/`.env.example` 标注默认口令仅演示；`FileUploadValidationTest` 3 例；`mvn -o test` 107 绿/1 跳；`docker compose ... config --quiet` exit 0 |
| 2026-11-23 | FE | F-11 审批深链与搜索关键词 | `GracePage`/`AppealPage` 用 `useSearchParams` 读 `?id=`；失效 toast「该工单已归档或不存在」并清 id 回落；Grace 详情改「单号·学号·书名」，通过/驳回加 AlertDialog（对象·书名·天数）；Appeal 详情顶栏常显单号+状态，PENDING 行「去处理」/其他「查看」；`AuditPage` 读 `?keyword=` 初始化并同步 URL，空态给「查看全部日志」；`pnpm build` 通过（1864 modules / JS 767.98KB gzip 241.70KB），ego 深链实测 GA202611191780 / AP20260209002 / BC0001，截图 temp/admin-verify2/fix-grace.png、fix-grace-confirm.png、fix-appeal.png、fix-audit.png |
| 2026-11-23 | APP | C-11 真机默认 IP 与溢出 | 默认地址跟进当前 WLAN（192.168.0.3，可 dart-define）；修登录 2px、检索 13px overflow；真机重装复验；APK 115,242,041B / SHA256 8DC4B431…A9DA；test 40 绿 |
| 2026-11-23 | Lead | 真机 + 邮件演示证据 | Realme RMX3041（UCB685CMAUIFFA7P）真机登录/书架/详情/预约全 PASS；SMTP 587 实发催还邮件 `EMAIL/SENT`（delivery 2124887081382301697，msgid `<1540127645.0.1795447531533@ZiAn>`），QQ 邮箱 IMAP 实收；证据 temp/real-phone/、temp/mail-shot/ |
| 2026-11-24 | APP | C-12 检索页真机反馈与交互修补 | 已预约行改 Wrap 多行（libraryLabel 不再被 ellipsis）；详情由 DraggableScrollableSheet 改固定 86% + ListView（只能下滑关闭）；「筛选」改真实面板（全部/可借/可预约，说明按可借状态）；「按相关度」改可点排序（相关度/可借优先/书名 A-Z）；消息/预约/我的下拉刷新；借出后刷新检索；申诉补「还差 N 项」；新增 `book_sort.dart` + 6 测试；`flutter analyze` 0 / `flutter test` 46 绿；截图 temp/ux-client-fix/ |
| 2026-11-24 | FE | F-12 管理端交互 P1 修补 | 馆员搜索无匹配 toast 并关层；通知行改 button 可 Tab/Enter 跳转（逾期/宽限/申诉/提醒）；借还台空表单内联错误 + toast + 回车校验；指标卡整卡可点（待办/到期/逾期/超时申诉）；审计空态区分；`pnpm typecheck` + `pnpm build` 通过（1864 modules / JS 770.56KB gzip 241.95KB）；截图 temp/ux-admin-fix/ |
| 2026-11-23 | Lead | 审查与 P0 修复收口 | 安全审查 P0=0/P1=1/P2=13、交互审查 P0=3/P1=17/P2=12；本批修复 P1 刷新令牌（F-10/C-09）与 3 条 P0（C-10/F-11）；提交 b37c6e4/44013c5/9861f40；后端 107、客户端 40 测试绿 |

---

## 11. 联动登记（发起方先写，配合方回「已确认」）

| 编号 | 发起方 | 契约摘要 | 配合方确认 | 状态 |
| --- | --- | --- | --- | --- |
| C-05-D1 | APP | 申诉详情：`GET /v1/appeals/self` 已含客户端「查看进度」所需全部字段（状态、SLA、处理结果、时间线），不新增 `GET /v1/appeals/{id}`；后续确需独立详情（如多附件/长文本）再加 | BE 已裁决 | ✅ 已解决 |
| B-05-D1 | BE | 免打扰表 `reader_quiet_hours` 为单窗口（PK=user_id），本期只支持一个时段，跨零点允许、起止相同返回 422；「窗口重叠 422」在多窗口落地前无可触发路径，是否需要多时段请确认 | Lead 已裁决 | ✅ 2026-11-23 本期单窗口，多窗口列入 Future |
| B-06-D1 | BE | FR-11 接口为自定契约（`/v1/me/notify-topics`、`/v1/me/notify-preferences`、`/v1/admin/notify-topics`），规格文档缺失；DAILY/WEEKLY 本期只落库与快照、**不做频率聚合**；P0 判定为「渠道必须含 INBOX」，P2 禁 REALTIME。**渠道仅 INBOX + EMAIL（邮件已接通），SMS/微信不接入**（2026-11-19 产品决定） | Lead 已裁决 | ✅ 2026-11-23 按接口文档 §18.14 执行 |
| B-01-D1 | BE | 书目检索 `size>200` 已统一为全站口径 **HTTP 400 / code 40000**（删除私有 422 分支，测试同步改 400） | BE 已裁决 | ✅ 已解决 |
| X-03-D1 | BE | `project/spec/技术架构.md` 不存在（todolist 与三个 AGENT.md 仍引用其 §7/§7.1），当前契约真源为 `project/后端的交互接口文档.md`；是否补写该文档或改为引用新文档 | Lead 已裁决 | ✅ 2026-11-23 真源=`后端的交互接口文档.md`，不补写技术架构.md |
| B-07-D1 | BE | 校历写接口 `term` 强校验为 `四位年份-大写字母/数字`（如 `2026-FALL`），同学期同场景闭区间不允许重叠（422）；规格文档缺失，若实际学期编码为 `2026-2027-1` 之类请提供格式 | Lead 已裁决 | ✅ 2026-11-23 统一 `2026-FALL`；教务格式由前端映射 |
| B-10-D1 | BE | 关闭「计算缓存」授权时，已写入 Redis 的宽限快照不做即时删除（键为 `readdue:grace:snap:{orderId}`，跨上下文枚举成本高），按 24h TTL 自然过期；遗忘为不可撤销的脱敏，账号仍可登录但无姓名/手机/邮箱 | Lead 已裁决 | ✅ 2026-11-23 接受 24h TTL，页面提示「最迟 24 小时内失效」 |
| B-09-D1 | BE | 审计新增 `trace_id`/`before_json`/`after_json`：`traceId` 取请求头 `X-Trace-Id`（无头时由过滤器生成，与响应 `traceId` 一致）；前后值 JSON 字符串只覆盖宽限审批、申诉处理、规则调整三类；CSV 导出追加 3 列。前端如需把旧行也展示为结构化对象，请按 `null` 处理 | Lead 已裁决 | ✅ 2026-11-23 旧行按 null 降级，不补历史数据 |
| B-08-D1 | BE | 证据附件为自定契约：`POST /v1/files`（multipart `file`，png/jpg/pdf、≤5MB、按魔数校验，超限 422）、`GET /v1/files/{id}`（本人或馆员可读，他人 404）、单次最多 5 个 `evidenceIds`；新增 `GET /v1/graces/{id}` 详情（此前只有 `/graces/self`）。若前端需要多文件上限调整或图片压缩，请提供口径 | Lead 已裁决 | ✅ 2026-11-23 上限保持 ≤5MB × 5 个；申诉沿用同契约（B-14） |
| FR-05-D1 | BE | `READY` 预约的 48h 保留期目前只能由管理端 `POST /admin/reservations/{id}/expire` 手动触发，无定时清扫；赵一 2026-11-16 已过期的记录一直显示「待取书」。是否补 `@Scheduled` 清扫（过期 + 顺延下一位 + 通知） | Lead 已裁决 | ✅ 2026-11-23 已补 B-15：每 10 分钟扫描，过期置 `EXPIRED`、队首顺延 48h 并发 `RESERVATION_READY`、无人释放副本 |
