# 接口对接说明

管理端已从演示数据切到真实后端：`http://127.0.0.1:8125/api/v1`，登录态为真实 JWT。
下表按页面列出所用接口与实测状态（2026-11-06 逐页核对）。

## 页面 → 接口

| 页面 | 接口 | 状态 | 备注 |
| --- | --- | --- | --- |
| 登录 | `POST /auth/login`、`GET /me` | 真实 | `expiresIn` 返回字符串（如 `"7200"`），前端已按数字解析 |
| 工作台 | `GET /admin/dashboard` | 真实 | stats/trend/rulesHit/notices/dueTodayItems/duty 全部可用 |
| 借还台 | `POST /borrows`、`POST /returns`、`GET /admin/circulation/today` | 真实 | 今日流水无 `channel` 字段，已移除「渠道」列；`operator` 为自由文本 |
| 预约管理 | `GET /admin/reservations`、`POST /admin/reservations/{id}/promote`、`/expire` | 真实 | promote 后返回 `readyAt`/`expiresAt`/`shelf` |
| 逾期看板 | `GET /admin/overdue`、`POST /admin/remind/batch` | 真实 | 后端按「同一读者 24h」去重，返回 `{ sent }`；提示以 sent 为准 |
| 宽限审批 | `GET /admin/graces`、`POST /admin/graces/{id}/approve`、`/reject` | 真实 | `scene` 为库码（EXAM/THESIS/GRADUATION），前端映射中文；`evidence` 恒为空数组 |
| 申诉工单 | `GET /admin/appeals`、`POST /admin/appeals/{id}/ack`、`/resolve`、`/reject`、`/compensate` | 真实 | 关联书目可能只有 `{ title: "关联借阅单", author: "" }`；PENDING 必须先受理，前端已限制结论区 |
| 提醒中心 | `GET /admin/templates`、`PUT /admin/templates/{id}`、`GET /admin/notify-tasks`、`POST /admin/notify-tasks/{id}/retry` | 真实 | `variables` 可能为空数组；`scene` 为库码，前端映射中文 |
| 用户与权限 | `GET /admin/users`、`POST /admin/users/{id}/blacklist`、`/restore` | 真实 | 仅读者可拉黑：职工行不显示操作、复选框禁用；`lastActiveAt` 可能为空 → 显示「—」 |
| 规则参数 | `GET /admin/rules`、`PUT /admin/rules` | 真实 | 5 组 27 项，`value` 全为字符串；`expectedVersion` 必填，保存后版本递增（实测 v10→v11） |
| 审计日志 | `GET /admin/audit-logs`、`/audit-logs/{id}`、`/audit-logs/export` | 真实 | `action` 是动作码，前端映射中文；`detail` 多为 `"{}"`、`traceId` 为空 → 显示「—」；实际无 EXCEPTION 分类，筛选已移除该段 |

## 未接 / 后端缺口

- `GET /admin/tiers`、`GET /admin/calendar`：后端有接口，前端暂未建页（校历无写接口）。
- 续借没有 HTTP 端点，借还台只做借出/归还。
- 宽限证据附件、审计 `traceId`/`before`/`after` 后端未落库，前端按空态处理。
- `sort` 参数后端接收但不生效，列表按服务端默认顺序；分页默认 `size=20`，页面各自传 8–12。
- `Idempotency-Key` 后端仅接收不去重，前端照发，重试语义仍以后端为准。

## 已处理的数据差异

1. 权限以登录返回的 `permissions` 为准：`can()` 不再用角色默认表兜底覆盖服务端结果（馆员曾因此误见「审计日志」）。
2. `expiresIn` 字符串 → `Number()` 解析后再算过期时间。
3. 宽限场景、通知场景、审计动作码、审计对象前缀、申诉分类均做码 → 中文映射，未知值原样展示。
4. 公告等级 `level` 实际为 `info|warning|danger|neutral`，类型已对齐。
5. 催还/启停模板/重试通知只保留 mutation 内的一处 toast，避免页面与 hook 重复提示。

## 安全与登录态

- 退出登录先调用 `POST /auth/logout`（后端吊销 Redis 中的 jti），再清本地会话；实测退出后同一 token 访问 `/me` 返回 401。
- 多标签页用 `storage` 事件同步会话，任一标签退出其余立即失效。
- 进入受保护路由时调用 `GET /me` 复核账号与权限，角色调整或拉黑后菜单/守卫即时收敛。
- 登录后的 `from` 参数只接受站内路径（`/` 开头且非 `//`）。
- 宽限证据链接只放行 `http(s)`，其余地址按文本展示。
- JWT 存在 localStorage，属于「同源 XSS 即可读」的取舍；当前代码无 `innerHTML`/`eval` 等注入点，若后续引入富文本需改为 HttpOnly Cookie。
- 演示账号（ADMIN001/LIB001，密码 123456）为本地联调预填，部署到非本机环境前必须移除。

