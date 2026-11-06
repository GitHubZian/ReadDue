import type { Tone } from "@/lib/api/types";
import { cn } from "@/lib/utils";

const TONE: Record<Tone, string> = {
  mint: "bg-mint-soft text-mint-deep",
  amber: "bg-warn-soft text-warn",
  red: "bg-danger-soft text-danger",
  slate: "bg-sunken text-ink-3",
};

export function StatusPill({
  tone = "slate",
  children,
  dot = true,
  className,
}: {
  tone?: Tone;
  children: React.ReactNode;
  dot?: boolean;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex h-[22px] items-center gap-1.5 whitespace-nowrap rounded-full px-2 text-[10px] font-semibold leading-none",
        TONE[tone],
        className,
      )}
    >
      {dot ? <i className="size-1.5 rounded-full bg-current opacity-70" aria-hidden /> : null}
      {children}
    </span>
  );
}
