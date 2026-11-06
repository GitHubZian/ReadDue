import type { TrendPoint } from "@/lib/api/models";

/** 双柱趋势：借出深绿、归还浅薄荷；纯 CSS，无图表库。 */
export function TrendChart({
  points,
  summary,
}: {
  points: TrendPoint[];
  summary: { borrows: number; returns: number };
}) {
  const max = Math.max(1, ...points.map((p) => Math.max(p.borrows, p.returns)));

  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <div className="flex items-center gap-3 text-[9.5px] text-ink-3">
          <span className="flex items-center gap-1.5">
            <i className="size-2 rounded-sm bg-mint-deep" /> 借出 <b className="tabular font-semibold text-ink">{summary.borrows}</b>
          </span>
          <span className="flex items-center gap-1.5">
            <i className="size-2 rounded-sm bg-mint/50" /> 归还 <b className="tabular font-semibold text-ink">{summary.returns}</b>
          </span>
        </div>
        <span className="text-[9px] text-ink-3">近 7 天</span>
      </div>
      <div className="flex items-end gap-2 border-b border-line-2 pb-1.5">
        {points.map((point) => (
          <div key={point.label} className="flex flex-1 flex-col items-center gap-1.5">
            <div className="flex h-[96px] w-full items-end justify-center gap-1.5">
              <div
                title={`${point.label} 借出 ${point.borrows}`}
                style={{ height: `${Math.max(4, (point.borrows / max) * 100)}%` }}
                className="w-2.5 rounded-t-[3px] bg-mint-deep transition-colors hover:bg-mint-deep/80"
              />
              <div
                title={`${point.label} 归还 ${point.returns}`}
                style={{ height: `${Math.max(4, (point.returns / max) * 100)}%` }}
                className="w-2.5 rounded-t-[3px] bg-mint/60 transition-colors hover:bg-mint/80"
              />
            </div>
            <span className="text-[8.5px] text-ink-3">{point.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}
