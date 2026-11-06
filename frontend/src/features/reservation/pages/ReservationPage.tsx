import { useMemo, useState } from "react";
import { CalendarClock, PackageCheck, Search, TimerOff } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { Pager } from "@/components/common/Pager";
import { StatCard } from "@/components/common/StatCard";
import { useReservations, usePromoteReservation, useExpireReservation } from "@/features/reservation/queries";
import { formatDateTime, fromNow } from "@/lib/format/date";
import type { Reservation, ReservationStatus } from "@/lib/api/models";

const statusMeta: Record<ReservationStatus, { label: string; tone: "mint" | "amber" | "red" | "slate" }> = {
  WAITING: { label: "排队中", tone: "slate" },
  READY: { label: "待取书", tone: "amber" },
  FULFILLED: { label: "已取书", tone: "mint" },
  EXPIRED: { label: "已过期", tone: "red" },
  CANCELLED: { label: "已取消", tone: "slate" },
};

export function ReservationPage() {
  const [status, setStatus] = useState<ReservationStatus | "ALL">("ALL");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);

  const query = useReservations({
    status: status === "ALL" ? undefined : status,
    keyword: keyword || undefined,
    page,
    size: 12,
  });
  const promote = usePromoteReservation();
  const expire = useExpireReservation();
  const items = query.data?.items ?? [];

  const stats = useMemo(() => {
    const all = items as Reservation[];
    return {
      waiting: all.filter((item) => item.status === "WAITING").length,
      ready: all.filter((item) => item.status === "READY").length,
      expired: all.filter((item) => item.status === "EXPIRED").length,
    };
  }, [items]);

  return (
    <div className="space-y-4">
      <PageHeader
        title="预约管理"
        description="到馆后 48 小时内取书，逾期自动释放给下一位。释放与到馆都会即时通知读者。"
      />

      <div className="grid grid-cols-3 gap-3">
        <StatCard label="排队中" value={stats.waiting} unit="人" icon={CalendarClock} tone="slate" hint="按提交时间排序" />
        <StatCard label="待取书" value={stats.ready} unit="人" icon={PackageCheck} tone="amber" hint="已上预约架" />
        <StatCard label="已过期" value={stats.expired} unit="单" icon={TimerOff} tone="red" hint="已释放或在待释放" />
      </div>

      <SectionCard bodyClassName="px-5 pb-4">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SegmentedFilter
            value={status}
            onChange={(value) => {
              setStatus(value);
              setPage(1);
            }}
            items={[
              { value: "ALL", label: "全部" },
              { value: "WAITING", label: "排队中" },
              { value: "READY", label: "待取书" },
              { value: "EXPIRED", label: "已过期" },
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
              placeholder="搜索读者或书目"
              className="h-[34px] w-[240px] pl-8 text-[11px]"
            />
          </div>
        </div>

        <Table className="mt-3">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="pl-0">读者</TableHead>
              <TableHead>书目</TableHead>
              <TableHead>排队</TableHead>
              <TableHead>提交时间</TableHead>
              <TableHead>状态</TableHead>
              <TableHead className="pr-0 text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {items.map((item) => (
              <TableRow key={item.id}>
                <TableCell className="pl-0">
                  <p className="font-medium text-ink">{item.reader.name}</p>
                  <p className="text-[9.5px] text-ink-3">{item.reader.studentNo}</p>
                </TableCell>
                <TableCell className="max-w-[200px]">
                  <p className="truncate text-ink-2">{item.book.title}</p>
                  <p className="text-[9.5px] text-ink-3">{item.book.barcode}</p>
                </TableCell>
                <TableCell className="tabular text-ink-3">
                  {item.status === "WAITING" ? `第 ${item.queuePosition} 位` : "—"}
                </TableCell>
                <TableCell className="text-ink-3">{fromNow(item.createdAt)}</TableCell>
                <TableCell>
                  <div className="flex items-center gap-1.5">
                    <StatusPill tone={statusMeta[item.status].tone} dot>
                      {statusMeta[item.status].label}
                    </StatusPill>
                    {item.status === "READY" && item.expiresAt ? (
                      <span className="text-[9px] text-ink-3">
                        {formatDateTime(item.expiresAt)} 前取
                      </span>
                    ) : null}
                  </div>
                  {item.shelf ? <p className="mt-0.5 text-[9px] text-ink-3">{item.shelf}</p> : null}
                </TableCell>
                <TableCell className="pr-0 text-right">
                  {item.status === "WAITING" ? (
                    <Button
                      variant="outline"
                      size="xs"
                      disabled={promote.isPending}
                      onClick={() =>
                        promote.mutate(item.id, {
                          onSuccess: () => toast.success(`已为 ${item.reader.name} 锁定馆藏并上架`),
                        })
                      }
                    >
                      到馆确认
                    </Button>
                  ) : item.status === "READY" ? (
                    <Button
                      variant="ghost"
                      size="xs"
                      className="text-danger hover:bg-danger-soft hover:text-danger"
                      disabled={expire.isPending}
                      onClick={() =>
                        expire.mutate(item.id, { onSuccess: () => toast.success("已释放预约") })
                      }
                    >
                      过期释放
                    </Button>
                  ) : (
                    <span className="text-[9.5px] text-ink-3">—</span>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!query.isLoading && items.length === 0 ? (
          <EmptyState title="没有预约记录" description="当前筛选下为空，切换状态或更换关键词。" />
        ) : null}

        <div className="mt-3">
          <Pager page={page} size={12} total={query.data?.total ?? 0} onChange={setPage} />
        </div>
      </SectionCard>
    </div>
  );
}
