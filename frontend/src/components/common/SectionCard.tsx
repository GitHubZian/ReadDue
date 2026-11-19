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
        <header className="flex items-start justify-between gap-3 px-6 pb-3 pt-5">
          <div className="min-w-0">
            <h2 className="text-[13px] font-bold tracking-tight text-ink">{title}</h2>
            {description ? (
              <p className="mt-0.5 text-[10.5px] leading-4 text-ink-3">{description}</p>
            ) : null}
          </div>
          {action ? <div className="flex shrink-0 items-center gap-2">{action}</div> : null}
        </header>
      ) : null}
      <div className={cn("card-body", bodyClassName ?? "px-6 pb-5")}>{children}</div>
    </section>
  );
}
