import { ShieldOff } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";

export function ForbiddenPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <span className="grid size-12 place-items-center rounded-2xl bg-warn-soft text-warn">
        <ShieldOff className="size-5" strokeWidth={1.7} />
      </span>
      <h1 className="mt-4 text-[22px] font-extrabold tracking-tight text-ink">没有访问权限</h1>
      <p className="mt-1.5 max-w-sm text-[11px] leading-5 text-ink-3">
        当前账号的角色不包含该页面的权限。若确需处理，请让管理员在「用户与权限」中调整角色。
      </p>
      <Button asChild className="mt-5" size="sm">
        <Link to="/workbench">返回工作台</Link>
      </Button>
    </div>
  );
}
