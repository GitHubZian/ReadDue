import { AlarmClock, ArrowUpRight, CalendarClock, Inbox, MoonStar, TriangleAlert } from "lucide-react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
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
import { graceSceneLabel } from "@/features/grace/labels";
import { StatCard } from "@/components/common/StatCard";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { TrendChart } from "@/features/workbench/components/TrendChart";
import { useDashboard } from "@/features/workbench/queries";
import { useGraces } from "@/features/grace/queries";
import { useAppeals } from "@/features/appeal/queries";
import { useRemindOverdue } from "@/features/overdue/queries";
import { formatMoneyNumber } from "@/lib/format/money";
import { formatTime, slaRemaining } from "@/lib/format/date";
import { categoryLabel, appealStatusMeta } from "@/features/appeal/labels";
import { can } from "@/lib/auth/permissions";
import { useSession } from "@/lib/auth/session";
import { maskName, maskStudentNo } from "@/lib/mask";

export function WorkbenchPage() {
  const navigate = useNavigate();
  const { user } = useSession();
  const canViewAudit = can(user, "audit:read");
  const canEditRules = can(user, "rule:update");
  const { data, isLoading } = useDashboard();
  const graces = useGraces({ status: "PENDING", page: 1, size: 5 });
  const appeals = useAppeals({ page: 1, size: 3 });
  const remind = useRemindOverdue();

  const stats = data?.stats;

  return (
    <div className="space-y-4">
      <PageHeader
        title="工作台"
        description="今天先处理 SLA 内到期的宽限与申诉。"
        actions={
          canViewAudit ? (
            <Button variant="outline" size="sm" asChild>
              <Link to="/audit">
                操作日志
                <ArrowUpRight className="size-3.5" />
              </Link>
            </Button>
          ) : null
        }
      />

      <div className="grid grid-cols-2 gap-3 xl:grid-cols-4">
        <StatCard
          label="待办工单"
          value={isLoading ? "—" : (stats?.pendingTotal ?? 0)}
          hint={
            <span>
              宽限 <b className="text-warn">{stats?.pendingGraces ?? 0}</b> · 申诉{" "}
              <b className="text-mint-deep">{stats?.pendingAppeals ?? 0}</b>
            </span>
          }
          icon={Inbox}
          tone="amber"
          to="/grace?status=PENDING"
        />
        <StatCard
          label="今日到期"
          value={isLoading ? "—" : (stats?.dueToday ?? 0)}
          unit="册"
          hint={
            (stats?.dueToday ?? 0) > 0 ? "17:00 自动提醒" : "今天没有到期的借阅"
          }
          icon={CalendarClock}
          tone="mint"
          to="/circulation"
        />
        <StatCard
          label="逾期未还"
          value={isLoading ? "—" : (stats?.overdueBooks ?? 0)}
          unit="册"
          hint={
            <span>
              {stats?.overdueReaders ?? 0} 位读者 · 滞纳金 ¥
              {formatMoneyNumber(stats?.overdueFine ?? "0")}
            </span>
          }
          icon={AlarmClock}
          tone="red"
          to="/overdue"
        />
        <StatCard
          label="超时申诉"
          value={isLoading ? "—" : (stats?.slaBreach ?? 0)}
          unit="单"
          hint={(stats?.slaBreach ?? 0) > 0 ? "已升级值班主管" : "暂无超时工单"}
          icon={TriangleAlert}
          tone="red"
          to="/appeals?status=PROCESSING"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-12">
        <div className="space-y-4 xl:col-span-7">
          <SectionCard
            title="宽限待裁决"
            description="按剩余 SLA 升序，超时单需优先说明"
            action={
              <Button variant="ghost" size="xs" asChild>
                <Link to="/grace">全部</Link>
              </Button>
            }
            >
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead >读者</TableHead>
                  <TableHead>书目</TableHead>
                  <TableHead>场景</TableHead>
                  <TableHead>剩余 SLA</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(graces.data?.items ?? []).map((item) => {
                  const sla = slaRemaining(item.slaDeadline);
                  return (
                    <TableRow key={item.id}>
                      <TableCell >
                        <p className="font-medium text-ink">{maskName(item.reader.name)}</p>
                        <p className="text-[9.5px] text-ink-3">{maskStudentNo(item.reader.studentNo)}</p>
                      </TableCell>
                      <TableCell className="max-w-[130px] truncate text-ink-2">
                        {item.book.title}
                      </TableCell>
                      <TableCell className="text-ink-3">{graceSceneLabel(item.scene)}</TableCell>
                      <TableCell>
                        <span
                          className={
                            sla.overdue
                              ? "font-semibold text-danger"
                              : sla.minutes !== null && sla.minutes <= 60
                                ? "font-semibold text-warn"
                                : "text-ink-2"
                          }
                        >
                          {sla.text}
                        </span>
                      </TableCell>
                      <TableCell className="text-right">
                        <Button
                          variant="outline"
                          size="xs"
                          onClick={() => navigate(`/grace?id=${item.id}`)}
                        >
                          裁决
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            {!graces.isLoading && (graces.data?.items.length ?? 0) === 0 ? (
              <EmptyState title="没有待裁决的宽限" description="新的申请会实时出现在这里。" />
            ) : null}
          </SectionCard>

          <SectionCard
            title="今日到期提醒"
            description="22:00–08:00 免打扰，其余时段即时发送"
          >
            {(data?.dueTodayItems ?? []).length === 0 ? (
              <div className="flex items-center gap-3 rounded-xl border border-dashed border-line bg-wash/60 px-4 py-4 text-[10.5px] text-ink-2">
                <CalendarClock className="size-4 shrink-0 text-ink-3" />
                <span>今天没有到期的借阅。明早再来看，或到「逾期看板」处理已逾期记录。</span>
              </div>
            ) : (
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead>读者</TableHead>
                  <TableHead>书目</TableHead>
                  <TableHead>到期时间</TableHead>
                  <TableHead>提醒</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(data?.dueTodayItems ?? []).map((item) => (
                  <TableRow key={item.id}>
                    <TableCell className="text-ink">{maskName(item.reader.name)}</TableCell>
                    <TableCell className="max-w-[140px] truncate text-ink-2">
                      {item.book.title}
                    </TableCell>
                    <TableCell className="tabular text-ink-3">{formatTime(item.dueAt)}</TableCell>
                    <TableCell>
                      <StatusPill tone={item.reminded ? "mint" : "slate"} dot>
                        {item.reminded ? "已提醒" : "待提醒"}
                      </StatusPill>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="xs"
                        disabled={remind.isPending}
                        onClick={() =>
                          remind.mutate([item.id], {
                            onSuccess: () => toast.success(`已向 ${maskName(item.reader.name)} 发送到期提醒`),
                          })
                        }
                      >
                        催还
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            )}
          </SectionCard>

          <SectionCard
            title="申诉工单"
            description="最近提交的三单"
            action={
              <Button variant="ghost" size="xs" asChild>
                <Link to="/appeals">全部</Link>
              </Button>
            }
            >
            <Table>
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead >工单</TableHead>
                  <TableHead>类型</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead className="text-right">详情</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(appeals.data?.items ?? []).map((item) => (
                  <TableRow key={item.id}>
                    <TableCell >
                      <p className="tabular font-medium text-ink">{item.code}</p>
                      <p className="text-[9.5px] text-ink-3">
                        {maskName(item.reader.name)} · {item.book.title}
                      </p>
                    </TableCell>
                    <TableCell className="text-ink-3">{categoryLabel[item.category]}</TableCell>
                    <TableCell>
                      <StatusPill tone={appealStatusMeta[item.status].tone} dot>
                        {appealStatusMeta[item.status].label}
                      </StatusPill>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="xs"
                        onClick={() => navigate(`/appeals?id=${item.id}`)}
                      >
                        查看
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </SectionCard>
        </div>

        <div className="space-y-4 xl:col-span-5">
          <SectionCard title="借还趋势" description="按自然日统计，含自助借还">
            {data ? <TrendChart points={data.trend} summary={data.trendSummary} /> : null}
            {!data ? (
              <div className="h-[140px] animate-pulse rounded-lg bg-wash" />
            ) : null}
          </SectionCard>

          <div className="rounded-2xl bg-deep px-5 py-4 text-white">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="grid size-7 place-items-center rounded-lg bg-white/10">
                  <MoonStar className="size-3.5 text-mint" />
                </span>
                <p className="text-[12px] font-semibold">夜间免打扰时段</p>
              </div>
              <StatusPill tone="mint">22:00–08:00</StatusPill>
            </div>
            <p className="mt-2.5 text-[10.5px] leading-5 text-white/55">
              此时段仅暂停外部推送，站内信正常送达；逾期首日与预约到馆通知不受影响。规则参数改动会即时作用于队列。
            </p>
            <div className="mt-3 flex items-center gap-4 border-t border-white/10 pt-3">
              <div>
                <p className="text-[9px] text-white/40">待办工单</p>
                <p className="tabular text-[15px] font-bold">{stats?.pendingTotal ?? 0}</p>
              </div>
              <div>
                <p className="text-[9px] text-white/40">值班时段</p>
                <p className="text-[11px] font-semibold">{data?.duty.window ?? "08:00–22:00"}</p>
              </div>
              <Button
                variant="ghost"
                size="xs"
                className="ml-auto text-mint hover:bg-white/10 hover:text-mint"
                asChild
              >
                <Link to="/notify">提醒中心</Link>
              </Button>
            </div>
          </div>

          <SectionCard title="规则命中" description="近 24 小时，自动拦截与补偿">
            <ul className="space-y-3">
              {(data?.rulesHit ?? []).map((rule) => (
                <li key={rule.label}>
                  <div className="flex items-center justify-between text-[10.5px]">
                    <span className="text-ink-2">{rule.label}</span>
                    <span className="tabular font-semibold text-ink">{rule.value} 次</span>
                  </div>
                  <div className="mt-1.5 h-1.5 overflow-hidden rounded-full bg-wash">
                    <div
                      className="h-full rounded-full bg-mint-deep/70"
                      style={{ width: `${Math.min(100, (rule.value / 20) * 100)}%` }}
                    />
                  </div>
                </li>
              ))}
            </ul>
            <div className="mt-3.5 flex items-center justify-between border-t border-line-2 pt-3">
              <p className="text-[9.5px] text-ink-3">
                值班 {data?.duty.name ?? "前台值班"} · {data?.duty.phone ?? "内线 8021"}
              </p>
              {canEditRules ? (
                <Button variant="ghost" size="xs" asChild>
                  <Link to="/rules">调整参数</Link>
                </Button>
              ) : null}
            </div>
          </SectionCard>

          {graces.data?.items.length ? (
            <p className="px-1 text-[9.5px] text-ink-3">
              {canViewAudit
                ? "提示：裁决记录会写入审计日志，可在「审计日志 · 审批」中追溯。"
                : "提示：裁决记录会写入审计日志。"}
            </p>
          ) : null}
        </div>
      </div>
    </div>
  );
}
