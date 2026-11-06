import { useState } from "react";
import { MessageSquareText, RefreshCw, Send } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Switch } from "@/components/ui/switch";
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
import { useTemplates, useUpdateTemplate, useNotifyTasks, useRetryNotifyTask } from "@/features/notify/queries";
import { formatDateTime, fromNow } from "@/lib/format/date";
import type { NotifyChannel, NotifyStatus, NotifyTemplate } from "@/lib/api/models";

const sceneLabel: Record<string, string> = {
  RETURN: "借还提醒",
  GRACE: "宽限通知",
  APPEAL: "申诉通知",
  RESERVATION: "预约通知",
  SYSTEM: "系统通知",
};

const channelLabel: Record<NotifyChannel, string> = {
  SMS: "短信",
  WECHAT: "微信",
  EMAIL: "邮件",
  INAPP: "站内信",
};

const taskMeta: Record<NotifyStatus, { label: string; tone: "mint" | "amber" | "red" | "slate" }> = {
  PENDING: { label: "排队中", tone: "slate" },
  SENDING: { label: "发送中", tone: "amber" },
  SENT: { label: "已送达", tone: "mint" },
  PARTIAL: { label: "部分失败", tone: "amber" },
  FAILED: { label: "发送失败", tone: "red" },
};

export function NotifyPage() {
  const [tab, setTab] = useState<"TASKS" | "TEMPLATES">("TASKS");
  const [page, setPage] = useState(1);
  const tasks = useNotifyTasks(page, 10);
  const templates = useTemplates();
  const retry = useRetryNotifyTask();
  const update = useUpdateTemplate();
  const [editing, setEditing] = useState<NotifyTemplate | null>(null);
  const [draft, setDraft] = useState("");

  return (
    <div className="space-y-4">
      <PageHeader
        title="提醒中心"
        description="到期、逾期、预约到馆共用一套模板；失败任务可单条重试，避免重复打扰。"
        actions={
          <Button size="sm" variant="outline" onClick={() => setTab("TEMPLATES")}>
            <MessageSquareText className="size-3.5" />
            模板管理
          </Button>
        }
      />

      <SectionCard bodyClassName="px-5 pb-4">
        <SegmentedFilter
          value={tab}
          onChange={(value) => setTab(value)}
          items={[
            { value: "TASKS", label: "发送任务" },
            { value: "TEMPLATES", label: "消息模板" },
          ]}
        />

        {tab === "TASKS" ? (
          <>
            <Table className="mt-3">
              <TableHeader>
                <TableRow className="hover:bg-transparent">
                  <TableHead className="pl-0">任务</TableHead>
                  <TableHead>模板 / 渠道</TableHead>
                  <TableHead>触达</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead>时间</TableHead>
                  <TableHead className="pr-0 text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {(tasks.data?.items ?? []).map((task) => (
                  <TableRow key={task.id}>
                    <TableCell className="pl-0">
                      <p className="font-medium text-ink">{task.title}</p>
                      <p className="tabular text-[9.5px] text-ink-3">{task.id}</p>
                    </TableCell>
                    <TableCell>
                      <p className="text-ink-2">{task.templateName}</p>
                      <p className="text-[9.5px] text-ink-3">{channelLabel[task.channel]}</p>
                    </TableCell>
                    <TableCell className="tabular text-[10.5px]">
                      <span className="text-ink-2">
                        {task.successCount}/{task.targetCount}
                      </span>
                      {task.failCount > 0 ? (
                        <span className="ml-1 text-danger">失败 {task.failCount}</span>
                      ) : null}
                    </TableCell>
                    <TableCell>
                      <StatusPill tone={taskMeta[task.status].tone} dot>
                        {taskMeta[task.status].label}
                      </StatusPill>
                      {task.error ? (
                        <p className="mt-0.5 max-w-[150px] truncate text-[9px] text-danger">{task.error}</p>
                      ) : null}
                    </TableCell>
                    <TableCell className="text-[10px] text-ink-3">
                      {fromNow(task.createdAt)}
                      {task.finishedAt ? (
                        <span className="block text-[9px]">{formatDateTime(task.finishedAt)}</span>
                      ) : null}
                    </TableCell>
                    <TableCell className="pr-0 text-right">
                      {task.status === "FAILED" || task.status === "PARTIAL" ? (
                        <Button
                          variant="outline"
                          size="xs"
                          disabled={retry.isPending}
                          onClick={() =>
                            retry.mutate(task.id)
                          }
                        >
                          <RefreshCw className="size-3" /> 重试
                        </Button>
                      ) : (
                        <span className="text-[9.5px] text-ink-3">—</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>

            {!tasks.isLoading && (tasks.data?.items.length ?? 0) === 0 ? (
              <EmptyState title="暂无发送任务" description="到期提醒会在每天 17:00 自动生成任务。" />
            ) : null}

            <div className="mt-3">
              <Pager page={page} size={10} total={tasks.data?.total ?? 0} onChange={setPage} />
            </div>
          </>
        ) : (
          <div className="mt-3 space-y-2">
            {(templates.data ?? []).map((template) => (
              <div
                key={template.id}
                className="rounded-xl border border-line bg-card px-4 py-3"
              >
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-[12px] font-semibold text-ink">{template.name}</p>
                    <p className="mt-0.5 text-[9.5px] text-ink-3">
                      {sceneLabel[template.scene] ?? template.scene} · {channelLabel[template.channel]} · 更新于 {fromNow(template.updatedAt)}
                    </p>
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      variant="ghost"
                      size="xs"
                      onClick={() => {
                        setEditing(template);
                        setDraft(template.content);
                      }}
                    >
                      编辑
                    </Button>
                  <label className="flex items-center gap-2 text-[9.5px] text-ink-3">
                    {template.enabled ? "启用" : "停用"}
                    <Switch
                      checked={template.enabled}
                      disabled={update.isPending}
                      onCheckedChange={(checked) =>
                        update.mutate({ id: template.id, payload: { enabled: checked } })
                      }
                    />
                  </label>
                  </div>
                </div>
                <p className="mt-2 rounded-lg bg-wash px-3 py-2 text-[10.5px] leading-5 text-ink-2">
                  {template.content}
                </p>
                <div className="mt-1.5 flex flex-wrap items-center gap-1">
                  {template.variables.map((variable) => (
                    <span
                      key={variable}
                      className="rounded bg-sunken px-1.5 py-0.5 font-mono text-[9px] text-ink-3"
                    >
                      {`{${variable}}`}
                    </span>
                  ))}
                  <span className="ml-auto flex items-center gap-1 text-[9px] text-ink-3">
                    <Send className="size-3" /> 变量在发送时替换
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </SectionCard>

      <Dialog open={editing !== null} onOpenChange={(open) => !open && setEditing(null)}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="text-[15px]">编辑模板 · {editing?.name}</DialogTitle>
            <DialogDescription className="text-[10.5px]">
              支持变量占位符，发送时按读者与书目数据替换；保存后立即用于新任务。
            </DialogDescription>
          </DialogHeader>
          <Textarea
            value={draft}
            onChange={(event) => setDraft(event.target.value)}
            rows={5}
            className="text-[11px] leading-5"
            placeholder="模板正文"
          />
          <div className="flex flex-wrap gap-1">
            {(editing?.variables ?? []).map((variable) => (
              <button
                key={variable}
                type="button"
                className="rounded bg-sunken px-1.5 py-0.5 font-mono text-[9px] text-ink-3 transition-colors hover:bg-mint-soft hover:text-mint-deep"
                onClick={() => setDraft((prev) => `${prev}{${variable}}`)}
              >
                {`{${variable}}`}
              </button>
            ))}
          </div>
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setEditing(null)}>
              取消
            </Button>
            <Button
              size="sm"
              disabled={!draft.trim() || update.isPending}
              onClick={() => {
                if (!editing) return;
                update.mutate(
                  { id: editing.id, payload: { content: draft.trim() } },
                  { onSuccess: () => setEditing(null) },
                );
              }}
            >
              保存模板
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
