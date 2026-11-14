# 读者端 AGENT.md · 阅有期 Flutter 客户端

> 本文件是给「读者端 AI / 前端工程师」的初始化与开发指令。
> 只描述**读者端**（Flutter，Android/iOS）。管理端在 `project/frontend/`，后端在 `project/backend/`，三者共用同一套接口与设计语言。

---

## 0. 工程现状（扫描于 2026-11-05，先读再动手）

当前 `project/client/` 是 **Flutter 官方计数器模板**，除脚手架外没有业务代码：

| 项 | 现状 |
| --- | --- |
| `pubspec.yaml` | `name: client`，`environment: sdk ^3.5.3`，依赖仅 `flutter` + `cupertino_icons ^1.0.8`，dev 依赖 `flutter_test` + `flutter_lints ^4.0.0`；`uses-material-design: true`，**未配置 assets / fonts** |
| `lib/main.dart` | 5093B 模板：`MyApp`（`MaterialApp`，`ColorScheme.fromSeed(Colors.deepPurple)`，`useMaterial3: true`）→ `MyHomePage` → `_MyHomePageState`（`_counter` + `setState` + `FloatingActionButton`） |
| `test/widget_test.dart` | 模板自带的 +1 计数测试，**重建时删除或替换** |
| 平台目录 | `android/`（含 gradle wrapper）、`ios/`（Runner、Podfile 待生成）、`.metadata`、`analysis_options.yaml`、`client.iml` |
| 缺失 | 主题、路由、网络层、状态管理、本地存储、国际化、错误处理、任何页面 |

**结论：这不是「改造」，是「重建」——除 `android/`、`ios/` 的签名与包名配置外，`lib/` 与 `test/` 应整体替换。**

---

## 1. 必读顺序（缺一不可）

文档在 Obsidian 项目库，代码仓在 `G:\workspace\ReadDue`，两者路径不同，注意区分：

| 顺序 | 文件 | 读什么 |
| --- | --- | --- |
| 1 | `Projects\阅有期\project\requires\require_total.md` | §3 功能需求（FR-01…FR-11）、§11.7 FR-11 提醒偏好 |
| 2 | `Projects\阅有期\project\spec\技术架构.md` | §7 接口清单、§10 权限分级、§11 用户状态、§13 通知偏好、§14 课表免打扰 |
| 3 | `Projects\阅有期\project\spec\借阅与归还说明书.md` | §3 借出/归还、§9 通知与免打扰、§12 状态机 |
| 4 | `Projects\阅有期\design\ui\交互说明书.md` | §1 设计语言、§2 全局交互、**§3 读者端 C1–C9**、§5 跨页流程 |
| 5 | `Projects\阅有期\design\ui\prototype.html` | 20 帧原型，读者端 C1–C9；`?shot=C3` 可单帧截屏 |
| 6 | `Projects\阅有期\project\spec\schema.sql` | 字段口径（`status`、`deleted`、`version`、时间字段） |

> 设计稿是**唯一视觉事实来源**：颜色、字号、间距、圆角一律从 `prototype.html` + 交互说明书 §1 取，不要自创。

---

## 2. 技术栈与不可协商

| 项 | 选择 | 说明 |
| --- | --- | --- |
| 框架 | Flutter 3.x / Dart `^3.5.3` | 保持 `pubspec.yaml` 现有 SDK 约束 |
| 设计体系 | Material 3 | `useMaterial3: true`，但主题必须按第 4 节 token 覆写 |
| 状态管理 | **Riverpod 2.x**（`flutter_riverpod` + `riverpod_annotation` 可选） | 禁止用 `setState` 管跨页/跨组件状态；页面内纯 UI 状态可用 `StatefulWidget` |
| 路由 | **go_router** | 声明式、支持深链（`/orders/:id`）、支持登录重定向 |
| 网络 | **dio** | 统一拦截器：Bearer、`traceId`、`Idempotency-Key`、错误码映射 |
| 模型 | **freezed + json_serializable** | DTO 不可变、`copyWith`、`==`；JSON 解析放 `data/` 层 |
| 本地存储 | **shared_preferences**（键值）+ **hive_flutter**（列表缓存） | 只缓存读多写少的数据；不缓存借阅单状态机 |
| 时间/金额 | `intl` | 统一格式化，见 §5.4 |
| 图标 | `lucide_icons` 或 Material Symbols | 全工程只用一套 |
| 测试 | `flutter_test` + `mocktail` | repository 层 mock，widget test 覆盖关键页 |

**不可协商（违反即返工）**

1. 颜色、字号、圆角、间距**只允许**引用 `lib/core/theme/` 中的 token，禁止在页面里写 `Color(0xFF...)`、`fontSize: 17`。
2. 页面四态（加载/空/错误/无权限）必须齐备，见 §6。
3. 所有写请求必须带 `Idempotency-Key`（UUID v4，页面级生成，重试复用同一个）。
4. 读者只能访问本人资源；他人单据统一按 `404` 处理，界面文案「单据不存在或无权查看」，**不得**提示「无权访问」。
5. 拉黑（`BLACKLISTED`）只拦「借出、续借、预约、申请宽限」；归还、申诉、查看记录、收提醒必须可用，且相关页面给同一句解释。
6. 任何金额、日期、编号必须来自接口，不在端上计算逾期费用（宽限天数由后端 `Bs×Kt×Kq` 计算）。
7. 触控目标 ≥ 44×44；正文对比度 ≥ 4.5:1（`#6A7486` 仅用于 ≥10.5px 的三级文字）。
8. 尊重 `prefers-reduced-motion`（Flutter 侧读 `MediaQuery.disableAnimations`），关闭非必要过渡。

---

## 3. 目录结构（按 feature 切，不按类型堆）

```
lib/
├── main.dart                     # runApp(ProviderScope(child: App()))
├── app.dart                      # MaterialApp.router + theme + locale
├── core/
│   ├── theme/
│   │   ├── tokens.dart           # 颜色/间距/圆角/字号常量（唯一来源）
│   │   ├── app_theme.dart        # ThemeData：ColorScheme、TextTheme、AppBarTheme…
│   │   └── typography.dart       # 中文/数字字体族与字重
│   ├── router/
│   │   ├── app_router.dart       # go_router 路由表 + 登录守卫
│   │   └── routes.dart           # 路由常量（含参数名）
│   ├── network/
│   │   ├── api_client.dart       # dio 实例 + 拦截器
│   │   ├── api_exception.dart    # 错误码 → 用户文案
│   │   └── endpoints.dart        # 路径常量，禁止页面拼字符串
│   ├── storage/
│   │   ├── kv_store.dart         # shared_preferences 封装
│   │   └── cache_box.dart        # hive box 初始化
│   ├── auth/
│   │   ├── auth_repository.dart
│   │   └── auth_controller.dart  # 登录态、token 刷新、登出
│   ├── widgets/                  # 跨 feature 复用：AppCard、StatusPill、SectionHeader、
│   │                             # EmptyState、ErrorState、SkeletonList、AppButton、AsyncValueView
│   └── utils/                    # 日期、金额、防抖、UUID
├── features/
│   ├── borrow/                   # C1 我的借阅
│   │   ├── data/                 # dto + repository 实现
│   │   ├── domain/               # 实体与用例（可选，纯 Dart）
│   │   └── presentation/         # page + widgets + provider
│   ├── order_detail/             # C2 借阅详情与时间线
│   ├── grace/                    # C3 宽限申请
│   ├── search/                   # C4 检索
│   ├── reservation/              # C5 预约
│   ├── notifications/            # C6 消息中心
│   ├── profile/                  # C7 我的
│   ├── appeal/                   # C8 申诉工单
│   └── auth/                     # C9 登录
└── l10n/                         # 中文为主，预留 en
```

规则：

- feature 之间**不直接 import** 对方的 `data/`；跨 feature 复用放 `core/`。
- `presentation/` 不出现 `http`/`dio`；只调用 provider → repository。
- 每个 feature 至少一个 `*_page.dart` + 一个 `*_providers.dart`；超过 400 行的页面必须拆 widget。

---

## 4. 设计系统（token 必须照抄）

### 4.1 颜色（一色一义）

| Token | 值 | 用途 | 禁止 |
| --- | --- | --- | --- |
| `ink` | `#334155` | 主文字、主按钮、深色概览卡 | 大面积铺底 |
| `mint` | `#64C2A6` | 状态标签、选中态、进度、成功 | **正文文字色**（对纸白约 2:1） |
| `bg` | `#FBFCF8` | 页面底色 | 卡片不用它，卡片用纯白 |
| `mintDeep` | `#2A7159` | 薄荷底上的文字、链接 | — |
| `mintSoft` | `#E7F5EF` | 选中行底、成功标签底 | — |
| `ink2` | `#515D6F` | 次级正文 | 大面积背景 |
| `ink3` | `#6A7486` | 三级说明、时间、表头 | < 10.5px 或彩色底上 |
| `wash` / `sunken` | `#F6F8F3` / `#F0F2EC` | 表头、次级底、分段控件槽 | 正文大面积背景 |
| `line` / `line2` | `#E6E9E3` / `#EFF1EC` | 卡片描边 / 分隔线 | 纯黑描边 |
| `deep` | `#26303F` | 登录页深色分栏 | 普通页面 |
| `amber` | `#8C5C16` | 待处理、临期、需注意 | 表示失败 |
| `red` | `#A3492F` | 失败、逾期、拒绝、危险操作 | 普通提醒 |

语义映射：`mint` = 正常/成功/已到馆；`amber` = 等待/临期/待裁决；`red` = 异常/逾期/拒绝；`slate` = 中性/已完成；`dark` = 当前选中筛选项。

### 4.2 字号与排版

- 中文 `Noto Sans SC / PingFang SC / 微软雅黑`；数字 `Inter`，金额/编号用 `FontFeature.tabularFigures()`。
- 阶梯：34 / 30 / 26 / 24 / 22 / 20 / 17 / 15 / 13.5 / 12.5 / 11.5 / 10.5 / 10 / 9.5。
- 行高：标题 1.2–1.3，正文 1.6–1.8；中文标题负字距 `-0.2 ~ -1.6`（Flutter 用 `letterSpacing` 负值，慎用）。
- 手机端页面标题 22–26，卡片标题 15–17，正文 13.5–15，辅助 11.5–12.5。

### 4.3 形状与密度

- 圆角：卡片 12–14、面板 14、按钮 9–11、胶囊 99。
- 间距基准 4：4/8/10/12/14/16/18/22/26/30；页面左右边距 20。
- 卡片靠 1px `line` 描边分层，**不用投影**（仅底部弹层/浮层可用一次轻投影）。
- 触控 ≥ 44×44；底部 tab 栏高度 56 + 安全区。

### 4.4 组件映射（先建组件，再拼页面）

| 组件 | 规格 | 出现在 |
| --- | --- | --- |
| `AppCard` | 白底、radius 14、1px line、padding 16 | C1–C8 |
| `StatusPill` | 高 24、radius 99、语义底 + 深阶文字 | 逾期/已到馆/已批准 |
| `BookCover` | 56×76 或 72×96、radius 8、左侧 3px 书脊 | C1/C2/C5 |
| `SectionHeader` | 标题 17/600 + 右侧动作文字 12.5 | C1/C2/C6 |
| `SummaryBlock` | 深色 `ink` 卡、三列数字 30/700、分隔线 1px `#45536B` | C1 |
| `TimelineItem` | 圆点 10 + 竖线 1px + 时间/标题/说明 | C2 |
| `EmptyState` / `ErrorState` | 图标 + 一句结论 + 一个动作 | 全页 |
| `SkeletonList` | 行高与真实卡片一致、≥800ms 才显示 | 全页 |
| `PrimaryButton` | 高 48、radius 12、`ink` 底白字；危险操作 `red` | C1–C8 |
| `AppTabBar` | 4 项：借阅 / 检索 / 预约 / 我的，图标 22 + 文字 11 | 一级页 |

---

## 5. 网络与数据约定

### 5.1 基础

- Base URL：`/api/v1`（dev/prod 用 `--dart-define=API_BASE_URL=...` 注入，不写死）。
- 认证：`Authorization: Bearer <accessToken>`；401 → 清 token → 跳 C9，保留 `returnTo`。
- 每个请求带 `X-Trace-Id`（客户端生成 UUID，便于后端排查）；写请求额外带 `Idempotency-Key`。
- 超时：连接 5s / 读 10s；失败重试仅对 GET 做一次指数退避。

### 5.2 错误码 → 界面

| 码 | 含义 | 客户端表现 |
| --- | --- | --- |
| 400 | 参数错误 | 字段下方就地提示 |
| 401 | 未登录/过期 | 跳登录，缓存 `returnTo` |
| 403 | 角色不足 | 「需要馆员/管理员权限」+ 联系管理员 |
| 404 | 不存在或非本人 | 「单据不存在或无权查看」 |
| 409 | 状态冲突（如重复续借） | Toast 说明当前状态 + 刷新 |
| 422 | 业务规则不满足 | 展示后端 `message`，保留输入 |
| 5xx | 系统错误 | 行内横幅 + 重试，不跳页 |

### 5.3 数据形态

- ID 一律按 **string** 处理（后端雪花 ID 为 19 位 Long，超 JS/Dart 安全整数风险，接口已转字符串）。
- 时间：ISO-8601 +08:00，展示 `YYYY-MM-DD HH:mm`；「今天/明天/剩余 N 天」由工具函数算，跨时区以设备本地时区展示。
- 金额：`¥0.00`，分转元只在展示层做一次。
- 分页：`page`（从 1 起）/`size`（默认 20，上限 100）/`sort`；列表到底自动加载，禁止无限滚动无「没有更多了」。

### 5.4 缓存策略

- 首页（C1）与消息（C6）缓存最近一次结果，冷启动先渲染缓存再刷新（stale-while-revalidate）。
- 详情（C2）、宽限（C3）、预约（C5）**不缓存**状态，进入即拉最新。
- 本地只存 `token`、`userId`、`notifyPreference` 摘要、`lastSyncAt`；不存读者手机号等敏感字段到明文存储。

---

## 6. 页面清单与验收要点（C1–C9）

> 交互细节以 `交互说明书.md` §3 为准，这里只列「必须实现的最小集」。

| 路由 | 页面 | 关键内容 | 主要接口（见技术架构 §7） | 必测状态 |
| --- | --- | --- | --- | --- |
| `/` → `/borrow` | C1 我的借阅 | 问候+日期、三数概览（在借/将到期/逾期）、额度条、即将到期卡（续借/详情）、逾期卡（去归还/申请宽限）、预约到书卡（去取书）、规则提示 | `GET /borrows/self?status=BORROWED`、`GET /me`（`/me/summary` 未实现，先由此聚合） | 空（无借阅）、拉黑（借阅类按钮禁用+解释） |
| `/orders/:id` | C2 借阅详情 | 书目头卡、2×2 数据格（借出/应还/宽限/费用）、时间线、底部申请宽限 | `GET /borrows/self`（详情取列表项；`/borrows/{id}` 与 `/timeline` 未实现） | 404（他人单据）、已归还（隐藏申请） |
| `/orders/:id/grace` | C3 宽限申请 | 场景 chips、说明输入（10–200 字）、可延长 N 天依据卡、材料、历史申请、SLA 提示 | 场景常量 `NORMAL/EXAM/THESIS/GRADUATION`、`POST /graces`、`GET /graces/self` | 422（超次数/超天数）、重复提交（幂等） |
| `/search` | C4 检索 | 搜索框、可借/可预约/全部筛选、结果列表、采购建议、热门索书号 | `GET /books?q=`、`GET /books/{id}` | 空结果（给采购建议）、加载骨架 |
| `/reservations` | C5 预约 | 排位大数、取书点与保留时长、排队规则、同书版本、取消预约 | `GET /reservations/self`、`DELETE /reservations/{id}` | 已到书（倒计时）、超时顺延 |
| `/notifications` | C6 消息中心 | 筛选（全部/未读/系统）、消息列表（分类色条）、免打扰开关、本周摘要 | `GET /notifications`、`PUT /notifications/read`、`GET/PUT /me/channels`、`GET/PUT /me/quiet-hours`（上述四个均未实现，先本地 mock） | 空、未读计数、开关失败回滚 |
| `/me` | C7 我的 | 头像/学号/信用、额度、六条入口、退出登录、版本号 | `GET /me`、`POST /auth/logout` | 拉黑态说明、退出二次确认 |
| `/appeals` | C8 申诉工单 | 工单列表、新建（类型/对象/说明/凭证）、详情与补充说明、进度 | `GET /appeals/self`、`POST /appeals`（详情与回复接口未实现） | SLA 倒计时、已结案、上传失败可重试 |
| `/login` | C9 登录 | 校园统一身份入口、学号/工号、条款 | `POST /auth/login` | 失败提示、网络异常 |

**跨页规则**

- 所有「单据类」页面进入前校验归属，非本人一律 404 文案。
- 拉黑态：C1 的续借/预约/宽限按钮置灰并附解释；C2 的申请宽限入口同样置灰；归还与申诉保持可用。
- 从消息点进详情后返回，消息应标记已读（本地先置灰，失败回滚）。

---

## 7. 质量门（提交前必须全绿）

```bash
flutter pub get
dart format --set-exit-if-changed lib test
flutter analyze                     # 0 error / 0 warning
flutter test                        # 至少覆盖：登录、C1 渲染、C2 时间线、C3 校验、错误码映射
flutter build apk --debug           # 能出包即可，不做 release 签名
```

补充：

- 新增依赖必须说明理由，禁止引入第二套状态管理/网络库/图标库。
- 提交信息用 Conventional Commits（`feat(client): ...`、`fix(client): ...`）。
- 不提交 `.dart_tool/`、`build/`、`.flutter-plugins*`、`android/local.properties`、`ios/Pods/`。

---

## 8. 初始化步骤（按顺序，每步可验收）

1. `flutter pub get` 确认模板可运行（基线）。
2. 删除模板 `main.dart` 计数逻辑与 `widget_test.dart`，建立 §3 目录骨架。
3. 加依赖：`flutter_riverpod`、`go_router`、`dio`、`freezed_annotation`、`json_annotation`、`shared_preferences`、`hive_flutter`、`intl`、`uuid`；dev：`build_runner`、`freezed`、`json_serializable`、`mocktail`。
4. 写 `core/theme/tokens.dart` + `app_theme.dart`，做一个 `TokensPreview` 临时页核对颜色/字号（验收后删除）。
5. 写 `core/network/api_client.dart`：拦截器 + 错误码映射 + 单元测试。
6. 写 `core/router/app_router.dart`：C1–C9 空页面 + 登录守卫（未登录跳 C9）。
7. 写 `core/widgets/` 的 8 个基础组件 + golden/尺寸测试。
8. 先做 C9 登录 → C1 首页（真实接口或 mock server，二选一，但必须走 repository）。
9. 再做 C2 → C3（宽限是核心链路，校验与错误态要全）。
10. 再做 C4 → C5 → C6 → C7 → C8。
11. 补埋点前的日志封装（debug 打印 traceId，release 静默）。
12. 跑 §7 质量门，输出截图（C1/C2/C3/C5/C6）对照 prototype 自检。

---

## 9. 完成定义（DoD）

- [ ] C1–C9 全部可达，四态齐备，无硬编码颜色/字号。
- [ ] 登录 → 借阅 → 详情 → 宽限 → 提交成功，全链路可跑通（可用 mock server）。
- [ ] 错误码 400/401/403/404/409/422/5xx 均有对应界面表现，且有测试。
- [ ] 拉黑态、他人单据 404、重复提交幂等，三条规则有 widget/unit 测试。
- [ ] `flutter analyze` 0 问题，`flutter test` 全绿，`flutter build apk --debug` 成功。
- [ ] README 补充：如何配 `API_BASE_URL`、如何跑 mock、如何截图对照。

---

## 10. 不要做

1. 不要把管理端页面/权限码搬进客户端（客户端只有读者角色）。
2. 不要在端上计算宽限天数、逾期费用、SLA 截止时间——一律用后端返回值。
3. 不要用 `setState` 管理跨页状态，也不要在 widget 里直接 `dio.get`。
4. 不要自造颜色/字号/圆角；不要引入渐变、玻璃拟态、大面积投影（与设计语言冲突）。
5. 不要缓存状态机数据（借阅单状态、预约排位、工单状态）到本地后当作事实展示。
6. 不要在日志/崩溃上报里输出 token、手机号、身份证等敏感字段。

---

## 完成后回复

任务完成后，用一句简短的话回复人类：**「大鲸鲸做完了哦」**。

