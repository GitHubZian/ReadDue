import { useMemo, useState } from "react";
import { Check, ShieldCheck, Undo2 } from "lucide-react";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { StatusPill } from "@/components/common/StatusPill";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
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

const MODULE_HINT: Record<string, string> = {
  workbench: "总览与待办",
  catalog: "馆藏检索",
  notify: "消息与模板",
  borrow: "借出、归还与续借",
  reservation: "预约队列",
  grace: "宽限审批",
  appeal: "申诉处理",
  overdue: "逾期看板",
  user: "账号与拉黑",
  rule: "参数与校历",
  audit: "操作留痕",
  privacy: "授权与注销",
  role: "角色权限分配",
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

  const percent = total ? Math.round((selected.length / total) * 100) : 0;

  return (
    <div className="space-y-5 pb-16">
      <PageHeader
        title="角色权限"
        description="选择角色后按模块分配能力；保存后该角色下一次请求即按新权限校验。"
        actions={
          <StatusPill tone={dirty ? "amber" : "slate"} dot>
            {dirty ? "有未保存的改动" : "已同步"}
          </StatusPill>
        }
      />

      <div className="grid grid-cols-1 items-start gap-5 lg:grid-cols-[248px_minmax(0,1fr)]">
        <aside className="lg:sticky lg:top-4">
          <p className="px-1 text-[9.5px] font-semibold uppercase tracking-wider text-ink-3">角色</p>
          <div className="mt-2 space-y-1.5">
            {(roles.data ?? []).map((role) => {
              const active = current?.code === role.code;
              return (
                <button
                  key={role.code}
                  type="button"
                  onClick={() => pickRole(role.code)}
                  className={
                    "w-full rounded-xl border px-3.5 py-3 text-left transition-colors " +
                    (active
                      ? "border-mint-deep/30 bg-mint-soft/60"
                      : "border-line bg-card hover:border-line hover:bg-wash/70")
                  }
                >
                  <div className="flex items-center justify-between gap-2">
                    <span className={"text-[12px] font-semibold " + (active ? "text-mint-deep" : "text-ink")}>
                      {role.name}
                    </span>
                    {active ? <Check className="size-3.5 text-mint-deep" /> : null}
                  </div>
                  <p className="mt-0.5 font-mono text-[9px] text-ink-3">{role.code}</p>
                  <div className="mt-2 flex items-center justify-between text-[9.5px] text-ink-3">
                    <span>{SCOPE_LABEL[role.dataScope] ?? role.dataScope}</span>
                    <span className="tabular">{role.permissionCount} 项</span>
                  </div>
                </button>
              );
            })}
          </div>
          <p className="mt-3 rounded-xl border border-dashed border-line bg-wash/60 px-3 py-2.5 text-[9.5px] leading-4 text-ink-3">
            未勾选的入口会在路由、菜单、按钮三层同时收敛；管理员需保留角色与审计权限。
          </p>
        </aside>

        <SectionCard
          title={
            current ? (
              <span className="flex flex-wrap items-center gap-2">
                {current.name}
                <code className="rounded bg-sunken px-1.5 py-0.5 font-mono text-[9px] font-normal text-ink-3">
                  {current.code}
                </code>
              </span>
            ) : (
              "角色与权限"
            )
          }
          description={
            current
              ? `数据范围 ${SCOPE_LABEL[current.dataScope] ?? current.dataScope} · 已选 ${selected.length} / ${total} 项`
              : "按模块勾选该角色可访问的菜单与操作"
          }
          action={
            <div className="hidden items-center gap-2 sm:flex">
              <Button
                variant="outline"
                size="sm"
                disabled={!dirty || save.isPending}
                onClick={() => setChecked(null)}
              >
                <Undo2 className="size-3.5" /> 撤销
              </Button>
              <Button size="sm" disabled={!dirty || save.isPending} onClick={onSave}>
                {save.isPending ? "保存中…" : "保存权限"}
              </Button>
            </div>
          }
        >
          {roles.isLoading || catalog.isLoading ? (
            <div className="space-y-3 py-4">
              <div className="h-2 w-full animate-pulse rounded bg-wash" />
              <div className="h-24 animate-pulse rounded-xl bg-wash" />
              <div className="h-24 animate-pulse rounded-xl bg-wash" />
            </div>
          ) : roles.isError || catalog.isError ? (
            <p className="py-12 text-center text-[11px] text-ink-3">权限数据加载失败，请稍后重试。</p>
          ) : (
            <>
              <div className="mb-4 rounded-xl border border-line-2 bg-wash/50 px-4 py-3">
                <div className="flex items-center justify-between text-[10px] text-ink-3">
                  <span className="flex items-center gap-1.5">
                    <ShieldCheck className="size-3.5 text-mint-deep" />
                    权限覆盖
                  </span>
                  <span className="tabular text-ink-2">
                    {selected.length} / {total} · {percent}%
                  </span>
                </div>
                <div className="mt-2 h-1.5 overflow-hidden rounded-full bg-sunken">
                  <div
                    className="h-full rounded-full bg-mint-deep transition-[width] duration-300"
                    style={{ width: `${percent}%` }}
                  />
                </div>
                {current?.code === "ADMIN" ? (
                  <p className="mt-2 text-[9.5px] text-warn">
                    管理员必须保留「角色权限」与「审计」两项，否则将失去后台入口。
                  </p>
                ) : null}
              </div>

              <div className="space-y-5">
                {groups.map(([module, items]) => (
                  <section key={module}>
                    <header className="mb-1.5 flex items-baseline gap-2 border-b border-line-2 pb-1.5">
                      <h3 className="text-[10.5px] font-semibold text-ink">
                        {MODULE_LABEL[module] ?? module}
                      </h3>
                      <span className="text-[9px] text-ink-3">{MODULE_HINT[module] ?? ""}</span>
                      <span className="tabular ml-auto text-[9px] text-ink-3">{items.length}</span>
                    </header>
                    <div className="grid grid-cols-1 gap-1.5 md:grid-cols-2">
                      {items.map((item) => {
                        const id = `perm-${item.code}`;
                        const active = selected.includes(item.code);
                        return (
                          <Label
                            key={item.code}
                            htmlFor={id}
                            className={
                              "flex cursor-pointer items-center gap-2.5 rounded-lg border px-3 py-2 text-[10.5px] font-normal transition-colors " +
                              (active
                                ? "border-mint/30 bg-mint-soft/35 text-ink"
                                : "border-line-2 text-ink-2 hover:bg-wash")
                            }
                          >
                            <Checkbox
                              id={id}
                              checked={active}
                              onCheckedChange={(value) => toggle(item.code, value === true)}
                            />
                            <span className="min-w-0 flex-1 truncate">{item.name}</span>
                            <code className="shrink-0 font-mono text-[8.5px] text-ink-3">{item.code}</code>
                          </Label>
                        );
                      })}
                    </div>
                  </section>
                ))}
              </div>

              {dirty ? (
                <div className="sticky bottom-2 mt-5 flex items-center justify-between gap-3 rounded-xl border border-mint-deep/20 bg-card/95 px-4 py-2.5 shadow-sm backdrop-blur">
                  <span className="flex min-w-0 items-center gap-2 text-[10.5px] text-ink-2">
                    <ShieldCheck className="size-3.5 shrink-0 text-mint-deep" />
                    <span className="truncate">
                      将保存 {selected.length} 项到「{current?.name}」
                    </span>
                  </span>
                  <span className="flex shrink-0 items-center gap-2">
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
    </div>
  );
}
