import { api } from "@/lib/api/client";
import { endpoints } from "@/lib/api/endpoints";
import type { PageParams, PageResult } from "@/lib/api/types";
import type { AdminUser, PermissionView, RoleView } from "@/lib/api/models";

export interface UserFilters extends PageParams {
  role?: string;
  status?: string;
  keyword?: string;
}

/** 管理端查看敏感详情时后端写入 DATA_READ，这里只读回放。 */
export interface DataReadRecord {
  id: string;
  operatorId?: string;
  operatorName: string;
  createdAt: string;
  targetType?: string;
  targetId?: string;
}

export interface DeletionRequestView {
  id: string;
  userId?: string;
  userName?: string;
  studentNo?: string;
  reason?: string;
  status?: string;
  createdAt: string;
}

/** 后端列表可能直接返回数组，也可能包一层分页；契约未定死时由适配层吸收。 */
const asList = <T>(data: T[] | PageResult<T>): T[] => (Array.isArray(data) ? data : data.items);

export const listUsers = (filters: UserFilters) =>
  api.get<PageResult<AdminUser>>(endpoints.users.list, { ...filters });

/** 读取用户敏感详情；后端在此接口内写 DATA_READ 审计。 */
export const getUserDetail = (id: string) =>
  api.get<AdminUser>(endpoints.users.detail(id));

export const listDataReads = async (targetId: string) =>
  asList(
    await api.get<DataReadRecord[] | PageResult<DataReadRecord>>(endpoints.dataReads, {
      targetType: "USER",
      targetId,
      limit: 20,
    }),
  );

export const listDeletionRequests = async (status = "PENDING") =>
  asList(
    await api.get<DeletionRequestView[] | PageResult<DeletionRequestView>>(
      endpoints.deletionRequests.list,
      { status },
    ),
  );

export const approveDeletionRequest = (id: string) =>
  api.post<unknown>(endpoints.deletionRequests.approve(id));

export const rejectDeletionRequest = (id: string, note: string) =>
  api.post<unknown>(endpoints.deletionRequests.reject(id), { note });

export const blacklistUser = (id: string, reason: string) =>
  api.post<AdminUser>(endpoints.users.blacklist(id), { reason });

export const restoreUser = (id: string) =>
  api.post<AdminUser>(endpoints.users.restore(id));

export const listRoles = () => api.get<RoleView[]>(endpoints.roles.list);

export const listPermissions = () => api.get<PermissionView[]>(endpoints.roles.permissions);

export const saveRolePermissions = (code: string, permissions: string[]) =>
  api.put<{ updated: number }>(endpoints.roles.permissionsOf(code), { permissions });
