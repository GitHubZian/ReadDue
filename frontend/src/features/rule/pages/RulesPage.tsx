import { useEffect, useMemo, useRef, useState } from "react";
import { AlertTriangle, Check, RotateCcw, Save } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { useRules, useSaveRules } from "@/features/rule/queries";
import { AcademicCalendarCard, OverdueTiersCard } from "@/features/rule/components/StrategyPanels";
import type { RuleGroup, RuleItem } from "@/lib/api/models";

interface Change {
  group: string;
  item: RuleItem;
  from: string;
  to: string;
}

const GROUP_ICON_TONE = [
  "bg-mint-soft text-mint-deep",
  "bg-warn-soft text-warn",
  "bg-sunken text-ink-2",
  "bg-mint-soft text-mint-deep",
  "bg-danger-soft text-danger",
];

export function RulesPage() {
  const { data, isLoading } = useRules();
  const save = useSaveRules();
  const [draft, setDraft] = useState<RuleGroup[]>([]);
  const [activeGroup, setActiveGroup] = useState(0);
  const sectionRefs = useRef<Array<HTMLDivElement | null>>([]);

  useEffect(() => {
    if (data) setDraft(data.groups);
  }, [data]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries
          .filter((entry) => entry.isIntersecting)
          .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)[0];
        if (visible) {
          const index = Number((visible.target as HTMLElement).dataset.index);
          if (!Number.isNaN(index)) setActiveGroup(index);
        }
      },
      { rootMargin: "-96px 0px -60% 0px", threshold: 0 },
    );
    for (const node of sectionRefs.current) if (node) observer.observe(node);
    return () => observer.disconnect();
  }, [draft.length]);

  const changes: Change[] = useMemo(() => {
    if (!data) return [];
    const source = new Map(data.groups.flatMap((group) => group.items).map((item) => [item.key, item]));
    return draft.flatMap((group) =>
      group.items.flatMap((item) => {
        const before = source.get(item.key);
        if (!before || before.value === item.value) return [];
        return [{ group: group.title, item, from: before.value, to: item.value }];
      }),
    );
  }, [data, draft]);

  const dirtyKeys = new Set(changes.map((change) => change.item.key));
  const totalItems = draft.reduce((sum, group) => sum + group.items.length, 0);

  const setValue = (groupIndex: number, key: string, value: string) => {
    setDraft((prev) =>
      prev.map((group, index) =>
        index !== groupIndex
          ? group
          : { ...group, items: group.items.map((item) => (item.key === key ? { ...item, value } : item)) },
      ),
    );
  };

  const reset = () => {
    if (data) setDraft(data.groups);
  };

  const submit = () => {
    if (!data) return;
    save.mutate(
      { groups: draft, expectedVersion: data.version },
      {
        onSuccess: () => toast.success("规则已保存并即刻生效", { description: "变更已写入审计日志。" }),
      },
    );
  };

  return (
    <div className="space-y-5 pb-16">
      <PageHeader
        title="规则参数"
        description="统一管理借阅、宽限、罚金与通知策略；保存前可逐项撤销，版本号避免多人覆盖。"
      />

      <div className="grid grid-cols-2 divide-line rounded-2xl border border-line bg-card md:grid-cols-4 md:divide-x">
        {[
          { label: "规则分组", value: draft.length, hint: "借阅 / 宽限 / 申诉 / 通知 / 罚金" },
          { label: "参数项", value: totalItems, hint: "后端下发的可调项" },
          { label: "当前版本", value: data?.version ?? "—", hint: "保存时校验版本" },
          { label: "待保存", value: changes.length, hint: changes.length ? "保存后立即生效" : "暂无改动" },
        ].map((item, index) => (
          <div key={item.label} className={index > 1 ? "border-t border-line md:border-t-0" : ""}>
            <div className="px-5 py-3.5">
              <p className="text-[9.5px] font-medium uppercase tracking-wider text-ink-3">{item.label}</p>
              <p className="mt-1 flex items-baseline gap-1.5">
                <span className="tabular text-[22px] font-bold leading-none text-ink">{item.value}</span>
                {index === 3 && changes.length ? (
                  <AlertTriangle className="size-3.5 text-warn" />
                ) : null}
              </p>
              <p className="mt-1 text-[9.5px] text-ink-3">{item.hint}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="flex items-start gap-5">
        <aside className="sticky top-4 hidden w-[184px] shrink-0 lg:block">
          <p className="px-2 text-[9.5px] font-semibold uppercase tracking-wider text-ink-3">参数分组</p>
          <nav className="mt-2 space-y-0.5">
            {draft.map((group, index) => (
              <a
                key={group.id}
                href={`#rules-${group.id}`}
                onClick={() => setActiveGroup(index)}
                className={
                  "flex items-center gap-2 rounded-lg px-2 py-2 text-[11px] transition-colors " +
                  (activeGroup === index
                    ? "bg-mint-soft font-semibold text-mint-deep"
                    : "text-ink-2 hover:bg-wash")
                }
              >
                <span className="tabular w-4 text-[9.5px] text-ink-3">
                  {String(index + 1).padStart(2, "0")}
                </span>
                <span className="min-w-0 flex-1 truncate">{group.title}</span>
                <span className="tabular text-[9px] text-ink-3">{group.items.length}</span>
              </a>
            ))}
          </nav>
          <p className="mt-4 rounded-xl border border-dashed border-line bg-wash/60 px-3 py-2.5 text-[9.5px] leading-4 text-ink-3">
            改动只在本页生效前保留；离开页面前请保存，或使用底部「撤销」。
          </p>
        </aside>

        <div className="min-w-0 flex-1 space-y-4">
          {isLoading ? (
            <div className="space-y-4">
              {Array.from({ length: 3 }).map((_, index) => (
                <div key={index} className="h-56 animate-pulse rounded-2xl bg-wash" />
              ))}
            </div>
          ) : (
            draft.map((group, groupIndex) => (
              <div
                key={group.id}
                id={`rules-${group.id}`}
                data-index={groupIndex}
                ref={(node) => {
                  sectionRefs.current[groupIndex] = node;
                }}
                className="scroll-mt-6"
              >
                <SectionCard
                  title={
                    <span className="flex items-center gap-2">
                      <span
                        className={
                          "grid size-5 place-items-center rounded-md text-[9.5px] font-bold tabular " +
                          GROUP_ICON_TONE[groupIndex % GROUP_ICON_TONE.length]
                        }
                      >
                        {String(groupIndex + 1).padStart(2, "0")}
                      </span>
                      {group.title}
                      <span className="text-[9.5px] font-normal text-ink-3">
                        {group.items.length} 项
                      </span>
                    </span>
                  }
                  description={group.description}
                >
                  <div className="grid gap-x-10 md:grid-cols-2">
                    {group.items.map((item) => {
                      const dirty = dirtyKeys.has(item.key);
                      return (
                        <div
                          key={item.key}
                          className={
                            "flex items-center justify-between gap-4 border-b border-line-2 py-3.5 last:border-b-0 " +
                            (dirty ? "-mx-2 rounded-lg border-transparent bg-warn-soft/60 px-2" : "")
                          }
                        >
                          <div className="min-w-0">
                            <p className="flex items-center gap-1.5 text-[11px] font-medium text-ink">
                              {item.label}
                              {dirty ? <AlertTriangle className="size-3 text-warn" /> : null}
                            </p>
                            <p className="mt-0.5 text-[9.5px] leading-4 text-ink-3">{item.description}</p>
                          </div>

                          <div className="shrink-0">
                            {item.type === "boolean" ? (
                              <Switch
                                checked={item.value === "true"}
                                onCheckedChange={(checked) =>
                                  setValue(groupIndex, item.key, checked ? "true" : "false")
                                }
                              />
                            ) : item.type === "select" ? (
                              <Select
                                value={item.value}
                                onValueChange={(value) => setValue(groupIndex, item.key, value)}
                              >
                                <SelectTrigger className="h-8 w-[132px] text-[10.5px]">
                                  <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                  {(item.options ?? []).map((option) => (
                                    <SelectItem key={option.value} value={option.value}>
                                      {option.label}
                                    </SelectItem>
                                  ))}
                                </SelectContent>
                              </Select>
                            ) : (
                              <div className="flex items-center gap-1.5">
                                <Input
                                  type="number"
                                  inputMode="decimal"
                                  value={item.value}
                                  min={item.min}
                                  max={item.max}
                                  onChange={(event) => setValue(groupIndex, item.key, event.target.value)}
                                  className="tabular h-8 w-[92px] text-right text-[11px]"
                                />
                                <span className="w-10 text-[9.5px] text-ink-3">{item.unit}</span>
                              </div>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </SectionCard>
              </div>
            ))
          )}

          <div className="grid grid-cols-1 gap-4">
            <OverdueTiersCard />
            <AcademicCalendarCard />
          </div>
        </div>
      </div>

      {changes.length ? (
        <div className="sticky bottom-0 z-10">
          <div className="flex flex-wrap items-center gap-3 rounded-2xl border border-mint-deep/20 bg-card/95 px-5 py-3 shadow-sm backdrop-blur">
            <div className="min-w-0 flex-1">
              <p className="flex items-center gap-1.5 text-[11px] font-semibold text-ink">
                <AlertTriangle className="size-3.5 text-warn" />
                {changes.length} 项改动待保存
              </p>
              <p className="mt-0.5 truncate text-[9.5px] text-ink-3">
                {changes
                  .slice(0, 3)
                  .map((change) => `${change.item.label} ${change.from} → ${change.to}`)
                  .join(" · ")}
                {changes.length > 3 ? ` 等 ${changes.length} 项` : ""}
              </p>
            </div>
            <Button variant="ghost" size="sm" onClick={reset} disabled={save.isPending}>
              <RotateCcw className="size-3.5" /> 撤销
            </Button>
            <Button size="sm" onClick={submit} disabled={save.isPending}>
              {save.isPending ? <Check className="size-3.5" /> : <Save className="size-3.5" />}
              {save.isPending ? "保存中…" : "保存变更"}
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
