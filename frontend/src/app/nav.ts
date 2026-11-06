import {
  AlarmClock,
  BellRing,
  CalendarClock,
  Hourglass,
  LayoutDashboard,
  MessagesSquare,
  ScanLine,
  ScrollText,
  SlidersHorizontal,
  Users,
  type LucideIcon,
} from "lucide-react";
import type { Permission } from "@/lib/auth/permissions";

export interface NavItem {
  label: string;
  path: string;
  icon: LucideIcon;
  permission: Permission;
}

export interface NavGroup {
  label: string;
  items: NavItem[];
}

export const NAV: NavGroup[] = [
  {
    label: "概览",
    items: [{ label: "工作台", path: "/workbench", icon: LayoutDashboard, permission: "workbench:read" }],
  },
  {
    label: "流通",
    items: [
      { label: "借还台", path: "/circulation", icon: ScanLine, permission: "borrow:return" },
      { label: "预约管理", path: "/reservations", icon: CalendarClock, permission: "reservation:manage" },
      { label: "逾期看板", path: "/overdue", icon: AlarmClock, permission: "overdue:read" },
    ],
  },
  {
    label: "审批",
    items: [
      { label: "宽限审批", path: "/grace", icon: Hourglass, permission: "grace:approve" },
      { label: "申诉工单", path: "/appeals", icon: MessagesSquare, permission: "appeal:handle" },
    ],
  },
  {
    label: "运营",
    items: [
      { label: "提醒中心", path: "/notify", icon: BellRing, permission: "notify:manage" },
      { label: "用户与权限", path: "/users", icon: Users, permission: "user:manage" },
      { label: "规则参数", path: "/rules", icon: SlidersHorizontal, permission: "rule:update" },
      { label: "审计日志", path: "/audit", icon: ScrollText, permission: "audit:read" },
    ],
  },
];

export const NAV_ITEMS = NAV.flatMap((group) => group.items);
