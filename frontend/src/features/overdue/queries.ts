import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { listOverdue, remindOverdue, type OverdueFilters } from "./api";

export const overdueKeys = {
  list: (filters: OverdueFilters) => ["overdue", filters] as const,
};

export function useOverdue(filters: OverdueFilters) {
  return useQuery({
    queryKey: overdueKeys.list(filters),
    queryFn: () => listOverdue(filters),
    placeholderData: (prev) => prev,
  });
}

export function useRemindOverdue() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (ids: string[]) => remindOverdue(ids),
    onSuccess: (result) => {
      toast.success(`已发送 ${result.sent} 条提醒`, {
        description: "同一读者 24 小时内只发一次；22:00–08:00 顺延至次日 08:00。",
      });
      void client.invalidateQueries({ queryKey: ["overdue"] });
    },
  });
}
