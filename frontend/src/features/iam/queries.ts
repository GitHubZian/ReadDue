import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { blacklistUser, listUsers, restoreUser, type UserFilters } from "./api";

export const userKeys = {
  list: (filters: UserFilters) => ["users", filters] as const,
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
