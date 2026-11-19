import { useState } from "react";
import { AlarmClock, BellRing, CircleDollarSign, Users } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
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
import { StatCard } from "@/components/common/StatCard";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { Pager } from "@/components/common/Pager";
import { useOverdue, useRemindOverdue } from "@/features/overdue/queries";
import { formatDate, formatDateTime, fromNow } from "@/lib/format/date";
import { formatCNY, formatMoneyNumber } from "@/lib/format/money";
import type { OverdueStatus } from "@/lib/api/models";

const statusMeta: Record<OverdueStatus, { label: string; tone: "mint" | "amber" | "red" | "slate" }> = {
  OVERDUE: { label: "逾期", tone: "red" },
  LOST: { label: "遗失", tone: "slate" },
  CLAIMED: { label: "已赔偿", tone: "mint" },
};

export function OverduePage() {
  const [status, setStatus] = useState<OverdueStatus | "ALL">("OVERDUE");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<string[]>([]);

  const query = useOverdue({
    status: status === "ALL" ? undefined : status,
    keyword: keyword || undefined,
    page,
    size: 12,
  });
  const remind = useRemindOverdue();
  const items = query.data?.items ?? [];

  const totalFine = items.reduce((sum, item) => sum + Number(item.fine), 0);
  const allChecked = items.length > 0 && selected.length === items.length;

  const toggle = (id: string) =>
    setSelected((prev) => (prev.includes(id) ? prev.filter((key) => key !== id) : [...prev, id]));

  const send = (ids: string[]) => remind.mutate(ids, { onSuccess: () => setSelected([]) });

  return (
    <div className="space-y-4">
      <PageHeader
        title="逾期看板"
        description="滞纳金按规则实时计算；批量催还会去重，同一读者 24 小时内只发一次。"
        actions={
          <Button size="sm" disabled={remind.isPending || selected.length === 0} onClick={() => send(selected)}>
            <BellRing className="size-3.5" />
            批量催还{selected.length ? `（${selected.length}）` : ""}
          </Button>
        }
      />

      <div className="grid grid-cols-3 gap-3">
        <StatCard label="当前逾期" value={query.data?.total ?? 0} unit="册" icon={AlarmClock} tone="red" hint="含预约占位失败" />
        <StatCard
          label="涉及读者"
          value={new Set(items.map((item) => item.reader.id)).size}
          unit="位"
          icon={Users}
          tone="amber"
        />
        <StatCard
          label="本页滞纳金"
          value={formatMoneyNumber(totalFine.toFixed(2))}
          unit="元"
          icon={CircleDollarSign}
          tone="slate"
          hint="≤ 20 元免收，超出部分按日计"
        />
      </div>

      <SectionCard bodyClassName="px-6 pb-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SegmentedFilter
            value={status}
            onChange={(value) => {
              setStatus(value);
              setPage(1);
              setSelected([]);
            }}
            items={[
              { value: "OVERDUE", label: "逾期" },
              { value: "LOST", label: "遗失" },
              { value: "CLAIMED", label: "已赔偿" },
              { value: "ALL", label: "全部" },
            ]}
          />
          <div className="relative">
            <Input
              value={keyword}
              onChange={(event) => {
                setKeyword(event.target.value);
                setPage(1);
              }}
              placeholder="搜索读者、书目或条码"
              className="h-[34px] w-[240px] text-[11px]"
            />
          </div>
        </div>

        <Table className="mt-3">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="w-8">
                <Checkbox
                  checked={allChecked}
                  onCheckedChange={(checked) => setSelected(checked ? items.map((item) => item.id) : [])}
                  aria-label="全选"
                />
              </TableHead>
              <TableHead>读者</TableHead>
              <TableHead>书目</TableHead>
              <TableHead>应还日期</TableHead>
              <TableHead>逾期</TableHead>
              <TableHead>滞纳金</TableHead>
              <TableHead>催还</TableHead>
              <TableHead className="text-right">状态</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {items.map((item) => (
              <TableRow key={item.id}>
                <TableCell className="">
                  <Checkbox
                    checked={selected.includes(item.id)}
                    onCheckedChange={() => toggle(item.id)}
                    aria-label={`选择 ${item.reader.name}`}
                  />
                </TableCell>
                <TableCell>
                  <p className="font-medium text-ink">{item.reader.name}</p>
                  <p className="text-[9.5px] text-ink-3">{item.reader.studentNo}</p>
                </TableCell>
                <TableCell className="max-w-[200px]">
                  <p className="truncate text-ink-2">{item.book.title}</p>
                  <p className="text-[9.5px] text-ink-3">{item.book.barcode}</p>
                </TableCell>
                <TableCell className="tabular text-ink-3">{formatDate(item.dueAt)}</TableCell>
                <TableCell>
                  <span className="tabular font-semibold text-danger">{item.daysOverdue} 天</span>
                </TableCell>
                <TableCell className="tabular text-ink-2">{formatCNY(item.fine)}</TableCell>
                <TableCell>
                  <p className="tabular text-[10.5px] text-ink-2">{item.remindCount} 次</p>
                  <p className="text-[9px] text-ink-3">
                    {item.lastRemindedAt ? fromNow(item.lastRemindedAt) : "未提醒"}
                  </p>
                </TableCell>
                <TableCell className="text-right">
                  <StatusPill tone={statusMeta[item.status].tone} dot>
                    {statusMeta[item.status].label}
                  </StatusPill>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!query.isLoading && items.length === 0 ? (
          <EmptyState title="没有逾期记录" description="当前筛选为空，说明馆藏周转良好。" />
        ) : null}

        <div className="mt-3 flex items-center justify-between gap-3">
          <p className="text-[9.5px] text-ink-3">
            最近一次批量发送：{items[0]?.lastRemindedAt ? formatDateTime(items[0].lastRemindedAt) : "—"}
          </p>
          <Pager page={page} size={12} total={query.data?.total ?? 0} onChange={setPage} />
        </div>
      </SectionCard>
    </div>
  );
}
