# 阅有期 · 管理台（readdue-admin）

馆藏流通后台管理端。React 18 + Vite 5 + TypeScript(strict) + Tailwind 3.4 + shadcn/ui（new-york），
对接本机 Spring Boot 后端 `http://127.0.0.1:8125`（`context-path=/api`）。

## 快速开始

```bash
pnpm install
pnpm dev            # http://127.0.0.1:5173
pnpm build          # tsc -b && vite build
pnpm typecheck
```

演示账号（直接登录真实后端，密码均为 `123456`）：

| 账号 | 密码 | 角色 | 可访问 |
| --- | --- | --- | --- |
| `ADMIN001` | `123456` | 管理员（王敏） | 全部页面 |
| `LIB001` | `123456` | 馆员（李倩） | 除「用户与权限」「规则参数」「审计日志」外的页面 |

## 环境变量

`.env.development` / `.env.example`：

| 变量 | 默认 | 说明 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | `/api/v1` | 所有请求前缀，走 Vite proxy |
| `VITE_PROXY_TARGET` | `http://127.0.0.1:8125` | dev 代理目标（后端 context-path 已含 `/api`） |
| `VITE_APP_TITLE` | `阅有期 · 管理台` | 文档标题 |

所有页面默认直连 `8125` 的真实接口：登录、工作台、宽限、申诉、预约、逾期、提醒、用户、规则、审计、借还均已按后端契约实现。

## OpenAPI

```bash
pnpm api:gen    # 拉取 http://127.0.0.1:8125/api/v3/api-docs → openapi.json → schema.d.ts
```

- 产物 `src/lib/api/openapi.json`、`src/lib/api/schema.d.ts` 已在 `.gitignore` 中（随后端演进重新生成）。
- 业务上仍使用 `src/lib/api/models.ts` 的手写模型（与设计文档字段一致），`schema.d.ts` 作为接口契约参考与类型核对依据。
- Vite proxy 已验证：`GET /api/v1/system/health` 经 5173 转发到 8125，返回 `code=0, mysql=UP, redis=UP, baseTables=26`。

## 目录

```
src/
  app/            router.tsx（createBrowserRouter + 权限守卫）、providers.tsx、layout/（Sidebar/Topbar/PageHeader/AppShell）、nav.ts
  components/     ui/（shadcn 源码）、common/（StatusPill/StatCard/SectionCard/SegmentedFilter/EmptyState/Can/Pager）
  features/       auth workbench circulation reservation overdue grace appeal notify iam rule audit
                  └ 每个域：api.ts（纯请求）、queries.ts（TanStack Query）、pages/、components/、labels.ts
  lib/            api/{client,endpoints,errors,models,types}、auth/{session,permissions}、format/{date,money}、utils.ts
  styles/         globals.css（设计 token，唯一颜色来源）
scripts/          fetch-openapi.mjs、gen-ui.mjs（shadcn registry 落盘）、real-shot.mjs（真实登录态 CDP 截图）
```

## 路由与权限

| 路由 | 页面 | 权限码 |
| --- | --- | --- |
| `/workbench` | 工作台 A1 | `workbench:read` |
| `/circulation` | 借还台 A2 | `borrow:return` |
| `/reservations` | 预约管理 A3 | `reservation:manage` |
| `/overdue` | 逾期看板 A4 | `overdue:read` |
| `/grace` | 宽限审批 A5 | `grace:approve` |
| `/appeals` | 申诉工单 A6 | `appeal:handle` |
| `/notify` | 提醒中心 A7 | `notify:manage` |
| `/users` | 用户与权限 A8 | `user:manage` |
| `/rules` | 规则参数 A9 | `rule:update` |
| `/audit` | 审计日志 A10 | `audit:read` |
| `/403` `/404` | 兜底页 | — |

三层校验：路由守卫（无权限跳 `/403`）→ 侧栏菜单按权限过滤 → `Can` 组件控制按钮显隐。

## 约定

- 颜色只来自 `src/styles/globals.css` 的 token（HSL 分量），组件内不写十六进制；语义色 `ink/mint/line/wash/sunken/warn/gold/danger/deep`。
- 请求统一走 `src/lib/api/client.ts`（注入 `Authorization`、`X-Trace-Id`、写操作 `Idempotency-Key`，401 清理会话并回登录）；
  数据读取与变更只用 TanStack Query，不要把 `fetch` 写进组件。
- 表格一律放在卡片（`SectionCard`）里；按钮高度 `sm=34px`、`md=40px`；状态用 `StatusPill`（mint/amber/red/slate）。
- 时间统一 ISO-8601 UTC 存取，展示转 `Asia/Shanghai`（`lib/format/date.ts`）；金额为字符串，展示走 `formatCNY`。

## 视觉自查

`scripts/real-shot.mjs` 用真实后端账号登录后截图（需先启动带 `--remote-debugging-port=9222` 的 Chrome）：

```bash
node scripts/real-shot.mjs /workbench shots/real-workbench.png
```

## 已知限制

- 后端未提供的能力（续借、校历写入、宽限证据附件、审计 traceId）在前端按空态展示，接口就绪后再接。
- 未做 dark mode（按设计稿只提供浅色纸白主题）。
- `dist` 单包约 742KB（gzip 235KB），后续可按路由拆包。

## 接口对接

各页面与 `/api/v1` 接口的对应关系、字段差异与后端缺口见 [docs/API-INTEGRATION.md](docs/API-INTEGRATION.md)。

