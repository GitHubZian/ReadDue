import { useEffect, useMemo, useState } from "react";
import { AlertTriangle, RotateCcw, Save } from "lucide-react";
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
import type { RuleGroup, RuleItem } from "@/lib/api/models";

interface Change {
  group: string;
  item: RuleItem;
  from: string;
  to: string;
}

export function RulesPage() {
  const { data, isLoading } = useRules();
  const save = useSaveRules();
  const [draft, setDraft] = useState<RuleGroup[]>([]);

  useEffect(() => {
    if (data) setDraft(data.groups);
  }, [data]);

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
    <div className="space-y-4 pb-16">
      <PageHeader
        title="规则参数"
        description="改动仅在本页生效前可撤销；保存会校验版本号，避免多人同时编辑互相覆盖。"
        actions={
          <p className="text-[10px] text-ink-3">
            版本 <span className="tabular font-semibold text-ink-2">{data?.version ?? "—"}</span>
          </p>
        }
      />

      {isLoading ? (
        <div className="grid grid-cols-1 items-start gap-4 xl:grid-cols-2">
          {Array.from({ length: 4 }).map((_, index) => (
            <div key={index} className="h-52 animate-pulse rounded-2xl bg-wash" />
          ))}
        </div>
      ) : (
        <div className="grid grid-cols-1 items-start gap-4 xl:grid-cols-2">
          {draft.map((group, groupIndex) => (
            <SectionCard key={group.id} title={group.title} description={group.description}>
              <ul className="divide-y divide-line-2">
                {group.items.map((item) => {
                  const dirty = dirtyKeys.has(item.key);
                  return (
                    <li
                      key={item.key}
                      className={
                        "flex items-center justify-between gap-4 py-2.5 " +
                        (dirty ? "-mx-2 rounded-lg bg-warn-soft/50 px-2" : "")
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
                            <SelectTrigger className="h-8 w-[124px] text-[10.5px]">
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
                              className="tabular h-8 w-[88px] text-right text-[11px]"
                            />
                            <span className="w-9 text-[9.5px] text-ink-3">{item.unit}</span>
                          </div>
                        )}
                      </div>
                    </li>
                  );
                })}
              </ul>
            </SectionCard>
          ))}
        </div>
      )}

      {changes.length ? (
        <div className="sticky bottom-0 z-10">
          <div className="flex flex-wrap items-center gap-3 rounded-2xl border border-mint-deep/20 bg-card/95 px-4 py-3 shadow-sm backdrop-blur">
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
              <Save className="size-3.5" />
              保存变更
            </Button>
          </div>
        </div>
      ) : null}
    </div>
  );
}
