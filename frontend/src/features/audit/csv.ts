import type { AuditLog } from "@/lib/api/models";
import { formatDateTime } from "@/lib/format/date";
import { maskIp, maskJsonText } from "@/lib/mask";
import { actionLabel, categoryInfo, targetLabel } from "./labels";

const HEADERS = [
  "id",
  "时间",
  "操作人",
  "角色",
  "动作",
  "分类",
  "对象",
  "IP",
  "详情",
  "traceId",
  "变更前",
  "变更后",
];

/** CSV 单元格转义：逗号、引号、换行都加引号；引号翻倍。 */
const escapeCell = (value: unknown) => {
  const text = value === null || value === undefined ? "" : String(value);
  return /[",\n\r]/.test(text) ? `"${text.replace(/"/g, '""')}"` : text;
};

/**
 * 前端生成脱敏 CSV：IP、详情、before/after 全部走 mask，不依赖后端导出接口。
 */
export function buildAuditCsv(logs: AuditLog[]): string {
  const rows = logs.map((log) => [
    log.id,
    formatDateTime(log.at),
    log.actor.name,
    log.actor.role,
    actionLabel(log.action),
    categoryInfo(log.category).label,
    targetLabel(log.target),
    maskIp(log.ip),
    maskJsonText(log.detail),
    log.traceId ?? "",
    maskJsonText(log.before),
    maskJsonText(log.after),
  ]);

  return `\uFEFF${[HEADERS, ...rows].map((row) => row.map(escapeCell).join(",")).join("\r\n")}`;
}
