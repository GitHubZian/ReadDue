import { useEffect, useMemo, useState } from "react";
import { Check, Clock3, Download, FileText, Search, X } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { SegmentedFilter } from "@/components/common/SegmentedFilter";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
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
  const match = file.url?.match(/\/files\/([^/?#]+)/);
  return match?.[1];
}

export function GracePage() {
  const [status, setStatus] = useState<GraceStatus>("PENDING");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [selectedId, setSelectedId] = useState<string | undefined>();
  const [days, setDays] = useState(3);
  const [note, setNote] = useState("");

  const query = useGraces({ status, keyword: keyword || undefined, page, size: 10 });
  const approve = useApproveGrace();
  const reject = useRejectGrace();

  const items = query.data?.items ?? [];
  const selected: GraceRecord | undefined = useMemo(
    () => items.find((item) => item.id === selectedId) ?? items[0],
    [items, selectedId],
  );

  useEffect(() => {
    if (selected) setDays(selected.requestedDays >= 9 ? 9 : selected.requestedDays >= 6 ? 6 : 3);
  }, [selected?.id]);

  const pending = selected?.status === "PENDING";
  const sla = selected ? slaRemaining(selected.slaDeadline) : null;

  const onApprove = () => {
    if (!selected) return;
    approve.mutate(
      { id: selected.id, days, note: note.trim() || undefined },
      { onSuccess: () => setNote("") },
    );
  };

  const onReject = () => {
    if (!selected) return;
    if (note.trim().length < 10) {
      toast.error("驳回需要填写不少于 10 字的说明", { description: "说明会进入审计日志并推送给读者。" });
      return;
    }
    reject.mutate({ id: selected.id, note: note.trim() }, { onSuccess: () => setNote("") });
  };

  return (
    <div className="space-y-5">
      <PageHeader
        title="宽限审批"
        description="按剩余 SLA 排队，超时单置顶标记；裁决后自动延长到期日并通知读者。"
      />

      <div className="grid grid-cols-1 items-start gap-5 xl:grid-cols-12">
        <div className="xl:col-span-7">
          <SectionCard
            title="待裁决队列"
            description={`共 ${query.data?.total ?? 0} 条申请`}
            action={
              <span className="flex items-center gap-2 text-[9px] text-ink-3">
                <span className="flex items-center gap-1">
                  <i className="size-1.5 rounded-full bg-danger" /> 超时
                </span>
                <span className="flex items-center gap-1">
                  <i className="size-1.5 rounded-full bg-warn" /> ≤1h
                </span>
              </span>
            }
            bodyClassName="px-4 pb-4"
          >
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-line-2 pb-3">
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
                  className="h-[34px] w-[228px] pl-9 pr-3 text-[11px]"
                />
              </div>
            </div>

            <div className="mt-2 space-y-1">
              {query.isLoading
                ? Array.from({ length: 4 }).map((_, index) => (
                    <div key={index} className="h-[62px] animate-pulse rounded-xl bg-wash" />
                  ))
                : items.map((item) => {
                    const itemSla = slaRemaining(item.slaDeadline);
                    const active = selected?.id === item.id;
                    const urgent = itemSla.overdue
                      ? "border-l-danger"
                      : itemSla.minutes !== null && itemSla.minutes <= 60
                        ? "border-l-warn"
                        : "border-l-transparent";
                    return (
                      <button
                        key={item.id}
                        type="button"
                        onClick={() => setSelectedId(item.id)}
                        className={cn(
                          "flex w-full items-center gap-3 rounded-xl border border-l-2 px-3 py-2.5 text-left transition-colors",
                          urgent,
                          active
                            ? "border-mint-deep/25 bg-mint-soft/45"
                            : "border-transparent hover:border-line hover:bg-wash/70",
                        )}
                      >
                        <span
                          className={cn(
                            "grid size-8 shrink-0 place-items-center rounded-full text-[11px] font-semibold ring-1",
                            itemSla.overdue
                              ? "bg-danger-soft text-danger ring-danger/20"
                              : "bg-card text-mint-deep ring-line",
                          )}
                        >
                          {item.reader.name.slice(0, 1)}
                        </span>
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2">
                            <p className="truncate text-[12px] font-semibold text-ink">{item.reader.name}</p>
                            <span className="tabular text-[9px] text-ink-3">{item.code}</span>
                          </div>
                          <p className="mt-0.5 truncate text-[10px] text-ink-3">
                            {item.book.title} · {graceSceneLabel(item.scene)}
                          </p>
                        </div>
                        <div className="shrink-0 text-right">
                          <p
                            className={cn(
                              "tabular text-[10.5px] font-semibold",
                              itemSla.overdue
                                ? "text-danger"
                                : itemSla.minutes !== null && itemSla.minutes <= 60
                                  ? "text-warn"
                                  : "text-ink-2",
                            )}
                          >
                            {itemSla.text}
                          </p>
                          <p className="text-[9px] text-ink-3">申请 {item.requestedDays} 天</p>
                        </div>
                        <StatusPill tone={graceStatusMeta[item.status].tone}>
                          {graceStatusMeta[item.status].label}
                        </StatusPill>
                      </button>
                    );
                  })}
            </div>

            {!query.isLoading && items.length === 0 ? (
              <EmptyState
                title="队列是空的"
                description="当前筛选下没有宽限申请，切换状态或稍后再看。"
              />
            ) : null}

            <div className="mt-3">
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
        </div>

        <div className="xl:col-span-5">
          {selected ? (
            <div className="space-y-4 xl:sticky xl:top-4">
              <SectionCard
                title="申请详情"
                description={
                  <span className="flex flex-wrap items-center gap-2">
                    <code className="font-mono text-[9px] text-ink-3">{selected.code}</code>
                    <span className="text-[9.5px] text-ink-3">
                      提交于 {formatDateTime(selected.createdAt)}
                    </span>
                  </span>
                }
                action={
                  <StatusPill tone={graceStatusMeta[selected.status].tone} dot>
                    {graceStatusMeta[selected.status].label}
                  </StatusPill>
                }
              >
                <div className="flex items-center gap-3">
                  <span className="grid size-10 place-items-center rounded-full bg-mint-soft text-[14px] font-bold text-mint-deep">
                    {selected.reader.name.slice(0, 1)}
                  </span>
                  <div className="min-w-0">
                    <p className="text-[13px] font-semibold text-ink">
                      {selected.reader.name}
                      <span className="ml-1.5 text-[10px] font-normal text-ink-3">
                        {selected.reader.studentNo}
                      </span>
                    </p>
                    <p className="text-[10px] text-ink-3">{selected.reader.college}</p>
                  </div>
                  <div className="ml-auto flex items-center gap-1.5 rounded-lg bg-wash px-2 py-1 text-[9.5px] text-ink-3">
                    <FileText className="size-3" />
                    {selected.evidence.length} 份材料
                  </div>
                </div>

                {sla ? (
                  <div
                    className={cn(
                      "mt-4 flex items-center justify-between rounded-xl border px-3.5 py-2.5",
                      sla.overdue
                        ? "border-danger/20 bg-danger-soft/70"
                        : sla.minutes !== null && sla.minutes <= 60
                          ? "border-warn/25 bg-warn-soft/70"
                          : "border-line-2 bg-wash/70",
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

                <dl className="mt-4 grid grid-cols-2 gap-x-5 gap-y-3.5">
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
                      <dt className="text-[9px] font-medium uppercase tracking-wider text-ink-3">
                        {item.label}
                      </dt>
                      <dd className="mt-1 truncate text-[11px] font-medium text-ink">{item.value}</dd>
                    </div>
                  ))}
                </dl>

                <div className="mt-4 border-t border-line-2 pt-3">
                  <p className="text-[9px] font-medium uppercase tracking-wider text-ink-3">申请理由</p>
                  <p className="mt-1.5 text-[10.5px] leading-5 text-ink-2">{selected.reason}</p>
                </div>

                {selected.evidence.length ? (
                  <div className="mt-3 border-t border-line-2 pt-3">
                    <p className="text-[9px] font-medium uppercase tracking-wider text-ink-3">
                      证据材料
                    </p>
                    <div className="mt-1.5 flex flex-wrap gap-1.5">
                      {selected.evidence.map((file, index) => {
                        const name = file.name ?? file.label ?? `附件 ${index + 1}`;
                        const remote =
                          file.url && /^https?:\/\//i.test(file.url) ? file.url : undefined;
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
                  </div>
                ) : null}
              </SectionCard>

              <SectionCard title="流转记录" description={`${selected.history.length} 条状态变更`}>
                <ol className="space-y-3">
                  {selected.history.map((entry, index) => (
                    <li key={`${entry.at}-${index}`} className="flex gap-2.5">
                      <span className="mt-1.5 size-1.5 shrink-0 rounded-full bg-mint-deep/60" />
                      <div className="min-w-0">
                        <p className="text-[10.5px] leading-4 text-ink-2">
                          <b className="font-semibold text-ink">{entry.actor}</b> {entry.text}
                        </p>
                        <p className="tabular mt-0.5 text-[9px] text-ink-3">{formatDateTime(entry.at)}</p>
                      </div>
                    </li>
                  ))}
                </ol>
              </SectionCard>

              {pending ? (
                <SectionCard
                  title="裁决"
                  description="通过后自动延长到期日并通知读者；驳回需说明理由。"
                >
                  <div className="flex items-center justify-between">
                    <p className="text-[9px] font-medium uppercase tracking-wider text-ink-3">延长天数</p>
                    <span className="text-[9px] text-ink-3">读者申请 {selected.requestedDays} 天</span>
                  </div>
                  <div className="mt-2 grid grid-cols-3 gap-2">
                    {DAY_OPTIONS.map((option) => (
                      <button
                        key={option}
                        type="button"
                        onClick={() => setDays(option)}
                        aria-pressed={days === option}
                        className={cn(
                          "rounded-xl border py-3 text-center transition-colors",
                          days === option
                            ? "border-mint-deep bg-mint-soft text-mint-deep"
                            : "border-line bg-card text-ink-2 hover:border-mint-deep/30",
                        )}
                      >
                        <span className="tabular block text-[16px] font-bold">{option}</span>
                        <span className="text-[9px]">天</span>
                      </button>
                    ))}
                  </div>

                  <div className="mt-4 flex items-center justify-between">
                    <p className="text-[9px] font-medium uppercase tracking-wider text-ink-3">
                      备注 <span className="font-normal normal-case">（驳回必填，≥10 字）</span>
                    </p>
                    <span className="tabular text-[9px] text-ink-3">{note.trim().length} / 200</span>
                  </div>
                  <Textarea
                    value={note}
                    onChange={(event) => setNote(event.target.value)}
                    placeholder="例如：已核对实习证明，同意延长 6 天。"
                    className="mt-1.5 min-h-[76px] resize-none text-[11px] leading-5"
                  />

                  <div className="mt-4 flex gap-2">
                    <Button size="sm" className="flex-1" disabled={approve.isPending} onClick={onApprove}>
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
                </SectionCard>
              ) : (
                <SectionCard title="裁决结果">
                  <div className="flex items-center gap-2 text-[11px] text-ink-2">
                    <StatusPill tone={graceStatusMeta[selected.status].tone} dot>
                      {graceStatusMeta[selected.status].label}
                    </StatusPill>
                    {selected.approvedDays ? `延长 ${selected.approvedDays} 天` : null}
                  </div>
                  <p className="mt-2 text-[10.5px] leading-5 text-ink-3">
                    该工单已归档，如需调整请通过「申诉工单」发起二次核对。
                  </p>
                </SectionCard>
              )}
            </div>
          ) : (
            <SectionCard>
              <EmptyState title="选择一条申请" description="左侧列表点选后展示读者材料与裁决操作。" />
            </SectionCard>
          )}
        </div>
      </div>
    </div>
  );
}
