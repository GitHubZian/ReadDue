import { useMemo, useState } from "react";
import { ShieldCheck } from "lucide-react";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { StatusPill } from "@/components/common/StatusPill";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { useRoles, usePermissionCatalog, useSaveRolePermissions } from "@/features/iam/queries";
import type { PermissionView } from "@/lib/api/models";

const MODULE_LABEL: Record<string, string> = {
  workbench: "工作台",
  catalog: "书目",
  notify: "通知",
  borrow: "借阅",
  reservation: "预约",
  grace: "宽限",
  appeal: "申诉",
  overdue: "逾期",
  user: "用户",
  rule: "规则",
  audit: "审计",
  privacy: "隐私",
  role: "角色",
};

const SCOPE_LABEL: Record<string, string> = {
  ALL: "全馆数据",
  LIBRARY: "本馆数据",
  SELF: "仅本人",
};

function groupByModule(items: PermissionView[]) {
  const groups = new Map<string, PermissionView[]>();
  for (const item of items) {
    const list = groups.get(item.module) ?? [];
    list.push(item);
    groups.set(item.module, list);
  }
  return [...groups.entries()];
}

export function RolesPage() {
  const roles = useRoles();
  const catalog = usePermissionCatalog();
  const save = useSaveRolePermissions();
  const [roleCode, setRoleCode] = useState<string>("");
  const [checked, setChecked] = useState<string[] | null>(null);

  const current = roles.data?.find((role) => role.code === roleCode) ?? roles.data?.[0];
  const baseline = current?.permissions ?? [];
  const selected = checked ?? baseline;
  const dirty = selected.length !== baseline.length || selected.some((code) => !baseline.includes(code));
  const total = catalog.data?.length ?? 0;
  const groups = useMemo(() => groupByModule(catalog.data ?? []), [catalog.data]);

  const pickRole = (code: string) => {
    setRoleCode(code);
    setChecked(null);
  };

  const toggle = (code: string, next: boolean) => {
    setChecked((prev) => {
      const list = prev ?? baseline;
      return next ? [...list, code] : list.filter((item) => item !== code);
    });
  };

  const onSave = () => {
    if (!current) return;
    save.mutate(
      { code: current.code, permissions: selected },
      { onSuccess: () => setChecked(null) },
    );
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="角色权限"
        description="按角色勾选管理端能力；保存后该角色下一次请求即按新权限校验。"
        actions={
          <StatusPill tone={dirty ? "amber" : "slate"} dot>
            {dirty ? "有未保存的改动" : "已同步"}
          </StatusPill>
        }
      />

      <SectionCard
        title="角色与权限"
        description="勾选表示该角色可访问的菜单与操作；未勾选的入口在路由、菜单、按钮三层同时收敛。"
        action={
          <div className="flex items-center gap-2">
            <Select value={current?.code} onValueChange={pickRole}>
              <SelectTrigger className="h-[34px] w-[132px] text-[11px]">
                <SelectValue placeholder="选择角色" />
              </SelectTrigger>
              <SelectContent>
                {(roles.data ?? []).map((role) => (
                  <SelectItem key={role.code} value={role.code} className="text-[11px]">
                    {role.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button
              variant="outline"
              size="sm"
              disabled={!dirty || save.isPending}
              onClick={() => setChecked(null)}
            >
              撤销改动
            </Button>
            <Button size="sm" disabled={!dirty || save.isPending} onClick={onSave}>
              {save.isPending ? "保存中…" : "保存权限"}
            </Button>
          </div>
        }
      >
        {roles.isLoading || catalog.isLoading ? (
          <div className="space-y-2 py-6">
            <div className="h-8 w-1/3 animate-pulse rounded bg-wash" />
            <div className="h-24 animate-pulse rounded-xl bg-wash" />
          </div>
        ) : roles.isError || catalog.isError ? (
          <p className="py-10 text-center text-[11px] text-ink-3">权限数据加载失败，请稍后重试。</p>
        ) : (
          <>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 border-b border-line pb-3 text-[10px] text-ink-3">
              <span>
                数据范围{" "}
                <span className="font-medium text-ink-2">
                  {SCOPE_LABEL[current?.dataScope ?? ""] ?? current?.dataScope ?? "—"}
                </span>
              </span>
              <span>
                已选 <span className="tabular font-semibold text-ink">{selected.length}</span> / {total} 项
              </span>
              {current?.code === "ADMIN" ? (
                <span className="text-warn">管理员必须保留「角色权限管理」与「审计查询」</span>
              ) : null}
            </div>

            <div className="space-y-4 pt-3">
              {groups.map(([module, items]) => (
                <div key={module}>
                  <p className="mb-1.5 text-[9.5px] font-medium uppercase tracking-wide text-ink-3">
                    {MODULE_LABEL[module] ?? module}
                  </p>
                  <div className="grid grid-cols-1 gap-1.5 sm:grid-cols-2 xl:grid-cols-3">
                    {items.map((item) => {
                      const id = `perm-${item.code}`;
                      return (
                        <Label
                          key={item.code}
                          htmlFor={id}
                          className="flex h-9 cursor-pointer items-center gap-2 rounded-lg border border-line-2 px-2.5 text-[10.5px] font-normal text-ink-2 transition-colors hover:bg-wash"
                        >
                          <Checkbox
                            id={id}
                            checked={selected.includes(item.code)}
                            onCheckedChange={(value) => toggle(item.code, value === true)}
                          />
                          <span className="truncate">{item.name}</span>
                          <code className="ml-auto shrink-0 font-mono text-[9px] text-ink-3">
                            {item.code}
                          </code>
                        </Label>
                      );
                    })}
                  </div>
                </div>
              ))}
            </div>

            {dirty ? (
              <div className="sticky bottom-0 mt-4 flex items-center justify-between rounded-xl border border-line bg-card px-4 py-2.5 shadow-sm">
                <span className="flex items-center gap-2 text-[10.5px] text-ink-2">
                  <ShieldCheck className="size-3.5 text-mint-deep" />
                  将保存 {selected.length} 项权限到「{current?.name}」
                </span>
                <span className="flex items-center gap-2">
                  <Button variant="ghost" size="sm" onClick={() => setChecked(null)}>
                    撤销
                  </Button>
                  <Button size="sm" disabled={save.isPending} onClick={onSave}>
                    保存
                  </Button>
                </span>
              </div>
            ) : null}
          </>
        )}
      </SectionCard>
    </div>
  );
}
