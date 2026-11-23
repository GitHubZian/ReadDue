import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ArrowRight, Check, Clock3, Download, FileText, Search, X } from "lucide-react";
import { toast } from "sonner";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { SegmentedFilter } from "@/components/common/SegmentedFilter";
import { StatusPill } from "@/components/common/StatusPill";
import { Pager } from "@/components/common/Pager";
import { useGraces, useApproveGrace, useRejectGrace } from "@/features/grace/queries";
import { fetchEvidenceBlob } from "@/features/grace/api";
import { graceSceneLabel, graceStatusMeta } from "@/features/grace/labels";
import { formatDateTime, slaRemaining } from "@/lib/format/date";
import { formatCNY } from "@/lib/format/money";
import { cn } from "@/lib/utils";
import type { GraceRecord, GraceStatus } from "@/lib/api/models";

const DAY_OPTIONS = [3, 6, 9];

function fileSize(size?: number) {
  if (!size) return "";
  return size >= 1024 * 1024
    ? `${(size / 1024 / 1024).toFixed(1)} MB`
    : `${Math.max(1, Math.round(size / 1024))} KB`;
}

function evidenceId(file: { id?: string; url?: string }) {
  if (file.id) return file.id;
  return file.url?.match(/\/files\/([^/?#]+)/)?.[1];
}

export function GracePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const urlId = searchParams.get("id") ?? undefined;
  const [status, setStatus] = useState<GraceStatus>(() => {
    const urlStatus = searchParams.get("status");
    return urlStatus === "APPROVED" || urlStatus === "REJECTED" ? urlStatus : "PENDING";
  });
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [selectedId, setSelectedId] = useState<string | undefined>(urlId);
  const [days, setDays] = useState(3);
  const [note, setNote] = useState("");
  const [confirm, setConfirm] = useState<"approve" | "reject" | null>(null);
  const invalidIdRef = useRef<string | undefined>(undefined);

  const query = useGraces({ status, keyword: keyword || undefined, page, size: 10 });
  const approve = useApproveGrace();
  const reject = useRejectGrace();

  const items = query.data?.items ?? [];
  const selected: GraceRecord | undefined = useMemo(
    () => items.find((item) => item.id === selectedId) ?? items[0],
    [items, selectedId],
  );

  // 工作台「裁决」带 ?id= 进来：先选中该单，而不是默认第一条。
  useEffect(() => {
    if (urlId) setSelectedId(urlId);
  }, [urlId]);

  // 深链失效（已归档/被筛掉）：明确提示后清掉 id，回落到列表。
  useEffect(() => {
    if (!urlId || selectedId !== urlId || !query.isSuccess || items.some((item) => item.id === urlId)) return;
    if (invalidIdRef.current === urlId) return;
    invalidIdRef.current = urlId;
    toast.error("该工单已归档或不存在", { description: "已回到宽限列表。" });
    const next = new URLSearchParams(searchParams);
    next.delete("id");
    setSearchParams(next, { replace: true });
    setSelectedId(undefined);
  }, [urlId, selectedId, items, query.isSuccess, searchParams, setSearchParams]);

  useEffect(() => {
    if (selected) setDays(selected.requestedDays >= 9 ? 9 : selected.requestedDays >= 6 ? 6 : 3);
  }, [selected?.id]);

  const pending = selected?.status === "PENDING";
  const sla = selected ? slaRemaining(selected.slaDeadline) : null;
  const overdueCount = items.filter((item) => slaRemaining(item.slaDeadline).overdue).length;

  const onApprove = () => {
    if (!selected) return;
    approve.mutate(
      { id: selected.id, days, note: note.trim() || undefined },
      { onSuccess: () => setNote("") },
    );
    setConfirm(null);
  };

  const onReject = () => {
    if (!selected) return;
    if (note.trim().length < 10) {
      toast.error("驳回需要填写不少于 10 字的说明", { description: "说明会进入审计日志并推送给读者。" });
      return;
    }
    setConfirm("reject");
  };

  const submitReject = () => {
    if (!selected) return;
    reject.mutate({ id: selected.id, note: note.trim() }, { onSuccess: () => setNote("") });
    setConfirm(null);
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="宽限审批"
        description="按剩余 SLA 排队；裁决后自动延长到期日并通知读者。"
      />

      <div className="grid grid-cols-1 gap-4 xl:h-[calc(100dvh-9.5rem)] xl:grid-cols-12">
        {/* 左：队列。窄列只承载「挑一条」，宽列留给材料与裁决。 */}
        <SectionCard
          className="flex min-h-0 flex-col xl:col-span-5"
          bodyClassName="flex min-h-0 flex-1 flex-col p-0"
          title={
            <span className="flex items-baseline gap-2">
              {status === "PENDING" ? "待裁决队列" : status === "APPROVED" ? "已通过" : "已驳回"}
              <span className="tabular text-[10px] font-normal text-ink-3">
                {query.data?.total ?? 0} 条
              </span>
            </span>
          }
          action={
            status === "PENDING" ? (
            <span className="flex items-center gap-2.5 text-[9px] text-ink-3">
              <span className="flex items-center gap-1">
                <i className="size-1.5 rounded-full bg-danger" /> 超时 {overdueCount}
              </span>
              <span className="flex items-center gap-1">
                <i className="size-1.5 rounded-full bg-warn" /> 临期
              </span>
            </span>
            ) : null
          }
        >
          <div className="space-y-2 border-b border-line-2 px-4 pb-3">
            <SegmentedFilter
              value={status}
              onChange={(value) => {
                setStatus(value);
                setPage(1);
                setSelectedId(undefined);
              }}
              items={[
                { value: "PENDING", label: "待裁决" },
                { value: "APPROVED", label: "已通过" },
                { value: "REJECTED", label: "已驳回" },
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
                placeholder="搜索读者、书目或工单号"
                className="h-[34px] pl-9 pr-3 text-[11px]"
              />
            </div>
          </div>

          <div className="scrollbar-slim min-h-0 flex-1 overflow-y-auto px-2 py-2">
            {query.isLoading ? (
              <div className="space-y-1.5 px-1">
                {Array.from({ length: 4 }).map((_, index) => (
                  <div key={index} className="h-[64px] animate-pulse rounded-lg bg-wash" />
                ))}
              </div>
            ) : items.length === 0 ? (
              <div className="flex h-full min-h-[160px] flex-col items-center justify-center gap-1 text-center">
                <p className="text-[11px] font-medium text-ink-2">队列是空的</p>
                <p className="text-[9.5px] text-ink-3">当前筛选下没有申请，切换状态或稍后再看。</p>
              </div>
            ) : (
              items.map((item) => {
                const itemSla = slaRemaining(item.slaDeadline);
                const urgent = itemSla.overdue
                  ? "border-l-danger"
                  : itemSla.minutes !== null && itemSla.minutes <= 60
                    ? "border-l-warn"
                    : "border-l-transparent";
                const active = selected?.id === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => setSelectedId(item.id)}
                    className={cn(
                      "mb-1 flex w-full items-center gap-2.5 rounded-lg border border-l-2 px-2.5 py-2 text-left transition-colors",
                      urgent,
                      active
                        ? "border-mint-deep/25 bg-mint-soft/50"
                        : "border-transparent hover:border-line hover:bg-wash/70",
                    )}
                  >
                    <span
                      className={cn(
                        "grid size-7 shrink-0 place-items-center rounded-full text-[10px] font-semibold ring-1",
                        itemSla.overdue
                          ? "bg-danger-soft text-danger ring-danger/20"
                          : "bg-card text-mint-deep ring-line",
                      )}
                    >
                      {item.reader.name.slice(0, 1)}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-baseline gap-1.5">
                        <p className="truncate text-[11.5px] font-semibold text-ink">
                          {item.reader.name}
                        </p>
                        <span className="tabular shrink-0 text-[8.5px] text-ink-3">{item.code}</span>
                      </div>
                      <p className="mt-0.5 truncate text-[9.5px] text-ink-3">
                        {item.book.title} · {graceSceneLabel(item.scene)}
                      </p>
                    </div>
                    <div className="shrink-0 text-right">
                      {item.status === "PENDING" ? (
                        <>
                          <p
                            className={cn(
                              "tabular text-[10px] font-semibold",
                              itemSla.overdue
                                ? "text-danger"
                                : itemSla.minutes !== null && itemSla.minutes <= 60
                                  ? "text-warn"
                                  : "text-ink-2",
                            )}
                          >
                            {itemSla.text}
                          </p>
                          <p className="text-[8.5px] text-ink-3">申请 {item.requestedDays} 天</p>
                        </>
                      ) : (
                        <>
                          <p
                            className={cn(
                              "text-[9.5px] font-medium",
                              item.status === "APPROVED" ? "text-mint-deep" : "text-danger",
                            )}
                          >
                            {graceStatusMeta[item.status].label}
                          </p>
                          <p className="text-[8.5px] text-ink-3">
                            {item.approvedDays ? `延长 ${item.approvedDays} 天` : "已归档"}
                          </p>
                        </>
                      )}
                    </div>
                  </button>
                );
              })
            )}
          </div>

          <div className="border-t border-line-2 px-4 py-2.5">
            <Pager
              page={page}
              size={10}
              total={query.data?.total ?? 0}
              onChange={(next) => {
                setPage(next);
                setSelectedId(undefined);
              }}
            />
          </div>
        </SectionCard>

        {/* 右：单一详情面板。材料滚动，裁决条固定在面板底部。 */}
        <SectionCard
          className="flex min-h-0 flex-col xl:col-span-7"
          bodyClassName="flex min-h-0 flex-1 flex-col p-0"
        >
          {selected ? (
            <>
              <div className="border-b border-line-2 px-6 pb-4 pt-5">
                <div className="flex items-start justify-between gap-3">
                  <div className="flex min-w-0 items-center gap-3">
                    <span className="grid size-10 shrink-0 place-items-center rounded-full bg-mint-soft text-[14px] font-bold text-mint-deep">
                      {selected.reader.name.slice(0, 1)}
                    </span>
                    <div className="min-w-0">
                      <div className="flex items-center gap-2">
                        <h2 className="truncate text-[15px] font-bold text-ink">
                          {selected.code}
                        </h2>
                        <StatusPill tone={graceStatusMeta[selected.status].tone} dot>
                          {graceStatusMeta[selected.status].label}
                        </StatusPill>
                      </div>
                      <p className="mt-1 truncate text-[10px] text-ink-3">
                        {selected.reader.studentNo} · 《{selected.book.title}》 · {selected.reader.name}
                      </p>
                      <p className="mt-0.5 text-[9.5px] text-ink-3">
                        {selected.reader.college} · 提交于 {formatDateTime(selected.createdAt)}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <div className="scrollbar-slim flex min-h-0 flex-1 flex-col overflow-y-auto px-6 py-4">
                {sla && pending ? (
                  <div
                    className={cn(
                      "flex items-center justify-between rounded-lg border px-3.5 py-2.5",
                      sla.overdue
                        ? "border-danger/20 bg-danger-soft/60"
                        : sla.minutes !== null && sla.minutes <= 60
                          ? "border-warn/25 bg-warn-soft/60"
                          : "border-line-2 bg-wash/60",
                    )}
                  >
                    <span className="flex items-center gap-1.5 text-[9.5px] text-ink-3">
                      <Clock3 className="size-3.5" />
                      剩余 SLA
                    </span>
                    <span
                      className={cn(
                        "tabular text-[15px] font-bold",
                        sla.overdue
                          ? "text-danger"
                          : sla.minutes !== null && sla.minutes <= 60
                            ? "text-warn"
                            : "text-ink",
                      )}
                    >
                      {sla.text}
                    </span>
                  </div>
                ) : null}

                <dl className="mt-4 grid grid-cols-2 gap-x-6 gap-y-3.5">
                  {[
                    { label: "书目", value: selected.book.title },
                    { label: "场景", value: graceSceneLabel(selected.scene) },
                    { label: "申请天数", value: `${selected.requestedDays} 天` },
                    {
                      label: "当前滞纳金",
                      value: selected.fineBefore ? formatCNY(selected.fineBefore) : "—",
                    },
                  ].map((item) => (
                    <div key={item.label} className="min-w-0">
                      <dt className="text-[9px] uppercase tracking-wider text-ink-3">{item.label}</dt>
                      <dd className="mt-0.5 truncate text-[11px] font-medium text-ink">{item.value}</dd>
                    </div>
                  ))}
                </dl>

                <div className="mt-4 border-t border-line-2 pt-3.5">
                  <p className="text-[9px] uppercase tracking-wider text-ink-3">申请理由</p>
                  <p className="mt-1.5 text-[10.5px] leading-5 text-ink-2">{selected.reason}</p>
                </div>

                <div className="mt-4 border-t border-line-2 pt-3.5">
                  <div className="flex items-center justify-between">
                    <p className="text-[9px] uppercase tracking-wider text-ink-3">证据材料</p>
                    <span className="text-[9px] text-ink-3">{selected.evidence.length} 份</span>
                  </div>
                  {selected.evidence.length ? (
                    <div className="mt-1.5 flex flex-wrap gap-1.5">
                      {selected.evidence.map((file, index) => {
                        const name = file.name ?? file.label ?? `附件 ${index + 1}`;
                        const remote = file.url && /^https?:\/\//i.test(file.url) ? file.url : undefined;
                        const id = evidenceId(file);
                        const className =
                          "inline-flex items-center gap-1 rounded-md border border-line bg-card px-2 py-1 text-[9.5px] text-ink-2";
                        if (remote) {
                          return (
                            <a
                              key={`${name}-${index}`}
                              href={remote}
                              target="_blank"
                              rel="noreferrer"
                              className={`${className} hover:border-mint-deep/30 hover:text-mint-deep`}
                            >
                              <FileText className="size-3" /> {name}
                            </a>
                          );
                        }
                        if (!id) {
                          return (
                            <span key={`${name}-${index}`} className={className}>
                              <FileText className="size-3" /> {name}
                            </span>
                          );
                        }
                        return (
                          <button
                            key={`${name}-${index}`}
                            type="button"
                            className={`${className} hover:border-mint-deep/30 hover:text-mint-deep`}
                            onClick={async () => {
                              try {
                                const blob = await fetchEvidenceBlob(id);
                                const url = URL.createObjectURL(blob);
                                const anchor = document.createElement("a");
                                anchor.href = url;
                                anchor.download = name;
                                anchor.click();
                                URL.revokeObjectURL(url);
                              } catch {
                                toast.error("附件下载失败，请稍后重试");
                              }
                            }}
                          >
                            <FileText className="size-3" /> {name}
                            {file.size ? (
                              <span className="tabular text-ink-3">{fileSize(file.size)}</span>
                            ) : null}
                            <Download className="size-3" />
                          </button>
                        );
                      })}
                    </div>
                  ) : (
                    <p className="mt-1.5 text-[10px] text-ink-3">该申请没有上传材料。</p>
                  )}
                </div>

                <div className="mt-auto border-t border-line-2 pt-3.5">
                  <div className="flex items-center justify-between">
                    <p className="text-[9px] uppercase tracking-wider text-ink-3">流转记录</p>
                    <span className="text-[9px] text-ink-3">{selected.history.length} 条</span>
                  </div>
                  <ol className="mt-2 space-y-2">
                    {selected.history.map((entry, index) => (
                      <li key={`${entry.at}-${index}`} className="flex gap-2.5">
                        <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-mint-deep/50" />
                        <div className="min-w-0">
                          <p className="text-[10px] leading-4 text-ink-2">
                            <b className="font-semibold text-ink">{entry.actor}</b> {entry.text}
                          </p>
                          <p className="tabular text-[8.5px] text-ink-3">{formatDateTime(entry.at)}</p>
                        </div>
                      </li>
                    ))}
                  </ol>
                </div>
              </div>

              {pending ? (
                <div className="border-t border-line-2 bg-wash/40 px-6 py-3">
                  <div className="flex flex-wrap items-center gap-3">
                    <span className="text-[9px] uppercase tracking-wider text-ink-3">延长天数</span>
                    <div className="flex gap-1.5">
                      {DAY_OPTIONS.map((option) => (
                        <button
                          key={option}
                          type="button"
                          onClick={() => setDays(option)}
                          aria-pressed={days === option}
                          className={cn(
                            "tabular rounded-md border px-3 py-1 text-[11px] font-semibold transition-colors",
                            days === option
                              ? "border-mint-deep bg-mint-soft text-mint-deep"
                              : "border-line bg-card text-ink-2 hover:border-mint-deep/30",
                          )}
                        >
                          {option} 天
                        </button>
                      ))}
                    </div>
                    <span className="text-[9px] text-ink-3">读者申请 {selected.requestedDays} 天</span>
                    <span className="tabular ml-auto text-[9px] text-ink-3">{note.trim().length} / 200</span>
                  </div>

                  <Textarea
                    value={note}
                    onChange={(event) => setNote(event.target.value)}
                    placeholder="核对依据（驳回必填，≥10 字）：如已核对实习证明，同意延长 6 天。"
                    className="mt-2 min-h-[52px] resize-none text-[10.5px] leading-5"
                  />

                  <div className="mt-2 flex gap-2">
                    <Button size="sm" className="flex-1" disabled={approve.isPending} onClick={() => setConfirm("approve")}>
                      <Check className="size-3.5" />
                      通过并延长 {days} 天
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="text-danger hover:bg-danger-soft hover:text-danger"
                      disabled={reject.isPending || note.trim().length < 10}
                      onClick={onReject}
                    >
                      <X className="size-3.5" />
                      驳回
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="flex items-center gap-2 border-t border-line-2 bg-wash/40 px-6 py-3 text-[10.5px] text-ink-2">
                  <StatusPill tone={graceStatusMeta[selected.status].tone} dot>
                    {graceStatusMeta[selected.status].label}
                  </StatusPill>
                  {selected.approvedDays ? <span>延长 {selected.approvedDays} 天</span> : null}
                  <span className="text-ink-3">已归档；如需调整请从「申诉工单」发起二次核对。</span>
                </div>
              )}
            </>
          ) : (
            <div className="flex h-full min-h-[240px] flex-col items-center justify-center gap-1.5 px-6 text-center">
              <ArrowRight className="size-4 text-ink-3" />
              <p className="text-[11px] font-medium text-ink-2">选择一条申请</p>
              <p className="text-[9.5px] text-ink-3">左侧点选后，这里展示读者材料与裁决操作。</p>
            </div>
          )}
        </SectionCard>
      </div>

      <AlertDialog open={confirm !== null} onOpenChange={(open) => !open && setConfirm(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{confirm === "reject" ? "确认驳回这笔宽限？" : "确认通过这笔宽限？"}</AlertDialogTitle>
            <AlertDialogDescription>
              {selected
                ? `${selected.reader.name}（${selected.reader.studentNo}）·《${selected.book.title}》· 申请 ${selected.requestedDays} 天。`
                : ""}
              {confirm === "reject"
                ? "驳回后读者会收到通知，说明将写入审计日志。"
                : `通过后到期日延长 ${days} 天，并通知读者。`}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className={confirm === "reject" ? "bg-danger text-white hover:bg-danger/90" : undefined}
              disabled={approve.isPending || reject.isPending}
              onClick={confirm === "reject" ? submitReject : onApprove}
            >
              {confirm === "reject" ? "确认驳回" : "确认通过"}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
