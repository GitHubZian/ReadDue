import { cn } from "@/lib/utils";

/** 白底、1px 描边、无投影的卡片容器；表格必须放进来。 */
export function SectionCard({
  title,
  description,
  action,
  children,
  className,
  bodyClassName,
}: {
  title?: React.ReactNode;
  description?: React.ReactNode;
  action?: React.ReactNode;
  children: React.ReactNode;
  className?: string;
  bodyClassName?: string;
}) {
  return (
    <section className={cn("rounded-2xl border border-line bg-card", className)}>
      {title ? (
        <header className="flex items-start justify-between gap-3 px-5 pb-2.5 pt-4">
          <div className="min-w-0">
            <h2 className="text-[13px] font-bold tracking-tight text-ink">{title}</h2>
            {description ? (
              <p className="mt-0.5 text-[10.5px] leading-4 text-ink-3">{description}</p>
            ) : null}
          </div>
          {action ? <div className="flex shrink-0 items-center gap-2">{action}</div> : null}
        </header>
      ) : null}
      <div className={cn(bodyClassName ?? "px-5 pb-4")}>{children}</div>
    </section>
  );
}
