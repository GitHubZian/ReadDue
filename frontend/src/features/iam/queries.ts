import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { fetchMe } from "@/features/auth/api";
import { updateUser } from "@/lib/auth/session";
import {
  blacklistUser,
  listPermissions,
  listRoles,
  listUsers,
  restoreUser,
  saveRolePermissions,
  type UserFilters,
} from "./api";

export const userKeys = {
  list: (filters: UserFilters) => ["users", filters] as const,
};

export const roleKeys = {
  list: ["roles"] as const,
  permissions: ["permissions"] as const,
};

export function useUsers(filters: UserFilters) {
  return useQuery({
    queryKey: userKeys.list(filters),
    queryFn: () => listUsers(filters),
    placeholderData: (prev) => prev,
  });
}

export function useBlacklistUser() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) => blacklistUser(id, reason),
    onSuccess: () => {
      toast.success("已拉黑，借阅权限即时冻结");
      void client.invalidateQueries({ queryKey: ["users"] });
    },
  });
}

export function useRestoreUser() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => restoreUser(id),
    onSuccess: () => {
      toast.success("已恢复借阅权限");
      void client.invalidateQueries({ queryKey: ["users"] });
    },
  });
}

export function useRoles() {
  return useQuery({ queryKey: roleKeys.list, queryFn: listRoles });
}

export function usePermissionCatalog() {
  return useQuery({
    queryKey: roleKeys.permissions,
    queryFn: listPermissions,
    staleTime: 10 * 60 * 1000,
  });
}

export function useSaveRolePermissions() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: ({ code, permissions }: { code: string; permissions: string[] }) =>
      saveRolePermissions(code, permissions),
    onSuccess: async () => {
      toast.success("角色权限已保存，下一次请求即生效");
      await client.invalidateQueries({ queryKey: roleKeys.list });
      // 自己所在角色可能被改，刷新会话里的权限，菜单与按钮立即收敛
      const me = await fetchMe();
      updateUser({ permissions: me.permissions, role: me.role });
      await client.invalidateQueries({ queryKey: roleKeys.list });
    },
  });
}