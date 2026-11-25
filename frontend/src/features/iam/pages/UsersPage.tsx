import { useEffect, useState } from "react";
import { Ban, Eye, RotateCcw, Search, ShieldCheck } from "lucide-react";
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
import {
  useUsers,
  useBlacklistUser,
  useBatchBlacklistUsers,
  useRestoreUser,
  useUserDetail,
  useDataReads,
  useDeletionRequests,
  useApproveDeletionRequest,
  useRejectDeletionRequest,
} from "@/features/iam/queries";
import { ROLE_LABEL } from "@/lib/auth/permissions";
import { formatDate, formatDateTime, fromNow } from "@/lib/format/date";
import { formatCNY } from "@/lib/format/money";
import { maskEmail, maskName, maskPhone, maskStudentNo, maskText } from "@/lib/mask";
import type { DeletionRequestView } from "@/features/iam/api";
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
  const [batchOpen, setBatchOpen] = useState(false);
  const [batchReason, setBatchReason] = useState("");
  const [view, setView] = useState<"USERS" | "DELETION">("USERS");
  const [detailUser, setDetailUser] = useState<AdminUser | null>(null);
  const [approveTarget, setApproveTarget] = useState<DeletionRequestView | null>(null);
  const [rejectTarget, setRejectTarget] = useState<DeletionRequestView | null>(null);
  const [rejectNote, setRejectNote] = useState("");

  const query = useUsers({
    role: role === "ALL" ? undefined : role,
    keyword: keyword || undefined,
    page,
    size: 12,
  });
  const blacklist = useBlacklistUser();
  const batchBlacklist = useBatchBlacklistUsers();
  const restore = useRestoreUser();
  // 先读详情（后端写 DATA_READ），成功后再回放该用户的读取记录
  const userDetail = useUserDetail(detailUser?.id ?? null);
  const reads = useDataReads(detailUser && userDetail.isSuccess ? detailUser.id : null);
  const deletions = useDeletionRequests("PENDING");
  const approveDeletion = useApproveDeletionRequest();
  const rejectDeletion = useRejectDeletionRequest();
  const items = query.data?.items ?? [];
  const deletionItems = deletions.data ?? [];
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
          toast.success(`已拉黑 ${maskName(target.name)}`, { description: "其借出与预约权限已冻结。" });
        },
      },
    );
  };

  const submitBatchBlacklist = () => {
    if (batchReason.trim().length < 6) {
      toast.error("请填写不少于 6 字的拉黑原因");
      return;
    }
    batchBlacklist.mutate(
      { ids: selected, reason: batchReason.trim() },
      {
        onSuccess: () => {
          setBatchOpen(false);
          setBatchReason("");
          setSelected([]);
        },
      },
    );
  };

  useEffect(() => {
    if (detailUser && userDetail.isSuccess) {
      void reads.refetch();
    }
    // 每次详情请求成功都刷新读取记录，确保刚写入的 DATA_READ 立即可见
  }, [detailUser, userDetail.dataUpdatedAt]);

  const submitApproveDeletion = () => {
    if (!approveTarget) return;
    approveDeletion.mutate(approveTarget.id, { onSuccess: () => setApproveTarget(null) });
  };

  const submitRejectDeletion = () => {
    if (!rejectTarget) return;
    const note = rejectNote.trim();
    if (!note) {
      toast.error("请填写驳回备注");
      return;
    }
    rejectDeletion.mutate(
      { id: rejectTarget.id, note },
      {
        onSuccess: () => {
          setRejectTarget(null);
          setRejectNote("");
        },
      },
    );
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="用户与权限"
        description={
          view === "USERS"
            ? "角色决定可用功能；拉黑会冻结借出、预约与续借，历史记录保留。"
            : "读者提交注销后在此审批；批准会立即打码并删除证据文件，操作不可撤销。"
        }
        actions={
          view === "USERS" ? (
            <Button
              size="sm"
              variant="outline"
              disabled={!selected.length}
              onClick={() => {
                setBatchReason("");
                setBatchOpen(true);
              }}
            >
              <ShieldCheck className="size-3.5" />
              批量处理{selected.length ? `（${selected.length}）` : ""}
            </Button>
          ) : null
        }
      />

      <SegmentedFilter
        value={view}
        onChange={setView}
        items={[
          { value: "USERS", label: "用户列表" },
          { value: "DELETION", label: "注销申请", count: deletionItems.length },
        ]}
      />

      {view === "USERS" ? (
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
                    aria-label={`选择 ${maskName(user.name)}`}
                  />
                </TableCell>
                <TableCell>
                  <div className="flex items-center gap-2">
                    <span className="grid size-6 place-items-center rounded-full bg-mint-soft text-[9.5px] font-semibold text-mint-deep">
                      {maskName(user.name).slice(0, 1)}
                    </span>
                    <div>
                      <p className="font-medium text-ink">{maskName(user.name)}</p>
                      <p className="tabular text-[9px] text-ink-3">
                        {maskStudentNo(user.studentNo)} · {user.college}
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
                      {maskText(user.blacklistReason)}
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
                <TableCell className="whitespace-nowrap text-right">
                  <Button variant="ghost" size="xs" onClick={() => setDetailUser(user)}>
                    <Eye className="size-3" /> 详情
                  </Button>
                  {user.role !== "READER" ? (
                    <span className="text-[9.5px] text-ink-3">—</span>
                  ) : user.status === "BLACKLISTED" ? (
                    <Button
                      variant="ghost"
                      size="xs"
                      disabled={restore.isPending}
                      onClick={() =>
                        restore.mutate(user.id, { onSuccess: () => toast.success(`已恢复 ${maskName(user.name)} 的权限`) })
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
      ) : (
        <SectionCard bodyClassName="px-6 pb-5">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div>
              <h3 className="text-[13px] font-semibold text-ink">待处理的注销申请</h3>
              <p className="mt-0.5 text-[10px] leading-relaxed text-ink-3">
                批准后立即打码姓名、手机与邮箱，删除证据文件并关闭授权；此操作不可撤销。
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              disabled={deletions.isFetching}
              onClick={() => void deletions.refetch()}
            >
              <RotateCcw className="size-3.5" /> 刷新
            </Button>
          </div>

          <Table className="mt-3">
            <TableHeader>
              <TableRow className="hover:bg-transparent">
                <TableHead>读者</TableHead>
                <TableHead>注销原因</TableHead>
                <TableHead>提交时间</TableHead>
                <TableHead className="text-right">操作</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {deletionItems.map((request) => (
                <TableRow key={request.id}>
                  <TableCell>
                    <p className="font-medium text-ink">{maskName(request.userName ?? "读者")}</p>
                    <p className="tabular text-[9px] text-ink-3">
                      {maskStudentNo(request.studentNo) || "—"}
                    </p>
                  </TableCell>
                  <TableCell className="max-w-[260px] text-[10.5px] text-ink-2">
                    {maskText(request.reason) || "—"}
                  </TableCell>
                  <TableCell className="whitespace-nowrap text-[10px] text-ink-3">
                    {formatDateTime(request.createdAt)}
                  </TableCell>
                  <TableCell className="whitespace-nowrap text-right">
                    <Button variant="ghost" size="xs" onClick={() => setApproveTarget(request)}>
                      批准
                    </Button>
                    <Button
                      variant="ghost"
                      size="xs"
                      className="text-danger hover:bg-danger-soft hover:text-danger"
                      onClick={() => {
                        setRejectTarget(request);
                        setRejectNote("");
                      }}
                    >
                      驳回
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>

          {deletions.isLoading ? (
            <p className="py-6 text-center text-[10.5px] text-ink-3">加载中…</p>
          ) : deletionItems.length === 0 ? (
            <EmptyState title="暂无待处理的注销申请" description="读者提交注销后会出现在这里。" />
          ) : null}
        </SectionCard>
      )}

      <Dialog open={Boolean(target)} onOpenChange={(open) => !open && setTarget(null)}>
        <DialogContent className="max-w-[380px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">拉黑 {maskName(target?.name)}</DialogTitle>
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

      <Dialog open={batchOpen} onOpenChange={(open) => !open && setBatchOpen(false)}>
        <DialogContent className="max-w-[380px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">批量拉黑 {selected.length} 位读者</DialogTitle>
            <DialogDescription className="text-[10.5px] text-ink-3">
              同一原因会写入每位读者的审计日志；已借图书仍需归还。失败的行会单独提示。
            </DialogDescription>
          </DialogHeader>
          <Textarea
            value={batchReason}
            onChange={(event) => setBatchReason(event.target.value)}
            placeholder="例如：期末集中逾期且未回应催还（≥6 字）"
            className="min-h-[72px] resize-none text-[11px]"
          />
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setBatchOpen(false)}>
              取消
            </Button>
            <Button
              size="sm"
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={batchBlacklist.isPending}
              onClick={submitBatchBlacklist}
            >
              确认拉黑 {selected.length} 位
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(detailUser)} onOpenChange={(open) => !open && setDetailUser(null)}>
        <DialogContent className="max-w-[420px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">用户详情</DialogTitle>
            <DialogDescription className="text-[10.5px] text-ink-3">
              敏感字段已脱敏；本次查看会写入数据访问记录。
            </DialogDescription>
          </DialogHeader>
          {detailUser ? (
            <div className="space-y-3">
              <div className="flex items-center gap-2.5">
                <span className="grid size-8 place-items-center rounded-full bg-mint-soft text-[11px] font-semibold text-mint-deep">
                  {maskName(detailUser.name).slice(0, 1)}
                </span>
                <div>
                  <p className="text-[13px] font-semibold text-ink">{maskName(detailUser.name)}</p>
                  <p className="tabular text-[10px] text-ink-3">
                    {maskStudentNo(detailUser.studentNo)} · {detailUser.college}
                  </p>
                </div>
                <span className="ml-auto text-[10px] text-ink-3">{ROLE_LABEL[detailUser.role]}</span>
              </div>

              <dl className="grid grid-cols-2 gap-x-3 gap-y-2 rounded-lg bg-wash p-3 text-[10.5px]">
                <div>
                  <dt className="text-[9px] text-ink-3">手机</dt>
                  <dd className="tabular text-ink-2">{maskPhone(detailUser.phone) || "—"}</dd>
                </div>
                <div>
                  <dt className="text-[9px] text-ink-3">邮箱</dt>
                  <dd className="truncate text-ink-2">{maskEmail(detailUser.email) || "—"}</dd>
                </div>
                <div>
                  <dt className="text-[9px] text-ink-3">状态</dt>
                  <dd className="text-ink-2">{statusMeta[detailUser.status].label}</dd>
                </div>
                <div>
                  <dt className="text-[9px] text-ink-3">借阅 / 滞纳金</dt>
                  <dd className="tabular text-ink-2">
                    {detailUser.borrowCount} 册 · {formatCNY(detailUser.fine)}
                  </dd>
                </div>
              </dl>

              <div>
                <p className="text-[10px] font-semibold text-ink">数据访问记录</p>
                <p className="mt-0.5 text-[9px] text-ink-3">最近 20 次查看该用户敏感信息的操作。</p>
                {reads.isLoading ? (
                  <p className="py-3 text-[10px] text-ink-3">加载中…</p>
                ) : reads.data && reads.data.length > 0 ? (
                  <ul className="mt-1.5 space-y-1">
                    {reads.data.map((record) => (
                      <li key={record.id} className="flex items-center justify-between gap-3 text-[10px]">
                        <span className="text-ink-2">{record.operatorName}</span>
                        <span className="tabular text-ink-3">{formatDateTime(record.createdAt)}</span>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="py-3 text-[10px] text-ink-3">暂无查看记录</p>
                )}
              </div>
            </div>
          ) : null}
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setDetailUser(null)}>
              关闭
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(approveTarget)} onOpenChange={(open) => !open && setApproveTarget(null)}>
        <DialogContent className="max-w-[400px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">
              批准 {maskName(approveTarget?.userName ?? "读者")} 的注销申请？
            </DialogTitle>
            <DialogDescription className="text-[10.5px] leading-relaxed text-ink-3">
              将立即打码该用户的姓名、手机与邮箱，删除其证据文件，并关闭账号授权。此操作不可撤销。
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setApproveTarget(null)}>
              取消
            </Button>
            <Button
              size="sm"
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              disabled={approveDeletion.isPending}
              onClick={submitApproveDeletion}
            >
              {approveDeletion.isPending ? "处理中…" : "确认批准并打码"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={Boolean(rejectTarget)} onOpenChange={(open) => !open && setRejectTarget(null)}>
        <DialogContent className="max-w-[400px]">
          <DialogHeader>
            <DialogTitle className="text-[15px]">驳回注销申请</DialogTitle>
            <DialogDescription className="text-[10.5px] text-ink-3">
              备注会通知读者；驳回后账号与数据保持不变。
            </DialogDescription>
          </DialogHeader>
          <Textarea
            value={rejectNote}
            onChange={(event) => setRejectNote(event.target.value)}
            placeholder="填写驳回原因（必填）"
            className="min-h-[72px] resize-none text-[11px]"
          />
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setRejectTarget(null)}>
              取消
            </Button>
            <Button size="sm" disabled={rejectDeletion.isPending} onClick={submitRejectDeletion}>
              {rejectDeletion.isPending ? "提交中…" : "确认驳回"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
