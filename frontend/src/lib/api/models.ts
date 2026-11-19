import type { Tone } from "./types";

export type Role = "READER" | "LIBRARIAN" | "ADMIN";

export interface SessionUser {
  id: string;
  name: string;
  role: Role;
  studentNo?: string;
  avatarUrl?: string;
  permissions: string[];
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  token: string;
  expiresIn: number | string;
  user: SessionUser;
}

/* ---------------- 工作台 ---------------- */

export interface DashboardStats {
  pendingGraces: number;
  pendingAppeals: number;
  dueToday: number;
  overdueBooks: number;
  overdueReaders: number;
  overdueFine: string;
  slaBreach: number;
  pendingTotal: number;
}

export interface TrendPoint {
  date: string;
  label: string;
  borrows: number;
  returns: number;
}

export interface RuleHit {
  label: string;
  value: number;
}

export interface NoticeItem {
  id: string;
  level: "info" | "warning" | "danger" | "neutral";
  title: string;
  body: string;
  at: string;
}

export interface DueTodayItem {
  id: string;
  reader: Person;
  book: BookRef;
  dueAt: string;
  reminded: boolean;
}

export interface DashboardData {
  stats: DashboardStats;
  trend: TrendPoint[];
  trendSummary: { borrows: number; returns: number };
  rulesHit: RuleHit[];
  notices: NoticeItem[];
  dueTodayItems: DueTodayItem[];
  duty: { name: string; window: string; phone: string };
}

/* ---------------- 宽限 ---------------- */

export type GraceStatus = "PENDING" | "APPROVED" | "REJECTED" | "EXPIRED";

export interface Person {
  id: string;
  name: string;
  studentNo: string;
  college?: string;
}

export interface BookRef {
  id?: string;
  title: string;
  author: string;
  barcode?: string;
  coverTone?: Tone;
}

export interface TimelineItem {
  at: string;
  actor: string;
  text: string;
}

export interface Evidence {
  id?: string;
  label?: string;
  url?: string;
  name?: string;
  size?: number;
  contentType?: string;
}

export interface GraceRecord {
  id: string;
  code: string;
  reader: Person;
  book: BookRef;
  scene: string;
  reason: string;
  status: GraceStatus;
  requestedDays: number;
  approvedDays?: number;
  slaDeadline: string;
  createdAt: string;
  evidence: Evidence[];
  history: TimelineItem[];
  fineBefore?: string;
}

export interface GraceDecision {
  days: number;
  note?: string;
}

/* ---------------- 申诉 ---------------- */

export type AppealStatus = "PENDING" | "PROCESSING" | "RESOLVED" | "REJECTED";
export type AppealCategory =
  | "OVERDUE_DISPUTE"
  | "FINE_DISPUTE"
  | "BLACKLIST"
  | "OTHER";

export interface AppealConclusion {
  kind: "REVOKE_OVERDUE" | "COMPENSATE" | "REJECT";
  days?: number;
  note: string;
  decidedAt: string;
  decidedBy: string;
}

export interface AppealTicket {
  id: string;
  code: string;
  reader: Person;
  book: BookRef;
  category: AppealCategory;
  status: AppealStatus;
  summary: string;
  createdAt: string;
  slaDeadline: string;
  conclusion?: AppealConclusion;
  timeline: TimelineItem[];
}

/* ---------------- 预约 ---------------- */

export type ReservationStatus =
  | "WAITING"
  | "READY"
  | "FULFILLED"
  | "EXPIRED"
  | "CANCELLED";

export interface Reservation {
  id: string;
  reader: Person;
  book: BookRef;
  createdAt: string;
  queuePosition: number;
  status: ReservationStatus;
  readyAt?: string;
  expiresAt?: string;
  shelf?: string;
}

/* ---------------- 逾期 ---------------- */

export type OverdueStatus = "OVERDUE" | "LOST" | "CLAIMED";

export interface OverdueItem {
  id: string;
  reader: Person;
  book: BookRef;
  dueAt: string;
  daysOverdue: number;
  fine: string;
  remindCount: number;
  lastRemindedAt?: string;
  status: OverdueStatus;
}

/* ---------------- 提醒中心 ---------------- */

export type NotifyChannel = "SMS" | "WECHAT" | "EMAIL" | "INAPP";
export type NotifyStatus = "PENDING" | "SENDING" | "SENT" | "PARTIAL" | "FAILED";

export interface NotifyTemplate {
  id: string;
  name: string;
  channel: NotifyChannel;
  scene: string;
  enabled: boolean;
  content: string;
  variables: string[];
  updatedAt: string;
}

export interface NotifyTask {
  id: string;
  title: string;
  templateName: string;
  channel: NotifyChannel;
  targetCount: number;
  successCount: number;
  failCount: number;
  status: NotifyStatus;
  createdAt: string;
  finishedAt?: string;
  error?: string;
}

/* ---------------- 用户 ---------------- */

export type UserStatus = "ACTIVE" | "BLACKLISTED" | "MUTED";

export interface AdminUser {
  id: string;
  name: string;
  studentNo: string;
  role: Role;
  status: UserStatus;
  college: string;
  phone: string;
  email: string;
  borrowCount: number;
  fine: string;
  joinedAt: string;
  lastActiveAt: string;
  blacklistReason?: string;
}

/* ---------------- 规则 ---------------- */

export type RuleValueType = "number" | "money" | "boolean" | "select";

export interface RuleOption {
  label: string;
  value: string;
}

export interface RuleItem {
  key: string;
  label: string;
  description: string;
  type: RuleValueType;
  value: string;
  unit?: string;
  options?: RuleOption[];
  min?: number;
  max?: number;
}

export interface RuleGroup {
  id: string;
  title: string;
  description: string;
  items: RuleItem[];
}

export interface RulePayload {
  groups: RuleGroup[];
  expectedVersion: string;
}

/* ---------------- 审计 ---------------- */

export type AuditCategory = "APPROVAL" | "RULE" | "USER" | "SYSTEM";

export interface AuditLog {
  id: string;
  at: string;
  actor: { name: string; role: string };
  action: string;
  category: AuditCategory;
  target: string;
  detail: string;
  ip: string;
  traceId: string;
  before?: string;
  after?: string;
}

/* ---------------- 借还台 ---------------- */

export interface CirculationFlow {
  id: string;
  at: string;
  type: "BORROW" | "RETURN";
  reader: Person;
  book: BookRef;
  operator: string;
}

export interface BorrowResult {
  borrowId: string;
  reader: Person;
  book: BookRef;
  dueAt: string;
}

export interface ReturnResult {
  book: BookRef;
  reader: Person;
  fine: string;
  overdueDays: number;
}

/* ---------------- 逾期分级 / 校历 ---------------- */

export interface TierRow {
  code: string;
  name: string;
  minDays: number;
  maxDays?: number | null;
  actionCode: string;
  toneCode?: string;
  allowRenew: boolean;
  description: string;
}

export interface CalendarEntry {
  id: string;
  term: string;
  scene: string;
  startDate: string;
  endDate: string;
  source: string;
  note: string;
}
/* ---------------- 角色与权限 ---------------- */

export interface RoleView {
  code: string;
  name: string;
  dataScope: string;
  permissions: string[];
  permissionCount: number;
}

export interface PermissionView {
  id: string;
  code: string;
  name: string;
  type: string;
  module: string;
}

/* ---------------- 通知主题策略 ---------------- */

export interface NotifyTopic {
  topicCode: string;
  name: string;
  priority: string;
  defaultFrequency: string;
  defaultChannels: string[];
  enabled: boolean;
  sort: number;
}