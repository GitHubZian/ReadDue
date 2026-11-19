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
import type { GraceRecord, GraceStatus } from "@/lib/api/models";

const DAY_OPTIONS = [3, 6, 9];

function fileSize(size?: number) {
  if (!size) return "";
  return size >= 1024 * 1024
    ? `${(size / 1024 / 1024).toFixed(1)} MB`
    : `${Math.max(1, Math.round(size / 1024))} KB`;
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
    reject.mutate(
      { id: selected.id, note: note.trim() },
      { onSuccess: () => setNote("") },
    );
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="宽限审批"
        description="按剩余 SLA 排队，超时单会在列表顶部标记；裁决结果即时通知读者。"
      />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-12">
        <div className="xl:col-span-7">
          <SectionCard bodyClassName="px-5 pb-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
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
                <Search className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
                <Input
                  value={keyword}
                  onChange={(event) => {
                    setKeyword(event.target.value);
                    setPage(1);
                  }}
                  placeholder="搜索读者、书目或工单号"
                  className="h-[34px] w-[240px] pl-8 text-[11px]"
                />
              </div>
            </div>

            <div className="mt-3 space-y-1.5">
              {items.map((item) => {
                const sla = slaRemaining(item.slaDeadline);
                const active = selected?.id === item.id;
                return (
                  <button
                    key={item.id}
                    type="button"
                    onClick={() => setSelectedId(item.id)}
                    className={
                      "flex w-full items-center gap-3 rounded-xl border px-3 py-2.5 text-left transition-colors " +
                      (active
                        ? "border-mint-deep/30 bg-mint-soft/50"
                        : "border-transparent hover:border-line hover:bg-wash/70")
                    }
                  >
                    <span className="grid size-7 shrink-0 place-items-center rounded-full bg-card text-[11px] font-semibold text-mint-deep ring-1 ring-line">
                      {item.reader.name.slice(0, 1)}
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="flex items-center gap-2">
                        <p className="truncate text-[12px] font-semibold text-ink">{item.reader.name}</p>
                        <span className="tabular text-[9.5px] text-ink-3">{item.code}</span>
                      </div>
                      <p className="truncate text-[10px] text-ink-3">
                        {item.book.title} · {graceSceneLabel(item.scene)}
                      </p>
                    </div>
                    <div className="shrink-0 text-right">
                      <p
                        className={
                          "tabular text-[10.5px] font-semibold " +
                          (sla.overdue
                            ? "text-danger"
                            : sla.minutes !== null && sla.minutes <= 60
                              ? "text-warn"
                              : "text-ink-2")
                        }
                      >
                        {sla.text}
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
                description={selected.code}
                action={
                  <StatusPill tone={graceStatusMeta[selected.status].tone} dot>
                    {graceStatusMeta[selected.status].label}
                  </StatusPill>
                }
              >
                <div className="flex items-center gap-3">
                  <span className="grid size-9 place-items-center rounded-full bg-mint-soft text-[13px] font-bold text-mint-deep">
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
                  <div className="ml-auto text-right">
                    <p className="text-[9px] text-ink-3">申请提交</p>
                    <p className="tabular text-[10px] text-ink-2">{formatDateTime(selected.createdAt)}</p>
                  </div>
                </div>

                <dl className="mt-3.5 grid grid-cols-2 gap-2 rounded-xl bg-wash/80 px-3 py-2.5 text-[10.5px]">
                  <div>
                    <dt className="text-[9px] text-ink-3">书目</dt>
                    <dd className="mt-0.5 truncate font-medium text-ink">{selected.book.title}</dd>
                  </div>
                  <div>
                    <dt className="text-[9px] text-ink-3">场景</dt>
                    <dd className="mt-0.5 text-ink-2">{graceSceneLabel(selected.scene)}</dd>
                  </div>
                  <div>
                    <dt className="text-[9px] text-ink-3">申请天数</dt>
                    <dd className="tabular mt-0.5 text-ink-2">{selected.requestedDays} 天</dd>
                  </div>
                  <div>
                    <dt className="text-[9px] text-ink-3">当前滞纳金</dt>
                    <dd className="tabular mt-0.5 text-ink-2">
                      {selected.fineBefore ? formatCNY(selected.fineBefore) : "—"}
                    </dd>
                  </div>
                </dl>

                <div className="mt-3">
                  <p className="text-[9px] font-semibold tracking-wider text-ink-3">申请理由</p>
                  <p className="mt-1 text-[10.5px] leading-5 text-ink-2">{selected.reason}</p>
                </div>

                {selected.evidence.length ? (
                  <div className="mt-2.5 flex flex-wrap gap-1.5">
                    {selected.evidence.map((file, index) => {
                      const name = file.name ?? file.label ?? `附件 ${index + 1}`;
                      // 读者填的外部链接不可信：只放行 http(s)，其余一律走受控下载
                      const remote = file.url && /^https?:\/\//i.test(file.url) ? file.url : undefined;
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
                      const id = file.id;
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
                ) : null}
              </SectionCard>

              <SectionCard title="流转记录">
                <ol className="space-y-2.5">
                  {selected.history.map((entry, index) => (
                    <li key={`${entry.at}-${index}`} className="flex gap-2.5">
                      <span className="mt-1 size-1.5 shrink-0 rounded-full bg-mint-deep/60" />
                      <div className="min-w-0">
                        <p className="text-[10.5px] text-ink-2">
                          <b className="font-semibold text-ink">{entry.actor}</b> {entry.text}
                        </p>
                        <p className="tabular text-[9px] text-ink-3">{formatDateTime(entry.at)}</p>
                      </div>
                    </li>
                  ))}
                </ol>
              </SectionCard>

              {pending ? (
                <SectionCard title="裁决" description="通过后自动延长到期日并通知读者">
                  <p className="text-[9px] font-semibold tracking-wider text-ink-3">延长天数</p>
                  <div className="mt-1.5 grid grid-cols-3 gap-2">
                    {DAY_OPTIONS.map((option) => (
                      <button
                        key={option}
                        type="button"
                        onClick={() => setDays(option)}
                        className={
                          "rounded-xl border py-2.5 text-center transition-colors " +
                          (days === option
                            ? "border-mint-deep bg-mint-soft text-mint-deep"
                            : "border-line bg-card text-ink-2 hover:border-mint-deep/30")
                        }
                      >
                        <span className="tabular block text-[15px] font-bold">{option}</span>
                        <span className="text-[9px]">天</span>
                      </button>
                    ))}
                  </div>

                  <p className="mt-3 text-[9px] font-semibold tracking-wider text-ink-3">
                    备注 <span className="font-normal">（驳回必填，≥10 字）</span>
                  </p>
                  <Textarea
                    value={note}
                    onChange={(event) => setNote(event.target.value)}
                    placeholder="例如：已核对实习证明，同意延长 6 天。"
                    className="mt-1.5 min-h-[72px] resize-none text-[11px]"
                  />
                  <div className="mt-1 flex justify-between text-[9px] text-ink-3">
                    <span className="flex items-center gap-1">
                      <Clock3 className="size-3" />
                      剩余 {slaRemaining(selected.slaDeadline).text}
                    </span>
                    <span className="tabular">{note.trim().length} / 200</span>
                  </div>

                  <div className="mt-3 flex gap-2">
                    <Button
                      size="sm"
                      className="flex-1"
                      disabled={approve.isPending}
                      onClick={onApprove}
                    >
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
