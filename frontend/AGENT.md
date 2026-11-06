# AGENT.md · 前端管理后台初始化指南

> 给接手本目录的 AI 代理：你负责把「阅有期」管理端（馆员 / 管理员用的 Web 后台）从零初始化成可运行、可联调、可持续加页面的骨架。
> 读者端是 Flutter，不在这里；本目录只做 React 管理端。与文档冲突时，以 `project/spec/` 与 `design/` 为准。

## 1. 先读什么（按顺序）

| 顺序 | 文件 | 只读这些节 |
| --- | --- | --- |
| 1 | `../requires/require_total.md` | §3 功能需求、§11 补充工程需求（§11.7 FR-11） |
| 2 | `../spec/技术架构.md` | §2 九上下文、§7 接口清单、§10 权限分级（权限码与矩阵）、§11 用户状态、§13 通知偏好 |
| 3 | `../../design/页面与接口分析.md` | 管理端 A1–A11 每页的模块、交互、接口；§6 原型入口、§7 偏好接口 |
| 4 | `../../design/ui/交互说明书.md` | 设计语言（token、字号、间距、状态）、画布操作与跳转约定 |
| 5 | `../../design/ui/prototype.html` | 视觉锚点：20 帧（读者端 C1–C9 + 管理端 A1–A11），`?shot=A1` 单帧截图 |
| 6 | `../PRD.md` | §6 决策记录（D-1…D-7） |

## 2. 不可协商的 11 条

1. **只做管理端**（馆员 + 管理员），不做读者端页面。
2. **视觉按锚点实现**：纸白底（`#FBFCF8`）+ 纯白卡片 + 墨色文字（`#334155`）+ 薄荷绿状态色（`#64C2A6`，只做状态与选中）+ 深绿主按钮（`#2A7159`）；不加渐变、不加投影、不加玻璃拟态。
3. **颜色只能来自主题变量**（`src/styles/tokens.css`），组件里禁止出现十六进制色值。
4. **表格一律在卡片内**（卡片自带头部标题 + 右侧说明），不要裸表格贴页面。
5. **按钮高度只有两档**：`sm=34px`（表格行内、工具条）、`md=40px`（表单底部主操作）。
6. **权限三层**：路由守卫 → 菜单/按钮显隐 → 请求 403 兜底提示；前端隐藏不等于安全，后端仍校验。
7. **所有请求走统一 client**（`src/lib/api`），不裸写 `fetch`；错误码统一映射成用户话术。
8. **数据请求只用 TanStack Query**，不把服务端数据塞进 Redux/Zustand。
9. **表单用 react-hook-form + zod**，校验文案中文，错误贴在字段下方。
10. **不做「以后可能要用」的抽象**：没有第二个页面用到之前，不抽 hooks/组件。
11. **基础组件一律 shadcn/ui**（底层 Radix）：不自研 Button/Card/Table/Dialog，不引第二套 UI 框架；主题只改 CSS 变量，组件里不写色值。

## 3. 技术栈

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Node | 20 LTS | 用 `pnpm`，锁 `pnpm-lock.yaml` |
| React | 18.3 | 函数组件 + hooks，不开 RSC |
| Vite | 5.x | `@vitejs/plugin-react-swc` |
| TypeScript | 5.5+ | `strict: true`，禁止 `any`（用 `unknown` + 收窄） |
| Tailwind | 3.4 | 设计 token 写进 `tailwind.config.ts` |
| shadcn/ui | 最新 CLI（new-york） | 只装用到的组件，源码进 `src/components/ui`；行为与可达性由 Radix 提供 |
| 图标 / 提示 | lucide-react / sonner | 唯一图标与 toast 体系，不混用 |
| TanStack Query | v5 | `queryKey` 用数组工厂 |
| React Router | 6.26 | `createBrowserRouter` + 路由守卫 |
| react-hook-form + zod | 最新 | `zodResolver` |
| dayjs | 1.11 | 时区插件，展示用 `Asia/Shanghai` |
| 测试 | Vitest + Testing Library + Playwright | 组件测 + 3 条冒烟 |

## 4. 目录结构

```
frontend/
├── AGENT.md
├── package.json
├── vite.config.ts
├── tailwind.config.ts
├── components.json            # shadcn 配置
├── src/
│   ├── main.tsx
│   ├── app/
│   │   ├── router.tsx          # 路由表 + 守卫
│   │   ├── providers.tsx       # QueryClient / Toaster / Theme
│   │   └── layout/             # Sidebar、Topbar、PageHeader、Breadcrumb
│   ├── features/
│   │   ├── auth/               # 登录、会话、登出
│   │   ├── workbench/          # A1 工作台
│   │   ├── circulation/        # A2 借还台
│   │   ├── reservation/        # A3 预约管理
│   │   ├── overdue/            # A4 逾期看板
│   │   ├── grace/              # A5 宽限审批
│   │   ├── appeal/             # A6 申诉工单
│   │   ├── notify/             # A7 提醒中心
│   │   ├── iam/                # A8 用户与权限
│   │   ├── rule/               # A9 规则参数
│   │   └── audit/              # A10 审计日志
│   ├── components/{ui,common}/ # ui=shadcn，common=DataTable/StatCard/StatusPill/ConfirmDialog
│   ├── lib/
│   │   ├── api/{client.ts,endpoints.ts,errors.ts}
│   │   ├── auth/{session.ts,permissions.ts}
│   │   └── format/{date.ts,money.ts}
│   └── styles/{tokens.css,globals.css}
└── tests/
```

`features/<域>` 内部固定四件：`api.ts`（请求）、`queries.ts`（queryKey + hooks）、`components/`、`pages/`。跨域复用放 `components/common`，不互相 import 对方的 `api.ts`。

## 5. 设计 token（写进 tailwind.config.ts）

| Token | 值 | 用途 |
| --- | --- | --- |
| `bg` | `#FBFCF8` | 页面底色（纸白） |
| `card` | `#FFFFFF` | 卡片、表格容器 |
| `ink` | `#334155` | 主文字、标题、主按钮、深色概览卡 |
| `ink2` | `#515D6F` | 次级正文（6.48:1） |
| `ink3` | `#6A7486` | 三级说明、时间、表头（4.57:1） |
| `line` / `line2` | `#E6E9E3` / `#EFF1EC` | 卡片描边 / 分隔线、进度条底 |
| `mint` | `#64C2A6` | 状态标签、选中态、进度、成功反馈（**禁止作正文色**，对纸白仅约 2:1） |
| `mint-deep` | `#2A7159` | 薄荷底上的文字、链接（对 `mint-soft` 5.19:1），深色按钮底 |
| `mint-soft` | `#E7F5EF` | 选中行底、成功标签底 |
| `wash` / `sunken` | `#F6F8F3` / `#F0F2EC` | 表头、次级底、分段控件槽 |
| `warn` / `warn-soft` | `#8B5A12` / `#FBF0DA` | 将到期、待处理 |
| `gold` | `#C08A2E` | 排位、额度条 |
| `danger` / `danger-soft` | `#A33A22` / `#FBE9E3` | 逾期、驳回、失败 |
| `dark` | `#334155` | 深色汇总卡，等同 `ink`（仅工作台允许一处） |

排版：页面标题 20/700；卡片标题 13/700；正文 13；辅助 11；数字指标 30/700；表格行高 44（紧凑）或 48（常规）；卡片圆角 12（小卡）/16（大卡）；间距一律 4 的倍数。

## 6. 路由与页面

| 路由 | 页面 | 权限码 |
| --- | --- | --- |
| `/login` | 登录（工号 + 密码，错误 401 提示） | — |
| `/` | 重定向 `/workbench` | — |
| `/workbench` | A1 工作台：四指标卡、宽限待裁决表、今日到期提醒、逾期分层、今日流水 | `workbench:read` |
| `/circulation` | A2 借还台：扫码/学号输入、读者卡、当前单据、今日汇总 | `borrow:return` |
| `/reservations` | A3 预约管理：队列、到书通知、顺延 | `reservation:manage` |
| `/overdue` | A4 逾期看板：分层指标、最久逾期、近 7 天新增、费用 | `overdue:read` |
| `/grace` | A5 宽限审批：待裁决表 + 右侧详情（说明、材料、建议、裁决） | `grace:approve` |
| `/appeals` | A6 申诉工单：工单表 + 详情时间线 + 内部备注 | `appeal:handle` |
| `/notify` | A7 提醒中心：模板表、投递记录、渠道健康、免打扰策略 | `notify:manage` |
| `/users` | A8 用户与权限：用户表、拉黑/恢复/停用、角色权限开关 | `user:manage` |
| `/rules` | A9 规则参数：六项参数、变更预览、发布 | `rule:update` |
| `/audit` | A10 审计日志：筛选、日志表、导出 | `audit:read` |

## 7. 权限实现

- 登录后 `GET /api/v1/me` 返回 `{user, role, permissions[]}`，存内存 + `sessionStorage`（不存 localStorage 的 token 之外内容）。
- `usePermission('grace:approve')` 控制按钮显隐；无权限的菜单不渲染（不要只置灰）。
- 路由守卫：未登录跳 `/login?redirect=`；有登录无权限渲染 403 页（带「返回工作台」）。
- 请求层统一处理 401（清会话跳登录）、403（toast「没有权限」）、404（页面级空态，不弹错）。

## 8. API 约定

- Base：`/api/v1`，dev 用 Vite proxy 指向后端，生产 Nginx 同源。
- 头：`Authorization: Bearer <jwt>`、`X-Trace-Id`（前端生成 uuid）、写操作带 `Idempotency-Key`。
- 分页：`params: {page,size,sort}`；返回 `{items,total,page,size}`；列表页 `size` 默认 20。
- 错误体：`{code,message,traceId,details?}`；`errors.ts` 维护 code → 中文话术映射，未命中用兜底文案 + traceId。
- 时间字符串 ISO-8601（UTC），展示前 `dayjs.utc(v).tz('Asia/Shanghai')`。
- 金额 `BigDecimal` 序列化为字符串，前端只做展示格式化，不做浮点运算。

## 9. 数据层规范

- `queryKey` 工厂：`qk.grace.list(params)`、`qk.grace.detail(id)`；禁止散字符串。
- 写操作成功后 `invalidateQueries` 对应列表 + 详情；工作台指标需同时失效 `qk.workbench.summary()`。
- 轮询只在必需处（SLA 倒计时用 30s `refetchInterval`），其余手动刷新。
- 乐观更新只用于「开关类」（权限开关、免打扰），失败回滚并 toast。
- 列表页统一用 `common/DataTable`：列定义、空态、加载骨架、行点击、批量选择都在这里，不各页重写。

## 10. 交互规范（对齐 prototype.html 的 A1–A11）

- 加载：骨架屏（表格 5 行、卡片 4 块），不用全屏 spinner。
- 空态：一句话说明 + 主操作（如「暂无待裁决，去看看逾期看板」）。
- 错误态：卡片内错误条 + 重试按钮，不清空整页。
- 危险操作（拉黑、驳回、注销、发布规则）必须二次确认弹窗，写清后果与是否可撤销。
- SLA 倒计时：<1h 用 `warn`，已超时用 `danger` 并显示「已超时」，不只靠颜色。
- 表格行内操作最多 2 个，其余进「更多」；批量操作在表头出现。
- 保存类操作：按钮 loading + 防重复提交（`Idempotency-Key` 复用同一个键）。

## 11. shadcn/ui 使用规范（必读）

管理端所有基础组件**必须**来自 shadcn/ui，不自研 Button/Card/Table/Dialog，也不引入 Ant Design、MUI、Chakra 等第二套 UI 框架。

### 11.1 初始化

```bash
pnpm dlx shadcn@latest init
```

`components.json` 固定为：

```json
{
  "style": "new-york",
  "rsc": false,
  "tsx": true,
  "tailwind": { "config": "tailwind.config.ts", "css": "src/styles/globals.css", "baseColor": "neutral", "cssVariables": true },
  "aliases": { "components": "@/components", "utils": "@/lib/utils", "ui": "@/components/ui", "hooks": "@/hooks" }
}
```

组件只通过 CLI 添加，不手抄源码、不从旧项目复制：

```bash
pnpm dlx shadcn@latest add button card table dialog alert-dialog sheet tabs select switch badge input textarea label form dropdown-menu popover tooltip skeleton separator avatar progress
```

### 11.2 主题：把 shadcn 变量映射到项目 token

shadcn 的颜色全部走 CSS 变量。**只改 `globals.css` 里的变量值**，让它们等于设计 token，组件代码里不出现任何十六进制色值：

```css
:root {
  --background: 75 40% 98%;      /* #FBFCF8 纸白 */
  --foreground: 215 25% 27%;     /* #334155 墨色 */
  --card: 0 0% 100%;             /* #FFFFFF */
  --card-foreground: 215 25% 27%;
  --primary: 160 46% 30%;        /* #2A7159 主操作（mint-deep，白字 4.6:1） */
  --primary-foreground: 0 0% 100%;
  --secondary: 154 41% 93%;      /* #E7F5EF 浅底 */
  --muted: 90 12% 90%;           /* #E6E9E3 */
  --muted-foreground: 219 12% 47%; /* #6A7486 */
  --accent: 154 41% 93%;
  --destructive: 12 65% 39%;     /* #A33A22 */
  --border: 90 12% 90%;          /* #E6E9E3 */
  --input: 90 12% 90%;
  --ring: 160 46% 30%;
  --radius: 0.75rem;             /* 12px，大卡用 rounded-2xl */
}
```

- `warn`（将到期 `#8B5A12/#FBF0DA`）与 `gold`（排位 `#C08A2E`）shadcn 没有对应语义，在 `tailwind.config.ts` 的 `colors` 里扩 `warn`/`warn-soft`/`gold`，通过 `<Badge className="bg-warn-soft text-warn">` 用。
- **不做 dark mode**：锚点只有纸色一套，`darkMode` 配置保留但不启用，不写 `dark:` 变体。
- 圆角只允许 `rounded-xl`(12) 与 `rounded-2xl`(16)，其余值不出现。

### 11.3 组件 → 页面映射

| 页面 | 主要 shadcn 组件 |
| --- | --- |
| A1 工作台 | `Card`（指标卡）、`Table`、`Progress`（3px 进度条）、`Badge`、`Avatar` |
| A2 借还台 | `Input`（扫码框，大字号）、`Card`、`Button`、`Alert`（异常处理条） |
| A3 预约管理 | `Table`、`Tabs`、`Badge`、`DropdownMenu`（行内操作） |
| A4 逾期看板 | `Card`、`Table`、`Progress`、`Tooltip`（分层说明） |
| A5 宽限审批 | `Table`、`Sheet`（右侧详情，窄屏改 `Dialog`）、`Textarea`、`RadioGroup`、`AlertDialog`（驳回确认） |
| A6 申诉工单 | `Table`、`Card`、`Textarea`（内部备注）、`AlertDialog` |
| A7 提醒中心 | `Table`、`Switch`（模板启停）、`Sheet`（编辑模板）、`Alert` |
| A8 用户与权限 | `Table`、`Input`（搜索）、`Tabs`（分段筛选）、`Switch`（权限开关）、`AlertDialog`（拉黑/注销） |
| A9 规则参数 | `Card`、`Table`、`Dialog`（编辑参数）、`Alert`（变更影响） |
| A10 审计日志 | `Table`、`Select`（筛选）、`Popover` + `Calendar`（时间范围）、`Button` |

通用件（`src/components/common`）只在 shadcn 之上做业务组合，不重写样式：

- `DataTable` = `Table` + 排序/分页/空态/骨架/行选择；
- `StatCard` = `Card` + 数字排版；
- `StatusPill` = `Badge` + 语义色（正常/已拉黑/逾期/已批准…）；
- `ConfirmDialog` = `AlertDialog` + 危险操作文案模板；
- `PageHeader` = 标题 + 说明 + 右侧主操作。

### 11.4 禁止事项

- 禁止修改 `src/components/ui/*` 的内部实现与 API；需要不同外观用 `className`/`variant`，需要不同行为在 `common` 里包一层。
- 禁止用 `div` 手搓弹窗、下拉、开关、Tab——Radix 已带焦点管理与 ARIA，手搓必丢键盘可达性。
- 禁止引入第二套 toast/表单/图标体系；`sonner` 作为唯一 toast，`lucide-react` 作为唯一图标。
- 禁止把 shadcn 组件再包一层同名组件（`MyButton` 包 `Button`），需要预设就写 `variant`。
- 禁止在 `components/ui` 里写业务逻辑与请求。

### 11.5 升级与体积

- 组件是源码，不是依赖：升级用 `pnpm dlx shadcn@latest diff <component>` 看差异后手动合并，不整体覆盖（会冲掉主题变量）。
- 只 `add` 用到的组件；每个组件的依赖是 ESM，Vite 会 tree-shake，不要为了「以后可能用」预装 40 个。
- 新增组件后在 `src/components/ui/README.md` 记一行：组件名、添加日期、是否改过样式。

## 12. 质量门

```bash
pnpm typecheck     # tsc --noEmit
pnpm lint          # eslint + prettier check
pnpm test          # vitest run
pnpm build         # 产物可启动
pnpm e2e           # playwright 三条：登录、宽限审批、用户拉黑
```

规则：`any` 0 处；`console.log` 0 处（用 `lib/log.ts`）；组件文件 ≤ 250 行，超了拆；每个 feature 至少 1 个组件测试。

## 13. 初始化顺序

1. `pnpm create vite frontend --template react-swc-ts`，接 Tailwind + shadcn `init`。
2. 写 `tokens.css` 与 `tailwind.config.ts`，做一个 `/styleguide` 页展示按钮/标签/表格/卡片四件套，确认与 `A1.png` 一致。
3. `lib/api/client.ts`：baseURL、token 注入、traceId、错误映射；`lib/auth` 会话与权限 hook。
4. 布局：Sidebar（分组 概览/业务/处理/系统）+ Topbar（页面标题、日期、主操作、头像），对齐锚点。
5. `common/DataTable`、`StatCard`、`StatusPill`、`ConfirmDialog` 四个基础件。
6. 按 A1 → A8 → A5 → A6 → A4 → A3 → A7 → A9 → A10 顺序做页面（先做数据最全的，能早暴露接口缺口）。
7. 每个页面完成即跑 `typecheck + lint + 组件测试`，不留到最后。
8. 联调：先用 MSW mock 顶住，接口就绪后逐个替换，mock 文件保留在 `tests/mocks`。
9. Playwright 三条冒烟，接 CI。
10. 写 `README.md`：起服务、配后端地址、跑测试、常见错误。

## 14. 完成定义（DoD）

- [ ] 10 个页面在 1440×900 下与锚点截图无明显偏差（间距、字号、卡片圆角）
- [ ] 五种状态齐全：加载 / 空 / 错误 / 无权限 / 成功
- [ ] 权限码生效：无权限用户看不到入口，直连 URL 得 403 页
- [ ] 所有写操作有二次确认或明确的不可逆提示
- [ ] `typecheck + lint + test + build` 全绿
- [ ] 无硬编码颜色、无裸 fetch、无 `any`

## 15. 不要做

- 不要做读者端、不要做营销页、不要引 UI 框架（Ant/MUI）与 shadcn 混用。
- 不要自己写日期/金额格式化，统一走 `lib/format`。
- 不要为 10 个页面写 10 套表格；先有 `DataTable` 再加页面。
- 不要用全局状态存列表数据；不要用 `useEffect` 拉数据。
- 不要为了「好看」加渐变、阴影、玻璃、动效堆叠；锚点是什么样就什么样。






---

## 完成后回复

任务完成后，用一句简短的话回复人类：**「大鲸鲸做完了哦」**。
