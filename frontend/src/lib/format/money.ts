const cny = new Intl.NumberFormat("zh-CN", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * BigDecimal 以字符串下发，避免浮点误差；展示时统一 ¥ 前缀。
 * 整数金额省略 .00（锚点里是 ¥186）。
 */
export function formatCNY(value?: string | number | null) {
  if (value === null || value === undefined || value === "") return "¥0";
  const num = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(num)) return "¥0";
  const text = cny.format(num);
  return `¥${text.replace(/\.00$/, "")}`;
}

export function formatMoneyNumber(value?: string | number | null) {
  if (value === null || value === undefined || value === "") return "0.00";
  const num = typeof value === "number" ? value : Number(value);
  if (!Number.isFinite(num)) return "0.00";
  return cny.format(num);
}

export function formatCount(value?: number | null) {
  if (value === null || value === undefined) return "0";
  return new Intl.NumberFormat("zh-CN").format(value);
}
