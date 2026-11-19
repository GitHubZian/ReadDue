import { useMemo, useState } from "react";
import { ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { PageHeader } from "@/app/layout/PageHeader";
import { SectionCard } from "@/components/common/SectionCard";
import { StatusPill } from "@/components/common/StatusPill";
import {
  PERMISSIONS,
  ROLE_LABEL,
  ROLE_PERMISSIONS,
  type Permission,
} from "@/lib/auth/permissions";
import type { Role } from "@/lib/api/models";

const PERMISSION_LABEL: Record<Permission, string> = {
  "workbench:read": "工作台",
  "borrow:return": "借还办理",
  "reservation:manage": "预约管理",
  "overdue:read": "逾期看板",
  "grace:approve": "宽限审批",
  "appeal:handle": "申诉处理",
  "notify:manage": "提醒管理",
  "user:manage": "用户与权限",
  "rule:update": "规则参数",
  "audit:read": "审计日志",
};

const ALL_PERMISSIONS = Object.values(PERMISSIONS);

export function RolesPage() {
  const [role, setRole] = useState<Role>("LIBRARIAN");
  const [checked, setChecked] = useState<Permission[]>(ROLE_PERMISSIONS.LIBRARIAN);
  const dirty = useMemo(() => {
    const baseline = ROLE_PERMISSIONS[role];
    return (
      checked.length !== baseline.length || checked.some((code) => !baseline.includes(code))
    );
  }, [checked, role]);

  const selectRole = (next: Role) => {
    setRole(next);
    setChecked(ROLE_PERMISSIONS[next]);
  };

  return (
    <div className="space-y-4">
      <PageHeader
        title="角色权限"
        description="按角色分配管理端权限；保存后该角色下一次请求即生效。"
        actions={
          <StatusPill tone="amber" dot>
            等待后端 B-12
          </StatusPill>
        }
      />

      <div className="rounded-xl border border-dashed border-line bg-wash/70 px-4 py-3 text-[10.5px] text-ink-2">
        <code className="font-mono text-[10px] text-ink">GET/PUT /admin/roles</code>（B-12）尚未就绪，
        当前展示前端角色基线，保存按钮暂不可用；后端就绪后改为读写接口，无需再改页面结构。
      </div>

      <SectionCard
        title="角色与权限"
        description="勾选表示该角色可访问的菜单与操作；未勾选的入口在路由、菜单、按钮三层同时收敛。"
        action={
          <div className="flex items-center gap-2">
            <Select value={role} onValueChange={(value) => selectRole(value as Role)}>
              <SelectTrigger className="h-[34px] w-[140px] text-[11px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {(Object.keys(ROLE_LABEL) as Role[]).map((code) => (
                  <SelectItem key={code} value={code} className="text-[11px]">
                    {ROLE_LABEL[code]}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button
              size="sm"
              variant="outline"
              disabled={!dirty}
              onClick={() => setChecked(ROLE_PERMISSIONS[role])}
            >
              撤销改动
            </Button>
            <Button size="sm" disabled title="等待后端 B-12：PUT /admin/roles/{code}/permissions">
              保存权限
            </Button>
          </div>
        }
      >
        <div className="grid grid-cols-1 gap-2 sm:grid-cols-2 lg:grid-cols-3">
          {ALL_PERMISSIONS.map((code) => {
            const enabled = checked.includes(code);
            return (
              <label
                key={code}
                className="flex cursor-pointer items-center gap-2.5 rounded-lg border border-line-2 px-3 py-2 text-[11px] text-ink-2 transition-colors hover:bg-wash"
              >
                <Checkbox
                  checked={enabled}
                  onCheckedChange={(value) =>
                    setChecked((prev) =>
                      value ? [...prev, code] : prev.filter((item) => item !== code),
                    )
                  }
                />
                <span className="flex-1">{PERMISSION_LABEL[code]}</span>
                <code className="font-mono text-[9px] text-ink-3">{code}</code>
              </label>
            );
          })}
        </div>

        <div className="mt-3 flex items-center gap-2 border-t border-line-2 pt-3 text-[10px] text-ink-3">
          <ShieldCheck className="size-3.5 text-mint-deep" />
          已选 {checked.length} / {ALL_PERMISSIONS.length} 项
          {dirty ? <span className="text-warn">· 有未保存的改动</span> : null}
        </div>
      </SectionCard>
    </div>
  );
}
