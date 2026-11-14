import { CalendarDays, Layers } from "lucide-react";
import { SectionCard } from "@/components/common/SectionCard";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useCalendar, useTiers } from "@/features/rule/queries";
import type { TierRow } from "@/lib/api/models";

const ACTION_LABEL: Record<string, string> = {
  REINFORCE: "提醒 + 一键续借",
  APPEAL_WINDOW: "开放申诉与宽限",
  LIMIT_BORROW: "暂停新增借阅",
  LIBRARIAN_HANDLE: "馆员人工跟进",
};

const TIER_TONE: Record<string, "mint" | "amber" | "red" | "slate"> = {
  T1: "mint",
  T2: "amber",
  T3: "amber",
  T4: "red",
  T5: "red",
};

function daysText(tier: TierRow) {
  return tier.maxDays == null ? `${tier.minDays} 天以上` : `${tier.minDays}–${tier.maxDays} 天`;
}

/** 逾期分级表：后端只读返回，规则参数页内展示策略现状。 */
export function OverdueTiersCard() {
  const { data, isLoading } = useTiers();

  return (
    <SectionCard
      title="逾期分级"
      description="按逾期天数自动分档，决定提醒动作与是否允许续借。"
      action={
        <span className="flex items-center gap-1 text-[9.5px] text-ink-3">
          <Layers className="size-3" /> 只读
        </span>
      }
    >
      {isLoading ? (
        <div className="h-40 animate-pulse rounded-lg bg-wash" />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="pl-0">分级</TableHead>
              <TableHead>逾期天数</TableHead>
              <TableHead>处理动作</TableHead>
              <TableHead>续借</TableHead>
              <TableHead className="pr-0">说明</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {(data ?? []).map((tier) => (
              <TableRow key={tier.code}>
                <TableCell className="pl-0">
                  <div className="flex items-center gap-2">
                    <StatusPill tone={TIER_TONE[tier.code] ?? "slate"}>{tier.code}</StatusPill>
                    <span className="font-medium text-ink">{tier.name}</span>
                  </div>
                </TableCell>
                <TableCell className="tabular text-ink-2">{daysText(tier)}</TableCell>
                <TableCell className="text-ink-2">
                  {ACTION_LABEL[tier.actionCode] ?? tier.actionCode}
                </TableCell>
                <TableCell>
                  <StatusPill tone={tier.allowRenew ? "mint" : "slate"} dot>
                    {tier.allowRenew ? "可续借" : "不可续借"}
                  </StatusPill>
                </TableCell>
                <TableCell className="pr-0 text-[10.5px] text-ink-3">{tier.description}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
      {!isLoading && (data ?? []).length === 0 ? (
        <EmptyState title="后端暂未配置逾期分级" description="策略表为空，逾期将只按默认提醒处理。" />
      ) : null}
    </SectionCard>
  );
}

/** 校历：后端目前只读且为空，用于说明考试周 / 毕业季命中依据。 */
export function AcademicCalendarCard() {
  const { data, isLoading } = useCalendar();
  const entries = data ?? [];

  return (
    <SectionCard
      title="校历命中"
      description="宽限与逾期策略按校历判断考试周、毕业季；数据由校历同步任务写入。"
      action={
        <span className="flex items-center gap-1 text-[9.5px] text-ink-3">
          <CalendarDays className="size-3" /> 只读
        </span>
      }
    >
      {isLoading ? (
        <div className="h-28 animate-pulse rounded-lg bg-wash" />
      ) : entries.length === 0 ? (
        <EmptyState
          className="py-6"
          title="暂无校历条目"
          description="校历写接口尚未开放；同步前所有学生按日常基准宽限计算。"
        />
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="pl-0">学期</TableHead>
              <TableHead>场景</TableHead>
              <TableHead>开始</TableHead>
              <TableHead>结束</TableHead>
              <TableHead>来源</TableHead>
              <TableHead className="pr-0">备注</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {entries.map((entry) => (
              <TableRow key={entry.id}>
                <TableCell className="pl-0 font-medium text-ink">{entry.term}</TableCell>
                <TableCell className="text-ink-2">{entry.scene}</TableCell>
                <TableCell className="tabular text-ink-2">{entry.startDate}</TableCell>
                <TableCell className="tabular text-ink-2">{entry.endDate}</TableCell>
                <TableCell className="text-ink-3">{entry.source}</TableCell>
                <TableCell className="pr-0 text-[10.5px] text-ink-3">{entry.note}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </SectionCard>
  );
}
