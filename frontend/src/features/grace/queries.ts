import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { approveGrace, listGraces, rejectGrace, type GraceFilters } from "./api";
import { dashboardKey } from "@/features/workbench/queries";

export const graceKeys = {
  list: (filters: GraceFilters) => ["graces", filters] as const,
  detail: (id: string) => ["graces", "detail", id] as const,
};

export function useGraces(filters: GraceFilters) {
  return useQuery({
    queryKey: graceKeys.list(filters),
    queryFn: () => listGraces(filters),
    placeholderData: (prev) => prev,
  });
}

function useInvalidate() {
  const client = useQueryClient();
  return () => {
    void client.invalidateQueries({ queryKey: ["graces"] });
    void client.invalidateQueries({ queryKey: dashboardKey });
  };
}

export function useApproveGrace() {
  const invalidate = useInvalidate();
  return useMutation({
    mutationFn: ({ id, days, note }: { id: string; days: number; note?: string }) =>
      approveGrace(id, { days, note }),
    onSuccess: (record) => {
      toast.success(`已通过，宽限 ${record.approvedDays} 天`);
      invalidate();
    },
  });
}

export function useRejectGrace() {
  const invalidate = useInvalidate();
  return useMutation({
    mutationFn: ({ id, note }: { id: string; note: string }) => rejectGrace(id, { note }),
    onSuccess: () => {
      toast.success("已驳回并通知读者");
      invalidate();
    },
  });
}
