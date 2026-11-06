import type { AppealCategory, AppealStatus } from "@/lib/api/models";
import type { Tone } from "@/lib/api/types";

export const categoryLabel: Record<AppealCategory, string> = {
  OVERDUE_DISPUTE: "逾期争议",
  FINE_DISPUTE: "费用争议",
  BLACKLIST: "黑名单",
  OTHER: "其他",
};

export const appealStatusMeta: Record<AppealStatus, { label: string; tone: Tone }> = {
  PENDING: { label: "待受理", tone: "slate" },
  PROCESSING: { label: "处理中", tone: "amber" },
  RESOLVED: { label: "已结案", tone: "mint" },
  REJECTED: { label: "已驳回", tone: "red" },
};

export const conclusionLabel: Record<"REVOKE_OVERDUE" | "COMPENSATE" | "REJECT", string> = {
  REVOKE_OVERDUE: "撤销逾期",
  COMPENSATE: "补偿宽限",
  REJECT: "驳回",
};
