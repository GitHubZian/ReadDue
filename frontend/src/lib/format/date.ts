import dayjs from "dayjs";
import timezone from "dayjs/plugin/timezone";
import utc from "dayjs/plugin/utc";

dayjs.extend(utc);
dayjs.extend(timezone);

export const DISPLAY_TZ = "Asia/Shanghai";

/** 后端给的是 ISO-8601 UTC，统一转 Asia/Shanghai 展示。 */
export function toLocal(value?: string | number | Date | null) {
  if (!value) return null;
  return dayjs(value).tz(DISPLAY_TZ);
}

export function formatDateTime(value?: string | number | Date | null) {
  const d = toLocal(value);
  return d ? d.format("YYYY-MM-DD HH:mm") : "—";
}

export function formatDate(value?: string | number | Date | null) {
  const d = toLocal(value);
  return d ? d.format("YYYY-MM-DD") : "—";
}

export function formatShortDate(value?: string | number | Date | null) {
  const d = toLocal(value);
  return d ? d.format("MM-DD") : "—";
}

export function formatTime(value?: string | number | Date | null) {
  const d = toLocal(value);
  return d ? d.format("HH:mm") : "—";
}

export function formatMonthDay(value?: string | number | Date | null) {
  const d = toLocal(value);
  return d ? d.format("M月D日") : "—";
}

export function fromNow(value?: string | number | Date | null) {
  const d = toLocal(value);
  if (!d) return "—";
  const diff = dayjs().diff(d, "minute");
  if (diff < 1) return "刚刚";
  if (diff < 60) return `${diff} 分钟前`;
  if (diff < 60 * 24) return `${Math.floor(diff / 60)} 小时前`;
  return `${Math.floor(diff / 1440)} 天前`;
}

export interface SlaInfo {
  text: string;
  /** 已超时为 true */
  overdue: boolean;
  minutes: number;
}

/** 剩余 SLA：1h12m / 0h38m / 已超时。 */
export function slaRemaining(deadline?: string | null): SlaInfo {
  const d = toLocal(deadline);
  if (!d) return { text: "—", overdue: false, minutes: 0 };
  const minutes = d.diff(dayjs(), "minute");
  if (minutes <= 0) {
    return { text: "已超时", overdue: true, minutes };
  }
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return { text: `${h}h${String(m).padStart(2, "0")}m`, overdue: false, minutes };
}

/** 距今日结束还有多久，今日到期卡片用。 */
export function untilEndOfDay() {
  const end = dayjs().tz(DISPLAY_TZ).endOf("day");
  const minutes = Math.max(0, end.diff(dayjs(), "minute"));
  return `${Math.floor(minutes / 60)}h${String(minutes % 60).padStart(2, "0")}m`;
}

export { dayjs };
