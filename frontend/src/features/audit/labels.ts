import type { AuditCategory } from "@/lib/api/models";

/** 后端存动作码，表里展示中文；未收录的码原样显示。 */
export const AUDIT_ACTION_LABELS: Record<string, string> = {
  AUTH_LOGIN: "登录",
  AUTH_LOGOUT: "退出登录",
  AUTH_REFRESH: "刷新登录",
  GRACE_APPROVE: "通过宽限",
  GRACE_REJECT: "驳回宽限",
  APPEAL_CREATE: "发起申诉",
  APPEAL_ACK: "受理申诉",
  APPEAL_RESOLVE: "撤销逾期",
  APPEAL_SLA_BREACH: "申诉超时补偿",
  APPEAL_COMPENSATE: "补偿宽限",
  APPEAL_REJECT: "驳回申诉",
  RULE_UPDATE: "更新规则",
  USER_BLACKLIST: "拉黑用户",
  USER_RESTORE: "恢复用户",
  USER_FORGET: "立即遗忘脱敏",
  NOTIFY_REMIND_BATCH: "批量催还",
  NOTIFY_TEMPLATE_UPDATE: "更新模板",
  NOTIFY_RETRY: "重试通知",
  BORROW_CREATE: "借出登记",
  BORROW_RENEW: "续借登记",
  BORROW_RETURN: "归还登记",
  RETURN_CREATE: "归还登记",
  RESERVATION_CREATE: "读者预约",
  RESERVATION_CANCEL: "取消预约",
  RESERVATION_PROMOTE: "预约到馆",
  RESERVATION_EXPIRE: "预约释放",
  CALENDAR_CREATE: "新增校历",
  CALENDAR_UPDATE: "更新校历",
  CALENDAR_DELETE: "删除校历",
  ROLE_PERMISSION_UPDATE: "更新角色权限",
  GRACE_APPLY: "提交宽限申请",
  NOTIFY_TOPIC_UPDATE: "更新主题策略",
  FILE_UPLOAD: "上传附件",
  CONSENT_GRANT: "同意隐私授权",
  CONSENT_REVOKE: "撤回隐私授权",
};

export const actionLabel = (action: string) => AUDIT_ACTION_LABELS[action] ?? action;

export const categoryMeta: Record<
  AuditCategory,
  { label: string; tone: "mint" | "amber" | "red" | "slate" }
> = {
  APPROVAL: { label: "审批", tone: "mint" },
  RULE: { label: "规则变更", tone: "amber" },
  USER: { label: "用户操作", tone: "slate" },
  SYSTEM: { label: "系统", tone: "slate" },
};

export const categoryInfo = (category: string) =>
  categoryMeta[category as AuditCategory] ?? { label: category, tone: "slate" as const };

export const targetLabel = (target: string) => {
  const [prefix, ...rest] = target.split(" ");
  const mapped =
    (
      {
        USER: "用户",
        BORROW_ORDER: "借阅单",
        GRACE: "宽限",
        APPEAL: "申诉",
        RULE: "规则",
        NOTIFY_TASK: "通知任务",
        RESERVATION: "预约",
        BOOK: "书目",
        ACADEMIC_CALENDAR: "校历",
        SYS_ROLE: "角色",
        GRACE_APPLICATION: "宽限申请",
        NOTIFY_TOPIC: "通知主题",
        FILE: "附件",
        CONSENT: "授权记录",
      } as Record<string, string>
    )[prefix] ?? prefix;
  return [mapped, ...rest].join(" ");
};

export interface AuditSearch {
  keyword?: string;
}

/**
 * 搜索框输入映射为后端可识别的 keyword：
 * - 中文动作标签（「通过宽限」）→ 动作码；
 * - 中文动作关键词（「宽限」「申诉」）→ 该词命中最多的动作前缀族；
 * - 纯数字/带 # 的对象 ID → 数字（后端 keyword 会匹配 object_id）；
 * - 其它（操作人姓名、动作码、traceId 片段）原样透传。
 */
export function mapAuditSearch(input: string): AuditSearch {
  const text = input.trim();
  if (!text) return {};

  const exact = Object.entries(AUDIT_ACTION_LABELS).find(([, label]) => label === text);
  if (exact) return { keyword: exact[0] };

  const matched = Object.entries(AUDIT_ACTION_LABELS).filter(([, label]) => label.includes(text));
  if (matched.length) {
    // 一个中文词可能跨多个动作族（如「宽限」既有 GRACE 也有 APPEAL）。
    // 后端 keyword 只能命中一个前缀，取命中最多的族，避免直接拿中文去 compare 得到 0 条。
    const counts = new Map<string, number>();
    for (const [code] of matched) {
      const prefix = code.split("_")[0];
      counts.set(prefix, (counts.get(prefix) ?? 0) + 1);
    }
    const ranked = [...counts.entries()].sort((a, b) => b[1] - a[1]);
    const best = ranked[0];
    if (best) return { keyword: best[0] };
  }

  const idOnly = text.match(/^#?\s*(\d{6,})$/);
  if (idOnly) return { keyword: idOnly[1] };

  const idWithTarget = text.match(/#\s*(\d{6,})/);
  if (idWithTarget) return { keyword: idWithTarget[1] };

  return { keyword: text };
}
