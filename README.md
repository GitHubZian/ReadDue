# project · 代码工程（占位）

阅有期的实际代码放这里（目前还没有初始化）。

## 既定形态

- 模块化单体：Spring Boot 3.x + MySQL 8 + Redis 7 + RabbitMQ 3.13；前端 React 18 + Vite + shadcn/ui + TanStack Query，移动端 Flutter 3。
- 包根 `com.zian`，按 9 个限界上下文垂直切片：

```
com.zian.{iam,catalog,borrow,reservation,grace,notify,appeal,rule,audit}
        .{interfaces,application,domain,infrastructure}
com.zian.bootstrap
```

- 依赖方向：`interfaces → application → domain ← infrastructure`，跨上下文只走应用服务接口与领域事件，禁止直连 Mapper。
- 默认「一事务一聚合」；归还时 `BorrowOrder` + `BookCopy` 是唯一强一致例外，其余走 outbox。
- 持久化统一 MyBatis-Plus：`BaseMapper` 只在 infrastructure；`deleted BIGINT` 写本行主键（逻辑删除）；`version` 乐观锁冲突返回 409 `CONCURRENT_MODIFIED`。

## 对应文档（Obsidian）

- 需求：`Projects/阅有期/project/requires/require_total.md`
- 架构与规约：`.../project/spec/技术架构.md`
- 业务规则与验收：`.../project/spec/借阅与归还说明书.md`
- 建库脚本：`.../project/spec/schema.sql`（26 表 + 2 视图）
