import type { Role, SessionUser } from "@/lib/api/models";

export const PERMISSIONS = {
  workbenchRead: "workbench:read",
  borrowCreate: "borrow:create",
  borrowReturn: "borrow:return",
  reservationManage: "reservation:manage",
  overdueRead: "overdue:read",
  graceApprove: "grace:approve",
  appealHandle: "appeal:handle",
  notifyManage: "notify:manage",
  userManage: "user:manage",
  ruleUpdate: "rule:update",
  auditRead: "audit:read",
} as const;

export type Permission = (typeof PERMISSIONS)[keyof typeof PERMISSIONS];

export const ROLE_LABEL: Record<Role, string> = {
  ADMIN: "管理员",
  LIBRARIAN: "馆员",
  READER: "读者",
};

export function can(user: SessionUser | null | undefined, code: Permission) {
  if (!user) return false;
  // 权限完全以服务端 /me 返回为准；服务端为空即无权限，不再按角色回退
  return user.permissions.includes(code);
}

export function canAny(user: SessionUser | null | undefined, codes: Permission[]) {
  return codes.some((code) => can(user, code));
}
