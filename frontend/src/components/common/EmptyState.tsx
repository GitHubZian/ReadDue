import { FileQuestion } from "lucide-react";
import { cn } from "@/lib/utils";

export function EmptyState({
  title = "这里还没有数据",
  description,
  action,
  className,
}: {
  title?: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("flex flex-col items-center justify-center gap-2 py-12 text-center", className)}>
      <span className="grid size-9 place-items-center rounded-full bg-wash text-ink-3">
        <FileQuestion className="size-4" strokeWidth={1.6} />
      </span>
      <p className="text-[12px] font-medium text-ink-2">{title}</p>
      {description ? <p className="max-w-sm text-[10.5px] text-ink-3">{description}</p> : null}
      {action}
    </div>
  );
}
