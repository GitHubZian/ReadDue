import { useState } from "react";
import { Layers, Pencil, Plus, Trash2 } from "lucide-react";
import { SectionCard } from "@/components/common/SectionCard";
import { StatusPill } from "@/components/common/StatusPill";
import { EmptyState } from "@/components/common/EmptyState";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import {
  useCalendar,
  useCreateCalendar,
  useDeleteCalendar,
  useTiers,
  useUpdateCalendar,
} from "@/features/rule/queries";
import type { CalendarEntry, TierRow } from "@/lib/api/models";

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

const SCENE_LABEL: Record<string, string> = {
  EXAM: "集中考试",
  THESIS: "论文写作",
  GRADUATION: "毕业季",
  NORMAL: "日常",
};

const SOURCE_LABEL: Record<string, string> = {
  CALENDAR: "校历同步",
  DECLARED: "自主申报",
};

const EMPTY_FORM = {
  term: "",
  scene: "EXAM",
  startDate: "",
  endDate: "",
  source: "CALENDAR",
  note: "",
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

/** 校历：考试周 / 论文期 / 毕业季的起止区间，供宽限与逾期策略命中。 */
export function AcademicCalendarCard() {
  const { data, isLoading } = useCalendar();
  const create = useCreateCalendar();
  const update = useUpdateCalendar();
  const remove = useDeleteCalendar();
  const [editing, setEditing] = useState<CalendarEntry | null>(null);
  const [pendingDelete, setPendingDelete] = useState<CalendarEntry | null>(null);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState(EMPTY_FORM);
  const [error, setError] = useState("");
  const entries = data ?? [];
  const saving = create.isPending || update.isPending;

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY_FORM);
    setError("");
    setOpen(true);
  };

  const openEdit = (entry: CalendarEntry) => {
    setEditing(entry);
    setForm({
      term: entry.term,
      scene: entry.scene,
      startDate: entry.startDate,
      endDate: entry.endDate,
      source: entry.source,
      note: entry.note,
    });
    setError("");
    setOpen(true);
  };

  const submit = () => {
    if (!form.term.trim()) return setError("请填写学期");
    if (!/^\d{4}-(SPRING|SUMMER|FALL|WINTER)$/.test(form.term.trim()))
      return setError("学期格式应为 2026-FALL");
    if (!form.startDate || !form.endDate) return setError("请填写起止日期");
    if (form.endDate < form.startDate) return setError("结束日期不能早于开始日期");
    setError("");
    const payload = { ...form, term: form.term.trim(), note: form.note.trim() };
    const done = { onSuccess: () => setOpen(false) };
    if (editing) update.mutate({ id: editing.id, payload }, done);
    else create.mutate(payload, done);
  };

  return (
    <SectionCard
      title="校历命中"
      description="宽限与逾期策略按校历判断考试周、论文期与毕业季；新增或调整后即时生效。"
      action={
        <Button variant="outline" size="sm" onClick={openCreate}>
          <Plus className="size-3.5" /> 新增校历
        </Button>
      }
    >
      {isLoading ? (
        <div className="h-28 animate-pulse rounded-lg bg-wash" />
      ) : entries.length === 0 ? (
        <EmptyState
          className="py-6"
          title="暂无校历条目"
          description="未命中任何校历区间时，所有学生按日常基准宽限计算。"
          action={
            <Button variant="outline" size="sm" onClick={openCreate}>
              录入第一条
            </Button>
          }
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
              <TableHead>备注</TableHead>
              <TableHead className="pr-0 text-right">操作</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {entries.map((entry) => (
              <TableRow key={entry.id}>
                <TableCell className="pl-0 font-medium text-ink">{entry.term}</TableCell>
                <TableCell>
                  <StatusPill tone="mint">{SCENE_LABEL[entry.scene] ?? entry.scene}</StatusPill>
                </TableCell>
                <TableCell className="tabular text-ink-2">{entry.startDate}</TableCell>
                <TableCell className="tabular text-ink-2">{entry.endDate}</TableCell>
                <TableCell className="text-ink-3">
                  {SOURCE_LABEL[entry.source] ?? entry.source}
                </TableCell>
                <TableCell className="max-w-[220px] truncate text-[10.5px] text-ink-3">
                  {entry.note || "—"}
                </TableCell>
                <TableCell className="pr-0 text-right">
                  <div className="flex justify-end gap-1">
                    <Button variant="ghost" size="icon-xs" onClick={() => openEdit(entry)}>
                      <Pencil className="size-3" />
                      <span className="sr-only">编辑</span>
                    </Button>
                    <Button
                      variant="ghost"
                      size="icon-xs"
                      className="text-danger hover:bg-danger-soft hover:text-danger"
                      onClick={() => setPendingDelete(entry)}
                    >
                      <Trash2 className="size-3" />
                      <span className="sr-only">删除</span>
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent className="max-w-lg">
          <DialogHeader>
            <DialogTitle className="text-[15px]">
              {editing ? "编辑校历条目" : "新增校历条目"}
            </DialogTitle>
            <DialogDescription className="text-[10.5px]">
              同一场景的日期区间不能重叠；保存后宽限与逾期策略按新校历命中。
            </DialogDescription>
          </DialogHeader>
          <div className="grid grid-cols-2 gap-3">
            <div className="space-y-1">
              <Label htmlFor="cal-term" className="text-[10px] text-ink-3">
                学期
              </Label>
              <Input
                id="cal-term"
                value={form.term}
                placeholder="2026-FALL"
                className="h-[34px] text-[11px]"
                onChange={(event) => setForm({ ...form, term: event.target.value })}
              />
            </div>
            <div className="space-y-1">
              <Label className="text-[10px] text-ink-3">场景</Label>
              <Select
                value={form.scene}
                onValueChange={(scene) => setForm({ ...form, scene })}
              >
                <SelectTrigger className="h-[34px] text-[11px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(SCENE_LABEL).map(([value, label]) => (
                    <SelectItem key={value} value={value} className="text-[11px]">
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-1">
              <Label htmlFor="cal-start" className="text-[10px] text-ink-3">
                开始日期
              </Label>
              <Input
                id="cal-start"
                type="date"
                value={form.startDate}
                className="h-[34px] text-[11px]"
                onChange={(event) => setForm({ ...form, startDate: event.target.value })}
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="cal-end" className="text-[10px] text-ink-3">
                结束日期
              </Label>
              <Input
                id="cal-end"
                type="date"
                value={form.endDate}
                className="h-[34px] text-[11px]"
                onChange={(event) => setForm({ ...form, endDate: event.target.value })}
              />
            </div>
            <div className="space-y-1">
              <Label htmlFor="cal-source" className="text-[10px] text-ink-3">
                来源
              </Label>
              <Select
                value={form.source}
                onValueChange={(value) => setForm({ ...form, source: value })}
              >
                <SelectTrigger id="cal-source" className="h-[34px] text-[11px]">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CALENDAR" className="text-[11px]">
                    校历同步
                  </SelectItem>
                  <SelectItem value="DECLARED" className="text-[11px]">
                    自主申报
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div className="col-span-2 space-y-1">
              <Label htmlFor="cal-note" className="text-[10px] text-ink-3">
                备注
              </Label>
              <Textarea
                id="cal-note"
                rows={2}
                value={form.note}
                placeholder="例如：期中考试周，仅影响考试周宽限基准"
                className="text-[11px]"
                onChange={(event) => setForm({ ...form, note: event.target.value })}
              />
            </div>
          </div>
          {error ? <p className="text-[10.5px] text-danger">{error}</p> : null}
          <DialogFooter>
            <Button variant="outline" size="sm" onClick={() => setOpen(false)}>
              取消
            </Button>
            <Button size="sm" disabled={saving} onClick={submit}>
              {saving ? "保存中…" : editing ? "保存修改" : "新增条目"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <AlertDialog
        open={pendingDelete !== null}
        onOpenChange={(next) => !next && setPendingDelete(null)}
      >
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>删除这条校历？</AlertDialogTitle>
            <AlertDialogDescription>
              {pendingDelete?.term} · {SCENE_LABEL[pendingDelete?.scene ?? ""] ?? pendingDelete?.scene}
              （{pendingDelete?.startDate} 至 {pendingDelete?.endDate}）。删除后该区间不再命中宽限与逾期策略。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction
              className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
              onClick={() => {
                if (pendingDelete) remove.mutate(pendingDelete.id);
                setPendingDelete(null);
              }}
            >
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </SectionCard>
  );
}
