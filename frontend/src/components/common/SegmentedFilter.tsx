import { cn } from "@/lib/utils";

export interface SegmentItem<T extends string = string> {
  value: T;
  label: string;
  count?: number;
}

/** 分段筛选：底部浅色槽 + 白色选中块，对应锚点里的状态胶囊。 */
export function SegmentedFilter<T extends string>({
  items,
  value,
  onChange,
  className,
}: {
  items: SegmentItem<T>[];
  value: T;
  onChange: (value: T) => void;
  className?: string;
}) {
  return (
    <div
      role="tablist"
      className={cn("inline-flex items-center gap-0.5 rounded-lg bg-wash p-0.5", className)}
    >
      {items.map((item) => {
        const active = item.value === value;
        return (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={active}
            onClick={() => onChange(item.value)}
            className={cn(
              "inline-flex h-[26px] items-center gap-1 rounded-md px-2.5 text-[11px] transition-colors",
              active ? "bg-card font-semibold text-ink shadow-sm" : "text-ink-3 hover:text-ink",
            )}
          >
            {item.label}
            {typeof item.count === "number" ? (
              <span className="tabular text-[9.5px] text-ink-3">{item.count}</span>
            ) : null}
          </button>
        );
      })}
    </div>
  );
}
