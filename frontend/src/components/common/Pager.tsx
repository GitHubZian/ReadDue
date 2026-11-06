import { ChevronLeft, ChevronRight } from "lucide-react";
import { Button } from "@/components/ui/button";

/** 分页按后端约定 1-based，size 默认 20。 */
export function Pager({
  page,
  size,
  total,
  onChange,
}: {
  page: number;
  size: number;
  total: number;
  onChange: (page: number) => void;
}) {
  const pages = Math.max(1, Math.ceil(total / size));
  const from = total === 0 ? 0 : (page - 1) * size + 1;
  const to = Math.min(total, page * size);

  return (
    <div className="flex items-center justify-between gap-3 border-t border-line-2 px-1 pt-3 text-[10px] text-ink-3">
      <p className="tabular">
        显示 {from}–{to} 条，共 {total} 条
      </p>
      <div className="flex items-center gap-1">
        <Button
          variant="outline"
          size="icon-xs"
          aria-label="上一页"
          disabled={page <= 1}
          onClick={() => onChange(page - 1)}
        >
          <ChevronLeft className="size-3.5" />
        </Button>
        <span className="tabular px-1.5 text-[10.5px] text-ink-2">
          {page} / {pages}
        </span>
        <Button
          variant="outline"
          size="icon-xs"
          aria-label="下一页"
          disabled={page >= pages}
          onClick={() => onChange(page + 1)}
        >
          <ChevronRight className="size-3.5" />
        </Button>
      </div>
    </div>
  );
}
