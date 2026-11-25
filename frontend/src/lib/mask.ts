/**
 * 管理端 PII 脱敏工具。
 *
 * 原则：列表与详情只暴露「可识别业务」的最小粒度，姓名、学号、手机号、
 * 邮箱、身份证号、IP 一律打码；书目、单号、天数、状态、金额等业务字段保持原值。
 * 详情里的 JSON 快照（审计 before/after）与导出 CSV 都复用这里，避免两处规则漂移。
 */

const STAR = "*";

const asText = (value: unknown): string =>
  value === null || value === undefined ? "" : String(value);

/** 张* / 张*明：保留首字（三字及以上保留末字）。 */
export function maskName(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  const chars = [...text];
  if (chars.length === 1) return STAR;
  if (chars.length === 2) return `${chars[0]}${STAR}`;
  return `${chars[0]}${STAR.repeat(chars.length - 2)}${chars[chars.length - 1]}`;
}

/** 2021***01：保留前 4 位与后 2 位；短学号只留前缀。 */
export function maskStudentNo(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  if (text.length <= 4) return `${text.slice(0, 1)}***`;
  if (text.length <= 6) return `${text.slice(0, 2)}***`;
  return `${text.slice(0, 4)}***${text.slice(-2)}`;
}

/** 138****1234：保留前 3 位与后 4 位。 */
export function maskPhone(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  const digits = text.replace(/\D/g, "");
  if (digits.length < 7) return "***";
  return `${digits.slice(0, 3)}****${digits.slice(-4)}`;
}

/** a***@x.com：本地部分只留首字符，域名保留。 */
export function maskEmail(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  const at = text.indexOf("@");
  if (at <= 0) return `${text.slice(0, 1)}***`;
  return `${text.slice(0, 1)}***${text.slice(at)}`;
}

/** 110101********1234：身份证保留前 6 后 4。 */
export function maskIdCard(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  if (text.length <= 10) return `${text.slice(0, 2)}****${text.slice(-2)}`;
  return `${text.slice(0, 6)}********${text.slice(-4)}`;
}

/** 192.168.*.*；IPv6 保留前两组。非法值统一 ***。 */
export function maskIp(value?: string | null): string {
  const text = asText(value).trim();
  if (!text) return "";
  if (text.includes(":")) {
    const groups = text.split(":").filter(Boolean);
    return groups.length >= 2 ? `${groups[0]}:${groups[1]}:*` : "***";
  }
  const parts = text.split(".");
  if (parts.length === 4) return `${parts[0]}.${parts[1]}.*.*`;
  return "***";
}

const PHONE_PATTERN = /\b1[3-9]\d{9}\b/g;
const EMAIL_PATTERN = /[\w.+-]+@[\w-]+(?:\.[\w-]+)+/g;

/**
 * 自由文本脱敏：只处理能高置信识别的手机号与邮箱，不动单号、金额、日期。
 * 用于申请理由、拉黑原因、审计描述等读者/管理员手输内容。
 */
export function maskText(value?: string | null): string {
  const text = asText(value);
  if (!text) return "";
  return text
    .replace(EMAIL_PATTERN, (match) => maskEmail(match))
    .replace(PHONE_PATTERN, (match) => maskPhone(match));
}

type Masker = (value?: string | null) => string;

const FIELD_MASKERS: Record<string, Masker> = {
  name: maskName,
  realname: maskName,
  readername: maskName,
  studentname: maskName,
  applicantname: maskName,
  phone: maskPhone,
  mobile: maskPhone,
  tel: maskPhone,
  phonenumber: maskPhone,
  contactphone: maskPhone,
  email: maskEmail,
  mail: maskEmail,
  studentno: maskStudentNo,
  studentid: maskStudentNo,
  studentnumber: maskStudentNo,
  idcard: maskIdCard,
  idcardno: maskIdCard,
  idnumber: maskIdCard,
  ip: maskIp,
  ipaddress: maskIp,
};

const normalizeKey = (key: string) => key.replace(/[_\-\s]/g, "").toLowerCase();

/** 递归脱敏对象/数组；字符串值额外清理内联手机号与邮箱。 */
export function maskJson(value: unknown): unknown {
  if (Array.isArray(value)) return value.map((item) => maskJson(item));
  if (value && typeof value === "object") {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>).map(([key, item]) => {
        const masker = FIELD_MASKERS[normalizeKey(key)];
        if (masker && (typeof item === "string" || typeof item === "number")) {
          return [key, masker(String(item))];
        }
        return [key, maskJson(item)];
      }),
    );
  }
  if (typeof value === "string") return maskText(value);
  return value;
}

const tryParse = (text: string): unknown => {
  try {
    return JSON.parse(text);
  } catch {
    return undefined;
  }
};

/**
 * 审计 before/after 是「字符串化 JSON」，先解析再脱敏。
 * 解析失败或非对象时返回 {}，让页面按「无快照」降级。
 */
export function parseSnapshot(value: unknown): Record<string, unknown> {
  let parsed: unknown = value;
  if (typeof value === "string") {
    const text = value.trim();
    if (!text) return {};
    parsed = tryParse(text);
  }
  const masked = maskJson(parsed);
  return masked && typeof masked === "object" && !Array.isArray(masked)
    ? (masked as Record<string, unknown>)
    : {};
}

/** 导出/摘要用的 JSON 文本：非 JSON 字符串按普通文本脱敏。 */
export function maskJsonText(value: unknown): string {
  if (value === null || value === undefined || value === "") return "";
  if (typeof value === "string") {
    const parsed = tryParse(value);
    if (parsed === undefined) return maskText(value);
    return JSON.stringify(maskJson(parsed));
  }
  return JSON.stringify(maskJson(value));
}

const NON_PERSON_TOKENS = new Set(["系统", "全体", "馆员", "读者", "管理员", "图书馆", "通知", "值班"]);

/**
 * 通知正文形如「陈昊 · 已送达站内信」：首段是 2–3 字人名时才脱敏，
 * 避免把「系统」「全体」这类主体打成「系*」。
 */
export function maskNoticeText(value?: string | null): string {
  const text = asText(value);
  if (!text) return "";
  const parts = text.split(" · ");
  const head = parts[0]?.trim() ?? "";
  if (parts.length >= 2 && /^[\u4e00-\u9fa5]{2,3}$/.test(head) && !NON_PERSON_TOKENS.has(head)) {
    parts[0] = maskName(head);
  }
  return maskText(parts.join(" · "));
}
