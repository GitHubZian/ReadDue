import { useQuery } from "@tanstack/react-query";
import { listAuditLogs, type AuditFilters } from "./api";

export const auditKeys = {
  list: (filters: AuditFilters) => ["audit", filters] as const,
};

export function useAuditLogs(filters: AuditFilters) {
  return useQuery({
    queryKey: auditKeys.list(filters),
    queryFn: () => listAuditLogs(filters),
    placeholderData: (prev) => prev,
  });
}
