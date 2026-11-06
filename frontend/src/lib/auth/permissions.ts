import type { Role, SessionUser } from "@/lib/api/models";

export const PERMISSIONS = {
  workbenchRead: "workbench:read",
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

/** 角色默认权限，后端 /me 返回 permissions 时以服务端为准。 */
export const ROLE_PERMISSIONS: Record<Role, Permission[]> = {
  ADMIN: Object.values(PERMISSIONS),
  LIBRARIAN: [
    PERMISSIONS.workbenchRead,
    PERMISSIONS.borrowReturn,
    PERMISSIONS.reservationManage,
    PERMISSIONS.overdueRead,
    PERMISSIONS.graceApprove,
    PERMISSIONS.appealHandle,
    PERMISSIONS.notifyManage,
  ],
  READER: [PERMISSIONS.workbenchRead],
};

export const ROLE_LABEL: Record<Role, string> = {
  ADMIN: "管理员",
  LIBRARIAN: "馆员",
  READER: "读者",
};

export function can(user: SessionUser | null | undefined, code: Permission) {
  if (!user) return false;
  // 后端每次请求都会重查权限，前端以登录返回的 permissions 为准
  const grants = user.permissions.length ? user.permissions : ROLE_PERMISSIONS[user.role];
  return grants.includes(code);
}

export function canAny(user: SessionUser | null | undefined, codes: Permission[]) {
  return codes.some((code) => can(user, code));
}
