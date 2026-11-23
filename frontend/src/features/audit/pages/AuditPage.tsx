import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Copy, Download, Search } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { SegmentedFilter } from "@/components/common/SegmentedFilter";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { Pager } from "@/components/common/Pager";
import { useAuditLogs } from "@/features/audit/queries";
import { exportAuditLogs } from "@/features/audit/api";
import { formatDateTime } from "@/lib/format/date";
import { ROLE_LABEL } from "@/lib/auth/permissions";
import type { AuditCategory, AuditLog } from "@/lib/api/models";

const asRecord = (value: unknown): Record<string, unknown> =>
  value && typeof value === "object" && !Array.isArray(value)
    ? (value as Record<string, unknown>)
    : {};

const stable = (value: unknown) => JSON.stringify(value ?? null);

/** 只列真正变化的字段；旧数据没有快照时返回空数组。 */
function diffOf(before: unknown, after: unknown) {
  const prev = asRecord(before);
  const next = asRecord(after);
  return [...new Set([...Object.keys(prev), ...Object.keys(next)])]
    .filter((field) => stable(prev[field]) !== stable(next[field]))
    .map((field) => ({ field, before: prev[field], after: next[field] }));
}

const showValue = (value: unknown) =>
  value === undefined ? "—" : typeof value === "string" ? value : JSON.stringify(value);

const categoryMeta: Record<AuditCategory, { label: string; tone: "mint" | "amber" | "red" | "slate" }> = {
  APPROVAL: { label: "审批", tone: "mint" },
  RULE: { label: "规则变更", tone: "amber" },
  USER: { label: "用户操作", tone: "slate" },
  SYSTEM: { label: "系统", tone: "slate" },
};

const categoryInfo = (category: string) =>
  categoryMeta[category as AuditCategory] ?? { label: category, tone: "slate" as const };

/** 后端存动作码，表里展示中文；未收录的码原样显示。 */
const actionLabel = (action: string) =>
  ({
    AUTH_LOGIN: "登录",
    AUTH_LOGOUT: "退出登录",
    GRACE_APPROVE: "通过宽限",
    GRACE_REJECT: "驳回宽限",
    APPEAL_ACK: "受理申诉",
    APPEAL_RESOLVE: "撤销逾期",
    APPEAL_COMPENSATE: "补偿宽限",
    APPEAL_REJECT: "驳回申诉",
    RULE_UPDATE: "更新规则",
    USER_BLACKLIST: "拉黑用户",
    USER_RESTORE: "恢复用户",
    NOTIFY_REMIND_BATCH: "批量催还",
    NOTIFY_TEMPLATE_UPDATE: "更新模板",
    NOTIFY_RETRY: "重试通知",
    BORROW_CREATE: "借出登记",
    RETURN_CREATE: "归还登记",
    RESERVATION_PROMOTE: "预约到馆",
    RESERVATION_EXPIRE: "预约释放",
    CALENDAR_CREATE: "新增校历",
    CALENDAR_UPDATE: "更新校历",
    CALENDAR_DELETE: "删除校历",
    ROLE_PERMISSION_UPDATE: "更新角色权限",
    GRACE_APPLY: "提交宽限申请",
    NOTIFY_TOPIC_UPDATE: "更新主题策略",
    FILE_UPLOAD: "上传附件",
    CONSENT_GRANT: "同意隐私授权",
    CONSENT_REVOKE: "撤回隐私授权",
  })[action] ?? action;

const targetLabel = (target: string) => {
  const [prefix, ...rest] = target.split(" ");
  const mapped =
    (
      {
        USER: "用户",
        BORROW_ORDER: "借阅单",
        GRACE: "宽限",
        APPEAL: "申诉",
        RULE: "规则",
        NOTIFY_TASK: "通知任务",
        RESERVATION: "预约",
        BOOK: "书目",
        ACADEMIC_CALENDAR: "校历",
        SYS_ROLE: "角色",
        GRACE_APPLICATION: "宽限申请",
        NOTIFY_TOPIC: "通知主题",
        FILE: "附件",
        CONSENT: "授权记录",
      } as Record<string, string>
    )[prefix] ?? prefix;
  return [mapped, ...rest].join(" ");
};

export function AuditPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const urlKeyword = searchParams.get("keyword") ?? "";
  const [category, setCategory] = useState<AuditCategory | "ALL">("ALL");
  const [keyword, setKeyword] = useState(urlKeyword);
  const [page, setPage] = useState(1);
  const [detail, setDetail] = useState<AuditLog | null>(null);
  const [exporting, setExporting] = useState(false);

  // 顶栏搜索无匹配时会带 ?keyword= 跳到这里；地址变化时同步并重新查询。
  useEffect(() => {
    setKeyword(urlKeyword);
    setPage(1);
  }, [urlKeyword]);

  const query = useAuditLogs({
    category: category === "ALL" ? undefined : category,
    keyword: keyword || undefined,
    page,
    size: 12,
  });

  const download = async () => {
    setExporting(true);
    try {
      const blob = await exportAuditLogs({ category: category === "ALL" ? undefined : category });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = `readdue-audit-${new Date().toISOString().slice(0, 10)}.csv`;
      link.click();
      URL.revokeObjectURL(url);
      toast.success("导出已开始");
    } catch {
      toast.error("导出失败，请稍后重试");
    } finally {
      setExporting(false);
    }
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="审计日志"
        description="审批、规则、权限与异常统一留痕，可按分类、对象或关键字检索。"
        actions={
          <Button size="sm" variant="outline" disabled={exporting} onClick={download}>
            <Download className="size-3.5" />
            导出 CSV
          </Button>
        }
      />

      <SectionCard bodyClassName="px-6 pb-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SegmentedFilter
            value={category}
            onChange={(value) => {
              setCategory(value);
              setPage(1);
            }}
            items={[
              { value: "ALL", label: "全部" },
              { value: "APPROVAL", label: "审批" },
              { value: "RULE", label: "规则变更" },
              { value: "USER", label: "用户操作" },
            ]}
          />
          <div className="relative">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
            <Input
              value={keyword}
              onChange={(event) => {
                setKeyword(event.target.value);
                setPage(1);
              }}
              placeholder="搜索操作人、对象或 traceId"
              className="h-[34px] w-[260px] pl-9 pr-3 font-mono text-[10.5px]"
            />
          </div>
        </div>

        <Table className="mt-3">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="">时间</TableHead>
              <TableHead>操作人</TableHead>
              <TableHead>动作</TableHead>
              <TableHead>分类</TableHead>
              <TableHead>对象</TableHead>
              <TableHead className="text-right">traceId</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(query.data?.items ?? []).map((log) => (
              <TableRow
                key={log.id}
                className="cursor-pointer"
                onClick={() => setDetail(log)}
              >
                <TableCell className="tabular text-[10px] text-ink-3">
                  {formatDateTime(log.at)}
                </TableCell>
                <TableCell>
                  <p className="text-ink">{log.actor.name}</p>
                  <p className="text-[9px] text-ink-3">{log.actor.role}</p>
                </TableCell>
                <TableCell className="max-w-[220px] truncate text-ink-2">{actionLabel(log.action)}</TableCell>
                <TableCell>
                  <StatusPill tone={categoryInfo(log.category).tone}>
                    {categoryInfo(log.category).label}
                  </StatusPill>
                </TableCell>
                <TableCell className="max-w-[160px] truncate text-ink-3">{targetLabel(log.target)}</TableCell>
                <TableCell className="text-right font-mono text-[9px] text-ink-3">
                  {log.traceId ? log.traceId.slice(0, 12) : "—"}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!query.isLoading && (query.data?.items.length ?? 0) === 0 ? (
          <EmptyState
            title={
              keyword
                ? `未找到「${keyword}」`
                : category !== "ALL"
                  ? "当前分类下没有日志"
                  : "还没有审计日志"
            }
            description={
              keyword
                ? "检查单号是否完整（BC 开头 10 位），或换个关键词再试。"
                : category !== "ALL"
                  ? "切换到「全部」或换个分类再试。"
                  : "审批、规则变更与用户操作会实时记录在这里。"
            }
            action={
              keyword || category !== "ALL" ? (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setKeyword("");
                    setCategory("ALL");
                    setPage(1);
                    const next = new URLSearchParams(searchParams);
                    next.delete("keyword");
                    setSearchParams(next, { replace: true });
                  }}
                >
                  查看全部日志
                </Button>
              ) : undefined
            }
          />
        ) : null}

        <div className="mt-3">
          <Pager page={page} size={12} total={query.data?.total ?? 0} onChange={setPage} />
        </div>
      </SectionCard>

      <Sheet open={Boolean(detail)} onOpenChange={(open) => !open && setDetail(null)}>
        <SheetContent className="w-[420px] sm:max-w-[420px]">
          <SheetHeader>
            <SheetTitle className="text-[15px]">{detail ? actionLabel(detail.action) : ""}</SheetTitle>
            <SheetDescription className="text-[10.5px] text-ink-3">
              {detail ? formatDateTime(detail.at) : ""} · {detail?.actor.name}（
              {detail ? ((ROLE_LABEL as Record<string, string>)[detail.actor.role] ?? detail.actor.role) : ""}）
            </SheetDescription>
          </SheetHeader>

          {detail ? (
            <div className="mt-4 space-y-3 px-4 text-[10.5px]">
              <dl className="grid grid-cols-2 gap-2 rounded-xl bg-wash px-3 py-2.5">
                <div>
                  <dt className="text-[9px] text-ink-3">分类</dt>
                  <dd className="mt-0.5">{categoryInfo(detail.category).label}</dd>
                </div>
                <div>
                  <dt className="text-[9px] text-ink-3">对象</dt>
                  <dd className="mt-0.5 truncate">{targetLabel(detail.target)}</dd>
                </div>
                <div>
                  <dt className="text-[9px] text-ink-3">IP</dt>
                  <dd className="tabular mt-0.5">{detail.ip}</dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-[9px] text-ink-3">traceId</dt>
                  <dd className="mt-0.5 flex items-center gap-1">
                    <code className="truncate font-mono text-[9px]">{detail.traceId || "—"}</code>
                    {detail.traceId ? (
                      <Button
                        variant="ghost"
                        size="icon-xs"
                        title="复制 traceId"
                        onClick={() => {
                          navigator.clipboard
                            .writeText(detail.traceId)
                            .then(() => toast.success("traceId 已复制"))
                            .catch(() => toast.error("复制失败，请手动选择"));
                        }}
                      >
                        <Copy className="size-3" />
                      </Button>
                    ) : null}
                  </dd>
                </div>
              </dl>

              <div>
                <p className="text-[9px] font-semibold tracking-wider text-ink-3">描述</p>
                <p className="mt-1 leading-5 text-ink-2">
                  {detail.detail && detail.detail !== "{}" ? detail.detail : "—"}
                </p>
              </div>

              {detail.before || detail.after ? (
                (() => {
                  const rows = diffOf(detail.before, detail.after);
                  if (!rows.length) {
                    return (
                      <p className="text-[9.5px] text-ink-3">快照已记录，但没有字段发生变化。</p>
                    );
                  }
                  return (
                    <div>
                      <p className="text-[9px] font-semibold tracking-wider text-ink-3">
                        变更差异 · {rows.length} 项
                      </p>
                      <div className="mt-1 overflow-hidden rounded-lg border border-line">
                        <table className="w-full table-fixed text-[9.5px]">
                          <thead className="bg-wash text-left text-[9px] text-ink-3">
                            <tr>
                              <th className="w-[30%] px-2 py-1 font-medium">字段</th>
                              <th className="px-2 py-1 font-medium">变更前</th>
                              <th className="px-2 py-1 font-medium">变更后</th>
                            </tr>
                          </thead>
                          <tbody>
                            {rows.map((row) => (
                              <tr key={row.field} className="border-t border-line-2 align-top">
                                <td className="break-all px-2 py-1 font-mono text-[9px] text-ink-3">
                                  {row.field}
                                </td>
                                <td className="break-all px-2 py-1 text-ink-2">
                                  {showValue(row.before)}
                                </td>
                                <td className="break-all px-2 py-1 text-mint-deep">
                                  {showValue(row.after)}
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    </div>
                  );
                })()
              ) : (
                <p className="text-[9.5px] text-ink-3">无变更快照：旧记录只保留动作与对象。</p>
              )}
            </div>
          ) : null}
        </SheetContent>
      </Sheet>
    </div>
  );
}
