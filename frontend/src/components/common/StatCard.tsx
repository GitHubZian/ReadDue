import type { LucideIcon } from "lucide-react";
import { Link } from "react-router-dom";
import { cn } from "@/lib/utils";

/** 指标卡：标签 10.5、数值 26、脚注 10.5，均按锚点字阶。传入 to 时整卡可点。 */
export function StatCard({
  label,
  value,
  unit,
  hint,
  icon: Icon,
  tone = "mint",
  className,
  to,
}: {
  label: string;
  value: React.ReactNode;
  unit?: string;
  hint?: React.ReactNode;
  icon?: LucideIcon;
  tone?: "mint" | "amber" | "red" | "slate";
  className?: string;
  to?: string;
}) {
  const iconTone = {
    mint: "bg-mint-soft text-mint-deep",
    amber: "bg-warn-soft text-warn",
    red: "bg-danger-soft text-danger",
    slate: "bg-sunken text-ink-3",
  }[tone];

  const content = (
    <>
      <div className="flex items-start justify-between gap-2">
        <p className="text-[10.5px] font-medium tracking-wide text-ink-3">{label}</p>
        {Icon ? (
          <span className={cn("grid size-7 shrink-0 place-items-center rounded-lg", iconTone)}>
            <Icon className="size-3.5" strokeWidth={1.8} />
          </span>
        ) : null}
      </div>
      <p className="mt-1.5 flex items-baseline gap-1">
        <span className="tabular text-[26px] font-bold leading-none tracking-tight text-ink">
          {value}
        </span>
        {unit ? <span className="text-[10.5px] text-ink-3">{unit}</span> : null}
      </p>
      {hint ? <div className="mt-1.5 text-[10px] leading-4 text-ink-3">{hint}</div> : null}
    </>
  );

  const cardClassName = cn(
    "block rounded-2xl border border-line bg-card px-4 pb-3.5 pt-3.5",
    to &&
      "transition-colors hover:border-mint-deep/30 hover:bg-wash/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-mint-deep/40 focus-visible:ring-offset-2 focus-visible:ring-offset-background",
    className,
  );

  if (to) {
    return (
      <Link to={to} aria-label={label} className={cardClassName}>
        {content}
      </Link>
    );
  }

  return <div className={cardClassName}>{content}</div>;
}
