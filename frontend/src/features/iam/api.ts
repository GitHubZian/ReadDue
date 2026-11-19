import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { AdminUser, PermissionView, RoleView } from "@/lib/api/models";

export interface UserFilters extends PageParams {
  role?: string;
  status?: string;
  keyword?: string;
}

export const listUsers = (filters: UserFilters) =>
  api.get<PageResult<AdminUser>>(endpoints.users.list, { ...filters });

export const blacklistUser = (id: string, reason: string) =>
  api.post<AdminUser>(endpoints.users.blacklist(id), { reason });

export const restoreUser = (id: string) =>
  api.post<AdminUser>(endpoints.users.restore(id));

export const listRoles = () => api.get<RoleView[]>(endpoints.roles.list);

export const listPermissions = () => api.get<PermissionView[]>(endpoints.roles.permissions);

export const saveRolePermissions = (code: string, permissions: string[]) =>
  api.put<{ updated: number }>(endpoints.roles.permissionsOf(code), { permissions });