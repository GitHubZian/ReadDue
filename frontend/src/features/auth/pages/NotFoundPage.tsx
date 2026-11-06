import { Compass } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "@/components/ui/button";

export function NotFoundPage() {
  return (
    <div className="flex min-h-[60vh] flex-col items-center justify-center text-center">
      <span className="grid size-12 place-items-center rounded-2xl bg-mint-soft text-mint-deep">
        <Compass className="size-5" strokeWidth={1.7} />
      </span>
      <h1 className="tabular mt-4 text-[22px] font-extrabold tracking-tight text-ink">404</h1>
      <p className="mt-1.5 max-w-sm text-[11px] leading-5 text-ink-3">
        页面不存在，可能链接已失效，或该功能尚未开放。
      </p>
      <Button asChild className="mt-5" size="sm">
        <Link to="/workbench">回到工作台</Link>
      </Button>
    </div>
  );
}
