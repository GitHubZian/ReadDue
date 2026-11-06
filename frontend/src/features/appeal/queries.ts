import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { ackAppeal, decideAppeal, listAppeals, type AppealAction, type AppealFilters } from "./api";
import { dashboardKey } from "@/features/workbench/queries";

export const appealKeys = {
  list: (filters: AppealFilters) => ["appeals", filters] as const,
  detail: (id: string) => ["appeals", "detail", id] as const,
};

export function useAppeals(filters: AppealFilters) {
  return useQuery({
    queryKey: appealKeys.list(filters),
    queryFn: () => listAppeals(filters),
    placeholderData: (prev) => prev,
  });
}

function useInvalidate() {
  const client = useQueryClient();
  return () => {
    void client.invalidateQueries({ queryKey: ["appeals"] });
    void client.invalidateQueries({ queryKey: dashboardKey });
  };
}

export function useAckAppeal() {
  const invalidate = useInvalidate();
  return useMutation({
    mutationFn: (id: string) => ackAppeal(id),
    onSuccess: () => {
      toast.success("已受理，工单进入核查");
      invalidate();
    },
  });
}

export function useDecideAppeal() {
  const invalidate = useInvalidate();
  return useMutation({
    mutationFn: ({ id, action, note, days }: { id: string; action: AppealAction; note: string; days?: number }) =>
      decideAppeal(id, action, { note, days }),
    onSuccess: () => {
      toast.success("结论已保存并同步通知读者");
      invalidate();
    },
  });
}
