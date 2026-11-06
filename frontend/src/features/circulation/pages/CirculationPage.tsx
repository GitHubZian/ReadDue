import { useState } from "react";
import { ArrowLeftRight, BookUp2, CheckCircle2, ScanLine } from "lucide-react";
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
import { StatCard } from "@/components/common/StatCard";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { useTodayFlow, useBorrowBook, useReturnBook } from "@/features/circulation/queries";
import { formatDateTime, formatTime } from "@/lib/format/date";
import { formatCNY } from "@/lib/format/money";

export function CirculationPage() {
  const { data } = useTodayFlow();
  const borrow = useBorrowBook();
  const ret = useReturnBook();

  const [readerNo, setReaderNo] = useState("");
  const [borrowBarcode, setBorrowBarcode] = useState("");
  const [returnBarcode, setReturnBarcode] = useState("");

  const onBorrow = () => {
    if (!readerNo.trim() || !borrowBarcode.trim()) return;
    borrow.mutate(
      { readerNo: readerNo.trim(), barcode: borrowBarcode.trim() },
      { onSuccess: () => setBorrowBarcode("") },
    );
  };

  const onReturn = () => {
    if (!returnBarcode.trim()) return;
    ret.mutate({ barcode: returnBarcode.trim() }, { onSuccess: () => setReturnBarcode("") });
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="借还台"
        description="扫码即办，借出与归还共用一套条码规则；异常会内联提示，不打断下一位读者。"
      />

      <div className="grid grid-cols-3 gap-3">
        <StatCard label="今日借出" value={data?.borrows ?? 0} unit="册" icon={BookUp2} tone="mint" />
        <StatCard label="今日归还" value={data?.returns ?? 0} unit="册" icon={ArrowLeftRight} tone="mint" />
        <StatCard
          label="当前在办"
          value={0}
          unit="笔"
          hint="柜台无排队"
          icon={CheckCircle2}
          tone="slate"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <SectionCard title="借出" description="输入读者证号与馆藏条码，回车即提交">
          <div className="space-y-3">
            <div>
              <label className="text-[9.5px] font-semibold tracking-wider text-ink-3" htmlFor="reader-no">
                读者证号
              </label>
              <Input
                id="reader-no"
                value={readerNo}
                onChange={(event) => setReaderNo(event.target.value)}
                placeholder="例如 2023A1042"
                className="mt-1 h-10 font-mono text-[12px]"
              />
            </div>
            <div>
              <label className="text-[9.5px] font-semibold tracking-wider text-ink-3" htmlFor="borrow-barcode">
                馆藏条码
              </label>
              <div className="mt-1 flex gap-2">
                <div className="relative flex-1">
                  <ScanLine className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
                  <Input
                    id="borrow-barcode"
                    value={borrowBarcode}
                    onChange={(event) => setBorrowBarcode(event.target.value)}
                    onKeyDown={(event) => event.key === "Enter" && onBorrow()}
                    placeholder="扫描或输入条码"
                    className="h-10 pl-8 font-mono text-[12px]"
                  />
                </div>
                <Button size="sm" className="h-10" onClick={onBorrow} disabled={borrow.isPending}>
                  确认借出
                </Button>
              </div>
            </div>

            {borrow.data ? (
              <div className="rounded-xl bg-mint-soft/70 px-3 py-2.5">
                <p className="text-[10.5px] font-semibold text-mint-deep">
                  借出成功 · {borrow.data.book.title}
                </p>
                <p className="mt-1 text-[10px] text-ink-2">
                  {borrow.data.reader.name} · 应还 {formatDateTime(borrow.data.dueAt)}
                </p>
              </div>
            ) : null}
            {borrow.error ? (
              <p className="rounded-xl bg-danger-soft px-3 py-2.5 text-[10.5px] text-danger">
                {borrow.error.message}
              </p>
            ) : null}
          </div>
        </SectionCard>

        <SectionCard title="归还" description="归还后自动计算逾期天数与滞纳金">
          <div className="space-y-3">
            <div>
              <label className="text-[9.5px] font-semibold tracking-wider text-ink-3" htmlFor="return-barcode">
                馆藏条码
              </label>
              <div className="mt-1 flex gap-2">
                <div className="relative flex-1">
                  <ScanLine className="pointer-events-none absolute left-2.5 top-1/2 size-3.5 -translate-y-1/2 text-ink-3" />
                  <Input
                    id="return-barcode"
                    value={returnBarcode}
                    onChange={(event) => setReturnBarcode(event.target.value)}
                    onKeyDown={(event) => event.key === "Enter" && onReturn()}
                    placeholder="扫描归还条码"
                    className="h-10 pl-8 font-mono text-[12px]"
                  />
                </div>
                <Button size="sm" variant="outline" className="h-10" onClick={onReturn} disabled={ret.isPending}>
                  确认归还
                </Button>
              </div>
            </div>

            {ret.data ? (
              <div className="rounded-xl bg-wash px-3 py-2.5">
                <p className="text-[10.5px] font-semibold text-ink">
                  《{ret.data.book.title}》已归还
                </p>
                <p className="mt-1 text-[10px] text-ink-2">
                  {ret.data.reader.name}
                  {ret.data.overdueDays > 0
                    ? ` · 逾期 ${ret.data.overdueDays} 天，滞纳金 ${formatCNY(ret.data.fine)}`
                    : " · 按期归还"}
                </p>
              </div>
            ) : null}
            {ret.error ? (
              <p className="rounded-xl bg-danger-soft px-3 py-2.5 text-[10.5px] text-danger">
                {ret.error.message}
              </p>
            ) : null}

            <div className="rounded-xl border border-dashed border-line px-3 py-2.5 text-[10px] leading-5 text-ink-3">
              遗失或污损的图书请先在「逾期看板」登记状态，再办理赔偿，避免产生错误的滞纳金。
            </div>
          </div>
        </SectionCard>
      </div>

      <SectionCard
        title="今日流水"
        description={`${data?.flows.length ?? 0} 笔 · 按时间倒序`}
        bodyClassName="px-0 pb-1"
      >
        <Table>
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="pl-5">时间</TableHead>
              <TableHead>类型</TableHead>
              <TableHead>读者</TableHead>
              <TableHead>书目</TableHead>
              <TableHead className="pr-5 text-right">操作人</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(data?.flows ?? []).map((flow) => (
              <TableRow key={flow.id}>
                <TableCell className="tabular pl-5 text-ink-3">{formatTime(flow.at)}</TableCell>
                <TableCell>
                  <StatusPill tone={flow.type === "BORROW" ? "mint" : "slate"}>
                    {flow.type === "BORROW" ? "借出" : "归还"}
                  </StatusPill>
                </TableCell>
                <TableCell className="text-ink">{flow.reader.name}</TableCell>
                <TableCell className="max-w-[220px] truncate text-ink-2">{flow.book.title}</TableCell>
                <TableCell className="pr-5 text-right text-ink-3">{flow.operator}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
        {!data?.flows.length ? (
          <EmptyState title="今天还没有流水" description="完成第一笔借出后，这里会实时更新。" />
        ) : null}
      </SectionCard>
    </div>
  );
}
