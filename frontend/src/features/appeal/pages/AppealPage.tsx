import { useEffect, useMemo, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { ArrowRight, CheckCheck, Gavel, Search, Timer, Undo2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
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
import { useAppeals, useAckAppeal, useDecideAppeal } from "@/features/appeal/queries";
import {
  appealStatusMeta,
  categoryLabel,
  conclusionLabel,
} from "@/features/appeal/labels";
import { formatDateTime, slaRemaining } from "@/lib/format/date";
import { maskName, maskText } from "@/lib/mask";
import type { AppealCategory, AppealStatus, AppealTicket } from "@/lib/api/models";
import { SLA_OPTIONS } from "@/features/appeal/constants";

type ConclusionKind = "REVOKE_OVERDUE" | "COMPENSATE" | "REJECT";

export function AppealPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const urlId = searchParams.get("id") ?? undefined;
  const [status, setStatus] = useState<AppealStatus | "ALL">(() => {
    const urlStatus = searchParams.get("status");
    return urlStatus === "PENDING" ||
      urlStatus === "PROCESSING" ||
      urlStatus === "RESOLVED" ||
      urlStatus === "REJECTED"
      ? urlStatus
      : "ALL";
  });
  const [category, setCategory] = useState<AppealCategory | "ALL">("ALL");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [selectedId, setSelectedId] = useState<string | undefined>(urlId);
  const [kind, setKind] = useState<ConclusionKind>("REVOKE_OVERDUE");
  const [days, setDays] = useState(3);
  const [note, setNote] = useState("");
  const invalidIdRef = useRef<string | undefined>(undefined);

  const query = useAppeals({
    status: status === "ALL" ? undefined : status,
    category: category === "ALL" ? undefined : category,
    keyword: keyword || undefined,
    page,
    size: 8,
  });
  const ack = useAckAppeal();
  const decide = useDecideAppeal();

  const items = query.data?.items ?? [];
  const selected: AppealTicket | undefined = useMemo(
    () => items.find((item) => item.id === selectedId) ?? items[0],
    [items, selectedId],
  );

  // 工作台「查看」带 ?id= 进来：选中该工单，避免落到第一条。
  useEffect(() => {
    if (urlId) setSelectedId(urlId);
  }, [urlId]);

  useEffect(() => {
    if (!urlId || selectedId !== urlId || !query.isSuccess || items.some((item) => item.id === urlId)) return;
    if (invalidIdRef.current === urlId) return;
    invalidIdRef.current = urlId;
    toast.error("该工单已归档或不存在", { description: "已回到申诉列表。" });
    const next = new URLSearchParams(searchParams);
    next.delete("id");
    setSearchParams(next, { replace: true });
    setSelectedId(undefined);
  }, [urlId, selectedId, items, query.isSuccess, searchParams, setSearchParams]);

  const canDecide = selected && selected.status !== "RESOLVED" && selected.status !== "REJECTED";

  const submit = () => {
    if (!selected) return;
    if (kind === "REJECT" && note.trim().length < 10) {
      toast.error("驳回需要填写不少于 10 字的结论说明");
      return;
    }
    decide.mutate(
      {
        id: selected.id,
        action: kind === "REVOKE_OVERDUE" ? "resolve" : kind === "COMPENSATE" ? "compensate" : "reject",
        note: note.trim(),
        days: kind === "COMPENSATE" ? days : undefined,
      },
      { onSuccess: () => setNote("") },
    );
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="申诉工单"
        description="逾期与费用争议在这里二次核对；结论会同步到借阅记录与审计日志。"
      />

      <SectionCard bodyClassName="px-6 pb-5">
        <div className="flex flex-wrap items-center gap-2">
          <SegmentedFilter
            value={status}
            onChange={(value) => {
              setStatus(value);
              setPage(1);
            }}
            items={[
              { value: "ALL", label: "全部" },
              { value: "PENDING", label: "待受理" },
              { value: "PROCESSING", label: "处理中" },
              { value: "RESOLVED", label: "已结案" },
            ]}
          />
          <Select
            value={category}
            onValueChange={(value) => {
              setCategory(value as AppealCategory | "ALL");
              setPage(1);
            }}
          >
            <SelectTrigger className="h-[34px] w-[132px] text-[11px]">
              <SelectValue placeholder="全部类型" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">全部类型</SelectItem>
              {(Object.keys(categoryLabel) as AppealCategory[]).map((key) => (
                <SelectItem key={key} value={key}>
                  {categoryLabel[key]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <div className="relative ml-auto">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
            <Input
              value={keyword}
              onChange={(event) => {
                setKeyword(event.target.value);
                setPage(1);
              }}
              placeholder="搜索工单号、读者或书目"
              className="h-[34px] w-[250px] pl-9 pr-3 text-[11px]"
            />
          </div>
        </div>

        <div className="mt-3 grid grid-cols-1 gap-4 xl:grid-cols-12">
          <div className="xl:col-span-7">
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead className="">工单</TableHead>
                  <TableHead>读者 / 书目</TableHead>
                  <TableHead>类型</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead className="text-right">SLA</TableHead>
                  <TableHead className="w-[64px] text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.map((item) => {
                  const sla = slaRemaining(item.slaDeadline);
                  const active = selected?.id === item.id;
                  return (
                    <TableRow
                      key={item.id}
                      onClick={() => setSelectedId(item.id)}
                      className={
                        "cursor-pointer " + (active ? "bg-mint-soft/40 hover:bg-mint-soft/40" : "")
                      }
                    >
                      <TableCell className="">
                        <p className="tabular font-semibold text-ink">{item.code}</p>
                        <p className="text-[9px] text-ink-3">{formatDateTime(item.createdAt)}</p>
                      </TableCell>
                      <TableCell>
                        <p className="text-ink">{maskName(item.reader.name)}</p>
                        <p className="max-w-[150px] truncate text-[9.5px] text-ink-3">
                          {item.book.title}
                        </p>
                      </TableCell>
                      <TableCell className="text-ink-3">{categoryLabel[item.category]}</TableCell>
                      <TableCell>
                        <StatusPill tone={appealStatusMeta[item.status].tone} dot>
                          {appealStatusMeta[item.status].label}
                        </StatusPill>
                      </TableCell>
                      <TableCell className="text-right">
                        <span
                          className={
                            item.status === "RESOLVED"
                              ? "text-ink-3"
                              : sla.overdue
                                ? "font-semibold text-danger"
                                : "text-ink-2"
                          }
                        >
                          {item.status === "RESOLVED" ? "—" : sla.text}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant={item.status === "PENDING" ? "default" : "ghost"}
                          size="sm"
                          className="h-7 px-2 text-[10px]"
                          onClick={(event) => {
                            event.stopPropagation();
                            setSelectedId(item.id);
                          }}
                        >
                          {item.status === "PENDING" ? "去处理" : "查看"}
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>

            {!query.isLoading && items.length === 0 ? (
              <EmptyState title="没有匹配的工单" description="调整筛选条件，或稍后再看。" />
            ) : null}

            <div className="mt-3">
              <Pager page={page} size={8} total={query.data?.total ?? 0} onChange={setPage} />
            </div>
          </div>

          <div className="xl:col-span-5">
            {selected ? (
              <div className="space-y-3 rounded-xl border border-line bg-wash/40 p-3.5">
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="tabular text-[12px] font-bold text-ink">{selected.code}</p>
                    <p className="mt-0.5 text-[10px] text-ink-3">
                      {maskName(selected.reader.name)} · {categoryLabel[selected.category]}
                    </p>
                  </div>
                  <StatusPill tone={appealStatusMeta[selected.status].tone} dot>
                    {appealStatusMeta[selected.status].label}
                  </StatusPill>
                </div>

                <p className="rounded-lg bg-card px-3 py-2 text-[10.5px] leading-5 text-ink-2 ring-1 ring-line">
                  {maskText(selected.summary)}
                </p>

                {selected.conclusion ? (
                  <div className="rounded-lg bg-mint-soft/70 px-3 py-2.5">
                    <p className="text-[9px] font-semibold tracking-wider text-mint-deep">处理结论</p>
                    <p className="mt-1 text-[11px] font-semibold text-ink">
                      {conclusionLabel[selected.conclusion.kind]}
                      {selected.conclusion.days ? ` · ${selected.conclusion.days} 天` : ""}
                    </p>
                    <p className="mt-1 text-[10px] leading-5 text-ink-2">{maskText(selected.conclusion.note)}</p>
                    <p className="tabular mt-1.5 text-[9px] text-ink-3">
                      {selected.conclusion.decidedBy} · {formatDateTime(selected.conclusion.decidedAt)}
                    </p>
                  </div>
                ) : null}

                <div>
                  <p className="text-[9px] font-semibold tracking-wider text-ink-3">流转</p>
                  <ol className="mt-1.5 space-y-2">
                    {selected.timeline.map((entry, index) => (
                      <li key={`${entry.at}-${index}`} className="flex gap-2">
                        <span className="mt-1 size-1.5 shrink-0 rounded-full bg-mint-deep/50" />
                        <div>
                          <p className="text-[10.5px] text-ink-2">
                            <b className="font-semibold text-ink">{entry.actor}</b> {maskText(entry.text)}
                          </p>
                          <p className="tabular text-[9px] text-ink-3">{formatDateTime(entry.at)}</p>
                        </div>
                      </li>
                    ))}
                  </ol>
                </div>

                {canDecide ? (
                  <div className="space-y-2.5 border-t border-line pt-3">
                    {selected.status === "PENDING" ? (
                      <>
                      <Button
                        variant="outline"
                        size="sm"
                        className="w-full"
                        disabled={ack.isPending}
                        onClick={() => ack.mutate(selected.id)}
                      >
                        <Timer className="size-3.5" />
                        受理工单并开始计时
                      </Button>
                      <p className="text-[9.5px] leading-4 text-ink-3">
                        先受理工单，SLA 计时开始后才能给出结论。
                      </p>
                      </>
                    ) : (
                      <>
                    <div className="grid grid-cols-3 gap-1.5">
                      {(
                        [
                          { value: "REVOKE_OVERDUE", label: "撤销逾期", icon: Undo2 },
                          { value: "COMPENSATE", label: "补偿宽限", icon: CheckCheck },
                          { value: "REJECT", label: "驳回", icon: Gavel },
                        ] as const
                      ).map((option) => (
                        <button
                          key={option.value}
                          type="button"
                          onClick={() => setKind(option.value)}
                          className={
                            "flex flex-col items-center gap-1 rounded-lg border py-2 text-[9.5px] transition-colors " +
                            (kind === option.value
                              ? "border-mint-deep bg-mint-soft text-mint-deep"
                              : "border-line bg-card text-ink-3 hover:border-mint-deep/30")
                          }
                        >
                          <option.icon className="size-3.5" />
                          {option.label}
                        </button>
                      ))}
                    </div>

                    {kind === "COMPENSATE" ? (
                      <div className="flex items-center gap-1.5">
                        {SLA_OPTIONS.map((option) => (
                          <button
                            key={option}
                            type="button"
                            onClick={() => setDays(option)}
                            className={
                              "tabular h-7 flex-1 rounded-md border text-[10.5px] transition-colors " +
                              (days === option
                                ? "border-mint-deep bg-card font-semibold text-mint-deep"
                                : "border-line bg-card text-ink-3")
                            }
                          >
                            {option} 天
                          </button>
                        ))}
                      </div>
                    ) : null}

                    <Textarea
                      value={note}
                      onChange={(event) => setNote(event.target.value)}
                      placeholder="写明核对依据与结论，≥10 字（驳回必填）"
                      className="min-h-[64px] resize-none bg-card text-[11px]"
                    />

                    <Button className="w-full" size="sm" disabled={decide.isPending} onClick={submit}>
                      提交结论
                      <ArrowRight className="size-3.5" />
                    </Button>
                      </>
                    )}
                  </div>
                ) : null}
              </div>
            ) : (
              <EmptyState title="选择一张工单" description="点选左侧列表查看详情与结论。" />
            )}
          </div>
        </div>
      </SectionCard>
    </div>
  );
}
