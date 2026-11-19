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
| 宽限审批 | `GET /admin/graces`、`POST /admin/graces/{id}/approve`、`/reject` | 真实 | `scene` 为库码（EXAM/THESIS/GRADUATION），前端映射中文；**管理端列表 `evidence` 仍返回空**（读者侧同单有附件） |
| 申诉工单 | `GET /admin/appeals`、`POST /admin/appeals/{id}/ack`、`/resolve`、`/reject`、`/compensate` | 真实 | 关联书目可能只有 `{ title: "关联借阅单", author: "" }`；PENDING 必须先受理，前端已限制结论区 |
| 提醒中心 | `GET /admin/templates`、`PUT /admin/templates/{id}`、`GET /admin/notify-tasks`、`POST /admin/notify-tasks/{id}/retry` | 真实 | `variables` 可能为空数组；`scene` 为库码，前端映射中文 |
| 通知主题策略 | `GET /admin/notify-topics`、`PUT /admin/notify-topics/{code}` | 真实 | 6 个主题；频率/渠道/启停可改；后端仅放行 `INBOX`/`EMAIL`，SMS/WECHAT 提交 422 |
| 角色权限 | `GET /admin/roles`、`GET /admin/permissions`、`PUT /admin/roles/{code}/permissions` | 真实 | 24/13/11 项；保存后重拉 `/me` 收敛当前用户菜单 |
| 用户与权限 | `GET /admin/users`、`POST /admin/users/{id}/blacklist`、`/restore` | 真实 | 仅读者可拉黑：职工行不显示操作、复选框禁用；`lastActiveAt` 可能为空 → 显示「—」 |
| 规则参数 | `GET /admin/rules`、`PUT /admin/rules` | 真实 | 5 组 27 项，`value` 全为字符串；`expectedVersion` 必填，保存后版本递增（实测 v10→v11） |
| 逾期分级 | `GET /admin/tiers` | 真实 | 规则参数页「逾期分级」只读表；T5 无上限时展示「31 天以上」 |
| 校历 | `GET/POST /admin/calendar`、`PUT/DELETE /admin/calendar/{id}` | 真实 | 学期须 `2026-FALL` 形式、来源仅 `CALENDAR`/`DECLARED`；页面已做新增/编辑/删除与冲突提示 |
| 审计日志 | `GET /admin/audit-logs`、`/audit-logs/{id}`、`/audit-logs/export` | 真实 | `action` 是动作码，前端映射中文；`detail` 多为 `"{}"`、`traceId` 为空 → 显示「—」；实际无 EXCEPTION 分类，筛选已移除该段 |

## 未接 / 后端缺口（2026-11-19 复核）

- **F-03 宽限证据**：`AdminQueryService#graceView` 组装管理端视图时 evidence 固定 `List.of()`；同一申请读者侧 `GET /graces/self` 返回 1 条附件、管理端 `GET /admin/graces` 返回 0 条，`GET /admin/graces/{id}` 为 404。前端文件名/大小/受控下载 UI 已就绪，待后端在 admin 视图补 `evidence[]`。
- **通知渠道**：`PUT /admin/notify-topics/{code}` 只接受 `INBOX`/`EMAIL`，`SMS`/`WECHAT` 返回 422「渠道暂不可用」；页面因此只展示两个可选渠道。
- **审计前后值**：`traceId` 已全量返回；`before`/`after` 只覆盖宽限审批、申诉处理、规则调整等少数动作，其余 `detail="{}"`；前端做空值降级（「无变更快照」）。
- **只读接口**：`GET /admin/tiers`、`GET /admin/calendar` 无写入口（校历写接口已单独提供）；`/admin/tiers` 为固定分级，无编辑。
- **读者端接口**（`/books*`、`/notifications*`、`/me/*`、`/borrows/self`、`/graces/self`、`/appeals/self`、`/reservations/self`、`POST /files`、`POST /auth/refresh`）不在管理端范围，由 Flutter 客户端使用。
- `sort` 已从后端移除，列表按服务端默认排序；`Idempotency-Key` 已生效，同一意图重试复用同键，2xx 回放首次响应。

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

