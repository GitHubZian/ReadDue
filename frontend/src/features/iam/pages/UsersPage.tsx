import { useState } from "react";
import { Ban, RotateCcw, Search, ShieldCheck } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
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
import { useUsers, useBlacklistUser, useRestoreUser } from "@/features/iam/queries";
import { ROLE_LABEL } from "@/lib/auth/permissions";
import { formatDate, fromNow } from "@/lib/format/date";
import { formatCNY } from "@/lib/format/money";
import type { AdminUser, Role } from "@/lib/api/models";

const statusMeta: Record<AdminUser["status"], { label: string; tone: "mint" | "amber" | "red" | "slate" }> = {
  ACTIVE: { label: "正常", tone: "mint" },
  BLACKLISTED: { label: "已拉黑", tone: "red" },
  MUTED: { label: "已禁言", tone: "amber" },
};

export function UsersPage() {
  const [role, setRole] = useState<Role | "ALL">("ALL");
  const [keyword, setKeyword] = useState("");
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState<string[]>([]);
  const [target, setTarget] = useState<AdminUser | null>(null);
  const [reason, setReason] = useState("");

  const query = useUsers({
    role: role === "ALL" ? undefined : role,
    keyword: keyword || undefined,
    page,
    size: 12,
  });
  const blacklist = useBlacklistUser();
  const restore = useRestoreUser();
  const items = query.data?.items ?? [];
  // 后端只允许拉黑读者，职工行不给批量与操作入口
  const selectable = items.filter((item) => item.role === "READER");
  const allChecked = selectable.length > 0 && selected.length === selectable.length;

  const submitBlacklist = () => {
    if (!target) return;
    if (reason.trim().length < 6) {
      toast.error("请填写不少于 6 字的拉黑原因");
      return;
    }
    blacklist.mutate(
      { id: target.id, reason: reason.trim() },
      {
        onSuccess: () => {
          setTarget(null);
          setReason("");
          toast.success(`已拉黑 ${target.name}`, { description: "其借出与预约权限已冻结。" });
        },
      },
    );
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="用户与权限"
        description="角色决定可用功能；拉黑会冻结借出、预约与续借，历史记录保留。"
        actions={
          <Button
            size="sm"
            variant="outline"
            disabled={!selected.length}
            onClick={() =>
              toast.info(`已选择 ${selected.length} 位用户`, {
                description: "批量操作需在规则参数中开启审批流。",
              })
            }
          >
            <ShieldCheck className="size-3.5" />
            批量处理{selected.length ? `（${selected.length}）` : ""}
          </Button>
        }
      />

      <SectionCard bodyClassName="px-6 pb-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <SegmentedFilter
            value={role}
            onChange={(value) => {
              setRole(value);
              setPage(1);
              setSelected([]);
            }}
            items={[
              { value: "ALL", label: "全部" },
              { value: "READER", label: "读者" },
              { value: "LIBRARIAN", label: "馆员" },
              { value: "ADMIN", label: "管理员" },
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
              placeholder="搜索姓名、学号或手机号"
              className="h-[34px] w-[250px] pl-9 pr-3 text-[11px]"
            />
          </div>
        </div>

        <Table className="mt-3">
          <TableHeader>
            <TableRow className="hover:bg-transparent">
              <TableHead className="w-8">
                <Checkbox
                  checked={allChecked}
                  onCheckedChange={(checked) => setSelected(checked ? selectable.map((item) => item.id) : [])}
                  aria-label="全选"
                />
              </TableHead>
              <TableHead>用户</TableHead>
              <TableHead>角色</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>借阅</TableHead>
              <TableHead>最近活跃</TableHead>
              <TableHead className="text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {items.map((user) => (
              <TableRow key={user.id}>
                <TableCell className="">
                  <Checkbox
                    checked={selected.includes(user.id)}
                    disabled={user.role !== "READER"}
                    onCheckedChange={() =>
                      setSelected((prev) =>
                        prev.includes(user.id) ? prev.filter((key) => key !== user.id) : [...prev, user.id],
                      )
                    }
                    aria-label={`选择 ${user.name}`}
                  />
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <span className="grid size-6 place-items-center rounded-full bg-mint-soft text-[9.5px] font-semibold text-mint-deep">
                      {user.name.slice(0, 1)}
                    </span>
                    <div>
                      <p className="font-medium text-ink">{user.name}</p>
                      <p className="tabular text-[9px] text-ink-3">
                        {user.studentNo} · {user.college}
                      </p>
                    </div>
                  </div>
                </TableCell>
                <TableCell className="text-ink-2">{ROLE_LABEL[user.role]}</TableCell>
                <TableCell>
                  <StatusPill tone={statusMeta[user.status].tone} dot>
                    {statusMeta[user.status].label}
                  </StatusPill>
                  {user.blacklistReason ? (
                    <p className="mt-0.5 max-w-[120px] truncate text-[9px] text-danger">
                      {user.blacklistReason}
                    </p>
                  ) : null}
                </TableCell>
                <TableCell className="tabular text-[10.5px] text-ink-2">
                  {user.borrowCount} 册
                  {Number(user.fine) > 0 ? (
                    <span className="block text-[9px] text-danger">{formatCNY(user.fine)}</span>
                  ) : null}
                </TableCell>
                <TableCell className="text-[10px] text-ink-3">
                  {fromNow(user.lastActiveAt)}
                  <span className="block text-[9px]">加入 {formatDate(user.joinedAt)}</span>
                </TableCell>
                <TableCell className="text-right">
                  {user.role !== "READER" ? (
                    <span className="text-[9.5px] text-ink-3">—</span>
                  ) : user.status === "BLACKLISTED" ? (
                    <Button
                      variant="ghost"
                      size="xs"
                      disabled={restore.isPending}
                      onClick={() =>
                        restore.mutate(user.id, { onSuccess: () => toast.success(`已恢复 ${user.name} 的权限`) })
                      }
                    >
                      <RotateCcw className="size-3" /> 恢复
                    </Button>
                  ) : (
                    <Button
                      variant="ghost"
                      size="xs"
                      className="text-danger hover:bg-danger-soft hover:text-danger"
                      onClick={() => {
                        setTarget(user);
                        setReason("");
                      }}
                    >
                      <Ban className="size-3" /> 拉黑
                    </Button>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {!query.isLoading && items.length === 0 ? (
          <EmptyState title="没有匹配的用户" description="换个关键词，或切换角色筛选。" />
        ) : null}

        <div className="mt-3">
          <Pager page={page} size={12} total={query.data?.total ?? 0} onChange={setPage} />
        </div>
      </SectionCard>

      <Dialog open={Boolean(target)} onOpenChange={(open) => !open && setTarget(null)}>
        <DialogContent className="max-w-[380px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">拉黑 {target?.name}</DialogTitle>
            <DialogDescription className="text-[10.5px] text-ink-3">
              拉黑后该用户无法借出、预约与续借，已借图书仍需归还。原因会写入审计日志并通知本人。
            </DialogDescription>
          </DialogHeader>
          <Textarea
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            placeholder="例如：多次逾期且拒绝沟通（≥6 字）"
            className="min-h-[72px] resize-none text-[11px]"
          />
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setTarget(null)}>
              取消
            </Button>
            <Button
              size="sm"
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={blacklist.isPending}
              onClick={submitBlacklist}
            >
              确认拉黑
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
